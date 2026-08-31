package com.example.ui.screens.buyer

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ArtisanEntity
import com.example.data.local.ProductEntity
import com.example.data.models.TrustScoreDetails
import com.example.ui.components.AudioPlayButton
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerArtisanProfileScreen(
    artisan: ArtisanEntity,
    artisanProducts: List<ProductEntity>,
    wishlistIds: Set<String>,
    trustScore: TrustScoreDetails,
    onBack: () -> Unit,
    onProductClick: (ProductEntity) -> Unit,
    onToggleWishlist: (String) -> Unit,
    onAddToCart: (ProductEntity) -> Unit,
    onOpenTrustScore: () -> Unit,
    onOpenChat: (ArtisanEntity) -> Unit,
    onOpenBulkRfq: () -> Unit
) {
    val context = LocalContext.current
    val avatarResId = remember(artisan.avatarDrawableRes) {
        val id = context.resources.getIdentifier(artisan.avatarDrawableRes, "drawable", context.packageName)
        if (id != 0) id else com.example.R.drawable.img_artisan_hero
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artisan Profile", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenTrustScore) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Trust Score",
                            tint = PeacockTealTertiary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 90.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(WarmOffWhiteCanvas)
        ) {
            // 1. HERO ARTISAN CARD (Spans 2 columns)
            item(span = { GridItemSpan(2) }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, TerracottaPrimary, CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = avatarResId),
                                    contentDescription = artisan.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = artisan.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepCharcoalSurface
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Artisan",
                                        tint = PeacockTealTertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = artisan.craftSpecialization,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TerracottaPrimary
                                )

                                Text(
                                    text = "📍 ${artisan.villageState}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GoldenAmberSecondary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${artisan.experienceYears} Years Heritage Experience",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepCharcoalSurface,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Row (Trust Score, Rating, Completed Orders, Capacity)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onOpenTrustScore() }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = PeacockTealTertiary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${trustScore.overallScore}/100",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PeacockTealTertiary
                                    )
                                }
                                Text("Trust Score", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = GoldenAmberSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${artisan.rating}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepCharcoalSurface
                                    )
                                }
                                Text("Buyer Rating", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${artisan.ordersCompleted}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepCharcoalSurface
                                )
                                Text("Delivered", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${artisan.monthlyCapacityUnits}/mo",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepCharcoalSurface
                                )
                                Text("Capacity", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Direct Chat & Bulk RFQ
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onOpenChat(artisan) },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, PeacockTealTertiary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = PeacockTealTertiary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Chat with Artisan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PeacockTealTertiary)
                            }

                            Button(
                                onClick = onOpenBulkRfq,
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Text("Request Custom Order", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. HERITAGE & CRAFT STORY CARD (Spans 2 columns)
            item(span = { GridItemSpan(2) }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, GoldenAmberSecondary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
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
                                    text = "❤️ TRADITION & LINEAGE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            AudioPlayButton(
                                audioText = artisan.story,
                                isPlaying = false,
                                onToggle = {}
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "“${artisan.story}”",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 21.sp,
                            color = DeepCharcoalSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Certifications: ${artisan.certifications}",
                            fontSize = 11.sp,
                            color = PeacockTealTertiary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // 3. CATALOG HEADER (Spans 2 columns)
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Crafts by ${artisan.name} (${artisanProducts.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoalSurface
                    )
                }
            }

            // 4. ARTISAN'S PRODUCTS GRID
            items(artisanProducts) { product ->
                BuyerProductGridCard(
                    product = product,
                    isWishlisted = wishlistIds.contains(product.id),
                    trustScore = trustScore,
                    onClick = { onProductClick(product) },
                    onToggleWishlist = { onToggleWishlist(product.id) },
                    onAddToCart = { onAddToCart(product) },
                    onOpenTrustScore = onOpenTrustScore
                )
            }
        }
    }
}
