package com.example.data.service

import com.example.data.local.ReviewEntity
import com.example.data.models.TrustScoreDetails

data class TransparentTrustMetric(
    val title: String,
    val earnedPoints: Int,
    val maxPoints: Int,
    val percentageWeight: String,
    val description: String,
    val statusText: String,
    val isPositive: Boolean
)

data class TransparentTrustBreakdown(
    val overallScore: Int,
    val artisanId: String,
    val artisanName: String,
    val isKycVerified: Boolean,
    val metrics: List<TransparentTrustMetric>,
    val tipsToImprove: List<String>,
    val totalOrdersCompleted: Int,
    val totalReviewsCount: Int,
    val avgOverallRating: Float,
    val avgQualityRating: Float,
    val avgPackagingRating: Float,
    val avgDeliveryRating: Float,
    val avgAuthenticityRating: Float,
    val onTimeFulfillmentPercent: Int,
    val onTimeDeliveryPercent: Int,
    val cancellationRatePercent: Int
)

object TrustScoreEngine {

    /**
     * Calculates the transparent Trust Score based on:
     * 1. Verified KYC & GI Registration Status (Max 25 pts)
     * 2. Completed Order Volume & Track Record (Max 20 pts)
     * 3. Verified Buyer Ratings across 5 dimensions (Max 20 pts)
     * 4. Craft Fulfillment Rate (on-time production) (Max 15 pts)
     * 5. Delivery Performance & Safe Packaging (Max 10 pts)
     * 6. Low Order Cancellation Rate (Max 10 pts)
     *
     * Total = 100 points maximum.
     */
    fun calculate(
        artisanId: String,
        artisanName: String,
        isKycVerified: Boolean,
        completedOrders: Int,
        reviews: List<ReviewEntity>,
        fulfillmentRatePercent: Int = 98,
        onTimeDeliveryPercent: Int = 96,
        cancellationRatePercent: Int = 1
    ): TransparentTrustBreakdown {
        // 1. Verification (25 pts)
        val kycPoints = if (isKycVerified) 25 else 10

        // 2. Completed Orders (20 pts)
        // 0-10 orders: 8 pts, 11-50 orders: 14 pts, 51-100 orders: 18 pts, >100 orders: 20 pts
        val ordersPoints = when {
            completedOrders >= 100 -> 20
            completedOrders >= 50 -> 18
            completedOrders >= 20 -> 15
            completedOrders >= 5 -> 12
            else -> 8
        }

        // 3. Buyer Ratings & Dimensional Quality (20 pts)
        val avgOverall = if (reviews.isNotEmpty()) reviews.map { it.overallRating }.average().toFloat() else 4.9f
        val avgQuality = if (reviews.isNotEmpty()) reviews.map { it.productQualityRating }.average().toFloat() else 4.9f
        val avgPackaging = if (reviews.isNotEmpty()) reviews.map { it.packagingRating }.average().toFloat() else 4.8f
        val avgDelivery = if (reviews.isNotEmpty()) reviews.map { it.deliveryRating }.average().toFloat() else 4.8f
        val avgAuthenticity = if (reviews.isNotEmpty()) reviews.map { it.authenticityRating }.average().toFloat() else 5.0f

        val ratingAverage = (avgOverall * 0.4f + avgQuality * 0.2f + avgAuthenticity * 0.2f + avgPackaging * 0.1f + avgDelivery * 0.1f)
        val ratingPoints = ((ratingAverage / 5.0f) * 20f).toInt().coerceIn(0, 20)

        // 4. Fulfillment Rate (15 pts)
        val fulfillmentPoints = when {
            fulfillmentRatePercent >= 95 -> 15
            fulfillmentRatePercent >= 90 -> 13
            fulfillmentRatePercent >= 80 -> 10
            else -> 6
        }

        // 5. On-Time Delivery Performance (10 pts)
        val deliveryPoints = when {
            onTimeDeliveryPercent >= 95 -> 10
            onTimeDeliveryPercent >= 90 -> 8
            onTimeDeliveryPercent >= 80 -> 6
            else -> 4
        }

        // 6. Low Cancellation Score (10 pts)
        val cancellationPoints = when {
            cancellationRatePercent <= 1 -> 10
            cancellationRatePercent <= 3 -> 8
            cancellationRatePercent <= 7 -> 5
            else -> 2
        }

        val totalEarned = (kycPoints + ordersPoints + ratingPoints + fulfillmentPoints + deliveryPoints + cancellationPoints).coerceIn(0, 100)

        val metrics = listOf(
            TransparentTrustMetric(
                title = "Government KYC & GI Tag Verification",
                earnedPoints = kycPoints,
                maxPoints = 25,
                percentageWeight = "25%",
                description = "Artisan Pehchan Card verified with Geographical Indication (GI) handcraft lineage.",
                statusText = if (isKycVerified) "Verified Master Artisan" else "Pending Document Upload",
                isPositive = isKycVerified
            ),
            TransparentTrustMetric(
                title = "Fulfillment Track Record",
                earnedPoints = ordersPoints,
                maxPoints = 20,
                percentageWeight = "20%",
                description = "$completedOrders orders successfully produced and received by direct buyers.",
                statusText = "$completedOrders Completed Orders",
                isPositive = completedOrders >= 20
            ),
            TransparentTrustMetric(
                title = "Verified Buyer Ratings & Authenticity",
                earnedPoints = ratingPoints,
                maxPoints = 20,
                percentageWeight = "20%",
                description = "Average score across Overall (${String.format("%.1f", avgOverall)}★), Craft Quality (${String.format("%.1f", avgQuality)}★), and Pure Handcraft Authenticity (${String.format("%.1f", avgAuthenticity)}★).",
                statusText = "${String.format("%.1f", avgOverall)} ★ (${reviews.size} verified reviews)",
                isPositive = avgOverall >= 4.5f
            ),
            TransparentTrustMetric(
                title = "Production Schedule Adherence",
                earnedPoints = fulfillmentPoints,
                maxPoints = 15,
                percentageWeight = "15%",
                description = "Percentage of orders woven and finished within the promised timeline.",
                statusText = "$fulfillmentRatePercent% On-Time Fulfillment",
                isPositive = fulfillmentRatePercent >= 90
            ),
            TransparentTrustMetric(
                title = "Dispatch & Packaging Quality",
                earnedPoints = deliveryPoints,
                maxPoints = 10,
                percentageWeight = "10%",
                description = "Eco-safe transit packaging and fast courier pickup handover via India Post Speed Post.",
                statusText = "$onTimeDeliveryPercent% On-Time Dispatch",
                isPositive = onTimeDeliveryPercent >= 90
            ),
            TransparentTrustMetric(
                title = "Zero Cancellation Reliability",
                earnedPoints = cancellationPoints,
                maxPoints = 10,
                percentageWeight = "10%",
                description = "Low artisan cancellation rate ensures reliable order delivery for buyers.",
                statusText = "$cancellationRatePercent% Low Cancellation Rate",
                isPositive = cancellationRatePercent <= 3
            )
        )

        val tips = mutableListOf<String>()
        if (!isKycVerified) tips.add("Upload your Artisan Pehchan card to unlock 15 additional trust points.")
        if (reviews.size < 5) tips.add("Ask satisfied buyers to submit verified voice reviews after receiving deliveries.")
        if (fulfillmentRatePercent < 95) tips.add("Update your production milestones in the Artisan Dashboard to improve fulfillment rating.")
        if (tips.isEmpty()) tips.add("Maintain your excellent on-time dispatch and artisan craft quality to retain Elite Guild status.")

        return TransparentTrustBreakdown(
            overallScore = totalEarned,
            artisanId = artisanId,
            artisanName = artisanName,
            isKycVerified = isKycVerified,
            metrics = metrics,
            tipsToImprove = tips,
            totalOrdersCompleted = completedOrders,
            totalReviewsCount = reviews.size,
            avgOverallRating = avgOverall,
            avgQualityRating = avgQuality,
            avgPackagingRating = avgPackaging,
            avgDeliveryRating = avgDelivery,
            avgAuthenticityRating = avgAuthenticity,
            onTimeFulfillmentPercent = fulfillmentRatePercent,
            onTimeDeliveryPercent = onTimeDeliveryPercent,
            cancellationRatePercent = cancellationRatePercent
        )
    }

