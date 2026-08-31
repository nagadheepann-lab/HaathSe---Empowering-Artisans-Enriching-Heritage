package com.example.data.service

import com.example.data.local.AppNotificationDao
import com.example.data.local.AppNotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

enum class ArtisanPushType(val title: String, val category: String) {
    NEW_ORDER("New Direct Buyer Order", "ORDERS"),
    PAYMENT("Direct Payout Credited", "PAYMENTS"),
    BULK_INVITATION("Craft Circle Bulk Invitation", "CRAFT_CIRCLES"),
    CIRCLE_APPROVAL("Circle Membership Approved", "CRAFT_CIRCLES"),
    INVENTORY_WARNING("Low Stock Inventory Warning", "INVENTORY"),
    DEMAND_INSIGHT("Market Demand Surge Insight", "MARKET_INSIGHTS"),
    TRADE_FAIR("National Trade Fair Stall Open", "EVENTS"),
    REVIEW("New 5-Star Buyer Review", "REVIEWS")
}

enum class BuyerPushType(val title: String, val category: String) {
    PAYMENT("Payment Confirmed", "PAYMENTS"),
    ORDER_CONFIRMATION("Order Accepted by Artisan", "ORDERS"),
    SHIPPING("Package Handed Over to Courier", "ORDERS"),
    DELIVERY("Package Delivered • Leave Review", "ORDERS"),
    PROMOTIONS("Festive Handloom Utsav 15% Off", "PROMOTIONS"),
    NEW_PRODUCTS("New Masterpiece Handcrafts Added", "PROMOTIONS")
}

/**
 * Firebase Cloud Messaging (FCM) abstraction service.
 * Supports push simulations, notification center streaming, and role-based filtering.
 */
