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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.ProductDraftEntity
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Meaningful AI Loading Progress Overlay
 * Replaces generic "Loading..." with human-centered AI milestone steps:
 * 1. "Understanding your product..."
 * 2. "Creating your description..."
 * 3. "Finding the story behind your craft..."
 * 4. "Checking market trends..."
 * 5. "Preparing your listing..."
 */
@Composable
fun MeaningfulAiLoadingDialog(
    currentStepIndex: Int = 0,
    customMessage: String? = null,
    onDismiss: (() -> Unit)? = null
) {
    val aiSteps = remember {
        listOf(
            "Understanding your product...",
            "Creating your description...",
            "Finding the story behind your craft...",
            "Checking market trends...",
            "Preparing your listing..."
        )
    }

    var animatedStep by remember { mutableStateOf(currentStepIndex) }

    LaunchedEffect(Unit) {
        if (customMessage == null) {
            while (true) {
                delay(1200)
                animatedStep = (animatedStep + 1) % aiSteps.size
            }
        }
    }

    val displayStepText = customMessage ?: aiSteps[animatedStep.coerceIn(0, aiSteps.size - 1)]

    Dialog(
        onDismissRequest = { onDismiss?.invoke() },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
                .testTag("dialog_ai_meaningful_loading")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Pulsing Mandala / Craft icon
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.92f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size((72 * scale).dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    TerracottaPrimary.copy(alpha = 0.25f),
                                    GoldenAmberSecondary.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Processing",
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Craft AI Studio",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = TerracottaPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedContent(
                    targetState = displayStepText,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(200))
                    },
                    label = "stepText"
                ) { text ->
                    Text(
                        text = text,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = DeepCharcoalSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = TerracottaPrimary,
                    trackColor = TerracottaPrimary.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Crafting your heritage story in regional language...",
                    fontSize = 11.sp,
                    color = DeepCharcoalSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * User-Friendly Error Handling Dialog
 * Never displays raw FirebaseException or technical stack traces.
 * Presents clear options: Retry, Go Back, Contact Support.
 */
@Composable
fun ArtisanFriendlyErrorDialog(
    userFacingMessage: String = "Something went wrong. Please try again.",
    onRetry: () -> Unit,
    onGoBack: () -> Unit,
    onContactSupport: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("dialog_friendly_error")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(TerracottaPrimary.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Alert",
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Notice",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DeepCharcoalSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = userFacingMessage,
                    fontSize = 14.sp,
                    color = DeepCharcoalSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action 1: Retry
                Button(
                    onClick = {
                        onDismiss()
                        onRetry()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_error_retry")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Retry", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action 2: Go Back
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onGoBack()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_error_goback")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Go Back", fontWeight = FontWeight.SemiBold, color = DeepCharcoalSurface)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action 3: Contact Support
                TextButton(
                    onClick = {
                        onDismiss()
                        onContactSupport()
                    },
                    modifier = Modifier.testTag("btn_error_contact_support")
                ) {
                    Icon(imageVector = Icons.Default.HeadsetMic, contentDescription = null, tint = PeacockTealTertiary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Contact Support (Karigar Saathi)", fontSize = 12.sp, color = PeacockTealTertiary, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

/**
 * Standardized Empty States with requested exact copy:
 *  - Products: "Your first craft is waiting."
 *  - Orders: "Your next customer could be just around the corner."
 *  - Notifications: "You're all caught up."
 *  - Craft Circles: "Find artisans near you and grow together."
 */
enum class EmptyStateType(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val actionText: String?
) {
    PRODUCTS(
        title = "Your first craft is waiting.",
        subtitle = "Tap One-Tap Studio to photograph your handcraft, speak in your language, and list it in 60 seconds.",
        icon = Icons.Default.AddPhotoAlternate,
        actionText = "Create Product"
    ),
    ORDERS(
        title = "Your next customer could be just around the corner.",
        subtitle = "New orders from conscious buyers and export guild inquiries will appear here.",
        icon = Icons.Default.LocalMall,
        actionText = "Share Shop"
    ),
    NOTIFICATIONS(
        title = "You're all caught up.",
        subtitle = "No unread alerts for your loom orders, payments, or circle invitations.",
        icon = Icons.Default.NotificationsNone,
        actionText = null
    ),
    CRAFT_CIRCLES(
        title = "Find artisans near you and grow together.",
        subtitle = "Join local cluster guilds to pool raw material purchases and fulfill large bulk export orders.",
        icon = Icons.Default.Groups,
        actionText = "Explore Circles"
    )
}

@Composable
fun StandardEmptyState(
    type: EmptyStateType,
    modifier: Modifier = Modifier,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(TerracottaPrimary.copy(alpha = 0.08f))
        ) {
            Icon(
                imageVector = type.icon,
                contentDescription = null,
                tint = TerracottaPrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = type.title,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = DeepCharcoalSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = type.subtitle,
            fontSize = 13.sp,
            color = DeepCharcoalSurface.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (type.actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(text = type.actionText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

/**
 * Continue Product Draft Card (Batch 10 Requirement)
 * Automatically saved progress during AI Studio.
 * e.g. "Your saree listing is 70% complete."
 * Button: "Continue"
 */
@Composable
fun ContinueProductDraftCard(
    draft: ProductDraftEntity,
    onContinueDraft: (ProductDraftEntity) -> Unit,
    onDiscardDraft: (ProductDraftEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)), // Warm amber container
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldenAmberSecondary.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_continue_product_draft")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(GoldenAmberSecondary.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = GoldenAmberSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue Your Product",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DeepCharcoalSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TerracottaPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${draft.completionPercentage}% Done",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerracottaPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your ${draft.title.ifBlank { "craft" }} listing is ${draft.completionPercentage}% complete.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = DeepCharcoalSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { draft.completionPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = GoldenAmberSecondary,
                trackColor = GoldenAmberSecondary.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onDiscardDraft(draft) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("Discard", fontSize = 12.sp, color = DeepCharcoalSurface.copy(alpha = 0.6f))
                }

                Button(
                    onClick = { onContinueDraft(draft) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_continue_draft")
                ) {
                    Text("Continue", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

/**
 * Offline Status Banner with friendly copy:
 * "Saved. We'll finish this when you're back online."
 */
@Composable
fun OfflineStatusBanner(
    isOnline: Boolean,
    pendingSyncCount: Int = 0,
    onToggleSimulatedOffline: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOnline) {
        Surface(
            color = DeepCharcoalSurface,
            modifier = modifier
                .fillMaxWidth()
                .testTag("banner_offline_status")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Offline",
                        tint = GoldenAmberSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Saved. We'll finish this when you're back online.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                TextButton(
                    onClick = onToggleSimulatedOffline,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Go Online", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldenAmberSecondary)
                }
            }
        }
    }
}
