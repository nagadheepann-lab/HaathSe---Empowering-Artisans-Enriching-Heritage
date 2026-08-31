package com.example.data.models

/**
 * PRIVACY RULES & DATA CLASSIFICATION
 *
 * CRITICAL PRIVACY DIRECTIVE:
 * Never publicly expose:
 *  - Government Artisan ID / Pehchan Card Number
 *  - Aadhaar / PAN / Tax documents
 *  - Phone numbers
 *  - Private residence / street addresses
 *  - Bank account numbers, IFSC codes, UPI handles
 *  - Private identity verification certificates & PDFs
 *
 * Public profiles MUST ONLY expose:
 *  - Name
 *  - Craft Specialization
 *  - General Location (e.g. "Kanchipuram, Tamil Nadu")
 *  - Verification Status (Verified badge)
 *  - Transparent Trust Score (0 - 100)
 *  - Public Products Catalog
 *  - Public Heritage Story & Lineage
 */

data class ArtisanPrivateSensitiveRecord(
    val artisanId: String,
    val fullName: String,
    val govtArtisanId: String, // e.g. "PEHCHAN-TN-84920194" - NEVER PUBLIC
    val aadhaarHash: String, // NEVER PUBLIC
    val panNumber: String, // NEVER PUBLIC
    val phoneNumber: String, // NEVER PUBLIC
    val privateResidenceStreet: String, // NEVER PUBLIC
    val villageState: String,
    val bankAccountNumber: String, // NEVER PUBLIC
    val bankIfscCode: String, // NEVER PUBLIC
    val upiId: String, // NEVER PUBLIC
    val kycDocumentUrls: List<String>, // NEVER PUBLIC
    val craftSpecialization: String,
    val experienceYears: Int,
    val trustScore: Int,
    val isKycVerified: Boolean,
    val bio: String,
    val publicStory: String,
    val awards: String,
    val avatarDrawableRes: String
)

data class PublicArtisanProfile(
    val artisanId: String,
    val displayName: String,
    val craftSpecialization: String,
    val generalLocation: String, // "Kanchipuram, Tamil Nadu"
    val isVerified: Boolean,
    val verificationBadgeText: String,
    val trustScore: Int,
    val experienceYears: Int,
    val bio: String,
    val publicStory: String,
    val awards: String,
    val avatarDrawableRes: String
)

object PrivacyFilter {
    /**
     * Sanitizes an artisan's full internal record into a strictly privacy-safe public representation.
     */
    fun sanitizeForPublicView(privateRecord: ArtisanPrivateSensitiveRecord): PublicArtisanProfile {
        return PublicArtisanProfile(
            artisanId = privateRecord.artisanId,
            displayName = privateRecord.fullName,
            craftSpecialization = privateRecord.craftSpecialization,
            generalLocation = privateRecord.villageState,
            isVerified = privateRecord.isKycVerified,
            verificationBadgeText = if (privateRecord.isKycVerified) "Verified Master Artisan" else "Artisan Member",
            trustScore = privateRecord.trustScore,
            experienceYears = privateRecord.experienceYears,
            bio = privateRecord.bio,
            publicStory = privateRecord.publicStory,
            awards = privateRecord.awards,
            avatarDrawableRes = privateRecord.avatarDrawableRes
        )
    }

    /**
     * Masks any phone number to prevent unintentional leaks.
     */
    fun maskPhoneNumber(phone: String): String {
        if (phone.length < 4) return "•••"
        return "••••••" + phone.takeLast(4)
    }

    /**
     * Masks government Pehchan ID for safe administrative preview.
     */
    fun maskGovtId(govtId: String): String {
        if (govtId.length < 4) return "PEHCHAN-••••"
        return govtId.take(7) + "-••••-" + govtId.takeLast(3)
    }

    /**
     * Masks Bank Account number.
     */
    fun maskBankAccount(accountNo: String): String {
        if (accountNo.length < 4) return "••••"
        return "•••• •••• " + accountNo.takeLast(4)
    }
}
