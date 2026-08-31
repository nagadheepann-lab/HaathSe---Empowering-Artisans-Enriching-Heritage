@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens.notifications

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppNotificationEntity
import com.example.data.models.AppRole
import com.example.data.models.SupportedLanguage
import com.example.data.repository.KarigarRepository
import com.example.data.service.ArtisanPushType
import com.example.data.service.BuyerPushType
import com.example.ui.components.AudioPlayButton
import com.example.ui.theme.*
import com.example.utils.AudioVoiceHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationCenterScreen(
    currentRole: AppRole,
    currentLanguage: SupportedLanguage,
    repository: KarigarRepository,
    audioHelper: AudioVoiceHelper?,
    onNavigateBack: () -> Unit,
    onActionRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeRoleTab by remember {
        mutableStateOf(if (currentRole == AppRole.ARTISAN) "ARTISAN" else "BUYER")
    }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showPushSimulatorSheet by remember { mutableStateOf(false) }

    val notificationsFlow = if (activeRoleTab == "ARTISAN") {
        repository.artisanNotifications
    } else {
        repository.buyerNotifications
    }
    val notifications by notificationsFlow.collectAsState(initial = emptyList())

    val unreadCount = remember(notifications) {
        notifications.count { !it.isRead }
    }

    val filteredNotifications = remember(notifications, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") {
            notifications
        } else {
            notifications.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    val categoryTabs = listOf(
        "ALL" to "All",
        "ORDERS" to "Orders",
        "PAYMENTS" to "Payments",
        "CRAFT_CIRCLES" to "Guild Circles",
        "EVENTS" to "Events",
        "REVIEWS" to "Reviews",
        "INVENTORY" to "Alerts",
        "PROMOTIONS" to "Offers"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Notification Center",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TerracottaPrimary
                            )
                            if (unreadCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = TerracottaPrimary
                                ) {
                                    Text(
                                        text = "$unreadCount",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Firebase Cloud Messaging Alerts & Push Center",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_notif_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    AudioPlayButton(
                        textToSpeak = "Notification Center. Stay updated with order notifications, escrow payments, craft circle invitations, and buyer reviews.",
                        language = currentLanguage,
                        audioHelper = audioHelper
                    )
                    IconButton(
                        onClick = { showPushSimulatorSheet = true },
                        modifier = Modifier.testTag("btn_open_simulator")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SendTimeExtension,
                            contentDescription = "Simulate FCM Push",
                            tint = PeacockTealTertiary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(WarmBgLight)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
        ) {
            // 1. ROLE SWITCHER (ARTISAN vs BUYER)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(4.dp)
                ) {
                    val isArtisan = activeRoleTab == "ARTISAN"
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isArtisan) TerracottaPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeRoleTab = "ARTISAN" }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Handyman,
                                contentDescription = null,
                                tint = if (isArtisan) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Artisan Alerts",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isArtisan) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isArtisan) PeacockTealTertiary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeRoleTab = "BUYER" }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = if (!isArtisan) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Buyer Updates",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isArtisan) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. CATEGORY FILTER CHIPS
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoryTabs.forEach { (key, label) ->
                        val isSelected = selectedCategoryFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = key },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (activeRoleTab == "ARTISAN") TerracottaPrimary else PeacockTealTertiary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                    }
                }
            }

            // 3. ACTION CONTROLS (Mark all as read & Clear)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredNotifications.size} Notifications",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoalSurface
                    )

                    Row {
                        if (unreadCount > 0) {
                            TextButton(
                                onClick = { repository.fcmService.markAllAsReadForRole(activeRoleTab) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Mark all read", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                            }
                        }

                        if (notifications.isNotEmpty()) {
                            TextButton(
                                onClick = { repository.fcmService.clearAllForRole(activeRoleTab) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Clear all", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // 4. NOTIFICATION LIST
            if (filteredNotifications.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "You're all caught up.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DeepCharcoalSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "You're completely caught up! Tap the simulator icon in the top bar to test live FCM alerts.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showPushSimulatorSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.SendTimeExtension, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test FCM Push Alerts", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                items(filteredNotifications, key = { it.id }) { notif ->
                    NotificationItemCard(
                        notification = notif,
                        onMarkRead = { repository.fcmService.markAsRead(notif.id) },
                        onDelete = { repository.fcmService.deleteNotification(notif.id) },
                        onActionClick = {
                            repository.fcmService.markAsRead(notif.id)
                            if (notif.actionRoute.isNotBlank()) {
                                onActionRoute(notif.actionRoute)
                            }
                        }
                    )
                }
            }
        }
    }

    // 5. FCM PUSH SIMULATOR BOTTOM SHEET / DIALOG
    if (showPushSimulatorSheet) {
        FcmPushSimulatorDialog(
            activeRole = activeRoleTab,
            onDismiss = { showPushSimulatorSheet = false },
            onTriggerArtisanPush = { type ->
                repository.fcmService.triggerArtisanPush(type)
            },
            onTriggerBuyerPush = { type ->
                repository.fcmService.triggerBuyerPush(type)
            }
        )
    }
}

// =======================================================================
// NOTIFICATION CARD
// =======================================================================

@Composable
fun NotificationItemCard(
    notification: AppNotificationEntity,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, iconColor, iconBg) = when (notification.category) {
        "ORDERS" -> Triple(Icons.Default.LocalShipping, TerracottaPrimary, TerracottaPrimary.copy(alpha = 0.12f))
        "PAYMENTS" -> Triple(Icons.Default.Payments, SuccessGreen, SuccessGreen.copy(alpha = 0.12f))
        "CRAFT_CIRCLES" -> Triple(Icons.Default.Hub, PeacockTealTertiary, PeacockTealTertiary.copy(alpha = 0.12f))
        "INVENTORY" -> Triple(Icons.Default.Warning, GoldenAmberSecondary, GoldenAmberSecondary.copy(alpha = 0.15f))
        "MARKET_INSIGHTS" -> Triple(Icons.Default.TrendingUp, Color(0xFF673AB7), Color(0xFF673AB7).copy(alpha = 0.12f))
        "EVENTS" -> Triple(Icons.Default.Event, TerracottaPrimary, TerracottaPrimary.copy(alpha = 0.12f))
        "REVIEWS" -> Triple(Icons.Default.Star, GoldenAmberSecondary, GoldenAmberSecondary.copy(alpha = 0.15f))
        "PROMOTIONS" -> Triple(Icons.Default.LocalOffer, Color(0xFFE91E63), Color(0xFFE91E63).copy(alpha = 0.12f))
        else -> Triple(Icons.Default.Notifications, TerracottaPrimary, TerracottaPrimary.copy(alpha = 0.12f))
    }

    val formattedTime = remember(notification.timestamp) {
        val diffMillis = System.currentTimeMillis() - notification.timestamp
        when {
            diffMillis < 60000 -> "Just now"
            diffMillis < 3600000 -> "${diffMillis / 60000} mins ago"
            diffMillis < 86400000 -> "${diffMillis / 3600000} hours ago"
            else -> SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(notification.timestamp))
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (notification.isRead) 1.dp else 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onActionClick() }
            .testTag("notif_card_${notification.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Category Icon Circle
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Body Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (notification.badgeText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = iconColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = notification.badgeText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = iconColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formattedTime,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!notification.isRead) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(TerracottaPrimary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoalSurface
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 17.sp
                )

                // Quick Action Bar
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (notification.actionRoute.isNotBlank()) {
                        val actionLabel = when (notification.actionRoute) {
                            "ORDERS_STOCK", "BUYER_ORDERS" -> "View Order →"
                            "CRAFT_CIRCLES" -> "Guild Circle →"
                            "CRAFT_EVENTS" -> "View Event →"
                            "ARTISAN_PROFILE" -> "View Review →"
                            "BUYER_MARKETPLACE" -> "Explore Collection →"
                            else -> "View Details →"
                        }
                        Text(
                            text = actionLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary,
                            modifier = Modifier.clickable { onActionClick() }
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete notification",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// =======================================================================
// FCM PUSH SIMULATOR DIALOG
// =======================================================================

@Composable
fun FcmPushSimulatorDialog(
    activeRole: String,
    onDismiss: () -> Unit,
    onTriggerArtisanPush: (ArtisanPushType) -> Unit,
    onTriggerBuyerPush: (BuyerPushType) -> Unit
) {
    var selectedRole by remember { mutableStateOf(activeRole) }
    var lastTriggeredMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SendTimeExtension,
                    contentDescription = null,
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FCM Cloud Push Simulator",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                Text(
                    text = "Simulate real-time push notifications dispatched by Firebase Cloud Messaging pipeline:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Role Switcher Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(2.dp)
                ) {
                    val isArtisan = selectedRole == "ARTISAN"
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isArtisan) TerracottaPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedRole = "ARTISAN" }
                    ) {
                        Text(
                            text = "Artisan Scenarios (8)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isArtisan) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (!isArtisan) PeacockTealTertiary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedRole = "BUYER" }
                    ) {
                        Text(
                            text = "Buyer Scenarios (6)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isArtisan) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons Grid / List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (selectedRole == "ARTISAN") {
                        ArtisanPushType.values().forEach { type ->
                            item {
                                OutlinedButton(
                                    onClick = {
                                        onTriggerArtisanPush(type)
                                        lastTriggeredMessage = "Sent: ${type.title}"
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_sim_${type.name}")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = type.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    } else {
                        BuyerPushType.values().forEach { type ->
                            item {
                                OutlinedButton(
                                    onClick = {
                                        onTriggerBuyerPush(type)
                                        lastTriggeredMessage = "Sent: ${type.title}"
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_sim_${type.name}")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = type.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                lastTriggeredMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SuccessGreen.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✓ $msg",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            modifier = Modifier.padding(6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
            ) {
                Text("Close")
            }
        }
    )
}
