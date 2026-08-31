package com.example.ui.screens.buyer

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ArtisanEntity
import com.example.data.local.ProductEntity
import com.example.data.models.ProductReview
import com.example.data.models.TrustScoreDetails
import com.example.ui.components.AudioPlayButton
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerProductDetailScreen(
    product: ProductEntity,
    artisan: ArtisanEntity?,
    isWishlisted: Boolean,
    trustScore: TrustScoreDetails,
    reviews: List<ProductReview>,
    onBack: () -> Unit,
    onToggleWishlist: () -> Unit,
    onAddToCart: (Int) -> Unit,
    onBuyNow: (Int) -> Unit,
    onArtisanClick: () -> Unit,
    onOpenTrustScore: () -> Unit,
    onOpenBulkRfq: () -> Unit
) {
    val context = LocalContext.current
    var selectedQuantity by remember { mutableStateOf(1) }
    var selectedImageIndex by remember { mutableStateOf(0) }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    val imgResId = remember(product.imageDrawableRes) {
        val id = context.resources.getIdentifier(product.imageDrawableRes, "drawable", context.packageName)
        if (id != 0) id else com.example.R.drawable.img_saree_sample
    }

    val galleryImages = remember(product.imageDrawableRes) {
        listOf(
            product.imageDrawableRes,
            "img_pottery_sample",
            "img_brass_dhokra",
            "img_wood_craft"
        )
    }

    Scaffold(
        bottomBar = {
            Surface(
                shadowElevation = 12.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quantity Selector
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (selectedQuantity > 1) selectedQuantity-- },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = "$selectedQuantity",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(
                                    onClick = { if (selectedQuantity < product.stockQuantity) selectedQuantity++ },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Add to Cart & Buy Now Buttons
                        OutlinedButton(
                            onClick = { onAddToCart(selectedQuantity) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, TerracottaPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add to Cart", fontWeight = FontWeight.Bold, color = TerracottaPrimary, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                onBuyNow(selectedQuantity)
                                showCheckoutDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text("Buy Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Direct Bulk Procurement Link
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenBulkRfq() },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = PeacockTealTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Planning a wedding or corporate order? Request Bulk Quote",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeacockTealTertiary
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmOffWhiteCanvas)
                .padding(padding)
        ) {
            // 1. IMMERSIVE HERO IMAGE & TOP BAR
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    val activeResName = galleryImages.getOrElse(selectedImageIndex) { product.imageDrawableRes }
                    val activeImgRes = remember(activeResName) {
                        val id = context.resources.getIdentifier(activeResName, "drawable", context.packageName)
                        if (id != 0) id else com.example.R.drawable.img_saree_sample
                    }

                    Image(
                        painter = painterResource(id = activeImgRes),
                        contentDescription = product.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient for top bar icons
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                                )
                            )
                    )

                    // Top Action Bar (Back, GI Tag, Wishlist)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.9f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = DeepCharcoalSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TerracottaPrimary
                            ) {
                                Text(
                                    text = "100% HANDMADE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = onToggleWishlist,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.9f))
                            ) {
                                Icon(
                                    imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Wishlist",
                                    tint = if (isWishlisted) TerracottaPrimary else DeepCharcoalSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Gallery Thumbnails (bottom of image)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        galleryImages.forEachIndexed { index, imgName ->
                            val thumbResId = remember(imgName) {
                                val id = context.resources.getIdentifier(imgName, "drawable", context.packageName)
                                if (id != 0) id else com.example.R.drawable.img_saree_sample
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (selectedImageIndex == index) 2.dp else 0.dp,
                                        color = if (selectedImageIndex == index) GoldenAmberSecondary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedImageIndex = index }
                            ) {
                                Image(
                                    painter = painterResource(id = thumbResId),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            // 2. PRODUCT HEADER INFO
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PeacockTealTertiary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = product.category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PeacockTealTertiary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldenAmberSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "4.9",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepCharcoalSurface
                            )
                            Text(
                                text = " (34 Reviews)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = product.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoalSurface,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Price & Fair Trade Guarantee
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "₹${product.activePrice.toInt()}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = TerracottaPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SuccessGreen.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "100% Fair Artisan Remuneration",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }
            }

            // 3. ARTISAN PROFILE CARD (Buyer View)
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, TerracottaPrimary, CircleShape)
                            ) {
                                val avatarResId = remember(artisan?.avatarDrawableRes) {
                                    val id = context.resources.getIdentifier(artisan?.avatarDrawableRes ?: "img_artisan_hero", "drawable", context.packageName)
                                    if (id != 0) id else com.example.R.drawable.img_artisan_hero
                                }
                                Image(
                                    painter = painterResource(id = avatarResId),
                                    contentDescription = product.artisanName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = product.artisanName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepCharcoalSurface
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = PeacockTealTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = product.region,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${artisan?.experienceYears ?: 24} years heritage master craftsperson",
                                    fontSize = 11.sp,
                                    color = TerracottaPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Trust Score Pill and Profile Navigation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PeacockTealTertiary.copy(alpha = 0.12f),
                                modifier = Modifier.clickable { onOpenTrustScore() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = PeacockTealTertiary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Trust Score: ${trustScore.overallScore}/100",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PeacockTealTertiary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info",
                                        tint = PeacockTealTertiary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            TextButton(
                                onClick = onArtisanClick,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("View Full Artisan Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // 4. CRAFT STORY (Visually prominent highlight card)
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, GoldenAmberSecondary.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = TerracottaPrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "❤️ CRAFT STORY & HERITAGE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            AudioPlayButton(
                                audioText = product.culturalStory,
                                isPlaying = false,
                                onToggle = {}
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "“${product.culturalStory}”",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 21.sp,
                            color = DeepCharcoalSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Lineage: ${product.storyLineage}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldenAmberSecondary
                        )
                    }
                }
            }

            // 5. SPECIFICATION MATRIX
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Authentic Craft Specifications",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepCharcoalSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SpecMatrixRow(title = "Raw Materials", value = product.materialsList)
                        SpecMatrixRow(title = "Craft Technique", value = product.craftTechnique)
                        SpecMatrixRow(title = "Dimensions", value = product.dimensions)
                        SpecMatrixRow(title = "Weight", value = product.weight)
                        SpecMatrixRow(title = "Production Time", value = "${product.productionDays} Days of Handwork (${product.laborHours.toInt()} artisan hours)")
                        SpecMatrixRow(title = "Care Instructions", value = product.careInstructions)
                        SpecMatrixRow(title = "Packaging", value = product.packagingSuggestions)
                    }
                }
            }

            // 6. VERIFIED BUYER REVIEWS
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Verified Buyer Reviews",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepCharcoalSurface
                            )
                            Text(
                                text = "4.9 ★ Rating",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        reviews.forEach { review ->
                            ReviewItem(review = review)
                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }
                    }
                }
            }

            // Bottom Spacing for Floating Bar
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Order Reserved", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Your direct artisan order for $selectedQuantity × ${product.title} has been staged.",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total: ₹${(product.activePrice * selectedQuantity).toInt()} • 100% artisan direct payment",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerracottaPrimary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCheckoutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Continue Exploring")
                }
            }
        )
    }
}

@Composable
private fun SpecMatrixRow(title: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(120.dp)
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeepCharcoalSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun ReviewItem(review: ProductReview) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = review.reviewerName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoalSurface
                )
                if (review.verifiedPurchase) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SuccessGreen.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "✓ Verified Purchase",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(text = review.date, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(5) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = GoldenAmberSecondary,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = review.location,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = review.comment,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 17.sp
        )
    }
}
