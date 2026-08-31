package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val artisanId: String,
    val artisanName: String,
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
    val activePrice: Double,
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
    val stockQuantity: Int,
    val soldQuantity: Int,
    val reservedQuantity: Int,
    val imageDrawableRes: String,
    val enhancedImagePreset: String,
    val isVerified: Boolean,
    val isPublished: Boolean,
    val isOfflineDraft: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "artisans")
data class ArtisanEntity(
    @PrimaryKey val id: String,
    val name: String,
    val craftSpecialization: String,
    val villageState: String,
    val experienceYears: Int,
    val languageCode: String,
    val phone: String,
    val bio: String,
    val story: String,
    val monthlyCapacityUnits: Int,
    val certifications: String,
    val awards: String,
    val rating: Float,
    val ordersCompleted: Int,
    val totalRevenue: Double,
    val isKycVerified: Boolean,
    val avatarDrawableRes: String
)

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val quantity: String,
    val unitCost: Double,
    val supplier: String,
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "buyer_requests")
data class BuyerRequestEntity(
    @PrimaryKey val id: String,
    val buyerName: String,
    val buyerOrganization: String,
    val productRequirement: String,
    val craftCategory: String,
    val quantity: Int,
    val targetUnitPrice: Double,
    val deliveryTimeline: String,
    val location: String,
    val status: String, // "PENDING", "ACCEPTED", "COUNTERED", "REJECTED"
    val counterPrice: Double = 0.0,
    val matchScore: Int = 85,
    val matchReasons: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderRole: String, // "artisan", "buyer"
    val senderName: String,
    val originalText: String,
    val originalLanguage: String,
    val translatedText: String,
    val targetLanguage: String,
    val suggestedReply: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String, // e.g. "HS-984210"
    val buyerId: String,
    val buyerName: String,
    val buyerPhone: String,
    val recipientName: String,
    val addressStreet: String,
    val addressCity: String,
    val addressState: String,
    val addressPin: String,
    val addressType: String, // "Home", "Work", "Studio"
    val itemsSummary: String,
    val itemsJson: String,
    val subtotal: Double,
    val deliveryFee: Double,
    val discountAmount: Double,
    val totalAmount: Double,
    val paymentState: String, // "CREATED", "PAYMENT_PENDING", "PAID", "FAILED", "REFUNDED"
    val orderState: String, // "PROCESSING", "SHIPPED", "DELIVERED", "COMPLETED", "CANCELLED"
    val artisanStatus: String, // "New", "Preparing", "Ready", "Shipped", "Delivered", "Completed"
    val artisanId: String,
    val artisanName: String,
    val paymentMethod: String,
    val isDemoPayment: Boolean,
    val razorpayOrderId: String,
    val razorpayPaymentId: String,
    val estimatedDeliveryDays: String = "4–7 days",
    val courierName: String = "India Post Speed Post",
    val trackingNumber: String = "IN-POST-84920194",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "artisan_notifications")
data class ArtisanNotificationEntity(
    @PrimaryKey val id: String,
    val artisanId: String,
    val orderId: String,
    val title: String,
    val message: String,
    val productTitle: String,
    val quantity: Int,
    val orderValue: Double,
    val languageCode: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "craft_circles")
data class CraftCircleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val craftType: String,
    val specialization: String,
    val story: String,
    val location: String,
    val latitude: Double = 13.0827,
    val longitude: Double = 80.2707,
    val distanceKm: Double = 12.0,
    val memberCount: Int,
    val monthlyCapacityUnits: Int,
    val availableCapacityUnits: Int,
    val activeBulkOrders: Int,
    val completedBulkOrders: Int,
    val trustScore: Int,
    val adminId: String,
    val adminName: String,
    val adminPhone: String = "+91 98401 23456",
    val adminAvatar: String = "avatar_lakshmi",
    val imageRes: String = "img_saree_sample",
    val isAvailableForBulk: Boolean = true,
    val minOrderQuantity: Int = 25,
    val avgFulfillmentDays: Int = 21,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "circle_members")
