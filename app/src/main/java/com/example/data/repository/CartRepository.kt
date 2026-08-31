package com.example.data.repository

import com.example.data.local.*
import com.example.data.models.*
import com.example.data.service.ArtisanNotificationService
import com.example.data.service.DemoPaymentService
import com.example.data.service.PaymentService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class CartRepository(
    private val productDao: ProductDao,
    private val artisanDao: ArtisanDao,
    private val orderDao: OrderDao,
    private val notificationDao: ArtisanNotificationDao,
    private val paymentService: PaymentService = com.example.data.service.SecureRazorpayBackendClient()
) {

    private val notificationService = ArtisanNotificationService(notificationDao)

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    private val _savedForLater = MutableStateFlow<List<CartItem>>(emptyList())
    private val _appliedCoupon = MutableStateFlow<String?>(null)
    private val _discountAmount = MutableStateFlow(0.0)
    private val _isLoading = MutableStateFlow(false)
    private val _message = MutableStateFlow<String?>(null)

    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val buyerOrders: Flow<List<OrderEntity>> = orderDao.getOrdersByBuyer("buyer_default")

    fun getArtisanOrders(artisanId: String): Flow<List<OrderEntity>> = orderDao.getOrdersByArtisan(artisanId)

    fun getArtisanNotifications(artisanId: String): Flow<List<ArtisanNotificationEntity>> =
        notificationDao.getNotificationsForArtisan(artisanId)

    val cartState: StateFlow<CartState> = combine(
        _cartItems,
        _savedForLater,
        _appliedCoupon,
        _discountAmount,
        _isLoading
    ) { items: List<CartItem>, saved: List<CartItem>, coupon: String?, discount: Double, loading: Boolean ->
        val subtotal = items.sumOf { it.product.activePrice * it.quantity }
        val deliveryFee = if (items.isEmpty() || subtotal > 999.0) 0.0 else 90.0
        val total = (subtotal + deliveryFee - discount).coerceAtLeast(0.0)

        CartState(
            items = items,
            savedForLater = saved,
            couponCode = coupon,
            discountAmount = discount,
            deliveryFee = deliveryFee,
            subtotal = subtotal,
            total = total,
            isLoading = loading,
            message = _message.value
        )
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Default),
        started = SharingStarted.Eagerly,
        initialValue = CartState()
    )

    fun addToCart(product: ProductEntity, quantity: Int = 1) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = current[index].quantity + quantity)
        } else {
            current.add(CartItem(product = product, quantity = quantity))
        }
        _cartItems.value = current
        recalculateDiscounts()
    }

    fun updateQuantity(productId: String, quantity: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            if (quantity <= 0) {
                current.removeAt(index)
            } else {
                current[index] = current[index].copy(quantity = quantity)
            }
            _cartItems.value = current
            recalculateDiscounts()
        }
    }

    fun removeFromCart(productId: String) {
        val current = _cartItems.value.toMutableList()
        current.removeAll { it.product.id == productId }
        _cartItems.value = current
        recalculateDiscounts()
    }

    fun saveForLater(productId: String) {
        val current = _cartItems.value.toMutableList()
        val itemIndex = current.indexOfFirst { it.product.id == productId }
        if (itemIndex >= 0) {
            val item = current.removeAt(itemIndex)
            _cartItems.value = current
            
            val saved = _savedForLater.value.toMutableList()
            if (saved.none { it.product.id == productId }) {
                saved.add(item.copy(isSavedForLater = true))
                _savedForLater.value = saved
            }
            recalculateDiscounts()
        }
    }

    fun moveToCartFromSaved(productId: String) {
        val saved = _savedForLater.value.toMutableList()
        val itemIndex = saved.indexOfFirst { it.product.id == productId }
        if (itemIndex >= 0) {
            val item = saved.removeAt(itemIndex)
            _savedForLater.value = saved

            addToCart(item.product, item.quantity)
        }
    }

    fun removeSavedItem(productId: String) {
        val saved = _savedForLater.value.toMutableList()
        saved.removeAll { it.product.id == productId }
        _savedForLater.value = saved
    }

    fun applyCoupon(code: String): Boolean {
        val trimmed = code.trim().uppercase()
        val subtotal = _cartItems.value.sumOf { it.product.activePrice * it.quantity }
        
        return when (trimmed) {
            "HANDMADE10" -> {
                _appliedCoupon.value = "HANDMADE10"
                _discountAmount.value = subtotal * 0.10
                _message.value = "Coupon HANDMADE10 applied: 10% discount"
                true
            }
            "CRAFTLOVE" -> {
                _appliedCoupon.value = "CRAFTLOVE"
                _discountAmount.value = 200.0.coerceAtMost(subtotal)
                _message.value = "Coupon CRAFTLOVE applied: ₹200 discount"
                true
            }
            "VISHWAKARMA" -> {
                _appliedCoupon.value = "VISHWAKARMA"
                _discountAmount.value = subtotal * 0.15
                _message.value = "Special Artisan Patron Coupon: 15% discount"
                true
            }
            else -> {
                _message.value = "Invalid coupon code. Try HANDMADE10 or CRAFTLOVE"
                false
            }
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
        _discountAmount.value = 0.0
        _message.value = null
    }

    private fun recalculateDiscounts() {
        val coupon = _appliedCoupon.value
        if (coupon != null) {
            applyCoupon(coupon)
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _appliedCoupon.value = null
        _discountAmount.value = 0.0
    }

    /**
     * Complete End-to-End Secure Payment & Order Creation Pipeline
     */
    suspend fun initiateCheckout(
        buyerId: String = "buyer_default",
        buyerName: String = "Ananya Sen",
        buyerPhone: String = "+91 98450 12345",
        deliveryAddress: DeliveryAddress,
        paymentMethod: PaymentMethod
    ): Result<PaymentOrderResponse> {
        val currentItems = _cartItems.value
        if (currentItems.isEmpty()) {
            return Result.failure(Exception("Cart is empty"))
        }

        val subtotal = currentItems.sumOf { it.product.activePrice * it.quantity }
        val deliveryFee = if (subtotal > 999.0) 0.0 else 90.0
        val discount = _discountAmount.value
        val total = (subtotal + deliveryFee - discount).coerceAtLeast(0.0)

        val request = PaymentOrderRequest(
            buyerId = buyerId,
            items = currentItems,
            deliveryAddress = deliveryAddress,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            discountAmount = discount,
            totalAmount = total,
            paymentMethod = paymentMethod,
            isDemo = paymentMethod == PaymentMethod.DEMO_PAYMENT || paymentService.isDemoMode()
        )

        return paymentService.createOrder(request)
    }

    /**
     * Verifies payment signature and atomically creates the Order in Room
     * and dispatches FCM/in-app notification to artisan.
     */
    suspend fun verifyAndCompleteOrder(
        paymentOrderResponse: PaymentOrderResponse,
        razorpayPaymentId: String,
        razorpaySignature: String,
        deliveryAddress: DeliveryAddress,
        paymentMethod: PaymentMethod
    ): Result<OrderEntity> {
        _isLoading.value = true
        try {
            val verifyRequest = PaymentVerificationRequest(
                internalOrderId = paymentOrderResponse.internalOrderId,
                razorpayPaymentId = razorpayPaymentId,
                razorpayOrderId = paymentOrderResponse.razorpayOrderId,
                razorpaySignature = razorpaySignature,
                isDemo = paymentOrderResponse.isDemo
            )

            val verifyResult = paymentService.verifyPayment(verifyRequest)
            if (verifyResult.isFailure) {
                _isLoading.value = false
                return Result.failure(verifyResult.exceptionOrNull() ?: Exception("Payment verification failed"))
            }

            val currentItems = _cartItems.value
            val primaryItem = currentItems.firstOrNull()
            val artisanId = primaryItem?.product?.artisanId ?: "artisan_lakshmi"
            val artisanName = primaryItem?.product?.artisanName ?: "Lakshmi Ammal"

            val itemsSummary = currentItems.joinToString(", ") { "${it.product.title} (x${it.quantity})" }

            val subtotal = currentItems.sumOf { it.product.activePrice * it.quantity }
            val deliveryFee = if (subtotal > 999.0) 0.0 else 90.0
            val discount = _discountAmount.value
            val total = (subtotal + deliveryFee - discount).coerceAtLeast(0.0)

            val orderEntity = OrderEntity(
                id = paymentOrderResponse.internalOrderId,
                buyerId = "buyer_default",
                buyerName = deliveryAddress.fullName,
                buyerPhone = deliveryAddress.phone,
                recipientName = deliveryAddress.fullName,
                addressStreet = deliveryAddress.streetAddress,
                addressCity = deliveryAddress.city,
                addressState = deliveryAddress.state,
                addressPin = deliveryAddress.pinCode,
                addressType = deliveryAddress.addressType,
                itemsSummary = itemsSummary,
                itemsJson = "[]",
                subtotal = subtotal,
                deliveryFee = deliveryFee,
                discountAmount = discount,
                totalAmount = total,
                paymentState = PaymentState.PAID.name,
                orderState = OrderState.PROCESSING.name,
                artisanStatus = ArtisanOrderStatus.NEW.name,
                artisanId = artisanId,
                artisanName = artisanName,
                paymentMethod = paymentMethod.displayName,
                isDemoPayment = paymentOrderResponse.isDemo,
                razorpayOrderId = paymentOrderResponse.razorpayOrderId,
                razorpayPaymentId = razorpayPaymentId,
                estimatedDeliveryDays = "4–7 days",
                courierName = "India Post Speed Post Express",
                trackingNumber = "IN-POST-${(10000000..99999999).random()}",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // Insert order in database
            orderDao.insertOrder(orderEntity)

            // Decrement product stock
            currentItems.forEach { item ->
                val newStock = (item.product.stockQuantity - item.quantity).coerceAtLeast(0)
                productDao.updateStock(item.product.id, newStock)
            }

            // Retrieve artisan to find preferred language
            val artisan = artisanDao.getArtisanById(artisanId)
            val artisanLang = artisan?.languageCode ?: "ta"

            // Dispatch notification to artisan
            notificationService.sendOrderNotification(
                artisanId = artisanId,
                orderId = orderEntity.id,
                productTitle = primaryItem?.product?.title ?: "Handcrafted Item",
                quantity = currentItems.sumOf { it.quantity },
                orderValue = total,
                artisanLanguageCode = artisanLang
            )

            // Clear active cart upon confirmed payment
            clearCart()
            _isLoading.value = false

            return Result.success(orderEntity)
        } catch (e: Exception) {
            _isLoading.value = false
            return Result.failure(e)
        }
    }

    suspend fun updateArtisanOrderStatus(orderId: String, artisanStatus: ArtisanOrderStatus) {
        val orderState = when (artisanStatus) {
            ArtisanOrderStatus.NEW -> OrderState.PROCESSING
            ArtisanOrderStatus.PREPARING -> OrderState.PROCESSING
            ArtisanOrderStatus.READY -> OrderState.PROCESSING
            ArtisanOrderStatus.SHIPPED -> OrderState.SHIPPED
            ArtisanOrderStatus.DELIVERED -> OrderState.DELIVERED
            ArtisanOrderStatus.COMPLETED -> OrderState.COMPLETED
        }
        orderDao.updateOrderStatus(
            id = orderId,
            orderState = orderState.name,
            artisanStatus = artisanStatus.name,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun markNotificationRead(notificationId: String) {
        notificationDao.markAsRead(notificationId)
    }
}
