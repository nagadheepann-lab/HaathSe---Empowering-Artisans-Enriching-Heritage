package com.example.ai

import com.example.data.models.ListingScoreReport
import com.example.data.models.PriceRecommendation
import com.example.data.models.SmartMatchResult
import com.example.data.models.SupportedLanguage

data class GeneratedProductCatalog(
    val title: String,
    val titleHindi: String,
    val titleRegional: String,
    val category: String,
    val craftTechnique: String,
    val region: String,
    val rawMaterialCost: Double,
    val laborHours: Double,
    val productionDays: Int,
    val suggestedPrice: Double,
    val fairMinPrice: Double,
    val premiumPrice: Double,
    val listingScore: Int,
    val materialsList: String,
    val dimensions: String,
    val weight: String,
    val description: String,
    val descriptionHindi: String,
    val descriptionRegional: String,
    val culturalStory: String,
    val storyLineage: String,
    val careInstructions: String,
    val packagingSuggestions: String,
    val searchKeywords: String,
    val pricingConfidence: Int,
    val pricingReasoning: String
)

object LocalAIIntelligenceEngine {

    fun generateCatalogFromInput(
        voiceOrText: String,
        artisanName: String,
        artisanRegion: String,
        rawMaterialInput: Double?,
        daysInput: Int?,
        userLang: SupportedLanguage
    ): GeneratedProductCatalog {
        val lower = voiceOrText.lowercase()
        val isSareeOrTextile = lower.contains("saree") || lower.contains("silk") || lower.contains("handloom") || lower.contains("weaving") || lower.contains("பட்டு") || lower.contains("साड़ी")
        val isPottery = lower.contains("pot") || lower.contains("vase") || lower.contains("clay") || lower.contains("blue pottery") || lower.contains("மண்பாண்டம்") || lower.contains("बर्तन")
        val isWoodcraft = lower.contains("wood") || lower.contains("toy") || lower.contains("lacquer") || lower.contains("channapatna") || lower.contains("பொம்மை") || lower.contains("खिलौना")
        val isMetalcraft = lower.contains("brass") || lower.contains("dhokra") || lower.contains("bell metal") || lower.contains("metal") || lower.contains("धातु")

        val category: String
        val craftTechnique: String
        val region: String
        val titleEn: String
        val titleHi: String
        val titleReg: String
        val rawMaterialCost: Double
        val laborHours: Double
        val productionDays: Int
        val materialsList: String
        val dimensions: String
        val weight: String
        val careInstructions: String
        val packagingSuggestions: String
        val searchKeywords: String
        val lineage: String

        if (isSareeOrTextile) {
            category = "Handloom & Silk Textiles"
            craftTechnique = "Korvai & Jacquard Pure Mulberry Silk Weaving"
            region = if (artisanRegion.isNotBlank()) artisanRegion else "Kanchipuram, Tamil Nadu"
            titleEn = "Handwoven Pure Silk Kanchipuram Saree (Peacock & Rudraksha Motifs)"
            titleHi = "पारंपरिक हस्तनिर्मित कांचीपुरम सिल्क साड़ी (मोर और ज़री बॉर्डर)"
            titleReg = when (userLang) {
                SupportedLanguage.TAMIL -> "கைத்தறி தூய காஞ்சிபுரம் பட்டு சேலை (மயில் ஜரிகை வேலைப்பாடு)"
                SupportedLanguage.TELUGU -> "సాంప్రదాయ చేనేత కాంచీపురం పట్టు చీర (జరీ డిజైన్)"
                SupportedLanguage.BENGALI -> "ঐতিহ্যবাহী হস্তনির্মিত কাঞ্চিপুরম রেশম শাড়ি"
                else -> "Handwoven Pure Silk Kanchipuram Saree"
            }
            rawMaterialCost = rawMaterialInput ?: 2100.0
            productionDays = daysInput ?: 5
            laborHours = productionDays * 7.5
            materialsList = "Pure Mulberry Silk Yarn, Gold-coated Silver Zari Threads, Natural Dye Extracts"
            dimensions = "5.5 meters length + 0.8 meter blouse piece (Width: 48 inches)"
            weight = "780 grams"
            careInstructions = "Dry clean only. Store in breathable muslin cotton cloth. Avoid direct perfume spray."
            packagingSuggestions = "Corrugated craft keepsake box with moisture-absorbent silica and authenticity heritage tag."
            searchKeywords = "kanchipuram saree, pure silk handloom, bridal silk, traditional gold zari, certified artisan weave, b2b bulk saree"
            lineage = "3rd Generation Weaver Family Heritage"
        } else if (isPottery) {
            category = "Clay & Blue Pottery"
            craftTechnique = "Quartz Glass Powder & Natural Cobalt Glazed Shaping"
            region = if (artisanRegion.isNotBlank()) artisanRegion else "Jaipur, Rajasthan"
            titleEn = "Handmade Jaipur Blue Pottery Floral Decorative Vase"
            titleHi = "जयपुर ब्लू पॉटरी हस्तनिर्मित सजावटी फूलदान"
            titleReg = when (userLang) {
                SupportedLanguage.TAMIL -> "ஜெய்ப்பூர் நீல மண்பாண்ட மலர் அலங்கார குவளை"
                SupportedLanguage.TELUGU -> "జైపూర్ బ్లూ పాటర్ చేతితో చేసిన అలంకార కుండీ"
                SupportedLanguage.BENGALI -> "জয়পুর ব্লু পটারি হস্তনির্মিত ফুলের ফুলদানি"
                else -> "Handmade Jaipur Blue Pottery Floral Vase"
            }
            rawMaterialCost = rawMaterialInput ?: 450.0
            productionDays = daysInput ?: 3
            laborHours = productionDays * 6.0
            materialsList = "Quartz Powder, Fuller's Earth, Natural Gum, Cobalt & Copper Oxide Glazes"
            dimensions = "12 inches Height x 6 inches Diameter"
            weight = "1.2 kg"
            careInstructions = "Wipe with a soft dry cloth. Fragile craft, protect from harsh impacts."
            packagingSuggestions = "Double-wall honeycomb foam cushioning with custom rigid B2B export cartons."
            searchKeywords = "jaipur blue pottery, ceramic handmade vase, floral cobalt craft, corporate gifting pottery, artisanal decor"
            lineage = "Master Potter Lineage of Sanganer"
        } else if (isWoodcraft) {
            category = "Carved Wood & Lacquerware"
            craftTechnique = "Ivory-wood Turning with Natural Lacquer Vegetable Dyeing"
            region = if (artisanRegion.isNotBlank()) artisanRegion else "Channapatna, Karnataka"
            titleEn = "Handcrafted Channapatna Wooden Stacking Toy & Decor Set"
            titleHi = "हस्तनिर्मित चन्नापटना लकड़ी का खिलौना एवं सजावट सेट"
            titleReg = when (userLang) {
                SupportedLanguage.TAMIL -> "சன்னபட்டணா மரத்தாலான பாரம்பரிய விளையாட்டு பொம்மை"
                SupportedLanguage.TELUGU -> "చెన్నపట్న చెక్క బొమ్మల అలంకరణ సెట్"
                SupportedLanguage.BENGALI -> "চন্নপত্তন হস্তনির্মিত কাঠের খেলনা সেট"
                else -> "Handcrafted Channapatna Wooden Toy Set"
            }
            rawMaterialCost = rawMaterialInput ?: 280.0
            productionDays = daysInput ?: 2
            laborHours = productionDays * 7.0
            materialsList = "Hale Wood (Wrightia Tinctoria), Natural Lac, Turmeric & Indigo Vegetable Dyes"
            dimensions = "8 inches x 4 inches x 4 inches"
            weight = "420 grams"
            careInstructions = "Non-toxic baby safe finish. Wipe with moist cloth. Avoid submerging in water."
            packagingSuggestions = "Eco-friendly recycled kraft box with transparent window and non-toxic certification stamp."
            searchKeywords = "channapatna toys, natural vegetable dye wood, montessori wooden toys, ethical wooden crafts, sustainable gifting"
            lineage = "GI-Tagged Karnataka Craft Guild"
        } else {
            category = "Dhokra & Brass Metalcraft"
            craftTechnique = "Lost-Wax (Cire Perdue) Bell Metal Casting"
            region = if (artisanRegion.isNotBlank()) artisanRegion else "Bastar, Chhattisgarh"
            titleEn = "Authentic Bastar Dhokra Brass Tribal Figurine & Lamp"
            titleHi = "बस्तर ढोकरा पारंपरिक पीतल जनजातीय मूर्ति"
            titleReg = when (userLang) {
                SupportedLanguage.TAMIL -> "பஸ்தார் டோக்ரா பித்தளை பாரம்பரிய கைவினை சிற்பம்"
                SupportedLanguage.TELUGU -> "బస్తర్ డోక్రా ఇత్తడి గిరిజన విగ్రహం"
                SupportedLanguage.BENGALI -> "বস্তার ঢোকরা ঐতিহ্যবাহী পিতলের মূর্তি"
                else -> "Authentic Bastar Dhokra Brass Figurine"
            }
            rawMaterialCost = rawMaterialInput ?: 620.0
            productionDays = daysInput ?: 4
            laborHours = productionDays * 6.5
            materialsList = "Recycled Brass & Bronze Alloy, Beeswax, River Bed Clay, Charcoal"
            dimensions = "10 inches Height x 4 inches Width"
            weight = "850 grams"
            careInstructions = "Clean with brass polish or lemon and tamarind paste for lustrous antique golden shine."
            packagingSuggestions = "Jute padded pouch encased in reinforced wooden crate for secure long-distance dispatch."
            searchKeywords = "dhokra art, lost wax casting, bastar tribal brass, antique brass lamp, indian handicraft b2b"
            lineage = "Centuries-old Tribal Artisan Collective"
        }

        val pricing = calculateFairPricing(rawMaterialCost, laborHours, category)

        val descEn = "Crafted with dedication by $artisanName in the historic clusters of $region. This masterpiece utilizes authentic $materialsList, molded through $craftTechnique over $productionDays days of meticulous handiwork. Every contour reflects ancestral heritage and uncompromised quality suitable for premium retail and corporate procurement."
        val descHi = "$region के प्रतिष्ठित शिल्प समूह में $artisanName द्वारा निर्मित। यह कृति $productionDays दिनों के समर्पित परिश्रम और $craftTechnique तकनीक से शुद्ध $materialsList के साथ तैयार की गई है।"
        val descReg = when (userLang) {
            SupportedLanguage.TAMIL -> "$region பகுதியில் வசிக்கும் $artisanName அவர்களால் $productionDays நாட்கள் அர்ப்பணிப்புடன் $craftTechnique முறையில் தூய $materialsList கொண்டு கையால் நெய்யப்பட்ட/உருவாக்கப்பட்ட பாரம்பரிய கலைப்படைப்பு."
            SupportedLanguage.TELUGU -> "$region ప్రాంతంలో $artisanName ద్వారా $productionDays రోజుల శ్రమతో $craftTechnique పద్ధతిలో సృష్టించబడిన ప్రత్యేక హస్తకళ."
            SupportedLanguage.BENGALI -> "$region অঞ্চলের কারিগর $artisanName দ্বারা $productionDays দিনের নিপুণ পরিশ্রমে ঐতিহ্যবাহী পদ্ধতিতে তৈরি অনন্য হস্তশিল্প।"
            else -> descEn
        }

        val culturalStory = "In the heritage lanes of $region, craft traditions have thrived for centuries as a testament to indigenous self-reliance. This work is preserved by $artisanName ($lineage). Every pattern carries deep philosophical symbolism, embodying nature, prosperity, and cultural resilience. Purchasing this authentic creation directly empowers marginalized craft families and secures the living traditions of India."

        val listingScoreReport = evaluateListingScore(
            hasPhoto = true,
            hasDesc = true,
            hasDimensions = true,
            hasCare = true,
            hasMaterials = true,
            hasKeywords = true,
            pricingConfidence = pricing.confidencePercentage
        )

        return GeneratedProductCatalog(
            title = titleEn,
            titleHindi = titleHi,
            titleRegional = titleReg,
            category = category,
            craftTechnique = craftTechnique,
            region = region,
            rawMaterialCost = rawMaterialCost,
            laborHours = laborHours,
            productionDays = productionDays,
            suggestedPrice = pricing.suggestedPrice,
            fairMinPrice = pricing.fairMinPrice,
            premiumPrice = pricing.premiumPrice,
            listingScore = listingScoreReport.totalScore,
            materialsList = materialsList,
            dimensions = dimensions,
            weight = weight,
            description = descEn,
            descriptionHindi = descHi,
            descriptionRegional = descReg,
            culturalStory = culturalStory,
            storyLineage = lineage,
            careInstructions = careInstructions,
            packagingSuggestions = packagingSuggestions,
            searchKeywords = searchKeywords,
            pricingConfidence = pricing.confidencePercentage,
            pricingReasoning = pricing.reasoning
        )
    }

