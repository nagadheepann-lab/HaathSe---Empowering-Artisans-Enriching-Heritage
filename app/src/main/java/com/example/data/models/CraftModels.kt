package com.example.data.models

import com.example.data.local.ProductEntity

enum class AppRole(val label: String) {
    ARTISAN("Artisan (कारीगर)"),
    BUYER("B2B Buyer (खरीदार)"),
    ADMIN("Ministry / NGO Admin")
}

enum class SupportedLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val voiceGreeting: String
) {
    HINDI("hi", "हिन्दी", "Hindi", "नमस्ते! आपका हाथ से (HaathSe) में स्वागत है।"),
    TAMIL("ta", "தமிழ்", "Tamil", "வணக்கம்! உங்கள் ஹாத்ஸே (HaathSe) செயலியில் வரவேற்கிறோம்."),
    ENGLISH("en", "English", "English", "Welcome to HaathSe. Made by Hand. Your craft, your story, your business."),
    TELUGU("te", "తెలుగు", "Telugu", "నమస్కారం! మీ హాత్‌సే (HaathSe)కి స్వాగతం."),
    KANNADA("kn", "ಕನ್ನಡ", "Kannada", "ನಮಸ್ಕಾರ! ನಿಮ್ಮ ಹಾತ್‌ಸೆ (HaathSe)ಗೆ ಸುಸ್ವಾಗತ."),
    MALAYALAM("ml", "മലയാളം", "Malayalam", "നമസ്കാരം! നിങ്ങളുടെ ഹാത്ത്‌സെ (HaathSe)യിലേക്ക് സ്വാഗതം."),
    BENGALI("bn", "বাংলা", "Bengali", "নমস্কার! হাত সে (HaathSe)-তে আপনাকে স্বাগতম।"),
    MARATHI("mr", "मराठी", "Marathi", "नमस्कार! हात से (HaathSe) मध्ये आपले स्वागत आहे."),
    GUJARATI("gu", "ગુજરાતી", "Gujarati", "નમસ્તે! હાથ સે (HaathSe) માં આપનું સ્વાગત છે."),
    PUNJABI("pa", "ਪੰਜਾਬੀ", "Punjabi", "ਸਤਿ ਸ੍ਰੀ ਅਕਾਲ! ਹਾਥ ਸੇ (HaathSe) ਵਿੱਚ ਤੁਹਾਡਾ ਸੁਆਗਤ ਹੈ।"),
    ODIA("or", "ଓଡ଼ିଆ", "Odia", "ନମସ୍କାର! ହାତ ସେ (HaathSe)ରେ ଆପଣଙ୍କୁ ସ୍ୱାଗତ।")
}

enum class CraftCategory(val label: String, val iconName: String) {
    TEXTILES("Handloom & Silk Textiles", "checkroom"),
    POTTERY("Clay & Blue Pottery", "wash"),
    WOODCRAFT("Carved Wood & Lacquerware", "forest"),
    METALCRAFT("Dhokra & Brass Metalcraft", "hardware"),
    LEATHER_BAMBOO("Bamboo & Cane Craft", "shopping_bag"),
    FOLK_PAINTING("Madhubani & Folk Paintings", "palette"),
    JEWELRY("Traditional Filigree Jewelry", "diamond")
}

enum class BackgroundPreset(val id: String, val label: String, val desc: String) {
    CLEAN_WHITE("white", "Clean White Studio", "Optimal for Amazon, Flipkart & B2B Catalogs"),
    TRADITIONAL_INDIAN("traditional", "Heritage Royal", "Silk motif warm festive background"),
    MINIMAL_STUDIO("minimal", "Minimal Modern", "Soft neutral studio shadow lighting"),
    RUSTIC_HANDCRAFT("rustic", "Rustic Handcrafted", "Natural wood & clay workshop backdrop"),
    TRANSPARENT("transparent", "Isolated Transparent", "Clean cutout for flyers and banners")
}

data class PriceRecommendation(
    val suggestedPrice: Double,
    val fairMinPrice: Double,
    val premiumPrice: Double,
    val rawMaterialCost: Double,
    val estimatedLaborCost: Double,
    val estimatedMargin: Double,
    val confidencePercentage: Int,
    val reasoning: String
)

data class ListingScoreReport(
    val totalScore: Int,
    val photoScore: Int,
    val descriptionScore: Int,
    val pricingScore: Int,
    val dimensionsProvided: Boolean,
    val careInstructionsProvided: Boolean,
    val culturalStoryProvided: Boolean,
    val improvementTips: List<String>
)

