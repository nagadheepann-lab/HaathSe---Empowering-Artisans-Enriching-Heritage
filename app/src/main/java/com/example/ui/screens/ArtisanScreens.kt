@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ai.GeminiClient
import com.example.ai.LocalAIIntelligenceEngine
import com.example.data.local.BuyerRequestEntity
import com.example.data.local.MaterialEntity
import com.example.data.local.ProductEntity
import com.example.data.models.SupportedLanguage
import com.example.data.repository.KarigarRepository
import com.example.ui.components.AudioPlayButton
import com.example.ui.components.ListingScoreBadge
import com.example.ui.components.QRCardDialog
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*
import com.example.utils.AudioVoiceHelper
import com.example.utils.MultilingualManager
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ArtisanHomeScreen(
    currentLanguage: SupportedLanguage,
    isSimpleMode: Boolean,
    repository: KarigarRepository,
    audioHelper: AudioVoiceHelper?,
    onNavigateOneTapStudio: () -> Unit,
    onNavigateCoach: () -> Unit,
    onNavigateOrders: () -> Unit,
    onNavigateMaterials: () -> Unit,
    onNavigateCraftMap: () -> Unit,
    onNavigateSchemes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val currentUser by repository.currentUser.collectAsState()
    val artisanDisplayName = currentUser?.name?.ifBlank { "Artisan Partner" } ?: "Artisan Partner"
    val craftSpecialty = currentUser?.craftSpecialty?.ifBlank { "Handmade Master Crafts" } ?: "Handmade Master Crafts"
    val stateLocation = currentUser?.stateLocation?.ifBlank { "Kanchipuram, Tamil Nadu" } ?: "Kanchipuram, Tamil Nadu"
    val products by repository.allProducts.collectAsState(initial = emptyList())
    val buyerRequests by repository.allBuyerRequests.collectAsState(initial = emptyList())

    var selectedQrProduct by remember { mutableStateOf<ProductEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmBgLight)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Welcome Artisan Header Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_artisan_hero),
                        contentDescription = "Artisan Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(2.dp, TerracottaPrimary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = artisanDisplayName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            VerifiedBadge(label = "Master Artisan")
                        }
                        Text(
                            text = "$stateLocation • Verified Crafts",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${currentLanguage.voiceGreeting}! Welcome to your digital atelier.",
                            fontSize = 12.sp,
                            color = TerracottaPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    AudioPlayButton(
                        textToSpeak = "${currentLanguage.voiceGreeting} $artisanDisplayName. You have ${products.size} active products and ${buyerRequests.size} buyer inquiries today.",
                        language = currentLanguage,
                        audioHelper = audioHelper
                    )
                }
            }
        }

        // Marquee Hero: ONE-TAP SELL / AI STUDIO CTA
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = TerracottaPrimary),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateOneTapStudio() }
                    .testTag("one_tap_sell_hero_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GoldenAmberSecondary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("MARQUEE AI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "One-Tap Product Studio",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Speak in ${currentLanguage.nativeName} + Snap Photo → AI builds Multilingual Catalog & Pricing in 10s",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = GoldenAmberLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Camera", fontSize = 11.sp, color = GoldenAmberLight)
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = GoldenAmberLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Voice Input", fontSize = 11.sp, color = GoldenAmberLight)
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(imageVector = Icons.Default.Translate, contentDescription = null, tint = GoldenAmberLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("11 Languages", fontSize = 11.sp, color = GoldenAmberLight)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Start Studio",
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Business Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ArtisanMetricCard(
                    title = "Live Products",
                    value = "${products.size}",
                    sub = "All Verified",
                    icon = Icons.Default.Inventory2,
                    color = TerracottaPrimary,
                    modifier = Modifier.weight(1f)
                )
                ArtisanMetricCard(
                    title = "Buyer Inquiries",
                    value = "${buyerRequests.size}",
                    sub = "₹1.91L Potential",
                    icon = Icons.Default.Handshake,
                    color = PeacockTealTertiary,
                    modifier = Modifier.weight(1f)
                )
                ArtisanMetricCard(
                    title = "Fair Earnings",
                    value = "₹5.84L",
                    sub = "4.9 ★ Rating",
                    icon = Icons.Default.MonetizationOn,
                    color = GoldenAmberSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // AI Business Coach Daily Tip Banner
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PeacockTealLight.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PeacockTealTertiary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateCoach() }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PeacockTealTertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Coach Insight (Festive Season)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeacockTealTertiary
                        )
                        Text(
                            text = "B2B buyers are seeking Handloom Silk Stoles. Consider weaving 15 units to meet Diwali bulk demand.",
                            fontSize = 11.sp,
                            color = PeacockTealTertiary,
                            lineHeight = 15.sp
                        )
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = PeacockTealTertiary)
                }
            }
        }

        // Quick Navigation Grid (Simple mode supported)
        item {
            Text(
                text = "Quick Tools & Hubs",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionChip(
                    title = "Orders & Stock",
                    icon = Icons.Default.LocalShipping,
                    onClick = onNavigateOrders,
                    modifier = Modifier.weight(1f)
                )
                QuickActionChip(
                    title = "Material Ledger",
                    icon = Icons.Default.AccountBalanceWallet,
                    onClick = onNavigateMaterials,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionChip(
                    title = "India Craft Map",
                    icon = Icons.Default.Map,
                    onClick = onNavigateCraftMap,
                    modifier = Modifier.weight(1f)
                )
                QuickActionChip(
                    title = "Govt Schemes",
                    icon = Icons.Default.AccountBalance,
                    onClick = onNavigateSchemes,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // My Active Product Catalog Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Digital Catalog (${products.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onNavigateOneTapStudio) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Craft", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                }
            }
        }

        // Product items
        items(products) { prod ->
            ArtisanProductCard(
                product = prod,
                onStockChange = { delta ->
                    val newStock = (prod.stockQuantity + delta).coerceAtLeast(0)
                    coroutineScope.launch {
                        repository.updateStock(prod.id, newStock)
                    }
                },
                onShowQr = { selectedQrProduct = prod },
                language = currentLanguage,
                audioHelper = audioHelper
            )
        }
    }

    if (selectedQrProduct != null) {
        val p = selectedQrProduct!!
        QRCardDialog(
            title = p.title,
            artisanName = p.artisanName,
            region = p.region,
            craftTechnique = p.craftTechnique,
            price = p.activePrice,
            story = p.culturalStory,
            onDismiss = { selectedQrProduct = null },
            audioHelper = audioHelper,
            language = currentLanguage
        )
    }
}