    fun calculateFairPricing(
        rawMaterialCost: Double,
        laborHours: Double,
        category: String
    ): PriceRecommendation {
        // Fair hourly rate ensuring dignified livelihood (higher than minimum wage for skilled craftsmanship)
        val hourlyWage = 110.0 // ₹110 per hour for skilled master artisan
        val laborCost = laborHours * hourlyWage
        val packagingAndOverhead = (rawMaterialCost * 0.08) + 90.0

        // Minimum Sustainable Price (artisan should never sell below this to avoid exploitation)
        val fairMinPrice = Math.round(rawMaterialCost + laborCost + packagingAndOverhead).toDouble()
        // Suggested fair market price (with healthy 35% margin for business growth & savings)
        val suggestedPrice = Math.round(fairMinPrice * 1.35).toDouble()
        // Premium tier for boutique / luxury / export markets
        val premiumPrice = Math.round(suggestedPrice * 1.25).toDouble()
        val estimatedMargin = suggestedPrice - (rawMaterialCost + packagingAndOverhead)

        val reasoning = "Raw material ₹${rawMaterialCost.toInt()} + Artisan skilled labor ($laborHours hrs @ ₹$hourlyWage/hr = ₹${laborCost.toInt()}) + Packaging ₹${packagingAndOverhead.toInt()}. Minimum sustainable baseline is ₹${fairMinPrice.toInt()} to protect your profit margin."

        return PriceRecommendation(
            suggestedPrice = suggestedPrice,
            fairMinPrice = fairMinPrice,
            premiumPrice = premiumPrice,
            rawMaterialCost = rawMaterialCost,
            estimatedLaborCost = laborCost,
            estimatedMargin = estimatedMargin,
            confidencePercentage = 94,
            reasoning = reasoning
        )
    }

