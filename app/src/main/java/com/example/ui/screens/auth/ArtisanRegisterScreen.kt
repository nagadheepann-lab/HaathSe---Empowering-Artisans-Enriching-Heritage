package com.example.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.example.data.models.VerificationStatus
import com.example.ui.components.HaathSePrimaryButton
import com.example.ui.components.VoiceButton
import com.example.ui.theme.*
import com.example.ui.viewmodels.AuthUiState
import com.example.ui.viewmodels.AuthViewModel
import com.example.utils.AudioVoiceHelper

@Composable
fun ArtisanRegisterScreen(
    viewModel: AuthViewModel,
    audioHelper: AudioVoiceHelper?,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onRegistrationSuccess: (AuthUser) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HaathSeCreamBg)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (state.artisanStep > 1) {
                                viewModel.prevArtisanStep()
                            } else {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("btn_artisan_reg_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = HaathSeTerracottaDark
                        )
                    }

                    // Multi-step indicator bar
                    ArtisanStepIndicator(
                        currentStep = state.artisanStep,
                        totalSteps = 5,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )

                    VoiceButton(
                        textToSpeak = getStepVoiceText(state.artisanStep, state.artisanLanguageInput),
                        language = state.artisanLanguageInput,
                        audioHelper = audioHelper
                    )
                }
            }
        },
        containerColor = HaathSeCreamBg
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Step title & badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(HaathSeTerracottaLight)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "STEP ${state.artisanStep} OF 5 • ${getStepBadge(state.artisanStep)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HaathSeTerracottaDark,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Animated step transitions
                AnimatedContent(
                    targetState = state.artisanStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn(tween(300))).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut(tween(300))
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn(tween(300))).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut(tween(300))
                            )
                        }
                    },
                    label = "artisan_reg_step"
                ) { step ->
                    when (step) {
                        1 -> Step1Name(viewModel = viewModel, state = state)
                        2 -> Step2Phone(viewModel = viewModel, state = state)
                        3 -> Step3Security(viewModel = viewModel, state = state)
                        4 -> Step4Language(viewModel = viewModel, state = state)
                        5 -> Step5ArtisanId(viewModel = viewModel, state = state)
                    }
                }

                if (state.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
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

                Spacer(modifier = Modifier.height(32.dp))

                // Bottom Action buttons
                if (state.artisanStep < 5) {
                    HaathSePrimaryButton(
                        text = "Continue →",
                        onClick = {
                            viewModel.nextArtisanStep()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_artisan_step_next")
                    )
                } else {
                    HaathSePrimaryButton(
                        text = "Complete Registration & Verify 🧵",
                        onClick = {
                            viewModel.submitArtisanRegistration { user ->
                                onRegistrationSuccess(user)
                            }
                        },
                        isLoading = state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_artisan_complete_reg")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sign in switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Already have an artisan account? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HaathSeTextSecondary
                    )
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = HaathSeTerracotta
                        ),
                        modifier = Modifier
                            .clickable { onNavigateToLogin() }
                            .testTag("btn_artisan_goto_login")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// =======================================================================
// STEP 1: NAME & REGIONAL LOCATION
// =======================================================================
@Composable
private fun Step1Name(viewModel: AuthViewModel, state: AuthUiState) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.detectGpsLocation(context)
    }

    val craftClusters = listOf(
        "Kanchipuram, Tamil Nadu" to Pair(12.8342, 79.7036),
        "Jaipur, Rajasthan" to Pair(26.9124, 75.7873),
        "Varanasi, UP" to Pair(25.3176, 82.9739),
        "Pochampally, Telangana" to Pair(17.3457, 78.8167),
        "Bastar, Chhattisgarh" to Pair(19.0734, 82.0287),
        "Mysore, Karnataka" to Pair(12.2958, 76.6394)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Friendly Illustration Emblem
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(TerracottaGradient)
                .border(3.dp, HaathSeMarigoldLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome, Master Artisan",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = HaathSeTerracottaDark,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Tell us your name and workshop location so we can connect you with nearby Trade Fairs & Buyers.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = HaathSeTextSecondary,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Full Name Field
        OutlinedTextField(
            value = state.artisanNameInput,
            onValueChange = { viewModel.updateArtisanName(it) },
            label = { Text("Your Full Name (e.g. Meenakshi Ammal)") },
            placeholder = { Text("e.g. Rameshwar Prajapati") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null, tint = HaathSeTerracotta)
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HaathSeTerracotta,
                focusedLabelColor = HaathSeTerracotta,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_artisan_name"),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Location & Craft Cluster Section
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, HaathSeBorderLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = HaathSeTerracotta, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Workshop / Craft Cluster",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = HaathSeTextPrimary
                        )
                    }

                    // Live GPS Detection Button
                    TextButton(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("btn_detect_gps_location")
                    ) {
                        if (state.isDetectingGps) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = HaathSeTerracotta)
                            Spacer(modifier = Modifier.width(4.dp))
                        } else {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = HaathSeTerracotta, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = if (state.isDetectingGps) "Detecting..." else "Use GPS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HaathSeTerracotta
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.artisanLocationInput,
                    onValueChange = { viewModel.updateArtisanLocation(it) },
                    placeholder = { Text("e.g. Kanchipuram, Tamil Nadu") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HaathSeTerracotta,
                        unfocusedContainerColor = HaathSeCreamBg.copy(alpha = 0.5f),
                        focusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_artisan_location")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Quick Select Craft Cluster:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HaathSeTextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Cluster Tags
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    craftClusters.forEach { (cluster, coords) ->
                        val isSelected = state.artisanLocationInput.contains(cluster.substringBefore(","))
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateArtisanLocation(cluster, coords.first, coords.second)
                            },
                            label = {
                                Text(
                                    text = cluster,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HaathSeTerracottaLight,
                                selectedLabelColor = HaathSeTerracottaDark
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) HaathSeTerracotta else HaathSeBorderLight
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HaathSeMarigoldLight.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Explore, contentDescription = null, tint = HaathSeMarigoldDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Used to show you nearest Trade Fairs, Govt Melas & Stall Subsidies.",
                            fontSize = 11.sp,
                            color = HaathSeTextSecondary
                        )
                    }
                }
            }
        }
    }
}

