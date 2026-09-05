package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.DocumentEntity
import com.example.engine.ContractType
import com.example.engine.LegalAuditEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LegalAuditRobolectricTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun testTier2SanityCheck_inactiveWithEmptyVocab() {
        val engine = LegalAuditEngine(context)
        // Since vocab.txt in assets has < 500 entries (only [UNK]), Tier 2 must be disabled
        assertFalse(
            "Tier-2 ML must be disabled when vocab contains fewer than 500 tokens",
            engine.isTier2Active()
        )
    }

    @Test
    fun testTokenizer_differentClausesProduceDifferentTokenSequences() {
        val engine = LegalAuditEngine(context)
        
        // Populate test vocabulary to verify tokenizer accuracy
        val testVocab = mapOf(
            "arbitration" to 10,
            "liability" to 11,
            "waiver" to 12,
            "deposit" to 20,
            "liquidated" to 21,
            "penalty" to 22
        )
        engine.setVocabForTesting(testVocab)

        val clause1 = "arbitration liability waiver"
        val clause2 = "deposit liquidated penalty"

        val tokens1 = engine.tokenize(clause1)
        val tokens2 = engine.tokenize(clause2)

        assertEquals(listOf(10, 11, 12), tokens1)
        assertEquals(listOf(20, 21, 22), tokens2)
        assertNotEquals("Different clauses must produce distinct token arrays", tokens1, tokens2)
    }

    @Test
    fun testContractTypeRouting_freelanceEvaluatesFreelanceSafeguards() = runBlocking {
        val engine = LegalAuditEngine(context)

        val sampleFreelanceContract = """
            INDEPENDENT CONTRACTOR AGREEMENT
            1. Services shall be performed in a professional manner.
            2. Confidential information will be maintained.
        """.trimIndent()

        val result = engine.analyzeContract(
            rawText = sampleFreelanceContract,
            country = "US",
            contractType = ContractType.EMPLOYMENT_FREELANCE
        )

        // Must evaluate freelance safeguards, NOT lease safeguards
        val missing = result.missingMandatoryClauses
        assertTrue(
            "Must flag missing freelance payment schedule",
            missing.contains("Clear Payment Schedule")
        )
        assertTrue(
            "Must flag missing IP assignment upon payment safeguard",
            missing.contains("IP Retention Until Payment")
        )
        assertTrue(
            "Must flag missing liability cap safeguard",
            missing.contains("Cap on Aggregate Liability")
        )
        assertFalse(
            "Must NOT evaluate lease-specific landlord maintenance obligations",
            missing.contains("Landlord Maintenance Obligations")
        )
        assertFalse(
            "Must NOT evaluate lease-specific security deposit return timeline",
            missing.contains("Security Deposit Return Timeline")
        )
    }

    @Test
    fun testContractTypeRouting_freelanceDetectsSpecificRedFlags() = runBlocking {
        val engine = LegalAuditEngine(context)

        val predatoryFreelanceText = """
            CONSULTING SERVICES CONTRACT
            1. Contractor agrees to unlimited liability and shall indemnify client without cap against all claims.
            2. Client retains exclusive ownership prior to payment.
            3. Contractor agrees to a worldwide non-compete for a period of 2 years post termination.
        """.trimIndent()

        val result = engine.analyzeContract(
            rawText = predatoryFreelanceText,
            country = "US",
            contractType = ContractType.EMPLOYMENT_FREELANCE
        )

        val categories = result.matchedRedFlags.map { it.category }
        assertTrue("Must detect uncapped liability", categories.contains("UNCAPPED_LIABILITY"))
        assertTrue("Must detect premature IP assignment", categories.contains("PREMATURE_IP_ASSIGNMENT"))
        assertTrue("Must detect overbroad non-compete", categories.contains("OVERBROAD_NON_COMPETE"))
        assertTrue("Predatory contract must be marked high risk", result.isHighRisk)
    }

    @Test
    fun testDocumentEntity_persistsUserCountryAndContractType() {
        val doc = DocumentEntity(
            id = "test-doc-123",
            title = "Consulting Agreement",
            country = "UK",
            contractType = com.example.engine.ContractType.EMPLOYMENT_FREELANCE,
            dateScanned = System.currentTimeMillis(),
            pageCount = 2,
            rawText = "sample contract text"
        )

        assertEquals("UK", doc.country)
        assertEquals(com.example.engine.ContractType.EMPLOYMENT_FREELANCE, doc.contractType)
    }
}
