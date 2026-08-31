package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.models.*
import com.example.data.repository.CraftCircleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CraftCircleViewModel(
    private val repository: CraftCircleRepository
) : ViewModel() {

    val filteredCircles: StateFlow<List<CraftCircleEntity>> = repository.filteredCircles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCircles: StateFlow<List<CraftCircleEntity>> = repository.allCircles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filterState: StateFlow<CraftCircleFilterState> = repository.filterState

    val userLocation: StateFlow<String?> = repository.userLocation
    val isLocationPermissionGranted: StateFlow<Boolean> = repository.isLocationPermissionGranted

    val allBulkOrders: StateFlow<List<BulkOrderEntity>> = repository.allBulkOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Artisan view: my bulk allocations & invitations
    val myAllocations: StateFlow<List<BulkAllocationEntity>> = repository.getAllocationsForArtisan("artisan_a")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All allocations for active monitoring
    val allCircleAllocations: StateFlow<List<BulkAllocationEntity>> = repository.getAllocationsForCircle("circle_chennai_weavers")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCircle = MutableStateFlow<CraftCircleEntity?>(null)
    val selectedCircle = _selectedCircle.asStateFlow()

    private val _selectedCircleMembers = MutableStateFlow<List<CircleMemberEntity>>(emptyList())
    val selectedCircleMembers = _selectedCircleMembers.asStateFlow()

    private val _circleJoinRequests = MutableStateFlow<List<CircleJoinRequestEntity>>(emptyList())
    val circleJoinRequests = _circleJoinRequests.asStateFlow()

    private val _matchingRecommendations = MutableStateFlow<List<BulkOrderMatchRecommendation>>(emptyList())
    val matchingRecommendations = _matchingRecommendations.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    fun selectCircle(circle: CraftCircleEntity?) {
        _selectedCircle.value = circle
        if (circle != null) {
            viewModelScope.launch {
                repository.getMembersForCircle(circle.id).collect { members ->
                    _selectedCircleMembers.value = members
                }
            }
            viewModelScope.launch {
                repository.getJoinRequestsForCircle(circle.id).collect { requests ->
                    _circleJoinRequests.value = requests
                }
            }
        }
    }

    fun updateFilter(filter: CraftCircleFilterState) {
        repository.updateFilter(filter)
    }

    fun setUserLocation(location: String) {
        repository.setUserLocation(location)
    }

    fun setLocationPermissionGranted(granted: Boolean) {
        repository.setLocationPermissionGranted(granted)
    }

    fun submitJoinRequest(
        circle: CraftCircleEntity,
        formData: CircleJoinFormData,
        artisanId: String = "artisan_a",
        artisanName: String = "Lakshmi Ammal",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.submitJoinRequest(
                circleId = circle.id,
                circleName = circle.name,
                artisanId = artisanId,
                artisanName = artisanName,
                formData = formData
            )
            _statusMessage.value = "Your join request for ${circle.name} has been submitted for Admin verification!"
            onSuccess()
        }
    }

    fun approveJoinRequest(request: CircleJoinRequestEntity) {
        viewModelScope.launch {
            repository.approveJoinRequest(
                requestId = request.id,
                circleId = request.circleId,
                artisanId = request.artisanId,
                artisanName = request.artisanName,
                specialization = request.craftSpecialization,
                capacity = request.productionCapacityMonthly
            )
            _statusMessage.value = "Approved ${request.artisanName} into the Craft Circle!"
        }
    }

    fun calculateRecommendations(requestData: BulkOrderRequestData) {
        viewModelScope.launch {
            val circles = allCircles.value
            val membersMap = mutableMapOf<String, List<CircleMemberEntity>>()
            circles.forEach { circle ->
                repository.getMembersForCircle(circle.id).firstOrNull()?.let { members ->
                    membersMap[circle.id] = members
                }
            }
            val recs = repository.matchingService.findRecommendations(
                request = requestData,
                circles = circles,
                circleMembersMap = membersMap,
                userLocation = userLocation.value
            )
            _matchingRecommendations.value = recs
        }
    }

    fun createBulkOrder(
        circle: CraftCircleEntity,
        requestData: BulkOrderRequestData,
        customAllocations: List<BulkAllocationItem>?,
        buyerOrg: String = "Taj Group of Heritage Hotels",
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val orderId = repository.createBulkOrder(
                buyerId = "buyer_org_1",
                buyerName = "Procurement Lead",
                buyerOrg = buyerOrg,
                circleId = circle.id,
                circleName = circle.name,
                requestData = requestData,
                customAllocations = customAllocations
            )
            _statusMessage.value = "Bulk Order $orderId broadcasted to ${circle.name}! Invitations dispatched."
            onSuccess(orderId)
        }
    }

    fun respondToInvitation(allocationId: String, accepted: Boolean) {
        viewModelScope.launch {
            repository.respondToInvitation(allocationId, accepted)
            _statusMessage.value = if (accepted) "Invitation Accepted! Added to My Work." else "Invitation declined."
        }
    }

    fun updateProductionProgress(allocationId: String, progress: Int) {
        viewModelScope.launch {
            repository.updateProductionProgress(allocationId, progress)
            if (progress >= 100) {
                _statusMessage.value = "Production 100% Complete! Ready for dispatch."
            } else {
                _statusMessage.value = "Progress updated to $progress%"
            }
        }
    }

    fun markReady(allocationId: String) {
        viewModelScope.launch {
            repository.markAllocationReady(allocationId)
            _statusMessage.value = "Marked as Ready! Quality check dispatched & payout unlocked."
        }
    }

    fun updateAllocationQuantity(allocationId: String, newQty: Int, unitPayout: Double) {
        viewModelScope.launch {
            repository.updateMemberAllocation(allocationId, newQty, unitPayout)
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
