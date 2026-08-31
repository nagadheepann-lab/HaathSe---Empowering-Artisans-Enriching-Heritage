package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Log
import com.example.R
import com.example.data.models.SupportedLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

enum class CameraEnhancementMode(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val badgeLabel: String
) {
    STUDIO_PRO(
        id = "studio_pro",
        title = "Studio Pro",
        description = "Clean studio daylight, micro-contrast & neutral catalog backdrop",
        iconEmoji = "🌟",
        badgeLabel = "STUDIO PRO"
    ),
    HERITAGE_WARM(
        id = "heritage_warm",
        title = "Heritage Warm",
        description = "Golden zari glow, silk luster & rich terracotta tones",
        iconEmoji = "🪔",
        badgeLabel = "HERITAGE GLOW"
    ),
    VIBRANT_DETAIL(
        id = "vibrant_detail",
        title = "Vibrant Detail",
        description = "Intricate thread weave, sharp texture & vivid pigments",
        iconEmoji = "✨",
        badgeLabel = "VIVID DETAIL"
    )
}

enum class DynamicGuidanceState(val iconEmoji: String) {
    ANALYZING("🔍"),
    TOO_DARK("☀️"),
    TOO_BRIGHT("⛅"),
    MOVE_CLOSER("📐"),
    HOLD_STEADY("📳"),
    PERFECT_FRAME("✨")
}