@Composable
fun ArtisanMetricCard(
    title: String,
    value: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text(text = sub, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = color, maxLines = 1)
        }
    }
}

@Composable
fun QuickActionChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ArtisanProductCard(
    product: ProductEntity,
    onStockChange: (Int) -> Unit,
    onShowQr: () -> Unit,
    language: SupportedLanguage,
    audioHelper: AudioVoiceHelper?
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                val imageRes = when (product.imageDrawableRes) {
                    "img_pottery_sample" -> R.drawable.img_pottery_sample
                    "img_artisan_hero" -> R.drawable.img_artisan_hero
                    else -> R.drawable.img_saree_sample
                }

                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ListingScoreBadge(score = product.listingScore)
                        AudioPlayButton(
                            textToSpeak = "${product.title}. Listing price ₹${product.activePrice.toInt()}. ${product.stockQuantity} pieces in stock.",
                            language = language,
                            audioHelper = audioHelper,
                            size = 30
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${product.activePrice.toInt()} (Fair Sustainable)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stock Controller & QR Passport Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Stock:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = { onStockChange(-1) }, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = TerracottaPrimary)
                    }
                    Text(
                        text = "${product.stockQuantity}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(onClick = { onStockChange(1) }, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = TerracottaPrimary)
                    }
                }

                OutlinedButton(
                    onClick = onShowQr,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Craft QR Passport", fontSize = 11.sp)
                }
            }
        }
    }
}

