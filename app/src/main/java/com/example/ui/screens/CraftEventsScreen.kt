@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.CraftEventEntity
import com.example.data.models.SupportedLanguage
import com.example.data.repository.KarigarRepository
import com.example.ui.components.AudioPlayButton
import com.example.ui.theme.*
import com.example.utils.AudioVoiceHelper
import com.example.utils.MapIntentHelper
import kotlinx.coroutines.launch

@Composable
fun CraftEventsScreen(
    currentLanguage: SupportedLanguage,
    repository: KarigarRepository,
    audioHelper: AudioVoiceHelper?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val events by repository.allCraftEvents.collectAsState(initial = emptyList())

    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedDetailEvent by remember { mutableStateOf<CraftEventEntity?>(null) }
    var showSuccessRegistrationDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var userWorkshopLocation by remember { mutableStateOf("Kanchipuram, Tamil Nadu") }
    var userLat by remember { mutableDoubleStateOf(12.8342) }
    var userLng by remember { mutableDoubleStateOf(79.7036) }

    val craftClusters = listOf(
        Triple("Kanchipuram, Tamil Nadu", 12.8342, 79.7036),
        Triple("Varanasi, Uttar Pradesh", 25.3176, 82.9739),
        Triple("Jaipur, Rajasthan", 26.9124, 75.7873),
        Triple("Pochampally, Telangana", 17.3457, 78.8156),
        Triple("Bastar, Chhattisgarh", 19.0748, 82.0296),
        Triple("Mysore, Karnataka", 12.2958, 76.6394)
    )

    fun calculateDist(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return Math.round((r * c) * 10.0) / 10.0
    }

    val categories = listOf(
        "ALL" to "All Events",
        "TRADE_FAIR" to "Trade Fairs",
        "EXHIBITION" to "Exhibitions",
        "GOVT_SUPPORTED" to "Govt Supported",
        "HANDICRAFT_FAIR" to "Handicraft Fairs",
        "ARTISAN_MARKET" to "Artisan Markets"
    )

    val filteredEvents = remember(events, selectedCategoryFilter, searchQuery, userLat, userLng) {
        events.map { ev ->
            val dist = calculateDist(userLat, userLng, ev.latitude, ev.longitude)
            ev.copy(distanceKm = dist)
        }.filter { event ->
            val matchesCategory = (selectedCategoryFilter == "ALL" || event.eventType.equals(selectedCategoryFilter, ignoreCase = true))
            val matchesSearch = searchQuery.isBlank() ||
                    event.title.contains(searchQuery, ignoreCase = true) ||
                    event.location.contains(searchQuery, ignoreCase = true) ||
                    event.organizer.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }.sortedBy { it.distanceKm }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Craft Events & Melas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TerracottaPrimary
                        )
                        Text(
                            text = "Trade Fairs, Exhibitions & Artisan Markets",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_events_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    AudioPlayButton(
                        textToSpeak = "Craft Events and Melas. Explore national trade fairs, government subsidized exhibitions, and artisan markets with direct Google Maps directions.",
                        language = currentLanguage,
                        audioHelper = audioHelper
                    )
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
            // 1. HERO BANNER
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.height(140.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_craft_mela),
                            contentDescription = "Craft Mela Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = GoldenAmberSecondary
                                ) {
                                    Text(
                                        text = "100% DIRECT BUYER SOURCING",
                                        color = DeepCharcoalSurface,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SuccessGreen
                                ) {
                                    Text(
                                        text = "GOVT SUBSIDIZED",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "National Craft Fairs & Exhibitions",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Apply for stalls, verify buyers, and navigate with Google Maps",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // 1.5 WORKSHOP LOCATION & CLUSTER SELECTOR
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, GoldenAmberSecondary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Your Workshop Location",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = userWorkshopLocation,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PeacockTealLight
                            ) {
                                Text(
                                    text = "Auto-Sorted by Distance",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PeacockTealTertiary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Cluster Switcher
                        Text(
                            text = "Quick Select Craft Cluster:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            craftClusters.forEach { (clusterName, lat, lng) ->
                                val isSelected = userWorkshopLocation == clusterName
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) TerracottaPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, if (isSelected) TerracottaPrimary else MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .clickable {
                                            userWorkshopLocation = clusterName
                                            userLat = lat
                                            userLng = lng
                                        }
                                        .testTag("chip_cluster_${clusterName.substringBefore(",")}")
                                ) {
                                    Text(
                                        text = clusterName.substringBefore(","),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. SEARCH & CATEGORY FILTER CHIPS
            item {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by event, city, or organizer...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_events")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal Category Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { (catKey, catLabel) ->
                            val isSelected = selectedCategoryFilter == catKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryFilter = catKey },
                                label = {
                                    Text(
                                        text = catLabel,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TerracottaPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("filter_chip_$catKey")
                            )
                        }
                    }
                }
            }

            // 3. EVENTS COUNT HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Found ${filteredEvents.size} Craft Events",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoalSurface
                    )
                    Text(
                        text = "Sorted by Date",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 4. EVENT CARDS LIST
            if (filteredEvents.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventBusy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No events found matching your filter",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try clearing search or choosing 'All Events'",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredEvents, key = { it.id }) { event ->
                    CraftEventCard(
                        event = event,
                        onGetDirections = {
                            MapIntentHelper.openDirections(
                                context = context,
                                latitude = event.latitude,
                                longitude = event.longitude,
                                destinationName = event.title
                            )
                        },
                        onViewDetails = { selectedDetailEvent = event }
                    )
                }
            }
        }
    }

    // 5. EVENT DETAIL DIALOG
    selectedDetailEvent?.let { event ->
        CraftEventDetailDialog(
            event = event,
            onDismiss = { selectedDetailEvent = null },
            onGetDirections = {
                MapIntentHelper.openDirections(
                    context = context,
                    latitude = event.latitude,
                    longitude = event.longitude,
                    destinationName = event.title
                )
            },
            onCallOrganizer = { phone ->
                MapIntentHelper.openDialer(context, phone)
            },
            onEmailOrganizer = { email ->
                MapIntentHelper.openEmail(context, email, "Stall Inquiry: ${event.title}")
            },
            onOpenWebsite = { url ->
                MapIntentHelper.openWebPage(context, url)
            },
            onRegisterStall = {
                coroutineScope.launch {
                    repository.registerForEvent(event.id)
                    selectedDetailEvent = null
                    showSuccessRegistrationDialog = true
                }
            }
        )
    }

    // 6. SUCCESS REGISTRATION ALERT
    if (showSuccessRegistrationDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessRegistrationDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Stall Application Submitted! ✓",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Your verified Artisan Pehchan Profile & GI Certification have been submitted to the event nodal committee.",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessGreen.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✓ Notification alert generated in your Notification Center\n✓ SMS pass will be sent 48 hours prior to event",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SuccessGreen,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessRegistrationDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Done")
                }
            }
        )
    }
}

