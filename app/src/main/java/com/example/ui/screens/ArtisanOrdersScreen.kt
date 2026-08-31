package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.ArtisanNotificationEntity
import com.example.data.local.OrderEntity
import com.example.data.models.ArtisanOrderStatus
import com.example.data.models.SupportedLanguage
import com.example.ui.components.AudioPlayButton
import com.example.ui.theme.*
import com.example.ui.viewmodels.CartViewModel
import com.example.utils.AudioVoiceHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtisanOrdersScreen(
    currentLanguage: SupportedLanguage,
    cartViewModel: CartViewModel,
    audioHelper: AudioVoiceHelper?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orders by cartViewModel.allOrders.collectAsState(initial = emptyList())
    val notifications by cartViewModel.getArtisanNotifications("artisan_lakshmi").collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(ArtisanOrderStatus.NEW) }
    var showAllTab by remember { mutableStateOf(false) }
    var selectedOrderForDetail by remember { mutableStateOf<OrderEntity?>(null) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    val filteredOrders = remember(orders, selectedTab, showAllTab) {
        if (showAllTab) orders
        else orders.filter { it.artisanStatus == selectedTab.name }
    }

    if (selectedOrderForDetail != null) {
        ArtisanOrderDetailDialog(
            order = selectedOrderForDetail!!,
            currentLanguage = currentLanguage,
            onDismiss = { selectedOrderForDetail = null },
            onUpdateStatus = { newStatus ->
                cartViewModel.updateArtisanOrderStatus(selectedOrderForDetail!!.id, newStatus)
                selectedOrderForDetail = null
            }
        )
    }

    if (showNotificationsDialog) {
        ArtisanNotificationsDialog(
            notifications = notifications,
            onDismiss = { showNotificationsDialog = false },
            onMarkRead = { notifId -> cartViewModel.markNotificationRead(notifId) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Order Fulfillment & Dispatch", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Real-time verified purchase orders", fontSize = 11.sp, color = TerracottaPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Notifications badge icon
                    BadgedBox(
                        badge = {
                            val unreadCount = notifications.count { !it.isRead }
                            if (unreadCount > 0) {
                                Badge(containerColor = TerracottaPrimary) {
                                    Text("$unreadCount", color = Color.White)
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { showNotificationsDialog = true }) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }

                    AudioPlayButton(
                        textToSpeak = "Order management screen. View new buyer orders, mark items as preparing or ready for dispatch, and track payments.",
                        language = currentLanguage,
                        audioHelper = audioHelper
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(WarmBgLight)
                .padding(innerPadding)
        ) {
            // Status Tabs
            ScrollableTabRow(
                selectedTabIndex = if (showAllTab) 0 else ArtisanOrderStatus.values().indexOf(selectedTab) + 1,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = TerracottaPrimary
            ) {
                Tab(
                    selected = showAllTab,
                    onClick = { showAllTab = true },
                    text = { Text("All (${orders.size})", fontWeight = if (showAllTab) FontWeight.Bold else FontWeight.Normal) }
                )

                ArtisanOrderStatus.values().forEach { status ->
                    val count = orders.count { it.artisanStatus == status.name }
                    val isSelected = !showAllTab && selectedTab == status

                    Tab(
                        selected = isSelected,
                        onClick = {
                            showAllTab = false
                            selectedTab = status
                        },
                        text = {
                            Text(
                                text = "${status.tabLabel} ($count)",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (filteredOrders.isEmpty()) {
                com.example.ui.components.StandardEmptyState(
                    type = com.example.ui.components.EmptyStateType.ORDERS
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredOrders) { order ->
                        ArtisanOrderCard(
                            order = order,
                            onOpenDetail = { selectedOrderForDetail = order },
                            onQuickAdvanceStatus = { nextStatus ->
                                cartViewModel.updateArtisanOrderStatus(order.id, nextStatus)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArtisanOrderCard(
    order: OrderEntity,
    onOpenDetail: () -> Unit,
    onQuickAdvanceStatus: (ArtisanOrderStatus) -> Unit
) {
    val currentStatus = remember(order.artisanStatus) {
        try {
            ArtisanOrderStatus.valueOf(order.artisanStatus)
        } catch (e: Exception) {
            ArtisanOrderStatus.NEW
        }
    }

    val (statusColor, statusBg) = when (currentStatus) {
        ArtisanOrderStatus.NEW -> Pair(TerracottaPrimary, TerracottaLight)
        ArtisanOrderStatus.PREPARING -> Pair(GoldenAmberSecondary, GoldenAmberLight)
        ArtisanOrderStatus.READY -> Pair(IndigoBlue, IndigoBlueBg)
        ArtisanOrderStatus.SHIPPED -> Pair(IndigoBlue, IndigoBlueBg)
        ArtisanOrderStatus.DELIVERED, ArtisanOrderStatus.COMPLETED -> Pair(SuccessGreen, SuccessGreenBg)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetail() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Order #${order.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepCharcoalSurface)
                    Text(text = "Buyer: ${order.buyerName} • ${order.addressCity}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = currentStatus.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = order.itemsSummary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = WarmBorderBeige, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Payout Amount (100%)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹${order.totalAmount.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = TerracottaPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SuccessGreenBg)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("✓ PAID", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }
                }

                // Next Action Button
                when (currentStatus) {
                    ArtisanOrderStatus.NEW -> {
                        Button(
                            onClick = { onQuickAdvanceStatus(ArtisanOrderStatus.PREPARING) },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Accept & Prepare", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    ArtisanOrderStatus.PREPARING -> {
                        Button(
                            onClick = { onQuickAdvanceStatus(ArtisanOrderStatus.READY) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldenAmberSecondary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Mark Ready", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    ArtisanOrderStatus.READY -> {
                        Button(
                            onClick = { onQuickAdvanceStatus(ArtisanOrderStatus.SHIPPED) },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Mark Shipped", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    ArtisanOrderStatus.SHIPPED -> {
                        Button(
                            onClick = { onQuickAdvanceStatus(ArtisanOrderStatus.DELIVERED) },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Mark Delivered", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    ArtisanOrderStatus.DELIVERED, ArtisanOrderStatus.COMPLETED -> {
                        OutlinedButton(
                            onClick = onOpenDetail,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("View Details", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArtisanOrderDetailDialog(
    order: OrderEntity,
    currentLanguage: SupportedLanguage,
    onDismiss: () -> Unit,
    onUpdateStatus: (ArtisanOrderStatus) -> Unit
) {
    val currentStatus = remember(order.artisanStatus) {
        try {
            ArtisanOrderStatus.valueOf(order.artisanStatus)
        } catch (e: Exception) {
            ArtisanOrderStatus.NEW
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Order #${order.id}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Status: ${currentStatus.label}", fontSize = 12.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = WarmBorderBeige, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Items Ordered", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(order.itemsSummary, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))
                Text("Buyer & Dispatch Info", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Buyer: ${order.buyerName}", fontSize = 12.sp)
                Text("Phone: ${order.buyerPhone}", fontSize = 12.sp)
                Text("Ship to: ${order.addressStreet}, ${order.addressCity}, ${order.addressState} - ${order.addressPin}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))
                Text("Courier Partner", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${order.courierName} (Tracking: ${order.trackingNumber})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = WarmBorderBeige, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Verified Payout", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("₹${order.totalAmount.toInt()}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = TerracottaPrimary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Advance Order Status", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                ArtisanOrderStatus.values().forEach { status ->
                    OutlinedButton(
                        onClick = { onUpdateStatus(status) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (currentStatus == status) TerracottaPrimary.copy(alpha = 0.1f) else Color.Transparent
                        )
                    ) {
                        Text(
                            text = if (currentStatus == status) "✓ ${status.label} (Current)" else "Change to: ${status.label}",
                            fontSize = 11.sp,
                            fontWeight = if (currentStatus == status) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentStatus == status) TerracottaPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArtisanNotificationsDialog(
    notifications: List<ArtisanNotificationEntity>,
    onDismiss: () -> Unit,
    onMarkRead: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Artisan Order Alerts", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepCharcoalSurface)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (notifications.isEmpty()) {
                    Text("No order alerts yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    notifications.forEach { notif ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (notif.isRead) WarmBgLight else GoldenAmberLight.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onMarkRead(notif.id) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TerracottaPrimary)
                                    if (!notif.isRead) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(TerracottaPrimary)
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("NEW", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = notif.message, fontSize = 11.sp, lineHeight = 15.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}
