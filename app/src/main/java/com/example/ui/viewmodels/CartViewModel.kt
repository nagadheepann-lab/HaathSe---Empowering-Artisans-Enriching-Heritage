package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.data.models.*
import com.example.data.repository.CartRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class CartViewModel(
    private val cartRepository: CartRepository
) : ViewModel() {

    val cartState: StateFlow<CartState> = cartRepository.cartState
    val buyerOrders: Flow<List<OrderEntity>> = cartRepository.buyerOrders
    val allOrders: Flow<List<OrderEntity>> = cartRepository.allOrders

    fun getArtisanOrders(artisanId: String): Flow<List<OrderEntity>> = cartRepository.getArtisanOrders(artisanId)
    fun getArtisanNotifications(artisanId: String) = cartRepository.getArtisanNotifications(artisanId)

    private val _deliveryAddress = MutableStateFlow(DeliveryAddress())
    val deliveryAddress = _deliveryAddress.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow(PaymentMethod.DEMO_PAYMENT)
    val selectedPaymentMethod = _selectedPaymentMethod.asStateFlow()

    private val _isPaymentProcessing = MutableStateFlow(false)
    val isPaymentProcessing = _isPaymentProcessing.asStateFlow()

    private val _paymentProcessingStage = MutableStateFlow("1. Initializing Payment...")
    val paymentProcessingStage = _paymentProcessingStage.asStateFlow()

    private val _lastConfirmedOrder = MutableStateFlow<OrderEntity?>(null)
    val lastConfirmedOrder = _lastConfirmedOrder.asStateFlow()

    fun addToCart(product: ProductEntity, quantity: Int = 1) {
        cartRepository.addToCart(product, quantity)
    }

    fun updateQuantity(productId: String, quantity: Int) {
        cartRepository.updateQuantity(productId, quantity)
    }

    fun removeFromCart(productId: String) {
        cartRepository.removeFromCart(productId)
    }

    fun saveForLater(productId: String) {
        cartRepository.saveForLater(productId)
    }

    fun moveToCartFromSaved(productId: String) {
        cartRepository.moveToCartFromSaved(productId)
    }

    fun removeSavedItem(productId: String) {
        cartRepository.removeSavedItem(productId)
    }

    fun applyCoupon(code: String): Boolean {
        return cartRepository.applyCoupon(code)
    }

    fun removeCoupon() {
        cartRepository.removeCoupon()
    }

    fun clearCart() {
        cartRepository.clearCart()
    }

    fun updateDeliveryAddress(address: DeliveryAddress) {
        _deliveryAddress.value = address
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
    }

    fun resetLastOrder() {
        _lastConfirmedOrder.value = null
    }

    /**
     * Executes the secure backend payment flow:
     * 1. Calls POST /api/payments/create-order
     * 2. Launches Razorpay standard Checkout dialog if Activity is provided and Razorpay method is selected
     * 3. Sends signature and payment ID to backend / PaymentService to verify
     * 4. Upon verification, atomically updates order state to PAID and creates OrderEntity
     * 5. Dispatches FCM notification to artisan in their native language
     */
    fun processCheckoutAndPayment(
        activity: android.app.Activity? = null,
        onSuccess: (OrderEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isPaymentProcessing.value = true
            _paymentProcessingStage.value = "1. Connecting to HaathSe Secure Backend..."

            val initResult = cartRepository.initiateCheckout(
                deliveryAddress = _deliveryAddress.value,
                paymentMethod = _selectedPaymentMethod.value
            )

            if (initResult.isFailure) {
                _isPaymentProcessing.value = false
                onError(initResult.exceptionOrNull()?.message ?: "Failed to initiate payment")
                return@launch
            }

            val orderPayload = initResult.getOrThrow()
            _paymentProcessingStage.value = "2. Authorizing with Payment Gateway..."

            // If Activity is provided and it's not explicitly DEMO_PAYMENT, launch Razorpay Checkout UI
            if (activity != null && _selectedPaymentMethod.value != PaymentMethod.DEMO_PAYMENT) {
                val primaryItem = cartState.value.items.firstOrNull()
                val productName = primaryItem?.product?.title ?: "Handcrafted Item"

                com.example.utils.RazorpayManager.startPayment(
                    activity = activity,
                    orderId = orderPayload.razorpayOrderId,
                    amountInRupees = cartState.value.total,
                    productName = productName,
                    buyerName = _deliveryAddress.value.fullName,
                    buyerEmail = "buyer@haathse.in",
                    buyerPhone = _deliveryAddress.value.phone,
                    callback = object : com.example.utils.RazorpayManager.PaymentCallback {
                        override fun onPaymentSuccess(paymentId: String, paymentData: com.razorpay.PaymentData?) {
                            viewModelScope.launch {
                                _paymentProcessingStage.value = "3. Verifying Cryptographic Signature on Server..."
                                val signature = paymentData?.signature ?: "rzp_sig_${UUID.randomUUID().toString().take(12)}"
                                val verifyResult = cartRepository.verifyAndCompleteOrder(
                                    paymentOrderResponse = orderPayload,
                                    razorpayPaymentId = paymentId,
                                    razorpaySignature = signature,
                                    deliveryAddress = _deliveryAddress.value,
                                    paymentMethod = _selectedPaymentMethod.value
                                )
                                _isPaymentProcessing.value = false
                                if (verifyResult.isSuccess) {
                                    val order = verifyResult.getOrThrow()
                                    _lastConfirmedOrder.value = order
                                    onSuccess(order)
                                } else {
                                    onError(verifyResult.exceptionOrNull()?.message ?: "Payment verification failed")
                                }
                            }
                        }

                        override fun onPaymentError(errorCode: Int, description: String?, paymentData: com.razorpay.PaymentData?) {
                            _isPaymentProcessing.value = false
                            onError(description ?: "Payment cancelled or failed. (Code $errorCode)")
                        }
                    }
                )
            } else {
                // Fast Demo / Sandbox simulation path
                val simulatedPaymentId = "pay_test_${UUID.randomUUID().toString().take(12)}"
                val simulatedSignature = "sig_test_${UUID.randomUUID().toString().take(16)}"

                _paymentProcessingStage.value = "3. Verifying Cryptographic Signature on Server..."

                val verifyResult = cartRepository.verifyAndCompleteOrder(
                    paymentOrderResponse = orderPayload,
                    razorpayPaymentId = simulatedPaymentId,
                    razorpaySignature = simulatedSignature,
                    deliveryAddress = _deliveryAddress.value,
                    paymentMethod = _selectedPaymentMethod.value
                )

                _isPaymentProcessing.value = false

                if (verifyResult.isSuccess) {
                    val order = verifyResult.getOrThrow()
                    _lastConfirmedOrder.value = order
                    onSuccess(order)
                } else {
                    onError(verifyResult.exceptionOrNull()?.message ?: "Signature verification failed")
                }
            }
        }
    }

    fun updateArtisanOrderStatus(orderId: String, status: ArtisanOrderStatus) {
        viewModelScope.launch {
            cartRepository.updateArtisanOrderStatus(orderId, status)
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            cartRepository.markNotificationRead(notificationId)
        }
    }
}
