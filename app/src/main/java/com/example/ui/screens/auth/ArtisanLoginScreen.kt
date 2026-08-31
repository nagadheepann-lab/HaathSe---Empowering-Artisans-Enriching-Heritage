package com.example.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.AuthUser
import com.example.data.models.SupportedLanguage
import com.example.data.repository.KarigarRepository
import com.example.ui.components.HaathSePrimaryButton
import com.example.ui.components.VoiceButton
import com.example.ui.theme.*
import com.example.ui.viewmodels.AuthViewModel
import com.example.utils.AudioVoiceHelper

@Composable
fun ArtisanLoginScreen(
    viewModel: AuthViewModel,
    audioHelper: AudioVoiceHelper?,
    onNavigateBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (AuthUser) -> Unit,
    modifier: Modifier = Modifier,
    repository: KarigarRepository? = null,
    currentLanguage: SupportedLanguage = SupportedLanguage.ENGLISH,
    onNavigateToEvents: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    var showForgotPasswordModal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HaathSeCreamBg)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("btn_artisan_login_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = HaathSeTerracottaDark
                    )
                }

                Text(
                    text = "Artisan Studio Login",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = HaathSeTerracottaDark
                    )
                )

                VoiceButton(
                    textToSpeak = "Artisan Login. Enter your mobile number and security PIN, or tap Instant Evaluation for quick demo access.",
                    language = currentLanguage,
                    audioHelper = audioHelper
                )
            }
        },
        containerColor = HaathSeCreamBg
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Brand Logo Header
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, HaathSeTerracottaLight, CircleShape)
                    .shadow(3.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.haathse_logo),
                    contentDescription = "HaathSe Logo",
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome Back, Artisan",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = HaathSeTerracottaDark,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Sign in to manage your workshop, voice listings, and direct buyer orders.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = HaathSeTextSecondary,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Phone Input
            OutlinedTextField(
                value = state.loginPhoneOrEmailInput,
                onValueChange = { viewModel.updateLoginInput(it) },
                label = { Text("Registered Mobile Number") },
                prefix = {
                    Text(
                        text = "🇮🇳 +91 ",
                        fontWeight = FontWeight.Bold,
                        color = HaathSeTextPrimary
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = HaathSeTerracotta)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HaathSeTerracotta,
                    focusedLabelColor = HaathSeTerracotta,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_artisan_login_phone")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // PIN/Password Input
            OutlinedTextField(
                value = state.loginPasswordOrPinInput,
                onValueChange = { viewModel.updateLoginPassword(it) },
                label = { Text("Security PIN / Password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = HaathSeTerracotta)
                },
                trailingIcon = {
                    IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                        Icon(
                            imageVector = if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Visibility",
                            tint = HaathSeTextSecondary
                        )
                    }
                },
                visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HaathSeTerracotta,
                    focusedLabelColor = HaathSeTerracotta,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    viewModel.submitArtisanLogin { onLoginSuccess(it) }
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_artisan_login_pin")
            )

            // Forgot Password Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { showForgotPasswordModal = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Forgot PIN / OTP Recovery?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HaathSeTerracotta
                    )
                }
            }

            if (state.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = state.error ?: "", color = Color(0xFF991B1B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (state.infoMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = state.infoMessage ?: "", color = Color(0xFF166534), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Primary Sign In Button
            HaathSePrimaryButton(
                text = "Sign In to Studio →",
                onClick = {
                    viewModel.submitArtisanLogin { onLoginSuccess(it) }
                },
                isLoading = state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_artisan_login_submit")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Demo 1-Click Access Box
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = HaathSeMarigoldLight.copy(alpha = 0.45f)),
                border = BorderStroke(1.dp, HaathSeMarigold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = HaathSeMarigoldDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Instant Evaluation",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = HaathSeMarigoldDark
                            )
                        }
                        Text(
                            text = "Login as verified master artisan 'Meenakshi Ammal'",
                            fontSize = 11.sp,
                            color = HaathSeTextSecondary
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.loginDemoArtisan { onLoginSuccess(it) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HaathSeTerracotta),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_demo_artisan_quick_login")
                    ) {
                        Text("⚡ Demo Sign In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Register Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an artisan account? ",
                    style = MaterialTheme.typography.bodySmall,
                    color = HaathSeTextSecondary
                )
                Text(
                    text = "Register Now",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = HaathSeTerracotta
                    ),
                    modifier = Modifier
                        .clickable { onNavigateToRegister() }
                        .testTag("btn_artisan_goto_register")
                )
            }

            // Quick Explore Trade Fairs separate link if available
            if (onNavigateToEvents != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, HaathSeBorderLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToEvents() }
                        .testTag("btn_explore_trade_fairs_page")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(HaathSeTerracottaLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Festival,
                                    contentDescription = null,
                                    tint = HaathSeTerracottaDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Nearby Trade Fairs & Melas",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HaathSeTextPrimary
                                )
                                Text(
                                    text = "View upcoming govt stalls & exhibitions",
                                    fontSize = 11.sp,
                                    color = HaathSeTextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = HaathSeTerracotta,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Forgot Password / OTP Recovery Modal
    if (showForgotPasswordModal) {
        ForgotPasswordModal(
            viewModel = viewModel,
            onDismiss = { showForgotPasswordModal = false }
        )
    }
}

@Composable
private fun ForgotPasswordModal(
    viewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var demoOtpMessage by remember { mutableStateOf<String?>(null) }
    var resetError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LockReset, contentDescription = null, tint = HaathSeTerracotta)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Artisan OTP PIN Recovery", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "We will send an SMS OTP to your registered phone number to reset your security PIN.",
                    fontSize = 13.sp,
                    color = HaathSeTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it.filter { c -> c.isDigit() }.take(10) },
                    label = { Text("10-digit Phone") },
                    prefix = { Text("+91 ") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (otpSent) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { otpInput = it.take(4) },
                        label = { Text("4-Digit OTP") },
                        placeholder = { Text("1234") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { newPinInput = it.take(6) },
                        label = { Text("New Security PIN") },
                        placeholder = { Text("e.g. 5678") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (demoOtpMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = demoOtpMessage ?: "",
                        color = HaathSeGreenSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                if (resetError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = resetError ?: "", color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            if (!otpSent) {
                Button(
                    onClick = {
                        if (phoneInput.length == 10) {
                            otpSent = true
                            demoOtpMessage = "Demo OTP: 1234 (sent via SMS)"
                            resetError = null
                        } else {
                            resetError = "Please enter valid 10-digit phone"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HaathSeTerracotta)
                ) {
                    Text("Send OTP")
                }
            } else {
                Button(
                    onClick = {
                        if (otpInput == "1234" && newPinInput.length >= 4) {
                            viewModel.verifyAndResetPin(phoneInput, otpInput, newPinInput) {
                                onDismiss()
                            }
                        } else {
                            resetError = "Invalid OTP or PIN too short. (Demo OTP is 1234)"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HaathSeTerracotta)
                ) {
                    Text("Reset PIN")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
