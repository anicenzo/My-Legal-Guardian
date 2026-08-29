package com.example.engine

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.InterpreterApi
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

data class MatchedRedFlag(
    val category: String,
    val displayName: String,
    val matchedSnippet: String,
    val severity: Int,
    val explanation: String
)

data class AuditResult(
    val missingMandatoryClauses: List<String>,
    val matchedRedFlags: List<MatchedRedFlag>,
    val isHighRisk: Boolean
) {
    val predatoryClauseCount: Int get() = matchedRedFlags.size
    val predatoryClausesFound: Int get() = matchedRedFlags.size
    val missingClauses: List<String> get() = missingMandatoryClauses
}

class LegalAuditEngine(private val context: Context) {
    private var interpreter: InterpreterApi? = null
    private val vocabMap = mutableMapOf<String, Int>()

    // Standard mandatory safeguards (Opt-In Detection)
    private val mandatorySafeguards = listOf(
        "Notice to Cure / Default Period",
        "Right to Quiet Enjoyment",
        "Mutual Termination Rights",
        "Security Deposit Return Timeline",
        "Landlord Maintenance Obligations"
    )

    init {
        try {
            interpreter = InterpreterApi.create(loadModelFile(), InterpreterApi.Options())
            loadVocab()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadVocab() {
        try {
            val reader = context.assets.open("vocab.txt").bufferedReader()
            var index = 0
            reader.forEachLine { line ->
                val word = line.trim()
                if (word.isNotEmpty()) {
                    vocabMap[word] = index
                    index++
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun analyzeContract(rawText: String): AuditResult = withContext(Dispatchers.Default) {
        val matchedRedFlags = mutableListOf<MatchedRedFlag>()
        
        matchedRedFlags.addAll(performEnhancedRuleCheck(rawText))
        
        // Split text into simple paragraphs for Tier 2 evaluation
        val paragraphs = rawText.split("\n").filter { it.trim().length > 50 }

        for (paragraph in paragraphs) {
            if (performTier2TFLiteCheck(paragraph)) {
                matchedRedFlags.add(
                    MatchedRedFlag(
                        category = "TFLITE_MODEL_FLAG",
                        displayName = "AI Risk Detection",
                        matchedSnippet = if (paragraph.length > 100) paragraph.substring(0, 100) + "..." else paragraph,
                        severity = 2,
                        explanation = "The AI model detected potential predatory language."
                    )
                )
            }
        }

        // Logic for missing safeguards (Opt-In): 
        val missingClauses = performOptInSafeguardCheck(rawText)

        val isHighRisk = matchedRedFlags.any { it.severity == 3 }

        AuditResult(
            missingMandatoryClauses = missingClauses,
            matchedRedFlags = matchedRedFlags,
            isHighRisk = isHighRisk
        )
    }

    private fun performOptInSafeguardCheck(text: String): List<String> {
        val lowerText = text.lowercase()
        val missing = mutableListOf<String>()
        
        for (safeguard in mandatorySafeguards) {
            val isPresent = when(safeguard) {
                "Notice to Cure / Default Period" -> {
                    listOf(
                        "notice to cure", "default period", "days to remedy", "cure period",
                        "days to cure", "written notice of default", "cure any default",
                        "opportunity to cure", "cure within", "notice of breach and", "days after notice of default"
                    ).any { lowerText.contains(it) }
                }
                "Right to Quiet Enjoyment" -> {
                    listOf(
                        "quiet enjoyment", "peaceful enjoyment", "peaceful possession",
                        "quietly enjoy", "peaceable possession", "undisturbed possession",
                        "enjoyment of the premises", "peaceable enjoyment"
                    ).any { lowerText.contains(it) }
                }
                "Mutual Termination Rights" -> {
                    listOf(
                        "mutual termination", "either party may terminate", "mutual agreement",
                        "mutual written consent", "terminate by mutual", "termination by either party",
                        "both parties agree in writing", "either party may cancel", "mutual written notice"
                    ).any { lowerText.contains(it) }
                }
                "Security Deposit Return Timeline" -> {
                    listOf(
                        "return of security deposit", "deposit within", "refund deposit",
                        "return the security deposit", "deposit shall be returned", "deposit will be returned",
                        "deposit shall be refunded", "return of the deposit", "refund of security deposit",
                        "return of deposit within", "days after vacating", "days of surrender"
                    ).any { lowerText.contains(it) }
                }
                "Landlord Maintenance Obligations" -> {
                    listOf(
                        "landlord shall maintain", "landlord's responsibility to repair", "maintain the premises",
                        "keep the premises in good repair", "landlord will maintain", "landlord agrees to maintain",
                        "warranty of habitability", "fit for habitation", "landlord is responsible for repairs",
                        "maintain the property in good", "repair structural", "plumbing and electrical"
                    ).any { lowerText.contains(it) }
                }
                else -> lowerText.contains(safeguard.lowercase())
            }
            
            if (!isPresent) {
                missing.add(safeguard)
            }
        }
        return missing
    }

    private data class Rule(val keywords: List<String>, val name: String, val severity: Int, val explanation: String)

    private fun performEnhancedRuleCheck(text: String): List<MatchedRedFlag> {
        val lowerText = text.lowercase()
        val flags = mutableListOf<MatchedRedFlag>()
        
        val rules = listOf(
            Rule(listOf("non-refundable", "forfeit deposit", "no refund", "forfeited"), 
                 "Non-Refundable Deposit", 3, "This clause implies you cannot get your money back under any circumstances."),
            Rule(listOf("early termination fee", "liquidated damages", "termination penalty"), 
                 "Early Termination Penalty", 3, "Imposes severe financial penalties for ending the contract early."),
            Rule(listOf("months' rent"), 
                 "Multi-Month Penalty", 2, "Requires paying multiple months of rent if terminated early."),
            Rule(listOf("late fee", "5 days", "10%", "interest on late"), 
                 "Late Fee Clause", 2, "Specifies penalties or high interest for late payments."),
            Rule(listOf("modify building policies", "right to change fees", "sole discretion to amend"), 
                 "Unilateral Modification", 3, "Allows the landlord/provider to change rules or fees without your consent."),
            Rule(listOf("waives any claim", "consequential damages", "hold harmless"), 
                 "Liability Waiver", 3, "Forces you to give up your rights to sue or claim damages."),
            Rule(listOf("resolved exclusively through arbitration", "arbitrator selected by landlord", "class action waiver"), 
                 "Forced Arbitration", 3, "Prevents you from taking disputes to court, forcing private arbitration.")
        )
        
        for (rule in rules) {
            for (keyword in rule.keywords) {
                val idx = lowerText.indexOf(keyword)
                if (idx != -1) {
                    val start = maxOf(0, idx - 40)
                    val end = minOf(text.length, idx + keyword.length + 40)
                    val snippet = text.substring(start, end).replace('\n', ' ').trim()
                    
                    // Only add one flag per rule (avoid duplicates for same category)
                    val categoryName = rule.name.uppercase().replace(" ", "_").replace("-", "_")
                    if (flags.none { it.category == categoryName }) {
                        flags.add(
                            MatchedRedFlag(
                                category = categoryName,
                                displayName = rule.name,
                                matchedSnippet = "...$snippet...",
                                severity = rule.severity,
                                explanation = rule.explanation
                            )
                        )
                    }
                }
            }
        }
        return flags
    }

    private fun performTier2TFLiteCheck(paragraph: String): Boolean {
        if (interpreter == null || vocabMap.isEmpty()) return false

        val tokens = tokenize(paragraph.lowercase())
        val input = Array(1) { IntArray(128) }
        
        for (i in 0 until 128) {
            if (i < tokens.size) {
                input[0][i] = tokens[i]
            } else {
                input[0][i] = 0 // padding token
            }
        }

        val output = Array(1) { FloatArray(2) }
        
        try {
            interpreter?.run(input, output)
            val predatoryScore = output[0][1]
            return predatoryScore > 0.6f
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return false
    }

    private fun tokenize(text: String): List<Int> {
        val words = text.split("\\s+".toRegex())
        val tokenIds = mutableListOf<Int>()
        
        for (word in words) {
            val cleanWord = word.replace(Regex("[^a-z]"), "")
            if (cleanWord.isNotEmpty()) {
                val id = vocabMap[cleanWord] ?: vocabMap["[UNK]"] ?: 1
                tokenIds.add(id)
            }
        }
        return tokenIds
    }
    
    fun close() {
        interpreter?.close()
    }
}
