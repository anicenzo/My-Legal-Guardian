package com.example.engine

/**
 * High-level contract classification for context-aware legal auditing.
 * Governs contextual missing safeguard detection, red flag rule sets,
 * and negotiation salutations.
 */
enum class ContractType(val displayName: String) {
    RESIDENTIAL_LEASE("Residential Lease"),
    EMPLOYMENT_FREELANCE("Employment / Freelance"),
    MEMBERSHIP_SUBSCRIPTION("Membership / Subscription"),
    GENERAL_AGREEMENT("General Agreement");

    companion object {
        fun fromString(value: String?): ContractType {
            if (value.isNullOrBlank()) return GENERAL_AGREEMENT
            return try {
                valueOf(value.trim().uppercase())
            } catch (_: IllegalArgumentException) {
                when (value.trim().lowercase()) {
                    "lease", "residential_lease" -> RESIDENTIAL_LEASE
                    "freelance", "contractor", "employment", "employment_freelance" -> EMPLOYMENT_FREELANCE
                    "subscription", "membership", "membership_subscription" -> MEMBERSHIP_SUBSCRIPTION
                    else -> GENERAL_AGREEMENT
                }
            }
        }
    }
}
