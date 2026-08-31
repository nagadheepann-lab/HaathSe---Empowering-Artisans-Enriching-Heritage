package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.SupportedLanguage
import com.example.ui.components.HaathSePrimaryButton
import com.example.ui.theme.*
import com.example.utils.AudioVoiceHelper
import com.example.utils.MultilingualManager
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    currentLanguage: SupportedLanguage,
    audioHelper: AudioVoiceHelper?,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.6f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logo_scale"
    )

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "content_alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        audioHelper?.speak(
            "HaathSe. Made by hand. Your craft. Your story. Your business.",
            currentLanguage
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        HaathSeCreamBg,
                        HaathSeSand,
                        HaathSeTerracottaLight.copy(alpha = 0.5f)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Decorative Craft Background Circles
        Box(
            modifier = Modifier
                .size(320.dp)
                .alpha(0.08f)
                .clip(CircleShape)
                .background(HaathSeTerracotta)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alphaAnim)
        ) {
            // Embellished Logo Emblem
            Box(
                modifier = Modifier
                    .scale(scaleAnim)
                    .size(110.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(TerracottaGradient)
                    .border(3.dp, HaathSeMarigoldLight, RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PanTool,
                    contentDescription = "HaathSe Emblem",
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Name
            Text(
                text = "HAATHSE",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = HaathSeTerracottaDark
                )
            )

            // Meaning Badge
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(HaathSeMarigoldLight)
                    .border(1.dp, HaathSeMarigold.copy(alpha = 0.6f), PillShape)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "हाथ से • MADE BY HAND",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = HaathSeMarigoldDark,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tagline
            Text(
                text = "Your craft. Your story. Your business.",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = HaathSeTextPrimary,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "AI-Powered Direct Commerce & Heritage Linkage",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = HaathSeTextSecondary,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Get Started Action Button
            HaathSePrimaryButton(
                text = "Enter Platform →",
                onClick = onNavigateNext,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .testTag("btn_splash_get_started")
            )
        }

        // Bottom Trust Seal
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = HaathSeTerracotta,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Verified Indian Craft Heritage & Living Wage Commerce",
                style = MaterialTheme.typography.labelSmall,
                color = HaathSeTextTertiary
            )
        }
    }
}
