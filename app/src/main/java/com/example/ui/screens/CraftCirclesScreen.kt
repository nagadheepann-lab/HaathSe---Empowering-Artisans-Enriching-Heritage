@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CraftCircleEntity
import com.example.data.models.SupportedLanguage
import com.example.data.repository.KarigarRepository
import com.example.ui.components.AudioPlayButton
import com.example.ui.screens.circles.*
import com.example.ui.theme.*
import com.example.utils.AudioVoiceHelper

enum class CraftCirclesScreenMode {
    DISCOVERY,
    DETAIL,
    BULK_ORDER
}

@Composable
fun CraftCirclesScreen(
    currentLanguage: SupportedLanguage,
    repository: KarigarRepository,
    audioHelper: AudioVoiceHelper?,
    onNavigateBack: () -> Unit,
    onOpenSaathiWithQuery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = repository.craftCircleViewModel

    val circles by viewModel.filteredCircles.collectAsState()
    val allCircles by viewModel.allCircles.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val isLocationPermissionGranted by viewModel.isLocationPermissionGranted.collectAsState()

    val myAllocations by viewModel.myAllocations.collectAsState()
    val selectedCircle by viewModel.selectedCircle.collectAsState()
    val selectedCircleMembers by viewModel.selectedCircleMembers.collectAsState()
    val circleJoinRequests by viewModel.circleJoinRequests.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val pendingInvitations = myAllocations.filter { it.invitationStatus == "PENDING" }
    val activeWorkAllocations = myAllocations.filter { it.invitationStatus == "ACCEPTED" }

    var screenMode by remember { mutableStateOf(CraftCirclesScreenMode.DISCOVERY) }
    var activeTab by remember { mutableStateOf(0) } // 0: Explore Circles, 1: Invitations (N), 2: My Work (N)

    var showJoinDialog by remember { mutableStateOf(false) }
    var circleForJoin by remember { mutableStateOf<CraftCircleEntity?>(null) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    when (screenMode) {
        CraftCirclesScreenMode.DETAIL -> {
            selectedCircle?.let { circle ->
                CircleDetailScreen(
                    circle = circle,
                    members = selectedCircleMembers,
                    joinRequests = circleJoinRequests,
                    onBack = { screenMode = CraftCirclesScreenMode.DISCOVERY },
                    onJoinClick = {
                        circleForJoin = circle
                        showJoinDialog = true
                    },
                    onOrderBulkClick = {
                        screenMode = CraftCirclesScreenMode.BULK_ORDER
                    },
                    onApproveRequest = { req ->
                        viewModel.approveJoinRequest(req)
                    }
                )
            } ?: run {
                screenMode = CraftCirclesScreenMode.DISCOVERY
            }
        }

        CraftCirclesScreenMode.BULK_ORDER -> {
            selectedCircle?.let { circle ->
                B2BBulkOrderScreen(
                    circle = circle,
                    members = selectedCircleMembers,
                    onBack = { screenMode = CraftCirclesScreenMode.DETAIL },
                    onSubmitBulkOrder = { reqData, allocations ->
                        viewModel.createBulkOrder(
                            circle = circle,
                            requestData = reqData,
                            customAllocations = allocations
                        ) {
                            screenMode = CraftCirclesScreenMode.DISCOVERY
                            activeTab = 2 // Navigate to My Work / active production
                        }
                    }
                )
            } ?: run {
                screenMode = CraftCirclesScreenMode.DISCOVERY
            }
        }

        CraftCirclesScreenMode.DISCOVERY -> {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("Craft Circles", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TerracottaPrimary)
                                Text("Collaborative Artisan Commerce & Guilds", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_circles_back")) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { showLocationDialog = true },
                                modifier = Modifier.testTag("btn_circle_location")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NearMe,
                                    contentDescription = "Location",
                                    tint = if (isLocationPermissionGranted) TerracottaPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { showFilterSheet = true },
                                modifier = Modifier.testTag("btn_circle_filters")
                            ) {
                                Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filters")
                            }
                            AudioPlayButton(
                                textToSpeak = "Welcome to Craft Circles. Combine handloom and pottery capacity with fellow master artisans to fulfill high-volume institutional B2B bulk orders.",
                                language = currentLanguage,
                                audioHelper = audioHelper
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(WarmBgLight)
                ) {
                    // Navigation Tabs
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = TerracottaPrimary
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = {
                                Text("Craft Circles (${circles.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Invitations", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    if (pendingInvitations.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Badge(containerColor = TerracottaPrimary) {
                                            Text("${pendingInvitations.size}", color = Color.White, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        )
                        Tab(
                            selected = activeTab == 2,
                            onClick = { activeTab = 2 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("My Work", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    if (activeWorkAllocations.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Badge(containerColor = SuccessGreen) {
                                            Text("${activeWorkAllocations.size}", color = Color.White, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        )
                    }

                    when (activeTab) {
                        0 -> {
                            // Explore Circles Tab
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
                            ) {
                                // Search & Location Banner
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorderBeige.copy(alpha = 0.8f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Search, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            OutlinedTextField(
                                                value = filterState.searchQuery,
                                                onValueChange = { viewModel.updateFilter(filterState.copy(searchQuery = it)) },
                                                placeholder = { Text("Search circles by craft, city, or weave...", fontSize = 12.sp) },
                                                modifier = Modifier.weight(1f),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    unfocusedBorderColor = Color.Transparent,
                                                    focusedBorderColor = Color.Transparent
                                                ),
                                                singleLine = true
                                            )
                                            if (filterState.searchQuery.isNotEmpty()) {
                                                IconButton(onClick = { viewModel.updateFilter(filterState.copy(searchQuery = "")) }) {
                                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                // Quick Horizontal Craft Filters
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf("All Crafts", "Handloom", "Blue Pottery", "Dhokra Metalcraft", "Woodcraft").forEach { craft ->
                                            val isSelected = if (craft == "All Crafts") filterState.selectedCraft == null else filterState.selectedCraft == craft
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    viewModel.updateFilter(
                                                        filterState.copy(selectedCraft = if (craft == "All Crafts") null else craft)
                                                    )
                                                },
                                                label = { Text(craft, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = TerracottaPrimary,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }

                                // Location context bar
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = SandGoldSecondary.copy(alpha = 0.12f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Nearest Hub: ${userLocation ?: "All India"}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            TextButton(onClick = { showLocationDialog = true }, contentPadding = PaddingValues(0.dp)) {
                                                Text("Change", fontSize = 11.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                // Circle Cards List
                                if (circles.isEmpty()) {
                                    item {
                                        com.example.ui.components.StandardEmptyState(
                                            type = com.example.ui.components.EmptyStateType.CRAFT_CIRCLES
                                        )
                                    }
                                } else {
                                    items(circles) { circle ->
                                        CraftCircleCard(
                                            circle = circle,
                                            onClick = {
                                                viewModel.selectCircle(circle)
                                                screenMode = CraftCirclesScreenMode.DETAIL
                                            },
                                            onJoinClick = {
                                                circleForJoin = circle
                                                showJoinDialog = true
                                            },
                                            onBulkOrderClick = {
                                                viewModel.selectCircle(circle)
                                                screenMode = CraftCirclesScreenMode.BULK_ORDER
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        1 -> {
                            // Invitations Tab
                            ArtisanInvitationsView(
                                invitations = pendingInvitations,
                                onAccept = { allocId -> viewModel.respondToInvitation(allocId, true) },
                                onDecline = { allocId -> viewModel.respondToInvitation(allocId, false) }
                            )
                        }

                        2 -> {
                            // My Work Tab
                            ArtisanMyWorkView(
                                activeAllocations = activeWorkAllocations,
                                onUpdateProgress = { allocId, progress -> viewModel.updateProductionProgress(allocId, progress) },
                                onMarkReady = { allocId -> viewModel.markReady(allocId) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Join Circle Dialog
    if (showJoinDialog && circleForJoin != null) {
        JoinCircleDialog(
            circle = circleForJoin!!,
            onDismiss = {
                showJoinDialog = false
                circleForJoin = null
            },
            onSubmit = { formData ->
                viewModel.submitJoinRequest(
                    circle = circleForJoin!!,
                    formData = formData,
                    onSuccess = {
                        showJoinDialog = false
                        circleForJoin = null
                    }
                )
            }
        )
    }

    // Location Permission & Hub Dialog
    if (showLocationDialog) {
        LocationPermissionRationaleDialog(
            onDismiss = { showLocationDialog = false },
            onGrantPermission = {
                viewModel.setLocationPermissionGranted(true)
                showLocationDialog = false
            },
            onManualLocationSelect = { city ->
                viewModel.setUserLocation(city)
                showLocationDialog = false
            }
        )
    }

    // Comprehensive Filter Modal Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filter Craft Circles", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    TextButton(onClick = { viewModel.updateFilter(com.example.data.models.CraftCircleFilterState()) }) {
                        Text("Reset All", color = TerracottaPrimary)
                    }
                }

                // Specialization
                OutlinedTextField(
                    value = filterState.selectedSpecialization ?: "",
                    onValueChange = { viewModel.updateFilter(filterState.copy(selectedSpecialization = if (it.isBlank()) null else it)) },
                    label = { Text("Specialization (e.g. Silk, Glaze, Lost-Wax)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Minimum Capacity
                Column {
                    Text("Minimum Monthly Capacity (${filterState.minCapacity} pcs)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = filterState.minCapacity.toFloat(),
                        onValueChange = { viewModel.updateFilter(filterState.copy(minCapacity = it.toInt())) },
                        valueRange = 0f..500f,
                        steps = 4,
                        colors = SliderDefaults.colors(thumbColor = TerracottaPrimary, activeTrackColor = TerracottaPrimary)
                    )
                }

                // Distance Radius
                Column {
                    Text("Maximum Distance Radius (${filterState.maxDistanceKm.toInt()} km)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = filterState.maxDistanceKm.toFloat(),
                        onValueChange = { viewModel.updateFilter(filterState.copy(maxDistanceKm = it.toDouble())) },
                        valueRange = 10f..1000f,
                        colors = SliderDefaults.colors(thumbColor = TerracottaPrimary, activeTrackColor = TerracottaPrimary)
                    )
                }

                // Available for Bulk Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Available for Bulk Orders", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = filterState.availableOnly,
                        onCheckedChange = { viewModel.updateFilter(filterState.copy(availableOnly = it)) }
                    )
                }

                Button(
                    onClick = { showFilterSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
}
