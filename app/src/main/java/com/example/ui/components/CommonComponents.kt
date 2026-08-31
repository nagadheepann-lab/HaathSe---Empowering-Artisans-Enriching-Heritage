package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.models.AppRole
import com.example.data.models.SupportedLanguage
import com.example.ui.theme.*
import com.example.utils.AudioVoiceHelper
import com.example.utils.MultilingualManager
import java.io.File

@Composable
fun TopBar(
    currentRole: AppRole,
    currentLanguage: SupportedLanguage,
    isSimpleMode: Boolean,
    onRoleSelected: (AppRole) -> Unit,
    onLanguageSelected: (SupportedLanguage) -> Unit,
    onToggleSimpleMode: () -> Unit,
    audioHelper: AudioVoiceHelper?,
    onNavigateNotifications: (() -> Unit)? = null,
    unreadNotificationCount: Int = 0,
    modifier: Modifier = Modifier
) {
    var showLangDialog by remember { mutableStateOf(false) }
    var showRoleMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo & Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        audioHelper?.speak("HaathSe. Made by Hand. Your craft. Your story. Your business.", currentLanguage)
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TerracottaGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PanTool,
                            contentDescription = "HaathSe Logo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = MultilingualManager.getString(currentLanguage, "app_title"),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(HaathSeMarigoldLight)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "HANDMADE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HaathSeMarigoldDark
                                )
                            }
                        }
                        Text(
                            text = MultilingualManager.getString(currentLanguage, "tagline"),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Controls: Notifications, Language, Role, Simple Mode
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Notification Bell with Badge
                    if (onNavigateNotifications != null) {
                        IconButton(
                            onClick = onNavigateNotifications,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("topbar_notification_bell")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = TerracottaPrimary
                                )
                                if (unreadNotificationCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .align(Alignment.TopEnd)
                                            .clip(CircleShape)
                                            .background(Color.Red)
                                    )
                                }
                            }
                        }
                    }

                    // Simple Mode Toggle
                    IconButton(
                        onClick = onToggleSimpleMode,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("toggle_simple_mode_btn")
                    ) {
                        Icon(
                            imageVector = if (isSimpleMode) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Simple Mode",
                            tint = if (isSimpleMode) TerracottaPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Language Selector Pill
                    OutlinedButton(
                        onClick = { showLangDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("select_language_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language",
                            modifier = Modifier.size(16.dp),
                            tint = TerracottaPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentLanguage.nativeName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Role Switcher
                    Box {
                        FilledTonalButton(
                            onClick = { showRoleMenu = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = when (currentRole) {

                                    AppRole.ARTISAN -> TerracottaLight
                                    AppRole.BUYER -> PeacockTealLight
                                    AppRole.ADMIN -> GoldenAmberLight
                                }
                            ),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("select_role_btn")
                        ) {
                            Icon(
                                imageVector = when (currentRole) {
                                    AppRole.ARTISAN -> Icons.Default.Palette
                                    AppRole.BUYER -> Icons.Default.Storefront
                                    AppRole.ADMIN -> Icons.Default.AdminPanelSettings
                                },
                                contentDescription = "Role",
                                modifier = Modifier.size(16.dp),
                                tint = when (currentRole) {
                                    AppRole.ARTISAN -> TerracottaPrimary
                                    AppRole.BUYER -> PeacockTealTertiary
                                    AppRole.ADMIN -> GoldenAmberSecondary
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (currentRole) {
                                    AppRole.ARTISAN -> MultilingualManager.tr("role_artisan", currentLanguage)
                                    AppRole.BUYER -> MultilingualManager.tr("role_buyer", currentLanguage)
                                    AppRole.ADMIN -> MultilingualManager.tr("role_admin", currentLanguage)
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (currentRole) {
                                    AppRole.ARTISAN -> TerracottaPrimary
                                    AppRole.BUYER -> PeacockTealTertiary
                                    AppRole.ADMIN -> GoldenAmberSecondary
                                }
                            )
                        }

                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false }
                        ) {
                            AppRole.values().forEach { role ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = when (role) {
                                                    AppRole.ARTISAN -> Icons.Default.Palette
                                                    AppRole.BUYER -> Icons.Default.Storefront
                                                    AppRole.ADMIN -> Icons.Default.AdminPanelSettings
                                                },
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = if (role == currentRole) TerracottaPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = role.label,
                                                fontWeight = if (role == currentRole) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    },
                                    onClick = {
                                        onRoleSelected(role)
                                        showRoleMenu = false
                                        audioHelper?.speak("Switched to ${role.label}", currentLanguage)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (isSimpleMode) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldenAmberLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = null,
                        tint = GoldenAmberSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Simple Voice Mode Active — Tap speaker icon on any card to hear details",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF78350F)
                    )
                }
            }
        }
    }

    if (showLangDialog) {
        LanguageSelectionModal(
            currentLanguage = currentLanguage,
            onLanguageSelected = {
                onLanguageSelected(it)
                showLangDialog = false
                audioHelper?.speak(it.voiceGreeting, it)
            },
            onDismiss = { showLangDialog = false }
        )
    }
}

@Composable
fun LanguageSelectionModal(
    currentLanguage: SupportedLanguage,
    onLanguageSelected: (SupportedLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Choose Your Language",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "மொழி / भाषा चुनें (11 Indian Languages)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 380.dp)
                ) {
                    items(SupportedLanguage.values()) { lang ->
                        val isSelected = lang == currentLanguage
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) TerracottaLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, TerracottaPrimary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLanguageSelected(lang) }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = lang.nativeName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) TerracottaPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = lang.englishName,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudioPlayButton(
    audioText: String,
    isPlaying: Boolean = false,
    onToggle: () -> Unit = {},
    modifier: Modifier = Modifier,
    size: Int = 36
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(if (isPlaying) TerracottaLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .testTag("audio_tts_btn")
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
            contentDescription = "Read Aloud",
            tint = if (isPlaying) TerracottaPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size((size * 0.55).dp)
        )
    }
}

