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
    val overallRiskScore: Int = calculateRiskScore(matchedRedFlags, missingMandatoryClauses),
    val contractType: ContractType = ContractType.GENERAL_AGREEMENT
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

        /**
         * Category keyword weight maps for confidence-weighted classification.
         * Stronger legal indicators are assigned higher weights.
         * Evaluated strictly with case-insensitive word-boundary matching.
         */
        val CATEGORY_KEYWORD_WEIGHTS: Map<ContractType, Map<String, Int>> = mapOf(
            ContractType.RESIDENTIAL_LEASE to mapOf(
                "landlord" to 4,
                "tenant" to 4,
                "eviction" to 4,
                "premises" to 3,
                "lease" to 3,
                "lessor" to 3,
                "lessee" to 3,
                "sublease" to 3,
                "security deposit" to 3,
                "habitability" to 3,
                "dwelling" to 3,
                "rent" to 2
            ),
            ContractType.EMPLOYMENT_FREELANCE to mapOf(
                "contractor" to 4,
                "deliverables" to 4,
                "intellectual property" to 4,
                "scope of work" to 4,
                "independent contractor" to 4,
                "work product" to 3,
                "client" to 3,
                "employee" to 3,
                "non-compete" to 3,
                "statement of work" to 4,
                "invoicing" to 2,
                "hourly rate" to 3
            ),
            ContractType.MEMBERSHIP_SUBSCRIPTION to mapOf(
                "subscription" to 4,
                "auto-renewal" to 4,
                "membership" to 4,
                "terms of service" to 4,
                "subscriber" to 4,
                "recurring fee" to 3,
                "billing cycle" to 3,
                "facility" to 3,
                "member" to 3,
                "cancellation policy" to 3
            )
        )

        /**
         * Builds a regex with word boundaries for keywords, including hyphen and multi-word handling.
         */
        fun toWordBoundaryRegex(keyword: String): Regex {
            val normalized = keyword.trim()
            val pattern = when {
                normalized.contains("-") -> {
                    val parts = normalized.split("-").map { Regex.escape(it) }
                    "\\b" + parts.joinToString("[- ]?") + "\\b"
                }
                normalized.contains(" ") -> {
                    val parts = normalized.split(Regex("\\s+")).map { Regex.escape(it) }
                    "\\b" + parts.joinToString("\\s+") + "\\b"
                }
                else -> "\\b" + Regex.escape(normalized) + "\\b"
            }
            return Regex(pattern, RegexOption.IGNORE_CASE)
        }

        /**
         * Confidence-weighted classifier.
         * Evaluates raw document text against weighted keyword maps using word boundaries.
         * Requires minimum absolute score (>= 6), >= 2 distinct keyword hits, and >= 1.5x lead
         * over the second-place category. Returns GENERAL_AGREEMENT if thresholds are not met.
         */
        fun classifyContract(rawText: String): ContractType {
            if (rawText.isBlank()) return ContractType.GENERAL_AGREEMENT

            val scores = mutableMapOf<ContractType, Int>()
            val hitCounts = mutableMapOf<ContractType, Int>()

            for ((type, weightMap) in CATEGORY_KEYWORD_WEIGHTS) {
                var totalScore = 0
                var distinctHits = 0
                for ((keyword, weight) in weightMap) {
                    val regex = toWordBoundaryRegex(keyword)
                    if (regex.containsMatchIn(rawText)) {
                        totalScore += weight
                        distinctHits++
                    }
                }
                scores[type] = totalScore
                hitCounts[type] = distinctHits
            }

            // Sort descending by score
            val sorted = scores.entries.sortedByDescending { it.value }
            val top = sorted.firstOrNull() ?: return ContractType.GENERAL_AGREEMENT
            val runnerUp = sorted.getOrNull(1)

            val topScore = top.value
            val topHits = hitCounts[top.key] ?: 0
            val runnerUpScore = runnerUp?.value ?: 0

            // Minimum absolute score >= 6 AND distinct hits >= 2 (single keyword hit never classifies)
            if (topScore >= 6 && topHits >= 2) {
                // Margin check: must lead runner-up by at least 1.5x
                if (runnerUpScore == 0 || topScore.toDouble() >= runnerUpScore * 1.5) {
                    return top.key
                }
            }

            return ContractType.GENERAL_AGREEMENT
        }
    }

    // Standard mandatory safeguards strictly scoped by ContractType
    val leaseSafeguards = listOf(
        "Notice to Cure / Default Period",
        "Right to Quiet Enjoyment",
        "Security Deposit Return Timeline",
        "Landlord Maintenance Obligations",
        "Mutual Termination Rights"
    )

    val freelanceSafeguards = listOf(
        "Clear Payment Schedule",
        "Mutual Termination Rights",
        "IP Retention Until Payment",
        "Cap on Aggregate Liability"
    )

    val subscriptionSafeguards = listOf(
        "Easy Cancellation Method",
        "Transparent Pricing"
    )

    val generalSafeguards = listOf(
        "Clear Termination Terms",
        "Dispute Resolution Process"
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

    /**
     * Confidence-weighted classifier (member delegation to Companion).
     */
    fun classifyContract(rawText: String): ContractType = Companion.classifyContract(rawText)

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
        contractType: ContractType = ContractType.GENERAL_AGREEMENT
    ): AuditResult = withContext(Dispatchers.Default) {
        // "Classify First, Analyze Second"
        // If caller passed GENERAL_AGREEMENT (default), run the confidence-weighted classifier.
        val resolvedType = if (contractType != ContractType.GENERAL_AGREEMENT) {
            contractType
        } else {
            classifyContract(rawText)
        }

        val matchedRedFlags = mutableListOf<MatchedRedFlag>()
        
        matchedRedFlags.addAll(performEnhancedRuleCheck(rawText, resolvedType))
        
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

        // Logic for missing safeguards (Opt-In, filtered strictly by ContractType):
        val missingClauses = performOptInSafeguardCheck(rawText, resolvedType)

        val isHighRisk = matchedRedFlags.any { it.severity == 3 }

        AuditResult(
            missingMandatoryClauses = missingClauses,
            matchedRedFlags = matchedRedFlags,
            isHighRisk = isHighRisk,
            overallRiskScore = AuditResult.calculateRiskScore(matchedRedFlags, missingClauses),
            contractType = resolvedType
        )
    }

    suspend fun analyzeContract(
        rawText: String,
        country: String = "US",
        contractType: String
    ): AuditResult = analyzeContract(rawText, country, ContractType.fromString(contractType))

    private fun performOptInSafeguardCheck(text: String, contractType: ContractType): List<String> {
        val lowerText = text.lowercase()
        val missing = mutableListOf<String>()
        val safeguards = when (contractType) {
            ContractType.RESIDENTIAL_LEASE -> leaseSafeguards
            ContractType.EMPLOYMENT_FREELANCE -> freelanceSafeguards
            ContractType.MEMBERSHIP_SUBSCRIPTION -> subscriptionSafeguards
            ContractType.GENERAL_AGREEMENT -> generalSafeguards
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
                // --- Employment / Freelance Safeguards ---
                "Clear Payment Schedule" -> {
                    listOf(
                        "payment terms", "net 30", "net 15", "net 60", "invoicing", "invoice within",
                        "payment schedule", "milestone payment", "due upon receipt", "payment within",
                        "compensation schedule", "billing schedule"
                    ).any { lowerText.contains(it) }
                }
                "Mutual Termination Rights" -> {
                    listOf(
                        "mutual termination", "either party may terminate", "mutual agreement",
                        "mutual written consent", "terminate by mutual", "termination by either party",
                        "both parties agree in writing", "either party may cancel", "mutual written notice",
                        "written notice of termination", "termination for convenience", "days written notice"
                    ).any { lowerText.contains(it) }
                }
                "IP Retention Until Payment" -> {
                    listOf(
                        "upon full payment", "upon receipt of payment", "retention of title",
                        "assignment effective upon payment", "pre-existing materials", "contractor retains",
                        "transfers upon full payment", "ownership transfers upon payment"
                    ).any { lowerText.contains(it) }
                }
                "Cap on Aggregate Liability" -> {
                    listOf(
                        "limitation of liability", "cap on liability", "aggregate liability",
                        "total liability shall not exceed", "fees paid under", "in no event shall liability exceed"
                    ).any { lowerText.contains(it) }
                }
                // --- Membership / Subscription Safeguards ---
                "Easy Cancellation Method" -> {
                    listOf(
                        "cancel online", "cancellation method", "cancel at any time", "how to cancel",
                        "cancel your subscription", "written notice to cancel", "cancellation portal",
                        "cancel membership", "opt out of renewal", "turn off auto-renewal"
                    ).any { lowerText.contains(it) }
                }
                "Transparent Pricing" -> {
                    listOf(
                        "transparent pricing", "breakdown of fees", "no hidden fees", "fee schedule",
                        "clear pricing", "recurring charge amount", "total cost", "billing amount",
                        "subscription fee of", "monthly rate of", "annual rate of"
                    ).any { lowerText.contains(it) }
                }
                // --- General Agreement Safeguards ---
                "Clear Termination Terms" -> {
                    listOf(
                        "termination", "term and termination", "terminate this agreement",
                        "right to terminate", "expiration of the term", "written notice to terminate"
                    ).any { lowerText.contains(it) }
                }
                "Dispute Resolution Process" -> {
                    listOf(
                        "dispute resolution", "governing law", "mediation", "arbitration",
                        "jurisdiction", "good faith negotiation", "venue"
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

    private fun performEnhancedRuleCheck(text: String, contractType: ContractType): List<MatchedRedFlag> {
        val lowerText = text.lowercase()
        val flags = mutableListOf<MatchedRedFlag>()
        
        // Universal Rules (all contract types)
        val universalRules = listOf(
            Rule(listOf("modify building policies", "right to change fees", "sole discretion to amend", "modify rules without notice", "modify terms without notice", "change terms at any time without notice"), 
                 "Unilateral Modification", 3, "Allows the other party to change terms or fees without your consent."),
            Rule(listOf("waives any claim", "consequential damages", "hold harmless", "waive all claims", "indemnify and hold harmless", "unconscionable liability"), 
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

        // Freelance / Employment Rules
        val freelanceRules = listOf(
            Rule(listOf("sole subjective discretion", "withhold payments indefinitely", "withhold payment at client's discretion", "in its sole and absolute discretion determine payment"),
                 "Subjective Payment Discretion", 3, "Allows client to withhold payments based on subjective discretion rather than objective completion."),
            Rule(listOf("waives all moral rights", "waive moral rights", "exclusive ownership prior to payment", "irrevocable assignment prior to full payment", "work for hire regardless of payment"),
                 "Premature IP Assignment", 3, "Forces surrender of moral rights or transfers intellectual property prior to full payment."),
            Rule(listOf("unlimited liability", "indemnify against all claims", "indemnify client without cap", "uncapped indemnification"),
                 "Uncapped Liability", 3, "Exposes you to unlimited financial liability without a cap on contract value."),
            Rule(listOf("worldwide non-compete", "shall not engage in any competing", "for a period of 2 years post", "restriction on all competitors"),
                 "Overbroad Non-Compete", 3, "Excessive restriction on future clients or independent livelihood.")
        )

        // Subscription / Membership Rules
        val subscriptionRules = listOf(
            Rule(listOf("automatic renewal for successive one-year terms", "automatically renews for one year", "successive one-year terms", "automatically renews indefinitely", "evergreen contract", "renews without prior written notice"),
                 "Automatic Renewal Trap", 3, "Locks you into successive full-term renewals without requiring affirmative confirmation."),
            Rule(listOf("mandatory early termination fee", "early cancellation penalty", "cancellation processing fee", "penalty for cancellation"),
                 "Mandatory Cancellation Fee", 3, "Imposes penalties or termination fees for ending a subscription."),
            Rule(listOf("waive all rights to sue", "waives any right to bring a class action", "no court proceedings"),
                 "Class Action & Litigation Waiver", 3, "Strips your right to seek judicial remedy or join collective consumer actions.")
        )

        val activeRules = when (contractType) {
            ContractType.RESIDENTIAL_LEASE -> universalRules + leaseRules
            ContractType.EMPLOYMENT_FREELANCE -> universalRules + freelanceRules
            ContractType.MEMBERSHIP_SUBSCRIPTION -> universalRules + subscriptionRules
            ContractType.GENERAL_AGREEMENT -> universalRules
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
