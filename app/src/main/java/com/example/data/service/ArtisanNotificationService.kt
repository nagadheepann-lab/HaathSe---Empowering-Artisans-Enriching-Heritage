package com.example.data.service

import com.example.data.local.ArtisanNotificationDao
import com.example.data.local.ArtisanNotificationEntity
import com.example.data.models.SupportedLanguage
import java.util.UUID

/**
 * Service for dispatching multilingual artisan push / in-app notifications
 * upon verified payment execution.
 */
class ArtisanNotificationService(
    private val notificationDao: ArtisanNotificationDao
) {

    suspend fun sendOrderNotification(
        artisanId: String,
        orderId: String,
        productTitle: String,
        quantity: Int,
        orderValue: Double,
        artisanLanguageCode: String
    ): ArtisanNotificationEntity {
        val (title, message) = generateMultilingualMessage(
            artisanLanguageCode = artisanLanguageCode,
            productTitle = productTitle,
            quantity = quantity,
            orderValue = orderValue,
            orderId = orderId
        )

        val notification = ArtisanNotificationEntity(
            id = "notif_${UUID.randomUUID().toString().take(8)}",
            artisanId = artisanId,
            orderId = orderId,
            title = title,
            message = message,
            productTitle = productTitle,
            quantity = quantity,
            orderValue = orderValue,
            languageCode = artisanLanguageCode,
            isRead = false,
            timestamp = System.currentTimeMillis()
        )

        notificationDao.insertNotification(notification)
        return notification
    }

    private fun generateMultilingualMessage(
        artisanLanguageCode: String,
        productTitle: String,
        quantity: Int,
        orderValue: Double,
        orderId: String
    ): Pair<String, String> {
        val formattedAmount = "₹${orderValue.toInt()}"

        return when (artisanLanguageCode.lowercase()) {
            "ta" -> Pair(
                "🎉 புதிய கைவினை ஆர்டர்! ($orderId)",
                "வாழ்த்துக்கள்! $productTitle க்கான புதிய ஆர்டர் வந்துள்ளது.\nஅளவு: $quantity | ஆர்டர் மதிப்பு: $formattedAmount\nகட்டணம்: ✓ உறுதி செய்யப்பட்டது"
            )
            "hi" -> Pair(
                "🎉 नया शिल्प ऑर्डर! ($orderId)",
                "बधाई हो! $productTitle के लिए नया ऑर्डर प्राप्त हुआ है।\nमात्रा: $quantity | ऑर्डर मूल्य: $formattedAmount\nभुगतान: ✓ पुष्टि हो गई"
            )
            "kn" -> Pair(
                "🎉 ಹೊಸ ಕರಕುಶಲ ಆದೇಶ! ($orderId)",
                "ಅಭಿನಂದನೆಗಳು! $productTitle ಗೆ ಹೊಸ ಆದೇಶ ಬಂದಿದೆ.\nಪ್ರಮಾಣ: $quantity | ಆದೇಶ ಮೌಲ್ಯ: $formattedAmount\nಪಾವತಿ: ✓ ದೃಢೀಕರಿಸಲಾಗಿದೆ"
            )
            "te" -> Pair(
                "🎉 కొత్త చేతివృత్తుల ఆర్డర్! ($orderId)",
                "అభినందనలు! $productTitle కోసం కొత్త ఆర్డర్ వచ్చింది.\nపరిమాణం: $quantity | ఆర్డర్ విలువ: $formattedAmount\nచెల్లింపు: ✓ నిర్ధారించబడింది"
            )
            "bn" -> Pair(
                "🎉 নতুন হস্তশিল্প অর্ডার! ($orderId)",
                "অভিনন্দন! $productTitle-এর জন্য একটি নতুন অর্ডার এসেছে।\nপরিমাণ: $quantity | অর্ডার মূল্য: $formattedAmount\nপেমেন্ট: ✓ নিশ্চিত হয়েছে"
            )
            "mr" -> Pair(
                "🎉 नवीन हस्तकला ऑर्डर! ($orderId)",
                "अभिनंदन! $productTitle साठी नवीन ऑर्डर आली आहे.\nप्रमाण: $quantity | ऑर्डर मूल्य: $formattedAmount\nपेमेंट: ✓ पुष्टी झाली"
            )
            else -> Pair(
                "🎉 New Order Received! ($orderId)",
                "Congratulations! New order placed for $productTitle.\nQuantity: $quantity | Order Value: $formattedAmount\nPayment: ✓ Confirmed"
            )
        }
    }
}
