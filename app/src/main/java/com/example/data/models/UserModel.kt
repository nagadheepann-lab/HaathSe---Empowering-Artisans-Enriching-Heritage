package com.example.data.models

enum class VerificationStatus(val label: String, val description: String) {
    VERIFIED("Verified Artisan", "Government Pehchan / GI cluster verified identity"),
    PENDING("Verification Pending", "Document under automated AI cluster review"),
    NEEDS_REVIEW("Needs Review", "Additional proof or regional cluster endorsement requested")
}

data class AuthUser(
    val uid: String,
    val name: String,
    val phoneNumber: String,
    val email: String? = null,
    val role: AppRole,
    val selectedLanguage: SupportedLanguage = SupportedLanguage.ENGLISH,
    val artisanId: String? = null,
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    val craftSpecialty: String? = null,
    val stateLocation: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isDemoAccount: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
