package com.example.engine

object NegotiationTemplateEngine {

    private val templates = mapOf(
        "Auto-Renewal / Silent Renewal Traps" to """
            Dear [Name],
            
            I am currently reviewing the contract we discussed and noticed the auto-renewal provision:
            "[CLAUSE_TEXT]"
            
            Automatic renewal provisions can lead to unintentional extensions and administrative overhead for both of us. Could we revise this section to require explicit, active written confirmation from both parties to renew the term?
            
            Additionally, I would appreciate a standard 30-day written notice window prior to any expiration so we can align on next steps.
            
            Best regards,
            [Your Name]
        """.trimIndent(),

        "Uncapped Liability & Indemnification" to """
            Dear [Name],
            
            I am reviewing our agreement and would like to propose a revision to the indemnification and liability clause:
            "[CLAUSE_TEXT]"
            
            As currently drafted, the liability is uncapped and highly one-sided. For a balanced partnership, could we make the indemnification obligations mutual? I would also like to add a standard liability cap, limited to the total fees paid under this agreement.
            
            This ensures both parties share proportional responsibility and reduces catastrophic risk.
            
            Best regards,
            [Your Name]
        """.trimIndent(),

        "Unilateral Termination or Modification Rights" to """
            Dear [Name],
            
            I noticed a provision in our contract regarding unilateral termination or modifications:
            "[CLAUSE_TEXT]"
            
            To maintain fairness and trust, I propose that any contract modifications or terminations without cause require mutual written agreement with at least 30 days of advance notice. This ensures we are both protected and can plan resources accordingly.
            
            Let me know if this adjustment is acceptable to you.
            
            Best regards,
            [Your Name]
        """.trimIndent(),

        "Non-Compete / Exclusivity Overreach" to """
            Dear [Name],
            
            I have a question regarding the non-compete and exclusivity covenants:
            "[CLAUSE_TEXT]"
            
            The current restriction is exceptionally broad and might impact my ongoing independent business activities. Could we limit the non-compete scope to apply strictly to direct competitors in our primary industry, and reduce the duration to 3 months post-contract?
            
            I want to ensure my career remains sustainable while fully respecting your proprietary interests.
            
            Best regards,
            [Your Name]
        """.trimIndent(),

        "Arbitration & Waiver of Legal Rights" to """
            Dear [Name],
            
            I am looking over the dispute resolution terms in our draft contract:
            "[CLAUSE_TEXT]"
            
            Mandatory arbitration can become disproportionately expensive and restrict access to normal local legal remedies. Could we revise this clause to specify that any disputes will first undergo standard good-faith mediation locally before moving to any formal proceedings?
            
            Let me know if we can specify our local municipal court system as the governing jurisdiction.
            
            Best regards,
            [Your Name]
        """.trimIndent(),

        "Hidden Fees & Penalties" to """
            Dear [Name],
            
            I noticed a penalty/fee provision in the contract regarding delayed performance or payments:
            "[CLAUSE_TEXT]"
            
            To make this terms more practical, I'd like to ask for a standard 5-day grace period before any late fees or liquidated damages are calculated. This protects against minor banking or shipping delays beyond our control.
            
            We always strive for timeliness, and a short buffer helps align our mutual risk.
            
            Best regards,
            [Your Name]
        """.trimIndent(),

        "IP / Data Ownership Grabs" to """
            Dear [Name],
            
            I want to touch base on the intellectual property assignment clause:
            "[CLAUSE_TEXT]"
            
            As an independent creator, I need to protect my pre-existing methodologies and tools. Could we clarify that I retain ownership of my 'background assets' and that the final work product IP transfers to you exclusively upon full and final payment of my invoices?
            
            This is standard practice and protects the integrity of our transaction.
            
            Best regards,
            [Your Name]
        """.trimIndent(),

        "Vague or Undefined Obligations" to """
            Dear [Name],
            
            I am reviewing the language concerning performance standards:
            "[CLAUSE_TEXT]"
            
            The terms 'sole discretion' or 'reasonable efforts' leave room for misinterpretation. Can we specify clear milestones or key performance indicators (KPIs) to establish clear objectives? 
            
            Explicit guidelines will help us both succeed and avoid any subjective disagreement down the road.
            
            Best regards,
            [Your Name]
        """.trimIndent()
    )