// =======================================================================
// EVENT CARD COMPONENT
// =======================================================================

@Composable
fun CraftEventCard(
    event: CraftEventEntity,
    onGetDirections: () -> Unit,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageResId = remember(event.imageRes) {
        val id = context.resources.getIdentifier(event.imageRes, "drawable", context.packageName)
        if (id != 0) id else R.drawable.img_craft_mela
    }

    val typeLabel = when (event.eventType) {
        "TRADE_FAIR" -> "Trade Fair"
        "EXHIBITION" -> "Exhibition"
        "GOVT_SUPPORTED" -> "Govt Supported"
        "HANDICRAFT_FAIR" -> "Handicraft Fair"
        "ARTISAN_MARKET" -> "Artisan Market"
        else -> "Craft Event"
    }

    val (statusBg, statusTextColor, statusText) = when {
        event.isRegistered || event.registrationStatus == "REGISTERED" ->
            Triple(SuccessGreen.copy(alpha = 0.15f), SuccessGreen, "✓ Registered")
        event.registrationStatus == "CLOSING_SOON" ->
            Triple(GoldenAmberSecondary.copy(alpha = 0.2f), GoldenAmberSecondary, "Closing Soon")
        event.registrationStatus == "INVITE_ONLY" ->
            Triple(Color(0xFF673AB7).copy(alpha = 0.15f), Color(0xFF673AB7), "Invite Only")
        event.registrationStatus == "WAITLIST" ->
            Triple(Color(0xFFE91E63).copy(alpha = 0.15f), Color(0xFFE91E63), "Waitlist")
        else ->
            Triple(PeacockTealTertiary.copy(alpha = 0.15f), PeacockTealTertiary, "Open for Stalls")
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
            .testTag("card_event_${event.id}")
    ) {
        Column {
            // Event Image Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.65f))
                            )
                        )
                )

                // Top Category & Status Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TerracottaPrimary
                    ) {
                        Text(
                            text = typeLabel.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = GoldenAmberSecondary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${event.distanceKm} km away",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Bottom Date & Status overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.dateRange,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = statusText,
                            color = statusTextColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Event Content Body
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = event.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoalSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.location,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Organizer: ${event.organizer}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Subsidy highlight
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SandGoldSecondary.copy(alpha = 0.25f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = GoldenAmberSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = event.subsidyDetails,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = DeepCharcoalSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons: Directions (Maps) + View Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onGetDirections,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, TerracottaPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_directions_${event.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Directions",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    }

                    Button(
                        onClick = onViewDetails,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_details_${event.id}")
                    ) {
                        Text(
                            text = if (event.isRegistered) "View Pass" else "Details & Stall",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

// =======================================================================
// EVENT DETAIL DIALOG
// =======================================================================

@Composable
fun CraftEventDetailDialog(
    event: CraftEventEntity,
    onDismiss: () -> Unit,
    onGetDirections: () -> Unit,
    onCallOrganizer: (String) -> Unit,
    onEmailOrganizer: (String) -> Unit,
    onOpenWebsite: (String) -> Unit,
    onRegisterStall: () -> Unit
) {
    val context = LocalContext.current
    val imageResId = remember(event.imageRes) {
        val id = context.resources.getIdentifier(event.imageRes, "drawable", context.packageName)
        if (id != 0) id else R.drawable.img_craft_mela
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(vertical = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = event.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.85f))
                                )
                            )
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = TerracottaPrimary
                        ) {
                            Text(
                                text = event.eventType.replace("_", " "),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                // Scrollable Content Details
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. DESCRIPTION
                    item {
                        Text(
                            text = "About the Event",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepCharcoalSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }

                    // 2. SCHEDULE & TIMING
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = WarmBgLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = TerracottaPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Dates: ${event.dateRange}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepCharcoalSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = TerracottaPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Hours: ${event.timeSchedule}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 3. VENUE & GOOGLE MAPS DIRECTIONS
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Venue & Location",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepCharcoalSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PeacockTealTertiary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "${event.distanceKm} km away",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PeacockTealTertiary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = TerracottaPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = event.location,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepCharcoalSurface
                                        )
                                        Text(
                                            text = event.fullAddress,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = onGetDirections,
                                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_modal_get_directions")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Directions,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open in Google Maps", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 4. REGISTRATION & SUBSIDY INFORMATION
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = WarmBgLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Stall Registration & Govt Subsidies",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepCharcoalSurface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                DetailInfoRow(title = "Registration Fee", value = event.registrationFee)
                                DetailInfoRow(title = "Deadline", value = event.registrationDeadline)
                                DetailInfoRow(title = "Subsidy Details", value = event.subsidyDetails)
                                DetailInfoRow(title = "Eligibility", value = event.stallRequirements)
                            }
                        }
                    }

                    // 5. ORGANIZER & CONTACT PERSON
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Organizer & Helpdesk",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepCharcoalSurface
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = event.organizer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DeepCharcoalSurface
                                )
                                Text(
                                    text = "Nodal Officer: ${event.contactPerson}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onCallOrganizer(event.contactPhone) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Call", fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { onEmailOrganizer(event.contactEmail) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Email", fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { onOpenWebsite(event.officialWebsite) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Portal", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Action Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back")
                        }

                        Button(
                            onClick = onRegisterStall,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (event.isRegistered) SuccessGreen else TerracottaPrimary
                            ),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("btn_modal_register_stall")
                        ) {
                            Icon(
                                imageVector = if (event.isRegistered) Icons.Default.Check else Icons.Default.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (event.isRegistered) "Registered ✓" else "Register for Stall",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInfoRow(title: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(110.dp)
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = DeepCharcoalSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
