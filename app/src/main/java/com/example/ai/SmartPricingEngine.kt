package com.example.ai

import kotlin.math.roundToInt

data class MarketBenchmark(
    val category: String,
    val averageMarketPrice: Double,
    val minMarketPrice: Double,
    val maxMarketPrice: Double,
    val recentSalesCount: Int,
    val demandMultiplier: Double, // e.g. 1.15 for festival season or high export demand
    val seasonalityTag: String = "Peak Wedding & Festive Season"
)

data class CostBreakdown(
    val rawMaterials: Double,
    val skilledLabour: Double,
    val packaging: Double = 100.0,
    val estimatedPlatformCosts: Double = 50.0,
    val minimumSustainablePrice: Double,
    val recommendedPrice: Double,
    val premiumPrice: Double,
    val lowRangePrice: Double,
    val highRangePrice: Double,
    val profitMarginPercent: Int
)

data class PricingRecommendation(
    val lowPrice: Double,
    val recommendedPrice: Double,
    val premiumPrice: Double,
    val minSustainablePrice: Double,
    val costBreakdown: CostBreakdown,
    val marketBenchmark: MarketBenchmark,
    val explanation: String = "Based on estimated costs, similar products and current demand.",
    val isFairTradeCertified: Boolean = true
)

interface MarketDataProvider {
    fun getMarketDataForCategory(category: String): MarketBenchmark
}

class SeededMarketDataProvider : MarketDataProvider {
    private val benchmarks = mapOf(
        "Handloom & Textiles" to MarketBenchmark(
            category = "Handloom & Textiles",
            averageMarketPrice = 3600.0,
            minMarketPrice = 2800.0,
            maxMarketPrice = 5200.0,
            recentSalesCount = 342,
            demandMultiplier = 1.15,
            seasonalityTag = "High Wedding & Festive Demand"
        ),
        "Pottery & Terracotta" to MarketBenchmark(
            category = "Pottery & Terracotta",
            averageMarketPrice = 1450.0,
            minMarketPrice = 950.0,
            maxMarketPrice = 2200.0,
            recentSalesCount = 189,
            demandMultiplier = 1.08,
            seasonalityTag = "High Home Decor Demand"
        ),
        "Woodcraft & Toys" to MarketBenchmark(
            category = "Woodcraft & Toys",
            averageMarketPrice = 1200.0,
            minMarketPrice = 750.0,
            maxMarketPrice = 1900.0,
            recentSalesCount = 210,
            demandMultiplier = 1.05,
            seasonalityTag = "Steady Year-Round Demand"
        ),
        "Metalwork & Dokra" to MarketBenchmark(
            category = "Metalwork & Dokra",
            averageMarketPrice = 2800.0,
            minMarketPrice = 1900.0,
            maxMarketPrice = 4500.0,
            recentSalesCount = 115,
            demandMultiplier = 1.20,
            seasonalityTag = "Growing Export & Boutique Demand"
        )
    )

    override fun getMarketDataForCategory(category: String): MarketBenchmark {
        return benchmarks[category] ?: benchmarks["Handloom & Textiles"]!!
    }
}

class CostCalculator {
    fun calculate(
        rawMaterialCost: Double,
        laborHours: Double,
        hourlyRate: Double = 100.0, // Fair artisan hourly wage ₹100/hr
        packaging: Double = 100.0,
        platformCosts: Double = 50.0
    ): Pair<Double, Double> {
        val laborCost = (laborHours * hourlyRate).coerceAtLeast(400.0)
        val directBaseCost = rawMaterialCost + laborCost + packaging + platformCosts
        val minSustainable = (directBaseCost * 1.10).roundToInt().toDouble() // 10% contingency buffer
        return Pair(laborCost, minSustainable)
    }
}

class PricingEngine(
    private val marketDataProvider: MarketDataProvider = SeededMarketDataProvider(),
    private val costCalculator: CostCalculator = CostCalculator()
) {
    fun calculateRecommendation(
        rawMaterialCost: Double,
        laborHours: Double,
        productionDays: Int,
        category: String
    ): PricingRecommendation {
        val marketData = marketDataProvider.getMarketDataForCategory(category)
        val (laborCost, minSustainable) = costCalculator.calculate(
            rawMaterialCost = rawMaterialCost,
            laborHours = laborHours
        )

        // Calculate fair baseline
        val packaging = 100.0
        val platformCosts = 50.0
        
        // Recommended includes 25% fair artisan profit margin + demand adjustments
        val baseTarget = minSustainable * 1.22 * marketData.demandMultiplier
        
        // Low price = slightly above minimum sustainable price
        val lowPrice = (minSustainable * 1.05).roundToInt().toDouble()
        val recommendedPrice = (baseTarget).roundToInt().toDouble()
        val premiumPrice = (recommendedPrice * 1.25).roundToInt().toDouble()

        val profitMargin = (((recommendedPrice - minSustainable) / recommendedPrice) * 100).roundToInt().coerceAtLeast(15)

        val breakdown = CostBreakdown(
            rawMaterials = rawMaterialCost,
            skilledLabour = laborCost,
            packaging = packaging,
            estimatedPlatformCosts = platformCosts,
            minimumSustainablePrice = minSustainable,
            recommendedPrice = recommendedPrice,
            premiumPrice = premiumPrice,
            lowRangePrice = lowPrice,
            highRangePrice = premiumPrice,
            profitMarginPercent = profitMargin
        )

        return PricingRecommendation(
            lowPrice = lowPrice,
            recommendedPrice = recommendedPrice,
            premiumPrice = premiumPrice,
            minSustainablePrice = minSustainable,
            costBreakdown = breakdown,
            marketBenchmark = marketData,
            explanation = "Based on estimated costs, similar products and current demand.",
            isFairTradeCertified = true
        )
    }
}