object DynamicCameraGuidanceManager {
    fun getGuidanceText(state: DynamicGuidanceState, language: SupportedLanguage): String {
        return when (state) {
            DynamicGuidanceState.ANALYZING -> when (language) {
                SupportedLanguage.HINDI -> "फ्रेम का विश्लेषण हो रहा है..."
                SupportedLanguage.TAMIL -> "பிரேம் ஆராயப்படுகிறது..."
                SupportedLanguage.TELUGU -> "ఫ్రేమ్ విశ్లేషించబడుతోంది..."
                SupportedLanguage.KANNADA -> "ಫ್ರೇಮ್ ವಿಶ್ಲೇಷಿಸಲಾಗುತ್ತಿದೆ..."
                SupportedLanguage.MALAYALAM -> "ഫ്രെയിം പരിശോധിക്കുന്നു..."
                SupportedLanguage.BENGALI -> "ফ্রেম বিশ্লেষণ করা হচ্ছে..."
                SupportedLanguage.MARATHI -> "फ्रेमचे विश्लेषण केले जात आहे..."
                SupportedLanguage.GUJARATI -> "ફ્રેમનું વિશ્લેષણ થઈ રહ્યું છે..."
                SupportedLanguage.PUNJABI -> "ਫਰੇਮ ਦਾ ਵਿਸ਼ਲੇਸ਼ਣ ਹੋ ਰਿਹਾ ਹੈ..."
                SupportedLanguage.ODIA -> "ଫ୍ରେମ୍ ବିଶ୍ଳେଷଣ କରାଯାଉଛି..."
                SupportedLanguage.ENGLISH -> "Analyzing lighting & position..."
            }
            DynamicGuidanceState.TOO_DARK -> when (language) {
                SupportedLanguage.HINDI -> "रोशनी बहुत कम है। कृपया उजाले में जाएं या फ्लैश ऑन करें।"
                SupportedLanguage.TAMIL -> "வெளிச்சம் குறைவாக உள்ளது. அதிக வெளிச்சத்திற்கு செல்லவும் அல்லது ஃபிளாஷ் ஆன் செய்யவும்."
                SupportedLanguage.TELUGU -> "వెలుతురు చాలా తక్కువగా ఉంది. ఎక్కువ వెలుతురు ఉన్న చోటికి వెళ్లండి లేదా ఫ్లాష్ ఆన్ చేయండి."
                SupportedLanguage.KANNADA -> "ಬೆಳಕು ಕಡಿಮೆಯಾಗಿದೆ. ಹೆಚ್ಚು ಬೆಳಕಿರುವಲ್ಲಿಗೆ ಸರಿಸಿ ಅಥವಾ ಫ್ಲ್ಯಾಷ್ ಆನ್ ಮಾಡಿ."
                SupportedLanguage.MALAYALAM -> "വെളിച്ചം കുറവാണ്. കൂടുതൽ വെളിച്ചമുള്ള സ്ഥലത്തേക്ക് മാറുക."
                SupportedLanguage.BENGALI -> "আলো খুব কম। উজ্জ্বল স্থানে যান অথবা ফ্ল্যাশ চালু করুন।"
                SupportedLanguage.MARATHI -> "प्रकाश खूप कमी आहे. अधिक प्रकाशात जा किंवा फ्लॅश चालू करा."
                SupportedLanguage.GUJARATI -> "પ્રકાશ ઓછો છે. વધુ અજવાળામાં જાઓ અથવા ફ્લેશ ચાલુ કરો."
                SupportedLanguage.PUNJABI -> "ਰੋਸ਼ਨੀ ਬਹੁਤ ਘੱਟ ਹੈ। ਕਿਰਪਾ ਕਰਕੇ ਚਾਨਣ ਵਿੱਚ ਜਾਓ ਜਾਂ ਫਲੈਸ਼ ਆਨ ਕਰੋ।"
                SupportedLanguage.ODIA -> "ଆଲୋକ ବହୁତ କମ୍ ଅଛି। ଉଜ୍ଜ୍ୱଳ ସ୍ଥାନକୁ ଯାଆନ୍ତୁ କିମ୍ବା ଫ୍ଲାସ୍ ଅନ୍ କରନ୍ତୁ।"
                SupportedLanguage.ENGLISH -> "Light is too low. Move to brighter light or turn on flash."
            }
            DynamicGuidanceState.TOO_BRIGHT -> when (language) {
                SupportedLanguage.HINDI -> "सीधी धूप से चमक आ रही है। छायादार जगह पर जाएं।"
                SupportedLanguage.TAMIL -> "அதிக வெளிச்சப் பிரதிபலிப்பு உள்ளது. நேரடி வெயிலை தவிர்க்கவும்."
                SupportedLanguage.TELUGU -> "తీవ్రమైన ఎండ మెరుపు ఉంది. ప్రత్యక్ష ఎండ నుండి పక్కకు తరలించండి."
                SupportedLanguage.KANNADA -> "ಹೆಚ್ಚು ಪ್ರಖರ ಬೆಳಕಿದೆ. ನೇರ ಸೂರ್ಯನ ಬೆಳಕಿನಿಂದ ದೂರವಿಡಿ."
                SupportedLanguage.MALAYALAM -> "തീവ്രമായ വെളിച്ചം. തണലുള്ള സ്ഥലത്തേക്ക് മാറ്റുക."
                SupportedLanguage.BENGALI -> "অতিরিক্ত ঝলক দেখা যাচ্ছে। ছায়াযুক্ত স্থানে যান।"
                SupportedLanguage.MARATHI -> "खूप जास्त चमक आहे. थेट सूर्यप्रकाशापासून बाजूला व्हा."
                SupportedLanguage.GUJARATI -> "તીવ્ર અજવાળું છે. સીધા સૂર્યપ્રકાશથી દૂર જાઓ."
                SupportedLanguage.PUNJABI -> "ਬਹੁਤ ਜ਼ਿਆਦਾ ਚਮਕ ਹੈ। ਸਿੱਧੀ ਧੁੱਪ ਤੋਂ ਹਟਾਓ।"
                SupportedLanguage.ODIA -> "ଅତ୍ୟଧିକ ଆଲୋକ ଅଛି। ଛାଇକୁ ଯାଆନ୍ତୁ।"
                SupportedLanguage.ENGLISH -> "Harsh glare detected. Move away from direct sunlight."
            }
            DynamicGuidanceState.MOVE_CLOSER -> when (language) {
                SupportedLanguage.HINDI -> "कारीगरी की बारीकियाँ दिखाने के लिए थोड़ा और पास लाएं।"
                SupportedLanguage.TAMIL -> "கைவினை நுணுக்கங்களை காட்ட சற்று அருகில் கொண்டு வரவும்."
                SupportedLanguage.TELUGU -> "హస్తకళ సూక్ష్మ వివరాల కోసం కొద్దిగా దగ్గరకు రండి."
                SupportedLanguage.KANNADA -> "ಕರಕುಶಲ ವಿವರಗಳನ್ನು ಸೆರೆಹಿಡಿಯಲು ಸ್ವಲ್ಪ ಹತ್ತಿರಕ್ಕೆ ತನ್ನಿ."
                SupportedLanguage.MALAYALAM -> "കരകൗശല വിശദാംശങ്ങൾക്കായി കുറച്ചുകൂടി അടുത്തേക്ക് കൊണ്ടുവരിക."
                SupportedLanguage.BENGALI -> "কারুকার্যের সূক্ষ্ম নকশা দেখাতে একটু কাছে আনুন।"
                SupportedLanguage.MARATHI -> "हस्तकलेचे बारकावे दाखवण्यासाठी थोडे जवळ आणा."
                SupportedLanguage.GUJARATI -> "કારીગરીની વિગતો માટે થોડા નજીક આવો."
                SupportedLanguage.PUNJABI -> "ਕਾਰੀਗਰੀ ਦੇ ਵੇਰਵਿਆਂ ਲਈ ਥੋੜ੍ਹਾ ਨੇੜੇ ਆਓ।"
                SupportedLanguage.ODIA -> "କାରିଗରୀର ସୂକ୍ଷ୍ମ କାର୍ଯ୍ୟ ପାଇଁ ଟିକିଏ ପାଖକୁ ଆଣନ୍ତୁ।"
                SupportedLanguage.ENGLISH -> "Move closer to capture fine craft details and texture."
            }
            DynamicGuidanceState.HOLD_STEADY -> when (language) {
                SupportedLanguage.HINDI -> "कैमरा स्थिर रखें और उत्पाद को बीच में रखें।"
                SupportedLanguage.TAMIL -> "அசையாமல் நிலை நிறுத்தி பொருளை நடுவில் வைக்கவும்."
                SupportedLanguage.TELUGU -> "కెమెరాను స్థిరంగా పట్టుకుని వస్తువును మధ్యలో ఉంచండి."
                SupportedLanguage.KANNADA -> "ಸ್ಥಿರವಾಗಿ ಹಿಡಿದುಕೊಳ್ಳಿ ಮತ್ತು ಉತ್ಪನ್ನವನ್ನು ಮಧ್ಯದಲ್ಲಿಡಿ."
                SupportedLanguage.MALAYALAM -> "ക്യാമറ അനങ്ങാതെ നടുവിൽ നിർത്തുക."
                SupportedLanguage.BENGALI -> "ক্যামেরা স্থির রাখুন এবং পণ্যটিকে ফ্রেমের মাঝে রাখুন।"
                SupportedLanguage.MARATHI -> "कॅमेरा स्थिर धरा आणि वस्तू मध्यभागी ठेवा."
                SupportedLanguage.GUJARATI -> "કેમેરા સ્થિર રાખો અને વસ્તુને મધ્યમાં રાખો."
                SupportedLanguage.PUNJABI -> "ਕੈਮਰਾ ਸਥਿਰ ਰੱਖੋ ਅਤੇ ਚੀਜ਼ ਨੂੰ ਵਿਚਕਾਰ ਰੱਖੋ।"
                SupportedLanguage.ODIA -> "କ୍ୟାମେରା ସ୍ଥିର ରଖନ୍ତୁ ଏବଂ ମଝିରେ ରଖନ୍ତୁ।"
                SupportedLanguage.ENGLISH -> "Hold steady. Keep the craft centered in the golden frame."
            }
            DynamicGuidanceState.PERFECT_FRAME -> when (language) {
                SupportedLanguage.HINDI -> "रोशनी और एंगल बिल्कुल सही है! अब फोटो खींचें।"
                SupportedLanguage.TAMIL -> "வெளிச்சமும் கோணமும் அற்புதம்! இப்போது படம் பிடிக்கவும்."
                SupportedLanguage.TELUGU -> "వెలుతురు మరియు స్థానం చక్కగా ఉన్నాయి! ఇప్పుడు ఫోటో తీయండి."
                SupportedLanguage.KANNADA -> "ಬೆಳಕು ಮತ್ತು ಕೋನ ಅತ್ಯುತ್ತಮವಾಗಿದೆ! ಈಗ ಫೋಟೋ ತೆಗೆಯಿರಿ."
                SupportedLanguage.MALAYALAM -> "വെളിച്ചവും ഫ്രെയിമും മികച്ചത്! ഇപ്പോൾ ഫോട്ടോ എടുക്കൂ."
                SupportedLanguage.BENGALI -> "আলো এবং ফ্রেম চমৎকার! এখনই ছবি তুলুন।"
                SupportedLanguage.MARATHI -> "प्रकाश आणि कोन अगदी योग्य आहे! आता फोटो काढा."
                SupportedLanguage.GUJARATI -> "પ્રકાશ અને ફ્રેમ એકદમ પરફેક્ટ છે! હવે ફોટો પાડો."
                SupportedLanguage.PUNJABI -> "ਰੋਸ਼ਨੀ ਅਤੇ ਐਂਗਲ ਬਿਲਕੁਲ ਸਹੀ ਹੈ! ਹੁਣ ਫੋਟੋ ਖਿੱਚੋ।"
                SupportedLanguage.ODIA -> "ଆଲୋକ ଏବଂ ଫ୍ରେମ୍ ଉତ୍କୃଷ୍ଟ! ବର୍ତ୍ତମାନ ଫଟୋ ଉଠାନ୍ତୁ।"
                SupportedLanguage.ENGLISH -> "Perfect lighting and framing! Tap the capture button."
            }
        }
    }
}

