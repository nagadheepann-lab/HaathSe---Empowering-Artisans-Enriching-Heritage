package com.example.data.repository

import com.example.data.local.*
import com.example.data.models.*
import com.example.data.service.BulkOrderMatchingService
import com.example.data.service.StandardBulkOrderMatchingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class CraftCircleRepository(
    private val circleDao: CraftCircleDao,
    private val memberDao: CircleMemberDao,
    private val joinRequestDao: CircleJoinRequestDao,
    private val bulkOrderDao: BulkOrderDao,
    private val bulkAllocationDao: BulkAllocationDao,
    private val notificationDao: ArtisanNotificationDao,
    val matchingService: BulkOrderMatchingService = StandardBulkOrderMatchingService()
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    val allCircles: Flow<List<CraftCircleEntity>> = circleDao.getAllCircles()
    val allBulkOrders: Flow<List<BulkOrderEntity>> = bulkOrderDao.getAllBulkOrders()

    private val _filterState = MutableStateFlow(CraftCircleFilterState())
    val filterState = _filterState.asStateFlow()

    private val _userLocation = MutableStateFlow<String?>("Chennai, Tamil Nadu")
    val userLocation = _userLocation.asStateFlow()

    private val _isLocationPermissionGranted = MutableStateFlow(false)
    val isLocationPermissionGranted = _isLocationPermissionGranted.asStateFlow()

    val filteredCircles: Flow<List<CraftCircleEntity>> = combine(
        allCircles,
        _filterState
    ) { circles, filter ->
        circles.filter { circle ->
            val matchesQuery = filter.searchQuery.isBlank() ||
                    circle.name.contains(filter.searchQuery, ignoreCase = true) ||
                    circle.craftType.contains(filter.searchQuery, ignoreCase = true) ||
                    circle.location.contains(filter.searchQuery, ignoreCase = true) ||
                    circle.specialization.contains(filter.searchQuery, ignoreCase = true)

            val matchesCraft = filter.selectedCraft == null ||
                    circle.craftType.contains(filter.selectedCraft, ignoreCase = true) ||
                    circle.specialization.contains(filter.selectedCraft, ignoreCase = true)

            val matchesLocation = filter.selectedLocation == null ||
                    circle.location.contains(filter.selectedLocation, ignoreCase = true)

            val matchesDistance = circle.distanceKm <= filter.maxDistanceKm

            val matchesCapacity = circle.availableCapacityUnits >= filter.minCapacity

            val matchesAvailability = !filter.availableOnly || circle.isAvailableForBulk

            val matchesTrust = circle.trustScore >= filter.minTrustScore

            matchesQuery && matchesCraft && matchesLocation && matchesDistance && matchesCapacity && matchesAvailability && matchesTrust
        }
    }

    init {
        scope.launch {
            seedInitialCircleData()
        }
    }

    fun updateFilter(newFilter: CraftCircleFilterState) {
        _filterState.value = newFilter
    }

    fun setUserLocation(location: String) {
        _userLocation.value = location
    }

    fun setLocationPermissionGranted(granted: Boolean) {
        _isLocationPermissionGranted.value = granted
        if (granted) {
            _userLocation.value = "Chennai, Tamil Nadu"
        }
    }

    fun getMembersForCircle(circleId: String): Flow<List<CircleMemberEntity>> {
        return memberDao.getMembersForCircle(circleId)
    }

    fun getJoinRequestsForCircle(circleId: String): Flow<List<CircleJoinRequestEntity>> {
        return joinRequestDao.getRequestsForCircle(circleId)
    }

    fun getAllocationsForArtisan(artisanId: String): Flow<List<BulkAllocationEntity>> {
        return bulkAllocationDao.getAllocationsForArtisan(artisanId)
    }

    fun getAllocationsForCircle(circleId: String): Flow<List<BulkAllocationEntity>> {
        return bulkAllocationDao.getAllocationsForCircle(circleId)
    }

    fun getAllocationsForOrder(bulkOrderId: String): Flow<List<BulkAllocationEntity>> {
        return bulkAllocationDao.getAllocationsForOrder(bulkOrderId)
    }

    suspend fun getCircleById(circleId: String): CraftCircleEntity? {
        return circleDao.getCircleById(circleId)
    }

    suspend fun submitJoinRequest(
        circleId: String,
        circleName: String,
        artisanId: String,
        artisanName: String,
        formData: CircleJoinFormData
    ) {
        val request = CircleJoinRequestEntity(
            id = "JR-${UUID.randomUUID().toString().take(8).uppercase()}",
            circleId = circleId,
            circleName = circleName,
            artisanId = artisanId,
            artisanName = artisanName,
            craftSpecialization = formData.craftSpecialization,
            experienceYears = formData.experienceYears,
            previousWorkDesc = formData.previousWorkDesc,
            productionCapacityMonthly = formData.productionCapacityMonthly,
            availabilityTimeline = formData.availabilityTimeline,
            location = formData.location,
            portfolioImagesCount = formData.portfolioImages.size,
            status = "PENDING"
        )
        joinRequestDao.insertRequest(request)
    }

    suspend fun approveJoinRequest(requestId: String, circleId: String, artisanId: String, artisanName: String, specialization: String, capacity: Int) {
        joinRequestDao.updateRequestStatus(requestId, "APPROVED")
        val newMember = CircleMemberEntity(
            id = "mem_${UUID.randomUUID().toString().take(6)}",
            circleId = circleId,
            artisanId = artisanId,
            artisanName = artisanName,
            craftSpecialization = specialization,
            experienceYears = 8,
            monthlyCapacityUnits = capacity,
            currentLoadUnits = 0,
            availableCapacityUnits = capacity,
            trustScore = 92,
            performanceRating = 4.8f,
            fulfillmentHistoryCount = 12,
            avatarRes = "avatar_lakshmi",
            role = "Artisan Member",
            status = "ACTIVE"
        )
        memberDao.insertMember(newMember)

        // Update member count
        val circle = circleDao.getCircleById(circleId)
        if (circle != null) {
            circleDao.updateCircle(
                circle.copy(
                    memberCount = circle.memberCount + 1,
                    monthlyCapacityUnits = circle.monthlyCapacityUnits + capacity,
                    availableCapacityUnits = circle.availableCapacityUnits + capacity
                )
            )
        }
    }

    suspend fun createBulkOrder(
        buyerId: String,
        buyerName: String,
        buyerOrg: String,
        circleId: String,
        circleName: String,
        requestData: BulkOrderRequestData,
        customAllocations: List<BulkAllocationItem>? = null
    ): String {
        val orderId = "BO-${(100000..999999).random()}"
        val bulkOrder = BulkOrderEntity(
            id = orderId,
            buyerId = buyerId,
            buyerName = buyerName,
            buyerOrg = buyerOrg,
            circleId = circleId,
            circleName = circleName,
            productRequirement = requestData.productRequirement,
            craftCategory = requestData.craftCategory,
            quantity = requestData.quantity,
            allocatedQuantity = requestData.quantity,
            totalBudget = requestData.totalBudget,
            unitBudget = requestData.targetUnitPrice,
            deadline = requestData.deadlineDate,
            customizationNotes = requestData.customizationNotes,
            technicalRequirements = requestData.technicalRequirements,
            status = "ALLOCATED",
            fulfillmentProgress = 0
        )
        bulkOrderDao.insertBulkOrder(bulkOrder)

        // Create Allocations
        val allocationsToSave = if (!customAllocations.isNullOrEmpty()) {
            customAllocations.map { item ->
                BulkAllocationEntity(
                    id = "alloc_${UUID.randomUUID().toString().take(8)}",
                    bulkOrderId = orderId,
                    circleId = circleId,
                    circleName = circleName,
                    productRequirement = requestData.productRequirement,
                    buyerOrg = buyerOrg,
                    artisanId = item.artisanId,
                    artisanName = item.artisanName,
                    artisanAvatar = item.avatarRes,
                    allocatedQuantity = item.allocatedQuantity,
                    unitPayout = item.unitPayout,
                    estimatedPayout = item.estimatedPayout,
                    deadline = requestData.deadlineDate,
                    invitationStatus = "PENDING",
                    productionProgress = 0,
                    isReadyForDispatch = false,
                    aiReasoning = item.aiReasoning
                )
            }
        } else {
            emptyList()
        }

        if (allocationsToSave.isNotEmpty()) {
            bulkAllocationDao.insertAllocations(allocationsToSave)

            // Notify artisans
            allocationsToSave.forEach { alloc ->
                notificationDao.insertNotification(
                    ArtisanNotificationEntity(
                        id = "notif_${UUID.randomUUID().toString().take(8)}",
                        artisanId = alloc.artisanId,
                        orderId = orderId,
                        title = "New Craft Circle Bulk Invitation 🏛️",
                        message = "${buyerOrg} requested ${alloc.allocatedQuantity} pcs via ${circleName}. Est. Payout: ₹${alloc.estimatedPayout.toInt()}",
                        productTitle = alloc.productRequirement,
                        quantity = alloc.allocatedQuantity,
                        orderValue = alloc.estimatedPayout,
                        languageCode = "en"
                    )
                )
            }
        }

        // Update circle active orders
        val circle = circleDao.getCircleById(circleId)
        if (circle != null) {
            circleDao.updateCircle(
                circle.copy(
                    activeBulkOrders = circle.activeBulkOrders + 1,
                    availableCapacityUnits = (circle.availableCapacityUnits - requestData.quantity).coerceAtLeast(0)
                )
            )
        }

        return orderId
    }

    suspend fun respondToInvitation(allocationId: String, accepted: Boolean) {
        val status = if (accepted) "ACCEPTED" else "DECLINED"
        bulkAllocationDao.updateInvitationStatus(allocationId, status)
    }

    suspend fun updateProductionProgress(allocationId: String, progress: Int) {
        val isReady = progress >= 100
        bulkAllocationDao.updateProgress(allocationId, progress, isReady)

        // Check if all allocations for the bulk order are progressing
        val alloc = bulkAllocationDao.getAllocationById(allocationId)
        if (alloc != null) {
            val allOrderAllocs = bulkAllocationDao.getAllocationById(allocationId)?.let {
                // simple progress check
            }
        }
    }

    suspend fun markAllocationReady(allocationId: String) {
        bulkAllocationDao.updateProgress(allocationId, 100, true)
    }

    suspend fun updateMemberAllocation(allocationId: String, newQuantity: Int, unitPayout: Double) {
        val newPayout = newQuantity * unitPayout
        bulkAllocationDao.updateAllocationQuantity(allocationId, newQuantity, newPayout)
    }

    private suspend fun seedInitialCircleData() {
        val existing = circleDao.getCircleById("circle_chennai_weavers")
        if (existing != null) return

        val seedCircles = listOf(
            CraftCircleEntity(
                id = "circle_chennai_weavers",
                name = "Chennai Weavers Circle",
                craftType = "Handloom",
                specialization = "Mulberry Silk, Zari & Organic Cotton",
                story = "A collective of traditional master handloom weavers across Kanchipuram and Chennai corridors, preserving 8th-century pit-loom motifs while executing large institutional hospitality orders.",
                location = "Chennai, Tamil Nadu",
                latitude = 13.0827,
                longitude = 80.2707,
                distanceKm = 8.5,
                memberCount = 42,
                monthlyCapacityUnits = 180,
                availableCapacityUnits = 180,
                activeBulkOrders = 3,
                completedBulkOrders = 48,
                trustScore = 96,
                adminId = "artisan_1",
                adminName = "Master G. Ramanathan",
                adminPhone = "+91 98401 22891",
                adminAvatar = "avatar_lakshmi",
                imageRes = "img_saree_sample",
                isAvailableForBulk = true,
                minOrderQuantity = 25,
                avgFulfillmentDays = 20
            ),
            CraftCircleEntity(
                id = "circle_jaipur_bluepottery",
                name = "Jaipur Royal Blue Pottery Guild",
                craftType = "Blue Pottery",
                specialization = "Crushed Quartz Glazed Ceramic Homeware",
                story = "Generational Rajasthani artisans crafting traditional Egyptian blue glazed ceramic platters, tilework, and tableware using authentic quartz and natural multani mitti.",
                location = "Jaipur, Rajasthan",
                latitude = 26.9124,
                longitude = 75.7873,
                distanceKm = 48.0,
                memberCount = 28,
                monthlyCapacityUnits = 350,
                availableCapacityUnits = 240,
                activeBulkOrders = 2,
                completedBulkOrders = 64,
                trustScore = 98,
                adminId = "artisan_2",
                adminName = "Ustad Rahimuddin",
                adminPhone = "+91 98290 54120",
                adminAvatar = "avatar_ramesh",
                imageRes = "img_pottery_sample",
                isAvailableForBulk = true,
                minOrderQuantity = 50,
                avgFulfillmentDays = 18
            ),
            CraftCircleEntity(
                id = "circle_bastar_dhokra",
                name = "Bastar Lost-Wax Dhokra Metal Guild",
                craftType = "Dhokra Metalcraft",
                specialization = "Ancient Lost-Wax Brass Casting & Sculptures",
                story = "4,000-year-old indigenous metal casting guild transforming recycled brass scrap and beeswax into intricate tribal art, corporate trophies, and bespoke architectural accents.",
                location = "Bastar, Chhattisgarh",
                latitude = 19.1071,
                longitude = 81.9535,
                distanceKm = 320.0,
                memberCount = 35,
                monthlyCapacityUnits = 150,
                availableCapacityUnits = 110,
                activeBulkOrders = 4,
                completedBulkOrders = 32,
                trustScore = 94,
                adminId = "artisan_3",
                adminName = "Devi Bai Dhokra",
                adminPhone = "+91 94252 87102",
                adminAvatar = "avatar_devi",
                imageRes = "img_brass_dhokra",
                isAvailableForBulk = true,
                minOrderQuantity = 20,
                avgFulfillmentDays = 25
            ),
            CraftCircleEntity(
                id = "circle_channapatna_wood",
                name = "Channapatna Lacquered Toycraft Guild",
                craftType = "Woodcraft",
                specialization = "Natural Vegetable Dyes & Turned Wrightia Wood",
                story = "Known as Gombegala Ooru (Toy Town), this GI-tagged artisans circle crafts eco-friendly, non-toxic educational toys, corporate desk decor, and turned wood games.",
                location = "Channapatna, Karnataka",
                latitude = 12.6518,
                longitude = 77.2089,
                distanceKm = 145.0,
                memberCount = 52,
                monthlyCapacityUnits = 800,
                availableCapacityUnits = 550,
                activeBulkOrders = 5,
                completedBulkOrders = 110,
                trustScore = 97,
                adminId = "artisan_4",
                adminName = "Syed Khaleel",
                adminPhone = "+91 98860 31456",
                adminAvatar = "avatar_ramesh",
                imageRes = "img_wood_craft",
                isAvailableForBulk = true,
                minOrderQuantity = 50,
                avgFulfillmentDays = 14
            ),
            CraftCircleEntity(
                id = "circle_varanasi_silk",
                name = "Varanasi Heritage Kadhwa Weavers",
                craftType = "Handloom",
                specialization = "Banarasi Brocade & Kadhwa Silk Weaving",
                story = "Master pitloom weavers from the historic ghats of Kashi, specializing in opulent zari borders, raw silk dupion stoles, and ceremonial yardage.",
                location = "Varanasi, Uttar Pradesh",
                latitude = 25.3176,
                longitude = 82.9739,
                distanceKm = 680.0,
                memberCount = 65,
                monthlyCapacityUnits = 400,
                availableCapacityUnits = 280,
                activeBulkOrders = 6,
                completedBulkOrders = 145,
                trustScore = 99,
                adminId = "artisan_5",
                adminName = "Maqbool Ansari",
                adminPhone = "+91 94150 99823",
                adminAvatar = "avatar_lakshmi",
                imageRes = "img_saree_sample",
                isAvailableForBulk = true,
                minOrderQuantity = 30,
                avgFulfillmentDays = 22
            )
        )
        circleDao.insertCircles(seedCircles)

        // Seed Members for Chennai Weavers Circle
        val chennaiMembers = listOf(
            CircleMemberEntity(
                id = "cm_chennai_1",
                circleId = "circle_chennai_weavers",
                artisanId = "artisan_a",
                artisanName = "Artisan A (Lakshmi Ammal)",
                craftSpecialization = "Mulberry Silk & Pure Zari Weaving",
                experienceYears = 24,
                monthlyCapacityUnits = 100,
                currentLoadUnits = 20,
                availableCapacityUnits = 80,
                trustScore = 98,
                performanceRating = 4.9f,
                fulfillmentHistoryCount = 68,
                avatarRes = "avatar_lakshmi",
                role = "Lead Master Weaver",
                status = "ACTIVE"
            ),
            CircleMemberEntity(
                id = "cm_chennai_2",
                circleId = "circle_chennai_weavers",
                artisanId = "artisan_b",
                artisanName = "Artisan B (Ramesh Babu)",
                craftSpecialization = "Fine Jacquard & Border Motif Handloom",
                experienceYears = 18,
                monthlyCapacityUnits = 80,
                currentLoadUnits = 10,
                availableCapacityUnits = 70,
                trustScore = 96,
                performanceRating = 4.8f,
                fulfillmentHistoryCount = 42,
                avatarRes = "avatar_ramesh",
                role = "Senior Artisan",
                status = "ACTIVE"
            ),
            CircleMemberEntity(
                id = "cm_chennai_3",
                circleId = "circle_chennai_weavers",
                artisanId = "artisan_c",
                artisanName = "Artisan C (Sundaram Murthy)",
                craftSpecialization = "Raw Silk Yardage & Organic Cotton Stoles",
                experienceYears = 22,
                monthlyCapacityUnits = 120,
                currentLoadUnits = 15,
                availableCapacityUnits = 105,
                trustScore = 97,
                performanceRating = 4.9f,
                fulfillmentHistoryCount = 55,
                avatarRes = "avatar_ramesh",
                role = "Senior Artisan",
                status = "ACTIVE"
            ),
            CircleMemberEntity(
                id = "cm_chennai_4",
                circleId = "circle_chennai_weavers",
                artisanId = "artisan_d",
                artisanName = "Artisan D (Kavitha Raman)",
                craftSpecialization = "Natural Indigo Dyeing & Pit-loom Weaving",
                experienceYears = 14,
                monthlyCapacityUnits = 100,
                currentLoadUnits = 10,
                availableCapacityUnits = 90,
                trustScore = 95,
                performanceRating = 4.7f,
                fulfillmentHistoryCount = 38,
                avatarRes = "avatar_devi",
                role = "Artisan Member",
                status = "ACTIVE"
            ),
            CircleMemberEntity(
                id = "cm_chennai_5",
                circleId = "circle_chennai_weavers",
                artisanId = "artisan_e",
                artisanName = "Artisan E (Anand Vel)",
                craftSpecialization = "Double Warp Handloom & Tassel Finishing",
                experienceYears = 16,
                monthlyCapacityUnits = 100,
                currentLoadUnits = 10,
                availableCapacityUnits = 90,
                trustScore = 94,
                performanceRating = 4.8f,
                fulfillmentHistoryCount = 34,
                avatarRes = "avatar_ramesh",
                role = "Artisan Member",
                status = "ACTIVE"
            )
        )
        memberDao.insertMembers(chennaiMembers)

        // Seed an initial active Bulk Order and Allocations
        val sampleOrderId = "BO-849201"
        val sampleBulkOrder = BulkOrderEntity(
            id = sampleOrderId,
            buyerId = "buyer_taj_hotels",
            buyerName = "Mr. Vikramaditya Rathore",
            buyerOrg = "Taj Group of Heritage Hotels",
            circleId = "circle_chennai_weavers",
            circleName = "Chennai Weavers Circle",
            productRequirement = "500 Handwoven Raw Mulberry Silk Stoles with Temple Zari Border",
            craftCategory = "Handloom",
            quantity = 500,
            allocatedQuantity = 500,
            totalBudget = 450000.0,
            unitBudget = 900.0,
            deadline = "30 Oct 2026",
            customizationNotes = "Gold zari temple motif border, custom hand-embroidered Taj heritage logo tag, individual eco-friendly bamboo gift boxes.",
            technicalRequirements = "100% natural silk warp & weft, certified azo-free natural dyes, minimum 200 GSM density, GI Tagged certification attached.",
            status = "IN_PRODUCTION",
            fulfillmentProgress = 45
        )
        bulkOrderDao.insertBulkOrder(sampleBulkOrder)

        val seedAllocations = listOf(
            BulkAllocationEntity(
                id = "alloc_1",
                bulkOrderId = sampleOrderId,
                circleId = "circle_chennai_weavers",
                circleName = "Chennai Weavers Circle",
                productRequirement = "500 Handwoven Raw Mulberry Silk Stoles with Temple Zari Border",
                buyerOrg = "Taj Group of Heritage Hotels",
                artisanId = "artisan_a",
                artisanName = "Artisan A (Lakshmi Ammal)",
                artisanAvatar = "avatar_lakshmi",
                allocatedQuantity = 100,
                unitPayout = 855.0,
                estimatedPayout = 85500.0,
                deadline = "30 Oct 2026",
                invitationStatus = "ACCEPTED",
                productionProgress = 50,
                isReadyForDispatch = false,
                aiReasoning = "Assigned 100 pcs based on master weaver status and 98% past fulfillment score."
            ),
            BulkAllocationEntity(
                id = "alloc_2",
                bulkOrderId = sampleOrderId,
                circleId = "circle_chennai_weavers",
                circleName = "Chennai Weavers Circle",
                productRequirement = "500 Handwoven Raw Mulberry Silk Stoles with Temple Zari Border",
                buyerOrg = "Taj Group of Heritage Hotels",
                artisanId = "artisan_b",
                artisanName = "Artisan B (Ramesh Babu)",
                artisanAvatar = "avatar_ramesh",
                allocatedQuantity = 80,
                unitPayout = 855.0,
                estimatedPayout = 68400.0,
                deadline = "30 Oct 2026",
                invitationStatus = "ACCEPTED",
                productionProgress = 50,
                isReadyForDispatch = false,
                aiReasoning = "Assigned 80 pcs based on available loom capacity."
            ),
            BulkAllocationEntity(
                id = "alloc_3",
                bulkOrderId = sampleOrderId,
                circleId = "circle_chennai_weavers",
                circleName = "Chennai Weavers Circle",
                productRequirement = "500 Handwoven Raw Mulberry Silk Stoles with Temple Zari Border",
                buyerOrg = "Taj Group of Heritage Hotels",
                artisanId = "artisan_c",
                artisanName = "Artisan C (Sundaram Murthy)",
                artisanAvatar = "avatar_ramesh",
                allocatedQuantity = 120,
                unitPayout = 855.0,
                estimatedPayout = 102600.0,
                deadline = "30 Oct 2026",
                invitationStatus = "ACCEPTED",
                productionProgress = 25,
                isReadyForDispatch = false,
                aiReasoning = "Assigned 120 pcs matching high available yardage workshop."
            ),
            BulkAllocationEntity(
                id = "alloc_4",
                bulkOrderId = sampleOrderId,
                circleId = "circle_chennai_weavers",
                circleName = "Chennai Weavers Circle",
                productRequirement = "500 Handwoven Raw Mulberry Silk Stoles with Temple Zari Border",
                buyerOrg = "Taj Group of Heritage Hotels",
                artisanId = "artisan_d",
                artisanName = "Artisan D (Kavitha Raman)",
                artisanAvatar = "avatar_devi",
                allocatedQuantity = 100,
                unitPayout = 855.0,
                estimatedPayout = 85500.0,
                deadline = "30 Oct 2026",
                invitationStatus = "PENDING",
                productionProgress = 0,
                isReadyForDispatch = false,
                aiReasoning = "Assigned 100 pcs for balanced guild throughput."
            ),
            BulkAllocationEntity(
                id = "alloc_5",
                bulkOrderId = sampleOrderId,
                circleId = "circle_chennai_weavers",
                circleName = "Chennai Weavers Circle",
                productRequirement = "500 Handwoven Raw Mulberry Silk Stoles with Temple Zari Border",
                buyerOrg = "Taj Group of Heritage Hotels",
                artisanId = "artisan_e",
                artisanName = "Artisan E (Anand Vel)",
                artisanAvatar = "avatar_ramesh",
                allocatedQuantity = 100,
                unitPayout = 855.0,
                estimatedPayout = 85500.0,
                deadline = "30 Oct 2026",
                invitationStatus = "PENDING",
                productionProgress = 0,
                isReadyForDispatch = false,
                aiReasoning = "Assigned 100 pcs for balanced guild throughput."
            )
        )
        bulkAllocationDao.insertAllocations(seedAllocations)
    }
}
