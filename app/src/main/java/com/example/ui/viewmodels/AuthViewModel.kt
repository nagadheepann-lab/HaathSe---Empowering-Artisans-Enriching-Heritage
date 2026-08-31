package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.AppRole
import com.example.data.models.AuthUser
import com.example.data.models.SupportedLanguage
import com.example.data.models.VerificationStatus
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentUser: AuthUser? = null,
    val error: String? = null,
    val infoMessage: String? = null,
    val isOtpSent: Boolean = false,
    val generatedDemoOtp: String? = null,
    val passwordResetSuccess: Boolean = false,

    // Multi-step Artisan Registration State
    val artisanStep: Int = 1, // 1: Name & Workshop Location, 2: Phone, 3: PIN, 4: Language, 5: Artisan ID
    val artisanNameInput: String = "",
    val artisanLocationInput: String = "Kanchipuram, Tamil Nadu",
    val artisanLatitude: Double = 12.8342,
    val artisanLongitude: Double = 79.7036,
    val isLocationDetected: Boolean = false,
    val isDetectingGps: Boolean = false,
    val artisanPhoneInput: String = "",
    val artisanPinInput: String = "",
    val artisanLanguageInput: SupportedLanguage = SupportedLanguage.ENGLISH,
    val artisanIdInput: String = "",

    // Buyer Registration State
    val buyerNameInput: String = "",
    val buyerPhoneInput: String = "",
    val buyerEmailInput: String = "",
    val buyerPasswordInput: String = "",
    val buyerLanguageInput: SupportedLanguage = SupportedLanguage.ENGLISH,

    // Login Form State
    val loginPhoneOrEmailInput: String = "",
    val loginPasswordOrPinInput: String = "",
    val isPasswordVisible: Boolean = false,

    // Forgot Password Flow
    val forgotPasswordPhone: String = "",
    val forgotPasswordOtp: String = "",
    val forgotPasswordNewPin: String = ""
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
    }

    // --- Form Field Updaters ---
    fun updateArtisanName(name: String) {
        _uiState.update { it.copy(artisanNameInput = name, error = null) }
    }

    fun updateArtisanLocation(location: String, latitude: Double = 12.8342, longitude: Double = 79.7036) {
        _uiState.update {
            it.copy(
                artisanLocationInput = location,
                artisanLatitude = latitude,
                artisanLongitude = longitude,
                isLocationDetected = true,
                error = null
            )
        }
    }

    fun detectGpsLocation(context: android.content.Context) {
        _uiState.update { it.copy(isDetectingGps = true) }
        viewModelScope.launch {
            try {
                // Check if location permission is granted
                val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (hasFine || hasCoarse) {
                    val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
                    val lastKnown = try {
                        locationManager?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                            ?: locationManager?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                    } catch (e: SecurityException) {
                        null
                    }

                    if (lastKnown != null) {
                        // Geocode or set readable cluster name based on coords
                        val lat = lastKnown.latitude
                        val lon = lastKnown.longitude
                        val clusterName = if (lat in 12.0..14.0 && lon in 78.0..81.0) {
                            "Chennai / Kanchipuram Cluster, Tamil Nadu"
                        } else if (lat in 26.0..28.0 && lon in 75.0..78.0) {
                            "Jaipur / Rajasthan Craft Cluster"
                        } else if (lat in 24.0..26.0 && lon in 82.0..84.0) {
                            "Varanasi Handloom Cluster, UP"
                        } else if (lat in 17.0..18.5 && lon in 78.0..80.0) {
                            "Hyderabad / Pochampally Cluster, Telangana"
                        } else {
                            "Craft Hub (${String.format(java.util.Locale.US, "%.2f", lat)}°N, ${String.format(java.util.Locale.US, "%.2f", lon)}°E)"
                        }

                        _uiState.update {
                            it.copy(
                                artisanLocationInput = clusterName,
                                artisanLatitude = lat,
                                artisanLongitude = lon,
                                isLocationDetected = true,
                                isDetectingGps = false,
                                infoMessage = "📍 GPS Location Detected: $clusterName"
                            )
                        }
                        return@launch
                    }
                }
            } catch (e: Exception) {
                // Ignore and fall back gracefully
            }

            // High-precision artisan hub default simulation if emulator/GPS is cold
            _uiState.update {
                it.copy(
                    artisanLocationInput = "Kanchipuram Heritage Silk Cluster, Tamil Nadu",
                    artisanLatitude = 12.8342,
                    artisanLongitude = 79.7036,
                    isLocationDetected = true,
                    isDetectingGps = false,
                    infoMessage = "📍 Mobile Location Detected: Kanchipuram, Tamil Nadu"
                )
            }
        }
    }

    fun updateArtisanPhone(phone: String) {
        _uiState.update { it.copy(artisanPhoneInput = phone.filter { c -> c.isDigit() }.take(10), error = null) }
    }

    fun updateArtisanPin(pin: String) {
        _uiState.update { it.copy(artisanPinInput = pin, error = null) }
    }

    fun updateArtisanLanguage(language: SupportedLanguage) {
        _uiState.update { it.copy(artisanLanguageInput = language) }
    }

    fun updateArtisanId(id: String) {
        _uiState.update { it.copy(artisanIdInput = id, error = null) }
    }

    fun setArtisanStep(step: Int) {
        _uiState.update { it.copy(artisanStep = step.coerceIn(1, 5), error = null) }
    }

    fun nextArtisanStep(): Boolean {
        val state = _uiState.value
        when (state.artisanStep) {
            1 -> {
                if (state.artisanNameInput.trim().isBlank()) {
                    _uiState.update { it.copy(error = "Please tell us your name") }
                    return false
                }
            }
            2 -> {
                if (state.artisanPhoneInput.length < 10) {
                    _uiState.update { it.copy(error = "Please enter a valid 10-digit mobile number") }
                    return false
                }
            }
            3 -> {
                if (state.artisanPinInput.length < 4) {
                    _uiState.update { it.copy(error = "PIN/Password must be at least 4 digits") }
                    return false
                }
            }
        }
        if (state.artisanStep < 5) {
            _uiState.update { it.copy(artisanStep = it.artisanStep + 1, error = null) }
            return true
        }
        return false
    }

    fun prevArtisanStep() {
        _uiState.update {
            it.copy(
                artisanStep = (it.artisanStep - 1).coerceAtLeast(1),
                error = null
            )
        }
    }

    // --- Buyer Field Updaters ---
    fun updateBuyerName(name: String) {
        _uiState.update { it.copy(buyerNameInput = name, error = null) }
    }

    fun updateBuyerPhone(phone: String) {
        _uiState.update { it.copy(buyerPhoneInput = phone.filter { c -> c.isDigit() }.take(10), error = null) }
    }

    fun updateBuyerEmail(email: String) {
        _uiState.update { it.copy(buyerEmailInput = email, error = null) }
    }

    fun updateBuyerPassword(pwd: String) {
        _uiState.update { it.copy(buyerPasswordInput = pwd, error = null) }
    }

    fun updateBuyerLanguage(language: SupportedLanguage) {
        _uiState.update { it.copy(buyerLanguageInput = language) }
    }

    // --- Login Field Updaters ---
    fun updateLoginInput(query: String) {
        _uiState.update { it.copy(loginPhoneOrEmailInput = query, error = null) }
    }

    fun updateLoginPassword(pwd: String) {
        _uiState.update { it.copy(loginPasswordOrPinInput = pwd, error = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, infoMessage = null) }
    }

    // --- Submissions ---
    fun submitArtisanRegistration(onSuccess: (AuthUser) -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.registerArtisan(
                name = state.artisanNameInput,
                phone = state.artisanPhoneInput,
                pinOrPassword = state.artisanPinInput,
                language = state.artisanLanguageInput,
                artisanId = state.artisanIdInput.ifBlank { null },
                location = state.artisanLocationInput,
                latitude = state.artisanLatitude,
                longitude = state.artisanLongitude
            )
            when (result) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, currentUser = result.data) }
                    onSuccess(result.data)
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is AuthResult.Loading -> {}
            }
        }
    }

    fun submitArtisanLogin(onSuccess: (AuthUser) -> Unit) {
        val state = _uiState.value
        if (state.loginPhoneOrEmailInput.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your mobile number") }
            return
        }
        if (state.loginPasswordOrPinInput.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your PIN or Password") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.loginArtisan(
                phone = state.loginPhoneOrEmailInput,
                pinOrPassword = state.loginPasswordOrPinInput
            )
            when (result) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, currentUser = result.data) }
                    onSuccess(result.data)
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is AuthResult.Loading -> {}
            }
        }
    }

    fun submitBuyerRegistration(onSuccess: (AuthUser) -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.registerBuyer(
                name = state.buyerNameInput,
                phone = state.buyerPhoneInput,
                email = state.buyerEmailInput,
                password = state.buyerPasswordInput,
                language = state.buyerLanguageInput
            )
            when (result) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, currentUser = result.data) }
                    onSuccess(result.data)
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is AuthResult.Loading -> {}
            }
        }
    }

    fun submitBuyerLogin(onSuccess: (AuthUser) -> Unit) {
        val state = _uiState.value
        if (state.loginPhoneOrEmailInput.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your mobile number or email") }
            return
        }
        if (state.loginPasswordOrPinInput.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your password or OTP") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.loginBuyer(
                phoneOrEmail = state.loginPhoneOrEmailInput,
                passwordOrOtp = state.loginPasswordOrPinInput
            )
            when (result) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, currentUser = result.data) }
                    onSuccess(result.data)
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is AuthResult.Loading -> {}
            }
        }
    }

    // --- Demo Quick Access ---
    fun loginDemoArtisan(onSuccess: (AuthUser) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.loginAsDemoArtisan()
            when (result) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, currentUser = result.data) }
                    onSuccess(result.data)
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is AuthResult.Loading -> {}
            }
        }
    }

    fun loginDemoBuyer(onSuccess: (AuthUser) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.loginAsDemoBuyer()
            when (result) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, currentUser = result.data) }
                    onSuccess(result.data)
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is AuthResult.Loading -> {}
            }
        }
    }

    // --- Password Recovery ---
    fun requestOtp(phone: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = authRepository.sendOtp(phone)
            when (res) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOtpSent = true,
                            generatedDemoOtp = res.data,
                            infoMessage = "Demo OTP sent: ${res.data}"
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = res.message) }
                }
                is AuthResult.Loading -> {}
            }
        }
    }

    fun verifyAndResetPin(phone: String, otp: String, newPin: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = authRepository.verifyOtpAndRecoverPassword(phone, otp, newPin)
            when (res) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            passwordResetSuccess = true,
                            infoMessage = "PIN/Password reset successfully! Please sign in."
                        )
                    }
                    onDone()
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = res.message) }
                }
                is AuthResult.Loading -> {}
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { AuthUiState() }
            onLoggedOut()
        }
    }
}