// =======================================================================
// STEP 2: PHONE
// =======================================================================
@Composable
private fun Step2Phone(viewModel: AuthViewModel, state: AuthUiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(HaathSeMarigold, HaathSeTerracotta))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhoneIphone,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Your phone number",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = HaathSeTerracottaDark,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We'll use your phone number to help you sign in securely. No complicated email required.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = HaathSeTextSecondary,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = state.artisanPhoneInput,
            onValueChange = { viewModel.updateArtisanPhone(it) },
            label = { Text("10-digit Mobile Number") },
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
            shape = RoundedCornerShape(16.dp),
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
            keyboardActions = KeyboardActions(onNext = { viewModel.nextArtisanStep() }),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_artisan_phone")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(HaathSeSand.copy(alpha = 0.5f))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = HaathSeMarigoldDark,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Phone numbers are encrypted and never shared publicly without consent.",
                style = MaterialTheme.typography.labelSmall,
                color = HaathSeTextSecondary
            )
        }
    }
}

// =======================================================================
// STEP 3: SECURITY (PIN/PASSWORD)
// =======================================================================
@Composable
private fun Step3Security(viewModel: AuthViewModel, state: AuthUiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(HaathSePeacockTeal, HaathSeTerracotta))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.VpnKey,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Create your security PIN",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = HaathSeTerracottaDark,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Set a 4 to 6-digit easy PIN or password to keep your earnings and orders secure.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = HaathSeTextSecondary,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = state.artisanPinInput,
            onValueChange = { viewModel.updateArtisanPin(it) },
            label = { Text("Security PIN or Password") },
            placeholder = { Text("e.g. 1234") },
            leadingIcon = {
                Icon(Icons.Default.Password, contentDescription = null, tint = HaathSeTerracotta)
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
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HaathSeTerracotta,
                focusedLabelColor = HaathSeTerracotta,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { viewModel.nextArtisanStep() }),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_artisan_pin")
        )
    }
}

