package com.example.ui.screens.buyer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ArtisanEntity
import com.example.data.local.ProductEntity
import com.example.data.models.BuyerMarketplaceCategory
import com.example.data.models.HeroCollection
import com.example.data.models.TrustScoreDetails
import com.example.ui.components.AudioPlayButton
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*

@Composable
fun BuyerHomeScreen(
    products: List<ProductEntity>,
    artisans: List<ArtisanEntity>,
    categories: List<BuyerMarketplaceCategory>,
    heroCollections: List<HeroCollection>,
    wishlistIds: Set<String>,
    onProductClick: (ProductEntity) -> Unit,
    onArtisanClick: (ArtisanEntity) -> Unit,
    onCategoryClick: (String) -> Unit,
    onToggleWishlist: (String) -> Unit,
    onAddToCart: (ProductEntity) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenVoiceSearch: () -> Unit,
    onOpenFilterSheet: () -> Unit,
    onOpenTrustScoreDialog: (TrustScoreDetails) -> Unit,
    onOpenBulkRfq: () -> Unit,
    getTrustScore: (String) -> TrustScoreDetails
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmOffWhiteCanvas),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. BRAND HEADER & SEARCH BAR
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(TerracottaPrimary, TerracottaPrimary.copy(alpha = 0.92f), TerracottaPrimary.copy(alpha = 0.0f))
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Discover Handmade India",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = (-0.5).sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Crafted by people. Chosen by you.",
                                fontSize = 13.sp,
                                color = GoldenAmberSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Authenticity GI Mark Icon Pill
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "GI Authentic",
                                    tint = GoldenAmberSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "100% GI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Premium Search Bar with Voice and Filter
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenSearch() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "What are you looking for?",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )

                            // Voice Search Button
                            IconButton(
                                onClick = onOpenVoiceSearch,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Search",
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Filter Button
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(36.dp).clickable { onOpenFilterSheet() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Filter",
                                        tint = DeepCharcoalSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. HERO COLLECTIONS (Horizontally scrollable visual spotlights)
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(heroCollections) { hero ->
                        HeroCollectionCard(
                            hero = hero,
                            onExplore = { onCategoryClick(hero.categoryTarget) }
                        )
                    }
                }
            }
        }

        // 3. CATEGORIES SECTION (9 Indian Craft Categories)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Craft Traditions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepCharcoalSurface
                        )
                        Text(
                            text = "Explore 9 centuries-old regional artisan techniques",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = onOpenSearch) {
                        Text("View All", color = TerracottaPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Horizontally scrollable category pills with images
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories) { category ->
                        CategoryCard(
                            category = category,
                            onClick = { onCategoryClick(category.title) }
                        )
                    }
                }
            }
        }

        // 4. TRENDING NOW SECTION
        item {
            CraftSectionHeader(
                title = "Trending Now",
                subtitle = "Most loved handmade pieces across India this week",
                icon = Icons.Default.Whatshot,
                iconTint = TerracottaPrimary
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(products.take(6)) { product ->
                    BuyerProductCard(
                        product = product,
                        isWishlisted = wishlistIds.contains(product.id),
                        trustScore = getTrustScore(product.artisanName),
                        onClick = { onProductClick(product) },
                        onToggleWishlist = { onToggleWishlist(product.id) },
                        onAddToCart = { onAddToCart(product) },
                        onOpenTrustScore = { onOpenTrustScoreDialog(getTrustScore(product.artisanName)) }
                    )
                }
            }
        }

        // 5. TOP RATED MASTER ARTISANS
        item {
            Spacer(modifier = Modifier.height(16.dp))
            CraftSectionHeader(
                title = "Top Rated Artisans",
                subtitle = "Direct support to certified master craftspeople",
                icon = Icons.Default.Stars,
                iconTint = GoldenAmberSecondary
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(artisans) { artisan ->
                    TopRatedArtisanCard(
                        artisan = artisan,
                        trustScore = getTrustScore(artisan.name),
                        onClick = { onArtisanClick(artisan) }
                    )
                }
            }
        }

        // 6. ARTISAN STORIES & TRADITION (Highlight Card)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, GoldenAmberSecondary.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TerracottaPrimary.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "❤️ MADE WITH TRADITION",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TerracottaPrimary,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            AudioPlayButton(
                                audioText = "Every piece in HaathSe comes from pure generational lineage without intermediaries.",
                                isPlaying = false,
                                onToggle = {}
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "“I learned this craft from my mother. It takes 49 days of pit-loom hand weaving to create a single heirloom.”",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp,
                            color = DeepCharcoalSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(PeacockTealTertiary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("L", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Lakshmi Ammal",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepCharcoalSurface
                                )
                                Text(
                                    text = "3rd Generation Weaver • Kanchipuram, Tamil Nadu",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. NEW ARRIVALS & MADE NEAR YOU
        item {
            Spacer(modifier = Modifier.height(24.dp))
            CraftSectionHeader(
                title = "New Arrivals",
                subtitle = "Fresh from the looms, kilns & workshops",
                icon = Icons.Default.FiberNew,
                iconTint = PeacockTealTertiary
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(products.reversed()) { product ->
                    BuyerProductCard(
                        product = product,
                        isWishlisted = wishlistIds.contains(product.id),
                        trustScore = getTrustScore(product.artisanName),
                        onClick = { onProductClick(product) },
                        onToggleWishlist = { onToggleWishlist(product.id) },
                        onAddToCart = { onAddToCart(product) },
                        onOpenTrustScore = { onOpenTrustScoreDialog(getTrustScore(product.artisanName)) }
                    )
                }
            }
        }

        // 8. HANDPICKED FOR YOU (Grid)
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                CraftSectionHeader(
                    title = "Handpicked For You",
                    subtitle = "Ethical heirloom pieces certified for purity and fair wages",
                    icon = Icons.Default.AutoAwesome,
                    iconTint = GoldenAmberSecondary
                )
            }
        }

        // Grid items for Handpicked
        val chunkedProducts = products.chunked(2)
        items(chunkedProducts) { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                for (product in pair) {
                    Box(modifier = Modifier.weight(1f)) {
                        BuyerProductGridCard(
                            product = product,
                            isWishlisted = wishlistIds.contains(product.id),
                            trustScore = getTrustScore(product.artisanName),
                            onClick = { onProductClick(product) },
                            onToggleWishlist = { onToggleWishlist(product.id) },
                            onAddToCart = { onAddToCart(product) },
                            onOpenTrustScore = { onOpenTrustScoreDialog(getTrustScore(product.artisanName)) }
                        )
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // 9. BULK ORDERS & B2B PROCUREMENT BANNER
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DeepCharcoalSurface),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GoldenAmberSecondary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "CORPORATE & WEDDING SOURCING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldenAmberSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = GoldenAmberSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Need Custom or Bulk Orders?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Source directly from registered artisan guilds with custom branding, milestone tracking, and volume discounts.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onOpenBulkRfq,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldenAmberSecondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Post Bulk Procurement Request",
                            fontWeight = FontWeight.Bold,
                            color = DeepCharcoalSurface,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeroCollectionCard(
    hero: HeroCollection,
    onExplore: () -> Unit
) {
    val context = LocalContext.current
    val imgResId = remember(hero.imageRes) {
        val id = context.resources.getIdentifier(hero.imageRes, "drawable", context.packageName)
        if (id != 0) id else com.example.R.drawable.img_saree_sample
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .width(310.dp)
            .height(190.dp)
            .clickable { onExplore() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = imgResId),
                contentDescription = hero.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient Overlay for High Contrast Legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.45f),
                                Color.Black.copy(alpha = 0.88f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = TerracottaPrimary
                ) {
                    Text(
                        text = hero.tag,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Column {
                    Text(
                        text = hero.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = hero.subtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = hero.actionText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldenAmberSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = GoldenAmberSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: BuyerMarketplaceCategory,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imgResId = remember(category.imageRes) {
        val id = context.resources.getIdentifier(category.imageRes, "drawable", context.packageName)
        if (id != 0) id else com.example.R.drawable.img_saree_sample
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .width(130.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(id = imgResId),
                    contentDescription = category.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DeepCharcoalSurface,
                maxLines = 1
            )
            Text(
                text = category.countLabel,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CraftSectionHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = DeepCharcoalSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BuyerProductCard(
    product: ProductEntity,
    isWishlisted: Boolean,
    trustScore: TrustScoreDetails,
    onClick: () -> Unit,
    onToggleWishlist: () -> Unit,
    onAddToCart: () -> Unit,
    onOpenTrustScore: () -> Unit
) {
    val context = LocalContext.current
    val imgResId = remember(product.imageDrawableRes) {
        val id = context.resources.getIdentifier(product.imageDrawableRes, "drawable", context.packageName)
        if (id != 0) id else com.example.R.drawable.img_saree_sample
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() }
    ) {
        Column {
            // Product Image & Floating Wishlist Heart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Image(
                    painter = painterResource(id = imgResId),
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Category Tag
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 10.dp),
                    color = DeepCharcoalSurface.copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = product.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Wishlist Button
                IconButton(
                    onClick = onToggleWishlist,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isWishlisted) TerracottaPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                // Artisan info with verified & trust score pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = product.artisanName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = PeacockTealTertiary,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // Trust Score Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PeacockTealTertiary.copy(alpha = 0.12f),
                        modifier = Modifier.clickable { onOpenTrustScore() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = PeacockTealTertiary,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${trustScore.overallScore}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PeacockTealTertiary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoalSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Price and Rating Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "₹${product.activePrice.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = TerracottaPrimary
                        )
                        Text(
                            text = "Fair Artisan Wage",
                            fontSize = 9.sp,
                            color = SuccessGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = GoldenAmberSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "4.9",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepCharcoalSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onAddToCart,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add to Cart", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BuyerProductGridCard(
    product: ProductEntity,
    isWishlisted: Boolean,
    trustScore: TrustScoreDetails,
    onClick: () -> Unit,
    onToggleWishlist: () -> Unit,
    onAddToCart: () -> Unit,
    onOpenTrustScore: () -> Unit
) {
    val context = LocalContext.current
    val imgResId = remember(product.imageDrawableRes) {
        val id = context.resources.getIdentifier(product.imageDrawableRes, "drawable", context.packageName)
        if (id != 0) id else com.example.R.drawable.img_saree_sample
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Image(
                    painter = painterResource(id = imgResId),
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = onToggleWishlist,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isWishlisted) TerracottaPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.artisanName,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoalSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${product.activePrice.toInt()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = TerracottaPrimary
                    )
                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(TerracottaPrimary.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopRatedArtisanCard(
    artisan: ArtisanEntity,
    trustScore: TrustScoreDetails,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imgResId = remember(artisan.avatarDrawableRes) {
        val id = context.resources.getIdentifier(artisan.avatarDrawableRes, "drawable", context.packageName)
        if (id != 0) id else com.example.R.drawable.img_artisan_hero
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .border(2.dp, TerracottaPrimary, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = imgResId),
                    contentDescription = artisan.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = artisan.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoalSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified",
                    tint = PeacockTealTertiary,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = artisan.craftSpecialization,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${artisan.experienceYears} Yrs",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoalSurface
                    )
                    Text("Heritage", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PeacockTealTertiary.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = PeacockTealTertiary,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${trustScore.overallScore} Trust",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeacockTealTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onClick,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, TerracottaPrimary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            ) {
                Text("View Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
            }
        }
    }
}