object ImageEnhancer {

    /**
     * Enhances a bitmap directly using ColorMatrix, brightness, contrast, and warmth calibration.
     * Preserves 100% of the authentic craft subject while giving it professional studio lighting.
     */
    fun enhanceBitmap(
        source: Bitmap,
        mode: CameraEnhancementMode
    ): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val finalMatrix = ColorMatrix()

        when (mode) {
            CameraEnhancementMode.STUDIO_PRO -> {
                // High-clarity neutral studio lighting:
                // Contrast +24%, Brightness +14, Saturation 1.15
                val contrast = 1.24f
                val translate = (-0.5f * contrast + 0.5f) * 255f + 14f
                val cmContrast = ColorMatrix(floatArrayOf(
                    contrast, 0f, 0f, 0f, translate,
                    0f, contrast, 0f, 0f, translate,
                    0f, 0f, contrast, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                ))
                val cmSat = ColorMatrix().apply { setSaturation(1.15f) }
                finalMatrix.setConcat(cmContrast, cmSat)
            }

            CameraEnhancementMode.HERITAGE_WARM -> {
                // Warm golden amber ambient boost for Indian crafts (silk, wood, brass, terracotta)
                val contrast = 1.20f
                val translate = (-0.5f * contrast + 0.5f) * 255f + 10f
                val cmWarm = ColorMatrix(floatArrayOf(
                    contrast * 1.08f, 0f, 0f, 0f, translate + 18f,  // Red / Warm gold
                    0f, contrast * 1.02f, 0f, 0f, translate + 8f,   // Green mild boost
                    0f, 0f, contrast * 0.94f, 0f, translate - 6f,   // Blue slight reduction for warmth
                    0f, 0f, 0f, 1f, 0f
                ))
                val cmSat = ColorMatrix().apply { setSaturation(1.28f) }
                finalMatrix.setConcat(cmWarm, cmSat)
            }

            CameraEnhancementMode.VIBRANT_DETAIL -> {
                // High vividness, punchy pigments and micro-texture accentuation
                val contrast = 1.32f
                val translate = (-0.5f * contrast + 0.5f) * 255f + 8f
                val cmVivid = ColorMatrix(floatArrayOf(
                    contrast, 0f, 0f, 0f, translate,
                    0f, contrast, 0f, 0f, translate,
                    0f, 0f, contrast, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                ))
                val cmSat = ColorMatrix().apply { setSaturation(1.36f) }
                finalMatrix.setConcat(cmVivid, cmSat)
            }
        }

