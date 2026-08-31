package com.example.data.service

import com.example.data.models.CraftCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Insight types for Artisan Market Intelligence
 */
enum class InsightType(val label: String, val badgeColorHex: Long) {
    HIGH_DEMAND("HIGH DEMAND", 0xFFD84315),
    TRENDING("TRENDING", 0xFFE65100),
    PRICING_OPPORTUNITY("PRICING OPPORTUNITY", 0xFF2E7D32),
    REGIONAL_DEMAND("REGIONAL DEMAND", 0xFF1565C0),
    SEASONAL_DEMAND("SEASONAL DEMAND", 0xFF6A1B9A)
}

/**
 * Market intelligence insight data model.
 * Important: isDemoData is true when backed by seeded intelligence engine.
 */
data class DemandInsight(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: InsightType,
    val category: CraftCategory,
    val headline: String,
    val detailedSummary: String,
    val recommendation: String,
    val growthPercentage: Int,
    val confidenceScore: Int,
    val targetRegion: String,
    val activeBuyerCount: Int,
    val avgExpectedPrice: Double,
    val seasonName: String? = null,
    val isDemoData: Boolean = true,
    val timestamp: String = "August 2026"
)

/**
 * Service interface for demand intelligence.
 * Abstracts local seeded datasets and external APIs.
 */
interface DemandIntelligenceService {
    val isRealtimeBackend: Boolean
    fun getMarketPulseStream(): Flow<List<DemandInsight>>
    fun getTrendingCrafts(): List<DemandInsight>
    fun getGrowingInsights(): List<DemandInsight>
    fun getPricingOpportunities(): List<DemandInsight>
    fun getRegionalDemands(): List<DemandInsight>
    fun getSeasonalDemands(): List<DemandInsight>
    fun getInsightById(id: String): DemandInsight?
    fun generateSaathiExplanation(insight: DemandInsight, artisanCraft: String): String
}

/**
 * Seeded implementation of DemandIntelligenceService for offline & prototype robustness.
 */
class SeededDemandIntelligenceService : DemandIntelligenceService {

    override val isRealtimeBackend: Boolean = false

