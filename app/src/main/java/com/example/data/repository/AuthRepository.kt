package com.example.data.repository

import com.example.data.models.AppRole
import com.example.data.models.AuthUser
import com.example.data.models.SupportedLanguage
import com.example.data.models.VerificationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

interface AuthRepository {
    val currentUser: StateFlow<AuthUser?>
    val isAuthenticated: Flow<Boolean>

    suspend fun registerArtisan(
        name: String,
        phone: String,
        pinOrPassword: String,
        language: SupportedLanguage,
        artisanId: String?,
        location: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): AuthResult<AuthUser>

    suspend fun loginArtisan(
        phone: String,
        pinOrPassword: String
    ): AuthResult<AuthUser>

    suspend fun registerBuyer(
        name: String,
        phone: String,
        email: String?,
        password: String,
        language: SupportedLanguage
    ): AuthResult<AuthUser>

    suspend fun loginBuyer(
        phoneOrEmail: String,
        passwordOrOtp: String
    ): AuthResult<AuthUser>

    suspend fun loginAsDemoArtisan(): AuthResult<AuthUser>

    suspend fun loginAsDemoBuyer(): AuthResult<AuthUser>

    suspend fun sendOtp(phoneNumber: String): AuthResult<String>

    suspend fun verifyOtpAndRecoverPassword(phone: String, otp: String, newPin: String): AuthResult<Boolean>

    suspend fun updateUserLanguage(language: SupportedLanguage): AuthResult<AuthUser>

    suspend fun logout()
}