    fun evaluateListingScore(
        hasPhoto: Boolean,
        hasDesc: Boolean,
        hasDimensions: Boolean,
        hasCare: Boolean,
        hasMaterials: Boolean,
        hasKeywords: Boolean,
        pricingConfidence: Int
    ): ListingScoreReport {
        var score = 0
        val tips = mutableListOf<String>()

        if (hasPhoto) score += 25 else tips.add("Add a bright, centered photo (+25 pts)")
        if (hasDesc) score += 20 else tips.add("Add a detailed multilingual description (+20 pts)")
        if (hasMaterials) score += 15 else tips.add("List authentic raw materials (+15 pts)")
        if (hasDimensions) score += 10 else tips.add("Specify dimensions and weight for B2B buyers (+10 pts)")
        if (hasCare) score += 10 else tips.add("Include care & storage instructions (+10 pts)")
        if (hasKeywords) score += 10 else tips.add("Include searchable B2B keywords (+10 pts)")
        if (pricingConfidence >= 80) score += 10 else tips.add("Verify raw material breakdown for fair pricing (+10 pts)")

        return ListingScoreReport(
            totalScore = score.coerceIn(0, 100),
            photoScore = if (hasPhoto) 25 else 0,
            descriptionScore = if (hasDesc) 20 else 0,
            pricingScore = if (pricingConfidence >= 80) 20 else 10,
            dimensionsProvided = hasDimensions,
            careInstructionsProvided = hasCare,
            culturalStoryProvided = true,
            improvementTips = tips
        )
    }

