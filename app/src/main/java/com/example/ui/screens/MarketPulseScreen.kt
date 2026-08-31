@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.models.SupportedLanguage
import com.example.data.repository.KarigarRepository
import com.example.data.service.DemandInsight
import com.example.data.service.InsightType
import com.example.ui.components.AudioPlayButton
import com.example.ui.theme.*
import com.example.utils.AudioVoiceHelper

enum class MarketPulseTab(val title: String, val icon: String) {
    TRENDING("Trending", "🔥"),
    GROWING("Growing", "📈"),
    PRICING("Pricing", "💰"),
    REGIONAL("Regional", "📍"),
    SEASONAL("Seasonal", "🎉")
}

@Composable
fun MarketPulseScreen(
    currentLanguage: SupportedLanguage,
    repository: KarigarRepository,
    audioHelper: AudioVoiceHelper?,
    onNavigateBack: () -> Unit,
    onOpenSaathiWithQuery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(MarketPulseTab.TRENDING) }
    val demandService = repository.demandIntelligenceService
    val allInsights by demandService.getMarketPulseStream().collectAsState(initial = emptyList())

    val filteredInsights = remember(selectedTab, allInsights) {
        when (selectedTab) {
            MarketPulseTab.TRENDING -> demandService.getTrendingCrafts()
            MarketPulseTab.GROWING -> demandService.getGrowingInsights()
            MarketPulseTab.PRICING -> demandService.getPricingOpportunities()
            MarketPulseTab.REGIONAL -> demandService.getRegionalDemands()
            MarketPulseTab.SEASONAL -> demandService.getSeasonalDemands()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Market Pulse",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TerracottaPrimary
                        )
                        Text(
                            "Demand & Pricing Intelligence",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_market_pulse_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    AudioPlayButton(
                        textToSpeak = "Market Pulse intelligence screen. Explore high demand craft categories, regional bulk buyers, and pricing opportunities.",
                        language = currentLanguage,
                        audioHelper = audioHelper
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            // Seeded Demo Disclaimer Banner (Transparency Rule)
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PeacockTealTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Market Intelligence Engine",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = PeacockTealTertiary
                            )
                            Text(
                                text = "Seeded prototype intelligence based on B2B handloom & handicraft trade queries. Not real-time financial advice.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Quick Stats Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TerracottaPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Festival Pre-Season Demand",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "+38% Bulk RFQs",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Top category: Handloom Sarees & Festive Clay",
                                color = GoldenAmberLight,
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Section Filter Tabs
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(MarketPulseTab.values()) { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) TerracottaPrimary else MaterialTheme.colorScheme.surface,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shadowElevation = if (isSelected) 2.dp else 0.dp,
                            modifier = Modifier
                                .clickable { selectedTab = tab }
                                .testTag("tab_market_${tab.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tab.icon, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Insights Cards List
            if (filteredInsights.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No market trends available for this category right now.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredInsights) { insight ->
                    MarketInsightCard(
                        insight = insight,
                        currentLanguage = currentLanguage,
                        audioHelper = audioHelper,
                        onExplainWithSaathi = {
                            onOpenSaathiWithQuery("Explain why: ${insight.headline}. What should I make or change in my pricing?")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MarketInsightCard(
    insight: DemandInsight,
    currentLanguage: SupportedLanguage,
    audioHelper: AudioVoiceHelper?,
    onExplainWithSaathi: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badge & Category Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(insight.type.badgeColorHex).copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${insight.type.label} • +${insight.growthPercentage}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(insight.type.badgeColorHex)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = insight.targetRegion,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    AudioPlayButton(
                        textToSpeak = "${insight.headline}. Recommendation: ${insight.recommendation}",
                        language = currentLanguage,
                        audioHelper = audioHelper
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Headline
            Text(
                text = insight.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = insight.headline,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Metric pills row
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
                        Text("Active Buyers", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${insight.activeBuyerCount} verified", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Avg Target Price", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${insight.avgExpectedPrice.toInt()} / pc", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Confidence", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${insight.confidenceScore}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actionable Recommendation Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = GoldenAmberLight.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldenAmberSecondary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = GoldenAmberSecondary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = insight.recommendation,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Explain This (Saathi Trigger) Button
            OutlinedButton(
                onClick = onExplainWithSaathi,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracottaPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, TerracottaPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_explain_insight_${insight.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Explain this with Saathi",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