        paint.colorFilter = ColorMatrixColorFilter(finalMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)

        return output
    }

    /**
     * Reads a real captured photo file, applies enhancement, and saves it as a new professional image file.
     */
    suspend fun enhanceImageFile(
        context: Context,
        inputFile: File,
        mode: CameraEnhancementMode
    ): File = withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(inputFile.absolutePath, options)

            // Downsample if huge (> 2048px) to conserve memory
            var inSampleSize = 1
            val maxDim = maxOf(options.outWidth, options.outHeight)
            while (maxDim / inSampleSize > 2048) {
                inSampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val rawBitmap = BitmapFactory.decodeFile(inputFile.absolutePath, decodeOptions)
                ?: return@withContext inputFile

            val enhancedBitmap = enhanceBitmap(rawBitmap, mode)

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(System.currentTimeMillis())
            val enhancedFile = File(context.cacheDir, "enhanced_craft_${mode.id}_$timeStamp.jpg")

            FileOutputStream(enhancedFile).use { out ->
                enhancedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }

            if (rawBitmap != enhancedBitmap) {
                rawBitmap.recycle()
            }

            enhancedFile
        } catch (e: Exception) {
            Log.e("ImageEnhancer", "Failed to enhance image file: ${e.message}", e)
            inputFile
        }
    }

    /**
     * Enhances a sample drawable image and returns the file path.
     */
    suspend fun enhanceSampleDrawable(
        context: Context,
        resName: String,
        mode: CameraEnhancementMode
    ): File = withContext(Dispatchers.IO) {
        try {
            val resId = when (resName) {
                "img_pottery_sample" -> R.drawable.img_pottery_sample
                "img_artisan_hero" -> R.drawable.img_artisan_hero
                else -> R.drawable.img_saree_sample
            }

            val rawBitmap = BitmapFactory.decodeResource(context.resources, resId)
                ?: return@withContext File(context.cacheDir, "$resName.jpg")

            val enhancedBitmap = enhanceBitmap(rawBitmap, mode)

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(System.currentTimeMillis())
            val enhancedFile = File(context.cacheDir, "enhanced_sample_${mode.id}_$timeStamp.jpg")

            FileOutputStream(enhancedFile).use { out ->
                enhancedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }

            enhancedFile
        } catch (e: Exception) {
            Log.e("ImageEnhancer", "Failed to enhance sample drawable: ${e.message}", e)
            File(context.cacheDir, "$resName.jpg")
        }
    }
}
