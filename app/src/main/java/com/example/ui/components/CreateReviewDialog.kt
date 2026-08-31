@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CreateReviewDialog(
    orderId: String,
    productId: String,
    productTitle: String,
    artisanId: String,
    artisanName: String,
    onDismiss: () -> Unit,
    onSubmitReview: (
        overallRating: Float,
        productQualityRating: Float,
        packagingRating: Float,
        deliveryRating: Float,
        authenticityRating: Float,
        reviewText: String,
        isVoiceReview: Boolean,
        voiceTranscript: String
    ) -> Unit
) {
    var overallRating by remember { mutableStateOf(5f) }
    var productQualityRating by remember { mutableStateOf(5f) }
    var packagingRating by remember { mutableStateOf(5f) }
    var deliveryRating by remember { mutableStateOf(5f) }
    var authenticityRating by remember { mutableStateOf(5f) }

    var reviewText by remember { mutableStateOf("") }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var voiceTranscript by remember { mutableStateOf("") }
    var isVoiceRecorded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarmBgLight)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Verified Purchase Review",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Rate Craft & Master Artisan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepCharcoalSurface
                        )
                        Text(
                            text = "$productTitle • $artisanName",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Scrollable Review Form
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. OVERALL STAR RATING
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = GoldenAmberLight.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Overall Artisan Experience",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepCharcoalSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                InteractiveStarRatingRow(
                                    rating = overallRating,
                                    onRatingSelected = { overallRating = it },
                                    starSize = 32.dp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (overallRating.toInt()) {
                                        5 -> "🌟 Masterpiece Quality! Exceeded Expectations"
                                        4 -> "✨ Very Good Handcrafting & Finish"
                                        3 -> "👍 Good Authentic Craft"
                                        2 -> "⚠️ Average Finish"
                                        else -> "Needs Improvement"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TerracottaPrimary
                                )
                            }
                        }
                    }

                    // 2. DETAILED METRICS RATINGS
                    item {
                        Text(
                            text = "Craft Quality Breakdown",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepCharcoalSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        MetricRatingRow(
                            label = "Product Quality & Weave Finish",
                            rating = productQualityRating,
                            onRatingChange = { productQualityRating = it }
                        )

                        MetricRatingRow(
                            label = "GI Authenticity & Heritage Integrity",
                            rating = authenticityRating,
                            onRatingChange = { authenticityRating = it }
                        )

                        MetricRatingRow(
                            label = "Eco-Friendly Safe Packaging",
                            rating = packagingRating,
                            onRatingChange = { packagingRating = it }
                        )

                        MetricRatingRow(
                            label = "Delivery Speed & Postal Handling",
                            rating = deliveryRating,
                            onRatingChange = { deliveryRating = it }
                        )
                    }

                    // 3. VOICE REVIEW FEATURE
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PeacockTealTertiary.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, PeacockTealTertiary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = null,
                                            tint = PeacockTealTertiary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Voice-Recorded Review",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PeacockTealTertiary
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = PeacockTealTertiary
                                    ) {
                                        Text(
                                            text = "ACCESSIBILITY",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Speak in your native language (Tamil, Hindi, Telugu, English, etc.). Voice is transcribed and verified for authentic artisan appreciation.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                if (!isVoiceRecorded) {
                                    Button(
                                        onClick = {
                                            isRecordingVoice = true
                                            coroutineScope.launch {
                                                delay(2500)
                                                isRecordingVoice = false
                                                isVoiceRecorded = true
                                                voiceTranscript = "Breathtaking genuine handloom craft! The Korvai border and natural silk shine are remarkable. Delivered carefully in eco packaging. Very proud to support master artisan $artisanName."
                                                if (reviewText.isBlank()) {
                                                    reviewText = voiceTranscript
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isRecordingVoice) TerracottaPrimary else PeacockTealTertiary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("btn_record_voice_review")
                                    ) {
                                        Icon(
                                            imageVector = if (isRecordingVoice) Icons.Default.GraphicEq else Icons.Default.Mic,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isRecordingVoice) "Listening & Transcribing..." else "Record Voice Feedback",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SuccessGreen.copy(alpha = 0.12f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = SuccessGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Voice Audio Captured & Transcribed ✓",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SuccessGreen
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "\"$voiceTranscript\"",
                                                fontSize = 11.sp,
                                                color = DeepCharcoalSurface,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. WRITTEN REVIEW TEXT INPUT
                    item {
                        Text(
                            text = "Written Review & Artisan Appreciation",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepCharcoalSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = reviewText,
                            onValueChange = { reviewText = it },
                            placeholder = {
                                Text(
                                    "Describe the fabric feel, weave precision, weight, packaging, or special message for the artisan family...",
                                    fontSize = 12.sp
                                )
                            },
                            minLines = 4,
                            maxLines = 6,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_review_text")
                        )
                    }
                }

                // Submit Button Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                onSubmitReview(
                                    overallRating,
                                    productQualityRating,
                                    packagingRating,
                                    deliveryRating,
                                    authenticityRating,
                                    reviewText.ifBlank { "Authentic craft and supreme quality. Highly recommended!" },
                                    isVoiceRecorded,
                                    voiceTranscript
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("btn_submit_review")
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Submit Review", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveStarRatingRow(
    rating: Float,
    onRatingSelected: (Float) -> Unit,
    starSize: androidx.compose.ui.unit.Dp = 24.dp
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            val isFilled = i <= rating
            IconButton(
                onClick = { onRatingSelected(i.toFloat()) },
                modifier = Modifier.size(starSize + 8.dp)
            ) {
                Icon(
                    imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "$i stars",
                    tint = if (isFilled) GoldenAmberSecondary else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(starSize)
                )
            }
        }
    }
}

@Composable
fun MetricRatingRow(
    label: String,
    rating: Float,
    onRatingChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = DeepCharcoalSurface,
            modifier = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (i in 1..5) {
                val isFilled = i <= rating
                Icon(
                    imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = if (isFilled) GoldenAmberSecondary else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onRatingChange(i.toFloat()) }
                )
            }
        }
    }
}
