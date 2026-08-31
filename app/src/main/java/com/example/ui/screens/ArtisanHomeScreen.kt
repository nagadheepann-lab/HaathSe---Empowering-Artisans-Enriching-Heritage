@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.StockAlertLevel
import com.example.data.models.SupportedLanguage
import com.example.data.repository.KarigarRepository
import com.example.ui.components.AudioPlayButton
import com.example.ui.components.ContinueProductDraftCard
import com.example.ui.components.OfflineStatusBanner
import com.example.ui.components.ArtisanFriendlyErrorDialog
import com.example.ui.components.StandardEmptyState
import com.example.ui.components.EmptyStateType
import com.example.ui.theme.*
import com.example.utils.AudioVoiceHelper
import com.example.utils.MapIntentHelper
import com.example.utils.MultilingualManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ArtisanHomeScreen(
    currentLanguage: SupportedLanguage,
    isSimpleMode: Boolean,
    repository: KarigarRepository,
    audioHelper: AudioVoiceHelper?,
    onNavigateOneTapStudio: () -> Unit,
    onNavigateSaathi: () -> Unit,
    onNavigateMarketPulse: () -> Unit,
    onNavigateOrders: () -> Unit,
    onNavigateMaterials: () -> Unit,
    onNavigateCircles: () -> Unit,
    onNavigateEvents: () -> Unit,
    onNavigateProducts: () -> Unit,
    onNavigateProfile: () -> Unit,
    onOpenSaathiWithQuery: (String) -> Unit,
    onNavigateNotifications: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val currentUser by repository.currentUser.collectAsState()
    val artisanDisplayName = currentUser?.name?.ifBlank { "Artisan Partner" } ?: "Artisan Partner"
    val products by repository.allProducts.collectAsState(initial = emptyList())
    val buyerRequests by repository.allBuyerRequests.collectAsState(initial = emptyList())
    val unreadNotifCount by repository.unreadArtisanCount.collectAsState(initial = 0)
    val latestDraft by repository.getLatestProductDraft(currentUser?.uid ?: "artisan_default").collectAsState(initial = null)
    val isOnline by repository.isOnlineFlow().collectAsState(initial = true)
    val inventoryAlerts = repository.sampleInventoryAlerts
    val circles = repository.craftCircles
    val events = repository.upcomingCraftEvents

    var showTrustScoreDialog by remember { mutableStateOf(false) }
    var trustBreakdown by remember { mutableStateOf<com.example.data.service.TransparentTrustBreakdown?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf("Something went wrong. Please try again.") }

    // Seed default initial draft if none exists
    LaunchedEffect(Unit) {
        if (latestDraft == null) {
            repository.saveProductDraft(
                com.example.data.local.ProductDraftEntity(
                    id = "draft_${currentUser?.uid ?: "artisan"}_01",
                    artisanId = currentUser?.uid ?: "artisan_default",
                    artisanName = artisanDisplayName,
                    title = "Handloom Craft Creation",
                    stepName = "CRAFT_STORY",
                    completionPercentage = 70,
                    category = "Handloom & Textiles",
                    craftTechnique = "Traditional Interlock Weaving",
                    material = "Pure Natural Fibers",
                    dimensions = "Custom Handmade",
                    productionTime = "5 Days",
                    rawMaterialCost = 2100.0,
                    laborHours = 40.0,
                    productionDays = 5,
                    chosenPrice = 3600.0,
                    stockQuantity = 3,
                    capturedPhotoUri = "img_saree_sample",
                    descriptionEn = "Handcrafted pure traditional creation by $artisanDisplayName.",
                    isOfflineSaved = true
                )
            )
        }
    }

    // Revenue time filter state
    var selectedRevenueFilter by remember { mutableStateOf("30D") }
    val revenueData = remember(selectedRevenueFilter) {
        repository.getRevenueData(selectedRevenueFilter)
    }


    // Dynamic Greeting based on Language
    val greetingPrefix = when (currentLanguage) {
        SupportedLanguage.TAMIL -> "வணக்கம்"
        SupportedLanguage.HINDI -> "नमस्ते"
        SupportedLanguage.TELUGU -> "నమస్కారం"
        SupportedLanguage.KANNADA -> "ನಮಸ್ಕಾರ"
        SupportedLanguage.MALAYALAM -> "നമസ്കാരം"
        SupportedLanguage.BENGALI -> "নমস্কার"
        SupportedLanguage.MARATHI -> "नमस्कार"
        SupportedLanguage.GUJARATI -> "નમસ્તે"
        SupportedLanguage.PUNJABI -> "ਸਤਿ ਸ੍ਰੀ ਅਕਾਲ"
        SupportedLanguage.ODIA -> "ନମସ୍କାର"
        else -> "Vanakkam"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmBgLight)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // ==========================================
        // 0. OFFLINE STATUS BANNER (Batch 10)
        // ==========================================
        if (!isOnline) {
            item {
                OfflineStatusBanner(
                    isOnline = isOnline,
                    onToggleSimulatedOffline = {
                        repository.offlineSyncManager.setSimulatedOffline(false)
                    }
                )
            }
        }

        // ==========================================
        // 0.1 CONTINUE PRODUCT DRAFT CARD (Batch 10)
        // ==========================================
        if (latestDraft != null) {
            item {
                ContinueProductDraftCard(
                    draft = latestDraft!!,
                    onContinueDraft = { draft ->
                        onNavigateOneTapStudio()
                    },
                    onDiscardDraft = { draft ->
                        coroutineScope.launch {
                            repository.deleteProductDraft(draft.id)
                        }
                    }
                )
            }
        }

        // ==========================================
        // 1. ARTISAN HEADER SECTION
        // ==========================================
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_artisan_header")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .border(2.5.dp, TerracottaPrimary, CircleShape)
                            .clickable { onNavigateProfile() }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_artisan_hero),
                            contentDescription = "Artisan Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$greetingPrefix, $artisanDisplayName",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SuccessGreenBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        "Verified Artisan",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                }
                            }

                            // Network Status Chip / Simulator
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isOnline) PeacockTealTertiary.copy(alpha = 0.12f) else DeepCharcoalSurface.copy(alpha = 0.12f),
                                modifier = Modifier.clickable {
                                    repository.offlineSyncManager.setSimulatedOffline(isOnline)
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.CloudOff,
                                        contentDescription = null,
                                        tint = if (isOnline) PeacockTealTertiary else DeepCharcoalSurface,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isOnline) "Online" else "Offline Cache",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOnline) PeacockTealTertiary else DeepCharcoalSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Kanchipuram, Tamil Nadu",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateNotifications,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("btn_artisan_notifications")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = TerracottaPrimary
                                )
                                if (unreadNotifCount > 0) {
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

                        AudioPlayButton(
                            textToSpeak = "$greetingPrefix $artisanDisplayName. Your artisan command center is ready. You have 48 completed orders, 98% trust score, and ₹1,42,800 monthly revenue.",
                            language = currentLanguage,
                            audioHelper = audioHelper
                        )
                    }
                }
            }
        }


        // ==========================================
        // 2. HERO SECTION: "Your craft is growing."
        // ==========================================
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = TerracottaPrimary),
                elevation = CardDefaults.cardElevation(5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_craft_growth_hero")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GoldenAmberSecondary)
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        "COMMAND CENTER",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "30-Day Growth",
                                    color = GoldenAmberLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your craft is growing.",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Key Metric Blocks inside Hero
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HeroMetricBlock(
                            label = "Revenue",
                            value = "₹1,42,800",
                            subText = "+24% vs last mo",
                            modifier = Modifier.weight(1f)
                        )
                        HeroMetricBlock(
                            label = "Orders",
                            value = "48",
                            subText = "100% fulfilled",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HeroMetricBlock(
                            label = "Trust Score ⓘ",
                            value = "98%",
                            subText = "Master Tier (Tap for breakdown)",
                            onClick = {
                                coroutineScope.launch {
                                    trustBreakdown = repository.getTransparentTrustBreakdown("artisan_lakshmi")
                                    showTrustScoreDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )


                        HeroMetricBlock(
                            label = "Products",
                            value = "${products.size} Live",
                            subText = "GI Certified",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. ANIMATED BUSINESS METRICS GRID
        // ==========================================
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Business Metrics",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimatedMetricCard(
                        title = "Total Revenue",
                        targetNumber = 142800,
                        prefix = "₹",
                        suffix = "",
                        icon = Icons.Default.CurrencyRupee,
                        tintColor = TerracottaPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    AnimatedMetricCard(
                        title = "Total Orders",
                        targetNumber = 48,
                        prefix = "",
                        suffix = " ord",
                        icon = Icons.Default.LocalShipping,
                        tintColor = PeacockTealTertiary,
                        modifier = Modifier.weight(1f)
                    )
                    AnimatedMetricCard(
                        title = "Products Listed",
                        targetNumber = products.size.coerceAtLeast(12),
                        prefix = "",
                        suffix = " items",
                        icon = Icons.Default.Inventory2,
                        tintColor = GoldenAmberSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimatedMetricCard(
                        title = "Trust Score",
                        targetNumber = 98,
                        prefix = "",
                        suffix = "%",
                        icon = Icons.Default.VerifiedUser,
                        tintColor = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    AnimatedMetricCard(
                        title = "Average Rating",
                        targetNumber = 49,
                        prefix = "4.",
                        suffix = " ★",
                        icon = Icons.Default.Star,
                        tintColor = DeepOrangeAccent,
                        modifier = Modifier.weight(1f)
                    )
                    AnimatedMetricCard(
                        title = "Pending Orders",
                        targetNumber = buyerRequests.filter { it.status == "PENDING" }.size.coerceAtLeast(2),
                        prefix = "",
                        suffix = " reqs",
                        icon = Icons.Default.PendingActions,
                        tintColor = RoyalBurgundy,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ==========================================
        // 4. REVENUE CARD WITH INTERACTIVE CHART
        // ==========================================
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_artisan_revenue_chart")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Revenue & Sales Curve",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Verified direct buyer settlements",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Time Filters (7D, 30D, 3M, 1Y)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(3.dp)
                        ) {
                            listOf("7D", "30D", "3M", "1Y").forEach { filter ->
                                val isSelected = selectedRevenueFilter == filter
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(if (isSelected) TerracottaPrimary else Color.Transparent)
                                        .clickable { selectedRevenueFilter = filter }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("btn_filter_$filter")
                                ) {
                                    Text(
                                        text = filter,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Compose Compatible Chart
                    RevenueComposeChart(
                        dataPoints = revenueData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            }
        }

        // ==========================================
        // 5. QUICK ACTIONS PROMINENT CARDS
        // ==========================================
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Row 1: Add Product (Hero Action) & My Orders
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionCard(
                        title = "➕ Add Product",
                        subtitle = "One-Tap AI Studio",
                        badge = "AI FAST",
                        containerColor = TerracottaPrimary,
                        contentColor = Color.White,
                        onClick = onNavigateOneTapStudio,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_add_product")
                    )

                    ActionCard(
                        title = "📦 My Orders",
                        subtitle = "${buyerRequests.size} Active POs",
                        badge = "B2B",
                        containerColor = PeacockTealTertiary,
                        contentColor = Color.White,
                        onClick = onNavigateOrders,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_my_orders")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2: My Business & Market Pulse
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionCard(
                        title = "📊 My Business",
                        subtitle = "Material Ledger",
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        onClick = onNavigateMaterials,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_my_business")
                    )

                    ActionCard(
                        title = "📈 Market Pulse",
                        subtitle = "High Demand Alerts",
                        badge = "TRENDS",
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldenAmberSecondary.copy(alpha = 0.5f)),
                        onClick = onNavigateMarketPulse,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_market_pulse")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 3: Craft Circles, Craft Events, Saathi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionCard(
                        title = "🤝 Craft Circles",
                        subtitle = "18 Artisans",
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        onClick = onNavigateCircles,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_craft_circles")
                    )

                    ActionCard(
                        title = "🎪 Craft Events",
                        subtitle = "3 Melas",
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        onClick = onNavigateEvents,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_craft_events")
                    )

                    ActionCard(
                        title = "🤖 Saathi",
                        subtitle = "AI Companion",
                        badge = "PRO",
                        containerColor = GoldenAmberSecondary,
                        contentColor = Color.White,
                        onClick = onNavigateSaathi,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_saathi_companion")
                    )
                }
            }
        }

        // ==========================================
        // 6. TOP PRODUCT SHOWCASE
        // ==========================================
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_top_product")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = GoldenAmberSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Top Product This Month",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SuccessGreenBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "BEST SELLER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_saree_sample),
                            contentDescription = "Top Product Saree",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pure Mulberry Kanchipuram Silk Saree",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "32 sales",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TerracottaPrimary
                                )
                                Text(
                                    text = "•  ₹86,400 revenue",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "•  4.9 ★",
                                    fontSize = 12.sp,
                                    color = GoldenAmberSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 6.5 NEARBY TRADE FAIRS & GOVT CRAFT MELAS (Dedicated Page Gateway)
        // ==========================================
        item {
            val context = LocalContext.current
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldenAmberSecondary.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_dashboard_trade_fairs_hub")
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(GoldenAmberSecondary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Festival,
                                    contentDescription = null,
                                    tint = GoldenAmberSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Nearby Trade Fairs & Melas",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Govt Subsidized Stalls & Exhibitions",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        TextButton(
                            onClick = onNavigateEvents,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("btn_view_all_fairs_page")
                        ) {
                            Text(
                                text = "View All →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Nearest Event Spotlight Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateEvents() }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "National Handloom & Silk Expo 2026",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Chennai Trade Centre, Nandambakkam",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = TerracottaLight
                                ) {
                                    Text(
                                        text = "8.4 km away",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SuccessGreenBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "100% Free Stall with Pehchan ID + ₹750/day DA",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SuccessGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        MapIntentHelper.openDirections(
                                            context = context,
                                            latitude = 13.0135,
                                            longitude = 80.1873,
                                            destinationName = "Chennai Trade Centre"
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TerracottaPrimary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("🗺️ Directions", fontSize = 11.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = onNavigateEvents,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1.3f)
                                ) {
                                    Text("Open Fairs Page 🎪", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 7. INVENTORY ALERTS (Low Stock / High Demand / Out of Stock)
        // ==========================================
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Inventory Alerts",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                    TextButton(onClick = onNavigateOrders) {
                        Text("Manage Stock", fontSize = 12.sp, color = TerracottaPrimary)
                    }
                }

                inventoryAlerts.forEach { alert ->
                    val (badgeText, badgeBg: Color, badgeTextColor: Color) = when (alert.alertLevel) {
                        StockAlertLevel.LOW_STOCK -> Triple("LOW STOCK", TerracottaLight, TerracottaPrimary)
                        StockAlertLevel.HIGH_DEMAND -> Triple("HIGH DEMAND", PeacockTealLight, PeacockTealTertiary)
                        StockAlertLevel.OUT_OF_STOCK -> Triple("OUT OF STOCK", DeepOrangeAccent.copy(alpha = 0.15f), DeepOrangeAccent)
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeTextColor
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = alert.message,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 16.sp
                                )
                            }

                            IconButton(
                                onClick = {
                                    onOpenSaathiWithQuery("How can I quickly produce and restock ${alert.suggestedRestock} pieces of ${alert.productName}?")
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Ask Saathi",
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 8. MARKET PULSE HIGHLIGHT CARD
        // ==========================================
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_home_market_pulse")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📈", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Market Pulse Highlights",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        TextButton(onClick = onNavigateMarketPulse) {
                            Text("Full Pulse", fontSize = 12.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Pulse Item 1: High Demand Blue Sarees
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "🔥 HIGH DEMAND",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary
                                )
                                TextButton(
                                    onClick = onNavigateMarketPulse,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("See why →", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                                }
                            }
                            Text(
                                text = "\"Blue handwoven sarees are receiving more attention this month across metro boutiques.\"",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pulse Item 2: Trending Festive Home Decor
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "📈 TRENDING",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldenAmberSecondary
                                )
                                TextButton(
                                    onClick = onNavigateMarketPulse,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("See why →", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldenAmberSecondary)
                                }
                            }
                            Text(
                                text = "\"Festive home décor demand is rising with corporate gifting agencies placing advance bulk orders.\"",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 9. SAATHI AI COMPANION CARD
        // ==========================================
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GoldenAmberLight.copy(alpha = 0.6f)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldenAmberSecondary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_saathi_companion")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_saathi_mascot),
                        contentDescription = "Saathi Mascot",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(2.dp, GoldenAmberSecondary, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Saathi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Your business companion",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TerracottaPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "\"Need help deciding what to make next?\"",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onNavigateSaathi,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_talk_to_saathi")
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Talk to Saathi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 10. CRAFT CIRCLE GUILD CARD
        // ==========================================
        item {
            val circle = circles.firstOrNull() ?: repository.craftCircles.first()
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_craft_circle_home")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = PeacockTealTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Your Craft Circle",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SuccessGreenBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("ACTIVE GUILD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = circle.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${circle.craftType} • ${circle.location}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Members", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${circle.memberCount} Artisans", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Active Orders", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${circle.activeCollectiveOrders} Collective", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Available Capacity", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(circle.availableMonthlyCapacity, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onNavigateCircles,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Circle Details", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ==========================================
        // 11. UPCOMING CRAFT EVENTS CARD
        // ==========================================
        item {
            val event = events.firstOrNull() ?: repository.upcomingCraftEvents.first()
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_craft_events_home")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Festival,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Upcoming Craft Events",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        TextButton(onClick = onNavigateEvents) {
                            Text("View All", fontSize = 12.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_craft_mela),
                            contentDescription = event.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "📅 ${event.dateRange}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TerracottaPrimary
                            )
                            Text(
                                text = "📍 ${event.location}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🎁 ${event.stallRentSubsidy}",
                                fontSize = 10.sp,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTrustScoreDialog && trustBreakdown != null) {
        com.example.ui.components.TransparentTrustScoreDialog(
            breakdown = trustBreakdown!!,
            onDismiss = { showTrustScoreDialog = false }
        )
    }

    if (showErrorDialog) {
        ArtisanFriendlyErrorDialog(
            userFacingMessage = errorDialogMessage,
            onRetry = {
                showErrorDialog = false
            },
            onGoBack = {
                showErrorDialog = false
            },
            onContactSupport = {
                showErrorDialog = false
                onOpenSaathiWithQuery("Help me with an app issue")
            },
            onDismiss = {
                showErrorDialog = false
            }
        )
    }
}

// ----------------------------------------------------
// SUPPORTING COMPOSABLE COMPONENTS
// ----------------------------------------------------

@Composable
fun HeroMetricBlock(
    label: String,
    value: String,
    subText: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.15f),
        modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subText, fontSize = 10.sp, color = GoldenAmberLight, fontWeight = FontWeight.Medium)
        }
    }
}


@Composable
fun AnimatedMetricCard(
    title: String,
    targetNumber: Int,
    prefix: String,
    suffix: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    var countUp by remember { mutableStateOf(0) }

    LaunchedEffect(targetNumber) {
        val step = (targetNumber / 20).coerceAtLeast(1)
        var current = 0
        while (current < targetNumber) {
            current = (current + step).coerceAtMost(targetNumber)
            countUp = current
            delay(25)
        }
    }

    val displayValue = if (prefix == "₹") {
        if (countUp >= 100000) {
            val inLakhs = countUp / 100000.0
            "₹%.2fL".format(inLakhs)
        } else {
            "₹%,d".format(countUp)
        }
    } else if (prefix == "4.") {
        "4.${countUp % 10}$suffix"
    } else {
        "$prefix$countUp$suffix"
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.5.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = displayValue,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    badge: String? = null,
    containerColor: Color,
    contentColor: Color,
    border: androidx.compose.foundation.BorderStroke? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = border,
        shadowElevation = if (containerColor != MaterialTheme.colorScheme.surface) 2.dp else 0.dp,
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (containerColor == TerracottaPrimary) GoldenAmberSecondary else TerracottaPrimary)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = contentColor.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}

@Composable
fun RevenueComposeChart(
    dataPoints: List<com.example.data.models.RevenueDataPoint>,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val maxAmount = dataPoints.maxOfOrNull { it.amount } ?: 1f

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(dataPoints) {
                    detectTapGestures { offset ->
                        val barWidth = size.width / dataPoints.size
                        val tappedIndex = (offset.x / barWidth).toInt().coerceIn(0, dataPoints.size - 1)
                        selectedIndex = tappedIndex
                    }
                }
        ) {
            val chartWidth = size.width
            val chartHeight = size.height - 20.dp.toPx()
            val pointCount = dataPoints.size
            val slotWidth = chartWidth / pointCount
            val barWidth = slotWidth * 0.55f

            // Draw horizontal light grid lines
            val gridLines = 3
            for (i in 0..gridLines) {
                val y = chartHeight * (i.toFloat() / gridLines)
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            }

            // Draw vertical bars with gradient
            dataPoints.forEachIndexed { index, point ->
                val normalizedHeight = (point.amount / maxAmount) * chartHeight
                val left = index * slotWidth + (slotWidth - barWidth) / 2f
                val top = chartHeight - normalizedHeight

                val isSelected = selectedIndex == index
                val barBrush = Brush.verticalGradient(
                    colors = if (isSelected) {
                        listOf(DeepOrangeAccent, TerracottaPrimary)
                    } else {
                        listOf(TerracottaPrimary.copy(alpha = 0.85f), TerracottaPrimary.copy(alpha = 0.45f))
                    }
                )

                // Draw Bar
                drawRoundRect(
                    brush = barBrush,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, normalizedHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                // Top highlight circle
                drawCircle(
                    color = if (isSelected) GoldenAmberSecondary else TerracottaPrimary,
                    radius = if (isSelected) 4.5.dp.toPx() else 3.dp.toPx(),
                    center = Offset(left + barWidth / 2f, top)
                )
            }
        }

        // X-Axis Labels Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dataPoints.forEachIndexed { index, point ->
                val isSelected = selectedIndex == index
                Text(
                    text = point.label,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isSelected) TerracottaPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Selected point tooltip if any
        if (selectedIndex != null && selectedIndex!! < dataPoints.size) {
            val pt = dataPoints[selectedIndex!!]
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "${pt.label}: ₹${pt.amount.toInt()} (${pt.orders} orders)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerracottaPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}
