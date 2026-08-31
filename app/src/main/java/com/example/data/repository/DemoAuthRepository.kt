package com.example.data.repository

import com.example.data.models.AppRole
import com.example.data.models.AuthUser
import com.example.data.models.SupportedLanguage
import com.example.data.models.VerificationStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class DemoAuthRepository : AuthRepository {

    // Pre-seeded mock accounts store in memory
    private val registeredArtisans = mutableMapOf<String, Pair<AuthUser, String>>(
        "9876543210" to Pair(
            AuthUser(
                uid = "artisan_demo_01",
                name = "Meenakshi Ammal",
                phoneNumber = "9876543210",
                role = AppRole.ARTISAN,
                selectedLanguage = SupportedLanguage.TAMIL,
                artisanId = "DEMO-PEHCHAN-88219",
                verificationStatus = VerificationStatus.VERIFIED,
                craftSpecialty = "Kanchipuram Pure Mulberry Silk Weaving",
                stateLocation = "Tamil Nadu, India",
                isDemoAccount = true
            ),
            "1234"
        ),
        "9811223344" to Pair(
            AuthUser(
                uid = "artisan_demo_02",
                name = "Rameshwar Prajapati",
                phoneNumber = "9811223344",
                role = AppRole.ARTISAN,
                selectedLanguage = SupportedLanguage.HINDI,
                artisanId = "DEMO-GI-77312",
                verificationStatus = VerificationStatus.VERIFIED,
                craftSpecialty = "Jaipur Blue Pottery & Terracotta",
                stateLocation = "Rajasthan, India",
                isDemoAccount = true
            ),
            "1234"
        )
    )

    private val registeredBuyers = mutableMapOf<String, Pair<AuthUser, String>>(
        "buyer@haathse.demo" to Pair(
            AuthUser(
                uid = "buyer_demo_01",
                name = "Aarav Sharma",
                phoneNumber = "9123456780",
                email = "buyer@haathse.demo",
                role = AppRole.BUYER,
                selectedLanguage = SupportedLanguage.ENGLISH,
                stateLocation = "Bangalore, India",
                isDemoAccount = true
            ),
            "buyer123"
        ),
        "9123456780" to Pair(
            AuthUser(
                uid = "buyer_demo_01",
                name = "Aarav Sharma",
                phoneNumber = "9123456780",
                email = "buyer@haathse.demo",
                role = AppRole.BUYER,
                selectedLanguage = SupportedLanguage.ENGLISH,
                stateLocation = "Bangalore, India",
                isDemoAccount = true
            ),
            "buyer123"
        )
    )

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    override val isAuthenticated = _currentUser.map { it != null }

    override suspend fun registerArtisan(
        name: String,
        phone: String,
        pinOrPassword: String,
        language: SupportedLanguage,
        artisanId: String?,
        location: String?,
        latitude: Double?,
        longitude: Double?
    ): AuthResult<AuthUser> {
        delay(600) // Realistic network delay

        val cleanPhone = phone.filter { it.isDigit() }
        if (cleanPhone.length < 10) {
            return AuthResult.Error("Please enter a valid 10-digit mobile number")
        }
        if (name.isBlank()) {
            return AuthResult.Error("Name cannot be empty")
        }
        if (pinOrPassword.length < 4) {
            return AuthResult.Error("Please set a secure PIN/Password of at least 4 characters")
        }

        // Demo verification logic
        val isDemoId = artisanId?.trim()?.uppercase()?.startsWith("DEMO") == true ||
                artisanId?.trim()?.isNotBlank() == true

        val status = if (!artisanId.isNullOrBlank()) {
            VerificationStatus.VERIFIED
        } else {
            VerificationStatus.PENDING
        }

        val newUser = AuthUser(
            uid = "artisan_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            phoneNumber = cleanPhone,
            role = AppRole.ARTISAN,
            selectedLanguage = language,
            artisanId = artisanId?.trim()?.ifBlank { null } ?: "DEMO-PEHCHAN-${(10000..99999).random()}",
            verificationStatus = status,
            craftSpecialty = "Handmade Artisan Craft",
            stateLocation = location ?: "India",
            latitude = latitude ?: 12.8342,
            longitude = longitude ?: 79.7036,
            isDemoAccount = true
        )

        registeredArtisans[cleanPhone] = Pair(newUser, pinOrPassword)
        _currentUser.value = newUser
        return AuthResult.Success(newUser)
    }

    override suspend fun loginArtisan(
        phone: String,
        pinOrPassword: String
    ): AuthResult<AuthUser> {
        delay(500)
        val cleanPhone = phone.filter { it.isDigit() }

        val stored = registeredArtisans[cleanPhone]
        if (stored != null) {
            if (stored.second == pinOrPassword || pinOrPassword == "1234") {
                _currentUser.value = stored.first
                return AuthResult.Success(stored.first)
            } else {
                return AuthResult.Error("Incorrect PIN or Password. Use demo PIN '1234' or reset your password.")
            }
        }

        // If not found but valid format, allow instant demo login
        if (cleanPhone.length == 10) {
            val newUser = AuthUser(
                uid = "artisan_${UUID.randomUUID().toString().take(8)}",
                name = "Artisan (${cleanPhone.takeLast(4)})",
                phoneNumber = cleanPhone,
                role = AppRole.ARTISAN,
                selectedLanguage = SupportedLanguage.ENGLISH,
                artisanId = "DEMO-PEHCHAN-${(10000..99999).random()}",
                verificationStatus = VerificationStatus.VERIFIED,
                craftSpecialty = "Traditional Master Crafts",
                isDemoAccount = true
            )
            registeredArtisans[cleanPhone] = Pair(newUser, pinOrPassword)
            _currentUser.value = newUser
            return AuthResult.Success(newUser)
        }

        return AuthResult.Error("Mobile number not registered. Please sign up first.")
    }

    override suspend fun registerBuyer(
        name: String,
        phone: String,
        email: String?,
        password: String,
        language: SupportedLanguage
    ): AuthResult<AuthUser> {
        delay(600)
        val cleanPhone = phone.filter { it.isDigit() }
        if (cleanPhone.length < 10) {
            return AuthResult.Error("Please enter a valid 10-digit mobile number")
        }
        if (name.isBlank()) {
            return AuthResult.Error("Full Name is required")
        }
        if (password.length < 4) {
            return AuthResult.Error("Password must be at least 4 characters")
        }

        val newUser = AuthUser(
            uid = "buyer_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            phoneNumber = cleanPhone,
            email = email?.trim()?.ifBlank { null },
            role = AppRole.BUYER,
            selectedLanguage = language,
            stateLocation = "India",
            isDemoAccount = true
        )

        registeredBuyers[cleanPhone] = Pair(newUser, password)
        if (!email.isNullOrBlank()) {
            registeredBuyers[email.trim().lowercase()] = Pair(newUser, password)
        }
        _currentUser.value = newUser
        return AuthResult.Success(newUser)
    }

    override suspend fun loginBuyer(
        phoneOrEmail: String,
        passwordOrOtp: String
    ): AuthResult<AuthUser> {
        delay(500)
        val query = phoneOrEmail.trim().lowercase()
        val cleanPhone = query.filter { it.isDigit() }

        val stored = registeredBuyers[query] ?: if (cleanPhone.length == 10) registeredBuyers[cleanPhone] else null

        if (stored != null) {
            if (stored.second == passwordOrOtp || passwordOrOtp == "1234" || passwordOrOtp == "buyer123") {
                _currentUser.value = stored.first
                return AuthResult.Success(stored.first)
            } else {
                return AuthResult.Error("Incorrect password or OTP. Demo password: 'buyer123' or '1234'.")
            }
        }

        // Allow graceful sign-in for any reasonable input in demo mode
        if (cleanPhone.length == 10 || query.contains("@")) {
            val newUser = AuthUser(
                uid = "buyer_${UUID.randomUUID().toString().take(8)}",
                name = if (query.contains("@")) query.substringBefore("@").replaceFirstChar { it.uppercase() } else "Buyer (${cleanPhone.takeLast(4)})",
                phoneNumber = if (cleanPhone.length == 10) cleanPhone else "9123456780",
                email = if (query.contains("@")) query else null,
                role = AppRole.BUYER,
                selectedLanguage = SupportedLanguage.ENGLISH,
                isDemoAccount = true
            )
            _currentUser.value = newUser
            return AuthResult.Success(newUser)
        }

        return AuthResult.Error("Invalid phone number or email address")
    }

    override suspend fun loginAsDemoArtisan(): AuthResult<AuthUser> {
        delay(300)
        val demoArtisan = AuthUser(
            uid = "demo_artisan_master",
            name = "Meenakshi Ammal",
            phoneNumber = "9876543210",
            role = AppRole.ARTISAN,
            selectedLanguage = SupportedLanguage.TAMIL,
            artisanId = "DEMO-PEHCHAN-88219",
            verificationStatus = VerificationStatus.VERIFIED,
            craftSpecialty = "Kanchipuram Pure Mulberry Silk Weaving",
            stateLocation = "Tamil Nadu, India",
            isDemoAccount = true
        )
        _currentUser.value = demoArtisan
        return AuthResult.Success(demoArtisan)
    }

    override suspend fun loginAsDemoBuyer(): AuthResult<AuthUser> {
        delay(300)
        val demoBuyer = AuthUser(
            uid = "demo_buyer_master",
            name = "Aarav Sharma",
            phoneNumber = "9123456780",
            email = "aarav.sharma@craftb2b.in",
            role = AppRole.BUYER,
            selectedLanguage = SupportedLanguage.ENGLISH,
            stateLocation = "Bangalore, Karnataka",
            isDemoAccount = true
        )
        _currentUser.value = demoBuyer
        return AuthResult.Success(demoBuyer)
    }

    override suspend fun sendOtp(phoneNumber: String): AuthResult<String> {
        delay(400)
        val clean = phoneNumber.filter { it.isDigit() }
        if (clean.length < 10) {
            return AuthResult.Error("Please enter a valid 10-digit mobile number")
        }
        // In demo mode, OTP is always 1234
        return AuthResult.Success("1234")
    }

    override suspend fun verifyOtpAndRecoverPassword(phone: String, otp: String, newPin: String): AuthResult<Boolean> {
        delay(500)
        if (otp != "1234") {
            return AuthResult.Error("Invalid OTP. In demo mode, use OTP '1234'")
        }
        val clean = phone.filter { it.isDigit() }
        val stored = registeredArtisans[clean]
        if (stored != null) {
            registeredArtisans[clean] = Pair(stored.first, newPin)
        }
        return AuthResult.Success(true)
    }

    override suspend fun updateUserLanguage(language: SupportedLanguage): AuthResult<AuthUser> {
        val user = _currentUser.value ?: return AuthResult.Error("No user logged in")
        val updated = user.copy(selectedLanguage = language)
        _currentUser.value = updated
        return AuthResult.Success(updated)
    }

    override suspend fun logout() {
        _currentUser.value = null
    }
}
