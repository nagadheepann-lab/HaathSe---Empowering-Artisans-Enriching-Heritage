package com.example.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.razorpay.Checkout
import com.razorpay.PaymentData
import org.json.JSONObject
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object RazorpayManager {
    private const val TAG = "RazorpayManager"
    
    // Default Test Key ID & Secret configured for Test Mode
    const val DEFAULT_TEST_KEY_ID = "rzp_test_1DP5mmOlF5G5ag"
    const val DEFAULT_TEST_KEY_SECRET = "tSGdSojH9w9tGfpFPgEdXlU2"

    var customKeyId: String? = null
    var customKeySecret: String? = null

    // Active callback for the in-flight checkout session
    var activePaymentCallback: PaymentCallback? = null

    interface PaymentCallback {
        fun onPaymentSuccess(paymentId: String, paymentData: PaymentData?)
        fun onPaymentError(errorCode: Int, description: String?, paymentData: PaymentData?)
    }

    fun getKeyId(): String {
        return customKeyId?.ifBlank { null } ?: DEFAULT_TEST_KEY_ID
    }

    fun getKeySecret(): String {
        return customKeySecret?.ifBlank { null } ?: DEFAULT_TEST_KEY_SECRET
    }

    fun preload(context: Context) {
        try {
            Checkout.preload(context.applicationContext)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to preload Razorpay Checkout: ${e.message}")
        }
    }

    fun startPayment(
        activity: Activity,
        orderId: String,
        amountInRupees: Double,
        productName: String,
        buyerName: String,
        buyerEmail: String = "artisan.buyer@haathse.in",
        buyerPhone: String = "9876543210",
        callback: PaymentCallback
    ) {
        try {
            activePaymentCallback = callback
            val checkout = Checkout()
            checkout.setKeyID(getKeyId())

            val options = JSONObject().apply {
                put("name", "HaathSe Artisan Direct")
                put("description", productName.take(40))
                put("currency", "INR")
                // Amount in paise (1 INR = 100 paise)
                put("amount", (amountInRupees * 100).toLong())
                put("theme.color", "#C2410C")
                
                // If a real backend order was generated, attach order_id
                if (orderId.startsWith("order_")) {
                    put("order_id", orderId)
                }

                val prefill = JSONObject().apply {
                    put("email", buyerEmail.ifBlank { "buyer@haathse.in" })
                    put("contact", buyerPhone.ifBlank { "9876543210" })
                    put("name", buyerName.ifBlank { "Patron" })
                }
                put("prefill", prefill)

                val retryObj = JSONObject().apply {
                    put("enabled", true)
                    put("max_count", 2)
                }
                put("retry", retryObj)
            }

            checkout.open(activity, options)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Razorpay checkout", e)
            callback.onPaymentError(
                Checkout.PAYMENT_CANCELED,
                e.localizedMessage ?: "Failed to open Razorpay payment gateway",
                null
            )
        }
    }

    fun notifyPaymentSuccess(paymentId: String, paymentData: PaymentData?) {
        activePaymentCallback?.onPaymentSuccess(paymentId, paymentData)
        activePaymentCallback = null
    }

    fun notifyPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        activePaymentCallback?.onPaymentError(code, response, paymentData)
        activePaymentCallback = null
    }

    /**
     * Verifies payment signature using HMAC SHA-256:
     * signature = HMAC_SHA256(order_id + "|" + razorpay_payment_id, key_secret)
     */
    fun verifySignature(orderId: String, paymentId: String, signature: String, secret: String = getKeySecret()): Boolean {
        return try {
            val payload = "$orderId|$paymentId"
            val hmac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
            hmac.init(secretKey)
            val hash = hmac.doFinal(payload.toByteArray(Charsets.UTF_8))
            val generatedSignature = hash.joinToString("") { "%02x".format(it) }
            generatedSignature.equals(signature, ignoreCase = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error validating signature", e)
            false
        }
    }
}

