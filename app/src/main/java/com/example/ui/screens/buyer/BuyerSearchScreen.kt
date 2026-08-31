package com.example.ui.screens.buyer

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProductEntity
import com.example.data.models.BuyerFilterState
import com.example.data.models.ProductSortOption
import com.example.data.models.TrustScoreDetails
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerSearchScreen(
    products: List<ProductEntity>,
    wishlistIds: Set<String>,
    filterState: BuyerFilterState,
    initialCategory: String? = null,
    onProductClick: (ProductEntity) -> Unit,
    onToggleWishlist: (String) -> Unit,
    onAddToCart: (ProductEntity) -> Unit,
    onFilterChange: (BuyerFilterState) -> Unit,
    onOpenFilterSheet: () -> Unit,
    onOpenVoiceSearch: () -> Unit,
    onOpenTrustScore: (TrustScoreDetails) -> Unit,
    getTrustScore: (String) -> TrustScoreDetails
) {
    var searchQuery by remember { mutableStateOf(filterState.searchQuery) }
    var selectedSortOption by remember { mutableStateOf(filterState.sortOption) }
    var isSortMenuOpen by remember { mutableStateOf(false) }

    val categories = listOf(
        "All", "Textiles", "Pottery", "Woodcraft", "Jewellery",
        "Basketry", "Paintings", "Home Decor", "Handloom", "Regional Crafts"
    )

    var activeCategory by remember {
        mutableStateOf(initialCategory ?: filterState.selectedCategory ?: "All")
    }

    LaunchedEffect(initialCategory) {
        if (initialCategory != null) {
            activeCategory = initialCategory
            onFilterChange(filterState.copy(selectedCategory = if (initialCategory == "All") null else initialCategory))
        }
    }

    // Filter and sort products
    val filteredProducts = remember(products, searchQuery, activeCategory, filterState, selectedSortOption) {
        var list = products.filter { product ->
            val matchesSearch = searchQuery.isBlank() ||
                    product.title.contains(searchQuery, ignoreCase = true) ||
                    product.artisanName.contains(searchQuery, ignoreCase = true) ||
                    product.category.contains(searchQuery, ignoreCase = true) ||
                    product.materialsList.contains(searchQuery, ignoreCase = true) ||
                    product.craftTechnique.contains(searchQuery, ignoreCase = true) ||
                    product.region.contains(searchQuery, ignoreCase = true)

            val matchesCategory = activeCategory == "All" ||
                    product.category.contains(activeCategory, ignoreCase = true)

            val matchesPrice = product.activePrice >= filterState.minPrice &&
                    product.activePrice <= filterState.maxPrice

            val matchesVerified = !filterState.verifiedOnly || product.isVerified

            val matchesStock = !filterState.inStockOnly || product.stockQuantity > 0

            val matchesRating = product.listingScore >= (filterState.minRating * 18).toInt()

            matchesSearch && matchesCategory && matchesPrice && matchesVerified && matchesStock && matchesRating
        }

        when (selectedSortOption) {
            ProductSortOption.RECOMMENDED -> list.sortedByDescending { it.listingScore }
            ProductSortOption.NEWEST -> list.reversed()
            ProductSortOption.PRICE_LOW_TO_HIGH, ProductSortOption.PRICE_LOW_HIGH -> list.sortedBy { it.activePrice }
            ProductSortOption.PRICE_HIGH_TO_LOW, ProductSortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.activePrice }
            ProductSortOption.TOP_RATED -> list.sortedByDescending { it.listingScore }
            ProductSortOption.TRENDING -> list.sortedByDescending { it.soldQuantity }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Search Input Row
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onFilterChange(filterState.copy(searchQuery = it))
                    },
                    placeholder = { Text("Search by craft, silk, pottery, region...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TerracottaPrimary
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    onFilterChange(filterState.copy(searchQuery = ""))
                                }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                            IconButton(onClick = onOpenVoiceSearch) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Search",
                                    tint = TerracottaPrimary
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TerracottaPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = activeCategory.equals(cat, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                activeCategory = cat
                                onFilterChange(
                                    filterState.copy(
                                        selectedCategory = if (cat == "All") null else cat
                                    )
                                )
                            },
                            label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TerracottaPrimary,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) TerracottaPrimary else MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action Bar: Results count, Sort Dropdown & Filter Sheet Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredProducts.size} Authentic Crafts Found",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Sort Dropdown
                        Box {
                            TextButton(
                                onClick = { isSortMenuOpen = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = DeepCharcoalSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = selectedSortOption.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepCharcoalSurface
                                )
                            }

                            DropdownMenu(
                                expanded = isSortMenuOpen,
                                onDismissRequest = { isSortMenuOpen = false }
                            ) {
                                ProductSortOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.displayName, fontSize = 12.sp) },
                                        onClick = {
                                            selectedSortOption = option
                                            onFilterChange(filterState.copy(sortOption = option))
                                            isSortMenuOpen = false
                                        },
                                        leadingIcon = if (selectedSortOption == option) {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = TerracottaPrimary) }
                                        } else null
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Filter Button
                        OutlinedButton(
                            onClick = onOpenFilterSheet,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracottaPrimary),
                            border = BorderStroke(1.dp, TerracottaPrimary),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Filters", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(TerracottaPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Handcrafted Items Found",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoalSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try searching for different materials, crafts, or resetting your price filters.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            searchQuery = ""
                            activeCategory = "All"
                            onFilterChange(BuyerFilterState())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset All Filters", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
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
                items(filteredProducts) { product ->
                    BuyerProductGridCard(
                        product = product,
                        isWishlisted = wishlistIds.contains(product.id),
                        trustScore = getTrustScore(product.artisanName),
                        onClick = { onProductClick(product) },
                        onToggleWishlist = { onToggleWishlist(product.id) },
                        onAddToCart = { onAddToCart(product) },
                        onOpenTrustScore = { onOpenTrustScore(getTrustScore(product.artisanName)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerFilterBottomSheet(
    filterState: BuyerFilterState,
    onDismiss: () -> Unit,
    onApply: (BuyerFilterState) -> Unit,
    onReset: () -> Unit
) {
    var minPrice by remember { mutableStateOf(filterState.minPrice.toFloat()) }
    var maxPrice by remember { mutableStateOf(filterState.maxPrice.toFloat()) }
    var verifiedOnly by remember { mutableStateOf(filterState.verifiedOnly) }
    var inStockOnly by remember { mutableStateOf(filterState.inStockOnly) }
    var minRating by remember { mutableStateOf(filterState.minRating.toFloat()) }
    var selectedLocation by remember { mutableStateOf(filterState.selectedLocation) }
    var selectedMaterial by remember { mutableStateOf(filterState.selectedMaterial) }

    val locations = listOf("All", "Tamil Nadu", "Rajasthan", "Karnataka", "Chhattisgarh", "Bihar", "Odisha", "Assam", "Telangana")
    val materials = listOf("All", "Pure Silk", "Quartz & Cobalt", "Hale Wood", "Recycled Brass", "Bamboo Cane", "925 Silver")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Crafts & Artisans",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = {
                    onReset()
                    minPrice = 0f
                    maxPrice = 10000f
                    verifiedOnly = false
                    inStockOnly = false
                    minRating = 0f
                    selectedLocation = null
                    selectedMaterial = null
                }) {
                    Text("Reset All", color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 1. Price Range
            Text(
                text = "Price Range: ₹${minPrice.toInt()} – ₹${maxPrice.toInt()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            RangeSlider(
                value = minPrice..maxPrice,
                onValueChange = { range ->
                    minPrice = range.start
                    maxPrice = range.endInclusive
                },
                valueRange = 0f..10000f,
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = TerracottaPrimary,
                    activeTrackColor = TerracottaPrimary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Region / Origin Location
            Text(text = "Craft Origin & State", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(locations) { loc ->
                    val isSelected = (selectedLocation == null && loc == "All") || selectedLocation == loc
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedLocation = if (loc == "All") null else loc
                        },
                        label = { Text(loc, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Materials
            Text(text = "Authentic Material", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(materials) { mat ->
                    val isSelected = (selectedMaterial == null && mat == "All") || selectedMaterial == mat
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedMaterial = if (mat == "All") null else mat
                        },
                        label = { Text(mat, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Verification & Availability Switches
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Verified Artisans Only", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("GI tagged & government certified craftspeople", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = verifiedOnly,
                            onCheckedChange = { verifiedOnly = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = TerracottaPrimary, checkedTrackColor = TerracottaPrimary.copy(alpha = 0.5f))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("In Stock & Ready to Ship", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Fast dispatch from weaver cluster", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = inStockOnly,
                            onCheckedChange = { inStockOnly = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = TerracottaPrimary, checkedTrackColor = TerracottaPrimary.copy(alpha = 0.5f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    onApply(
                        filterState.copy(
                            minPrice = minPrice.toDouble(),
                            maxPrice = maxPrice.toDouble(),
                            verifiedOnly = verifiedOnly,
                            inStockOnly = inStockOnly,
                            minRating = minRating.toDouble(),
                            selectedLocation = selectedLocation,
                            selectedMaterial = selectedMaterial
                        )
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Apply Filters", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
