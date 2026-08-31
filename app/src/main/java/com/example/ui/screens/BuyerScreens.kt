@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.example.ai.LocalAIIntelligenceEngine
import com.example.data.local.ArtisanEntity
import com.example.data.local.BuyerRequestEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ProductEntity
import com.example.data.models.SupportedLanguage
import com.example.data.repository.KarigarRepository
import com.example.ui.components.AudioPlayButton
import com.example.ui.components.QRCardDialog
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*
import com.example.utils.AudioVoiceHelper
import androidx.compose.material.icons.outlined.*
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun BuyerMarketplaceScreen(
    currentLanguage: SupportedLanguage,
    repository: KarigarRepository,
    audioHelper: AudioVoiceHelper?,
    onNavigateChat: () -> Unit,
    onNavigatePostRfq: () -> Unit,
    onSwitchToArtisanMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val products by repository.publishedProducts.collectAsState(initial = emptyList())
    val artisans by repository.allArtisans.collectAsState(initial = emptyList())
    val wishlistIds by repository.buyerWishlist.collectAsState()
    val cartItems by repository.buyerCart.collectAsState()
    val filterState by repository.buyerFilter.collectAsState()

    val cartViewModel = repository.cartViewModel
    val cartState by cartViewModel.cartState.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Home, 1: Search, 2: Wishlist, 3: Cart, 4: Profile
    var selectedProductForDetail by remember { mutableStateOf<ProductEntity?>(null) }
    var selectedArtisanForProfile by remember { mutableStateOf<ArtisanEntity?>(null) }
    var initialCategoryFilter by remember { mutableStateOf<String?>(null) }
    var isCheckingOut by remember { mutableStateOf(false) }
    var isViewingOrderHistory by remember { mutableStateOf(false) }

    var showFilterSheet by remember { mutableStateOf(false) }
    var showVoiceSearch by remember { mutableStateOf(false) }
    var activeTrustScoreDialog by remember { mutableStateOf<com.example.data.models.TrustScoreDetails?>(null) }

    val wishlistProducts = remember(products, wishlistIds) {
        products.filter { wishlistIds.contains(it.id) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isCheckingOut -> {
                com.example.ui.screens.buyer.CheckoutScreen(
                    cartViewModel = cartViewModel,
                    onBack = { isCheckingOut = false },
                    onOrderCompleted = { _ ->
                        isCheckingOut = false
                        isViewingOrderHistory = true
                    }
                )
            }
            isViewingOrderHistory -> {
                com.example.ui.screens.buyer.BuyerOrdersScreen(
                    cartViewModel = cartViewModel,
                    repository = repository,
                    onNavigateBack = { isViewingOrderHistory = false },
                    onExploreMarketplace = {
                        isViewingOrderHistory = false
                        activeTab = 0
                    }
                )
            }

            selectedProductForDetail != null -> {
                val prod = selectedProductForDetail!!
                val artisan = artisans.find { it.id == prod.artisanId } ?: artisans.firstOrNull()
                val trustScore = repository.getTrustScoreForArtisan(prod.artisanName)
                val reviews = repository.getProductReviews(prod.id)

                com.example.ui.screens.buyer.BuyerProductDetailScreen(
                    product = prod,
                    artisan = artisan,
                    isWishlisted = wishlistIds.contains(prod.id),
                    trustScore = trustScore,
                    reviews = reviews,
                    onBack = { selectedProductForDetail = null },
                    onToggleWishlist = { repository.toggleWishlist(prod.id) },
                    onAddToCart = { qty -> cartViewModel.addToCart(prod, qty) },
                    onBuyNow = { qty ->
                        cartViewModel.addToCart(prod, qty)
                        selectedProductForDetail = null
                        isCheckingOut = true
                    },
                    onArtisanClick = {
                        selectedArtisanForProfile = artisan
                        selectedProductForDetail = null
                    },
                    onOpenTrustScore = { activeTrustScoreDialog = trustScore },
                    onOpenBulkRfq = onNavigatePostRfq
                )
            }
            selectedArtisanForProfile != null -> {
                val art = selectedArtisanForProfile!!
                val artProducts = products.filter { it.artisanId == art.id || it.artisanName.contains(art.name, ignoreCase = true) }
                val trustScore = repository.getTrustScoreForArtisan(art.name)

                com.example.ui.screens.buyer.BuyerArtisanProfileScreen(
                    artisan = art,
                    artisanProducts = artProducts,
                    wishlistIds = wishlistIds,
                    trustScore = trustScore,
                    onBack = { selectedArtisanForProfile = null },
                    onProductClick = { prod -> selectedProductForDetail = prod },
                    onToggleWishlist = { prodId -> repository.toggleWishlist(prodId) },
                    onAddToCart = { prod -> cartViewModel.addToCart(prod, 1) },
                    onOpenTrustScore = { activeTrustScoreDialog = trustScore },
                    onOpenChat = { onNavigateChat() },
                    onOpenBulkRfq = onNavigatePostRfq
                )
            }
            else -> {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = activeTab == 0,
                                onClick = { activeTab = 0 },
                                icon = {
                                    Icon(
                                        imageVector = if (activeTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                                        contentDescription = "Home"
                                    )
                                },
                                label = { Text("Home", fontSize = 11.sp, fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TerracottaPrimary,
                                    selectedTextColor = TerracottaPrimary,
                                    indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
                                )
                            )

                            NavigationBarItem(
                                selected = activeTab == 1,
                                onClick = { activeTab = 1 },
                                icon = {
                                    Icon(
                                        imageVector = if (activeTab == 1) Icons.Default.Search else Icons.Outlined.Search,
                                        contentDescription = "Search"
                                    )
                                },
                                label = { Text("Categories", fontSize = 11.sp, fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TerracottaPrimary,
                                    selectedTextColor = TerracottaPrimary,
                                    indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
                                )
                            )

                            NavigationBarItem(
                                selected = activeTab == 2,
                                onClick = { activeTab = 2 },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (wishlistIds.isNotEmpty()) {
                                                Badge(containerColor = TerracottaPrimary) {
                                                    Text("${wishlistIds.size}")
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (activeTab == 2) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Wishlist"
                                        )
                                    }
                                },
                                label = { Text("Wishlist", fontSize = 11.sp, fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Normal) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TerracottaPrimary,
                                    selectedTextColor = TerracottaPrimary,
                                    indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
                                )
                            )

                            NavigationBarItem(
                                selected = activeTab == 3,
                                onClick = { activeTab = 3 },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            val count = cartState.items.sumOf { it.quantity }
                                            if (count > 0) {
                                                Badge(containerColor = TerracottaPrimary) {
                                                    Text("$count")
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (activeTab == 3) Icons.Default.ShoppingBag else Icons.Outlined.ShoppingBag,
                                            contentDescription = "Cart"
                                        )
                                    }
                                },
                                label = { Text("Cart", fontSize = 11.sp, fontWeight = if (activeTab == 3) FontWeight.Bold else FontWeight.Normal) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TerracottaPrimary,
                                    selectedTextColor = TerracottaPrimary,
                                    indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
                                )
                            )

                            NavigationBarItem(
                                selected = activeTab == 4,
                                onClick = { activeTab = 4 },
                                icon = {
                                    Icon(
                                        imageVector = if (activeTab == 4) Icons.Default.Person else Icons.Outlined.Person,
                                        contentDescription = "Profile"
                                    )
                                },
                                label = { Text("Profile", fontSize = 11.sp, fontWeight = if (activeTab == 4) FontWeight.Bold else FontWeight.Normal) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TerracottaPrimary,
                                    selectedTextColor = TerracottaPrimary,
                                    indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (activeTab) {
                            0 -> com.example.ui.screens.buyer.BuyerHomeScreen(
                                products = products,
                                artisans = artisans,
                                categories = repository.marketplaceCategories,
                                heroCollections = repository.heroCollections,
                                wishlistIds = wishlistIds,
                                onProductClick = { prod -> selectedProductForDetail = prod },
                                onArtisanClick = { art -> selectedArtisanForProfile = art },
                                onCategoryClick = { cat ->
                                    initialCategoryFilter = cat
                                    activeTab = 1
                                },
                                onToggleWishlist = { prodId -> repository.toggleWishlist(prodId) },
                                onAddToCart = { prod -> cartViewModel.addToCart(prod, 1) },
                                onOpenSearch = { activeTab = 1 },
                                onOpenVoiceSearch = { showVoiceSearch = true },
                                onOpenFilterSheet = { showFilterSheet = true },
                                onOpenTrustScoreDialog = { ts -> activeTrustScoreDialog = ts },
                                onOpenBulkRfq = onNavigatePostRfq,
                                getTrustScore = { name -> repository.getTrustScoreForArtisan(name) }
                            )
                            1 -> com.example.ui.screens.buyer.BuyerSearchScreen(
                                products = products,
                                wishlistIds = wishlistIds,
                                filterState = filterState,
                                initialCategory = initialCategoryFilter,
                                onProductClick = { prod -> selectedProductForDetail = prod },
                                onToggleWishlist = { prodId -> repository.toggleWishlist(prodId) },
                                onAddToCart = { prod -> cartViewModel.addToCart(prod, 1) },
                                onFilterChange = { newFilter -> repository.updateFilter(newFilter) },
                                onOpenFilterSheet = { showFilterSheet = true },
                                onOpenVoiceSearch = { showVoiceSearch = true },
                                onOpenTrustScore = { ts -> activeTrustScoreDialog = ts },
                                getTrustScore = { name -> repository.getTrustScoreForArtisan(name) }
                            )
                            2 -> com.example.ui.screens.buyer.BuyerWishlistScreen(
                                wishlistProducts = wishlistProducts,
                                onProductClick = { prod -> selectedProductForDetail = prod },
                                onRemoveFromWishlist = { prodId -> repository.toggleWishlist(prodId) },
                                onAddToCart = { prod -> cartViewModel.addToCart(prod, 1) },
                                onExploreCrafts = { activeTab = 0 }
                            )
                            3 -> com.example.ui.screens.buyer.BuyerCartScreen(
                                cartState = cartState,
                                onProductClick = { prod -> selectedProductForDetail = prod },
                                onUpdateQuantity = { prodId, qty -> cartViewModel.updateQuantity(prodId, qty) },
                                onRemoveItem = { prodId -> cartViewModel.removeFromCart(prodId) },
                                onSaveForLater = { prodId -> cartViewModel.saveForLater(prodId) },
                                onMoveToCartFromSaved = { prodId -> cartViewModel.moveToCartFromSaved(prodId) },
                                onRemoveSavedItem = { prodId -> cartViewModel.removeSavedItem(prodId) },
                                onApplyCoupon = { code -> cartViewModel.applyCoupon(code) },
                                onRemoveCoupon = { cartViewModel.removeCoupon() },
                                onClearCart = { cartViewModel.clearCart() },
                                onExploreCrafts = { activeTab = 0 },
                                onProceedToCheckout = { isCheckingOut = true }
                            )
                            4 -> com.example.ui.screens.buyer.BuyerProfileTabScreen(
                                repository = repository,
                                onOpenOrderHistory = { isViewingOrderHistory = true },
                                onSwitchToArtisanMode = onSwitchToArtisanMode,
                                onOpenBulkRfq = onNavigatePostRfq
                            )
                        }
                    }
                }
            }
        }

        // Filter Sheet Dialog
        if (showFilterSheet) {
            com.example.ui.screens.buyer.BuyerFilterBottomSheet(
                filterState = filterState,
                onDismiss = { showFilterSheet = false },
                onApply = { newFilter -> repository.updateFilter(newFilter) },
                onReset = { repository.resetFilter() }
            )
        }

        // Voice Search Modal
        if (showVoiceSearch) {
            com.example.ui.screens.buyer.VoiceSearchModal(
                onDismiss = { showVoiceSearch = false },
                onQueryResult = { query ->
                    repository.updateFilter(filterState.copy(searchQuery = query))
                    activeTab = 1
                }
            )
        }

        // Trust Score Dialog
        if (activeTrustScoreDialog != null) {
            com.example.ui.screens.buyer.TrustScoreExplanationDialog(
                trustScore = activeTrustScoreDialog!!,
                onDismiss = { activeTrustScoreDialog = null }
            )
        }
    }
}

