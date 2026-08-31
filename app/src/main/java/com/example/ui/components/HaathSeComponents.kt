package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.AppRole
import com.example.data.models.SupportedLanguage
import com.example.ui.theme.*
import com.example.utils.AudioVoiceHelper
import com.example.utils.MultilingualManager

// =======================================================================
// 1. HAATHSE TOP BAR
// =======================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HaathSeTopBar(
    title: String,
    subtitle: String? = null,
    currentLanguage: SupportedLanguage = SupportedLanguage.ENGLISH,
    onLanguageClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    voiceGuidanceText: String? = null,
    audioHelper: AudioVoiceHelper? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = HaathSeDimens.elevationLow,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (onBackClick != null) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .testTag("btn_back")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (voiceGuidanceText != null && audioHelper != null) {
                        VoiceButton(
                            textToSpeak = voiceGuidanceText,
                            language = currentLanguage,
                            audioHelper = audioHelper
                        )
                    }

                    if (onLanguageClick != null) {
                        OutlinedButton(
                            onClick = onLanguageClick,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = PillShape,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(listOf(HaathSeTerracotta, HaathSeMarigold))
                            ),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("btn_topbar_lang")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Language Selection",
                                modifier = Modifier.size(16.dp),
                                tint = HaathSeTerracotta
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentLanguage.nativeName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    actions()
                }
            }
        }
    }
}

// =======================================================================
// 2. HAATHSE BUTTONS (Primary & Secondary with 48dp+ touch target)
// =======================================================================

@Composable
fun HaathSePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    testTag: String = "haathse_primary_btn"
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = HaathSeTerracotta,
            contentColor = Color.White,
            disabledContainerColor = HaathSeTerracotta.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = HaathSeDimens.buttonHeight)
            .testTag(testTag)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Processing...",
                style = MaterialTheme.typography.labelLarge.copy(color = Color.White)
            )
        } else {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
fun HaathSeSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    containerColor: Color = HaathSeSaffronLight,
    contentColor: Color = HaathSeSaffron,
    testTag: String = "haathse_secondary_btn"
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag(testTag)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        )
    }
}

// =======================================================================
// 3. HAATHSE CARDS & SURFACES
// =======================================================================

@Composable
fun HaathSeCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    elevation: Dp = HaathSeDimens.elevationCard,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clip(CardShape)
            .clickable(onClick = onClick)
    } else {
        modifier.fillMaxWidth()
    }

    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(elevation),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = cardModifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HaathSeDimens.cardContentPadding),
            content = content
        )
    }
}

// =======================================================================
// 4. METRIC CARD (For Business Dashboard, Growth & Sales Stats)
// =======================================================================

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    accentColor: Color = HaathSeTerracotta,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    HaathSeCard(
        modifier = modifier,
        onClick = onClick,
        backgroundColor = backgroundColor
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// =======================================================================
// 5. SECTION HEADER
// =======================================================================

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (actionText != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.heightIn(min = 36.dp)
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = HaathSeTerracotta
                    )
                )
            }
        }
    }
}

// =======================================================================
// 6. PRODUCT CARD (Rich craft photography, price, tags, and QR trigger)
// =======================================================================

@Composable
fun ProductCard(
    title: String,
    artisanName: String,
    region: String,
    price: Double,
    imageUrl: String,
    craftCategory: String,
    isGiTagged: Boolean = false,
    stockCount: Int = 1,
    onProductClick: () -> Unit,
    onQrClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(HaathSeDimens.elevationCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .clickable(onClick = onProductClick)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HaathSeDimens.productCardImageHeight)
                    .background(HaathSeSand)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top Badge Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(HaathSeIndigoDeep.copy(alpha = 0.85f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = craftCategory,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (isGiTagged) {
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(HaathSeMarigold)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "GI Tagged",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // QR trigger
                if (onQrClick != null) {
                    IconButton(
                        onClick = onQrClick,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "Craft QR Passport",
                            tint = HaathSeTerracotta,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Info Content
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "By $artisanName • $region",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "₹${price.toInt()}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = HaathSeTerracotta
                            )
                        )
                        Text(
                            text = if (stockCount > 0) "$stockCount ready to ship" else "Made to Order",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (stockCount > 0) HaathSeGreenSuccess else HaathSeMarigoldDark
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(HaathSeTerracottaLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Fair Price",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HaathSeTerracottaDark
                        )
                    }
                }
            }
        }
    }
}

// =======================================================================
// 7. ARTISAN PROFILE CARD
// =======================================================================

@Composable
fun ArtisanCard(
    artisanName: String,
    craftType: String,
    region: String,
    experienceYears: Int,
    trustScore: Int,
    isPehchanVerified: Boolean,
    avatarUrl: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HaathSeCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(HaathSeDimens.avatarLargeSize)
                    .clip(CircleShape)
                    .background(HaathSeSand),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = artisanName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = HaathSeTerracotta,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = artisanName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (isPehchanVerified) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Govt Pehchan Verified",
                            tint = HaathSePeacock,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = "$craftType Master • $region",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$experienceYears+ Years Heritage Lineage",
                    style = MaterialTheme.typography.labelSmall,
                    color = HaathSeTerracotta,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrustScoreBadge(score = trustScore)
                    if (isPehchanVerified) {
                        VerificationBadge(label = "Pehchan ID")
                    }
                }
            }
        }
    }
}