data class CircleMemberEntity(
    @PrimaryKey val id: String,
    val circleId: String,
    val artisanId: String,
    val artisanName: String,
    val craftSpecialization: String,
    val experienceYears: Int,
    val monthlyCapacityUnits: Int,
    val currentLoadUnits: Int,
    val availableCapacityUnits: Int,
    val trustScore: Int,
    val performanceRating: Float,
    val fulfillmentHistoryCount: Int,
    val avatarRes: String,
    val role: String, // "Admin", "Master Artisan", "Artisan Member"
    val status: String = "ACTIVE" // "ACTIVE", "PENDING_APPROVAL"
)

@Entity(tableName = "circle_join_requests")
data class CircleJoinRequestEntity(
    @PrimaryKey val id: String,
    val circleId: String,
    val circleName: String,
    val artisanId: String,
    val artisanName: String,
    val craftSpecialization: String,
    val experienceYears: Int,
    val previousWorkDesc: String,
    val productionCapacityMonthly: Int,
    val availabilityTimeline: String,
    val location: String,
    val portfolioImagesCount: Int = 3,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val submittedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bulk_orders")
data class BulkOrderEntity(
    @PrimaryKey val id: String,
    val buyerId: String,
    val buyerName: String,
    val buyerOrg: String,
    val circleId: String,
    val circleName: String,
    val productRequirement: String,
    val craftCategory: String,
    val quantity: Int,
    val allocatedQuantity: Int = 0,
    val totalBudget: Double,
    val unitBudget: Double,
    val deadline: String,
    val customizationNotes: String,
    val technicalRequirements: String,
    val status: String = "PENDING_ALLOCATION", // "PENDING_ALLOCATION", "ALLOCATED", "IN_PRODUCTION", "READY_FOR_DISPATCH", "COMPLETED"
    val fulfillmentProgress: Int = 0, // 0 to 100%
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bulk_allocations")
data class BulkAllocationEntity(
    @PrimaryKey val id: String,
    val bulkOrderId: String,
    val circleId: String,
    val circleName: String,
    val productRequirement: String,
    val buyerOrg: String,
    val artisanId: String,
    val artisanName: String,
    val artisanAvatar: String = "avatar_lakshmi",
    val allocatedQuantity: Int,
    val unitPayout: Double,
    val estimatedPayout: Double,
    val deadline: String,
    val invitationStatus: String = "PENDING", // "PENDING", "ACCEPTED", "DECLINED"
    val productionProgress: Int = 0, // 0, 25, 50, 75, 100
    val isReadyForDispatch: Boolean = false,
    val shippingInstructions: String = "Attach GI Certification Tag #GI-TN-8492. Pack in moisture-sealed eco-corrugated carton. Handover to India Post Speed Post Dispatch Hub.",
    val aiReasoning: String = "Optimal allocation based on 98% past on-time fulfillment, high craft fidelity, and verified monthly capacity.",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "craft_events")
data class CraftEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val eventType: String, // "TRADE_FAIR", "EXHIBITION", "GOVT_SUPPORTED", "HANDICRAFT_FAIR", "ARTISAN_MARKET"
    val description: String,
    val dateRange: String,
    val timeSchedule: String,
    val location: String,
    val fullAddress: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double,
    val organizer: String,
    val contactPerson: String,
    val contactPhone: String,
    val contactEmail: String,
    val officialWebsite: String,
    val registrationStatus: String, // "OPEN", "CLOSING_SOON", "REGISTERED", "WAITLIST", "INVITE_ONLY"
    val registrationFee: String,
    val registrationDeadline: String,
    val subsidyDetails: String,
    val stallRequirements: String,
    val isGovtSponsored: Boolean = true,
    val imageRes: String = "img_craft_mela",
    val isRegistered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_notifications")
data class AppNotificationEntity(
    @PrimaryKey val id: String,
    val recipientRole: String, // "ARTISAN", "BUYER", "ADMIN"
    val recipientId: String,
    val category: String, // "ORDERS", "PAYMENTS", "CRAFT_CIRCLES", "INVENTORY", "MARKET_INSIGHTS", "EVENTS", "REVIEWS", "PROMOTIONS"
    val type: String, // "NEW_ORDER", "PAYMENT_RECEIVED", "BULK_INVITE", "CIRCLE_APPROVAL", "LOW_STOCK", "DEMAND_INSIGHT", "TRADE_FAIR", "NEW_REVIEW", "ORDER_CONFIRMED", "ORDER_SHIPPED", "ORDER_DELIVERED", "PROMO_OFFER", "NEW_PRODUCT"
    val title: String,
    val message: String,
    val badgeText: String = "",
    val actionRoute: String = "",
    val relatedEntityId: String = "",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val productTitle: String,
    val artisanId: String,
    val artisanName: String,
    val orderId: String,
    val buyerId: String,
    val buyerName: String,
    val overallRating: Float,
    val productQualityRating: Float,
    val packagingRating: Float,
    val deliveryRating: Float,
    val authenticityRating: Float,
    val reviewText: String,
    val isVoiceReview: Boolean = false,
    val voiceTranscript: String = "",
    val isVerifiedPurchase: Boolean = true,
    val buyerLocation: String = "Verified Buyer",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "product_drafts")
data class ProductDraftEntity(
    @PrimaryKey val id: String, // e.g. "draft_artisan_lakshmi"
    val artisanId: String,
    val artisanName: String = "Lakshmi Ammal",
    val title: String,
    val stepName: String, // "CAMERA_CAPTURE", "VOICE_DESCRIPTION", "CATALOG_REVIEW", "CRAFT_STORY", "CRAFT_ANALYZER", "SMART_PRICING", "FINAL_PREVIEW"
    val completionPercentage: Int, // e.g. 70
    val category: String = "Handloom & Textiles",
    val craftTechnique: String = "Korvai Interlock Handloom Weaving",
    val material: String = "Pure Mulberry Silk & Gold Zari Thread",
    val dimensions: String = "5.5m Length + 0.8m Blouse Piece",
    val productionTime: String = "5 Days (40 Hours dedicated artisan labor)",
    val rawMaterialCost: Double = 2100.0,
    val laborHours: Double = 40.0,
    val productionDays: Int = 5,
    val chosenPrice: Double = 3600.0,
    val stockQuantity: Int = 3,
    val capturedPhotoUri: String = "img_saree_sample",
    val enhancedPreset: String = "TRADITIONAL_INDIAN",
    val voiceTranscript: String = "",
    val descriptionEn: String = "",
    val descriptionHi: String = "",
    val storyHighlightBadge: String = "❤️ MADE WITH TRADITION",
    val storyHighlightSummary: String = "A weaving technique passed down through generations.",
    val storyFullText: String = "",
    val keywords: String = "Handloom, SilkSaree, Korvai, GI_Tag, ArtisanDirect",
    val isOfflineSaved: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "offline_sync_queue")
data class OfflineSyncQueueEntity(
    @PrimaryKey val id: String,
    val actionType: String, // "PRODUCT_DRAFT_PUBLISH", "VOICE_TRANSCRIBE", "IMAGE_ENHANCE", "ORDER_STATUS_UPDATE", "REVIEW_SUBMIT"
    val entityId: String,
    val payloadJson: String,
    val status: String = "PENDING", // "PENDING", "SYNCING", "COMPLETED", "FAILED"
    val retryCount: Int = 0,
    val errorMessage: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)

@Entity(tableName = "offline_voice_recordings")
data class OfflineVoiceRecordingEntity(
    @PrimaryKey val id: String,
    val artisanId: String,
    val audioFilePath: String,
    val recordedDurationSec: Int = 15,
    val localTranscript: String,
    val associatedImageUri: String = "",
    val productDraftId: String = "",
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings_cache")
data class AppSettingsCacheEntity(
    @PrimaryKey val id: String = "global_settings",
    val selectedLanguageCode: String = "en",
    val activeRole: String = "ARTISAN",
    val isSimpleMode: Boolean = false,
    val isOfflineModeForced: Boolean = false,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)


