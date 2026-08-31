package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ai.CameraEnhancementMode
import com.example.ai.DynamicCameraGuidanceManager
import com.example.ai.DynamicGuidanceState
import com.example.ai.ImageEnhancer
import com.example.data.models.SupportedLanguage
import com.example.ui.theme.GoldenAmberSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.utils.AudioVoiceHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.sqrt

enum class FlashModeState {
    OFF,
    ON,
    AUTO
}

@Composable
fun CameraXLivePreview(
    currentLanguage: SupportedLanguage,
    audioHelper: AudioVoiceHelper?,
    onPhotoCaptured: (imageIdentifier: String) -> Unit,
    onSelectGallerySample: (sampleRes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var flashMode by remember { mutableStateOf(FlashModeState.AUTO) }
    var isLensFacingBack by remember { mutableStateOf(true) }
    var isVoiceGuidanceEnabled by remember { mutableStateOf(true) }
    var isCameraHardwareAvailable by remember { mutableStateOf(true) }
    var selectedSampleDrawable by remember { mutableStateOf("img_saree_sample") }
    var isCapturingAndEnhancing by remember { mutableStateOf(false) }

    // Selected Enhancement Mode (Three buttons in the camera module)
    var selectedEnhancementMode by remember { mutableStateOf(CameraEnhancementMode.STUDIO_PRO) }

    // Dynamic Live Analysis State
    var currentGuidanceState by remember { mutableStateOf(DynamicGuidanceState.ANALYZING) }
    var liveLuminance by remember { mutableFloatStateOf(120f) }
    var liveDeviceTilt by remember { mutableFloatStateOf(0f) }
    var liveShakeMagnitude by remember { mutableFloatStateOf(0f) }
    var liveSubjectVariance by remember { mutableFloatStateOf(45f) }

    val currentGuidanceText = remember(currentGuidanceState, currentLanguage) {
        DynamicCameraGuidanceManager.getGuidanceText(currentGuidanceState, currentLanguage)
    }

    // Pulse animation for shutter button
    val infiniteTransition = rememberInfiniteTransition(label = "shutterPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Accelerometer Sensor for real tilt and shake analysis
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        var lastX = 0f
        var lastY = 0f
        var lastZ = 0f
        var isFirstReading = true

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]

                    if (!isFirstReading) {
                        val dx = abs(x - lastX)
                        val dy = abs(y - lastY)
                        val dz = abs(z - lastZ)
                        val delta = sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
                        liveShakeMagnitude = delta
                    } else {
                        isFirstReading = false
                    }
                    lastX = x
                    lastY = y
                    lastZ = z

                    // Calculate tilt relative to vertical holding
                    val tilt = abs(y)
                    liveDeviceTilt = tilt
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (accelSensor != null) {
            sensorManager.registerListener(sensorListener, accelSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(sensorListener)
        }
    }

    // Dynamic State Evaluator & Speaker based on real-time lighting, position, and sensor data
    LaunchedEffect(liveLuminance, liveDeviceTilt, liveShakeMagnitude, liveSubjectVariance) {
        val newState = when {
            liveShakeMagnitude > 3.2f || liveDeviceTilt < 3.0f -> DynamicGuidanceState.HOLD_STEADY
            liveLuminance < 60f -> DynamicGuidanceState.TOO_DARK
            liveLuminance > 215f -> DynamicGuidanceState.TOO_BRIGHT
            liveSubjectVariance < 20f -> DynamicGuidanceState.MOVE_CLOSER
            else -> DynamicGuidanceState.PERFECT_FRAME
        }

        if (newState != currentGuidanceState) {
            currentGuidanceState = newState
            if (isVoiceGuidanceEnabled) {
                val cue = DynamicCameraGuidanceManager.getGuidanceText(newState, currentLanguage)
                audioHelper?.speak(cue, currentLanguage)
            }
        }
    }

    // Periodic Voice Guidance Refresh (speaks dynamic advice if still needed after 4.5s)
    LaunchedEffect(isVoiceGuidanceEnabled, currentLanguage, currentGuidanceState) {
        if (isVoiceGuidanceEnabled) {
            while (true) {
                delay(4500)
                if (currentGuidanceState != DynamicGuidanceState.PERFECT_FRAME) {
                    val cue = DynamicCameraGuidanceManager.getGuidanceText(currentGuidanceState, currentLanguage)
                    audioHelper?.speak(cue, currentLanguage)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission && isCameraHardwareAvailable) {
            // Live CameraX AndroidView with Live Frame ImageAnalysis
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val provider = cameraProviderFuture.get()
                            cameraProvider = provider

                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .setFlashMode(
                                    when (flashMode) {
                                        FlashModeState.ON -> ImageCapture.FLASH_MODE_ON
                                        FlashModeState.OFF -> ImageCapture.FLASH_MODE_OFF
                                        FlashModeState.AUTO -> ImageCapture.FLASH_MODE_AUTO
                                    }
                                )
                                .build()
                            imageCapture = capture

                            // Real-time Live Image Analysis Use Case
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            val analysisExecutor = Executors.newSingleThreadExecutor()
                            imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                try {
                                    val buffer = imageProxy.planes[0].buffer
                                    val data = ByteArray(buffer.remaining())
                                    buffer.get(data)

                                    if (data.isNotEmpty()) {
                                        var sum = 0L
                                        val step = maxOf(1, data.size / 1500)
                                        var count = 0
                                        for (i in 0 until data.size step step) {
                                            sum += (data[i].toInt() and 0xFF)
                                            count++
                                        }
                                        val avgLuma = if (count > 0) sum.toFloat() / count else 120f
                                        liveLuminance = avgLuma

                                        // Variance calculation for distance/texture
                                        var varSum = 0.0
                                        for (i in 0 until data.size step (step * 2)) {
                                            val pixel = (data[i].toInt() and 0xFF).toDouble()
                                            varSum += (pixel - avgLuma) * (pixel - avgLuma)
                                        }
                                        val variance = sqrt(varSum / maxOf(1, count / 2)).toFloat()
                                        liveSubjectVariance = variance
                                    }
                                } catch (e: Exception) {
                                    Log.e("CameraXAnalysis", "Error analyzing frame", e)
                                } finally {
                                    imageProxy.close()
                                }
                            }

                            val cameraSelector = if (isLensFacingBack) {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            } else {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            }

                            provider.unbindAll()
                            camera = provider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                capture,
                                imageAnalysis
                            )
                        } catch (exc: Exception) {
                            Log.e("CameraX", "Use case binding failed", exc)
                            isCameraHardwareAvailable = false
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Fallback for emulator / permission denied
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1B18)),
                contentAlignment = Alignment.Center
            ) {
                val imageRes = when (selectedSampleDrawable) {
                    "img_pottery_sample" -> R.drawable.img_pottery_sample
                    "img_artisan_hero" -> R.drawable.img_artisan_hero
                    else -> R.drawable.img_saree_sample
                }
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Craft Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 70.dp, start = 16.dp, end = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = GoldenAmberSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (!hasCameraPermission) "Camera permission required — select craft sample below" else "Live Craft View Ready",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Camera Framing Guide Overlay Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Center framing square
            val boxWidth = canvasWidth * 0.82f
            val boxHeight = boxWidth * 1.15f
            val left = (canvasWidth - boxWidth) / 2f
            val top = (canvasHeight - boxHeight) / 2.3f

            val cornerRadius = 24.dp.toPx()
            val strokeWidth = 3.dp.toPx()

            // Dynamic color for framing guide: Green if perfect, Amber if optimizing
            val guideColor = when (currentGuidanceState) {
                DynamicGuidanceState.PERFECT_FRAME -> Color(0xFF10B981)
                DynamicGuidanceState.HOLD_STEADY -> Color(0xFFF59E0B)
                DynamicGuidanceState.TOO_DARK -> Color(0xFFE11D48)
                DynamicGuidanceState.TOO_BRIGHT -> Color(0xFFF59E0B)
                DynamicGuidanceState.MOVE_CLOSER -> Color(0xFF3B82F6)
                DynamicGuidanceState.ANALYZING -> Color(0xFFF59E0B)
            }

            drawRoundRect(
                color = guideColor.copy(alpha = 0.85f),
                topLeft = Offset(left, top),
                size = Size(boxWidth, boxHeight),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f), 0f)
                )
            )

            // Center crosshair / focus reticle
            val centerX = canvasWidth / 2f
            val centerY = top + (boxHeight / 2f)
            val reticleLength = 20.dp.toPx()

            drawLine(
                color = guideColor.copy(alpha = 0.9f),
                start = Offset(centerX - reticleLength, centerY),
                end = Offset(centerX + reticleLength, centerY),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = guideColor.copy(alpha = 0.9f),
                start = Offset(centerX, centerY - reticleLength),
                end = Offset(centerX, centerY + reticleLength),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Top Control Bar (Flash, Dynamic Guidance Banner & Toggle, Flip Camera)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flash button
                IconButton(
                    onClick = {
                        flashMode = when (flashMode) {
                            FlashModeState.AUTO -> FlashModeState.ON
                            FlashModeState.ON -> FlashModeState.OFF
                            FlashModeState.OFF -> FlashModeState.AUTO
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = when (flashMode) {
                            FlashModeState.ON -> Icons.Default.FlashOn
                            FlashModeState.OFF -> Icons.Default.FlashOff
                            FlashModeState.AUTO -> Icons.Default.FlashAuto
                        },
                        contentDescription = "Flash mode",
                        tint = if (flashMode == FlashModeState.OFF) Color.White.copy(alpha = 0.6f) else GoldenAmberSecondary
                    )
                }

                // Voice guidance mute/unmute toggle
                IconButton(
                    onClick = {
                        isVoiceGuidanceEnabled = !isVoiceGuidanceEnabled
                        if (isVoiceGuidanceEnabled) {
                            val cue = DynamicCameraGuidanceManager.getGuidanceText(currentGuidanceState, currentLanguage)
                            audioHelper?.speak(cue, currentLanguage)
                        } else {
                            audioHelper?.stop()
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = if (isVoiceGuidanceEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Voice Guidance Toggle",
                        tint = if (isVoiceGuidanceEnabled) GoldenAmberSecondary else Color.Gray
                    )
                }

                // Flip camera button
                IconButton(
                    onClick = { isLensFacingBack = !isLensFacingBack },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Flip Camera",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // DYNAMIC MOTHER TONGUE GUIDANCE BANNER
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = when (currentGuidanceState) {
                    DynamicGuidanceState.PERFECT_FRAME -> Color(0xE6064E3B)
                    DynamicGuidanceState.TOO_DARK -> Color(0xE6881337)
                    DynamicGuidanceState.TOO_BRIGHT -> Color(0xE678350F)
                    DynamicGuidanceState.HOLD_STEADY -> Color(0xE678350F)
                    DynamicGuidanceState.MOVE_CLOSER -> Color(0xE61E3A8A)
                    DynamicGuidanceState.ANALYZING -> Color(0xE61F2937)
                },
                border = BorderStroke(
                    1.5.dp,
                    when (currentGuidanceState) {
                        DynamicGuidanceState.PERFECT_FRAME -> Color(0xFF10B981)
                        DynamicGuidanceState.TOO_DARK -> Color(0xFFF43F5E)
                        DynamicGuidanceState.TOO_BRIGHT -> Color(0xFFF59E0B)
                        DynamicGuidanceState.HOLD_STEADY -> Color(0xFFF59E0B)
                        DynamicGuidanceState.MOVE_CLOSER -> Color(0xFF60A5FA)
                        DynamicGuidanceState.ANALYZING -> Color.Gray
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        audioHelper?.speak(currentGuidanceText, currentLanguage)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentGuidanceState.iconEmoji,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentGuidanceText,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Speak instruction",
                        tint = GoldenAmberSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Bottom Controls: Three Enhancement Buttons, Shutter Button, Samples
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp, start = 12.dp, end = 12.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // THREE BUTTONS IN CAMERA MODULE: STUDIO PRO | HERITAGE WARM | VIBRANT DETAIL
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.75f),
                border = BorderStroke(1.dp, GoldenAmberSecondary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CHOOSE PHOTO ENHANCEMENT STYLE",
                        color = GoldenAmberSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CameraEnhancementMode.values().forEach { mode ->
                            val isSelected = mode == selectedEnhancementMode
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) TerracottaPrimary else Color.White.copy(alpha = 0.1f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) GoldenAmberSecondary else Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedEnhancementMode = mode
                                        val confirmText = when (mode) {
                                            CameraEnhancementMode.STUDIO_PRO -> "Studio Pro mode selected."
                                            CameraEnhancementMode.HERITAGE_WARM -> "Heritage Warm glow mode selected."
                                            CameraEnhancementMode.VIBRANT_DETAIL -> "Vibrant Detail mode selected."
                                        }
                                        audioHelper?.speak(confirmText, currentLanguage)
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = mode.iconEmoji,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = mode.title,
                                        color = if (isSelected) Color.White else Color.LightGray,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Sample selector chips for fallback / testing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CraftSampleThumbnailChip(
                    name = "Silk Saree",
                    resName = "img_saree_sample",
                    isSelected = selectedSampleDrawable == "img_saree_sample",
                    onClick = {
                        selectedSampleDrawable = "img_saree_sample"
                        onSelectGallerySample("img_saree_sample")
                    }
                )
                Spacer(modifier = Modifier.width(6.dp))
                CraftSampleThumbnailChip(
                    name = "Blue Pottery",
                    resName = "img_pottery_sample",
                    isSelected = selectedSampleDrawable == "img_pottery_sample",
                    onClick = {
                        selectedSampleDrawable = "img_pottery_sample"
                        onSelectGallerySample("img_pottery_sample")
                    }
                )
                Spacer(modifier = Modifier.width(6.dp))
                CraftSampleThumbnailChip(
                    name = "Handloom",
                    resName = "img_artisan_hero",
                    isSelected = selectedSampleDrawable == "img_artisan_hero",
                    onClick = {
                        selectedSampleDrawable = "img_artisan_hero"
                        onSelectGallerySample("img_artisan_hero")
                    }
                )
            }

            // Main Shutter Button Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery button
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            isCapturingAndEnhancing = true
                            val enhancedSample = ImageEnhancer.enhanceSampleDrawable(
                                context = context,
                                resName = selectedSampleDrawable,
                                mode = selectedEnhancementMode
                            )
                            isCapturingAndEnhancing = false
                            onSelectGallerySample(enhancedSample.absolutePath)
                            onPhotoCaptured(enhancedSample.absolutePath)
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = Color.White
                    )
                }

                // Primary Capture Shutter Button (Takes real picture & applies real enhancement)
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(TerracottaPrimary.copy(alpha = 0.35f))
                        .clickable(enabled = !isCapturingAndEnhancing) {
                            val capture = imageCapture
                            if (capture != null && hasCameraPermission && isCameraHardwareAvailable) {
                                isCapturingAndEnhancing = true
                                val rawPhotoFile = File(
                                    context.cacheDir,
                                    "raw_" + SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
                                )
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(rawPhotoFile).build()

                                capture.takePicture(
                                    outputOptions,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                            coroutineScope.launch {
                                                // Real image enhancement on the clicked photo
                                                val enhancedPhotoFile = ImageEnhancer.enhanceImageFile(
                                                    context = context,
                                                    inputFile = rawPhotoFile,
                                                    mode = selectedEnhancementMode
                                                )
                                                isCapturingAndEnhancing = false
                                                onPhotoCaptured(enhancedPhotoFile.absolutePath)
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            Log.e("CameraX", "Photo capture failed: ${exception.message}", exception)
                                            coroutineScope.launch {
                                                val enhancedSample = ImageEnhancer.enhanceSampleDrawable(
                                                    context = context,
                                                    resName = selectedSampleDrawable,
                                                    mode = selectedEnhancementMode
                                                )
                                                isCapturingAndEnhancing = false
                                                onPhotoCaptured(enhancedSample.absolutePath)
                                            }
                                        }
                                    }
                                )
                            } else {
                                coroutineScope.launch {
                                    isCapturingAndEnhancing = true
                                    val enhancedSample = ImageEnhancer.enhanceSampleDrawable(
                                        context = context,
                                        resName = selectedSampleDrawable,
                                        mode = selectedEnhancementMode
                                    )
                                    isCapturingAndEnhancing = false
                                    onPhotoCaptured(enhancedSample.absolutePath)
                                }
                            }
                        }
                        .testTag("camera_capture_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, TerracottaPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCapturingAndEnhancing) {
                            CircularProgressIndicator(
                                color = TerracottaPrimary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(TerracottaPrimary)
                            )
                        }
                    }
                }

                // Guidance Info / Repeat button
                IconButton(
                    onClick = {
                        audioHelper?.speak(currentGuidanceText, currentLanguage)
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Voice Guide",
                        tint = GoldenAmberSecondary
                    )
                }
            }
        }

        // Enhancing overlay if processing
        AnimatedVisibility(
            visible = isCapturingAndEnhancing,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, GoldenAmberSecondary),
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = TerracottaPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Enhancing Craft Photo (${selectedEnhancementMode.title})...",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Calibrating studio daylight & preserving authentic craft texture.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CraftSampleThumbnailChip(
    name: String,
    resName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) TerracottaPrimary else Color.Black.copy(alpha = 0.6f),
        border = BorderStroke(
            1.dp,
            if (isSelected) GoldenAmberSecondary else Color.White.copy(alpha = 0.3f)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Image,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color.LightGray,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = name,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