data class SmartMatchResult(
    val matchPercentage: Int,
    val reasons: List<String>,
    val canDeliverOnTime: Boolean,
    val fitsBudget: Boolean,
    val fitsCapacity: Boolean
)

data class GovtScheme(
    val id: String,
    val name: String,
    val ministry: String,
    val category: String,
    val financialSupport: String,
    val eligibility: String,
    val benefits: String,
    val applicationProcess: String,
    val officialPortal: String
)

data class CraftCluster(
    val id: String,
    val craftName: String,
    val state: String,
    val district: String,
    val category: String,
    val approximateArtisans: Int,
    val heritageDescription: String,
    val keyMaterials: String,
    val famousProducts: List<String>
)

data class CraftCircle(
    val id: String,
    val name: String,
    val craftType: String,
    val location: String,
    val memberCount: Int,
    val activeCollectiveOrders: Int,
    val availableMonthlyCapacity: String,
    val leaderName: String,
    val clusterId: String? = null
)

data class CraftEvent(
    val id: String,
    val title: String,
    val dateRange: String,
    val location: String,
    val organizer: String,
    val stallRentSubsidy: String,
    val isGovtSponsored: Boolean = true,
    val registrationDeadline: String
)

enum class StockAlertLevel {
    LOW_STOCK,
    OUT_OF_STOCK,
    HIGH_DEMAND
}

data class InventoryAlert(
    val id: String,
    val productName: String,
    val alertLevel: StockAlertLevel,
    val message: String,
    val currentStock: Int,
    val suggestedRestock: Int
)

data class RevenueDataPoint(
    val label: String,
    val amount: Float,
    val orders: Int
)

enum class ProductSortOption(val displayName: String) {
    RECOMMENDED("Recommended"),
    NEWEST("Newest"),
    PRICE_LOW_TO_HIGH("Price: Low to High"),
    PRICE_HIGH_TO_LOW("Price: High to Low"),
    PRICE_LOW_HIGH("Price Low → High"),
    PRICE_HIGH_LOW("Price High → Low"),
    TOP_RATED("Top Rated"),
    TRENDING("Trending")
}

data class BuyerFilterState(
    val searchQuery: String = "",
    val minPrice: Double = 0.0,
    val maxPrice: Double = 25000.0,
    val selectedCategory: String? = null,
    val selectedMaterial: String? = null,
    val selectedCraft: String? = null,
    val selectedLocation: String? = null,
    val minRating: Double = 0.0,
    val verifiedOnly: Boolean = false,
    val inStockOnly: Boolean = false,
    val expressDeliveryOnly: Boolean = false,
    val sortOption: ProductSortOption = ProductSortOption.RECOMMENDED,
    val sortBy: ProductSortOption = ProductSortOption.RECOMMENDED
)


data class BuyerCartItem(
    val product: ProductEntity,
    var quantity: Int
)

data class TrustScoreDetails(
    val overallScore: Int,
    val artisanName: String,
    val isVerifiedArtisan: Boolean,
    val verifiedArtisanScore: Int = 25,
    val completedOrdersScore: Int = 20,
    val buyerRatingsScore: Int = 20,
    val fulfillmentRateScore: Int = 15,
    val deliveryPerformanceScore: Int = 10,
    val cancellationScore: Int = 10,
    val completedOrdersCount: Int = 142,
    val fulfillmentRatePercent: Int = 98,
    val averageRating: Float = 4.9f,
    val onTimeDeliveryPercent: Int = 96,
    val cancellationRatePercent: Int = 1
)

data class ProductReview(
    val id: String,
    val reviewerName: String,
    val rating: Float,
    val date: String,
    val comment: String,
    val verifiedPurchase: Boolean = true,
    val location: String = "Verified Buyer"
)

data class BuyerMarketplaceCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageRes: String,
    val countLabel: String
)

data class HeroCollection(
    val id: String,
    val tag: String,
    val title: String,
    val subtitle: String,
    val imageRes: String,
    val actionText: String,
    val categoryTarget: String
)

// ==========================================
// BATCH 7: CART, CHECKOUT, RAZORPAY & ORDER MODELS
// ==========================================

enum class PaymentState(val label: String) {
    CREATED("Order Created"),
    PAYMENT_PENDING("Payment Pending"),
    PAID("Paid ✓"),
    FAILED("Payment Failed"),
    REFUNDED("Refunded")
}