// INVENTORY & ORDERS SCREEN
@Composable
fun InventoryOrdersScreen(
    currentLanguage: SupportedLanguage,
    repository: KarigarRepository,
    audioHelper: AudioVoiceHelper?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val products by repository.allProducts.collectAsState(initial = emptyList())
    val buyerRequests by repository.allBuyerRequests.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory & Order Fulfillment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    AudioPlayButton(
                        textToSpeak = "Inventory and orders screen. Monitor your active craft stock, lead times, and dispatch status.",
                        language = currentLanguage,
                        audioHelper = audioHelper
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(WarmBgLight)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("B2B Purchase Orders & RFQs", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            items(buyerRequests) { req ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = req.buyerOrganization, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "${req.buyerName} • ${req.location}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (req.status == "CONFIRMED") SuccessGreenBg else GoldenAmberLight)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = req.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (req.status == "CONFIRMED") SuccessGreen else GoldenAmberSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = req.productRequirement, fontSize = 12.sp, lineHeight = 16.sp)

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Quantity: ${req.quantity} units", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Offer: ₹${req.targetUnitPrice.toInt()} / unit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        repository.updateBuyerRequestStatus(req.id, "CONFIRMED")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Accept Deal", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        repository.updateBuyerRequestStatus(req.id, "COUNTER_OFFER", req.targetUnitPrice * 1.1)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                            ) {
                                Text("Counter Offer (+10%)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Stock Warning & Production Lead Time", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            items(products) { prod ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = prod.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(text = "Production Time: ${prod.productionDays} days per unit", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (prod.stockQuantity < 5) TerracottaLight else SuccessGreenBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${prod.stockQuantity} units left",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (prod.stockQuantity < 5) TerracottaPrimary else SuccessGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

// MATERIAL LEDGER SCREEN
@Composable
fun MaterialLedgerScreen(
    currentLanguage: SupportedLanguage,
    repository: KarigarRepository,
    audioHelper: AudioVoiceHelper?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val materials by repository.allMaterials.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    var newMatName by remember { mutableStateOf("") }
    var newMatQty by remember { mutableStateOf("") }
    var newMatCost by remember { mutableStateOf("") }
    var newMatSupplier by remember { mutableStateOf("") }

    val totalInvested = materials.sumOf { it.unitCost }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Raw Material Ledger", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    AudioPlayButton(
                        textToSpeak = "Material ledger. Track costs of pure silk, dyes, clay and packaging to calculate true minimum sustainable prices.",
                        language = currentLanguage,
                        audioHelper = audioHelper
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = TerracottaPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Material", tint = Color.White)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(WarmBgLight)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TerracottaPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Total Material Investment", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                        Text(text = "₹${totalInvested.toInt()}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Accurate material tracking guarantees that no product is sold below Minimum Sustainable Cost.",
                            color = GoldenAmberLight,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            items(materials) { mat ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = mat.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "${mat.quantity} • ${mat.supplier}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "₹${mat.unitCost.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TerracottaPrimary)
                            IconButton(
                                onClick = {
                                    coroutineScope.launch { repository.deleteMaterial(mat.id) }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add Raw Material", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = newMatName, onValueChange = { newMatName = it }, label = { Text("Material Name (e.g. Mulberry Silk Yarn)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newMatQty, onValueChange = { newMatQty = it }, label = { Text("Quantity (e.g. 5 kg)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newMatCost, onValueChange = { newMatCost = it }, label = { Text("Unit Cost (₹)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newMatSupplier, onValueChange = { newMatSupplier = it }, label = { Text("Supplier / Cluster Co-op") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val cost = newMatCost.toDoubleOrNull() ?: 100.0
                            coroutineScope.launch {
                                repository.insertMaterial(
                                    MaterialEntity(
                                        id = "mat_" + UUID.randomUUID().toString().take(6),
                                        name = if (newMatName.isBlank()) "Handloom Raw Material" else newMatName,
                                        category = "Raw Material",
                                        quantity = if (newMatQty.isBlank()) "1 unit" else newMatQty,
                                        unitCost = cost,
                                        supplier = if (newMatSupplier.isBlank()) "Cluster Guild" else newMatSupplier
                                    )
                                )
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add to Ledger")
                    }
                }
            }
        }
    }
}

// AI BUSINESS COACH SCREEN
@Composable
fun BusinessCoachScreen(
    currentLanguage: SupportedLanguage,
    repository: KarigarRepository,
    audioHelper: AudioVoiceHelper?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var userQuestion by remember { mutableStateOf("") }
    var coachResponse by remember {
        mutableStateOf("வணக்கம் லட்சுமி! நான் உங்கள் AI வணிக ஆலோசகர் (KarigarSetu AI Coach). உங்கள் விலை நிர்ணயம், புகைப்பட தரம் அல்லது வாங்குபவர் பேச்சுவார்த்தை பற்றி கேளுங்கள்.")
    }
    var isLoadingCoach by remember { mutableStateOf(false) }

    val presetQuestions = listOf(
        "How much should I price my silk saree?",
        "Which craft product has high festive demand?",
        "How do I improve my product photos?",
        "Write a reply to a bulk buyer inquiry"
    )

    fun askCoach(q: String) {
        userQuestion = q
        isLoadingCoach = true
        coroutineScope.launch {
            // Try Gemini API first, fall back to domain engine
            val geminiPrompt = "You are KarigarSetu AI Business Coach, an expert supporting Indian traditional artisans. Answer concisely in a supportive tone in language matching: $q."
            val result = GeminiClient.generateWithGemini(geminiPrompt)
            val answer = result.getOrElse {
                LocalAIIntelligenceEngine.answerBusinessCoachQuestion(
                    question = q,
                    artisanName = "Lakshmi Ammal",
                    activeProductsCount = 4,
                    pendingOrdersCount = 2,
                    userLang = currentLanguage
                )
            }
            coachResponse = answer
            isLoadingCoach = false
            audioHelper?.speak(answer, currentLanguage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Artisan Business Coach", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    AudioPlayButton(
                        textToSpeak = coachResponse,
                        language = currentLanguage,
                        audioHelper = audioHelper
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(WarmBgLight)
                .padding(16.dp)
        ) {
            // Coach Response Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(3.dp),
                modifier = Modifier.fillMaxWidth()
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PeacockTealTertiary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "KarigarSetu Coach", fontWeight = FontWeight.Bold, color = PeacockTealTertiary)
                        }
                        AudioPlayButton(
                            textToSpeak = coachResponse,
                            language = currentLanguage,
                            audioHelper = audioHelper
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLoadingCoach) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = TerracottaPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Coach is thinking...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Text(
                            text = coachResponse,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Suggested Questions:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            presetQuestions.forEach { pq ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable { askCoach(pq) }
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = pq, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // User Query Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userQuestion,
                    onValueChange = { userQuestion = it },
                    placeholder = { Text("Ask anything in ${currentLanguage.nativeName}...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (userQuestion.isNotBlank()) {
                            askCoach(userQuestion)
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(TerracottaPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}
