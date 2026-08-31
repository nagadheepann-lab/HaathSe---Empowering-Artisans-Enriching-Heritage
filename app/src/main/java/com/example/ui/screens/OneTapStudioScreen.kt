package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.*
import com.example.data.local.ProductEntity
import com.example.data.models.BackgroundPreset
import com.example.data.models.SupportedLanguage
import com.example.data.repository.KarigarRepository
import com.example.ui.components.AudioPlayButton
import com.example.ui.components.CameraXLivePreview
import com.example.ui.components.SmartCraftImage
import com.example.ui.theme.*
import com.example.utils.AudioVoiceHelper
import com.example.utils.MultilingualManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.roundToInt

enum class StudioFlowStep {
    CAMERA_CAPTURE,
    VOICE_DESCRIPTION,
    PHOTO_PROCESSING,
    BEFORE_AFTER_STUDIO,
    CATALOG_PROCESSING,
    CATALOG_REVIEW,
    CRAFT_STORY,
    CRAFT_ANALYZER,
    SMART_PRICING,
    FINAL_PREVIEW,
    PUBLISH_SUCCESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneTapStudioScreen(
    currentLanguage: SupportedLanguage,
    repository: KarigarRepository,
    audioHelper: AudioVoiceHelper?,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Domain Services
    val imageProcessingService = remember { DemoImageProcessingService() }
    val speechService = remember { AndroidSpeechRecognitionService(context) }
    val catalogService = remember { GeminiCatalogService(context) }
    val craftStoryService = remember { GeminiCraftStoryService() }
    val craftAnalyzerService = remember { GeminiCraftAnalyzerService() }
    val pricingEngine = remember { PricingEngine() }

    // Navigation & Flow State
    var currentStep by remember { mutableStateOf(StudioFlowStep.CAMERA_CAPTURE) }

    // Photo Capture & Enhancement State
    var capturedPhotoUriOrRes by remember { mutableStateOf("img_saree_sample") }
    var processingPipelineStep by remember { mutableStateOf(ProcessingPipelineStep.BACKGROUND_REMOVAL) }
    var processingProgress by remember { mutableStateOf(0.1f) }
    var selectedBgPreset by remember { mutableStateOf(BackgroundPreset.TRADITIONAL_INDIAN) }
    var lightingBoost by remember { mutableStateOf(0.85f) }
    var showBeforeState by remember { mutableStateOf(false) }

    // Voice Description State
    val defaultSample = MultilingualManager.getVoiceSamples(currentLanguage).firstOrNull()
        ?: "This is an authentic handwoven Kanchipuram silk saree with peacock zari border, 5 days handloom work, raw silk cost ₹2100."
    var voiceTranscriptText by remember { mutableStateOf(defaultSample) }
    var isListeningSpeech by remember { mutableStateOf(false) }
    var speechErrorMessage by remember { mutableStateOf<String?>(null) }

    // Catalog Extracted State
    var catalogResult by remember { mutableStateOf<CatalogExtractResult?>(null) }
    var editTitle by remember { mutableStateOf("Handwoven Kanchipuram Pure Silk Saree") }
    var editCategory by remember { mutableStateOf("Handloom & Textiles") }
    var editMaterial by remember { mutableStateOf("Pure Mulberry Silk & Gold Zari Thread") }
    var editCraftTechnique by remember { mutableStateOf("Korvai Interlock Handloom Weaving") }
    var editDimensions by remember { mutableStateOf("5.5m Length + 0.8m Blouse Piece") }
    var editProductionTime by remember { mutableStateOf("5 Days (40 Hours dedicated artisan labor)") }
    var editCare by remember { mutableStateOf("Dry clean only. Store wrapped in muslin cloth.") }
    var editDescEn by remember { mutableStateOf("Authentic GI-recognized handloom silk saree handcrafted with traditional Korvai interlocking weave.") }
    var editDescHi by remember { mutableStateOf("पारंपरिक कोरवई बुनाई तकनीक से निर्मित शुद्ध कांचीपुरम सिल्क साड़ी।") }
    var editKeywords by remember { mutableStateOf(listOf("Handloom", "SilkSaree", "Korvai", "GI_Tag", "ArtisanDirect")) }
    var selectedDescTab by remember { mutableStateOf(0) }

    // Craft Story State
    var craftStoryResult by remember { mutableStateOf<CraftStoryResult?>(null) }
    var editStoryHighlightBadge by remember { mutableStateOf("❤️ MADE WITH TRADITION") }
    var editStoryHighlightSummary by remember { mutableStateOf("A weaving technique passed down through generations.") }
    var editStoryFullText by remember { mutableStateOf("Each piece took 5 days of careful handwork on traditional pit looms.") }
    var isStoryApproved by remember { mutableStateOf(false) }

    // Craft Analyzer State
    var craftAnalysisResult by remember { mutableStateOf<CraftAnalysisResult?>(null) }
    var confirmedMaterial by remember { mutableStateOf(editMaterial) }
    var hasConfirmedUncertainty by remember { mutableStateOf(false) }

    // Smart Pricing State
    var rawMaterialCost by remember { mutableDoubleStateOf(2100.0) }
    var laborHours by remember { mutableDoubleStateOf(40.0) }
    var productionDays by remember { mutableIntStateOf(5) }
    var pricingRecommendation by remember { mutableStateOf<PricingRecommendation?>(null) }
    var chosenPrice by remember { mutableDoubleStateOf(3600.0) }
    var stockQuantity by remember { mutableIntStateOf(3) }

    // Execute Image Pipeline
    fun runPhotoPipeline(photoPath: String) {
        capturedPhotoUriOrRes = photoPath
        currentStep = StudioFlowStep.PHOTO_PROCESSING
        coroutineScope.launch {
            audioHelper?.speak("Enhancing your craft photograph with studio daylight and background cleanup.", currentLanguage)
            imageProcessingService.processImage(photoPath) { step, progress ->
                processingPipelineStep = step
                processingProgress = progress
            }
            delay(300)
            currentStep = StudioFlowStep.BEFORE_AFTER_STUDIO
            audioHelper?.speak("Your studio photo is ready. Check the before and after comparison.", currentLanguage)
        }
    }

    // Execute Catalog Generation
    fun runCatalogGeneration() {
        currentStep = StudioFlowStep.CATALOG_PROCESSING
        coroutineScope.launch {
            audioHelper?.speak("Analyzing your craft description with AI to build the master catalog.", currentLanguage)
            val result = catalogService.generateCatalog(
                transcript = voiceTranscriptText,
                artisanLanguage = currentLanguage,
                imageHint = capturedPhotoUriOrRes
            )
            catalogResult = result
            editTitle = result.productName
            editCategory = result.category
            editMaterial = result.material
            editCraftTechnique = result.craftTechnique
            editDimensions = result.dimensions
            editProductionTime = result.productionTime
            editCare = result.careInstructions
            editDescEn = result.englishDescription
            editDescHi = result.hindiDescription
            editKeywords = result.keywords
            rawMaterialCost = result.rawMaterialCost
            laborHours = result.laborHours.toDouble()
            productionDays = result.productionDays

            delay(400)
            currentStep = StudioFlowStep.CATALOG_REVIEW
            audioHelper?.speak("AI Catalog ready! Review your product details, heritage technique, and descriptions.", currentLanguage)
        }
    }

    // Execute Story Generation
    fun runStoryGeneration() {
        coroutineScope.launch {
            val story = craftStoryService.generateStory(
                transcript = voiceTranscriptText,
                artisanName = "Lakshmi Ammal",
                region = "Kanchipuram, Tamil Nadu",
                language = currentLanguage
            )
            craftStoryResult = story
            editStoryHighlightBadge = story.highlightBadge
            editStoryHighlightSummary = story.highlightSummary
            editStoryFullText = story.fullStory
            isStoryApproved = false
            currentStep = StudioFlowStep.CRAFT_STORY
            audioHelper?.speak("Every handmade product has a story. Review the genuine facts extracted from your voice.", currentLanguage)
        }
    }

    // Execute Analyzer
    fun runCraftAnalysis() {
        coroutineScope.launch {
            val analysis = craftAnalyzerService.analyzeCraft(
                imageIdentifier = capturedPhotoUriOrRes,
                transcript = voiceTranscriptText,
                existingTitle = editTitle,
                existingCategory = editCategory
            )
            craftAnalysisResult = analysis
            confirmedMaterial = analysis.probableMaterial.value
            currentStep = StudioFlowStep.CRAFT_ANALYZER
            audioHelper?.speak("AI Craft Analyzer has inspected your materials and technique confidence scores.", currentLanguage)
        }
    }

    // Execute Smart Pricing
    fun runPricingEngine() {
        val recommendation = pricingEngine.calculateRecommendation(
            rawMaterialCost = rawMaterialCost,
            laborHours = laborHours,
            productionDays = productionDays,
            category = editCategory
        )
        pricingRecommendation = recommendation
        chosenPrice = recommendation.recommendedPrice
        currentStep = StudioFlowStep.SMART_PRICING
        audioHelper?.speak("Smart Pricing calculated fair sustainable rates based on materials, skilled labor, and market demand.", currentLanguage)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Product Studio",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Show us your craft. HaathSe will help with the rest.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TerracottaPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            when (currentStep) {
                                StudioFlowStep.CAMERA_CAPTURE -> onNavigateHome()
                                StudioFlowStep.VOICE_DESCRIPTION -> currentStep = StudioFlowStep.CAMERA_CAPTURE
                                StudioFlowStep.PHOTO_PROCESSING -> currentStep = StudioFlowStep.VOICE_DESCRIPTION
                                StudioFlowStep.BEFORE_AFTER_STUDIO -> currentStep = StudioFlowStep.VOICE_DESCRIPTION
                                StudioFlowStep.CATALOG_PROCESSING -> currentStep = StudioFlowStep.BEFORE_AFTER_STUDIO
                                StudioFlowStep.CATALOG_REVIEW -> currentStep = StudioFlowStep.BEFORE_AFTER_STUDIO
                                StudioFlowStep.CRAFT_STORY -> currentStep = StudioFlowStep.CATALOG_REVIEW
                                StudioFlowStep.CRAFT_ANALYZER -> currentStep = StudioFlowStep.CRAFT_STORY
                                StudioFlowStep.SMART_PRICING -> currentStep = StudioFlowStep.CRAFT_ANALYZER
                                StudioFlowStep.FINAL_PREVIEW -> currentStep = StudioFlowStep.SMART_PRICING
                                StudioFlowStep.PUBLISH_SUCCESS -> onNavigateHome()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    AudioPlayButton(
                        textToSpeak = "Welcome to HaathSe AI Product Studio. Capture your craft, speak naturally, and let our AI create a certified B2B catalog.",
                        language = currentLanguage,
                        audioHelper = audioHelper
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(WarmBgLight)
        ) {
            // Visual Progress Timeline (8 sequential steps)
            StudioVisualTimeline(currentStep = currentStep)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (currentStep) {
                    // STEP 1: Photo
                    StudioFlowStep.CAMERA_CAPTURE -> {
                        CameraXLivePreview(
                            currentLanguage = currentLanguage,
                            audioHelper = audioHelper,
                            onPhotoCaptured = { photoPath ->
                                capturedPhotoUriOrRes = photoPath
                                currentStep = StudioFlowStep.VOICE_DESCRIPTION
                                audioHelper?.speak("Tell us about your product. Speak naturally, you don't need to type.", currentLanguage)
                            },
                            onSelectGallerySample = { sampleRes ->
                                capturedPhotoUriOrRes = sampleRes
                            }
                        )
                    }

                    // STEP 2: Tell
                    StudioFlowStep.VOICE_DESCRIPTION -> {
                        VoiceDescriptionScreen(
                            transcriptText = voiceTranscriptText,
                            onTranscriptChange = { voiceTranscriptText = it },
                            isListening = isListeningSpeech,
                            onToggleListening = {
                                if (isListeningSpeech) {
                                    speechService.stopListening()
                                    isListeningSpeech = false
                                } else {
                                    isListeningSpeech = true
                                    speechErrorMessage = null
                                    speechService.startListening(
                                        language = currentLanguage,
                                        onPartialResult = { partial ->
                                            voiceTranscriptText = partial
                                        },
                                        onFinalResult = { result ->
                                            voiceTranscriptText = result.transcript
                                            isListeningSpeech = false
                                        },
                                        onError = { error ->
                                            isListeningSpeech = false
                                            speechErrorMessage = error
                                        }
                                    )
                                }
                            },
                            errorMessage = speechErrorMessage,
                            currentLanguage = currentLanguage,
                            audioHelper = audioHelper,
                            onContinue = {
                                runPhotoPipeline(capturedPhotoUriOrRes)
                            }
                        )
                    }

                    // STEP 3: Enhance Pipeline
                    StudioFlowStep.PHOTO_PROCESSING -> {
                        PhotoProcessingPipelineScreen(
                            currentStep = processingPipelineStep,
                            progress = processingProgress,
                            photoRes = capturedPhotoUriOrRes
                        )
                    }

                    // STEP 3: Enhance (Before / After Studio)
                    StudioFlowStep.BEFORE_AFTER_STUDIO -> {
                        BeforeAfterComparisonScreen(
                            photoRes = capturedPhotoUriOrRes,
                            selectedPreset = selectedBgPreset,
                            onSelectPreset = { selectedBgPreset = it },
                            lightingBoost = lightingBoost,
                            onLightingBoostChange = { lightingBoost = it },
                            showBefore = showBeforeState,
                            onToggleBefore = { showBeforeState = !showBeforeState },
                            onUsePhoto = {
                                runCatalogGeneration()
                            },
                            onRetake = {
                                currentStep = StudioFlowStep.CAMERA_CAPTURE
                            },
                            currentLanguage = currentLanguage,
                            audioHelper = audioHelper
                        )
                    }

                    // STEP 4: Catalog Processing
                    StudioFlowStep.CATALOG_PROCESSING -> {
                        CatalogGeneratingScreen(language = currentLanguage)
                    }

                    // STEP 4: Catalog Review
                    StudioFlowStep.CATALOG_REVIEW -> {
                        if (catalogResult != null) {
                            CatalogReviewScreen(
                                catalog = catalogResult!!,
                                photoRes = capturedPhotoUriOrRes,
                                bgPreset = selectedBgPreset,
                                editTitle = editTitle,
                                onTitleChange = { editTitle = it },
                                editCategory = editCategory,
                                onCategoryChange = { editCategory = it },
                                editMaterial = editMaterial,
                                onMaterialChange = { editMaterial = it },
                                editCraftTechnique = editCraftTechnique,
                                onCraftTechniqueChange = { editCraftTechnique = it },
                                editDimensions = editDimensions,
                                onDimensionsChange = { editDimensions = it },
                                editProductionTime = editProductionTime,
                                onProductionTimeChange = { editProductionTime = it },
                                editCare = editCare,
                                onCareChange = { editCare = it },
                                editDescEn = editDescEn,
                                onDescEnChange = { editDescEn = it },
                                editDescHi = editDescHi,
                                onDescHiChange = { editDescHi = it },
                                editKeywords = editKeywords,
                                selectedDescTab = selectedDescTab,
                                onDescTabSelected = { selectedDescTab = it },
                                currentLanguage = currentLanguage,
                                audioHelper = audioHelper,
                                onRegenerate = {
                                    runCatalogGeneration()
                                },
                                onApproveAndPublish = {
                                    runStoryGeneration()
                                }
                            )
                        }
                    }

                    // STEP 5: Craft Story
                    StudioFlowStep.CRAFT_STORY -> {
                        CraftStoryScreen(
                            story = craftStoryResult,
                            highlightBadge = editStoryHighlightBadge,
                            onBadgeChange = { editStoryHighlightBadge = it },
                            highlightSummary = editStoryHighlightSummary,
                            onSummaryChange = { editStoryHighlightSummary = it },
                            fullStory = editStoryFullText,
                            onStoryChange = { editStoryFullText = it },
                            isApproved = isStoryApproved,
                            onApprove = { isStoryApproved = true },
                            onRegenerate = { runStoryGeneration() },
                            currentLanguage = currentLanguage,
                            audioHelper = audioHelper,
                            onContinue = {
                                runCraftAnalysis()
                            }
                        )
                    }

                    // STEP 6: AI Craft Analyzer
                    StudioFlowStep.CRAFT_ANALYZER -> {
                        if (craftAnalysisResult != null) {
                            CraftAnalyzerScreen(
                                analysis = craftAnalysisResult!!,
                                photoRes = capturedPhotoUriOrRes,
                                confirmedMaterial = confirmedMaterial,
                                onMaterialConfirmed = { confirmedMaterial = it; hasConfirmedUncertainty = true },
                                hasConfirmedUncertainty = hasConfirmedUncertainty,
                                currentLanguage = currentLanguage,
                                audioHelper = audioHelper,
                                onContinue = {
                                    editMaterial = confirmedMaterial
                                    runPricingEngine()
                                }
                            )
                        }
                    }

                    // STEP 7: Smart Pricing
                    StudioFlowStep.SMART_PRICING -> {
                        if (pricingRecommendation != null) {
                            SmartPricingScreen(
                                recommendation = pricingRecommendation!!,
                                chosenPrice = chosenPrice,
                                onPriceChange = { chosenPrice = it },
                                rawMaterialCost = rawMaterialCost,
                                onRawMaterialCostChange = {
                                    rawMaterialCost = it
                                    val updated = pricingEngine.calculateRecommendation(it, laborHours, productionDays, editCategory)
                                    pricingRecommendation = updated
                                    chosenPrice = updated.recommendedPrice
                                },
                                laborHours = laborHours,
                                onLaborHoursChange = {
                                    laborHours = it
                                    val updated = pricingEngine.calculateRecommendation(rawMaterialCost, it, productionDays, editCategory)
                                    pricingRecommendation = updated
                                    chosenPrice = updated.recommendedPrice
                                },
                                currentLanguage = currentLanguage,
                                audioHelper = audioHelper,
                                onContinue = {
                                    currentStep = StudioFlowStep.FINAL_PREVIEW
                                    audioHelper?.speak("Final preview ready! Review your complete product listing before publishing.", currentLanguage)
                                }
                            )
                        }
                    }

                    // STEP 8: Final Review & Publish
                    StudioFlowStep.FINAL_PREVIEW -> {
                        FinalListingPreviewScreen(
                            photoRes = capturedPhotoUriOrRes,
                            bgPreset = selectedBgPreset,
                            title = editTitle,
                            category = editCategory,
                            price = chosenPrice,
                            minSustainablePrice = pricingRecommendation?.minSustainablePrice ?: (chosenPrice * 0.8),
                            descriptionEn = editDescEn,
                            descriptionHi = editDescHi,
                            craftStory = editStoryFullText,
                            storyBadge = editStoryHighlightBadge,
                            material = editMaterial,
                            craftTechnique = editCraftTechnique,
                            productionTime = editProductionTime,
                            stockQuantity = stockQuantity,
                            onStockChange = { stockQuantity = it },
                            currentLanguage = currentLanguage,
                            audioHelper = audioHelper,
                            onPublishProduct = {
                                coroutineScope.launch {
                                    val newProduct = ProductEntity(
                                        id = "prod_" + UUID.randomUUID().toString().take(8),
                                        artisanId = "artisan_lakshmi",
                                        artisanName = "Lakshmi Ammal",
                                        title = editTitle,
                                        titleHindi = editDescHi.take(60),
                                        titleRegional = voiceTranscriptText.take(60),
                                        category = editCategory,
                                        craftTechnique = editCraftTechnique,
                                        region = "Kanchipuram, Tamil Nadu",
                                        rawMaterialCost = rawMaterialCost,
                                        laborHours = laborHours,
                                        productionDays = productionDays,
                                        suggestedPrice = pricingRecommendation?.recommendedPrice ?: chosenPrice,
                                        fairMinPrice = pricingRecommendation?.minSustainablePrice ?: (chosenPrice * 0.85),
                                        premiumPrice = pricingRecommendation?.premiumPrice ?: (chosenPrice * 1.25),
                                        activePrice = chosenPrice,
                                        listingScore = 98,
                                        materialsList = editMaterial,
                                        dimensions = editDimensions,
                                        weight = "850g",
                                        description = editDescEn,
                                        descriptionHindi = editDescHi,
                                        descriptionRegional = voiceTranscriptText,
                                        culturalStory = editStoryFullText,
                                        storyLineage = editStoryHighlightSummary,
                                        careInstructions = editCare,
                                        packagingSuggestions = "Heritage gift box with organic butter paper wrap",
                                        searchKeywords = editKeywords.joinToString(", "),
                                        stockQuantity = stockQuantity,
                                        soldQuantity = 0,
                                        reservedQuantity = 0,
                                        imageDrawableRes = capturedPhotoUriOrRes,
                                        enhancedImagePreset = selectedBgPreset.id,
                                        isVerified = true,
                                        isPublished = true,
                                        isOfflineDraft = false
                                    )
                                    repository.insertProduct(newProduct)
                                    currentStep = StudioFlowStep.PUBLISH_SUCCESS
                                    audioHelper?.speak("Your product is now live on the HaathSe marketplace!", currentLanguage)
                                }
                            }
                        )
                    }

                    // PUBLISH SUCCESS CELEBRATION
                    StudioFlowStep.PUBLISH_SUCCESS -> {
                        PublishSuccessCelebrationScreen(
                            title = editTitle,
                            category = editCategory,
                            price = chosenPrice,
                            photoRes = capturedPhotoUriOrRes,
                            listingScore = 98,
                            onDone = onNavigateHome,
                            onAddAnother = {
                                currentStep = StudioFlowStep.CAMERA_CAPTURE
                            },
                            currentLanguage = currentLanguage,
                            audioHelper = audioHelper
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TIMELINE COMPONENT
// -------------------------------------------------------------
@Composable
fun StudioVisualTimeline(currentStep: StudioFlowStep) {
    val steps = listOf(
        Pair("📷", "Photo"),
        Pair("🎤", "Tell"),
        Pair("✨", "Enhance"),
        Pair("📝", "Catalog"),
        Pair("❤️", "Story"),
        Pair("🧠", "Analyze"),
        Pair("💰", "Price"),
        Pair("🚀", "Publish")
    )

    val activeIndex = when (currentStep) {
        StudioFlowStep.CAMERA_CAPTURE -> 0
        StudioFlowStep.VOICE_DESCRIPTION -> 1
        StudioFlowStep.PHOTO_PROCESSING -> 2
        StudioFlowStep.BEFORE_AFTER_STUDIO -> 2
        StudioFlowStep.CATALOG_PROCESSING -> 3
        StudioFlowStep.CATALOG_REVIEW -> 3
        StudioFlowStep.CRAFT_STORY -> 4
        StudioFlowStep.CRAFT_ANALYZER -> 5
        StudioFlowStep.SMART_PRICING -> 6
        StudioFlowStep.FINAL_PREVIEW -> 7
        StudioFlowStep.PUBLISH_SUCCESS -> 7
    }

    val scrollState = rememberScrollState()

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, (emoji, name) ->
                val isDone = index < activeIndex
                val isCurrent = index == activeIndex

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isDone -> SuccessGreen
                                    isCurrent -> TerracottaPrimary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .border(
                                width = if (isCurrent) 1.5.dp else 0.dp,
                                color = if (isCurrent) GoldenAmberSecondary else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = emoji,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = name,
                        fontSize = 11.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isCurrent -> TerracottaPrimary
                            isDone -> SuccessGreen
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 2: VOICE DESCRIPTION COMPONENT
// -------------------------------------------------------------
@Composable
fun VoiceDescriptionScreen(
    transcriptText: String,
    onTranscriptChange: (String) -> Unit,
    isListening: Boolean,
    onToggleListening: () -> Unit,
    errorMessage: String?,
    currentLanguage: SupportedLanguage,
    audioHelper: AudioVoiceHelper?,
    onContinue: () -> Unit
) {
    val samples = MultilingualManager.getVoiceSamples(currentLanguage)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tell us about your craft",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = TerracottaPrimary
        )
        Text(
            text = "Speak naturally in your mother tongue. Tell us material, hours spent, raw costs, and heritage technique.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Big Voice Mic Button with Pulsing Effect
        val infiniteTransition = rememberInfiniteTransition()
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = if (isListening) 1.2f else 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    if (isListening) TerracottaPrimary.copy(alpha = 0.2f) else PeacockTealTertiary.copy(alpha = 0.15f)
                ),
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = onToggleListening,
                containerColor = if (isListening) TerracottaPrimary else PeacockTealTertiary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(72.dp)
                    .testTag("mic_toggle_button")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Microphone",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isListening) "Listening in ${currentLanguage.englishName}... Speak now" else "Tap to Speak",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isListening) TerracottaPrimary else PeacockTealTertiary
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = errorMessage,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Editable Live Transcript
        OutlinedTextField(
            value = transcriptText,
            onValueChange = onTranscriptChange,
            label = { Text("Spoken Voice Transcript") },
            minLines = 4,
            maxLines = 6,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("voice_transcript_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TerracottaPrimary,
                focusedLabelColor = TerracottaPrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Spoken Samples
        Text(
            text = "Or tap a sample phrase to test:",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(6.dp))
        samples.forEach { sample ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clickable { onTranscriptChange(sample) }
            ) {
                Text(
                    text = "“$sample”",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onContinue,
            enabled = transcriptText.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("continue_to_enhance_btn")
        ) {
            Icon(Icons.Default.AutoFixHigh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Enhance Photo & Generate Studio Catalog", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------
// STEP 3: PHOTO PIPELINE ANIMATION COMPONENT
// -------------------------------------------------------------
@Composable
fun PhotoProcessingPipelineScreen(
    currentStep: ProcessingPipelineStep,
    progress: Float,
    photoRes: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(72.dp),
            color = TerracottaPrimary,
            strokeWidth = 6.dp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "AI Photo Studio Processing",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = TerracottaPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = currentStep.stepName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = GoldenAmberSecondary
        )
    }
}

// -------------------------------------------------------------
// STEP 3: BEFORE / AFTER PHOTO STUDIO COMPONENT
// -------------------------------------------------------------
@Composable
fun BeforeAfterComparisonScreen(
    photoRes: String,
    selectedPreset: BackgroundPreset,
    onSelectPreset: (BackgroundPreset) -> Unit,
    lightingBoost: Float,
    onLightingBoostChange: (Float) -> Unit,
    showBefore: Boolean,
    onToggleBefore: () -> Unit,
    onUsePhoto: () -> Unit,
    onRetake: () -> Unit,
    currentLanguage: SupportedLanguage,
    audioHelper: AudioVoiceHelper?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI Photo Studio",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TerracottaPrimary
                )
                Text(
                    text = "Clean daylight, studio backdrop, and shadow enhancement.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AudioPlayButton(
                textToSpeak = "Your photo has been enhanced with studio lighting and clean Indian craft backdrop.",
                language = currentLanguage,
                audioHelper = audioHelper
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Image Preview with Before/After Toggle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            SmartCraftImage(
                imageIdentifier = photoRes,
                contentDescription = "Product Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Before / After Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.75f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clickable { onToggleBefore() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Compare, contentDescription = null, tint = GoldenAmberSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showBefore) "BEFORE (Raw)" else "AFTER (Enhanced)",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Studio Background Presets
        Text(
            text = "Select Studio Background Preset:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(BackgroundPreset.values()) { preset ->
                val isSelected = preset == selectedPreset
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) TerracottaPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { onSelectPreset(preset) }
                ) {
                    Text(
                        text = preset.label,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onRetake,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Retake")
            }

            Button(
                onClick = onUsePhoto,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp)
                    .testTag("use_studio_photo_btn")
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Use This Photo", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 4: CATALOG GENERATION LOADING SCREEN (Batch 10 Meaningful AI Progress)
// -------------------------------------------------------------
@Composable
fun CatalogGeneratingScreen(language: SupportedLanguage) {
    val aiSteps = remember {
        listOf(
            "Understanding your product...",
            "Creating your description...",
            "Finding the story behind your craft...",
            "Checking market trends...",
            "Preparing your listing..."
        )
    }
    var activeIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            activeIndex = (activeIndex + 1) % aiSteps.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(TerracottaPrimary.copy(alpha = 0.12f))
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = TerracottaPrimary,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "AI Product Studio",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = TerracottaPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedContent(
            targetState = aiSteps[activeIndex],
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(200))
            },
            label = "aiProgressStep"
        ) { stepText ->
            Text(
                text = stepText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeepCharcoalSurface,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = TerracottaPrimary,
            trackColor = TerracottaPrimary.copy(alpha = 0.15f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Translating heritage terminology into verified buyer standards...",
            fontSize = 11.sp,
            color = DeepCharcoalSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

// -------------------------------------------------------------
// STEP 4: CATALOG REVIEW COMPONENT
// -------------------------------------------------------------
@Composable
fun CatalogReviewScreen(
    catalog: CatalogExtractResult,
    photoRes: String,
    bgPreset: BackgroundPreset,
    editTitle: String,
    onTitleChange: (String) -> Unit,
    editCategory: String,
    onCategoryChange: (String) -> Unit,
    editMaterial: String,
    onMaterialChange: (String) -> Unit,
    editCraftTechnique: String,
    onCraftTechniqueChange: (String) -> Unit,
    editDimensions: String,
    onDimensionsChange: (String) -> Unit,
    editProductionTime: String,
    onProductionTimeChange: (String) -> Unit,
    editCare: String,
    onCareChange: (String) -> Unit,
    editDescEn: String,
    onDescEnChange: (String) -> Unit,
    editDescHi: String,
    onDescHiChange: (String) -> Unit,
    editKeywords: List<String>,
    selectedDescTab: Int,
    onDescTabSelected: (Int) -> Unit,
    currentLanguage: SupportedLanguage,
    audioHelper: AudioVoiceHelper?,
    onRegenerate: () -> Unit,
    onApproveAndPublish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI Catalog Draft",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TerracottaPrimary
                )
                Text(
                    text = "Review and fine-tune your structured specifications.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AudioPlayButton(
                textToSpeak = "AI has structured your product title as $editTitle, category $editCategory, and technique $editCraftTechnique.",
                language = currentLanguage,
                audioHelper = audioHelper
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Title Input
        OutlinedTextField(
            value = editTitle,
            onValueChange = onTitleChange,
            label = { Text("Product Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category & Technique
        OutlinedTextField(
            value = editCategory,
            onValueChange = onCategoryChange,
            label = { Text("Marketplace Category") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = editCraftTechnique,
            onValueChange = onCraftTechniqueChange,
            label = { Text("Craft Technique & Heritage") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Material & Dimensions
        OutlinedTextField(
            value = editMaterial,
            onValueChange = onMaterialChange,
            label = { Text("Material Composition") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = editDimensions,
            onValueChange = onDimensionsChange,
            label = { Text("Dimensions / Size") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = editProductionTime,
            onValueChange = onProductionTimeChange,
            label = { Text("Artisan Labor Duration") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Description Tabs (English / Hindi)
        TabRow(
            selectedTabIndex = selectedDescTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Tab(
                selected = selectedDescTab == 0,
                onClick = { onDescTabSelected(0) },
                text = { Text("English Description") }
            )
            Tab(
                selected = selectedDescTab == 1,
                onClick = { onDescTabSelected(1) },
                text = { Text("Hindi (हिंदी)") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedDescTab == 0) {
            OutlinedTextField(
                value = editDescEn,
                onValueChange = onDescEnChange,
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            OutlinedTextField(
                value = editDescHi,
                onValueChange = onDescHiChange,
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onApproveAndPublish,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("continue_to_story_btn")
        ) {
            Icon(Icons.Default.Favorite, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Proceed to Craft Story", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------
// STEP 5: CRAFT STORY SCREEN
// -------------------------------------------------------------
@Composable
fun CraftStoryScreen(
    story: CraftStoryResult?,
    highlightBadge: String,
    onBadgeChange: (String) -> Unit,
    highlightSummary: String,
    onSummaryChange: (String) -> Unit,
    fullStory: String,
    onStoryChange: (String) -> Unit,
    isApproved: Boolean,
    onApprove: () -> Unit,
    onRegenerate: () -> Unit,
    currentLanguage: SupportedLanguage,
    audioHelper: AudioVoiceHelper?,
    onContinue: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Craft Story",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TerracottaPrimary
                )
                Text(
                    text = "Every handmade product has a story.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            AudioPlayButton(
                textToSpeak = "$highlightBadge. $highlightSummary. $fullStory",
                language = currentLanguage,
                audioHelper = audioHelper
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Heritage Rule Notice Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = GoldenAmberLight.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, GoldenAmberSecondary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Zero-AI-Hallucination Policy: Story extracted strictly from your genuine spoken words.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Emotional Highlight Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = TerracottaLight,
                    border = BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = highlightBadge,
                        color = TerracottaPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = highlightSummary,
                        onValueChange = onSummaryChange,
                        label = { Text("Emotional Highlight Hook") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fullStory,
                        onValueChange = onStoryChange,
                        label = { Text("Craft Narrative") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "\"$highlightSummary\"",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fullStory,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 19.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Extracted Facts Chips
                if (story?.extractedFacts?.isNotEmpty() == true) {
                    Text(
                        text = "Genuine Facts Extracted:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    story.extractedFacts.forEach { fact ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = fact.icon, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${fact.category}: ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary
                                )
                                Text(
                                    text = fact.factText,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions: Edit, Regenerate, Listen, Approve
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { isEditing = !isEditing },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(if (isEditing) Icons.Default.Done else Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEditing) "Done" else "Edit", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onRegenerate,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Regenerate", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onApprove()
                            audioHelper?.speak("Story approved!", currentLanguage)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isApproved) SuccessGreen else TerracottaPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(if (isApproved) Icons.Default.CheckCircle else Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isApproved) "Approved" else "Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onContinue,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("continue_to_analyzer_btn")
        ) {
            Icon(Icons.Default.Psychology, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Proceed to AI Craft Analyzer", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------
// STEP 6: AI CRAFT ANALYZER SCREEN
// -------------------------------------------------------------
@Composable
fun CraftAnalyzerScreen(
    analysis: CraftAnalysisResult,
    photoRes: String,
    confirmedMaterial: String,
    onMaterialConfirmed: (String) -> Unit,
    hasConfirmedUncertainty: Boolean,
    currentLanguage: SupportedLanguage,
    audioHelper: AudioVoiceHelper?,
    onContinue: () -> Unit
) {
    var tempMaterialInput by remember { mutableStateOf(confirmedMaterial) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI Craft Analyzer",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TerracottaPrimary
                )
                Text(
                    text = "Inspecting material purity, weave signature, and provenance sources.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AudioPlayButton(
                textToSpeak = "AI Craft Analyzer verified your craft. Authenticity score is ${analysis.authenticityScore} percent.",
                language = currentLanguage,
                audioHelper = audioHelper
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Score Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = TerracottaLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Craft Authenticity Score",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerracottaPrimary
                    )
                    Text(
                        text = "Zero synthetic patterns detected",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = TerracottaPrimary
                ) {
                    Text(
                        text = "${analysis.authenticityScore}%",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Analyzed Fields with Source Labels
        AnalyzedFieldRow(field = analysis.probableMaterial, overrideValue = confirmedMaterial)
        Spacer(modifier = Modifier.height(8.dp))
        AnalyzedFieldRow(field = analysis.craftTechnique)
        Spacer(modifier = Modifier.height(8.dp))
        AnalyzedFieldRow(field = analysis.category)
        Spacer(modifier = Modifier.height(8.dp))
        AnalyzedFieldRow(field = analysis.inferredDimensions)

        Spacer(modifier = Modifier.height(14.dp))

        // Visual Characteristics
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Visual Characteristics (AI Vision)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                analysis.visualCharacteristics.forEach { char ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = char, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // Uncertainty Dialog / Prompt if Material is uncertain or needs artisan confirmation
        if (analysis.probableMaterial.isUncertain && !hasConfirmedUncertainty) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFFFFBEB),
                border = BorderStroke(1.dp, GoldenAmberSecondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = GoldenAmberSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Please confirm the material",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF92400E)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "We don't publish uncertain information to buyers. Please verify the exact fabric or material composition.",
                        fontSize = 11.sp,
                        color = Color(0xFF92400E)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempMaterialInput,
                        onValueChange = { tempMaterialInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onMaterialConfirmed(tempMaterialInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldenAmberSecondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Confirm Material", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onContinue,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("continue_to_pricing_btn")
        ) {
            Icon(Icons.Default.CurrencyRupee, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Proceed to Smart Pricing", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AnalyzedFieldRow(field: AnalyzedField, overrideValue: String? = null) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = field.fieldName,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = overrideValue ?: field.value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when (field.provenance) {
                    CraftFieldProvenanceType.ARTISAN_PROVIDED -> PeacockTealLight
                    CraftFieldProvenanceType.AI_ESTIMATED -> GoldenAmberLight
                },
                border = BorderStroke(
                    1.dp,
                    when (field.provenance) {
                        CraftFieldProvenanceType.ARTISAN_PROVIDED -> PeacockTealTertiary
                        CraftFieldProvenanceType.AI_ESTIMATED -> GoldenAmberSecondary
                    }
                )
            ) {
                Text(
                    text = when (field.provenance) {
                        CraftFieldProvenanceType.ARTISAN_PROVIDED -> "ARTISAN PROVIDED"
                        CraftFieldProvenanceType.AI_ESTIMATED -> "AI ESTIMATED — ${field.confidencePercentage}%"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (field.provenance) {
                        CraftFieldProvenanceType.ARTISAN_PROVIDED -> PeacockTealTertiary
                        CraftFieldProvenanceType.AI_ESTIMATED -> Color(0xFFB45309)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 7: SMART PRICING SCREEN
// -------------------------------------------------------------
@Composable
fun SmartPricingScreen(
    recommendation: PricingRecommendation,
    chosenPrice: Double,
    onPriceChange: (Double) -> Unit,
    rawMaterialCost: Double,
    onRawMaterialCostChange: (Double) -> Unit,
    laborHours: Double,
    onLaborHoursChange: (Double) -> Unit,
    currentLanguage: SupportedLanguage,
    audioHelper: AudioVoiceHelper?,
    onContinue: () -> Unit
) {
    val breakdown = recommendation.costBreakdown

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Smart Price",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TerracottaPrimary
                )
                Text(
                    text = recommendation.explanation,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AudioPlayButton(
                textToSpeak = "Suggested price range is ₹${recommendation.lowPrice.roundToInt()} to ₹${recommendation.premiumPrice.roundToInt()}. Recommended price is ₹${recommendation.recommendedPrice.roundToInt()}.",
                language = currentLanguage,
                audioHelper = audioHelper
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Visually Impressive Pricing Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SUGGESTED RANGE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "₹${recommendation.lowPrice.roundToInt()} — ₹${recommendation.premiumPrice.roundToInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TerracottaPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "RECOMMENDED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PeacockTealTertiary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "₹${recommendation.recommendedPrice.roundToInt()}",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = PeacockTealTertiary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Based on estimated costs, similar products and current demand.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Price Option Chips: LOW, RECOMMENDED, PREMIUM
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PriceTierChip(
                        tierName = "LOW",
                        price = recommendation.lowPrice,
                        isSelected = chosenPrice == recommendation.lowPrice,
                        onClick = { onPriceChange(recommendation.lowPrice) },
                        modifier = Modifier.weight(1f)
                    )
                    PriceTierChip(
                        tierName = "RECOMMENDED",
                        price = recommendation.recommendedPrice,
                        isSelected = chosenPrice == recommendation.recommendedPrice,
                        onClick = { onPriceChange(recommendation.recommendedPrice) },
                        modifier = Modifier.weight(1.2f)
                    )
                    PriceTierChip(
                        tierName = "PREMIUM",
                        price = recommendation.premiumPrice,
                        isSelected = chosenPrice == recommendation.premiumPrice,
                        onClick = { onPriceChange(recommendation.premiumPrice) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Slider for Artisan Custom Choice
                Text(
                    text = "Your Selected Price: ₹${chosenPrice.roundToInt()}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerracottaPrimary
                )

                Slider(
                    value = chosenPrice.toFloat(),
                    onValueChange = { onPriceChange(it.toDouble().roundToInt().toDouble()) },
                    valueRange = (recommendation.lowPrice.toFloat() * 0.85f)..(recommendation.premiumPrice.toFloat() * 1.3f),
                    colors = SliderDefaults.colors(
                        thumbColor = TerracottaPrimary,
                        activeTrackColor = TerracottaPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("price_slider")
                )

                Text(
                    text = "The artisan always chooses the final price.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cost Breakdown Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Cost Breakdown",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                CostItemRow("Raw Materials", breakdown.rawMaterials)
                CostItemRow("Skilled Labour (${laborHours.roundToInt()} hrs)", breakdown.skilledLabour)
                CostItemRow("Packaging", breakdown.packaging)
                CostItemRow("Estimated Platform Costs", breakdown.estimatedPlatformCosts)

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                CostItemRow("Minimum Sustainable Price", breakdown.minimumSustainablePrice, isBold = true, color = Color(0xFFB45309))
                CostItemRow("Recommended Price", breakdown.recommendedPrice, isBold = true, color = PeacockTealTertiary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onContinue,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("continue_to_final_review_btn")
        ) {
            Icon(Icons.Default.Visibility, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Preview Complete Listing", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PriceTierChip(
    tierName: String,
    price: Double,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) TerracottaPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = tierName,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "₹${price.roundToInt()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CostItemRow(label: String, value: Double, isBold: Boolean = false, color: Color = Color.Unspecified) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "₹${value.roundToInt()}",
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface
        )
    }
}

// -------------------------------------------------------------
// STEP 8: FINAL LISTING PREVIEW SCREEN
// -------------------------------------------------------------
@Composable
fun FinalListingPreviewScreen(
    photoRes: String,
    bgPreset: BackgroundPreset,
    title: String,
    category: String,
    price: Double,
    minSustainablePrice: Double,
    descriptionEn: String,
    descriptionHi: String,
    craftStory: String,
    storyBadge: String,
    material: String,
    craftTechnique: String,
    productionTime: String,
    stockQuantity: Int,
    onStockChange: (Int) -> Unit,
    currentLanguage: SupportedLanguage,
    audioHelper: AudioVoiceHelper?,
    onPublishProduct: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Final Listing Review",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TerracottaPrimary
                )
                Text(
                    text = "This is how buyers across India and worldwide will see your craft.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AudioPlayButton(
                textToSpeak = "Review your listing for $title at price of ₹${price.roundToInt()}. Tap Publish Product when ready.",
                language = currentLanguage,
                audioHelper = audioHelper
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Customer Facing Product Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Product Image Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    SmartCraftImage(
                        imageIdentifier = photoRes,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Certified GI Tag & Verification Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.75f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = GoldenAmberSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("GI Certified Craft", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Price Tag
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TerracottaPrimary,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "₹${price.roundToInt()}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Crafted by Lakshmi Ammal • Kanchipuram, Tamil Nadu",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Story Highlight
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = TerracottaLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = storyBadge,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = craftStory,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Specification Matrix
                    Text(text = "Craft Specifications", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))

                    SpecRow("Material", material)
                    SpecRow("Technique", craftTechnique)
                    SpecRow("Production Time", productionTime)
                    SpecRow("Fair Min Price", "₹${minSustainablePrice.roundToInt()} (Guaranteed)")

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stock Quantity Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Initial Stock Available:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (stockQuantity > 1) onStockChange(stockQuantity - 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease")
                            }
                            Text(
                                text = "$stockQuantity units",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { onStockChange(stockQuantity + 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Publish Button
        Button(
            onClick = onPublishProduct,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("publish_product_button")
        ) {
            Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("Publish Product", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// -------------------------------------------------------------
// PUBLISH SUCCESS CELEBRATION SCREEN
// -------------------------------------------------------------
@Composable
fun PublishSuccessCelebrationScreen(
    title: String,
    category: String,
    price: Double,
    photoRes: String,
    listingScore: Int,
    onDone: () -> Unit,
    onAddAnother: () -> Unit,
    currentLanguage: SupportedLanguage,
    audioHelper: AudioVoiceHelper?
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(SuccessGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🎉", fontSize = 48.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your product is now live!",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = TerracottaPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Your masterwork is published with certified heritage verification and smart fair-trade pricing.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Product Live Snapshot Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmartCraftImage(
                    imageIdentifier = photoRes,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "₹${price.roundToInt()} • $category",
                        fontSize = 12.sp,
                        color = TerracottaPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "ACTIVE",
                        color = SuccessGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons: View Product, Share, Add Another Product, Go to Dashboard
        Button(
            onClick = onDone,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("view_live_product_btn")
        ) {
            Icon(Icons.Default.Visibility, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Product", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Check out my handcrafted $title on HaathSe")
                    putExtra(Intent.EXTRA_TEXT, "Look at my handcrafted $title available directly on HaathSe marketplace for ₹${price.roundToInt()}! Certified authentic Indian handicraft.")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share your craft"))
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share Listing")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onAddAnother,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Another Product")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go to Dashboard", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
