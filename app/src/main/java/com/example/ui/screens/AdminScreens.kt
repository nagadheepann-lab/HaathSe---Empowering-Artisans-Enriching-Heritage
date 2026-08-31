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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.CraftCluster
import com.example.data.models.GovtScheme
import com.example.data.models.SupportedLanguage
import com.example.data.repository.KarigarRepository
import com.example.ui.components.AudioPlayButton
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*
import com.example.utils.AudioVoiceHelper

// ADMIN & MINISTRY ANALYTICS SCREEN
@Composable
fun AdminAnalyticsScreen(
    currentLanguage: SupportedLanguage,
    repository: KarigarRepository,
    audioHelper: AudioVoiceHelper?,
    onNavigateCraftMap: () -> Unit,
    onNavigateSchemes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val artisans by repository.allArtisans.collectAsState(initial = emptyList())
    val products by repository.allProducts.collectAsState(initial = emptyList())
    val requests by repository.allBuyerRequests.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmBgLight)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        item {
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
                            Text(
                                text = "Ministry & NGO Impact Dashboard",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "SIH 26090 • Ministry of Social Justice & Empowerment",
                                fontSize = 11.sp,
                                color = GoldenAmberSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        AudioPlayButton(
                            textToSpeak = "Ministry impact dashboard. Tracking digitized artisans, fair wages, craft cluster preservation, and B2B linkages across India.",
                            language = currentLanguage,
                            audioHelper = audioHelper
                        )
                    }
                }
            }
        }

        // Aggregate Metrics Grid
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminKpiCard(title = "Digitized Artisans", value = "1,480+", sub = "18 States", icon = Icons.Default.Groups, color = TerracottaPrimary, modifier = Modifier.weight(1f))
                AdminKpiCard(title = "AI Catalogs", value = "5,240+", sub = "11 Languages", icon = Icons.Default.AutoAwesome, color = PeacockTealTertiary, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminKpiCard(title = "B2B Deals Closed", value = "384", sub = "₹42.8L Revenue", icon = Icons.Default.Handshake, color = GoldenAmberSecondary, modifier = Modifier.weight(1f))
                AdminKpiCard(title = "Avg Wage Uplift", value = "+38%", sub = "Above Min Wage", icon = Icons.Default.TrendingUp, color = SuccessGreen, modifier = Modifier.weight(1f))
            }
        }

        // Marginalized Communities Inclusion Progress
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Target Community Inclusion & Empowerment", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    CommunityProgressRow("Scheduled Caste / Scheduled Tribe Weavers", 0.74f, "74% Onboarded")
                    Spacer(modifier = Modifier.height(8.dp))
                    CommunityProgressRow("Traditional Women Craft Self-Help Groups", 0.88f, "88% Onboarded")
                    Spacer(modifier = Modifier.height(8.dp))
                    CommunityProgressRow("Remote Tribal Metal & Bamboo Clusters", 0.62f, "62% Onboarded")
                }
            }
        }

        // Quick Hub Links
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onNavigateCraftMap,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("India Craft Map", fontSize = 11.sp)
                }
                Button(
                    onClick = onNavigateSchemes,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PeacockTealTertiary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Govt Schemes", fontSize = 11.sp)
                }
            }
        }

        // Verified Artisan Registrations List
        item {
            Text("Registered Master Craftspersons", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        items(artisans) { art ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_artisan_hero),
                        contentDescription = null,
                        modifier = Modifier.size(46.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = art.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            VerifiedBadge(label = "Verified KYC")
                        }
                        Text(text = "${art.craftSpecialization} • ${art.villageState}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Capacity: ${art.monthlyCapacityUnits} units/mo • ${art.ordersCompleted} fulfilled", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminKpiCard(
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
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = sub, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

@Composable
fun CommunityProgressRow(title: String, progress: Float, label: String) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            color = TerracottaPrimary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
        )
    }
}

// INDIA CRAFT MAP SCREEN
@Composable
fun IndiaCraftMapScreen(
    currentLanguage: SupportedLanguage,
    repository: KarigarRepository,
    audioHelper: AudioVoiceHelper?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clusters = repository.craftClusters
    var selectedCluster by remember { mutableStateOf<CraftCluster?>(clusters.firstOrNull()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("India Artisan Clusters Map", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    AudioPlayButton(
                        textToSpeak = "India Craft Map. Explore traditional clusters from Kanchipuram silk to Bastar Dhokra metalcraft.",
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
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Traditional Heritage Craft Clusters", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Select a regional cluster to inspect heritage lineage and active artisans:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(clusters) { c ->
                                val isSelected = c.id == selectedCluster?.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCluster = c },
                                    label = { Text("${c.state}: ${c.craftName}", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TerracottaLight,
                                        selectedLabelColor = TerracottaPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (selectedCluster != null) {
                val c = selectedCluster!!
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
                                Column {
                                    Text(text = c.craftName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(text = "${c.district}, ${c.state} • ${c.category}", fontSize = 12.sp, color = TerracottaPrimary, fontWeight = FontWeight.SemiBold)
                                }
                                AudioPlayButton(
                                    textToSpeak = "${c.craftName} in ${c.state}. ${c.heritageDescription}",
                                    language = currentLanguage,
                                    audioHelper = audioHelper
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "Heritage History:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = c.heritageDescription, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 17.sp)

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "Key Materials: ${c.keyMaterials}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(text = "Artisan Population: ~${c.approximateArtisans} traditional families", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "Signature Masterpieces:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            c.famousProducts.forEach { fp ->
                                Text(text = "• $fp", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}

// SCHEME FINDER SCREEN
@Composable
fun SchemeFinderScreen(
    currentLanguage: SupportedLanguage,
    repository: KarigarRepository,
    audioHelper: AudioVoiceHelper?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val schemes = repository.govtSchemes

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Government Schemes for Artisans", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    AudioPlayButton(
                        textToSpeak = "Government Schemes directory. Access PM Vishwakarma, AHVY, SFURTI and MUDRA financial assistance.",
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
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldenAmberLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = GoldenAmberSecondary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Central & State Welfare Schemes", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldenAmberSecondary)
                            Text(text = "Direct subsidies, toolkit grants & low-interest collateral-free loans for marginalized artisans.", fontSize = 11.sp, color = TextSecondaryMuted)
                        }
                    }
                }
            }

            items(schemes) { s ->
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = s.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text(text = s.ministry, fontSize = 11.sp, color = TerracottaPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            AudioPlayButton(
                                textToSpeak = "${s.name}. Financial support: ${s.financialSupport}. Eligibility: ${s.eligibility}.",
                                language = currentLanguage,
                                audioHelper = audioHelper
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Financial Support:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SuccessGreen)
                        Text(text = s.financialSupport, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Eligibility:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = s.eligibility, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Key Benefits: ${s.benefits}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Portal: ${s.officialPortal}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TerracottaPrimary)
                                Text(text = "Apply via CSC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}