// =======================================================================
// 8. TRUST SCORE & VERIFICATION BADGES
// =======================================================================

@Composable
fun TrustScoreBadge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val color = when {
        score >= 85 -> HaathSeGreenSuccess
        score >= 60 -> HaathSeMarigold
        else -> HaathSeTerracotta
    }
    val bg = when {
        score >= 85 -> HaathSeGreenLight
        score >= 60 -> HaathSeMarigoldLight
        else -> HaathSeTerracottaLight
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Trust: $score/100",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun VerificationBadge(
    label: String = "Verified",
    icon: ImageVector = Icons.Default.VerifiedUser,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(HaathSePeacockLight)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HaathSePeacock,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0369A1)
        )
    }
}

// =======================================================================
// 9. VOICE BUTTON (Text-to-Speech Accessibility guidance)
// =======================================================================

@Composable
fun VoiceButton(
    textToSpeak: String,
    language: SupportedLanguage,
    audioHelper: AudioVoiceHelper?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    var isSpeaking by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            if (isSpeaking) {
                audioHelper?.stop()
                isSpeaking = false
            } else {
                audioHelper?.speak(textToSpeak, language)
                isSpeaking = true
            }
        },
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (isSpeaking) HaathSeTerracottaLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .testTag("voice_guidance_btn")
    ) {
        Icon(
            imageVector = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
            contentDescription = "Read Aloud in ${language.englishName}",
            tint = if (isSpeaking) HaathSeTerracotta else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

// =======================================================================
// 10. AI INSIGHT CARD (AI Saathi / Market Intelligence / Demand trends)
// =======================================================================

@Composable
fun AIInsightCard(
    title: String,
    insight: String,
    confidenceLabel: String = "AI Estimated",
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, HaathSePeacockTeal.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(HaathSeDimens.elevationCard),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(HaathSePeacockLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = HaathSePeacockTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(HaathSeSand)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = confidenceLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = insight,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HaathSeSecondaryButton(
                    text = actionLabel,
                    onClick = onActionClick,
                    containerColor = HaathSePeacockLight,
                    contentColor = HaathSePeacockTeal,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// =======================================================================
// 11. CRAFT STORY CARD (Heritage preservation & Artisan lineage)
// =======================================================================

@Composable
fun CraftStoryCard(
    craftName: String,
    artisanStory: String,
    historyOrigin: String,
    techniqueSummary: String,
    onAudioStoryClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    HaathSeCard(
        modifier = modifier,
        backgroundColor = HaathSeSand.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HistoryEdu,
                    contentDescription = null,
                    tint = HaathSeTerracotta,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Authentic Craft Story",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = HaathSeTerracottaDark
                    )
                )
            }

            if (onAudioStoryClick != null) {
                IconButton(
                    onClick = onAudioStoryClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Listen to Craft Story",
                        tint = HaathSeTerracotta
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "“$artisanStory”",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = HaathSeCardBorder)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Origin & Lineage", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = historyOrigin, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Technique", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = techniqueSummary, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

// =======================================================================
// 12. PRICE RECOMMENDATION CARD (Smart Pricing Breakdown)
// =======================================================================

@Composable
fun PriceCard(
    rawMaterialCost: Double,
    laborHours: Double,
    hourlyWage: Double,
    suggestedRetailPrice: Double,
    suggestedWholesalePrice: Double,
    minimumSustainablePrice: Double,
    onApplyPrice: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    HaathSeCard(modifier = modifier) {
        Text(
            text = "Smart Fair-Wage Pricing Engine",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Calculated to guarantee living wages & material index pricing.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Cost breakdown grid
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Raw Materials:", style = MaterialTheme.typography.bodySmall)
                    Text("₹${rawMaterialCost.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Labor (${laborHours.toInt()}h @ ₹${hourlyWage.toInt()}/h):", style = MaterialTheme.typography.bodySmall)
                    Text("₹${(laborHours * hourlyWage).toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Sustainable Minimum Floor:", style = MaterialTheme.typography.bodySmall, color = HaathSeTerracotta)
                    Text("₹${minimumSustainablePrice.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = HaathSeTerracotta))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = HaathSeTerracottaLight,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, HaathSeTerracotta),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onApplyPrice(suggestedRetailPrice) }
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Direct Buyer", style = MaterialTheme.typography.labelSmall, color = HaathSeTerracottaDark)
                    Text("₹${suggestedRetailPrice.toInt()}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = HaathSeTerracotta))
                    Text("Recommended", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HaathSeTerracottaDark)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = HaathSeMarigoldLight,
                border = androidx.compose.foundation.BorderStroke(1.dp, HaathSeMarigold),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onApplyPrice(suggestedWholesalePrice) }
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Bulk / B2B", style = MaterialTheme.typography.labelSmall, color = HaathSeMarigoldDark)
                    Text("₹${suggestedWholesalePrice.toInt()}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = HaathSeMarigoldDark))
                    Text("Min 10 units", fontSize = 10.sp, color = HaathSeTextSecondary)
                }
            }
        }
    }
}

// =======================================================================
// 13. ORDER CARD (Status tracking, customer details, fulfillment)
// =======================================================================

@Composable
fun OrderCard(
    orderId: String,
    productName: String,
    quantity: Int,
    totalAmount: Double,
    buyerName: String,
    status: String,
    orderDate: String,
    onViewOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (status.lowercase()) {
        "delivered", "completed" -> HaathSeGreenSuccess
        "shipped", "in transit" -> HaathSePeacock
        "processing", "weaving" -> HaathSeMarigoldDark
        else -> HaathSeTerracotta
    }
    val statusBg = when (status.lowercase()) {
        "delivered", "completed" -> HaathSeGreenLight
        "shipped", "in transit" -> HaathSePeacockLight
        "processing", "weaving" -> HaathSeMarigoldLight
        else -> HaathSeTerracottaLight
    }

    HaathSeCard(
        modifier = modifier,
        onClick = onViewOrder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Order #$orderId",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = status,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$productName (Qty: $quantity)",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = "Buyer: $buyerName • $orderDate",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total: ₹${totalAmount.toInt()}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = HaathSeTerracotta
                )
            )

            Text(
                text = "View Details →",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = HaathSeTerracotta
                )
            )
        }
    }
}

// =======================================================================
// 14. CRAFT CIRCLE & EVENT CARDS
// =======================================================================

@Composable
fun CraftCircleCard(
    circleName: String,
    region: String,
    memberCount: Int,
    leadCraft: String,
    activeBulkOrders: Int,
    onJoinCircle: () -> Unit,
    modifier: Modifier = Modifier
) {
    HaathSeCard(modifier = modifier, onClick = onJoinCircle) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HaathSeTerracottaLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = HaathSeTerracotta,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = circleName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "$leadCraft • $region",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$memberCount Artisans • $activeBulkOrders active shared RFQs",
                    style = MaterialTheme.typography.labelSmall,
                    color = HaathSeGreenSuccess,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun EventCard(
    eventName: String,
    city: String,
    dates: String,
    stallsAvailable: Int,
    subsidyAvailable: Boolean = true,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    HaathSeCard(modifier = modifier, onClick = onApply) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = eventName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "$city • $dates",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (subsidyAvailable) {
                    Text(
                        text = "✓ 80% Govt Travel & Stall Subsidy",
                        style = MaterialTheme.typography.labelSmall,
                        color = HaathSeGreenSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(HaathSeMarigoldLight)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$stallsAvailable stalls left",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = HaathSeMarigoldDark
                )
            }
        }
    }
}

