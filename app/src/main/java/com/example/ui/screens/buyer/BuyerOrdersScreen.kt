package com.example.ui.screens.buyer

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.local.OrderEntity
import com.example.data.models.ArtisanOrderStatus
import com.example.data.models.OrderState
import com.example.data.repository.KarigarRepository
import com.example.ui.components.CreateReviewDialog
import com.example.ui.theme.*
import com.example.ui.viewmodels.CartViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerOrdersScreen(
    cartViewModel: CartViewModel,
    onNavigateBack: () -> Unit,
    onExploreMarketplace: () -> Unit,
    repository: KarigarRepository? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val orders by cartViewModel.buyerOrders.collectAsState(initial = emptyList())
    var selectedOrderForDetail by remember { mutableStateOf<OrderEntity?>(null) }
    var orderToReview by remember { mutableStateOf<OrderEntity?>(null) }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    val filteredOrders = remember(orders, selectedStatusFilter) {
        when (selectedStatusFilter) {
            "ACTIVE" -> orders.filter { it.orderState == OrderState.PROCESSING.name || it.orderState == OrderState.SHIPPED.name }
            "DELIVERED" -> orders.filter { it.orderState == OrderState.DELIVERED.name || it.orderState == OrderState.COMPLETED.name }
            else -> orders
        }
    }

    if (selectedOrderForDetail != null) {
        BuyerOrderDetailDialog(
            order = selectedOrderForDetail!!,
            onDismiss = { selectedOrderForDetail = null },
            onWriteReview = {
                val ord = selectedOrderForDetail
                selectedOrderForDetail = null
                orderToReview = ord
            }
        )
    }

    if (orderToReview != null) {
        CreateReviewDialog(
            orderId = orderToReview!!.id,
            productId = "prod_${orderToReview!!.id}",
            productTitle = orderToReview!!.itemsSummary,
            artisanId = orderToReview!!.artisanId,
            artisanName = orderToReview!!.artisanName,
            onDismiss = { orderToReview = null },
            onSubmitReview = { overallRating, productQualityRating, packagingRating, deliveryRating, authenticityRating, reviewText, isVoiceReview, voiceTranscript ->
                coroutineScope.launch {
                    repository?.submitReview(
                        productId = "prod_${orderToReview!!.id}",
                        productTitle = orderToReview!!.itemsSummary,
                        artisanId = orderToReview!!.artisanId,
                        artisanName = orderToReview!!.artisanName,
                        orderId = orderToReview!!.id,
                        buyerName = orderToReview?.recipientName?.ifBlank { "Verified Buyer" } ?: "Verified Buyer",
                        overallRating = overallRating,
                        productQualityRating = productQualityRating,
                        packagingRating = packagingRating,
                        deliveryRating = deliveryRating,
                        authenticityRating = authenticityRating,
                        reviewText = reviewText,
                        isVoiceReview = isVoiceReview,
                        voiceTranscript = voiceTranscript
                    )
                    orderToReview = null
                    snackbarHostState.showSnackbar("Verified Review Submitted! Thank you for backing Indian artisans.")
                }
            }
        )
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {

            TopAppBar(
                title = { Text("My Orders (${orders.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(WarmOffWhiteCanvas)
                .padding(innerPadding)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatusFilter == "ALL",
                    onClick = { selectedStatusFilter = "ALL" },
                    label = { Text("All Orders (${orders.size})", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedStatusFilter == "ACTIVE",
                    onClick = { selectedStatusFilter = "ACTIVE" },
                    label = {
                        Text(
                            "In Progress (${orders.count { it.orderState == OrderState.PROCESSING.name || it.orderState == OrderState.SHIPPED.name }})",
                            fontSize = 12.sp
                        )
                    }
                )
                FilterChip(
                    selected = selectedStatusFilter == "DELIVERED",
                    onClick = { selectedStatusFilter = "DELIVERED" },
                    label = {
                        Text(
                            "Delivered (${orders.count { it.orderState == OrderState.DELIVERED.name || it.orderState == OrderState.COMPLETED.name }})",
                            fontSize = 12.sp
                        )
                    }
                )
            }

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(TerracottaPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("No Orders Found", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepCharcoalSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "You have not placed any orders matching this category yet.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onExploreMarketplace,
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Shop Artisan Marketplace", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOrders) { order ->
                        BuyerOrderCard(
                            order = order,
                            onClick = { selectedOrderForDetail = order },
                            onWriteReview = { orderToReview = order }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BuyerOrderCard(
    order: OrderEntity,
    onClick: () -> Unit,
    onWriteReview: () -> Unit = {}
) {

    val dateStr = remember(order.createdAt) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(Date(order.createdAt))
    }

    val (statusColor, statusBg, statusIcon) = when (order.orderState) {
        OrderState.DELIVERED.name, OrderState.COMPLETED.name -> Triple(SuccessGreen, SuccessGreenBg, Icons.Default.CheckCircle)
        OrderState.SHIPPED.name -> Triple(IndigoBlue, IndigoBlueBg, Icons.Default.LocalShipping)
        OrderState.CANCELLED.name -> Triple(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer, Icons.Default.Cancel)
        else -> Triple(GoldenAmberSecondary, GoldenAmberLight, Icons.Default.Handyman)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Order #${order.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepCharcoalSurface)
                    Text(text = dateStr, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (order.artisanStatus) {
                                ArtisanOrderStatus.PREPARING.name -> "Artisan Preparing"
                                ArtisanOrderStatus.READY.name -> "Ready for Dispatch"
                                ArtisanOrderStatus.SHIPPED.name -> "Dispatched"
                                ArtisanOrderStatus.DELIVERED.name -> "Delivered"
                                else -> "Processing"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = WarmBorderBeige, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = order.itemsSummary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeepCharcoalSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Artisan: ${order.artisanName}",
                fontSize = 12.sp,
                color = TerracottaPrimary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Paid", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${order.totalAmount.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = DeepCharcoalSurface)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onWriteReview,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("btn_write_review_${order.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(13.dp), tint = GoldenAmberSecondary)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Review", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Track", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(14.dp), tint = TerracottaPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun BuyerOrderDetailDialog(
    order: OrderEntity,
    onDismiss: () -> Unit,
    onWriteReview: () -> Unit = {}
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
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Order #${order.id}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepCharcoalSurface)
                        Text(order.paymentMethod, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Progress Timeline Tracker
                Text("Order Journey Tracker", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepCharcoalSurface)
                Spacer(modifier = Modifier.height(10.dp))

                OrderTimelineView(order = order)

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = WarmBorderBeige, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Item Details
                Text("Item Details", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepCharcoalSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(order.itemsSummary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("Craft Artisan: ${order.artisanName}", fontSize = 11.sp, color = TerracottaPrimary, fontWeight = FontWeight.Medium)

                Spacer(modifier = Modifier.height(12.dp))

                // Shipping Details
                Text("Courier & Tracking", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepCharcoalSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Courier: ${order.courierName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Tracking No: ${order.trackingNumber}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                Text("Estimated Delivery: ${order.estimatedDeliveryDays}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))

                // Delivery Address
                Text("Shipping Address", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepCharcoalSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${order.recipientName} (${order.addressType})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("${order.addressStreet}, ${order.addressCity}, ${order.addressState} - ${order.addressPin}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Phone: ${order.buyerPhone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = WarmBorderBeige, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Payout breakdown
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Paid (Verified)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("₹${order.totalAmount.toInt()}", fontWeight = FontWeight.Black, fontSize = 15.sp, color = TerracottaPrimary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                FilledTonalButton(
                    onClick = onWriteReview,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = GoldenAmberSecondary.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_dialog_write_review")
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = GoldenAmberSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rate & Review Artisan", fontWeight = FontWeight.Bold, color = DeepCharcoalSurface)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }

            }
        }
    }
}

@Composable
fun OrderTimelineView(order: OrderEntity) {
    val currentStep = when (order.artisanStatus) {
        ArtisanOrderStatus.NEW.name -> 1
        ArtisanOrderStatus.PREPARING.name -> 2
        ArtisanOrderStatus.READY.name -> 3
        ArtisanOrderStatus.SHIPPED.name -> 4
        ArtisanOrderStatus.DELIVERED.name, ArtisanOrderStatus.COMPLETED.name -> 5
        else -> 1
    }

    val steps = listOf(
        Pair("1. Order Placed & Payment Verified", "Transaction confirmed via Razorpay / Demo backend"),
        Pair("2. Artisan Hand-Crafting", "Lakshmi Ammal weaving authentic silk on loom"),
        Pair("3. Quality Checked & GI Sealed", "Inspected and packed in authentic keepsake box"),
        Pair("4. Dispatched via India Post", "Tracking ID: ${order.trackingNumber}"),
        Pair("5. Delivered to Doorstep", "Handed over directly to recipient")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        steps.forEachIndexed { index, (title, subtitle) ->
            val stepNumber = index + 1
            val isCompleted = stepNumber <= currentStep
            val isCurrent = stepNumber == currentStep

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent -> TerracottaPrimary
                                isCompleted -> SuccessGreen
                                else -> WarmBorderBeige
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted && !isCurrent) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    } else {
                        Text(
                            text = "$stepNumber",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = if (isCurrent || isCompleted) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrent) TerracottaPrimary else if (isCompleted) DeepCharcoalSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
