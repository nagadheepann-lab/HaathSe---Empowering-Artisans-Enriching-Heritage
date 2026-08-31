package com.example.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.ui.components.HaathSePrimaryButton
import com.example.ui.components.LanguageSelectionModal
import com.example.ui.theme.*
import com.example.ui.viewmodels.AuthViewModel
import com.example.utils.AudioVoiceHelper

@Composable
fun BuyerRegisterScreen(
    viewModel: AuthViewModel,
    audioHelper: AudioVoiceHelper?,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onRegistrationSuccess: (AuthUser) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showLanguageModal by remember { mutableStateOf(false) }

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
                    modifier = Modifier.testTag("btn_buyer_reg_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = HaathSeIndigoDeep
                    )
                }

                Text(
                    text = "Buyer & Sourcing Account",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = HaathSeIndigoDeep
                    )
                )

                OutlinedButton(
                    onClick = { showLanguageModal = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = PillShape
                ) {
                    Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(14.dp), tint = HaathSeIndigoDeep)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(state.buyerLanguageInput.nativeName, fontSize = 12.sp, color = HaathSeIndigoDeep)
                }
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

            // Marketplace Emblem
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(IndigoPeacockGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Create Buyer Account",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = HaathSeIndigoDeep,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Direct-from-maker handmade crafts, verified GI provenance, and easy B2B bulk sourcing.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = HaathSeTextSecondary,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Form Inputs
            OutlinedTextField(
                value = state.buyerNameInput,
                onValueChange = { viewModel.updateBuyerName(it) },
                label = { Text("Full Name / Business Name *") },
                placeholder = { Text("e.g. Aarav Sharma / FabCraft Ltd") },
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
                    .testTag("input_buyer_name")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = state.buyerPhoneInput,
                onValueChange = { viewModel.updateBuyerPhone(it) },
                label = { Text("Mobile Number *") },
                prefix = { Text("🇮🇳 +91 ", fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = HaathSePeacockTeal)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HaathSeIndigoDeep,
                    focusedLabelColor = HaathSeIndigoDeep,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_buyer_phone")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = state.buyerEmailInput,
                onValueChange = { viewModel.updateBuyerEmail(it) },
                label = { Text("Email Address (Optional for Invoices)") },
                placeholder = { Text("e.g. aarav@example.com") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = HaathSePeacockTeal)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HaathSeIndigoDeep,
                    focusedLabelColor = HaathSeIndigoDeep,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_buyer_email")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = state.buyerPasswordInput,
                onValueChange = { viewModel.updateBuyerPassword(it) },
                label = { Text("Password / PIN *") },
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
                    viewModel.submitBuyerRegistration { onRegistrationSuccess(it) }
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_buyer_password")
            )

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
                    viewModel.submitBuyerRegistration { onRegistrationSuccess(it) }
                },
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = HaathSeIndigoDeep),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_buyer_register_submit")
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text(
                        text = "Create Account & Explore Crafts →",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have a buyer account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HaathSeTextSecondary
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = HaathSeIndigoDeep
                    ),
                    modifier = Modifier
                        .clickable { onNavigateToLogin() }
                        .testTag("btn_buyer_goto_login")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showLanguageModal) {
        LanguageSelectionModal(
            currentLanguage = state.buyerLanguageInput,
            onLanguageSelected = {
                viewModel.updateBuyerLanguage(it)
                showLanguageModal = false
            },
            onDismiss = { showLanguageModal = false }
        )
    }
}