@Composable
fun BuyerProductCard(
    product: ProductEntity,
    onViewDetails: () -> Unit,
    onContactArtisan: () -> Unit,
    language: SupportedLanguage,
    audioHelper: AudioVoiceHelper?
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onViewDetails() }
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
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VerifiedBadge(label = "Verified Cluster")
                        AudioPlayButton(
                            textToSpeak = "${product.title}. Handcrafted by ${product.artisanName} in ${product.region}.",
                            language = language,
                            audioHelper = audioHelper,
                            size = 28
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "By ${product.artisanName} • ${product.region}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${product.activePrice.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SuccessGreen
                        )
                        Text(
                            text = " (B2B Fair Price)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stock: ${product.stockQuantity} ready • Lead time ${product.productionDays}d",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onContactArtisan,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Chat & Negotiate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// POST RFQ SCREEN
@Composable
fun BuyerRfqScreen(
    currentLanguage: SupportedLanguage,
    repository: KarigarRepository,
    audioHelper: AudioVoiceHelper?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var reqDesc by remember { mutableStateOf("Looking for 50 pieces of Authentic Pure Silk Kanchipuram Stoles for corporate annual gifting.") }
    var reqCategory by remember { mutableStateOf("Handloom & Silk Textiles") }
    var reqQty by remember { mutableStateOf("50") }
    var reqTargetPrice by remember { mutableStateOf("2200") }
    var reqTimeline by remember { mutableStateOf("25 Days") }
    var showMatchResults by remember { mutableStateOf(false) }

    val matchResult = remember(reqQty, reqTargetPrice) {
        LocalAIIntelligenceEngine.evaluateSmartMatch(
            buyerQuantity = reqQty.toIntOrNull() ?: 50,
            buyerTargetPrice = reqTargetPrice.toDoubleOrNull() ?: 2200.0,
            artisanCapacity = 80,
            artisanMinPrice = 2100.0,
            categoryMatch = true
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post B2B Bulk Order (RFQ)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
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
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Describe Bulk Sourcing Need", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = reqDesc,
                            onValueChange = { reqDesc = it },
                            label = { Text("Product Requirement") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = reqQty, onValueChange = { reqQty = it }, label = { Text("Quantity (units)") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = reqTargetPrice, onValueChange = { reqTargetPrice = it }, label = { Text("Target Price (₹/unit)") }, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(value = reqTimeline, onValueChange = { reqTimeline = it }, label = { Text("Delivery Timeline") }, modifier = Modifier.fillMaxWidth())

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    repository.updateBuyerRequestStatus(
                                        id = "rfq_" + UUID.randomUUID().toString().take(6),
                                        status = "PENDING"
                                    )
                                    showMatchResults = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PeacockTealTertiary),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run AI Smart Matchmaker", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (showMatchResults) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("AI Match Result", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SuccessGreenBg)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("${matchResult.matchPercentage}% Compatibility", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            matchResult.reasons.forEach { r ->
                                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = r, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Top Matched Artisan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_artisan_hero),
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp).clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Lakshmi Ammal (Kanchipuram Silk Cluster)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Capacity: 80 units/mo • Sustainable Baseline: ₹2,100", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// UNIVERSAL CHAT SCREEN (Auto Translation & Fair Deal Assistant)
@Composable
fun UniversalChatScreen(
    currentLanguage: SupportedLanguage,
    repository: KarigarRepository,
    audioHelper: AudioVoiceHelper?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val chatMessages by repository.allChatMessages.collectAsState(initial = emptyList())
    var newMessageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Universal Multilingual Chat", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Auto-translates English ↔ ${currentLanguage.nativeName}", fontSize = 11.sp, color = TerracottaPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
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
            // Negotiation Fair Deal banner
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = GoldenAmberLight,
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
            ) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = GoldenAmberSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fair Deal Assistant: Artisan minimum sustainable price is ₹2,450. Counter-offer ₹2,600 preserves fair artisan wages.",
                        fontSize = 11.sp,
                        color = GoldenAmberSecondary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chatMessages) { msg ->
                    val isBuyer = msg.senderRole == "buyer"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isBuyer) Arrangement.Start else Arrangement.End
                    ) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isBuyer) MaterialTheme.colorScheme.surface else TerracottaLight
                            ),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = msg.senderName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isBuyer) PeacockTealTertiary else TerracottaPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = msg.originalText, fontSize = 12.sp)

                                if (msg.translatedText.isNotBlank() && msg.translatedText != msg.originalText) {
                                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                                    Text(
                                        text = "🌐 Translated: ${msg.translatedText}",
                                        fontSize = 11.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    AudioPlayButton(
                                        textToSpeak = if (msg.translatedText.isNotBlank()) msg.translatedText else msg.originalText,
                                        language = currentLanguage,
                                        audioHelper = audioHelper,
                                        size = 24
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick reply pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.clickable {
                    newMessageText = "Yes, we can handloom 40 units with customized zari motifs in 25 days @ ₹2,600 each."
                }
            ) {
                Text(
                    text = "💡 Quick Reply: 'Yes, we can handloom 40 units in 25 days @ ₹2,600 each.'",
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newMessageText,
                    onValueChange = { newMessageText = it },
                    placeholder = { Text("Type in ${currentLanguage.nativeName} or English...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (newMessageText.isNotBlank()) {
                            coroutineScope.launch {
                                repository.sendChatMessage(
                                    ChatMessageEntity(
                                        id = "msg_" + UUID.randomUUID().toString().take(6),
                                        conversationId = "conv_fabindia",
                                        senderRole = "artisan",
                                        senderName = "Lakshmi Ammal",
                                        originalText = newMessageText,
                                        originalLanguage = currentLanguage.englishName,
                                        translatedText = newMessageText,
                                        targetLanguage = "English"
                                    )
                                )
                                newMessageText = ""
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(TerracottaPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}