    private val genericTemplate = """
        Dear [Name],
        
        I am currently reviewing the contract draft we discussed. I noticed a few dense clauses regarding terms, risks, and liability, specifically:
        "[CLAUSE_TEXT]"
        
        Before signing, I would like to schedule a brief call to go over these points and discuss a few standard, mutually-protective adjustments to make our agreement more balanced.
        
        Please let me know when you are free this week.
        
        Best regards,
        [Your Name]
    """.trimIndent()

    private fun resolveTemplate(categoryOrName: String?): String {
        if (categoryOrName == null) return genericTemplate
        templates[categoryOrName]?.let { return it }

        val lower = categoryOrName.lowercase().replace("_", " ").replace("-", " ")
        return when {
            lower.contains("arbitrat") || lower.contains("dispute") || lower.contains("court") -> 
                templates["Arbitration & Waiver of Legal Rights"] ?: genericTemplate
            lower.contains("liab") || lower.contains("indemn") || lower.contains("waiver") || lower.contains("hold harmless") -> 
                templates["Uncapped Liability & Indemnification"] ?: genericTemplate
            lower.contains("terminat") || lower.contains("modifi") || lower.contains("amend") || lower.contains("discretion") -> 
                templates["Unilateral Termination or Modification Rights"] ?: genericTemplate
            lower.contains("fee") || lower.contains("penalty") || lower.contains("deposit") || lower.contains("refund") || lower.contains("rent") -> 
                templates["Hidden Fees & Penalties"] ?: genericTemplate
            lower.contains("renew") || lower.contains("duration") || lower.contains("extension") -> 
                templates["Auto-Renewal / Silent Renewal Traps"] ?: genericTemplate
            lower.contains("compete") || lower.contains("exclusiv") -> 
                templates["Non-Compete / Exclusivity Overreach"] ?: genericTemplate
            lower.contains("ip") || lower.contains("intellectual") || lower.contains("copyright") || lower.contains("ownership") -> 
                templates["IP / Data Ownership Grabs"] ?: genericTemplate
            else -> genericTemplate
        }
    }

    /**
     * Resolves the appropriate salutation for the contract category.
     */
    fun getSalutation(contractType: ContractType): String = when (contractType) {
        ContractType.RESIDENTIAL_LEASE -> "Dear [Landlord / Property Manager],"
        ContractType.EMPLOYMENT_FREELANCE -> "Dear [Client / Hiring Manager],"
        ContractType.MEMBERSHIP_SUBSCRIPTION -> "Dear [Service Provider / Management],"
        ContractType.GENERAL_AGREEMENT -> "To Whom It May Concern,"
    }

    /**
     * Generates a negotiation draft email text based on the category, isPro status, and contractType.
     */
    fun generateDraft(
        category: String?,
        clauseText: String,
        isPro: Boolean = true,
        contractType: ContractType = ContractType.GENERAL_AGREEMENT
    ): String {
        val baseTemplate = if (isPro) {
            resolveTemplate(category)
        } else {
            genericTemplate
        }

        val salutation = getSalutation(contractType)

        return baseTemplate
            .replace("Dear [Name],", salutation)
            .replace("[CLAUSE_TEXT]", clauseText)
            .replace("[Name]", "Partner / Landlord")
            .replace("[Your Name]", "Contract Signee")
    }

    /**
     * Generates a complete combined email for multiple selected red flag clauses.
     */
    fun generateCombinedEmail(
        selectedFlags: List<MatchedRedFlag>,
        isPro: Boolean = true,
        contractType: ContractType = ContractType.GENERAL_AGREEMENT
    ): String {
        if (selectedFlags.isEmpty()) return ""

        val clausePoints = selectedFlags.joinToString("\n\n") { flag ->
            val snippet = if (flag.matchedSnippet.isNotBlank()) "\n   Quote: \"${flag.matchedSnippet.trim()}\"" else ""
            "• ${flag.displayName}:${snippet}\n   Concern: ${flag.explanation}"
        }

        val salutation = getSalutation(contractType)

        return """
Subject: Proposed Amendments to Contract Draft

$salutation

I am currently reviewing our proposed contract draft. Before proceeding with signing, I would like to request clarification and standard mutual adjustments regarding the following provisions:

$clausePoints

I respectfully propose that we adjust these clauses to ensure balanced protections and clear obligations for both parties. Please let me know when you are available to align on these updates.

Best regards,
[Your Name]
        """.trimIndent()
    }
}