class FcmNotificationService(
    private val notificationDao: AppNotificationDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    val allNotifications: Flow<List<AppNotificationEntity>> = notificationDao.getAllNotifications()

    fun getNotificationsForRole(role: String): Flow<List<AppNotificationEntity>> {
        return notificationDao.getNotificationsForRole(role)
    }

    fun getUnreadCountForRole(role: String): Flow<Int> {
        return notificationDao.getUnreadCountForRole(role)
    }

    fun markAsRead(id: String) {
        scope.launch {
            notificationDao.markAsRead(id)
        }
    }

    fun markAllAsReadForRole(role: String) {
        scope.launch {
            notificationDao.markAllAsReadForRole(role)
        }
    }

    fun deleteNotification(id: String) {
        scope.launch {
            notificationDao.deleteNotification(id)
        }
    }

    fun clearAllForRole(role: String) {
        scope.launch {
            notificationDao.clearNotificationsForRole(role)
        }
    }

    fun sendPushNotification(notification: AppNotificationEntity) {
        scope.launch {
            notificationDao.insertNotification(notification)
        }
    }

    /**
     * Triggers simulated push notifications for Artisan testing scenarios.
     */
    fun triggerArtisanPush(type: ArtisanPushType, artisanId: String = "artisan_lakshmi", artisanName: String = "Lakshmi Ammal") {
        val notification = when (type) {
            ArtisanPushType.NEW_ORDER -> AppNotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                recipientRole = "ARTISAN",
                recipientId = artisanId,
                category = "ORDERS",
                type = "NEW_ORDER",
                title = "New Direct Order #HS-984210",
                message = "Priya Sharma placed an order for 'Kanchipuram Temple Border Silk Saree' (₹8,500). Please accept and start weaving.",
                badgeText = "NEW ORDER",
                actionRoute = "ORDERS_STOCK",
                relatedEntityId = "HS-984210",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            ArtisanPushType.PAYMENT -> AppNotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                recipientRole = "ARTISAN",
                recipientId = artisanId,
                category = "PAYMENTS",
                type = "PAYMENT_RECEIVED",
                title = "Payment Credited: ₹14,200",
                message = "UPI Payout from KarigarSetu Escrow successfully deposited to your Canara Bank A/c ending in 8492. Zero platform deduction.",
                badgeText = "₹ PAID",
                actionRoute = "ARTISAN_PROFILE",
                relatedEntityId = "pay_9842",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            ArtisanPushType.BULK_INVITATION -> AppNotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                recipientRole = "ARTISAN",
                recipientId = artisanId,
                category = "CRAFT_CIRCLES",
                type = "BULK_INVITE",
                title = "B2B Bulk Order Allocation (₹95,000)",
                message = "Kanchipuram Master Weavers Guild allocated 100 sarees for Taj Palace Corporate Gifting. Review quota & tap to accept.",
                badgeText = "BULK INVITE",
                actionRoute = "CRAFT_CIRCLES",
                relatedEntityId = "alloc_kpm_taj",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            ArtisanPushType.CIRCLE_APPROVAL -> AppNotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                recipientRole = "ARTISAN",
                recipientId = artisanId,
                category = "CRAFT_CIRCLES",
                type = "CIRCLE_APPROVAL",
                title = "Craft Circle Membership Approved! ✓",
                message = "Congratulations! Your application to join 'Kanchipuram Master Weavers Guild' has been verified and approved by the administrator.",
                badgeText = "APPROVED",
                actionRoute = "CRAFT_CIRCLES",
                relatedEntityId = "circle_kpm_weavers",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            ArtisanPushType.INVENTORY_WARNING -> AppNotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                recipientRole = "ARTISAN",
                recipientId = artisanId,
                category = "INVENTORY",
                type = "LOW_STOCK",
                title = "Low Stock Warning: Only 2 units left",
                message = "Your listed item 'Natural Indigo Silk Stole' has 2 units remaining. High buyer traffic recorded this week.",
                badgeText = "LOW STOCK",
                actionRoute = "ARTISAN_PRODUCTS",
                relatedEntityId = "prod_stole_01",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            ArtisanPushType.DEMAND_INSIGHT -> AppNotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                recipientRole = "ARTISAN",
                recipientId = artisanId,
                category = "MARKET_INSIGHTS",
                type = "DEMAND_INSIGHT",
                title = "Demand Insight: Festive Sarees +45%",
                message = "Buyer searches for pure zari temple border sarees rose 45% ahead of Diwali. Suggested pricing: ₹9,200 – ₹11,500.",
                badgeText = "INSIGHT",
                actionRoute = "MARKET_PULSE",
                relatedEntityId = "insight_festive_saree",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            ArtisanPushType.TRADE_FAIR -> AppNotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                recipientRole = "ARTISAN",
                recipientId = artisanId,
                category = "EVENTS",
                type = "TRADE_FAIR",
                title = "Surajkund Crafts Mela: Free Stall Available",
                message = "Ministry of Textiles opened 100% subsidized stall applications for GI Tagged master artisans at Surajkund Mela 2026.",
                badgeText = "TRADE FAIR",
                actionRoute = "CRAFT_EVENTS",
                relatedEntityId = "evt_surajkund_2026",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            ArtisanPushType.REVIEW -> AppNotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                recipientRole = "ARTISAN",
                recipientId = artisanId,
                category = "REVIEWS",
                type = "NEW_REVIEW",
                title = "New 5.0 ★ Review from Radhika Iyer",
                message = "'Breathtaking silk quality and authentic handloom weight!' Your Artisan Trust Score increased by +2 points (now 98/100).",
                badgeText = "5.0 ★ REVIEW",
                actionRoute = "ARTISAN_PROFILE",
                relatedEntityId = "rev_1",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
        }
        sendPushNotification(notification)
    }

    /**
     * Triggers simulated push notifications for Buyer testing scenarios.
     */
    fun triggerBuyerPush(type: BuyerPushType, buyerId: String = "buyer_default") {
        val notification = when (type) {
            BuyerPushType.PAYMENT -> AppNotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                recipientRole = "BUYER",
                recipientId = buyerId,
                category = "PAYMENTS",
                type = "PAYMENT_CONFIRMED",
                title = "Payment Successful: ₹8,500",
                message = "Your Razorpay UPI transaction for Order #HS-984210 was successful. Funds safely placed in Karigar Escrow.",
                badgeText = "PAID ✓",
                actionRoute = "BUYER_ORDERS",
                relatedEntityId = "HS-984210",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            BuyerPushType.ORDER_CONFIRMATION -> AppNotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                recipientRole = "BUYER",
                recipientId = buyerId,
                category = "ORDERS",
                type = "ORDER_CONFIRMED",
                title = "Order Confirmed by Lakshmi Ammal",
                message = "Master weaver Lakshmi Ammal in Kanchipuram has accepted your order and began loom preparation. Estimated dispatch: 4 days.",
                badgeText = "CONFIRMED",
                actionRoute = "BUYER_ORDERS",
                relatedEntityId = "HS-984210",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            BuyerPushType.SHIPPING -> AppNotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                recipientRole = "BUYER",
                recipientId = buyerId,
                category = "ORDERS",
                type = "ORDER_SHIPPED",
                title = "Handcrafted Piece Shipped!",
                message = "Your authentic handcrafted saree has been dispatched via India Post Speed Post. Tracking #IN-POST-84920194.",
                badgeText = "SHIPPED",
                actionRoute = "BUYER_ORDERS",
                relatedEntityId = "HS-984210",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            BuyerPushType.DELIVERY -> AppNotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                recipientRole = "BUYER",
                recipientId = buyerId,
                category = "ORDERS",
                type = "ORDER_DELIVERED",
                title = "Delivered: How was your craft piece?",
                message = "Your order has been delivered! Please take 30 seconds to rate Lakshmi Ammal's craftsmanship and voice-record a review.",
                badgeText = "DELIVERED",
                actionRoute = "BUYER_ORDERS",
                relatedEntityId = "HS-984210",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            BuyerPushType.PROMOTIONS -> AppNotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                recipientRole = "BUYER",
                recipientId = buyerId,
                category = "PROMOTIONS",
                type = "PROMO_OFFER",
                title = "Festive Handloom Utsav: 15% Off",
                message = "Direct festival discount applied to all verified Kanchipuram, Chanderi, and Banarasi handlooms this festive week.",
                badgeText = "15% OFF",
                actionRoute = "BUYER_MARKETPLACE",
                relatedEntityId = "promo_festive",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            BuyerPushType.NEW_PRODUCTS -> AppNotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                recipientRole = "BUYER",
                recipientId = buyerId,
                category = "PROMOTIONS",
                type = "NEW_PRODUCT",
                title = "New Arrivals: Bastar Dhokra Art",
                message = "Master artisan Mangal Dhurwa has uploaded 4 newly cast Bell Metal elephant figurines with GI passports.",
                badgeText = "NEW ARRIVALS",
                actionRoute = "BUYER_MARKETPLACE",
                relatedEntityId = "prod_dhokra_01",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
        }
        sendPushNotification(notification)
    }
}
