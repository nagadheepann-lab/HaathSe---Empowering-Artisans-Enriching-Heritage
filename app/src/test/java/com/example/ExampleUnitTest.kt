package com.example

import com.example.ai.*
import com.example.data.models.SupportedLanguage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CraftIntelligenceUnitTest {

    @Test
    fun testPricingEngineRecommendation() {
        val engine = PricingEngine()
        val recommendation = engine.calculateRecommendation(
            rawMaterialCost = 2100.0,
            laborHours = 40.0,
            productionDays = 5,
            category = "Handloom & Textiles"
        )

        assertTrue("Recommended price must be greater than raw material cost", recommendation.recommendedPrice > 2100.0)
        assertTrue("Recommended price must be greater than or equal to minimum sustainable price", recommendation.recommendedPrice >= recommendation.minSustainablePrice)
        assertTrue("Premium price must exceed recommended price", recommendation.premiumPrice > recommendation.recommendedPrice)
        assertEquals("Cost breakdown packaging must be standardized", 100.0, recommendation.costBreakdown.packaging, 0.01)
        assertTrue("Profit margin should be sustainable", recommendation.costBreakdown.profitMarginPercent >= 15)
    }

    @Test
    fun testCraftStoryExtractionZeroHallucination() = runBlocking {
        val storyService = GeminiCraftStoryService()
        val transcript = "I learned this pit-loom technique from my mother. It took 49 days to weave this pure mulberry silk saree."
        
        val result = storyService.generateStory(
            transcript = transcript,
            artisanName = "Lakshmi Ammal",
            region = "Kanchipuram",
            language = SupportedLanguage.ENGLISH
        )

        assertNotNull("Craft story result must not be null", result)
        assertTrue("Facts must extract generational or time data", result.extractedFacts.isNotEmpty())
        assertTrue("Highlight badge must celebrate tradition or time", result.highlightBadge.isNotBlank())
        assertFalse("Story should not be auto-approved without artisan consent", result.isApprovedByArtisan)
    }

    @Test
    fun testCraftAnalyzerSourceLabeling() = runBlocking {
        val analyzerService = GeminiCraftAnalyzerService()
        val transcript = "Pure Mulberry silk handwoven on pit loom."

        val analysis = analyzerService.analyzeCraft(
            imageIdentifier = "img_saree_sample",
            transcript = transcript,
            existingTitle = "Kanchipuram Saree",
            existingCategory = "Handloom & Textiles"
        )

        assertNotNull(analysis)
        assertTrue("Authenticity score should be high", analysis.authenticityScore >= 90)
        assertEquals(CraftFieldProvenanceType.ARTISAN_PROVIDED, analysis.probableMaterial.provenance)
        assertEquals(CraftFieldProvenanceType.AI_ESTIMATED, analysis.craftTechnique.provenance)
    }
}