    fun toTrustScoreDetails(breakdown: TransparentTrustBreakdown): TrustScoreDetails {
        return TrustScoreDetails(
            overallScore = breakdown.overallScore,
            artisanName = breakdown.artisanName,
            isVerifiedArtisan = breakdown.isKycVerified,
            verifiedArtisanScore = breakdown.metrics.getOrNull(0)?.earnedPoints ?: 25,
            completedOrdersScore = breakdown.metrics.getOrNull(1)?.earnedPoints ?: 20,
            buyerRatingsScore = breakdown.metrics.getOrNull(2)?.earnedPoints ?: 20,
            fulfillmentRateScore = breakdown.metrics.getOrNull(3)?.earnedPoints ?: 15,
            deliveryPerformanceScore = breakdown.metrics.getOrNull(4)?.earnedPoints ?: 10,
            cancellationScore = breakdown.metrics.getOrNull(5)?.earnedPoints ?: 10,
            completedOrdersCount = breakdown.totalOrdersCompleted,
            fulfillmentRatePercent = breakdown.onTimeFulfillmentPercent,
            averageRating = breakdown.avgOverallRating,
            onTimeDeliveryPercent = breakdown.onTimeDeliveryPercent,
            cancellationRatePercent = breakdown.cancellationRatePercent
        )
    }
}
