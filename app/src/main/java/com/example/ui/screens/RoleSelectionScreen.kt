package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.AppRole
import com.example.data.models.SupportedLanguage
import com.example.ui.components.LanguageSelectionModal
import com.example.ui.components.VoiceButton
import com.example.ui.theme.*
import com.example.utils.AudioVoiceHelper
import com.example.utils.MultilingualManager

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoleSelectionScreen(
    currentLanguage: SupportedLanguage,
    onLanguageChange: (SupportedLanguage) -> Unit,
    onSelectRole: (AppRole) -> Unit,
    onNavigateArtisanLogin: () -> Unit = { onSelectRole(AppRole.ARTISAN) },
    onNavigateArtisanRegister: () -> Unit = { onSelectRole(AppRole.ARTISAN) },
    onNavigateBuyerLogin: () -> Unit = { onSelectRole(AppRole.BUYER) },
    onNavigateBuyerRegister: () -> Unit = { onSelectRole(AppRole.BUYER) },
    audioHelper: AudioVoiceHelper?,
    modifier: Modifier = Modifier
) {
    var showLanguageModal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(
                color = HaathSeCreamBg,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo & App Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TerracottaGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PanTool,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "HaathSe",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = HaathSeTerracottaDark,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            if (currentLanguage != SupportedLanguage.ENGLISH) {
                                Text(
                                    text = currentLanguage.nativeName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = HaathSeTextSecondary
                                    )
                                )
                            }
                        }
                    }

                    // Action Controls: Voice + Language Switcher
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VoiceButton(
                            textToSpeak = "${MultilingualManager.tr("Welcome to HaathSe", currentLanguage)}. ${MultilingualManager.tr("Choose your role to get started", currentLanguage)}",
                            language = currentLanguage,
                            audioHelper = audioHelper
                        )

                        Surface(
                            onClick = { showLanguageModal = true },
                            shape = PillShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, HaathSeTerracotta.copy(alpha = 0.4f)),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("btn_role_select_lang")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Language",
                                    tint = HaathSeTerracotta,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = currentLanguage.nativeName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HaathSeTextPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
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
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand Logo Emblem
            Card(
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                border = BorderStroke(2.dp, HaathSeTerracotta.copy(alpha = 0.35f)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .size(96.dp)
                    .padding(4.dp)
                    .testTag("img_haathse_brand_logo")
            ) {
                Image(
                    painter = painterResource(id = R.drawable.haathse_logo),
                    contentDescription = "HaathSe Emblem",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(18.dp))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Header
            Text(
                text = MultilingualManager.tr("Welcome to HaathSe", currentLanguage),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = HaathSeTextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = MultilingualManager.tr("Made by hand. Powered by people and AI.", currentLanguage),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    color = HaathSeTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ===================================================================
            // ROLE 1: ARTISAN STUDIO CARD (Rich Terracotta Clay Brown Theme)
            // ===================================================================
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4A1A0F)), // Warm rich brown theme
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                border = BorderStroke(1.5.dp, Color(0xFFE67E22).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable {
                        onSelectRole(AppRole.ARTISAN)
                        audioHelper?.speak(MultilingualManager.tr("role_artisan", currentLanguage), currentLanguage)
                    }
                    .testTag("card_role_artisan")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFFE65100), Color(0xFFB45309)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Artisan Icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = MultilingualManager.tr("I am an Artisan / Maker", currentLanguage),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.5.sp,
                                        color = Color(0xFFFFFFFF),
                                        lineHeight = 20.sp
                                    )
                                )
                                Text(
                                    text = "🧵 " + MultilingualManager.tr("role_artisan", currentLanguage),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD166)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = MultilingualManager.tr("Sell crafts, get fair prices, create digital passport", currentLanguage),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            color = Color(0xFFF3E5D8),
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Feature highlights using wrapping FlowRow with high contrast chips on brown
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FeatureChipOnDark(
                            label = MultilingualManager.tr("quick_sale", currentLanguage),
                            icon = Icons.Default.Mic,
                            accentColor = Color(0xFFFFAB91),
                            bgColor = Color(0xFF65291C)
                        )
                        FeatureChipOnDark(
                            label = MultilingualManager.tr("fair_value", currentLanguage),
                            icon = Icons.Default.AttachMoney,
                            accentColor = Color(0xFF86EFAC),
                            bgColor = Color(0xFF1E3A2B)
                        )
                        FeatureChipOnDark(
                            label = MultilingualManager.tr("Circles", currentLanguage),
                            icon = Icons.Default.Groups,
                            accentColor = Color(0xFFFDE047),
                            bgColor = Color(0xFF533917)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons Row with flexible sizing and strong contrast
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateArtisanLogin,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFFFD8BF)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0x22FFFFFF),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 40.dp)
                                .testTag("btn_role_artisan_login")
                        ) {
                            Text(
                                text = MultilingualManager.tr("Login", currentLanguage),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFFFFFFF),
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = onNavigateArtisanRegister,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9532F)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .heightIn(min = 40.dp)
                                .testTag("btn_role_artisan_register")
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    SupportedLanguage.TAMIL -> "ஸ்டுடியோ தொடங்கு →"
                                    SupportedLanguage.HINDI -> "स्टूडियो शुरू करें →"
                                    SupportedLanguage.TELUGU -> "స్టూడియో ప్రారంభించు →"
                                    SupportedLanguage.KANNADA -> "ಸ್ಟುಡಿಯೋ ಆರಂಭಿಸಿ →"
                                    SupportedLanguage.MALAYALAM -> "സ്റ്റുഡിയോ ആരംഭിക്കുക →"
                                    SupportedLanguage.BENGALI -> "স্টুডিও শুরু করুন →"
                                    SupportedLanguage.MARATHI -> "स्टुडिओ सुरू करा →"
                                    SupportedLanguage.GUJARATI -> "સ્ટુડિયો શરૂ કરો →"
                                    SupportedLanguage.PUNJABI -> "ਸਟੂਡੀਓ ਸ਼ੁਰੂ ਕਰੋ →"
                                    SupportedLanguage.ODIA -> "ଷ୍ଟୁଡିଓ ଆରମ୍ଭ କରନ୍ତୁ →"
                                    SupportedLanguage.ENGLISH -> "Enter Studio →"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.5.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ===================================================================
            // ROLE 2: BUYER MARKETPLACE CARD (Rich Dark Espresso Brown Theme)
            // ===================================================================
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF231B18)), // Dark Espresso Brown Theme
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                border = BorderStroke(1.5.dp, Color(0xFF14B8A6).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable {
                        onSelectRole(AppRole.BUYER)
                        audioHelper?.speak(MultilingualManager.tr("role_buyer", currentLanguage), currentLanguage)
                    }
                    .testTag("card_role_buyer")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF0284C7)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = "Buyer Icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = MultilingualManager.tr("I am a B2B Buyer / Retailer", currentLanguage),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.5.sp,
                                        color = Color(0xFFFFFFFF),
                                        lineHeight = 20.sp
                                    )
                                )
                                Text(
                                    text = "🛍️ " + MultilingualManager.tr("role_buyer", currentLanguage),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5EEAD4)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = MultilingualManager.tr("Source authentic handmade heritage goods directly", currentLanguage),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Feature highlights FlowRow
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FeatureChipOnDark(
                            label = MultilingualManager.tr("verified_badge", currentLanguage),
                            icon = Icons.Default.Verified,
                            accentColor = Color(0xFF5EEAD4),
                            bgColor = Color(0xFF134E48)
                        )
                        FeatureChipOnDark(
                            label = MultilingualManager.tr("Explore", currentLanguage),
                            icon = Icons.Default.Handshake,
                            accentColor = Color(0xFFFFCC80),
                            bgColor = Color(0xFF4E2B14)
                        )
                        FeatureChipOnDark(
                            label = MultilingualManager.tr("Post RFQ", currentLanguage),
                            icon = Icons.Default.PostAdd,
                            accentColor = Color(0xFF93C5FD),
                            bgColor = Color(0xFF1E3A5F)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateBuyerLogin,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, Color(0xFF5EEAD4)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0x22FFFFFF),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 40.dp)
                                .testTag("btn_role_buyer_login")
                        ) {
                            Text(
                                text = MultilingualManager.tr("Login", currentLanguage),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFFFFFFF),
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = onNavigateBuyerRegister,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .heightIn(min = 40.dp)
                                .testTag("btn_role_buyer_register")
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    SupportedLanguage.TAMIL -> "சந்தை செல்க →"
                                    SupportedLanguage.HINDI -> "बाज़ार देखें →"
                                    SupportedLanguage.TELUGU -> "మార్కెట్ చూడండి →"
                                    SupportedLanguage.KANNADA -> "ಮಾರುಕಟ್ಟೆ ನೋಡಿ →"
                                    SupportedLanguage.MALAYALAM -> "മാർക്കറ്റ് കാണുക →"
                                    SupportedLanguage.BENGALI -> "মার্কেট দেখুন →"
                                    SupportedLanguage.MARATHI -> "बाजार पहा →"
                                    SupportedLanguage.GUJARATI -> "બજાર જુઓ →"
                                    SupportedLanguage.PUNJABI -> "ਮਾਰਕੀਟ ਵੇਖੋ →"
                                    SupportedLanguage.ODIA -> "ବଜାର ଦେଖନ୍ତୁ →"
                                    SupportedLanguage.ENGLISH -> "Buyer Portal →"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.5.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ===================================================================
            // ROLE 3: ADMIN / MINISTRY HUB (Warm Golden Brown Theme)
            // ===================================================================
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF382315)), // Golden Earthy Brown
                border = BorderStroke(1.2.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        onSelectRole(AppRole.ADMIN)
                        audioHelper?.speak(MultilingualManager.tr("Ministry & NGO Monitor", currentLanguage), currentLanguage)
                    }
                    .testTag("card_role_admin")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF5A391D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = MultilingualManager.tr("Ministry & NGO Monitor", currentLanguage),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color(0xFFFFFBEB)
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = MultilingualManager.tr("Track artisan clusters, sales impact and GI verification", currentLanguage),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.5.sp,
                                color = Color(0xFFE5D5C5),
                                lineHeight = 16.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showLanguageModal) {
        LanguageSelectionModal(
            currentLanguage = currentLanguage,
            onLanguageSelected = {
                onLanguageChange(it)
                showLanguageModal = false
                audioHelper?.speak(it.voiceGreeting, it)
            },
            onDismiss = { showLanguageModal = false }
        )
    }
}

@Composable
private fun FeatureChipOnDark(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    bgColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(0.6.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