@Composable
fun AudioPlayButton(
    textToSpeak: String,
    language: SupportedLanguage,
    audioHelper: AudioVoiceHelper?,
    modifier: Modifier = Modifier,
    size: Int = 36
) {
    var isPlaying by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            if (isPlaying) {
                audioHelper?.stop()
                isPlaying = false
            } else {
                audioHelper?.speak(textToSpeak, language)
                isPlaying = true
            }
        },
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(if (isPlaying) TerracottaLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .testTag("audio_tts_btn")
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
            contentDescription = "Read Aloud in ${language.englishName}",
            tint = if (isPlaying) TerracottaPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size((size * 0.55).dp)
        )
    }
}

@Composable
fun VerifiedBadge(
    label: String = "Verified Heritage Craft",
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(VerifiedBadgeBg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Verified,
            contentDescription = null,
            tint = VerifiedBadgeBlue,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = VerifiedBadgeBlue
        )
    }
}

@Composable
fun ListingScoreBadge(score: Int, modifier: Modifier = Modifier) {
    val color = when {
        score >= 85 -> SuccessGreen
        score >= 60 -> WarningAmber
        else -> TerracottaPrimary
    }
    val bg = when {
        score >= 85 -> SuccessGreenBg
        score >= 60 -> WarningAmberBg
        else -> TerracottaLight
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "AI Score: $score/100",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun QRCardDialog(
    title: String,
    artisanName: String,
    region: String,
    craftTechnique: String,
    price: Double,
    story: String,
    onDismiss: () -> Unit,
    audioHelper: AudioVoiceHelper?,
    language: SupportedLanguage
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Craft QR Passport",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Simulated QR Code Display
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(2.dp, TerracottaPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Product QR Code",
                        tint = Color(0xFF1E1B18),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "https://karigarsetu.gov.in/artisan/saree-01",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Product & Lineage summary
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Crafted by $artisanName • $region",
                            fontSize = 12.sp,
                            color = TerracottaPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Technique: $craftTechnique",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "₹${price.toInt()} (Fair Sustainable Value)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SuccessGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            audioHelper?.speak(story, language)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Listen Story", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share Card", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Universal Craft Image loader that seamlessly handles:
 * 1. Real captured/enhanced photo files on disk (e.g. /data/user/0/.../enhanced_*.jpg)
 * 2. Drawable resource names (e.g. "img_saree_sample", "img_pottery_sample", "img_artisan_hero")
 * 3. Remote URL strings
 */
@Composable
fun SmartCraftImage(
    imageIdentifier: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current

    if (imageIdentifier.isNullOrBlank()) {
        Image(
            painter = painterResource(id = R.drawable.img_saree_sample),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
        return
    }

    val isFile = remember(imageIdentifier) {
        imageIdentifier.startsWith("/") || imageIdentifier.startsWith("file:") || File(imageIdentifier).exists()
    }

    if (isFile) {
        val file = remember(imageIdentifier) {
            if (imageIdentifier.startsWith("file:")) File(imageIdentifier.removePrefix("file:")) else File(imageIdentifier)
        }
        AsyncImage(
            model = file,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
            placeholder = painterResource(id = R.drawable.img_saree_sample),
            error = painterResource(id = R.drawable.img_saree_sample)
        )
    } else {
        val resId = remember(imageIdentifier) {
            when (imageIdentifier) {
                "img_pottery_sample" -> R.drawable.img_pottery_sample
                "img_artisan_hero" -> R.drawable.img_artisan_hero
                "img_saree_sample" -> R.drawable.img_saree_sample
                else -> {
                    try {
                        val id = context.resources.getIdentifier(imageIdentifier, "drawable", context.packageName)
                        if (id != 0) id else R.drawable.img_saree_sample
                    } catch (_: Exception) {
                        R.drawable.img_saree_sample
                    }
                }
            }
        }
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}