enum class OrderState(val label: String) {
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

enum class ArtisanOrderStatus(val label: String, val tabLabel: String) {
    NEW("New Order", "New"),
    PREPARING("Preparing / Weaving", "Preparing"),
    READY("Ready for Dispatch", "Ready"),
    SHIPPED("Shipped to Buyer", "Shipped"),
    DELIVERED("Delivered to Buyer", "Delivered"),
    COMPLETED("Order Completed", "Completed")
}

enum class PaymentMethod(val displayName: String, val description: String) {
    RAZORPAY_UPI("UPI (GPay / PhonePe / Paytm / BHIM)", "Instant direct bank transfer with zero fee"),
    RAZORPAY_CARD("Credit / Debit Card & EMI", "Visa, MasterCard, RuPay & Diners"),
    RAZORPAY_NETBANKING("NetBanking", "All major Indian scheduled banks"),
    DEMO_PAYMENT("DEMO PAYMENT", "Secure sandbox simulation with verification handshake")
}

data class CartItem(
    val product: ProductEntity,
    val quantity: Int = 1,
    val isSavedForLater: Boolean = false
)

data class CartState(
    val items: List<CartItem> = emptyList(),
    val savedForLater: List<CartItem> = emptyList(),
    val couponCode: String? = null,
    val discountAmount: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val subtotal: Double = 0.0,
    val total: Double = 0.0,
    val isLoading: Boolean = false,
    val message: String? = null
)

data class DeliveryAddress(
    val fullName: String = "Ananya Sen",
    val phone: String = "+91 98450 12345",
    val streetAddress: String = "#42, 3rd Cross, Indiranagar 100ft Road",
    val city: String = "Bengaluru",
    val state: String = "Karnataka",
    val pinCode: String = "560038",
    val addressType: String = "Home" // "Home", "Work", "Studio"
)

data class PaymentOrderRequest(
    val buyerId: String,
    val items: List<CartItem>,
    val deliveryAddress: DeliveryAddress,
    val subtotal: Double,
    val deliveryFee: Double,
    val discountAmount: Double,
    val totalAmount: Double,
    val paymentMethod: PaymentMethod,
    val isDemo: Boolean = false
)

data class PaymentOrderResponse(
    val internalOrderId: String,
    val razorpayOrderId: String,
    val amountInPaise: Long,
    val currency: String = "INR",
    val keyId: String,
    val isDemo: Boolean,
    val customerName: String,
    val customerEmail: String,
    val customerContact: String
)

data class PaymentVerificationRequest(
    val internalOrderId: String,
    val razorpayPaymentId: String,
    val razorpayOrderId: String,
    val razorpaySignature: String,
    val isDemo: Boolean
)

data class PaymentVerificationResponse(
    val isSuccess: Boolean,
    val message: String,
    val paymentState: PaymentState,
    val internalOrderId: String,
    val razorpayPaymentId: String
)

// ==========================================
// BATCH 8: CRAFT CIRCLES & B2B COMMERCE MODELS
// ==========================================

data class CraftCircleFilterState(
    val searchQuery: String = "",
    val selectedCraft: String? = null,
    val selectedSpecialization: String? = null,
    val selectedLocation: String? = null,
    val maxDistanceKm: Double = 500.0,
    val minCapacity: Int = 0,
    val availableOnly: Boolean = false,
    val minTrustScore: Int = 0
)

data class CircleJoinFormData(
    val craftSpecialization: String = "",
    val experienceYears: Int = 5,
    val previousWorkDesc: String = "",
    val productionCapacityMonthly: Int = 50,
    val availabilityTimeline: String = "Immediate",
    val location: String = "Chennai, Tamil Nadu",
    val portfolioImages: List<String> = listOf("portfolio_sample_1", "portfolio_sample_2")
)

data class BulkOrderRequestData(
    val craftCategory: String = "Handloom",
    val productRequirement: String = "",
    val quantity: Int = 100,
    val targetUnitPrice: Double = 950.0,
    val totalBudget: Double = 95000.0,
    val deadlineDays: Int = 30,
    val deadlineDate: String = "30 Oct 2026",
    val customizationNotes: String = "",
    val technicalRequirements: String = ""
)

data class BulkOrderMatchRecommendation(
    val circleId: String,
    val circleName: String,
    val craftType: String,
    val location: String,
    val distanceKm: Double,
    val trustScore: Int,
    val matchScore: Int,
    val matchReasons: List<String>,
    val availableCapacity: Int,
    val totalArtisans: Int,
    val estimatedLeadTimeDays: Int,
    val isRecommended: Boolean,
    val suggestedAllocations: List<BulkAllocationItem>
)

data class BulkAllocationItem(
    val artisanId: String,
    val artisanName: String,
    val craftSpecialization: String,
    val allocatedQuantity: Int,
    val unitPayout: Double,
    val estimatedPayout: Double,
    val trustScore: Int,
    val monthlyCapacity: Int,
    val avatarRes: String,
    val aiReasoning: String
)


