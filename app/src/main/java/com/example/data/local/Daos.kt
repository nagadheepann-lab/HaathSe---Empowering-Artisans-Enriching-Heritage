package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isPublished = 1 ORDER BY createdAt DESC")
    fun getPublishedProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE artisanId = :artisanId ORDER BY createdAt DESC")
    fun getProductsByArtisan(artisanId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET stockQuantity = :newStock WHERE id = :id")
    suspend fun updateStock(id: String, newStock: Int)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: String)
}

@Dao
interface ArtisanDao {
    @Query("SELECT * FROM artisans")
    fun getAllArtisans(): Flow<List<ArtisanEntity>>

    @Query("SELECT * FROM artisans WHERE id = :id")
    suspend fun getArtisanById(id: String): ArtisanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtisans(artisans: List<ArtisanEntity>)

    @Update
    suspend fun updateArtisan(artisan: ArtisanEntity)
}

@Dao
interface MaterialDao {
    @Query("SELECT * FROM materials ORDER BY dateAdded DESC")
    fun getAllMaterials(): Flow<List<MaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: MaterialEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterials(materials: List<MaterialEntity>)

    @Query("DELETE FROM materials WHERE id = :id")
    suspend fun deleteMaterial(id: String)
}

@Dao
interface BuyerRequestDao {
    @Query("SELECT * FROM buyer_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<BuyerRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: BuyerRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<BuyerRequestEntity>)

    @Update
    suspend fun updateRequest(request: BuyerRequestEntity)

    @Query("UPDATE buyer_requests SET status = :status, counterPrice = :counterPrice WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, counterPrice: Double)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE artisanId = :artisanId ORDER BY createdAt DESC")
    fun getOrdersByArtisan(artisanId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE buyerId = :buyerId ORDER BY createdAt DESC")
    fun getOrdersByBuyer(buyerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET orderState = :orderState, artisanStatus = :artisanStatus, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateOrderStatus(id: String, orderState: String, artisanStatus: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET paymentState = :paymentState, razorpayPaymentId = :razorpayPaymentId, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePaymentStatus(id: String, paymentState: String, razorpayPaymentId: String, updatedAt: Long = System.currentTimeMillis())
}

@Dao
interface ArtisanNotificationDao {
    @Query("SELECT * FROM artisan_notifications WHERE artisanId = :artisanId ORDER BY timestamp DESC")
    fun getNotificationsForArtisan(artisanId: String): Flow<List<ArtisanNotificationEntity>>

    @Query("SELECT * FROM artisan_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<ArtisanNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: ArtisanNotificationEntity)

    @Query("UPDATE artisan_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)
}

@Dao
interface CraftCircleDao {
    @Query("SELECT * FROM craft_circles ORDER BY trustScore DESC")
    fun getAllCircles(): Flow<List<CraftCircleEntity>>

    @Query("SELECT * FROM craft_circles WHERE id = :id")
    suspend fun getCircleById(id: String): CraftCircleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCircles(circles: List<CraftCircleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCircle(circle: CraftCircleEntity)

    @Update
    suspend fun updateCircle(circle: CraftCircleEntity)
}

@Dao
interface CircleMemberDao {
    @Query("SELECT * FROM circle_members WHERE circleId = :circleId ORDER BY trustScore DESC")
    fun getMembersForCircle(circleId: String): Flow<List<CircleMemberEntity>>

    @Query("SELECT * FROM circle_members WHERE artisanId = :artisanId")
    fun getCirclesForArtisan(artisanId: String): Flow<List<CircleMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<CircleMemberEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: CircleMemberEntity)
}

@Dao
interface CircleJoinRequestDao {
    @Query("SELECT * FROM circle_join_requests WHERE circleId = :circleId ORDER BY submittedAt DESC")
    fun getRequestsForCircle(circleId: String): Flow<List<CircleJoinRequestEntity>>

    @Query("SELECT * FROM circle_join_requests WHERE artisanId = :artisanId ORDER BY submittedAt DESC")
    fun getRequestsByArtisan(artisanId: String): Flow<List<CircleJoinRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: CircleJoinRequestEntity)

    @Query("UPDATE circle_join_requests SET status = :status WHERE id = :id")
    suspend fun updateRequestStatus(id: String, status: String)
}

@Dao
interface BulkOrderDao {
    @Query("SELECT * FROM bulk_orders ORDER BY createdAt DESC")
    fun getAllBulkOrders(): Flow<List<BulkOrderEntity>>

    @Query("SELECT * FROM bulk_orders WHERE circleId = :circleId ORDER BY createdAt DESC")
    fun getBulkOrdersForCircle(circleId: String): Flow<List<BulkOrderEntity>>

    @Query("SELECT * FROM bulk_orders WHERE buyerId = :buyerId ORDER BY createdAt DESC")
    fun getBulkOrdersForBuyer(buyerId: String): Flow<List<BulkOrderEntity>>

    @Query("SELECT * FROM bulk_orders WHERE id = :id")
    suspend fun getBulkOrderById(id: String): BulkOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBulkOrder(order: BulkOrderEntity)

    @Update
    suspend fun updateBulkOrder(order: BulkOrderEntity)

    @Query("UPDATE bulk_orders SET status = :status, fulfillmentProgress = :progress WHERE id = :id")
    suspend fun updateBulkOrderStatus(id: String, status: String, progress: Int)
}

@Dao
interface BulkAllocationDao {
    @Query("SELECT * FROM bulk_allocations WHERE artisanId = :artisanId ORDER BY updatedAt DESC")
    fun getAllocationsForArtisan(artisanId: String): Flow<List<BulkAllocationEntity>>

    @Query("SELECT * FROM bulk_allocations WHERE bulkOrderId = :bulkOrderId ORDER BY allocatedQuantity DESC")
    fun getAllocationsForOrder(bulkOrderId: String): Flow<List<BulkAllocationEntity>>

    @Query("SELECT * FROM bulk_allocations WHERE circleId = :circleId ORDER BY updatedAt DESC")
    fun getAllocationsForCircle(circleId: String): Flow<List<BulkAllocationEntity>>

    @Query("SELECT * FROM bulk_allocations WHERE id = :id")
    suspend fun getAllocationById(id: String): BulkAllocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocations(allocations: List<BulkAllocationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocation(allocation: BulkAllocationEntity)

    @Update
    suspend fun updateAllocation(allocation: BulkAllocationEntity)

    @Query("UPDATE bulk_allocations SET invitationStatus = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateInvitationStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE bulk_allocations SET productionProgress = :progress, isReadyForDispatch = :isReady, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int, isReady: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE bulk_allocations SET allocatedQuantity = :qty, estimatedPayout = :payout, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateAllocationQuantity(id: String, qty: Int, payout: Double, updatedAt: Long = System.currentTimeMillis())
}

@Dao
interface CraftEventDao {
    @Query("SELECT * FROM craft_events ORDER BY createdAt ASC")
    fun getAllEvents(): Flow<List<CraftEventEntity>>

    @Query("SELECT * FROM craft_events WHERE id = :id")
    suspend fun getEventById(id: String): CraftEventEntity?

    @Query("SELECT * FROM craft_events WHERE eventType = :type ORDER BY createdAt ASC")
    fun getEventsByType(type: String): Flow<List<CraftEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CraftEventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CraftEventEntity)

    @Update
    suspend fun updateEvent(event: CraftEventEntity)

    @Query("UPDATE craft_events SET isRegistered = :isRegistered, registrationStatus = :status WHERE id = :id")
    suspend fun updateRegistrationStatus(id: String, isRegistered: Boolean, status: String)
}

@Dao
interface AppNotificationDao {
    @Query("SELECT * FROM app_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<AppNotificationEntity>>

    @Query("SELECT * FROM app_notifications WHERE recipientRole = :role ORDER BY timestamp DESC")
    fun getNotificationsForRole(role: String): Flow<List<AppNotificationEntity>>

    @Query("SELECT * FROM app_notifications WHERE recipientId = :recipientId ORDER BY timestamp DESC")
    fun getNotificationsForUser(recipientId: String): Flow<List<AppNotificationEntity>>

    @Query("SELECT COUNT(*) FROM app_notifications WHERE recipientRole = :role AND isRead = 0")
    fun getUnreadCountForRole(role: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<AppNotificationEntity>)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE recipientRole = :role")
    suspend fun markAllAsReadForRole(role: String)

    @Query("DELETE FROM app_notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)

    @Query("DELETE FROM app_notifications WHERE recipientRole = :role")
    suspend fun clearNotificationsForRole(role: String)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE productId = :productId ORDER BY createdAt DESC")
    fun getReviewsForProduct(productId: String): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE artisanId = :artisanId ORDER BY createdAt DESC")
    fun getReviewsForArtisan(artisanId: String): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<ReviewEntity>)

    @Query("SELECT AVG(overallRating) FROM reviews WHERE artisanId = :artisanId")
    suspend fun getAverageRatingForArtisan(artisanId: String): Float?

    @Query("SELECT COUNT(*) FROM reviews WHERE artisanId = :artisanId")
    suspend fun getReviewCountForArtisan(artisanId: String): Int
}

@Dao
interface ProductDraftDao {
    @Query("SELECT * FROM product_drafts WHERE artisanId = :artisanId ORDER BY updatedAt DESC")
    fun getDraftsForArtisan(artisanId: String): Flow<List<ProductDraftEntity>>

    @Query("SELECT * FROM product_drafts WHERE artisanId = :artisanId ORDER BY updatedAt DESC LIMIT 1")
    fun getLatestDraft(artisanId: String): Flow<ProductDraftEntity?>

    @Query("SELECT * FROM product_drafts WHERE id = :id")
    suspend fun getDraftById(id: String): ProductDraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: ProductDraftEntity)

    @Query("DELETE FROM product_drafts WHERE id = :id")
    suspend fun deleteDraft(id: String)

    @Query("DELETE FROM product_drafts WHERE artisanId = :artisanId")
    suspend fun clearDraftsForArtisan(artisanId: String)
}

@Dao
interface OfflineSyncQueueDao {
    @Query("SELECT * FROM offline_sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun getPendingSyncItems(): Flow<List<OfflineSyncQueueEntity>>

    @Query("SELECT * FROM offline_sync_queue ORDER BY createdAt DESC")
    fun getAllSyncItems(): Flow<List<OfflineSyncQueueEntity>>

    @Query("SELECT COUNT(*) FROM offline_sync_queue WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueItem(item: OfflineSyncQueueEntity)

    @Update
    suspend fun updateItem(item: OfflineSyncQueueEntity)

    @Query("UPDATE offline_sync_queue SET status = :status, syncedAt = :syncedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, syncedAt: Long? = System.currentTimeMillis())

    @Query("DELETE FROM offline_sync_queue WHERE id = :id")
    suspend fun deleteItem(id: String)

    @Query("DELETE FROM offline_sync_queue WHERE status = 'COMPLETED'")
    suspend fun purgeCompleted()
}

@Dao
interface OfflineVoiceRecordingDao {
    @Query("SELECT * FROM offline_voice_recordings WHERE artisanId = :artisanId ORDER BY createdAt DESC")
    fun getRecordingsForArtisan(artisanId: String): Flow<List<OfflineVoiceRecordingEntity>>

    @Query("SELECT * FROM offline_voice_recordings WHERE isSynced = 0")
    fun getUnsyncedRecordings(): Flow<List<OfflineVoiceRecordingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: OfflineVoiceRecordingEntity)

    @Query("UPDATE offline_voice_recordings SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("DELETE FROM offline_voice_recordings WHERE id = :id")
    suspend fun deleteRecording(id: String)
}

@Dao
interface AppSettingsCacheDao {
    @Query("SELECT * FROM app_settings_cache WHERE id = 'global_settings' LIMIT 1")
    fun getSettings(): Flow<AppSettingsCacheEntity?>

    @Query("SELECT * FROM app_settings_cache WHERE id = 'global_settings' LIMIT 1")
    suspend fun getSettingsDirect(): AppSettingsCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AppSettingsCacheEntity)
}


