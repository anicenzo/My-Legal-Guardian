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
    val isHighRisk: Boolean,
    val overallRiskScore: Int = calculateRiskScore(matchedRedFlags, missingMandatoryClauses)
) {
    val predatoryClauseCount: Int get() = matchedRedFlags.size
    val predatoryClausesFound: Int get() = matchedRedFlags.size
    val missingClauses: List<String> get() = missingMandatoryClauses

    companion object {
        fun calculateRiskScore(
            matchedRedFlags: List<MatchedRedFlag>,
            missingMandatoryClauses: List<String>
        ): Int {
            if (matchedRedFlags.isEmpty() && missingMandatoryClauses.isEmpty()) {
                return 0
            }

            // 1. Predatory clauses dominate the score
            val predatoryScore = matchedRedFlags.sumOf { flag ->
                when (flag.severity) {
                    3 -> 35
                    2 -> 15
                    else -> 8
                }
            }

            // 2. Missing mandatory safeguards act as secondary modifiers (capped at 20)
            val missingSafeguardsScore = minOf(20, missingMandatoryClauses.size * 5)

            val baseScore = predatoryScore + missingSafeguardsScore

            // 3. Severity floor: if any detected clause is tagged HIGH severity (severity >= 3)
            // in financial-forfeiture family or high-risk category, score cannot resolve below 65 (High Risk band)
            val hasHighSeverity = matchedRedFlags.any { it.severity >= 3 }
            val scoreWithFloor = if (hasHighSeverity) {
                maxOf(65, baseScore)
            } else {
                baseScore
            }

            return scoreWithFloor.coerceIn(0, 100)
        }
    }
}

class LegalAuditEngine(private val context: Context) {
    private var interpreter: InterpreterApi? = null
    private val vocabMap = mutableMapOf<String, Int>()

    companion object {
        const val MIN_VOCAB_SIZE_FOR_TIER2 = 500
        private const val TAG = "LegalAuditEngine"

        fun calculateRiskScore(
            matchedRedFlags: List<MatchedRedFlag>,
            missingMandatoryClauses: List<String>
        ): Int = AuditResult.calculateRiskScore(matchedRedFlags, missingMandatoryClauses)
    }

    // Standard mandatory safeguards by contract type (Opt-In Detection)
    private val leaseSafeguards = listOf(
        "Notice to Cure / Default Period",
        "Right to Quiet Enjoyment",
        "Mutual Termination Rights",
        "Security Deposit Return Timeline",
        "Landlord Maintenance Obligations"
    )

    private val freelanceSafeguards = listOf(
        "Payment Terms / Late Payment Fee",
        "IP Ownership Assigned Upon Payment",
        "Mutual Termination & Notice Window",
        "Cap on Aggregate Liability",
        "Scope of Work & Revision Terms"
    )

    private val employmentSafeguards = listOf(
        "Written Termination Notice Period",
        "Reasonable Non-Compete Scope",
        "Invention Assignment Carve-Out",
        "Mutual Confidentiality Protections",
        "Dispute Resolution in Local Court"
    )

    init {
        try {
            loadVocab()
            if (vocabMap.size < MIN_VOCAB_SIZE_FOR_TIER2) {
                android.util.Log.w(
                    TAG,
                    "Startup Sanity Check: vocab.txt contains ${vocabMap.size} entries (< $MIN_VOCAB_SIZE_FOR_TIER2 minimum). Tier-2 ML model is disabled; audit will rely 100% on Tier-1 deterministic legal rules."
                )
                interpreter = null
            } else {
                interpreter = InterpreterApi.create(loadModelFile(), InterpreterApi.Options())
            }
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Tier-2 ML initialization bypassed: ${e.message}. Using Tier-1 deterministic legal rules.")
            interpreter = null
        }
    }

