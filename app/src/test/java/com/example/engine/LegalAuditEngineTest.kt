package com.example.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegalAuditEngineTest {

    @Test
    fun testSeverity3Floor_guaranteesHighRiskScore65OrHigher() {
        // A single severity 3 flag (e.g. Non-Refundable Deposit or Liquidated Damages)
        val sev3Flags = listOf(
            MatchedRedFlag(
                category = "NON_REFUNDABLE_DEPOSIT",
                displayName = "Non-Refundable Deposit",
                matchedSnippet = "deposit is strictly non-refundable",
                severity = 3,
                explanation = "Implies deposit is forfeited under all conditions."
            )
        )
        val missingSafeguards = emptyList<String>()

        val score = LegalAuditEngine.calculateRiskScore(sev3Flags, missingSafeguards)

        // Must enforce floor >= 65
        assertTrue(
            "Overall risk score must be >= 65 for severity 3 predatory clauses (was $score)",
            score >= 65
        )
    }

    @Test
    fun testPredatoryLeaseScenario_strictlyNonRefundableDepositLiquidatedDamages() {
        // Lease with strictly non-refundable deposit + liquidated damages + missing safeguards
        val flags = listOf(
            MatchedRedFlag(
                category = "NON_REFUNDABLE_DEPOSIT",
                displayName = "Non-Refundable Deposit",
                matchedSnippet = "deposit is strictly non-refundable",
                severity = 3,
                explanation = "Non-refundable deposit."
            ),
            MatchedRedFlag(
                category = "EARLY_TERMINATION_PENALTY",
                displayName = "Early Termination Penalty",
                matchedSnippet = "pay 6 months liquidated damages",
                severity = 3,
                explanation = "Liquidated damages penalty."
            )
        )
        val missingSafeguards = listOf(
            "Notice to Cure / Default Period",
            "Mutual Termination Rights",
            "Right to Quiet Enjoyment"
        )

        val score = LegalAuditEngine.calculateRiskScore(flags, missingSafeguards)

        // Must be deep in the High Risk / Danger zone (>= 75)
        assertTrue(
            "Predatory lease score must be >= 75 (was $score)",
            score >= 75
        )
    }

    @Test
    fun testCleanContract_scoresLowRisk() {
        val noFlags = emptyList<MatchedRedFlag>()
        val oneMissingSafeguard = listOf("Right to Quiet Enjoyment")

        val score = LegalAuditEngine.calculateRiskScore(noFlags, oneMissingSafeguard)

        // 5 points for 1 missing safeguard -> clean low risk
        assertEquals(5, score)
        assertTrue("Clean contract must score <= 30", score <= 30)
    }

    @Test
    fun testMissingSafeguardsCap_doesNotExceedMaxSafeguardPenalty() {
        val noFlags = emptyList<MatchedRedFlag>()
        val manyMissingSafeguards = listOf(
            "Notice to Cure / Default Period",
            "Right to Quiet Enjoyment",
            "Mutual Termination Rights",
            "Security Deposit Return Timeline",
            "Landlord Maintenance Obligations",
            "Habitability Warranty",
            "Limitation of Liability"
        )

        val score = LegalAuditEngine.calculateRiskScore(noFlags, manyMissingSafeguards)

        // Capped at 20 points max (4 safeguards * 5 = 20 max)
        assertEquals(20, score)
    }

    @Test
    fun testDecoupledWeighting_predatoryDominatesSafeguards() {
        val minorFlag = listOf(
            MatchedRedFlag(
                category = "LATE_FEE_CLAUSE",
                displayName = "Late Fee Clause",
                matchedSnippet = "5% late fee after 5 days",
                severity = 2,
                explanation = "Late fee."
            )
        )
        val missingSafeguards = listOf("Notice to Cure / Default Period")

        // 15 (severity 2) + 5 (1 missing safeguard) = 20
        val score = LegalAuditEngine.calculateRiskScore(minorFlag, missingSafeguards)
        assertEquals(20, score)
    }
}
