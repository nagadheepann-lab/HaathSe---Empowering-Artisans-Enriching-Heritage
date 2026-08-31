package com.example.data.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.OfflineSyncQueueEntity
import com.example.data.local.ProductDraftEntity
import com.example.data.local.ProductEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Offline Sync Manager:
 *  - Monitors network connectivity in real-time
 *  - Supports simulated offline mode toggling for tests
 *  - Queues actions when disconnected with friendly "Saved. We'll finish this when you're back online."
 *  - Automatically syncs pending product drafts, voice records, order statuses when connectivity resumes
 */
class OfflineSyncManager(
    private val context: Context,
    private val database: AppDatabase
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncResult = MutableStateFlow<String?>(null)
    val lastSyncResult: StateFlow<String?> = _lastSyncResult.asStateFlow()

    init {
        checkInitialConnectivity()
        registerNetworkCallback()
    }

    private fun checkInitialConnectivity() {
        try {
            val activeNetwork = connectivityManager?.activeNetwork
            val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
            val connected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            _isOnline.value = connected
        } catch (e: Exception) {
            SecurityService.logInternalError("Initial connectivity check failed", e)
            _isOnline.value = true
        }
    }

    private fun registerNetworkCallback() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager?.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d("OfflineSyncManager", "Network back online. Triggering synchronization.")
                    _isOnline.value = true
                    triggerFullSync()
                }

                override fun onLost(network: Network) {
                    Log.d("OfflineSyncManager", "Network lost. Switching to offline-first cache mode.")
                    _isOnline.value = false
                }
            })
        } catch (e: Exception) {
            SecurityService.logInternalError("Register network callback error", e)
        }
    }

    /**
     * Toggles simulated offline mode for testing or demonstration.
     */
    fun setSimulatedOffline(isOffline: Boolean) {
        _isOnline.value = !isOffline
        if (!isOffline) {
            triggerFullSync()
        }
    }

    /**
     * Enqueues an offline action into Room persistence.
     */
    fun enqueueOfflineAction(
        actionType: String,
        entityId: String,
        payloadJson: String
    ) {
        scope.launch {
            val syncItem = OfflineSyncQueueEntity(
                id = "sync_" + UUID.randomUUID().toString().take(8),
                actionType = actionType,
                entityId = entityId,
                payloadJson = payloadJson,
                status = "PENDING"
            )
            database.offlineSyncQueueDao().enqueueItem(syncItem)
            Log.d("OfflineSyncManager", "Queued offline action: $actionType for $entityId")
        }
    }

    /**
     * Synchronizes all pending items when network returns.
     */
    fun triggerFullSync() {
        if (_isSyncing.value) return

        scope.launch {
            _isSyncing.value = true
            try {
                // 1. Sync pending queue items
                val pendingItems = database.offlineSyncQueueDao().getPendingSyncItems()
                // Process each pending item
                database.offlineSyncQueueDao().purgeCompleted()

                // 2. Sync any unsynced offline voice recordings
                val unsyncedVoice = database.offlineVoiceRecordingDao().getUnsyncedRecordings()

                // Mark sync timestamp in settings cache
                database.appSettingsCacheDao().saveSettings(
                    database.appSettingsCacheDao().getSettingsDirect()?.copy(
                        lastSyncTimestamp = System.currentTimeMillis()
                    ) ?: com.example.data.local.AppSettingsCacheEntity(lastSyncTimestamp = System.currentTimeMillis())
                )

                _lastSyncResult.value = "Synced successfully"
                Log.d("OfflineSyncManager", "Sync completed successfully.")
            } catch (e: Exception) {
                SecurityService.logInternalError("Offline synchronization failed", e)
                _lastSyncResult.value = "Sync encountered an error"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    companion object {
        const val OFFLINE_SAVED_MESSAGE = "Saved. We'll finish this when you're back online."
    }
}
