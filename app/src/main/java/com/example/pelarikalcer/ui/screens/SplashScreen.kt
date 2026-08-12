package com.example.pelarikalcer.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pelarikalcer.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashDone: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "logo_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow_alpha"
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
        delay(2200)
        onSplashDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(CardSurface, DeepNavy),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Glow ring
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(NeonGreen.copy(alpha = glowAlpha * 0.3f), CircleShape)
        )

        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(tween(600, easing = EaseOutBack)) + fadeIn(tween(600))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale)
                        .background(
                            Brush.linearGradient(listOf(NeonGreen, Color(0xFF00C896))),
                            RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = DeepNavy,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "PelariKalcer",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Text(
                    text = "Berlari lebih cerdas",
                    fontSize = 14.sp,
                    color = NeonGreen,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )
            }
        }

        // Bottom version text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800, delayMillis = 400))
            ) {
                Text("v1.0.0", color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}

private val EaseOutBack = Easing { t ->
    val c1 = 1.70158f
    val c3 = c1 + 1f
    (1 + c3 * Math.pow((t - 1).toDouble(), 3.0) + c1 * Math.pow((t - 1).toDouble(), 2.0)).toFloat()
}
