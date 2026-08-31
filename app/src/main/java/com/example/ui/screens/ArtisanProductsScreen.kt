@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ProductEntity
import com.example.data.models.SupportedLanguage
import com.example.data.repository.KarigarRepository
import com.example.ui.components.AudioPlayButton
import com.example.ui.components.ListingScoreBadge
import com.example.ui.components.QRCardDialog
import com.example.ui.components.SmartCraftImage
import com.example.ui.theme.*
import com.example.utils.AudioVoiceHelper
import kotlinx.coroutines.launch

@Composable
fun ArtisanProductsScreen(
    currentLanguage: SupportedLanguage,
    repository: KarigarRepository,
    audioHelper: AudioVoiceHelper?,
    onNavigateBack: () -> Unit,
    onNavigateAddProduct: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val products by repository.allProducts.collectAsState(initial = emptyList())
    var selectedQrProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var filterCategory by remember { mutableStateOf("All") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Products Catalog", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TerracottaPrimary)
                        Text("${products.size} Handcrafted Items", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_products_back")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateAddProduct, modifier = Modifier.testTag("btn_add_product_top")) {
                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Add Product", tint = TerracottaPrimary)
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
        ) {
            if (products.isEmpty()) {
                item {
                    com.example.ui.components.StandardEmptyState(
                        type = com.example.ui.components.EmptyStateType.PRODUCTS,
                        onActionClick = onNavigateAddProduct
                    )
                }
            } else {
                items(products) { prod ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SmartCraftImage(
                                imageIdentifier = prod.imageDrawableRes,
                                contentDescription = prod.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = prod.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ListingScoreBadge(score = prod.listingScore)
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${prod.category} • ${prod.region}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "₹${prod.activePrice.toInt()}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TerracottaPrimary
                                    )
                                    Text(
                                        text = "Stock: ${prod.stockQuantity} pcs",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (prod.stockQuantity <= 2) TerracottaPrimary else SuccessGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Update Stock:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        if (prod.stockQuantity > 0) {
                                            coroutineScope.launch { repository.updateStock(prod.id, prod.stockQuantity - 1) }
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = TerracottaPrimary)
                                }
                                Text(
                                    text = "${prod.stockQuantity}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch { repository.updateStock(prod.id, prod.stockQuantity + 1) }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = TerracottaPrimary)
                                }
                            }

                            OutlinedButton(
                                onClick = { selectedQrProduct = prod },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("QR Passport", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
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
