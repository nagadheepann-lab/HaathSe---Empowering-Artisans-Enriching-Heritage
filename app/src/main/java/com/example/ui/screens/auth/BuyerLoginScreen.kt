package com.example.ui.screens.auth

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AuthUser
import com.example.data.models.SupportedLanguage
import com.example.ui.components.HaathSeSecondaryButton
import com.example.ui.theme.*
import com.example.ui.viewmodels.AuthViewModel
import com.example.utils.AudioVoiceHelper

@Composable
fun BuyerLoginScreen(
    viewModel: AuthViewModel,
    audioHelper: AudioVoiceHelper?,
    onNavigateBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (AuthUser) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var isOtpMode by remember { mutableStateOf(false) }
    var demoOtpSent by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HaathSeCreamBg)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("btn_buyer_login_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = HaathSeIndigoDeep
                    )
                }

                Text(
                    text = "Buyer Sign In",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = HaathSeIndigoDeep
                    )
                )

                Spacer(modifier = Modifier.width(48.dp))
            }
        },
        containerColor = HaathSeCreamBg
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // HaathSe Brand Emblem
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, HaathSeIndigoLight, CircleShape)
                    .shadow(3.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.haathse_logo),
                    contentDescription = "HaathSe Logo",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Welcome Back, Patron",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = HaathSeIndigoDeep,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Sign in to track handcrafted orders, place RFQs, and verify artisan GI certificates.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = HaathSeTextSecondary,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Toggle Password vs OTP Mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PillShape)
                    .background(HaathSeSand)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(PillShape)
                        .background(if (!isOtpMode) Color.White else Color.Transparent)
                        .clickable { isOtpMode = false }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Password / PIN",
                        fontWeight = if (!isOtpMode) FontWeight.Bold else FontWeight.Normal,
                        color = if (!isOtpMode) HaathSeIndigoDeep else HaathSeTextSecondary,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(PillShape)
                        .background(if (isOtpMode) Color.White else Color.Transparent)
                        .clickable { isOtpMode = true }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SMS OTP Sign In",
                        fontWeight = if (isOtpMode) FontWeight.Bold else FontWeight.Normal,
                        color = if (isOtpMode) HaathSeIndigoDeep else HaathSeTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Phone or Email Input
            OutlinedTextField(
                value = state.loginPhoneOrEmailInput,
                onValueChange = { viewModel.updateLoginInput(it) },
                label = { Text("Mobile Number or Email Address") },
                placeholder = { Text("e.g. 9123456780 or buyer@example.com") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = HaathSePeacockTeal)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HaathSeIndigoDeep,
                    focusedLabelColor = HaathSeIndigoDeep,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_buyer_login_credential")
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (!isOtpMode) {
                // Password Input
                OutlinedTextField(
                    value = state.loginPasswordOrPinInput,
                    onValueChange = { viewModel.updateLoginPassword(it) },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = HaathSePeacockTeal)
                    },
                    trailingIcon = {
                        IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                            Icon(
                                imageVector = if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = HaathSeTextSecondary
                            )
                        }
                    },
                    visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HaathSeIndigoDeep,
                        focusedLabelColor = HaathSeIndigoDeep,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        viewModel.submitBuyerLogin { onLoginSuccess(it) }
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_buyer_login_password")
                )
            } else {
                // OTP Input Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.loginPasswordOrPinInput,
                        onValueChange = { viewModel.updateLoginPassword(it) },
                        label = { Text("4-Digit OTP") },
                        placeholder = { Text("1234") },
                        leadingIcon = {
                            Icon(Icons.Default.Pin, contentDescription = null, tint = HaathSePeacockTeal)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HaathSeIndigoDeep,
                            focusedLabelColor = HaathSeIndigoDeep,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_buyer_otp")
                    )

                    Button(
                        onClick = {
                            demoOtpSent = true
                            viewModel.updateLoginPassword("1234")
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HaathSePeacockTeal),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Text(if (demoOtpSent) "Resend" else "Get OTP", fontSize = 12.sp)
                    }
                }

                if (demoOtpSent) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Demo Mode OTP: 1234 (auto-filled)",
                        color = HaathSeGreenSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            if (state.error != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = state.error ?: "", color = Color(0xFF991B1B), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.submitBuyerLogin { onLoginSuccess(it) }
                },
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = HaathSeIndigoDeep),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_buyer_login_submit")
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text(
                        text = "Sign In to Marketplace →",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Demo Buyer Instant Login Box
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = HaathSePeacockLight.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, HaathSePeacockTeal.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = HaathSePeacockTeal, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Instant Prototype Evaluation",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Sign in directly as corporate conscious buyer 'Aarav Sharma' with active cart, RFQs, and order history.",
                        fontSize = 12.sp,
                        color = HaathSeTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.loginDemoBuyer { onLoginSuccess(it) }
                        },
                        shape = ButtonShape,
                        border = BorderStroke(1.dp, HaathSePeacockTeal),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_demo_buyer_quick_login")
                    ) {
                        Text(
                            text = "⚡ Enter as Demo Buyer",
                            fontWeight = FontWeight.Bold,
                            color = HaathSePeacockTeal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have a buyer account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HaathSeTextSecondary
                )
                Text(
                    text = "Register",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = HaathSeIndigoDeep
                    ),
                    modifier = Modifier
                        .clickable { onNavigateToRegister() }
                        .testTag("btn_buyer_goto_register")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