    fun isTier2Active(): Boolean = interpreter != null && vocabMap.size >= MIN_VOCAB_SIZE_FOR_TIER2

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
            android.util.Log.w(TAG, "Could not load vocab.txt: ${e.message}")
        }
    }

    suspend fun analyzeContract(
        rawText: String,
        country: String = "US",
        contractType: String = "lease"
    ): AuditResult = withContext(Dispatchers.Default) {
        val matchedRedFlags = mutableListOf<MatchedRedFlag>()
        
        matchedRedFlags.addAll(performEnhancedRuleCheck(rawText, contractType))
        
        // Tier 2 ML Evaluation (Only if active and vocabulary is verified)
        if (isTier2Active()) {
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
        }

        // Logic for missing safeguards (Opt-In):
        val missingClauses = performOptInSafeguardCheck(rawText, contractType)

        val isHighRisk = matchedRedFlags.any { it.severity == 3 }

        AuditResult(
            missingMandatoryClauses = missingClauses,
            matchedRedFlags = matchedRedFlags,
            isHighRisk = isHighRisk
        )
    }

    private fun performOptInSafeguardCheck(text: String, contractType: String): List<String> {
        val lowerText = text.lowercase()
        val missing = mutableListOf<String>()
        val safeguards = when (contractType.lowercase()) {
            "freelance", "contractor" -> freelanceSafeguards
            "employment" -> employmentSafeguards
            else -> leaseSafeguards
        }
        
        for (safeguard in safeguards) {
            val isPresent = when(safeguard) {
                // --- Lease Safeguards ---
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
                // --- Freelance / Contractor Safeguards ---
                "Payment Terms / Late Payment Fee" -> {
                    listOf(
                        "payment terms", "net 30", "net 15", "net 60", "invoicing", "invoice within",
                        "interest on late payments", "late payment fee", "milestone payment", "due upon receipt"
                    ).any { lowerText.contains(it) }
                }
                "IP Ownership Assigned Upon Payment" -> {
                    listOf(
                        "upon full payment", "upon receipt of payment", "retention of title",
                        "assignment effective upon payment", "pre-existing materials", "contractor retains"
                    ).any { lowerText.contains(it) }
                }
                "Mutual Termination & Notice Window" -> {
                    listOf(
                        "either party may terminate", "written notice of termination", "kill fee",
                        "terminate upon", "days written notice", "termination for convenience"
                    ).any { lowerText.contains(it) }
                }
                "Cap on Aggregate Liability" -> {
                    listOf(
                        "limitation of liability", "cap on liability", "aggregate liability",
                        "total liability shall not exceed", "fees paid under", "in no event shall liability exceed"
                    ).any { lowerText.contains(it) }
                }
                "Scope of Work & Revision Terms" -> {
                    listOf(
                        "scope of work", "change order", "additional revisions",
                        "written amendment", "specifications", "deliverables schedule"
                    ).any { lowerText.contains(it) }
                }
                // --- Employment Safeguards ---
                "Written Termination Notice Period" -> {
                    listOf("notice period", "weeks notice", "severance", "written notice of termination").any { lowerText.contains(it) }
                }
                "Reasonable Non-Compete Scope" -> {
                    listOf("geographic limitation", "compete within", "months following termination").any { lowerText.contains(it) }
                }
                "Invention Assignment Carve-Out" -> {
                    listOf("prior inventions", "excluded inventions", "personal time and resources").any { lowerText.contains(it) }
                }
                "Mutual Confidentiality Protections" -> {
                    listOf("mutual non-disclosure", "confidential information", "both parties agree to hold").any { lowerText.contains(it) }
                }
                "Dispute Resolution in Local Court" -> {
                    listOf("governing law", "exclusive jurisdiction", "courts of").any { lowerText.contains(it) }
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

    private fun performEnhancedRuleCheck(text: String, contractType: String): List<MatchedRedFlag> {
        val lowerText = text.lowercase()
        val flags = mutableListOf<MatchedRedFlag>()
        
        // Universal Rules (all contract types)
        val universalRules = listOf(
            Rule(listOf("modify building policies", "right to change fees", "sole discretion to amend", "modify rules without notice", "modify terms without notice"), 
                 "Unilateral Modification", 3, "Allows the other party to change terms or fees without your consent."),
            Rule(listOf("waives any claim", "consequential damages", "hold harmless", "waive all claims", "indemnify and hold harmless"), 
                 "Liability Waiver", 3, "Forces you to give up your rights to sue or claim damages."),
            Rule(listOf("resolved exclusively through arbitration", "arbitrator selected by", "class action waiver", "waive right to jury trial"), 
                 "Forced Arbitration", 3, "Prevents you from taking disputes to court, forcing private arbitration."),
            Rule(listOf("late fee", "interest on late", "daily late charge"), 
                 "Late Fee Clause", 2, "Specifies penalties or high interest for delayed payments.")
        )

        // Lease-specific Rules
        val leaseRules = listOf(
            Rule(listOf("non-refundable", "strictly non-refundable", "nonrefundable", "forfeit deposit", "no refund", "forfeited", "deposit is non-refundable"), 
                 "Non-Refundable Deposit", 3, "This clause implies you cannot get your deposit back under any circumstances."),
            Rule(listOf("early termination fee", "liquidated damages", "termination penalty", "penalty for early termination", "break lease penalty"), 
                 "Early Termination Penalty", 3, "Imposes severe financial penalties for ending the lease early."),
            Rule(listOf("prepaid rent shall be forfeited", "forfeit prepaid rent", "forfeiture of advance rent"), 
                 "Prepaid Rent Forfeiture", 3, "Forces forfeiture of advance or prepaid rent upon departure."),
            Rule(listOf("months' rent", "months rent", "remaining months", "all remaining rent"), 
                 "Multi-Month Penalty", 3, "Requires paying multiple months of rent if terminated early.")
        )

        // Freelance / Commercial / Employment Rules
        val freelanceRules = listOf(
            Rule(listOf("unlimited liability", "indemnify against all claims", "indemnify client without cap", "uncapped indemnification"),
                 "Uncapped Liability", 3, "Exposes you to unlimited financial liability without a cap on contract value."),
            Rule(listOf("exclusive ownership prior to payment", "irrevocable assignment prior to full payment", "work for hire regardless of payment"),
                 "Premature IP Assignment", 3, "Transfers intellectual property before you have received full payment."),
            Rule(listOf("worldwide non-compete", "shall not engage in any competing", "for a period of 2 years post", "restriction on all competitors"),
                 "Overbroad Non-Compete", 3, "Excessive restriction on future clients or independent livelihood."),
            Rule(listOf("automatically renews indefinitely", "evergreen contract", "renews without prior written notice"),
                 "Silent Auto-Renewal Trap", 2, "Contracts that renew automatically without explicit affirmative confirmation.")
        )

        val activeRules = when (contractType.lowercase()) {
            "freelance", "contractor", "employment" -> universalRules + freelanceRules
            else -> universalRules + leaseRules
        }
        
        for (rule in activeRules) {
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
            if (com.example.BuildConfig.DEBUG) {
                android.util.Log.e("LegalAuditEngine", "TFLite inference failed: ${e.message}", e)
            }
            com.example.util.LocalErrorLogger.record(context, "LegalAuditEngine", "TFLite inference failed: ${e.message}", e)
        }
        
        return false
    }

    fun tokenize(text: String): List<Int> {
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

    @androidx.annotation.VisibleForTesting
    fun setVocabForTesting(vocab: Map<String, Int>) {
        vocabMap.clear()
        vocabMap.putAll(vocab)
    }
    
    fun close() {
        interpreter?.close()
    }
}
