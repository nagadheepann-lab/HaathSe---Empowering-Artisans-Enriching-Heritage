package com.example.ui.screens.buyer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.TrustScoreDetails
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun TrustScoreExplanationDialog(
    trustScore: TrustScoreDetails,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = PeacockTealTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Artisan Trust Score",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Big Score Circular Badge
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PeacockTealTertiary, TerracottaPrimary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${trustScore.overallScore}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "OUT OF 100",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Calculated for ${trustScore.artisanName}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "HaathSe evaluates multi-dimensional verified integrity — not just subjective reviews.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Breakdown Items
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TrustScoreRow(
                        title = "Government & GI Certification",
                        points = "${trustScore.verifiedArtisanScore}/25 pts",
                        progress = trustScore.verifiedArtisanScore / 25f,
                        detail = "Verified Artisan & GI Handloom Identity",
                        color = SuccessGreen
                    )

                    TrustScoreRow(
                        title = "Completed Orders & Track Record",
                        points = "${trustScore.completedOrdersScore}/20 pts",
                        progress = trustScore.completedOrdersScore / 20f,
                        detail = "${trustScore.completedOrdersCount} orders successfully delivered",
                        color = PeacockTealTertiary
                    )

                    TrustScoreRow(
                        title = "Buyer Rating & Authenticity Reviews",
                        points = "${trustScore.buyerRatingsScore}/20 pts",
                        progress = trustScore.buyerRatingsScore / 20f,
                        detail = "${trustScore.averageRating} ★ average across all buyers",
                        color = GoldenAmberSecondary
                    )

                    TrustScoreRow(
                        title = "Order Fulfillment & Capacity Rate",
                        points = "${trustScore.fulfillmentRateScore}/15 pts",
                        progress = trustScore.fulfillmentRateScore / 15f,
                        detail = "${trustScore.fulfillmentRatePercent}% orders fulfilled accurately",
                        color = PeacockTealTertiary
                    )

                    TrustScoreRow(
                        title = "On-Time Dispatch & Delivery",
                        points = "${trustScore.deliveryPerformanceScore}/10 pts",
                        progress = trustScore.deliveryPerformanceScore / 10f,
                        detail = "${trustScore.onTimeDeliveryPercent}% shipments dispatched on time",
                        color = TerracottaPrimary
                    )

                    TrustScoreRow(
                        title = "Low Cancellation Integrity",
                        points = "${trustScore.cancellationScore}/10 pts",
                        progress = trustScore.cancellationScore / 10f,
                        detail = "${trustScore.cancellationRatePercent}% low order cancellation rate",
                        color = SuccessGreen
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PeacockTealTertiary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("Got It, Transparent & Fair", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun TrustScoreRow(
    title: String,
    points: String,
    progress: Float,
    detail: String,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = points, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = detail, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun VoiceSearchModal(
    onDismiss: () -> Unit,
    onQueryResult: (String) -> Unit
) {
    var isListening by remember { mutableStateOf(true) }
    var recognizedText by remember { mutableStateOf("") }
    val promptSamples = listOf(
        "Pure Kanchipuram Silk Sarees with Zari",
        "Jaipur Blue Pottery Ceramic Vases",
        "Channapatna Wooden Montessori Toys",
        "Bastar Lost-Wax Dhokra Figurines",
        "Authentic Madhubani Folk Art Scrolls"
    )

    val waveTransition = rememberInfiniteTransition(label = "voice_wave")
    val waveScale by waveTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(1600)
        recognizedText = "Handwoven Pure Silk Kanchipuram Saree"
        delay(1200)
        onQueryResult(recognizedText)
        onDismiss()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Voice Craft Search",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size((90 * waveScale).dp)
                        .clip(CircleShape)
                        .background(TerracottaPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(TerracottaPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Microphone",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (recognizedText.isEmpty()) "Listening... Speak any craft, artisan or region" else recognizedText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (recognizedText.isEmpty()) TerracottaPrimary else PeacockTealTertiary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Try saying:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    promptSamples.take(3).forEach { prompt ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onQueryResult(prompt)
                                    onDismiss()
                                }
                        ) {
                            Text(
                                text = "• \"$prompt\"",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