// =======================================================================
// 15. NOTIFICATION CARD
// =======================================================================

@Composable
fun NotificationCard(
    title: String,
    message: String,
    timestamp: String,
    isUnread: Boolean = false,
    icon: ImageVector = Icons.Default.Notifications,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HaathSeCard(
        modifier = modifier,
        onClick = onClick,
        backgroundColor = if (isUnread) HaathSeTerracottaLight.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isUnread) HaathSeTerracotta else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isUnread) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =======================================================================
// 16. STATE VIEWS (Empty, Loading, Error)
// =======================================================================

@Composable
fun EmptyState(
    title: String,
    description: String,
    icon: ImageVector = Icons.Default.Inventory2,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(HaathSeSand),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = HaathSeTerracotta,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(20.dp))
            HaathSePrimaryButton(
                text = actionLabel,
                onClick = onActionClick,
                modifier = Modifier.widthIn(max = 240.dp)
            )
        }
    }
}

@Composable
fun LoadingState(
    message: String = "Crafting your experience...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = HaathSeTerracotta,
            strokeWidth = 3.dp,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = HaathSeTerracottaDark
            )
        )
    }
}

@Composable
fun ErrorState(
    title: String = "Something went wrong",
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = HaathSeTerracotta,
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        HaathSePrimaryButton(
            text = "Try Again",
            onClick = onRetry,
            modifier = Modifier.widthIn(max = 200.dp)
        )
    }
}

// =======================================================================
// 17. SEARCH BAR, FILTER CHIP & QUANTITY SELECTOR
// =======================================================================

@Composable
fun HaathSeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search handloom, pottery, metal crafts...",
    onSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = HaathSeTerracotta
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Search"
                    )
                }
            }
        },
        singleLine = true,
        shape = PillShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HaathSeTerracotta,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .testTag("haathse_search_bar")
    )
}

@Composable
fun HaathSeFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Surface(
        shape = PillShape,
        color = if (selected) HaathSeTerracotta else MaterialTheme.colorScheme.surface,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = modifier
            .height(HaathSeDimens.chipHeight)
            .clip(PillShape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    minQuantity: Int = 1,
    maxQuantity: Int = 999,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (quantity > minQuantity) onQuantityChange(quantity - 1) },
            enabled = quantity > minQuantity,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
        }

        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        IconButton(
            onClick = { if (quantity < maxQuantity) onQuantityChange(quantity + 1) },
            enabled = quantity < maxQuantity,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
        }
    }
}