    fun evaluateSmartMatch(
        buyerQuantity: Int,
        buyerTargetPrice: Double,
        artisanCapacity: Int,
        artisanMinPrice: Double,
        categoryMatch: Boolean
    ): SmartMatchResult {
        val reasons = mutableListOf<String>()
        var score = 50

        val fitsCapacity = artisanCapacity >= buyerQuantity
        if (fitsCapacity) {
            score += 25
            reasons.add("Artisan monthly capacity ($artisanCapacity units) can comfortably fulfill $buyerQuantity units.")
        } else {
            reasons.add("Artisan capacity ($artisanCapacity units) may require a partial batch delivery schedule.")
        }

        val fitsBudget = buyerTargetPrice >= (artisanMinPrice * 0.9)
        if (fitsBudget) {
            score += 20
            reasons.add("Buyer budget (₹${buyerTargetPrice.toInt()}/unit) meets the artisan's minimum sustainable threshold.")
        } else {
            reasons.add("Buyer target price (₹${buyerTargetPrice.toInt()}) is below artisan sustainable minimum (₹${artisanMinPrice.toInt()}). Counter-offer advised.")
        }

        if (categoryMatch) {
            score += 15
            reasons.add("Verified specialty in requested craft category with historical 5-star ratings.")
        }

        return SmartMatchResult(
            matchPercentage = score.coerceIn(10, 98),
            reasons = reasons,
            canDeliverOnTime = true,
            fitsBudget = fitsBudget,
            fitsCapacity = fitsCapacity
        )
    }