    private val seedInsights = listOf(
        DemandInsight(
            id = "pulse_1",
            title = "Blue Handwoven Silk & Cotton Sarees",
            type = InsightType.HIGH_DEMAND,
            category = CraftCategory.TEXTILES,
            headline = "Blue handwoven sarees are receiving 34% more buyer attention this month.",
            detailedSummary = "B2B boutique buyers from Bengaluru, Mumbai, and New Delhi are actively seeking indigo and peacock-blue natural dye handloom drapes for the upcoming festive season.",
            recommendation = "Prioritize weaving royal blue and midnight indigo sarees with contrast golden zari borders. Recommended batch: 8–12 units.",
            growthPercentage = 34,
            confidenceScore = 94,
            targetRegion = "South & West India",
            activeBuyerCount = 28,
            avgExpectedPrice = 6800.0,
            seasonName = "Diwali & Wedding Pre-Season"
        ),
        DemandInsight(
            id = "pulse_2",
            title = "Festive Terracotta & Clay Diya Lamps",
            type = InsightType.TRENDING,
            category = CraftCategory.POTTERY,
            headline = "Festive home décor demand is rising rapidly across metro hubs.",
            detailedSummary = "B2B corporate gifting agencies and eco-conscious home stores are placing bulk RFQs for hand-painted terracotta diya sets and scented earthen pots.",
            recommendation = "Produce medium-sized (4-6 inch) decorative clay oil lamps in sets of 4 and 6. Package in reusable jute boxes for a 20% price premium.",
            growthPercentage = 42,
            confidenceScore = 91,
            targetRegion = "All India (Metros)",
            activeBuyerCount = 45,
            avgExpectedPrice = 450.0,
            seasonName = "Festive Autumn 2026"
        ),
        DemandInsight(
            id = "pulse_3",
            title = "Carved Sheesham & Teak Kitchenware",
            type = InsightType.PRICING_OPPORTUNITY,
            category = CraftCategory.WOODCRAFT,
            headline = "Premium buyers willing to pay +22% for food-safe organic beeswax finish.",
            detailedSummary = "High-end export buyers and domestic specialty stores are paying higher margins for certified organic finish wooden cutlery and cutting boards.",
            recommendation = "Upgrade surface finishing to natural beeswax or walnut oil and clearly state '100% Food Safe' on the Craft Passport tag.",
            growthPercentage = 22,
            confidenceScore = 88,
            targetRegion = "Export & North India",
            activeBuyerCount = 19,
            avgExpectedPrice = 1650.0
        ),
        DemandInsight(
            id = "pulse_4",
            title = "Dhokra Brass Tribal Figurines",
            type = InsightType.REGIONAL_DEMAND,
            category = CraftCategory.METALCRAFT,
            headline = "High demand in Hyderabad, Bengaluru & NCR for heritage office artifacts.",
            detailedSummary = "Architectural interior firms are sourcing authentic lost-wax brass bell metal statues and wall murals for modern office reception areas.",
            recommendation = "Focus on medium 8-12 inch dancing figures and elephant motifs with rustic patinas.",
            growthPercentage = 29,
            confidenceScore = 86,
            targetRegion = "Bengaluru, Hyderabad, Gurgaon",
            activeBuyerCount = 14,
            avgExpectedPrice = 3200.0
        ),
        DemandInsight(
            id = "pulse_5",
            title = "Bamboo Desktop Organizers & Planters",
            type = InsightType.SEASONAL_DEMAND,
            category = CraftCategory.LEATHER_BAMBOO,
            headline = "Eco-friendly corporate gift hampers up 65% for Q3 festival orders.",
            detailedSummary = "Large enterprises are replacing plastic stationery with hand-woven North-East cane and bamboo organizers.",
            recommendation = "Create standardized multi-compartment pen stands and planter holders with prompt 14-day delivery promises.",
            growthPercentage = 65,
            confidenceScore = 96,
            targetRegion = "National Corporate Sector",
            activeBuyerCount = 52,
            avgExpectedPrice = 850.0,
            seasonName = "Corporate Festive Season"
        ),
        DemandInsight(
            id = "pulse_6",
            title = "Handmade Madhubani Folk Art Trays",
            type = InsightType.PRICING_OPPORTUNITY,
            category = CraftCategory.FOLK_PAINTING,
            headline = "Your painting category has grown 18% this month.",
            detailedSummary = "Buyers are actively moving from framed canvases to functional folk art items like serving trays, coasters, and pen boxes.",
            recommendation = "Consider producing more medium-sized tea trays with waterproof lacquer coatings.",
            growthPercentage = 18,
            confidenceScore = 89,
            targetRegion = "Mumbai, Pune, Ahmedabad",
            activeBuyerCount = 21,
            avgExpectedPrice = 1450.0
        )
    )

    private val _stream = MutableStateFlow(seedInsights)

    override fun getMarketPulseStream(): Flow<List<DemandInsight>> = _stream.asStateFlow()

    override fun getTrendingCrafts(): List<DemandInsight> {
        return seedInsights.filter { it.type == InsightType.TRENDING || it.type == InsightType.HIGH_DEMAND }
    }

    override fun getGrowingInsights(): List<DemandInsight> {
        return seedInsights.filter { it.growthPercentage >= 25 }
    }

    override fun getPricingOpportunities(): List<DemandInsight> {
        return seedInsights.filter { it.type == InsightType.PRICING_OPPORTUNITY }
    }

    override fun getRegionalDemands(): List<DemandInsight> {
        return seedInsights.filter { it.type == InsightType.REGIONAL_DEMAND }
    }

    override fun getSeasonalDemands(): List<DemandInsight> {
        return seedInsights.filter { it.type == InsightType.SEASONAL_DEMAND }
    }

    override fun getInsightById(id: String): DemandInsight? {
        return seedInsights.find { it.id == id }
    }

    override fun generateSaathiExplanation(insight: DemandInsight, artisanCraft: String): String {
        return "नमस्ते / வணக்கம்! Based on our market intelligence, ${insight.headline}\n\n" +
                "Why this matters for your workshop:\n" +
                "• Active Buyers: ${insight.activeBuyerCount} verified B2B buyers looking for ${insight.category.label}.\n" +
                "• Average Expected Price: ₹${insight.avgExpectedPrice.toInt()} per piece.\n" +
                "• Growth Trend: +${insight.growthPercentage}% growth compared to last month.\n\n" +
                "Actionable Advice:\n${insight.recommendation}\n\n" +
                "Would you like me to help you calculate the exact raw material budget or create a new product listing for this?"
    }
}
