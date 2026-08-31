package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProductEntity::class,
        ArtisanEntity::class,
        MaterialEntity::class,
        BuyerRequestEntity::class,
        ChatMessageEntity::class,
        OrderEntity::class,
        ArtisanNotificationEntity::class,
        CraftCircleEntity::class,
        CircleMemberEntity::class,
        CircleJoinRequestEntity::class,
        BulkOrderEntity::class,
        BulkAllocationEntity::class,
        CraftEventEntity::class,
        AppNotificationEntity::class,
        ReviewEntity::class,
        ProductDraftEntity::class,
        OfflineSyncQueueEntity::class,
        OfflineVoiceRecordingEntity::class,
        AppSettingsCacheEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun artisanDao(): ArtisanDao
    abstract fun materialDao(): MaterialDao
    abstract fun buyerRequestDao(): BuyerRequestDao
    abstract fun chatDao(): ChatDao
    abstract fun orderDao(): OrderDao
    abstract fun artisanNotificationDao(): ArtisanNotificationDao
    abstract fun craftCircleDao(): CraftCircleDao
    abstract fun circleMemberDao(): CircleMemberDao
    abstract fun circleJoinRequestDao(): CircleJoinRequestDao
    abstract fun bulkOrderDao(): BulkOrderDao
    abstract fun bulkAllocationDao(): BulkAllocationDao
    abstract fun craftEventDao(): CraftEventDao
    abstract fun appNotificationDao(): AppNotificationDao
    abstract fun reviewDao(): ReviewDao
    abstract fun productDraftDao(): ProductDraftDao
    abstract fun offlineSyncQueueDao(): OfflineSyncQueueDao
    abstract fun offlineVoiceRecordingDao(): OfflineVoiceRecordingDao
    abstract fun appSettingsCacheDao(): AppSettingsCacheDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "karigar_setu.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