    fun answerBusinessCoachQuestion(
        question: String,
        artisanName: String,
        activeProductsCount: Int,
        pendingOrdersCount: Int,
        userLang: SupportedLanguage
    ): String {
        val q = question.lowercase()
        return when {
            q.contains("price") || q.contains("sell for") || q.contains("कीमत") || q.contains("விலை") || q.contains("ధర") -> {
                "Always calculate: Raw Material Cost + Labor (hours × ₹110) + Packaging. Never sell below your 'Minimum Sustainable Price'. For corporate bulk orders above 100 units, offer a 10-15% discount only if your raw material costs reduce with bulk buying."
            }
            q.contains("more") || q.contains("demand") || q.contains("बनाएं") || q.contains("உற்பத்தி") || q.contains("డిమాండ్") -> {
                "Currently, B2B corporate buyers are searching heavily for Handloom Silk Stoles and Eco-friendly Handcrafted Desk Decor sets. Your craftsmanship score is highest in traditional weaving—consider preparing 15-20 ready units for festive exhibitions!"
            }
            q.contains("order") || q.contains("not getting") || q.contains("बिक्री") || q.contains("ஆர்டர்") || q.contains("ఆర్డర్లు") -> {
                "Tips to boost buyer inquiries: 1) Enhance your photo with our 'Clean White Studio' preset. 2) Ensure product dimensions and care instructions are filled to reach 90+ Listing Score. 3) Keep your Minimum Sustainable Price transparent."
            }
            q.contains("photo") || q.contains("camera") || q.contains("तस्वीर") || q.contains("புகைப்படம்") -> {
                "Use the 'AI Product Studio' tab! Place your craft on a clean flat surface with soft morning daylight. Our AI will automatically adjust the lighting, center the craft, and replace background noise with a pristine studio setting."
            }
            q.contains("reply") || q.contains("buyer message") || q.contains("संदेश") || q.contains("செய்தி") -> {
                "Suggested response to buyer: 'Dear Buyer, thank you for appreciating our authentic handmade craft. We can deliver your required quantity with custom GI-tagged quality certification within 14 days. Looking forward to our fruitful partnership!'"
            }
            else -> {
                "Hello $artisanName! You currently have $activeProductsCount live products and $pendingOrdersCount active buyer inquiries. KarigarSetu AI is actively linking your catalog with verified ethical B2B retailers across India. Feel free to ask about pricing, raw materials, or buyer negotiations!"
            }
        }
    }
}
