package com.example.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class ContractClassifierTest {

    // =========================================================================
    // Word-Boundary & False-Positive Tests
    // =========================================================================

    @Test
    fun testWordBoundary_subwordsDoNotTriggerKeywords() {
        // Words containing "rent" (parent, transparent, different) and "member" (remember)
        val deceptiveText = """
            My parents visited transparent and different places this year.
            Please remember to bring your documents.
            No facility or agreement exists here.
        """.trimIndent()

        val classification = LegalAuditEngine.classifyContract(deceptiveText)
        assertEquals(
            "Subwords ('parent', 'different', 'remember') must not trigger word-boundary matches",
            ContractType.GENERAL_AGREEMENT,
            classification
        )
    }

    @Test
    fun testSingleKeywordHit_doesNotTriggerClassification() {
        // Even a strong 4-point keyword like "landlord" must not classify on its own (needs score >= 6 & hits >= 2)
        val singleHitText = "The landlord called yesterday to say hello."
        val classification = LegalAuditEngine.classifyContract(singleHitText)
        assertEquals(
            "A single keyword hit must never trigger classification; score and hit thresholds must be met",
            ContractType.GENERAL_AGREEMENT,
            classification
        )
    }

    @Test
    fun testEmptyOrWhitespaceText_returnsGeneralAgreement() {
        assertEquals(ContractType.GENERAL_AGREEMENT, LegalAuditEngine.classifyContract(""))
        assertEquals(ContractType.GENERAL_AGREEMENT, LegalAuditEngine.classifyContract("   \n\t  "))
    }

    // =========================================================================
    // RESIDENTIAL_LEASE Fixtures (>= 3)
    // =========================================================================

    @Test
    fun testResidentialLease_fixture1_standardApartmentLease() {
        val text = """
            RESIDENTIAL LEASE AGREEMENT
            This Agreement is entered into between Landlord and Tenant for the leased premises located at
            123 Elm Street. The monthly rent shall be $2,000 payable on the first day of each calendar month.
            A security deposit of $2,000 is required upon execution. Landlord warrants habitability of the dwelling.
            Eviction proceedings may be instituted if rent remains unpaid after notice to cure.
        """.trimIndent()

        val result = LegalAuditEngine.classifyContract(text)
        assertEquals(ContractType.RESIDENTIAL_LEASE, result)
    }

    @Test
    fun testResidentialLease_fixture2_subleaseAgreement() {
        val text = """
            SUBLEASE AND OCCUPANCY CONTRACT
            The Lessor and Lessee agree that Lessee may sublease the premises to Subtenant.
            The tenant agrees to pay monthly rent to landlord in exchange for occupancy of the premises.
            Security deposit shall be held in escrow in accordance with statutory requirements.
        """.trimIndent()

        val result = LegalAuditEngine.classifyContract(text)
        assertEquals(ContractType.RESIDENTIAL_LEASE, result)
    }

    @Test
    fun testResidentialLease_fixture3_evictionAndTenancyNotice() {
        val text = """
            TENANCY NOTICE AND LEASE RIDER
            Notice to Tenant from Landlord: Your residential lease for the premises expires on June 30.
            Failure to vacate the dwelling may result in immediate eviction under municipal housing codes.
            All unpaid rent and forfeited security deposit charges must be settled immediately.
        """.trimIndent()

        val result = LegalAuditEngine.classifyContract(text)
        assertEquals(ContractType.RESIDENTIAL_LEASE, result)
    }

    // =========================================================================
    // EMPLOYMENT_FREELANCE Fixtures (>= 3)
    // =========================================================================

    @Test
    fun testEmploymentFreelance_fixture1_independentContractorAgreement() {
        val text = """
            INDEPENDENT CONTRACTOR AGREEMENT
            This Agreement is made between Client and Independent Contractor.
            Contractor shall provide software engineering deliverables in accordance with Schedule A.
            All work product and intellectual property created shall transfer to Client upon full payment.
            Invoicing shall occur bi-weekly based on the agreed hourly rate.
        """.trimIndent()

        val result = LegalAuditEngine.classifyContract(text)
        assertEquals(ContractType.EMPLOYMENT_FREELANCE, result)
    }

    @Test
    fun testEmploymentFreelance_fixture2_statementOfWorkConsulting() {
        val text = """
            STATEMENT OF WORK AND CONSULTING CONTRACT
            Scope of work: The contractor will design UI deliverables for the client.
            All intellectual property rights remain with the creator until final invoice is paid.
            Contractor agrees to a limited non-compete within a 5-mile radius during the project term.
        """.trimIndent()

        val result = LegalAuditEngine.classifyContract(text)
        assertEquals(ContractType.EMPLOYMENT_FREELANCE, result)
    }

    @Test
    fun testEmploymentFreelance_fixture3_employeeNonCompeteDeliverables() {
        val text = """
            EMPLOYEE PROPRIETARY INFORMATION AND NON-COMPETE AGREEMENT
            The employee acknowledges that all deliverables and work product created during employment
            constitute intellectual property owned exclusively by the employer.
            Contractor and employee obligations include strict non-compete clauses and confidentiality.
        """.trimIndent()

        val result = LegalAuditEngine.classifyContract(text)
        assertEquals(ContractType.EMPLOYMENT_FREELANCE, result)
    }

    // =========================================================================
    // MEMBERSHIP_SUBSCRIPTION Fixtures (>= 3)
    // =========================================================================

    @Test
    fun testMembershipSubscription_fixture1_gymMembership() {
        val text = """
            FITNESS CENTER MEMBERSHIP CONTRACT
            The member agrees to pay a recurring fee for access to the gym facility.
            This membership is subject to auto-renewal each year unless cancelled 30 days in advance.
            Please review the full cancellation policy and billing cycle terms at our service desk.
        """.trimIndent()

        val result = LegalAuditEngine.classifyContract(text)
        assertEquals(ContractType.MEMBERSHIP_SUBSCRIPTION, result)
    }

    @Test
    fun testMembershipSubscription_fixture2_saasTermsOfService() {
        val text = """
            CLOUD SOFTWARE TERMS OF SERVICE
            By purchasing a subscription, the subscriber agrees to these terms of service.
            The subscription plan includes automatic auto-renewal at the end of each billing cycle.
            A recurring fee will be charged to the payment method on file until cancellation.
        """.trimIndent()

        val result = LegalAuditEngine.classifyContract(text)
        assertEquals(ContractType.MEMBERSHIP_SUBSCRIPTION, result)
    }

    @Test
    fun testMembershipSubscription_fixture3_clubMembershipTier() {
        val text = """
            EXCLUSIVE CLUB MEMBERSHIP AGREEMENT
            Each registered member gains admittance to the club facility and amenities.
            Annual subscription renewal applies, with recurring fees billed on January 1.
            Our membership terms of service stipulate that fees are non-refundable after 14 days.
        """.trimIndent()

        val result = LegalAuditEngine.classifyContract(text)
        assertEquals(ContractType.MEMBERSHIP_SUBSCRIPTION, result)
    }

    // =========================================================================
    // GENERAL_AGREEMENT Fixtures (>= 3)
    // =========================================================================

    @Test
    fun testGeneralAgreement_fixture1_mutualNDA() {
        val text = """
            MUTUAL NON-DISCLOSURE AGREEMENT
            This Agreement governs the disclosure of confidential and proprietary business information
            between the Parties. Neither party shall disclose confidential terms to any third party.
            In the event of a dispute, arbitration shall be conducted under commercial rules.
        """.trimIndent()

        val result = LegalAuditEngine.classifyContract(text)
        assertEquals(ContractType.GENERAL_AGREEMENT, result)
    }

    @Test
    fun testGeneralAgreement_fixture2_releaseAndWaiver() {
        val text = """
            GENERAL RELEASE AND WAIVER OF CLAIMS
            Releasor hereby covenants not to sue Releasee for any past disputes or causes of action.
            Both parties acknowledge that this document represents the complete and final understanding
            superseding all prior oral or written discussions.
        """.trimIndent()

        val result = LegalAuditEngine.classifyContract(text)
        assertEquals(ContractType.GENERAL_AGREEMENT, result)
    }

    @Test
    fun testGeneralAgreement_fixture3_partnershipMOU() {
        val text = """
            MEMORANDUM OF UNDERSTANDING FOR STRATEGIC ALLIANCE
            Party A and Party B agree to explore collaborative business opportunities in good faith.
            This memorandum sets forth mutual intentions regarding joint marketing and technical integration.
            Either party may terminate this relationship with 30 days written notice.
        """.trimIndent()

        val result = LegalAuditEngine.classifyContract(text)
        assertEquals(ContractType.GENERAL_AGREEMENT, result)
    }

    // =========================================================================
    // Ambiguous / Mixed-Signal Fixtures (>= 2) -> GENERAL_AGREEMENT
    // =========================================================================

    @Test
    fun testAmbiguousMixed_fixture1_contractorWorkingOnPremisesWithRent() {
        // Freelance keywords ("contractor", "deliverables", "client") mixed with Lease keywords ("tenant", "premises", "rent")
        val mixedText = """
            OFFICE SHARING AND SERVICES AGREEMENT
            The contractor agrees to supply software deliverables to the client while occupying office premises.
            The client acts as landlord and the contractor as tenant, paying rent of $500 monthly.
            Both parties agree to mutual deliverables and prompt rent payments.
        """.trimIndent()

        val result = LegalAuditEngine.classifyContract(mixedText)
        assertEquals(
            "Mixed signals without a 1.5x margin over runner-up must fall back to GENERAL_AGREEMENT",
            ContractType.GENERAL_AGREEMENT,
            result
        )
    }

    @Test
    fun testAmbiguousMixed_fixture2_gymFacilityEmployingContractor() {
        // Subscription keywords ("member", "facility", "subscription") mixed with Employment/Freelance ("contractor", "employee", "deliverables")
        val mixedText = """
            TRAINER AND FACILITY ACCESS AGREEMENT
            The independent contractor provides fitness deliverables to club members within the gym facility.
            The contractor is not an employee, but must maintain an active member subscription to use the facility.
            Terms of service apply to both subscriber privileges and contractor scope of work.
        """.trimIndent()

        val result = LegalAuditEngine.classifyContract(mixedText)
        assertEquals(
            "Mixed signals between subscription and freelance without decisive lead must fall back to GENERAL_AGREEMENT",
            ContractType.GENERAL_AGREEMENT,
            result
        )
    }
}