// =======================================================================
// STEP 4: LANGUAGE SELECTION
// =======================================================================
@Composable
private fun Step4Language(viewModel: AuthViewModel, state: AuthUiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(HaathSeGold, HaathSeTerracotta))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Which language would you like to use?",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = HaathSeTerracottaDark,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Select your preferred tongue. HaathSe will adapt all voice commands, AI tools, and voice assistance.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = HaathSeTextSecondary,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Large Language Selection Grid
        val languages = SupportedLanguage.values().toList()
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            languages.chunked(2).forEach { rowLangs ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowLangs.forEach { lang ->
                        val isSelected = state.artisanLanguageInput == lang
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) HaathSeTerracottaLight else Color.White
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) HaathSeTerracotta else HaathSeCardBorder
                            ),
                            elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(74.dp)
                                .clickable {
                                    viewModel.updateArtisanLanguage(lang)
                                }
                                .testTag("lang_card_${lang.code}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = lang.nativeName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) HaathSeTerracottaDark else HaathSeTextPrimary
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = HaathSeTerracotta,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = lang.englishName,
                                    fontSize = 12.sp,
                                    color = if (isSelected) HaathSeTerracotta else HaathSeTextSecondary
                                )
                            }
                        }
                    }
                    if (rowLangs.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// =======================================================================
// STEP 5: ARTISAN VERIFICATION ID
// =======================================================================
@Composable
private fun Step5ArtisanId(viewModel: AuthViewModel, state: AuthUiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(HaathSeGreenSuccess, HaathSePeacockTeal))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Verify your artisan identity",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = HaathSeTerracottaDark,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "HaathSe is dedicated to genuine artisans and craft makers. Your government-issued Artisan ID (Pehchan Card) helps us verify eligibility.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = HaathSeTextSecondary,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.artisanIdInput,
            onValueChange = { viewModel.updateArtisanId(it) },
            label = { Text("Government Artisan ID / Pehchan No.") },
            placeholder = { Text("e.g. DEMO-PEHCHAN-88219") },
            leadingIcon = {
                Icon(Icons.Default.Badge, contentDescription = null, tint = HaathSeGreenSuccess)
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HaathSeGreenSuccess,
                focusedLabelColor = HaathSeGreenSuccess,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_artisan_gov_id")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Demo Helper Quick-Fill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Demo Prototype Testing:",
                fontSize = 12.sp,
                color = HaathSeTextTertiary
            )

            TextButton(
                onClick = { viewModel.updateArtisanId("DEMO-PEHCHAN-88219") },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Insert Fictional Demo ID",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = HaathSeTerracotta
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prominent Verified Artisan Demo Badge & Privacy Box
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HaathSeGreenLight.copy(alpha = 0.6f)),
            border = BorderStroke(1.dp, HaathSeGreenSuccess.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(HaathSeGreenSuccess),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "✓ Verified Artisan Badge",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = HaathSeForestGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Privacy Notice: Your government ID is securely encrypted and used solely for authentic craft provenance validation. We never expose your private documents or IDs publicly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF166534),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Demo Mode: For prototype evaluation, fictional IDs are accepted and clearly labeled as Demo Verification.",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF14532D)
                )
            }
        }
    }
}

// =======================================================================
// STEP INDICATOR HELPER
// =======================================================================
@Composable
private fun ArtisanStepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalSteps) {
            val isCompleted = i < currentStep
            val isCurrent = i == currentStep
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(PillShape)
                    .background(
                        when {
                            isCurrent -> HaathSeTerracotta
                            isCompleted -> HaathSeMarigold
                            else -> HaathSeCardBorder
                        }
                    )
            )
        }
    }
}

private fun getStepBadge(step: Int): String {
    return when (step) {
        1 -> "PERSONAL"
        2 -> "PHONE"
        3 -> "SECURITY"
        4 -> "LANGUAGE"
        5 -> "ARTISAN ID"
        else -> "STEP"
    }
}

private fun getStepVoiceText(step: Int, lang: SupportedLanguage): String {
    return when (step) {
        1 -> "Step 1. What should we call you? Please type your name."
        2 -> "Step 2. Please enter your 10 digit mobile number."
        3 -> "Step 3. Create your secure security PIN."
        4 -> "Step 4. Which language would you like to use?"
        5 -> "Step 5. Verify your artisan identity with your government ID."
        else -> "Artisan Registration"
    }
}
