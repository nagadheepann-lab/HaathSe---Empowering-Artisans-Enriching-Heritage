package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.AppNotificationEntity
import com.example.data.local.ArtisanEntity
import com.example.data.local.ArtisanNotificationEntity
import com.example.data.local.BuyerRequestEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.CraftEventEntity
import com.example.data.local.MaterialEntity
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.data.local.ReviewEntity
import com.example.data.models.*
import com.example.data.service.FcmNotificationService
import com.example.data.service.TransparentTrustBreakdown
import com.example.data.service.TrustScoreEngine
import com.example.ui.viewmodels.CartViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KarigarRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val productDao = db.productDao()
    private val artisanDao = db.artisanDao()
    private val materialDao = db.materialDao()
    private val buyerRequestDao = db.buyerRequestDao()
    private val chatDao = db.chatDao()
    val orderDao = db.orderDao()
    val notificationDao = db.artisanNotificationDao()
    val craftCircleDao = db.craftCircleDao()
    val circleMemberDao = db.circleMemberDao()
    val circleJoinRequestDao = db.circleJoinRequestDao()
    val bulkOrderDao = db.bulkOrderDao()
    val bulkAllocationDao = db.bulkAllocationDao()
    val craftEventDao = db.craftEventDao()
    val appNotificationDao = db.appNotificationDao()
    val reviewDao = db.reviewDao()
    val productDraftDao = db.productDraftDao()
    val offlineSyncQueueDao = db.offlineSyncQueueDao()
    val offlineVoiceRecordingDao = db.offlineVoiceRecordingDao()
    val appSettingsCacheDao = db.appSettingsCacheDao()

    val offlineSyncManager = com.example.data.service.OfflineSyncManager(context, db)

    val fcmService = FcmNotificationService(appNotificationDao)

    val cartRepository = CartRepository(productDao, artisanDao, orderDao, notificationDao)
    val cartViewModel = CartViewModel(cartRepository)

    val craftCircleRepository = CraftCircleRepository(
        circleDao = craftCircleDao,
        memberDao = circleMemberDao,
        joinRequestDao = circleJoinRequestDao,
        bulkOrderDao = bulkOrderDao,
        bulkAllocationDao = bulkAllocationDao,
        notificationDao = notificationDao
    )
    val craftCircleViewModel = com.example.ui.viewmodels.CraftCircleViewModel(craftCircleRepository)

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val publishedProducts: Flow<List<ProductEntity>> = productDao.getPublishedProducts()
    val allArtisans: Flow<List<ArtisanEntity>> = artisanDao.getAllArtisans()
    val allMaterials: Flow<List<MaterialEntity>> = materialDao.getAllMaterials()
    val allBuyerRequests: Flow<List<BuyerRequestEntity>> = buyerRequestDao.getAllRequests()
    val allChatMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val buyerOrders: Flow<List<OrderEntity>> = orderDao.getOrdersByBuyer("buyer_default")

    val allCraftEvents: Flow<List<CraftEventEntity>> = craftEventDao.getAllEvents()
    val allReviews: Flow<List<ReviewEntity>> = reviewDao.getAllReviews()
    val artisanNotifications: Flow<List<AppNotificationEntity>> = appNotificationDao.getNotificationsForRole("ARTISAN")
    val buyerNotifications: Flow<List<AppNotificationEntity>> = appNotificationDao.getNotificationsForRole("BUYER")
    val unreadArtisanCount: Flow<Int> = appNotificationDao.getUnreadCountForRole("ARTISAN")
    val unreadBuyerCount: Flow<Int> = appNotificationDao.getUnreadCountForRole("BUYER")

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    fun setCurrentUser(user: AuthUser?) {
        _currentUser.value = user
    }


    // Buyer In-Memory Reactive States

    private val _buyerWishlist = MutableStateFlow<Set<String>>(setOf("prod_saree_01", "prod_dhokra_01"))
    val buyerWishlist = _buyerWishlist.asStateFlow()

    private val _buyerCart = MutableStateFlow<List<BuyerCartItem>>(emptyList())
    val buyerCart = _buyerCart.asStateFlow()

    private val _buyerFilter = MutableStateFlow(BuyerFilterState())
    val buyerFilter = _buyerFilter.asStateFlow()

    fun toggleWishlist(productId: String) {
        val current = _buyerWishlist.value.toMutableSet()
        if (current.contains(productId)) {
            current.remove(productId)
        } else {
            current.add(productId)
        }
        _buyerWishlist.value = current
    }

    fun isWishlisted(productId: String): Boolean {
        return _buyerWishlist.value.contains(productId)
    }

    fun addToCart(product: ProductEntity, quantity: Int = 1) {
        val current = _buyerCart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = current[index].quantity + quantity)
        } else {
            current.add(BuyerCartItem(product, quantity))
        }
        _buyerCart.value = current
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        val current = _buyerCart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            if (quantity <= 0) {
                current.removeAt(index)
            } else {
                current[index] = current[index].copy(quantity = quantity)
            }
            _buyerCart.value = current
        }
    }

    fun removeFromCart(productId: String) {
        val current = _buyerCart.value.toMutableList()
        current.removeAll { it.product.id == productId }
        _buyerCart.value = current
    }

    fun clearCart() {
        _buyerCart.value = emptyList()
    }

    fun updateFilter(newFilter: BuyerFilterState) {
        _buyerFilter.value = newFilter
    }

    fun resetFilter() {
        _buyerFilter.value = BuyerFilterState()
    }

    fun getTrustScoreForArtisan(artisanName: String): TrustScoreDetails {
        return when {
            artisanName.contains("Lakshmi", ignoreCase = true) -> TrustScoreDetails(
                overallScore = 96,
                artisanName = "Lakshmi Ammal",
                isVerifiedArtisan = true,
                verifiedArtisanScore = 25,
                completedOrdersScore = 20,
                buyerRatingsScore = 20,
                fulfillmentRateScore = 15,
                deliveryPerformanceScore = 8,
                cancellationScore = 8,
                completedOrdersCount = 142,
                fulfillmentRatePercent = 99,
                averageRating = 4.9f,
                onTimeDeliveryPercent = 97,
                cancellationRatePercent = 1
            )
            artisanName.contains("Ramesh", ignoreCase = true) -> TrustScoreDetails(
                overallScore = 92,
                artisanName = "Ramesh Prajapati",
                isVerifiedArtisan = true,
                verifiedArtisanScore = 25,
                completedOrdersScore = 19,
                buyerRatingsScore = 19,
                fulfillmentRateScore = 14,
                deliveryPerformanceScore = 8,
                cancellationScore = 7,
                completedOrdersCount = 96,
                fulfillmentRatePercent = 97,
                averageRating = 4.8f,
                onTimeDeliveryPercent = 94,
                cancellationRatePercent = 2
            )
            artisanName.contains("Mangal", ignoreCase = true) -> TrustScoreDetails(
                overallScore = 95,
                artisanName = "Mangal Dhurwa",
                isVerifiedArtisan = true,
                verifiedArtisanScore = 25,
                completedOrdersScore = 20,
                buyerRatingsScore = 19,
                fulfillmentRateScore = 14,
                deliveryPerformanceScore = 9,
                cancellationScore = 8,
                completedOrdersCount = 118,
                fulfillmentRatePercent = 98,
                averageRating = 4.9f,
                onTimeDeliveryPercent = 95,
                cancellationRatePercent = 1
            )
            artisanName.contains("Gururaj", ignoreCase = true) -> TrustScoreDetails(
                overallScore = 94,
                artisanName = "Gururaj Shrestha",
                isVerifiedArtisan = true,
                verifiedArtisanScore = 25,
                completedOrdersScore = 19,
                buyerRatingsScore = 20,
                fulfillmentRateScore = 14,
                deliveryPerformanceScore = 8,
                cancellationScore = 8,
                completedOrdersCount = 84,
                fulfillmentRatePercent = 98,
                averageRating = 4.9f,
                onTimeDeliveryPercent = 96,
                cancellationRatePercent = 1
            )
            else -> TrustScoreDetails(
                overallScore = 94,
                artisanName = artisanName,
                isVerifiedArtisan = true,
                verifiedArtisanScore = 25,
                completedOrdersScore = 19,
                buyerRatingsScore = 19,
                fulfillmentRateScore = 14,
                deliveryPerformanceScore = 9,
                cancellationScore = 8,
                completedOrdersCount = 104,
                fulfillmentRatePercent = 98,
                averageRating = 4.8f,
                onTimeDeliveryPercent = 96,
                cancellationRatePercent = 1
            )
        }
    }

    fun getProductReviews(productId: String): List<ProductReview> {
        return listOf(
            ProductReview(
                id = "rev_1",
                reviewerName = "Radhika Iyer",
                rating = 5.0f,
                date = "2 days ago",
                comment = "Breathtaking craftsmanship! The weight of the silk and the zari work are genuine. You can clearly feel the heritage pit-loom texture. Shipped in safe handmade packaging.",
                verifiedPurchase = true,
                location = "Bengaluru, Karnataka"
            ),
            ProductReview(
                id = "rev_2",
                reviewerName = "Anand Vardhan",
                rating = 5.0f,
                date = "1 week ago",
                comment = "Ordered 12 pieces for family wedding gifting. Every piece is unique and came with the verified GI passport tag. Directly supporting our master artisans feels so rewarding.",
                verifiedPurchase = true,
                location = "Mumbai, Maharashtra"
            ),
            ProductReview(
                id = "rev_3",
                reviewerName = "Dr. Meenakshi Sundaram",
                rating = 4.8f,
                date = "3 weeks ago",
                comment = "Flawless finish and vivid natural dyes. Exactly as described by Lakshmi Ammal. Very impressed with the packaging and quick delivery.",
                verifiedPurchase = true,
                location = "Chennai, Tamil Nadu"
            )
        )
    }

    val marketplaceCategories = listOf(
        BuyerMarketplaceCategory(
            id = "cat_textiles",
            title = "Textiles",
            subtitle = "Kanchipuram, Chanderi & Banarasi",
            imageRes = "img_saree_sample",
            countLabel = "420+ Crafts"
        ),
        BuyerMarketplaceCategory(
            id = "cat_pottery",
            title = "Pottery",
            subtitle = "Jaipur Blue & Terracotta Clay",
            imageRes = "img_pottery_sample",
            countLabel = "280+ Crafts"
        ),
        BuyerMarketplaceCategory(
            id = "cat_woodcraft",
            title = "Woodcraft",
            subtitle = "Channapatna Lacquer & Teak",
            imageRes = "img_wood_craft",
            countLabel = "195+ Crafts"
        ),
        BuyerMarketplaceCategory(
            id = "cat_jewellery",
            title = "Jewellery",
            subtitle = "Cuttack Filigree & Kundan",
            imageRes = "img_saree_sample",
            countLabel = "310+ Crafts"
        ),
        BuyerMarketplaceCategory(
            id = "cat_basketry",
            title = "Basketry",
            subtitle = "Assam Bamboo & Sabai Grass",
            imageRes = "img_pottery_sample",
            countLabel = "140+ Crafts"
        ),
        BuyerMarketplaceCategory(
            id = "cat_paintings",
            title = "Paintings",
            subtitle = "Madhubani, Pattachitra & Warli",
            imageRes = "img_artisan_hero",
            countLabel = "260+ Crafts"
        ),
        BuyerMarketplaceCategory(
            id = "cat_homedecor",
            title = "Home Decor",
            subtitle = "Dhokra Bell Metal & Brass Lamps",
            imageRes = "img_brass_dhokra",
            countLabel = "350+ Crafts"
        ),
        BuyerMarketplaceCategory(
            id = "cat_handloom",
            title = "Handloom",
            subtitle = "Pashmina, Ikat & Khadi Weaves",
            imageRes = "img_saree_sample",
            countLabel = "510+ Crafts"
        ),
        BuyerMarketplaceCategory(
            id = "cat_regional",
            title = "Regional Crafts",
            subtitle = "Bidriware, Tanjore & Dokra",
            imageRes = "img_brass_dhokra",
            countLabel = "230+ Crafts"
        )
    )

    val heroCollections = listOf(
        HeroCollection(
            id = "hero_1",
            tag = "FESTIVE SPOTLIGHT",
            title = "Timeless Royal Kanchipuram Weaves",
            subtitle = "Directly from 3rd-generation weaver looms in Tamil Nadu",
            imageRes = "img_saree_sample",
            actionText = "Explore Collection",
            categoryTarget = "Textiles"
        ),
        HeroCollection(
            id = "hero_2",
            tag = "TRIBAL HERITAGE",
            title = "4,000-Year-Old Bastar Dhokra Art",
            subtitle = "Lost-wax brass castings by master tribal guilds",
            imageRes = "img_brass_dhokra",
            actionText = "Discover Dhokra",
            categoryTarget = "Home Decor"
        ),
        HeroCollection(
            id = "hero_3",
            tag = "GI TAGGED TOY CRAFT",
            title = "Natural Non-Toxic Channapatna Toys",
            subtitle = "Organic vegetable-lacquered turned wood from Karnataka",
            imageRes = "img_wood_craft",
            actionText = "View Lacquerware",
            categoryTarget = "Woodcraft"
        ),
        HeroCollection(
            id = "hero_4",
            tag = "PALACE CRAFTS",
            title = "Cobalt Glazed Jaipur Blue Pottery",
            subtitle = "Handcrafted without clay using crushed quartz stone",
            imageRes = "img_pottery_sample",
            actionText = "Shop Blue Pottery",
            categoryTarget = "Pottery"
        )
    )

    val demandIntelligenceService: com.example.data.service.DemandIntelligenceService = 
        com.example.data.service.SeededDemandIntelligenceService()

    val craftCircles = listOf(
        com.example.data.models.CraftCircle(
            id = "circle_kpm_weavers",
            name = "Kanchipuram Master Weavers Guild",
            craftType = "Handloom Silk & Zari Weaving",
            location = "Kanchipuram, Tamil Nadu",
            memberCount = 18,
            activeCollectiveOrders = 7,
            availableMonthlyCapacity = "450 meters / month",
            leaderName = "Lakshmi Ammal (Coordinator)"
        ),
        com.example.data.models.CraftCircle(
            id = "circle_jaipur_clay",
            name = "Jaipur Blue Glaze Artisans Co-op",
            craftType = "Quartz & Blue Glaze Pottery",
            location = "Jaipur, Rajasthan",
            memberCount = 24,
            activeCollectiveOrders = 12,
            availableMonthlyCapacity = "1,200 pottery units",
            leaderName = "Rameshwar Prajapati"
        ),
        com.example.data.models.CraftCircle(
            id = "circle_bastar_metal",
            name = "Bastar Lost-Wax Guild",
            craftType = "Dhokra Bell Metal Castings",
            location = "Bastar, Chhattisgarh",
            memberCount = 14,
            activeCollectiveOrders = 5,
            availableMonthlyCapacity = "350 statues / month",
            leaderName = "Mangal Dhurwa"
        )
    )

    val upcomingCraftEvents = listOf(
        com.example.data.models.CraftEvent(
            id = "evt_handloom_expo_2026",
            title = "National Handloom Expo 2026",
            dateRange = "Oct 12 – 20, 2026",
            location = "Chennai Trade Centre, Nandambakkam",
            organizer = "Development Commissioner for Handlooms, Govt of India",
            stallRentSubsidy = "100% Free Stall for GI Tagged Artisans",
            isGovtSponsored = true,
            registrationDeadline = "Sept 25, 2026"
        ),
        com.example.data.models.CraftEvent(
            id = "evt_surajkund_2026",
            title = "Surajkund International Crafts Mela",
            dateRange = "Nov 02 – 18, 2026",
            location = "Surajkund Mela Grounds, Faridabad / NCR",
            organizer = "Haryana Tourism & Ministry of Textiles",
            stallRentSubsidy = "TA/DA + 80% Subsidy on Travel & Logistics",
            isGovtSponsored = true,
            registrationDeadline = "Oct 05, 2026"
        ),
        com.example.data.models.CraftEvent(
            id = "evt_dastkar_bazaar",
            title = "Dastkar Nature Bazaar Autumn Festival",
            dateRange = "Oct 28 – Nov 04, 2026",
            location = "Kisan Haat, Andheria Modh, New Delhi",
            organizer = "Dastkar Crafts Society",
            stallRentSubsidy = "Direct B2B Boutique Buyer Meets Included",
            isGovtSponsored = false,
            registrationDeadline = "Oct 10, 2026"
        )
    )

    val sampleInventoryAlerts = listOf(
        com.example.data.models.InventoryAlert(
            id = "alert_1",
            productName = "Blue Handwoven Silk & Cotton Saree",
            alertLevel = com.example.data.models.StockAlertLevel.LOW_STOCK,
            message = "Your blue cotton saree has only 2 pieces left in stock.",
            currentStock = 2,
            suggestedRestock = 8
        ),
        com.example.data.models.InventoryAlert(
            id = "alert_2",
            productName = "Festive Terracotta Diya Lamps",
            alertLevel = com.example.data.models.StockAlertLevel.HIGH_DEMAND,
            message = "Your terracotta lamps are selling quickly across metro buyers.",
            currentStock = 6,
            suggestedRestock = 25
        ),
        com.example.data.models.InventoryAlert(
            id = "alert_3",
            productName = "Natural Temple Border Silk Stole",
            alertLevel = com.example.data.models.StockAlertLevel.OUT_OF_STOCK,
            message = "Temple border stoles are sold out! 3 B2B buyers have requested restock.",
            currentStock = 0,
            suggestedRestock = 15
        )
    )

    fun getRevenueData(filter: String): List<com.example.data.models.RevenueDataPoint> {
        return when (filter) {
            "7D" -> listOf(
                com.example.data.models.RevenueDataPoint("Mon", 4200f, 2),
                com.example.data.models.RevenueDataPoint("Tue", 6800f, 3),
                com.example.data.models.RevenueDataPoint("Wed", 3500f, 1),
                com.example.data.models.RevenueDataPoint("Thu", 9200f, 4),
                com.example.data.models.RevenueDataPoint("Fri", 14500f, 5),
                com.example.data.models.RevenueDataPoint("Sat", 18200f, 6),
                com.example.data.models.RevenueDataPoint("Sun", 12400f, 4)
            )
            "30D" -> listOf(
                com.example.data.models.RevenueDataPoint("W1", 24500f, 8),
                com.example.data.models.RevenueDataPoint("W2", 38900f, 14),
                com.example.data.models.RevenueDataPoint("W3", 31200f, 11),
                com.example.data.models.RevenueDataPoint("W4", 48200f, 15)
            )
            "3M" -> listOf(
                com.example.data.models.RevenueDataPoint("Jun", 98000f, 32),
                com.example.data.models.RevenueDataPoint("Jul", 118500f, 41),
                com.example.data.models.RevenueDataPoint("Aug", 142800f, 48)
            )
            "1Y" -> listOf(
                com.example.data.models.RevenueDataPoint("Q1", 240000f, 75),
                com.example.data.models.RevenueDataPoint("Q2", 295000f, 92),
                com.example.data.models.RevenueDataPoint("Q3", 360000f, 110),
                com.example.data.models.RevenueDataPoint("Q4", 489000f, 148)
            )
            else -> listOf(
                com.example.data.models.RevenueDataPoint("W1", 24500f, 8),
                com.example.data.models.RevenueDataPoint("W2", 38900f, 14),
                com.example.data.models.RevenueDataPoint("W3", 31200f, 11),
                com.example.data.models.RevenueDataPoint("W4", 48200f, 15)
            )
        }
    }


    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfEmpty()
        }
    }

    suspend fun insertProduct(product: ProductEntity) {
        productDao.insertProduct(product)
    }

    suspend fun updateStock(productId: String, newStock: Int) {
        productDao.updateStock(productId, newStock)
    }

    suspend fun insertMaterial(material: MaterialEntity) {
        materialDao.insertMaterial(material)
    }

    suspend fun deleteMaterial(id: String) {
        materialDao.deleteMaterial(id)
    }

    suspend fun updateBuyerRequestStatus(id: String, status: String, counterPrice: Double = 0.0) {
        buyerRequestDao.updateStatus(id, status, counterPrice)
    }

    suspend fun sendChatMessage(message: ChatMessageEntity) {
        chatDao.insertMessage(message)
    }

    val govtSchemes = listOf(
        GovtScheme(
            id = "scheme_vishwakarma",
            name = "PM Vishwakarma Yojana",
            ministry = "Ministry of MSME & Social Justice",
            category = "Financial Assistance & Skill Training",
            financialSupport = "Collateral-free credit up to ₹3,00,000 @ 5% concessional interest rate + ₹15,000 modern toolkit incentive.",
            eligibility = "Artisans and craftspeople working with hands and tools in 18 traditional trades (Weavers, Potters, Sculptors, Toymakers, etc.)",
            benefits = "Skill upgradation, digital transaction incentives (₹1 per transaction up to 100/mo), national branding & onboarding onto GeM portal.",
            applicationProcess = "Free biometric-based registration at any Common Services Centre (CSC) with Aadhaar and bank account.",
            officialPortal = "pmvishwakarma.gov.in"
        ),
        GovtScheme(
            id = "scheme_ahvy",
            name = "Ambedkar Hastshilp Vikas Yojana (AHVY)",
            ministry = "Ministry of Textiles & Social Justice",
            category = "Cluster Development & Market Linkage",
            financialSupport = "100% financial grant for artisan Self-Help Groups (SHGs) and cluster infrastructure.",
            eligibility = "Marginalized Scheduled Caste/Tribe and traditional craft community clusters with at least 20 artisan families.",
            benefits = "Direct buyer-seller meets, national exhibition participation subsidies, design mentorship by NID/NIFT experts.",
            applicationProcess = "Application via State Handicrafts Development Corporation or registered Handicraft Service Centres.",
            officialPortal = "handicrafts.nic.in"
        ),
        GovtScheme(
            id = "scheme_sfurti",
            name = "SFURTI (Scheme of Fund for Regeneration of Traditional Industries)",
            ministry = "Ministry of MSME",
            category = "Common Facility Centres (CFC)",
            financialSupport = "Up to ₹2.5 Crore to ₹5 Crore per craft cluster for high-tech machinery and value addition.",
            eligibility = "Artisan clusters comprising 500+ artisans organized into Producer Companies or Co-operatives.",
            benefits = "Modern raw-material bank, common design studio, automated testing labs, and bulk export packaging facilities.",
            applicationProcess = "Project proposal submission via KVIC or designated Nodal Agencies.",
            officialPortal = "sfurti.msme.gov.in"
        ),
        GovtScheme(
            id = "scheme_mudra",
            name = "Pradhan Mantri MUDRA Yojana (Shishu & Kishore)",
            ministry = "Ministry of Finance",
            category = "Micro-enterprise Working Capital",
            financialSupport = "Loans up to ₹50,000 (Shishu) and up to ₹5,00,000 (Kishore) with zero processing fees.",
            eligibility = "Individual craft entrepreneurs, micro-weavers, and home-based artisan workshops.",
            benefits = "MUDRA RuPay Debit Card for easy working capital withdrawal for purchasing silk, yarn, clay, and brass scrap.",
            applicationProcess = "Apply through any nationalized public sector bank or Udyamimitra portal.",
            officialPortal = "mudra.org.in"
        )
    )

    val craftClusters = listOf(
        CraftCluster(
            id = "cluster_kanchipuram",
            craftName = "Kanchipuram Silk Weaving",
            state = "Tamil Nadu",
            district = "Kanchipuram",
            category = "Textiles",
            approximateArtisans = 24000,
            heritageDescription = "Famous for pure mulberry silk woven with heavy gold zari using the traditional Korvai interlocking technique dating back to the Chola dynasty.",
            keyMaterials = "Mulberry Silk Yarn, Pure Silver & Gold Zari",
            famousProducts = listOf("Bridal Silk Sarees", "Silk Stoles", "Temple Border Fabrics")
        ),
        CraftCluster(
            id = "cluster_jaipur",
            craftName = "Jaipur Blue Pottery",
            state = "Rajasthan",
            district = "Jaipur",
            category = "Pottery",
            approximateArtisans = 8500,
            heritageDescription = "Unique glazed pottery made from ground quartz stone and Fuller's earth without using clay, colored with natural cobalt blue and copper green minerals.",
            keyMaterials = "Quartz Powder, Fuller's Earth, Natural Glazes",
            famousProducts = listOf("Decorative Floral Vases", "Hand-painted Tiles", "Ceramic Tableware")
        ),
        CraftCluster(
            id = "cluster_channapatna",
            craftName = "Channapatna Lacquer Woodcraft",
            state = "Karnataka",
            district = "Ramanagara",
            category = "Woodcraft",
            approximateArtisans = 6200,
            heritageDescription = "GI-tagged toy craft originated during Tipu Sultan's era using soft Hale wood turned on lathes and coated with natural non-toxic vegetable lacquer.",
            keyMaterials = "Wrightia Tinctoria (Hale Wood), Natural Lac, Turmeric & Indigo",
            famousProducts = listOf("Montessori Stacking Toys", "Decorative Figurines", "Wooden Desk Accessories")
        ),
        CraftCluster(
            id = "cluster_bastar",
            craftName = "Bastar Dhokra Bell Metal",
            state = "Chhattisgarh",
            district = "Bastar & Kondagaon",
            category = "Metalcraft",
            approximateArtisans = 4800,
            heritageDescription = "Ancient 4,000-year-old lost-wax (Cire Perdue) casting technique by tribal artisans creating intricate hollow brass and bronze ethnic artifacts.",
            keyMaterials = "Recycled Brass Alloy, Beeswax, River Bed Clay",
            famousProducts = listOf("Tribal Dancing Figurines", "Elephant Oil Lamps", "Wall Sculptures")
        ),
        CraftCluster(
            id = "cluster_madhubani",
            craftName = "Mithila / Madhubani Painting",
            state = "Bihar",
            district = "Madhubani",
            category = "Folk Painting",
            approximateArtisans = 14000,
            heritageDescription = "Geometric and folklore paintings done using twigs, fingers, and natural plant dyes on handmade paper, canvas, and silk textiles.",
            keyMaterials = "Natural Mineral & Plant Pigments, Handmade Paper, Tussar Silk",
            famousProducts = listOf("Wall Art Scrolls", "Hand-painted Silk Dupattas", "Folk Bookmarks")
        ),
        CraftCluster(
            id = "cluster_varanasi",
            craftName = "Banarasi Brocade Weaving",
            state = "Uttar Pradesh",
            district = "Varanasi",
            category = "Textiles",
            approximateArtisans = 35000,
            heritageDescription = "Mughal-era brocade weaving featuring intricate floral jaal, kalga, and bel motifs crafted with fine gold and silver zari threads.",
            keyMaterials = "Katan Silk, Zari, Satin Weave",
            famousProducts = listOf("Banarasi Silk Sarees", "Brocade Dupattas", "Furnishing Tapestries")
        )
    )

    private suspend fun seedInitialDataIfEmpty() {
        val initialArtisans = listOf(
            ArtisanEntity(
                id = "artisan_lakshmi",
                name = "Lakshmi Ammal",
                craftSpecialization = "Kanchipuram Silk Handloom Weaving",
                villageState = "Kanchipuram, Tamil Nadu",
                experienceYears = 24,
                languageCode = "ta",
                phone = "+91 98412 76540",
                bio = "Third-generation master weaver from Kanchipuram specializing in heavy Korvai borders and traditional peacock motifs.",
                story = "Our family has practiced the handloom craft for over 65 years. We dye each yarn naturally and weave using pure mulberry silk to create pieces that last generations.",
                monthlyCapacityUnits = 25,
                certifications = "GI Tag Certified Handloom Artisan, Weaver Identity Card #TN-KPM-9402",
                awards = "State Master Craftsperson Award 2021",
                rating = 4.9f,
                ordersCompleted = 142,
                totalRevenue = 584000.0,
                isKycVerified = true,
                avatarDrawableRes = "img_artisan_hero"
            ),
            ArtisanEntity(
                id = "artisan_ramesh",
                name = "Ramesh Prajapati",
                craftSpecialization = "Jaipur Blue Pottery",
                villageState = "Kot Jewar, Jaipur, Rajasthan",
                experienceYears = 18,
                languageCode = "hi",
                phone = "+91 94140 88219",
                bio = "Traditional artisan crafting authentic cobalt glazed quartz pottery with intricate Persian and Rajasthani floral motifs.",
                story = "Blue pottery is unique because it uses quartz instead of clay. It requires three firings and patience to achieve the signature translucent blue shine.",
                monthlyCapacityUnits = 80,
                certifications = "Development Commissioner (Handicrafts) Certified",
                awards = "National Merit Certificate 2019",
                rating = 4.8f,
                ordersCompleted = 96,
                totalRevenue = 320000.0,
                isKycVerified = true,
                avatarDrawableRes = "img_pottery_sample"
            ),
            ArtisanEntity(
                id = "artisan_mangal",
                name = "Mangal Dhurwa",
                craftSpecialization = "Bastar Dhokra Bell Metalcraft",
                villageState = "Kondagaon, Bastar, Chhattisgarh",
                experienceYears = 22,
                languageCode = "hi",
                phone = "+91 97550 12384",
                bio = "Tribal master metalsmith practicing 4,000-year-old lost-wax bell metal casting passed down through 5 generations.",
                story = "Each statue takes days of winding pure beeswax threads over clay core. Once molten brass is poured, the clay mold is broken, making every piece one-of-a-kind.",
                monthlyCapacityUnits = 35,
                certifications = "Tribal Co-operative Marketing Federation (TRIFED) Master Craftsperson",
                awards = "National Tribal Craft Award 2020",
                rating = 4.9f,
                ordersCompleted = 118,
                totalRevenue = 412000.0,
                isKycVerified = true,
                avatarDrawableRes = "img_brass_dhokra"
            ),
            ArtisanEntity(
                id = "artisan_gururaj",
                name = "Gururaj Shrestha",
                craftSpecialization = "Channapatna Lacquer Woodcraft",
                villageState = "Channapatna, Ramanagara, Karnataka",
                experienceYears = 15,
                languageCode = "kn",
                phone = "+91 98860 45712",
                bio = "Traditional toy craftsman carving Wrightia Tinctoria (Hale) wood coated with organic non-toxic vegetable dyes.",
                story = "We only use soft ivory wood and natural lac extracted from trees, colored with turmeric and indigo so it is 100% child-safe and eco-friendly.",
                monthlyCapacityUnits = 120,
                certifications = "GI Tag Certificate #GI-KRN-0024",
                awards = "Karnataka Craft Council Excellence Award",
                rating = 4.9f,
                ordersCompleted = 84,
                totalRevenue = 245000.0,
                isKycVerified = true,
                avatarDrawableRes = "img_wood_craft"
            ),
            ArtisanEntity(
                id = "artisan_sunita",
                name = "Sunita Devi",
                craftSpecialization = "Mithila / Madhubani Painting",
                villageState = "Ranti, Madhubani, Bihar",
                experienceYears = 20,
                languageCode = "hi",
                phone = "+91 94310 67891",
                bio = "Folk artist renowned for intricate Bharni style Madhubani canvases and hand-painted Tussar silk sarees.",
                story = "I learned Madhubani from my mother on our mud walls. Every line is drawn with bamboo twigs using natural pigments made from crushed leaves and flowers.",
                monthlyCapacityUnits = 20,
                certifications = "State Master Craftsperson & GI Certified",
                awards = "Bihar State Award in Folk Art",
                rating = 4.9f,
                ordersCompleted = 135,
                totalRevenue = 480000.0,
                isKycVerified = true,
                avatarDrawableRes = "img_artisan_hero"
            )
        )

        artisanDao.insertArtisans(initialArtisans)

        val initialProducts = listOf(
            ProductEntity(
                id = "prod_saree_01",
                artisanId = "artisan_lakshmi",
                artisanName = "Lakshmi Ammal",
                title = "Handwoven Pure Silk Kanchipuram Saree (Peacock & Zari Border)",
                titleHindi = "पारंपरिक हस्तनिर्मित कांचीपुरम सिल्क साड़ी (मोर और ज़री बॉर्डर)",
                titleRegional = "கைத்தறி தூய காஞ்சிபுரம் பட்டு சேலை (மயில் ஜரிகை வேலைப்பாடு)",
                category = "Textiles",
                craftTechnique = "Korvai & Jacquard Pure Mulberry Silk Weaving",
                region = "Kanchipuram, Tamil Nadu",
                rawMaterialCost = 2100.0,
                laborHours = 38.0,
                productionDays = 5,
                suggestedPrice = 2850.0,
                fairMinPrice = 2450.0,
                premiumPrice = 3300.0,
                activePrice = 2850.0,
                listingScore = 96,
                materialsList = "Pure Mulberry Silk Yarn, Gold-coated Silver Zari Threads, Natural Dye Extracts",
                dimensions = "5.5 meters length + 0.8m blouse piece (Width: 48 in)",
                weight = "780 grams",
                description = "Authentic heritage Kanchipuram saree hand-woven on a traditional pit loom with contrasting pallu and intricate peacock zari motifs. Certified pure silk.",
                descriptionHindi = "पारंपरिक कांचीपुरम सिल्क साड़ी, जिसे शुद्ध शहतूत रेशम और सोने की ज़री से हथकरघे पर बुना गया है।",
                descriptionRegional = "பாரம்பரிய பிட் லூமில் நெய்யப்பட்ட தூய காஞ்சிபுரம் பட்டு சேலை. மயில் ஜரிகை மற்றும் இயற்கை வண்ணங்கள்.",
                culturalStory = "Crafted by Lakshmi Ammal, carrying forward her grandfather's weaving patterns dating back 6 decades in Kanchipuram temple cluster.",
                storyLineage = "3rd Generation Weaver Family Heritage",
                careInstructions = "Dry clean only. Store in breathable muslin cotton cloth.",
                packagingSuggestions = "Corrugated craft keepsake box with authenticity certificate.",
                searchKeywords = "kanchipuram saree, pure silk, handloom bridal, traditional zari, ethical artisan",
                stockQuantity = 8,
                soldQuantity = 34,
                reservedQuantity = 3,
                imageDrawableRes = "img_saree_sample",
                enhancedImagePreset = "traditional",
                isVerified = true,
                isPublished = true,
                isOfflineDraft = false
            ),
            ProductEntity(
                id = "prod_pottery_01",
                artisanId = "artisan_ramesh",
                artisanName = "Ramesh Prajapati",
                title = "Handmade Jaipur Blue Pottery Floral Decorative Vase",
                titleHindi = "जयपुर ब्लू पॉटरी हस्तनिर्मित सजावटी फूलदान",
                titleRegional = "ஜெய்ப்பூர் நீல மண்பாண்ட மலர் அலங்கார குவளை",
                category = "Pottery",
                craftTechnique = "Quartz Glass Powder & Natural Cobalt Glazed Shaping",
                region = "Jaipur, Rajasthan",
                rawMaterialCost = 450.0,
                laborHours = 18.0,
                productionDays = 3,
                suggestedPrice = 1150.0,
                fairMinPrice = 880.0,
                premiumPrice = 1450.0,
                activePrice = 1150.0,
                listingScore = 92,
                materialsList = "Quartz Powder, Fuller's Earth, Natural Gum, Cobalt Oxide",
                dimensions = "12 in Height x 6 in Diameter",
                weight = "1.2 kg",
                description = "Hand-thrown and brush-painted Jaipur Blue Pottery vase with cobalt blue floral motifs. Impervious to water and color-fast.",
                descriptionHindi = "क्वार्ट्ज पत्थर और प्राकृतिक रंगों से हस्तनिर्मित जयपुर ब्लू पॉटरी फूलदान।",
                descriptionRegional = "ஜெய்ப்பூரின் பாரம்பரிய முறையில் கையால் வர்ணம் தீட்டப்பட்ட நீல மண்பாண்ட குவளை.",
                culturalStory = "Crafted by Ramesh Prajapati using traditional wood-fired kilns in Kot Jewar village.",
                storyLineage = "Master Potter Lineage of Sanganer",
                careInstructions = "Wipe with a soft dry cloth. Fragile craft, handle with care.",
                packagingSuggestions = "Double-wall honeycomb foam cushioning with custom rigid export box.",
                searchKeywords = "jaipur blue pottery, ceramic vase, cobalt floral, corporate gifting",
                stockQuantity = 14,
                soldQuantity = 62,
                reservedQuantity = 4,
                imageDrawableRes = "img_pottery_sample",
                enhancedImagePreset = "white",
                isVerified = true,
                isPublished = true,
                isOfflineDraft = false
            ),
            ProductEntity(
                id = "prod_wood_01",
                artisanId = "artisan_gururaj",
                artisanName = "Gururaj Shrestha",
                title = "Channapatna Natural Lacquered Wood Stacking Tower & Toys",
                titleHindi = "चन्नापटना हस्तनिर्मित प्राकृतिक लाख लकड़ी खिलौने",
                titleRegional = "சன்னபட்னா இயற்கை மர விளையாட்டுப் பொருட்கள்",
                category = "Woodcraft",
                craftTechnique = "Hand-turned Ivory Wood Lathe Carving & Organic Lac Dyeing",
                region = "Ramanagara, Karnataka",
                rawMaterialCost = 320.0,
                laborHours = 12.0,
                productionDays = 2,
                suggestedPrice = 780.0,
                fairMinPrice = 620.0,
                premiumPrice = 980.0,
                activePrice = 780.0,
                listingScore = 94,
                materialsList = "Hale Wood (Wrightia Tinctoria), Natural Tree Lac, Turmeric & Indigo Pigments",
                dimensions = "8 in Height x 4 in Base Diameter",
                weight = "420 grams",
                description = "Classic Montessori stacking rings handcrafted with smooth rounded edges. 100% non-toxic vegetable dye finish, completely child-safe.",
                descriptionHindi = "हस्तनिर्मित सुरक्षित लकड़ी के खिलौने, प्राकृतिक रंगों से रंगे हुए।",
                descriptionRegional = "குழந்தைகளுக்கு பாதுகாப்பான இயற்கை வண்ணங்கள் பூசப்பட்ட மர பொம்மைகள்.",
                culturalStory = "Turned by hand on traditional wood lathes by Gururaj Shrestha, preserving Karnataka's 200-year-old royal toy-making legacy.",
                storyLineage = "Royal Toy-makers Guild of Channapatna",
                careInstructions = "Wipe with clean dry cloth. Avoid submerging in water.",
                packagingSuggestions = "Recycled Kraft gift cylinder box.",
                searchKeywords = "channapatna toys, wooden stacker, montessori eco toy, handcrafted wood",
                stockQuantity = 22,
                soldQuantity = 94,
                reservedQuantity = 5,
                imageDrawableRes = "img_wood_craft",
                enhancedImagePreset = "rustic",
                isVerified = true,
                isPublished = true,
                isOfflineDraft = false
            ),
            ProductEntity(
                id = "prod_dhokra_01",
                artisanId = "artisan_mangal",
                artisanName = "Mangal Dhurwa",
                title = "Bastar Lost-Wax Dhokra Bell Metal Tribal Dancing Couple",
                titleHindi = "बस्तर ढोकरा कांस्य धातु जनजातीय नृत्य प्रतिमा",
                titleRegional = "பஸ்தார் டோக்ரா பித்தளை பழங்குடி நடனச் சிலை",
                category = "Home Decor",
                craftTechnique = "Ancient Lost-Wax (Cire Perdue) Tribal Bell Metal Casting",
                region = "Bastar, Chhattisgarh",
                rawMaterialCost = 750.0,
                laborHours = 24.0,
                productionDays = 4,
                suggestedPrice = 1850.0,
                fairMinPrice = 1450.0,
                premiumPrice = 2250.0,
                activePrice = 1850.0,
                listingScore = 95,
                materialsList = "Recycled Brass Alloy, Beeswax Wire Threads, River Bed Clay Core",
                dimensions = "10 in Height x 5 in Width x 3.5 in Depth",
                weight = "1.6 kg",
                description = "Exquisite tribal rhythm figurine depicting traditional Bastar festival dancers. Hollow cast with unique rough rustic bronze patina.",
                descriptionHindi = "4,000 साल पुरानी मोम तकनीक से ढली पारंपरिक बस्तरिया धातु मूर्ति।",
                descriptionRegional = "பாரம்பரிய மெழுகு வார்ப்பு முறையில் தயாரிக்கப்பட்ட பழங்குடி பித்தளை கலைப்பொருள்.",
                culturalStory = "Created by Mangal Dhurwa in Kondagaon using natural beeswax threads and river clay, upholding tribal lore.",
                storyLineage = "5th Generation Tribal Dhokra Metalsmiths",
                careInstructions = "Dust with a dry soft bristle brush. Polish with brass cleaner if sheen is desired.",
                packagingSuggestions = "Wooden padded crate with provenance certificate.",
                searchKeywords = "dhokra art, bastar bell metal, lost wax bronze, tribal home decor",
                stockQuantity = 9,
                soldQuantity = 48,
                reservedQuantity = 2,
                imageDrawableRes = "img_brass_dhokra",
                enhancedImagePreset = "traditional",
                isVerified = true,
                isPublished = true,
                isOfflineDraft = false
            ),
            ProductEntity(
                id = "prod_madhubani_01",
                artisanId = "artisan_sunita",
                artisanName = "Sunita Devi",
                title = "Mithila Tree of Life & Fish Motif Hand-Painted Folk Canvas",
                titleHindi = "मिथिला मधुबनी हस्तचित्रित जीवन वृक्ष और मत्स्य कलाकृति",
                titleRegional = "மதுபானி கைவினை வாழ்க்கை மரம் ஓவியம்",
                category = "Paintings",
                craftTechnique = "Fine-nib Bamboo Pen & Natural Botanical Dye Painting",
                region = "Madhubani, Bihar",
                rawMaterialCost = 500.0,
                laborHours = 28.0,
                productionDays = 4,
                suggestedPrice = 1650.0,
                fairMinPrice = 1300.0,
                premiumPrice = 2100.0,
                activePrice = 1650.0,
                listingScore = 94,
                materialsList = "Handmade Rice Straw Paper, Cow Dung Base Wash, Indigo, Turmeric, Madder Pigments",
                dimensions = "18 in x 24 in (Unframed Scroll)",
                weight = "250 grams",
                description = "Authentic Bharni style painting symbolizing fertility and abundance with twin peacock and sacred fish geometry. Museum-quality natural pigments.",
                descriptionHindi = "प्राकृतिक रंगों और बाँस की कलम से बनी प्रामाणिक मधुबनी पेंटिंग।",
                descriptionRegional = "இயற்கை வண்ணங்களால் வரையப்பட்ட பாரம்பரிய மதுபானி நாட்டுப்புற ஓவியம்.",
                culturalStory = "Hand-drawn line-by-line by Sunita Devi in Ranti village without chemical paints.",
                storyLineage = "Mithila Women's Heritage Art Collective",
                careInstructions = "Frame behind UV-protective glass. Keep away from direct moisture.",
                packagingSuggestions = "Rigid protective postal shipping tube.",
                searchKeywords = "madhubani painting, tree of life, folk art canvas, mithila tribal wall decor",
                stockQuantity = 12,
                soldQuantity = 51,
                reservedQuantity = 1,
                imageDrawableRes = "img_artisan_hero",
                enhancedImagePreset = "rustic",
                isVerified = true,
                isPublished = true,
                isOfflineDraft = false
            ),
            ProductEntity(
                id = "prod_filigree_01",
                artisanId = "artisan_lakshmi",
                artisanName = "Bipin Sahu",
                title = "Cuttack Tarakasi Pure 925 Silver Filigree Peacock Jhumkas",
                titleHindi = "कटक तारकशी शुद्ध 925 चांदी की मोर झुमकी",
                titleRegional = "கட்டாக் வெள்ளி நுண்ணிய வேலைப்பாடு ஜிமிக்கி",
                category = "Jewellery",
                craftTechnique = "Ultra-fine Silver Wire Twisting & Granulation (Tarakasi)",
                region = "Cuttack, Odisha",
                rawMaterialCost = 1400.0,
                laborHours = 20.0,
                productionDays = 3,
                suggestedPrice = 2350.0,
                fairMinPrice = 1950.0,
                premiumPrice = 2800.0,
                activePrice = 2350.0,
                listingScore = 95,
                materialsList = "92.5 Sterling Silver Wire, Natural Pearl Drops",
                dimensions = "2.5 in Length x 1.2 in Width",
                weight = "26 grams",
                description = "Delicate gossamer silver earrings created by twisting silver wires finer than hair into intricate floral and peacock patterns.",
                descriptionHindi = "बारीक चांदी के तारों से बनी कटक की प्रसिद्ध तारकशी झुमकी।",
                descriptionRegional = "பாரம்பரிய கட்டாக் வெள்ளி கம்பி வேலைப்பாடு தோடுகள்.",
                culturalStory = "Crafted in the historic silver lanes of Cuttack, continuing the 500-year-old maritime filigree trade tradition.",
                storyLineage = "Master Filigree Goldsmith Guild of Odisha",
                careInstructions = "Store in anti-tarnish zip pouch. Clean gently with silver polishing cloth.",
                packagingSuggestions = "Velvet jewelry keepsake box with 925 hallmarking card.",
                searchKeywords = "silver filigree, tarakasi earrings, handmade 925 jhumkas, heritage jewelry",
                stockQuantity = 16,
                soldQuantity = 73,
                reservedQuantity = 2,
                imageDrawableRes = "img_saree_sample",
                enhancedImagePreset = "white",
                isVerified = true,
                isPublished = true,
                isOfflineDraft = false
            ),
            ProductEntity(
                id = "prod_bamboo_01",
                artisanId = "artisan_gururaj",
                artisanName = "Rumi Borah",
                title = "Assam Golden Cane & Bamboo Ergonomic Planter Basket",
                titleHindi = "असम हस्तनिर्मित बेंत और बांस की टोकरी",
                titleRegional = "அசாம் மூங்கில் மற்றும் பிரம்பு கூடை",
                category = "Basketry",
                craftTechnique = "Fine Sliced Cane Weaving & Natural Resin Polishing",
                region = "Majuli Island, Assam",
                rawMaterialCost = 280.0,
                laborHours = 10.0,
                productionDays = 2,
                suggestedPrice = 690.0,
                fairMinPrice = 520.0,
                premiumPrice = 850.0,
                activePrice = 690.0,
                listingScore = 92,
                materialsList = "Bhaluka Bamboo, Wild Cane Strips, Smoked Mustard Oil Finish",
                dimensions = "10 in Diameter x 9 in Height",
                weight = "380 grams",
                description = "Eco-friendly, lightweight and durable multipurpose basket handcrafted by river island artisans of Majuli. Naturally mold resistant.",
                descriptionHindi = "असम के माजुली द्वीप से हस्तनिर्मित पर्यावरण अनुकूल बांस की टोकरी।",
                descriptionRegional = "இயற்கை மூங்கிலால் நெய்யப்பட்ட உறுதியான அழகிய கூடை.",
                culturalStory = "Woven by indigenous artisan self-help groups using sustainable river-basin bamboo culms.",
                storyLineage = "Brahmaputra Bamboo Craft Co-op",
                careInstructions = "Wipe with damp cloth and dry in indirect sunlight.",
                packagingSuggestions = "Corrugated breathable eco-sleeve.",
                searchKeywords = "assam bamboo, cane basket, boho planter, sustainable home decor",
                stockQuantity = 25,
                soldQuantity = 110,
                reservedQuantity = 6,
                imageDrawableRes = "img_pottery_sample",
                enhancedImagePreset = "rustic",
                isVerified = true,
                isPublished = true,
                isOfflineDraft = false
            ),
            ProductEntity(
                id = "prod_ikat_01",
                artisanId = "artisan_lakshmi",
                artisanName = "Narasimha Chary",
                title = "Pochampally Ikat Handloom Silk Stole (Geometric Chevron)",
                titleHindi = "पोचमपल्ली इकत हथकरघा सिल्क दुपट्टा",
                titleRegional = "போச்சம்பள்ளி இக்கத் பட்டு சால்வை",
                category = "Handloom",
                craftTechnique = "Double Ikat Tie-and-Dye Warp and Weft Weaving",
                region = "Bhoodan Pochampally, Telangana",
                rawMaterialCost = 1100.0,
                laborHours = 22.0,
                productionDays = 3,
                suggestedPrice = 1750.0,
                fairMinPrice = 1450.0,
                premiumPrice = 2100.0,
                activePrice = 1750.0,
                listingScore = 96,
                materialsList = "Mulberry Silk, Natural Mineral Dyes",
                dimensions = "2 meters x 28 inches",
                weight = "220 grams",
                description = "World-famous GI-tagged Pochampally double ikat with sharp diamond patterns. Soft, featherlight drape with luminous sheen.",
                descriptionHindi = "तेलंगाना का प्रसिद्ध पोचमपल्ली इकत सिल्क दुपट्टा।",
                descriptionRegional = "தெலுங்கானா போச்சம்பள்ளி தூய பட்டு சால்வை.",
                culturalStory = "Calculated and tied thread-by-thread before weaving on traditional frame looms by master weaver Narasimha Chary.",
                storyLineage = "Pochampally Handloom Weavers Society",
                careInstructions = "Gentle hand wash in cold water with mild shampoo. Dry in shade.",
                packagingSuggestions = "Handmade silk pouch with silk mark tag.",
                searchKeywords = "pochampally ikat, silk stole, geometric handloom, double ikat scarf",
                stockQuantity = 15,
                soldQuantity = 58,
                reservedQuantity = 3,
                imageDrawableRes = "img_saree_sample",
                enhancedImagePreset = "traditional",
                isVerified = true,
                isPublished = true,
                isOfflineDraft = false
            ),
            ProductEntity(
                id = "prod_bidri_01",
                artisanId = "artisan_mangal",
                artisanName = "Rahimuddin Bidri",
                title = "Bidriware Pure Silver Inlay Floral Keepsake Plate",
                titleHindi = "बिदरीवेयर शुद्ध चांदी जड़ाई सजावटी प्लेट",
                titleRegional = "பித்ரிவேர் வெள்ளி வேலைப்பாடு தட்டு",
                category = "Regional Crafts",
                craftTechnique = "Zinc-Copper Alloy Casting with Pure Silver Sheet Inlay (Tarkashi)",
                region = "Bidar, Karnataka",
                rawMaterialCost = 900.0,
                laborHours = 26.0,
                productionDays = 4,
                suggestedPrice = 2150.0,
                fairMinPrice = 1750.0,
                premiumPrice = 2600.0,
                activePrice = 2150.0,
                listingScore = 95,
                materialsList = "Zinc-Copper Alloy, 99.9% Pure Silver Foil, Bidar Fort Clay Oxidizing Paste",
                dimensions = "8 in Diameter x 1 in Rim Height",
                weight = "650 grams",
                description = "Striking jet-black metal plate inlaid with pure silver Persian arabesque patterns. The deep black color is achieved using historic Bidar fort soil paste.",
                descriptionHindi = "कर्नाटक का प्रसिद्ध बिदरी शिल्प, शुद्ध चांदी की बारीक जड़ाई के साथ।",
                descriptionRegional = "பிதார் கோட்டை மண்ணால் கருமையாக்கப்பட்ட வெள்ளி வேலைப்பாடு தட்டு.",
                culturalStory = "Hand-engraved using chisels and oxidized with 500-year-old traditional Bahmani sultanate techniques.",
                storyLineage = "Bidar Heritage Craft Guild",
                careInstructions = "Wipe with soft cotton cloth lightly moistened with coconut oil.",
                packagingSuggestions = "Velvet lined presentation box with brass latch.",
                searchKeywords = "bidriware plate, silver inlay metalcraft, bidar artisan, indian royal gift",
                stockQuantity = 11,
                soldQuantity = 42,
                reservedQuantity = 1,
                imageDrawableRes = "img_brass_dhokra",
                enhancedImagePreset = "white",
                isVerified = true,
                isPublished = true,
                isOfflineDraft = false
            )
        )

        productDao.insertProducts(initialProducts)

        val initialMaterials = listOf(
            MaterialEntity(
                id = "mat_silk_yarn",
                name = "Pure Mulberry Silk Yarn (Grade A)",
                category = "Yarn & Thread",
                quantity = "12 kg",
                unitCost = 1450.0,
                supplier = "Co-op Silk Federation, Salem"
            ),
            MaterialEntity(
                id = "mat_gold_zari",
                name = "Tested Silver-Gold Zari Spool",
                category = "Embellishments",
                quantity = "8 spools",
                unitCost = 650.0,
                supplier = "Surat Zari Guild"
            ),
            MaterialEntity(
                id = "mat_natural_dyes",
                name = "Natural Indigo & Madder Dye Powder",
                category = "Dyes",
                quantity = "5 kg",
                unitCost = 280.0,
                supplier = "Organic Herbal Dyes, Dindigul"
            ),
            MaterialEntity(
                id = "mat_box_packaging",
                name = "Rigid Keepsake Gift Packaging Box",
                category = "Packaging",
                quantity = "50 boxes",
                unitCost = 45.0,
                supplier = "EcoKraft Printers, Chennai"
            )
        )

        materialDao.insertMaterials(initialMaterials)

        val initialRequests = listOf(
            BuyerRequestEntity(
                id = "rfq_fabindia_01",
                buyerName = "Ananya Sen (Sourcing Lead)",
                buyerOrganization = "Heritage Crafts Retail Collective",
                productRequirement = "Bulk Requirement: 40 Authentic Kanchipuram Silk Stoles with Zari Borders for upcoming Festive Collection",
                craftCategory = "Handloom & Silk Textiles",
                quantity = 40,
                targetUnitPrice = 2400.0,
                deliveryTimeline = "28 Days",
                location = "Bengaluru, Karnataka",
                status = "PENDING",
                counterPrice = 0.0,
                matchScore = 96,
                matchReasons = "✓ Certified Weaving Lineage  ✓ Capacity to deliver 40 units  ✓ Target price within sustainable margin"
            ),
            BuyerRequestEntity(
                id = "rfq_taj_hotels_02",
                buyerName = "Vikram Malhotra",
                buyerOrganization = "Taj Hotels Luxury Gifting Division",
                productRequirement = "100 Handcrafted Jaipur Blue Pottery Desk Planters for Corporate VIP Guests",
                craftCategory = "Clay & Blue Pottery",
                quantity = 100,
                targetUnitPrice = 950.0,
                deliveryTimeline = "21 Days",
                location = "Mumbai, Maharashtra",
                status = "PENDING",
                counterPrice = 0.0,
                matchScore = 92,
                matchReasons = "✓ GI-tag authenticity  ✓ Fits MOQ  ✓ Timely dispatch history"
            )
        )

        buyerRequestDao.insertRequests(initialRequests)

        val initialChat = listOf(
            ChatMessageEntity(
                id = "msg_01",
                conversationId = "conv_fabindia",
                senderRole = "buyer",
                senderName = "Ananya Sen (Buyer)",
                originalText = "Hello Lakshmi, we loved your handloom silk collection! Can you produce 40 units with customized golden zari motif for our Diwali showcase?",
                originalLanguage = "English",
                translatedText = "வணக்கம் லட்சுமி, உங்கள் கைத்தறி பட்டு சேகரிப்பு எங்களுக்கு மிகவும் பிடித்திருந்தது! எங்கள் தீபாவளி காட்சிக்கு தனிப்பயனாக்கப்பட்ட தங்க ஜரிகை வேலைப்பாட்டுடன் 40 உருப்படிகளை உருவாக்க முடியுமா?",
                targetLanguage = "Tamil",
                suggestedReply = "வணக்கம்! நிச்சயம் 40 உருப்படிகளை 25 நாட்களில் சிறந்த தரத்துடன் செய்து தர முடியும். ஒரு துண்டு விலை ₹2,600."
            ),
            ChatMessageEntity(
                id = "msg_02",
                conversationId = "conv_fabindia",
                senderRole = "artisan",
                senderName = "Lakshmi Ammal (Artisan)",
                originalText = "வணக்கம் அனன்யா! எங்கள் கைத்தறியில் தூய பட்டு கொண்டு 40 துண்டுகளை 25 நாட்களில் சிறந்த முறையில் நெய்து தர தயாராக உள்ளோம்.",
                originalLanguage = "Tamil",
                translatedText = "Hello Ananya! We are delighted and ready to hand-weave 40 pieces of pure silk stoles on our looms within 25 days with finest craftsmanship.",
                targetLanguage = "English",
                suggestedReply = ""
            )
        )

        chatDao.insertMessages(initialChat)

        // Seed Sample Orders (Batch 7)
        val initialOrders = listOf(
            OrderEntity(
                id = "HS-784291",
                buyerId = "buyer_default",
                buyerName = "Ananya Sen",
                buyerPhone = "+91 98450 12345",
                recipientName = "Ananya Sen",
                addressStreet = "#42, 3rd Cross, Indiranagar 100ft Road",
                addressCity = "Bengaluru",
                addressState = "Karnataka",
                addressPin = "560038",
                addressType = "Home",
                itemsSummary = "Handwoven Pure Silk Kanchipuram Saree (x1)",
                itemsJson = "[]",
                subtotal = 4250.0,
                deliveryFee = 0.0,
                discountAmount = 425.0,
                totalAmount = 3825.0,
                paymentState = PaymentState.PAID.name,
                orderState = OrderState.PROCESSING.name,
                artisanStatus = ArtisanOrderStatus.PREPARING.name,
                artisanId = "artisan_lakshmi",
                artisanName = "Lakshmi Ammal",
                paymentMethod = "UPI (GPay / PhonePe)",
                isDemoPayment = false,
                razorpayOrderId = "order_rzp_984210",
                razorpayPaymentId = "pay_live_4829104",
                estimatedDeliveryDays = "4–7 days",
                courierName = "India Post Speed Post Express",
                trackingNumber = "IN-POST-84920194",
                createdAt = System.currentTimeMillis() - (86400000L * 2),
                updatedAt = System.currentTimeMillis() - (86400000L * 1)
            ),
            OrderEntity(
                id = "HS-612984",
                buyerId = "buyer_default",
                buyerName = "Ananya Sen",
                buyerPhone = "+91 98450 12345",
                recipientName = "Ananya Sen",
                addressStreet = "#42, 3rd Cross, Indiranagar 100ft Road",
                addressCity = "Bengaluru",
                addressState = "Karnataka",
                addressPin = "560038",
                addressType = "Home",
                itemsSummary = "Dhokra Tribal Brass Bell Figurine (x2)",
                itemsJson = "[]",
                subtotal = 1960.0,
                deliveryFee = 0.0,
                discountAmount = 200.0,
                totalAmount = 1760.0,
                paymentState = PaymentState.PAID.name,
                orderState = OrderState.SHIPPED.name,
                artisanStatus = ArtisanOrderStatus.SHIPPED.name,
                artisanId = "artisan_ramesh",
                artisanName = "Ramesh Baghel",
                paymentMethod = "DEMO PAYMENT",
                isDemoPayment = true,
                razorpayOrderId = "order_demo_612984",
                razorpayPaymentId = "pay_demo_749281",
                estimatedDeliveryDays = "3–5 days",
                courierName = "India Post Speed Post Express",
                trackingNumber = "IN-POST-72819402",
                createdAt = System.currentTimeMillis() - (86400000L * 5),
                updatedAt = System.currentTimeMillis() - (86400000L * 2)
            ),
            OrderEntity(
                id = "HS-519203",
                buyerId = "buyer_default",
                buyerName = "Ananya Sen",
                buyerPhone = "+91 98450 12345",
                recipientName = "Ananya Sen",
                addressStreet = "#42, 3rd Cross, Indiranagar 100ft Road",
                addressCity = "Bengaluru",
                addressState = "Karnataka",
                addressPin = "560038",
                addressType = "Home",
                itemsSummary = "Jaipur Blue Pottery Royal Cobalt Vase (x1)",
                itemsJson = "[]",
                subtotal = 1450.0,
                deliveryFee = 0.0,
                discountAmount = 0.0,
                totalAmount = 1450.0,
                paymentState = PaymentState.PAID.name,
                orderState = OrderState.DELIVERED.name,
                artisanStatus = ArtisanOrderStatus.COMPLETED.name,
                artisanId = "artisan_kailash",
                artisanName = "Kailash Chand Kumhar",
                paymentMethod = "Credit / Debit Card",
                isDemoPayment = false,
                razorpayOrderId = "order_rzp_519203",
                razorpayPaymentId = "pay_live_8392019",
                estimatedDeliveryDays = "Delivered on Aug 24",
                courierName = "India Post Speed Post Express",
                trackingNumber = "IN-POST-49201847",
                createdAt = System.currentTimeMillis() - (86400000L * 12),
                updatedAt = System.currentTimeMillis() - (86400000L * 6)
            )
        )
        orderDao.insertOrders(initialOrders)

        val initialNotifications = listOf(
            ArtisanNotificationEntity(
                id = "notif_01",
                artisanId = "artisan_lakshmi",
                orderId = "HS-784291",
                title = "🎉 புதிய கைவினை ஆர்டர்! (HS-784291)",
                message = "வாழ்த்துக்கள்! கைத்தறி பட்டு புடவைக்கான புதிய ஆர்டர் வந்துள்ளது.\nஅளவு: 1 | ஆர்டர் மதிப்பு: ₹3,825\nகட்டணம்: ✓ உறுதி செய்யப்பட்டது",
                productTitle = "Handwoven Pure Silk Kanchipuram Saree",
                quantity = 1,
                orderValue = 3825.0,
                languageCode = "ta",
                isRead = false,
                timestamp = System.currentTimeMillis() - (86400000L * 2)
            )
        )
        notificationDao.insertNotification(initialNotifications[0])

        // Seed Rich Craft Events (Batch 9)
        val initialEvents = listOf(
            CraftEventEntity(
                id = "evt_trade_fair_1",
                title = "National Handloom & Handicrafts B2B Expo",
                eventType = "TRADE_FAIR",
                description = "Premier international buyer-seller meet and institutional procurement trade fair featuring direct artisan stalls, export facilitation, and wholesale sourcing desks.",
                dateRange = "Oct 12 – 20, 2026",
                timeSchedule = "10:00 AM – 08:00 PM Daily",
                location = "Chennai Trade Centre, Nandambakkam",
                fullAddress = "CTC Complex, Mount Poonamallee Rd, Nandambakkam, Chennai, Tamil Nadu 600089",
                latitude = 13.0189,
                longitude = 80.1804,
                distanceKm = 8.4,
                organizer = "Development Commissioner (Handlooms & Handicrafts), Govt of India",
                contactPerson = "Shri. R. Senthil Kumar (Director)",
                contactPhone = "+91 44 2256 7100",
                contactEmail = "support@handlooms-expo.gov.in",
                officialWebsite = "handicrafts.nic.in",
                registrationStatus = "OPEN",
                registrationFee = "Free (100% Subsidized for Verified Artisans)",
                registrationDeadline = "Sept 25, 2026",
                subsidyDetails = "Includes free 3x3m octanorm stall, standard spot lighting, display tables, and daily TA/DA travel grant for outstation weavers.",
                stallRequirements = "Artisan Pehchan Card or verified GI registration tag required.",
                isGovtSponsored = true,
                imageRes = "img_craft_mela",
                isRegistered = false
            ),
            CraftEventEntity(
                id = "evt_surajkund_2",
                title = "Surajkund International Crafts Mela 2026",
                eventType = "GOVT_SUPPORTED",
                description = "World's largest crafts mela celebrating traditional folk heritage, live artisan loom demonstrations, tribal metal casters, and global buyer pavilions.",
                dateRange = "Nov 02 – 18, 2026",
                timeSchedule = "09:30 AM – 08:30 PM Daily",
                location = "Surajkund Mela Grounds, Faridabad / NCR",
                fullAddress = "Surajkund Crafts Mela Grounds, Lakewood City, Surajkund, Faridabad, Haryana 121009",
                latitude = 28.4891,
                longitude = 77.2831,
                distanceKm = 18.2,
                organizer = "Surajkund Mela Authority & Ministry of Textiles",
                contactPerson = "Smt. Sunita Sharma (Chief Nodal Officer)",
                contactPhone = "+91 129 251 3000",
                contactEmail = "mela@surajkundmelaauthority.com",
                officialWebsite = "haryanatourism.gov.in",
                registrationStatus = "CLOSING_SOON",
                registrationFee = "Free Stall + TA/DA Allowance",
                registrationDeadline = "Oct 05, 2026",
                subsidyDetails = "80% logistics and shipping reimbursement for craft inventory + free food and lodging in Mela Artisan Village.",
                stallRequirements = "Master craftspersons with verified state/national merit awards or certified cooperative membership.",
                isGovtSponsored = true,
                imageRes = "img_pottery_sample",
                isRegistered = true
            ),
            CraftEventEntity(
                id = "evt_dastkar_3",
                title = "Dastkar Nature Bazaar Autumn Festival",
                eventType = "HANDICRAFT_FAIR",
                description = "Vibrant curated autumn handicraft bazaar bringing together 200+ artisan self-help groups, natural fiber weavers, and organic dye specialists.",
                dateRange = "Oct 28 – Nov 04, 2026",
                timeSchedule = "11:00 AM – 07:30 PM Daily",
                location = "Nature Bazaar Kisan Haat, Andheria Modh, New Delhi",
                fullAddress = "Nature Bazaar, Anuvrat Marg, Kisan Haat, Andheria Modh, New Delhi, Delhi 110074",
                latitude = 28.5029,
                longitude = 77.1852,
                distanceKm = 24.5,
                organizer = "Dastkar Society for Crafts & Craftspeople",
                contactPerson = "Meera Roy (Curator)",
                contactPhone = "+91 11 2680 8633",
                contactEmail = "bazaar@dastkar.org",
                officialWebsite = "dastkar.org",
                registrationStatus = "OPEN",
                registrationFee = "₹1,500 / stall (Subsidized for Rural SHGs)",
                registrationDeadline = "Oct 10, 2026",
                subsidyDetails = "Includes custom lighting, digital UPI QR banner, workshop stage for live weaving, and buyer marketing spotlight.",
                stallRequirements = "100% natural, handmade products only. No factory or synthetic materials permitted.",
                isGovtSponsored = false,
                imageRes = "img_saree_sample",
                isRegistered = false
            ),
            CraftEventEntity(
                id = "evt_dakshinachitra_4",
                title = "South India Master Weavers & Heritage Crafts Showcase",
                eventType = "EXHIBITION",
                description = "Exclusive heritage exhibition highlighting rare Korvai temple border weaving, Kalamkari hand-painting, and Chola lost-wax bronze casting.",
                dateRange = "Nov 12 – 16, 2026",
                timeSchedule = "10:00 AM – 06:00 PM Daily",
                location = "DakshinaChitra Heritage Museum, Muttukadu, Chennai",
                fullAddress = "DakshinaChitra Heritage Museum, East Coast Road, Muttukadu, Chengalpattu, Tamil Nadu 603112",
                latitude = 12.8272,
                longitude = 80.2415,
                distanceKm = 14.8,
                organizer = "Madras Craft Foundation & Ministry of Culture",
                contactPerson = "K. Ramanathan (Program Director)",
                contactPhone = "+91 44 2747 2603",
                contactEmail = "exhibits@dakshinachitra.net",
                officialWebsite = "dakshinachitra.net",
                registrationStatus = "INVITE_ONLY",
                registrationFee = "Fully Sponsored (No Fee)",
                registrationDeadline = "Oct 20, 2026",
                subsidyDetails = "Living museum workshop residency, live loom setup, and feature in National Craft Documentation Journal.",
                stallRequirements = "Traditional lineage artisans with 15+ years master experience.",
                isGovtSponsored = true,
                imageRes = "img_brass_dhokra",
                isRegistered = false
            ),
            CraftEventEntity(
                id = "evt_chitra_santhe_5",
                title = "Bangalore Chitra Santhe & Artisan Shilp Street",
                eventType = "ARTISAN_MARKET",
                description = "Massive open-air cultural street festival connecting over 300,000 conscious buyers with verified folk artisans, terracotta potters, and lacquer toymakers.",
                dateRange = "Jan 04, 2027",
                timeSchedule = "08:00 AM – 08:00 PM (1-Day Mega Festival)",
                location = "Karnataka Chitrakala Parishath, Kumara Krupa Road, Bengaluru",
                fullAddress = "Chitrakala Parishath, Kumara Krupa Rd, Seshadripuram, Bengaluru, Karnataka 560001",
                latitude = 12.9868,
                longitude = 77.5815,
                distanceKm = 12.0,
                organizer = "Karnataka Chitrakala Parishath & Dept of Kannada and Culture",
                contactPerson = "Prof. V. Gopal (Registrar)",
                contactPhone = "+91 80 2226 1816",
                contactEmail = "chitrasanthe@artbangalore.org",
                officialWebsite = "karnatakachitrakalaparishath.com",
                registrationStatus = "OPEN",
                registrationFee = "₹500 / stall space",
                registrationDeadline = "Dec 01, 2026",
                subsidyDetails = "Prime footpath exhibition space, event security, clean water, and city-wide press coverage.",
                stallRequirements = "Original handmade paintings, sculptures, terracotta, or handloom crafts.",
                isGovtSponsored = true,
                imageRes = "img_wood_craft",
                isRegistered = false
            ),
            CraftEventEntity(
                id = "evt_shilparamam_6",
                title = "Shilparamam All-India Craft Mela & Bazaar",
                eventType = "HANDICRAFT_FAIR",
                description = "Celebration of all-India rural crafts, brass metalwork, Kondapalli toys, and Chanderi fabrics in an ethnic village atmosphere.",
                dateRange = "Dec 15 – 31, 2026",
                timeSchedule = "10:30 AM – 08:30 PM Daily",
                location = "Shilparamam Cultural Society, Madhapur, Hyderabad",
                fullAddress = "Shilparamam Crafts Village, Hi-Tech City Main Road, Madhapur, Hyderabad, Telangana 500081",
                latitude = 17.4504,
                longitude = 78.3756,
                distanceKm = 22.4,
                organizer = "Shilparamam Arts & Crafts Society, Telangana Tourism",
                contactPerson = "B. Anjaneyulu (Special Officer)",
                contactPhone = "+91 40 6451 8164",
                contactEmail = "info@shilparamam.org",
                officialWebsite = "shilparamam.in",
                registrationStatus = "WAITLIST",
                registrationFee = "₹2,000 for 15-day slot",
                registrationDeadline = "Nov 15, 2026",
                subsidyDetails = "Traditional thatched hut stall with electricity and on-site storage locker.",
                stallRequirements = "Valid Aadhaar and Artisan Pehchan ID card.",
                isGovtSponsored = true,
                imageRes = "img_craft_mela",
                isRegistered = false
            )
        )
        craftEventDao.insertEvents(initialEvents)

        // Seed Initial App Notifications (Artisan & Buyer)
        val initialAppNotifications = listOf(
            // Artisan Notifications
            AppNotificationEntity(
                id = "notif_art_01",
                recipientRole = "ARTISAN",
                recipientId = "artisan_lakshmi",
                category = "ORDERS",
                type = "NEW_ORDER",
                title = "New Direct Order #HS-784291",
                message = "Ananya Sen placed an order for 'Handwoven Pure Silk Kanchipuram Saree' (₹3,825). Ready to weave?",
                badgeText = "NEW ORDER",
                actionRoute = "ORDERS_STOCK",
                relatedEntityId = "HS-784291",
                isRead = false,
                timestamp = System.currentTimeMillis() - (86400000L * 1)
            ),
            AppNotificationEntity(
                id = "notif_art_02",
                recipientRole = "ARTISAN",
                recipientId = "artisan_lakshmi",
                category = "PAYMENTS",
                type = "PAYMENT_RECEIVED",
                title = "Payout Credited: ₹14,200",
                message = "Direct UPI payment for completed bulk orders deposited into your Canara Bank A/c ending 8492. Zero commission deducted.",
                badgeText = "₹ PAID",
                actionRoute = "ARTISAN_PROFILE",
                relatedEntityId = "pay_01",
                isRead = false,
                timestamp = System.currentTimeMillis() - (86400000L * 2)
            ),
            AppNotificationEntity(
                id = "notif_art_03",
                recipientRole = "ARTISAN",
                recipientId = "artisan_lakshmi",
                category = "CRAFT_CIRCLES",
                type = "BULK_INVITE",
                title = "B2B Quota Allocation: 100 Sarees (₹95,000)",
                message = "Kanchipuram Master Weavers Guild received a corporate bulk order from Taj Hotels. Your allocated quota is ready.",
                badgeText = "BULK INVITE",
                actionRoute = "CRAFT_CIRCLES",
                relatedEntityId = "alloc_kpm_taj",
                isRead = false,
                timestamp = System.currentTimeMillis() - (86400000L * 3)
            ),
            AppNotificationEntity(
                id = "notif_art_04",
                recipientRole = "ARTISAN",
                recipientId = "artisan_lakshmi",
                category = "CRAFT_CIRCLES",
                type = "CIRCLE_APPROVAL",
                title = "Craft Circle Membership Approved! ✓",
                message = "Your membership to Kanchipuram Master Weavers Guild has been verified and active. You can now take collective bulk orders.",
                badgeText = "APPROVED",
                actionRoute = "CRAFT_CIRCLES",
                relatedEntityId = "circle_kpm_weavers",
                isRead = true,
                timestamp = System.currentTimeMillis() - (86400000L * 5)
            ),
            AppNotificationEntity(
                id = "notif_art_05",
                recipientRole = "ARTISAN",
                recipientId = "artisan_lakshmi",
                category = "INVENTORY",
                type = "LOW_STOCK",
                title = "Inventory Alert: Only 2 Silk Sarees Left",
                message = "Your listed item 'Natural Indigo Silk Stole' has 2 units remaining. High buyer demand recorded.",
                badgeText = "LOW STOCK",
                actionRoute = "ARTISAN_PRODUCTS",
                relatedEntityId = "prod_stole_01",
                isRead = true,
                timestamp = System.currentTimeMillis() - (86400000L * 4)
            ),
            AppNotificationEntity(
                id = "notif_art_06",
                recipientRole = "ARTISAN",
                recipientId = "artisan_lakshmi",
                category = "MARKET_INSIGHTS",
                type = "DEMAND_INSIGHT",
                title = "Demand Insight: Pure Zari Sarees +45%",
                message = "Buyer searches for pure zari temple border sarees rose 45% ahead of festive season. Suggested price: ₹9,200.",
                badgeText = "INSIGHT",
                actionRoute = "MARKET_PULSE",
                relatedEntityId = "insight_zari",
                isRead = true,
                timestamp = System.currentTimeMillis() - (86400000L * 6)
            ),
            AppNotificationEntity(
                id = "notif_art_07",
                recipientRole = "ARTISAN",
                recipientId = "artisan_lakshmi",
                category = "EVENTS",
                type = "TRADE_FAIR",
                title = "Free Stall: Surajkund Crafts Mela 2026",
                message = "Ministry of Textiles 100% subsidized stall applications open for GI Tagged master artisans at Surajkund Mela.",
                badgeText = "TRADE FAIR",
                actionRoute = "CRAFT_EVENTS",
                relatedEntityId = "evt_surajkund_2",
                isRead = false,
                timestamp = System.currentTimeMillis() - (86400000L * 2)
            ),
            AppNotificationEntity(
                id = "notif_art_08",
                recipientRole = "ARTISAN",
                recipientId = "artisan_lakshmi",
                category = "REVIEWS",
                type = "NEW_REVIEW",
                title = "5.0 ★ Verified Review from Radhika Iyer",
                message = "\"Breathtaking silk quality and authentic heritage texture!\" Your Artisan Trust Score increased by +2 points (now 98/100).",
                badgeText = "5.0 ★ REVIEW",
                actionRoute = "ARTISAN_PROFILE",
                relatedEntityId = "rev_1",
                isRead = false,
                timestamp = System.currentTimeMillis() - (86400000L * 1)
            ),

            // Buyer Notifications
            AppNotificationEntity(
                id = "notif_buy_01",
                recipientRole = "BUYER",
                recipientId = "buyer_default",
                category = "PAYMENTS",
                type = "PAYMENT_CONFIRMED",
                title = "Payment Successful: ₹3,825",
                message = "Your Razorpay UPI transaction for Order #HS-784291 was successful. Funds safely placed in Karigar Escrow.",
                badgeText = "PAID ✓",
                actionRoute = "BUYER_ORDERS",
                relatedEntityId = "HS-784291",
                isRead = false,
                timestamp = System.currentTimeMillis() - (86400000L * 1)
            ),
            AppNotificationEntity(
                id = "notif_buy_02",
                recipientRole = "BUYER",
                recipientId = "buyer_default",
                category = "ORDERS",
                type = "ORDER_CONFIRMED",
                title = "Order Accepted by Lakshmi Ammal",
                message = "Master weaver Lakshmi Ammal in Kanchipuram has accepted your order and began loom preparation.",
                badgeText = "CONFIRMED",
                actionRoute = "BUYER_ORDERS",
                relatedEntityId = "HS-784291",
                isRead = false,
                timestamp = System.currentTimeMillis() - (86400000L * 1)
            ),
            AppNotificationEntity(
                id = "notif_buy_03",
                recipientRole = "BUYER",
                recipientId = "buyer_default",
                category = "ORDERS",
                type = "ORDER_SHIPPED",
                title = "Package Dispatched via India Post",
                message = "Your handcrafted silk piece has been handed over to courier. Tracking Number: #IN-POST-84920194.",
                badgeText = "SHIPPED",
                actionRoute = "BUYER_ORDERS",
                relatedEntityId = "HS-784291",
                isRead = false,
                timestamp = System.currentTimeMillis() - (86400000L * 2)
            ),
            AppNotificationEntity(
                id = "notif_buy_04",
                recipientRole = "BUYER",
                recipientId = "buyer_default",
                category = "ORDERS",
                type = "ORDER_DELIVERED",
                title = "Delivered: Rate Your Handcraft Experience",
                message = "Order #HS-519203 (Jaipur Blue Pottery Vase) was delivered! Tap to leave a voice-verified review for Kailash Chand Kumhar.",
                badgeText = "DELIVERED",
                actionRoute = "BUYER_ORDERS",
                relatedEntityId = "HS-519203",
                isRead = false,
                timestamp = System.currentTimeMillis() - (86400000L * 3)
            ),
            AppNotificationEntity(
                id = "notif_buy_05",
                recipientRole = "BUYER",
                recipientId = "buyer_default",
                category = "PROMOTIONS",
                type = "PROMO_OFFER",
                title = "Festive Handloom Utsav: 15% Off",
                message = "Direct festival savings applied to all verified Kanchipuram, Chanderi, and Banarasi handlooms this week.",
                badgeText = "15% OFF",
                actionRoute = "BUYER_MARKETPLACE",
                relatedEntityId = "promo_festive",
                isRead = true,
                timestamp = System.currentTimeMillis() - (86400000L * 4)
            ),
            AppNotificationEntity(
                id = "notif_buy_06",
                recipientRole = "BUYER",
                recipientId = "buyer_default",
                category = "PROMOTIONS",
                type = "NEW_PRODUCT",
                title = "New Arrivals: Bastar Dhokra Art",
                message = "Master artisan Mangal Dhurwa has uploaded 4 newly cast Bell Metal elephant figurines with GI passports.",
                badgeText = "NEW ARRIVALS",
                actionRoute = "BUYER_MARKETPLACE",
                relatedEntityId = "prod_dhokra_01",
                isRead = true,
                timestamp = System.currentTimeMillis() - (86400000L * 5)
            )
        )
        appNotificationDao.insertNotifications(initialAppNotifications)

        // Seed Initial Verified Reviews
        val initialSeedReviews = listOf(
            ReviewEntity(
                id = "rev_01",
                productId = "prod_saree_01",
                productTitle = "Handwoven Pure Silk Kanchipuram Saree",
                artisanId = "artisan_lakshmi",
                artisanName = "Lakshmi Ammal",
                orderId = "HS-784291",
                buyerId = "buyer_radhika",
                buyerName = "Radhika Iyer",
                overallRating = 5.0f,
                productQualityRating = 5.0f,
                packagingRating = 5.0f,
                deliveryRating = 4.8f,
                authenticityRating = 5.0f,
                reviewText = "Breathtaking craftsmanship! The weight of the silk and the pure silver zari Korvai work are undeniably authentic. You can clearly feel the heritage pit-loom texture. Arrived safely in handmade eco-packaging with the verified GI passport tag.",
                isVoiceReview = true,
                voiceTranscript = "Breathtaking craftsmanship! The weight of the silk and the pure silver zari Korvai work are undeniably authentic. Arrived safely in handmade eco packaging with the verified GI passport tag.",
                isVerifiedPurchase = true,
                buyerLocation = "Bengaluru, Karnataka",
                createdAt = System.currentTimeMillis() - (86400000L * 2)
            ),
            ReviewEntity(
                id = "rev_02",
                productId = "prod_saree_01",
                productTitle = "Handwoven Pure Silk Kanchipuram Saree",
                artisanId = "artisan_lakshmi",
                artisanName = "Lakshmi Ammal",
                orderId = "HS-981204",
                buyerId = "buyer_anand",
                buyerName = "Anand Vardhan",
                overallRating = 5.0f,
                productQualityRating = 5.0f,
                packagingRating = 4.9f,
                deliveryRating = 5.0f,
                authenticityRating = 5.0f,
                reviewText = "Ordered multiple pieces for family wedding gifting. Every saree is unique and came with the verified GI passport tag. Knowing 100% of my payment went directly to Lakshmi Ammal's weaver family without middlemen makes this priceless.",
                isVoiceReview = false,
                voiceTranscript = "",
                isVerifiedPurchase = true,
                buyerLocation = "Mumbai, Maharashtra",
                createdAt = System.currentTimeMillis() - (86400000L * 7)
            ),
            ReviewEntity(
                id = "rev_03",
                productId = "prod_stole_01",
                productTitle = "Natural Indigo Temple Border Silk Stole",
                artisanId = "artisan_lakshmi",
                artisanName = "Lakshmi Ammal",
                orderId = "HS-442109",
                buyerId = "buyer_meenakshi",
                buyerName = "Dr. Meenakshi Sundaram",
                overallRating = 4.8f,
                productQualityRating = 4.9f,
                packagingRating = 4.8f,
                deliveryRating = 4.7f,
                authenticityRating = 5.0f,
                reviewText = "Flawless finish and vivid natural dyes. Exactly as shown during the live loom demonstration video. Very impressed with the quick India Post Speed Post delivery.",
                isVoiceReview = false,
                voiceTranscript = "",
                isVerifiedPurchase = true,
                buyerLocation = "Chennai, Tamil Nadu",
                createdAt = System.currentTimeMillis() - (86400000L * 15)
            ),
            ReviewEntity(
                id = "rev_04",
                productId = "prod_dhokra_01",
                productTitle = "Dhokra Tribal Brass Bell Figurine",
                artisanId = "artisan_ramesh",
                artisanName = "Ramesh Baghel",
                orderId = "HS-612984",
                buyerId = "buyer_sangeetha",
                buyerName = "Sangeetha Narayanan",
                overallRating = 5.0f,
                productQualityRating = 5.0f,
                packagingRating = 5.0f,
                deliveryRating = 4.9f,
                authenticityRating = 5.0f,
                reviewText = "The lost-wax hollow casting detail on the tribal elephant is museum quality. The brass bell has a soothing ethnic resonance. 100% authentic Bastar craft!",
                isVoiceReview = true,
                voiceTranscript = "The lost-wax hollow casting detail on the tribal elephant is museum quality. The brass bell has a soothing ethnic resonance. 100% authentic Bastar craft!",
                isVerifiedPurchase = true,
                buyerLocation = "Hyderabad, Telangana",
                createdAt = System.currentTimeMillis() - (86400000L * 5)
            ),
            ReviewEntity(
                id = "rev_05",
                productId = "prod_pottery_01",
                productTitle = "Jaipur Blue Pottery Royal Cobalt Vase",
                artisanId = "artisan_kailash",
                artisanName = "Kailash Chand Kumhar",
                orderId = "HS-519203",
                buyerId = "buyer_rohan",
                buyerName = "Rohan Deshmukh",
                overallRating = 4.9f,
                productQualityRating = 5.0f,
                packagingRating = 4.8f,
                deliveryRating = 4.9f,
                authenticityRating = 5.0f,
                reviewText = "The quartz stone glaze and hand-painted Persian floral motifs are exquisite. Packed in rigid shock-absorbing padding so it arrived in perfect condition.",
                isVoiceReview = false,
                voiceTranscript = "",
                isVerifiedPurchase = true,
                buyerLocation = "Pune, Maharashtra",
                createdAt = System.currentTimeMillis() - (86400000L * 12)
            )
        )
        reviewDao.insertReviews(initialSeedReviews)
    }

    suspend fun registerForEvent(eventId: String) {
        craftEventDao.updateRegistrationStatus(eventId, isRegistered = true, status = "REGISTERED")
        fcmService.sendPushNotification(
            AppNotificationEntity(
                id = "notif_evt_${System.currentTimeMillis()}",
                recipientRole = "ARTISAN",
                recipientId = "artisan_lakshmi",
                category = "EVENTS",
                type = "TRADE_FAIR",
                title = "Stall Registration Confirmed! ✓",
                message = "Your artisan stall allocation has been confirmed. Free electricity, display racks, and GI passport showcase included.",
                badgeText = "REGISTERED",
                actionRoute = "CRAFT_EVENTS",
                relatedEntityId = eventId,
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun submitReview(
        productId: String,
        productTitle: String,
        artisanId: String,
        artisanName: String,
        orderId: String,
        buyerId: String = "buyer_default",
        buyerName: String = "Radhika Iyer",
        buyerLocation: String = "Bengaluru, Karnataka",
        overallRating: Float,
        productQualityRating: Float,
        packagingRating: Float,
        deliveryRating: Float,
        authenticityRating: Float,
        reviewText: String,
        isVoiceReview: Boolean = false,
        voiceTranscript: String = ""
    ) {
        val review = ReviewEntity(
            id = "rev_${System.currentTimeMillis()}",
            productId = productId,
            productTitle = productTitle,
            artisanId = artisanId,
            artisanName = artisanName,
            orderId = orderId,
            buyerId = buyerId,
            buyerName = buyerName,
            overallRating = overallRating,
            productQualityRating = productQualityRating,
            packagingRating = packagingRating,
            deliveryRating = deliveryRating,
            authenticityRating = authenticityRating,
            reviewText = reviewText.ifBlank { voiceTranscript },
            isVoiceReview = isVoiceReview,
            voiceTranscript = voiceTranscript,
            isVerifiedPurchase = true,
            buyerLocation = buyerLocation,
            createdAt = System.currentTimeMillis()
        )
        reviewDao.insertReview(review)

        // Trigger push notification to artisan
        fcmService.sendPushNotification(
            AppNotificationEntity(
                id = "notif_rev_${System.currentTimeMillis()}",
                recipientRole = "ARTISAN",
                recipientId = artisanId,
                category = "REVIEWS",
                type = "NEW_REVIEW",
                title = "New ${String.format("%.1f", overallRating)} ★ Review from $buyerName",
                message = "\"${review.reviewText.take(90)}...\" Your Artisan Trust Score has been updated.",
                badgeText = "${String.format("%.1f", overallRating)} ★ REVIEW",
                actionRoute = "ARTISAN_PROFILE",
                relatedEntityId = review.id,
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun getTransparentTrustBreakdown(artisanId: String, artisanName: String = "Lakshmi Ammal"): TransparentTrustBreakdown {
        // Collect reviews from database
        val reviews = mutableListOf<ReviewEntity>()
        return TrustScoreEngine.calculate(
            artisanId = artisanId,
            artisanName = artisanName,
            isKycVerified = true,
            completedOrders = 104,
            reviews = reviews,
            fulfillmentRatePercent = 98,
            onTimeDeliveryPercent = 96,
            cancellationRatePercent = 1
        )
    }

    // --- BATCH 10: OFFLINE-FIRST & PRODUCT DRAFTS ---
    fun getLatestProductDraft(artisanId: String): Flow<com.example.data.local.ProductDraftEntity?> =
        productDraftDao.getLatestDraft(artisanId)

    suspend fun saveProductDraft(draft: com.example.data.local.ProductDraftEntity) {
        productDraftDao.insertDraft(draft)
    }

    suspend fun deleteProductDraft(id: String) {
        productDraftDao.deleteDraft(id)
    }

    suspend fun recordOfflineVoice(
        artisanId: String,
        audioPath: String,
        transcript: String,
        imageUri: String,
        draftId: String
    ) {
        val voiceRecord = com.example.data.local.OfflineVoiceRecordingEntity(
            id = "voice_" + System.currentTimeMillis(),
            artisanId = artisanId,
            audioFilePath = audioPath,
            localTranscript = transcript,
            associatedImageUri = imageUri,
            productDraftId = draftId,
            isSynced = false
        )
        offlineVoiceRecordingDao.insertRecording(voiceRecord)
    }

    fun getPendingOfflineSyncCount(): Flow<Int> = offlineSyncQueueDao.getPendingCount()

    fun isOnlineFlow(): Flow<Boolean> = offlineSyncManager.isOnline
}

