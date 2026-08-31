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
import androidx.compose.ui.graphics.Brush
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
import com.example.R
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.ui.viewmodels.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onOrderCompleted: (OrderEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cartState by cartViewModel.cartState.collectAsState()
    val deliveryAddress by cartViewModel.deliveryAddress.collectAsState()
    val selectedPaymentMethod by cartViewModel.selectedPaymentMethod.collectAsState()
    val isPaymentProcessing by cartViewModel.isPaymentProcessing.collectAsState()
    val paymentProcessingStage by cartViewModel.paymentProcessingStage.collectAsState()
    val lastConfirmedOrder by cartViewModel.lastConfirmedOrder.collectAsState()

    var showAddressEditDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // If an order has just been confirmed, render the Order Confirmation Screen
    if (lastConfirmedOrder != null) {
        OrderConfirmationScreen(
            order = lastConfirmedOrder!!,
            onTrackOrder = { order ->
                onOrderCompleted(order)
            },
            onContinueShopping = {
                cartViewModel.resetLastOrder()
                onBack()
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Secure Checkout", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("100% Verified Artisan Direct Purchase", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Medium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 16.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Payable", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "₹${cartState.total.toInt()}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = TerracottaPrimary
                            )
                        }

                        Button(
                            onClick = {
                                errorMessage = null
                                val activity = context as? android.app.Activity
                                cartViewModel.processCheckoutAndPayment(
                                    activity = activity,
                                    onSuccess = { order ->
                                        // Handled reactively by lastConfirmedOrder
                                    },
                                    onError = { err ->
                                        errorMessage = err
                                    }
                                )
                            },
                            enabled = !isPaymentProcessing && cartState.items.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .width(200.dp)
                                .testTag("proceed_to_payment_button")
                        ) {
                            if (isPaymentProcessing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pay ₹${cartState.total.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(WarmOffWhiteCanvas)
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Alert Banner if any
            if (errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                        }
                    }
                }
            }

            // SECTION 1: DELIVERY ADDRESS
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delivery Address", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepCharcoalSurface)
                            }
                            TextButton(
                                onClick = { showAddressEditDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Change", color = TerracottaPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = deliveryAddress.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(WarmBgLight)
                                    .border(1.dp, WarmBorderBeige, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(deliveryAddress.addressType, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${deliveryAddress.streetAddress}, ${deliveryAddress.city}, ${deliveryAddress.state} - ${deliveryAddress.pinCode}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Phone: ${deliveryAddress.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // SECTION 2: PRODUCTS IN ORDER
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Products in Order (${cartState.items.sumOf { it.quantity }})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepCharcoalSurface)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        cartState.items.forEachIndexed { idx, item ->
                            val imgResId = remember(item.product.imageDrawableRes) {
                                val id = context.resources.getIdentifier(item.product.imageDrawableRes, "drawable", context.packageName)
                                if (id != 0) id else R.drawable.img_saree_sample
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                ) {
                                    Image(
                                        painter = painterResource(id = imgResId),
                                        contentDescription = item.product.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.product.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(text = "Crafted by ${item.product.artisanName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "Qty: ${item.quantity} × ₹${item.product.activePrice.toInt()}", fontSize = 12.sp, color = TerracottaPrimary, fontWeight = FontWeight.SemiBold)
                                }

                                Text(
                                    text = "₹${(item.product.activePrice * item.quantity).toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = DeepCharcoalSurface
                                )
                            }

                            if (idx < cartState.items.size - 1) {
                                HorizontalDivider(color = WarmBorderBeige, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }

            // SECTION 3: PRICE BREAKDOWN & ARTISAN IMPACT
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Price & Fair-Trade Impact", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepCharcoalSurface)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Item Subtotal", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${cartState.subtotal.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Carbon-Neutral Speed Delivery", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.Default.Eco, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                            }
                            if (cartState.deliveryFee == 0.0) {
                                Text("FREE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            } else {
                                Text("₹${cartState.deliveryFee.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        if (cartState.discountAmount > 0.0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Artisan Coupon (${cartState.couponCode})", fontSize = 13.sp, color = SuccessGreen)
                                Text("- ₹${cartState.discountAmount.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                        }

                        HorizontalDivider(color = WarmBorderBeige, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Payable", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("₹${cartState.total.toInt()}", fontSize = 17.sp, fontWeight = FontWeight.Black, color = TerracottaPrimary)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Artisan Direct Remuneration Badge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SuccessGreenBg)
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("100% Direct Artisan Remuneration", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                    Text("Zero middlemen margin. Full earnings sent directly to the artisan's verified bank account.", fontSize = 11.sp, color = DeepCharcoalSurface)
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 4: PAYMENT METHOD (RAZORPAY + SECURE BACKEND)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Payment, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Payment Method", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepCharcoalSurface)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GoldenAmberLight)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Razorpay Secured", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldenAmberSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        PaymentMethod.values().forEach { method ->
                            val isSelected = selectedPaymentMethod == method

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) TerracottaPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) TerracottaPrimary else WarmBorderBeige
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { cartViewModel.selectPaymentMethod(method) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { cartViewModel.selectPaymentMethod(method) },
                                        colors = RadioButtonDefaults.colors(selectedColor = TerracottaPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = method.displayName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (method == PaymentMethod.DEMO_PAYMENT) GoldenAmberSecondary else DeepCharcoalSurface
                                            )
                                            if (method == PaymentMethod.DEMO_PAYMENT) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(GoldenAmberSecondary)
                                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                                ) {
                                                    Text("INSTANT TEST", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }
                                        Text(text = method.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // In-Flight Payment Processing Modal
    if (isPaymentProcessing) {
        Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = TerracottaPrimary,
                        modifier = Modifier.size(52.dp),
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Securing Your Order",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = DeepCharcoalSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = paymentProcessingStage,
                        fontSize = 12.sp,
                        color = TerracottaPrimary,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Please do not press back or close the application.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Edit Address Dialog
    if (showAddressEditDialog) {
        AddressEditDialog(
            currentAddress = deliveryAddress,
            onDismiss = { showAddressEditDialog = false },
            onSave = { updatedAddress ->
                cartViewModel.updateDeliveryAddress(updatedAddress)
                showAddressEditDialog = false
            }
        )
    }
}

@Composable
fun OrderConfirmationScreen(
    order: OrderEntity,
    onTrackOrder: (OrderEntity) -> Unit,
    onContinueShopping: () -> Unit
) {
    Scaffold(
        bottomBar = {
            Surface(
                shadowElevation = 12.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onContinueShopping,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Continue Shopping", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onTrackOrder(order) },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Track Order", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmOffWhiteCanvas)
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = SuccessGreen,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Order Confirmed!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = DeepCharcoalSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Order ID: ${order.id}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerracottaPrimary
                )
                Text(
                    text = "A direct notification has been dispatched to ${order.artisanName}.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Order Details Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Payment Status", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SuccessGreenBg)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (order.isDemoPayment) "✓ PAID (DEMO PAYMENT)" else "✓ PAID (RAZORPAY)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = WarmBorderBeige, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Amount Paid", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${order.totalAmount.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = TerracottaPrimary)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Payment Method", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(order.paymentMethod, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Estimated Delivery", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(order.estimatedDeliveryDays, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepCharcoalSurface)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Courier Partner", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(order.courierName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tracking Number", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(order.trackingNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                        }
                    }
                }
            }

            // Delivery Address Summary
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Shipping To", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "${order.recipientName} (${order.addressType})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "${order.addressStreet}, ${order.addressCity}, ${order.addressState} - ${order.addressPin}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Phone: ${order.buyerPhone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun AddressEditDialog(
    currentAddress: DeliveryAddress,
    onDismiss: () -> Unit,
    onSave: (DeliveryAddress) -> Unit
) {
    var fullName by remember { mutableStateOf(currentAddress.fullName) }
    var phone by remember { mutableStateOf(currentAddress.phone) }
    var streetAddress by remember { mutableStateOf(currentAddress.streetAddress) }
    var city by remember { mutableStateOf(currentAddress.city) }
    var state by remember { mutableStateOf(currentAddress.state) }
    var pinCode by remember { mutableStateOf(currentAddress.pinCode) }
    var addressType by remember { mutableStateOf(currentAddress.addressType) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Edit Delivery Address", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DeepCharcoalSurface)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = streetAddress,
                    onValueChange = { streetAddress = it },
                    label = { Text("Flat / House / Street Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = pinCode,
                        onValueChange = { pinCode = it },
                        label = { Text("PIN Code") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("State") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Address Tag", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Home", "Work", "Studio").forEach { tag ->
                        FilterChip(
                            selected = addressType == tag,
                            onClick = { addressType = tag },
                            label = { Text(tag) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                DeliveryAddress(
                                    fullName = fullName,
                                    phone = phone,
                                    streetAddress = streetAddress,
                                    city = city,
                                    state = state,
                                    pinCode = pinCode,
                                    addressType = addressType
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                    ) {
                        Text("Save Address", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
