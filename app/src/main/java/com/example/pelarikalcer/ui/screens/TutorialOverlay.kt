package com.example.pelarikalcer.ui.screens

import android.content.Context
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pelarikalcer.ui.theme.*

data class TutorialStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val spotlightAlignment: Alignment,
    val spotlightOffset: Modifier = Modifier,
    val cardAlignment: Alignment = Alignment.Center
)

val tutorialSteps = listOf(
    TutorialStep(
        title = "Selamat Datang!",
        description = "Ini adalah Dashboard utamamu. Di sini kamu bisa lihat statistik, jarak tempuh, dan streak harianmu.",
        icon = Icons.Default.Home,
        spotlightAlignment = Alignment.Center,
        spotlightOffset = Modifier.offset(y = (-100).dp),
        cardAlignment = Alignment.BottomCenter
    ),
    TutorialStep(
        title = "Streakmu Penting!",
        description = "Berlari minimal 1 km setiap hari untuk menyalakan api streak. Poin akan terus bertambah!",
        icon = Icons.Default.LocalFireDepartment,
        spotlightAlignment = Alignment.TopCenter,
        spotlightOffset = Modifier.padding(top = 110.dp),
        cardAlignment = Alignment.Center
    ),
    TutorialStep(
        title = "Tanya PelariCoach",
        description = "Tekan tombol robot oranye di kanan atas untuk meminta saran latihan, tips pemulihan cedera, dan motivasi langsung dari AI!",
        icon = Icons.Default.SmartToy,
        spotlightAlignment = Alignment.TopEnd,
        spotlightOffset = Modifier.padding(top = 24.dp, end = 20.dp),
        cardAlignment = Alignment.Center
    ),
    TutorialStep(
        title = "Mulai Berlari",
        description = "Tekan tombol hijau besar 'MULAI LARI' untuk melacak larimu secara langsung. GPS akan aktif merekam rute.",
        icon = Icons.Default.DirectionsRun,
        spotlightAlignment = Alignment.Center,
        spotlightOffset = Modifier.offset(y = 120.dp),
        cardAlignment = Alignment.TopCenter
    ),
    TutorialStep(
        title = "Navigasi Menu",
        description = "Gunakan bar menu di bawah untuk membuka menu Utama, Toko Pet, Tantangan, Leaderboard, dan Profil.",
        icon = Icons.Default.Navigation,
        spotlightAlignment = Alignment.BottomCenter,
        spotlightOffset = Modifier.padding(bottom = 20.dp),
        cardAlignment = Alignment.Center
    )
)

fun setTutorialDone(context: Context) {
    context.getSharedPreferences("pelarikalcer_prefs", Context.MODE_PRIVATE)
        .edit().putBoolean("tutorial_done", true).apply()
}

fun isTutorialDone(context: Context): Boolean {
    return context.getSharedPreferences("pelarikalcer_prefs", Context.MODE_PRIVATE)
        .getBoolean("tutorial_done", false)
}

@Composable
fun TutorialOverlay(
    onFinish: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val step = tutorialSteps[currentStep]
    val isLast = currentStep == tutorialSteps.size - 1

    val infiniteTransition = rememberInfiniteTransition(label = "spotlight")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulse"
    )
    val arrowOffset by infiniteTransition.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "arrow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) {
                if (isLast) onFinish() else currentStep++
            }
    ) {
        // Spotlight Glow Ring pointing to the feature
        Box(
            modifier = Modifier
                .align(step.spotlightAlignment)
                .then(step.spotlightOffset)
                .size(76.dp)
                .background(NeonGreen.copy(alpha = 0.2f), CircleShape)
                .border(2.dp, NeonGreen, CircleShape)
                .scale(pulseScale)
        )

        // Indicator Arrow pointing to the spotlight
        Box(
            modifier = Modifier
                .align(step.spotlightAlignment)
                .then(step.spotlightOffset)
                .offset(
                    y = when (step.spotlightAlignment) {
                        Alignment.TopCenter, Alignment.TopEnd, Alignment.TopStart -> 50.dp + arrowOffset.dp
                        Alignment.BottomCenter -> (-50).dp - arrowOffset.dp
                        else -> 50.dp + arrowOffset.dp
                    }
                )
        ) {
            Icon(
                imageVector = when (step.spotlightAlignment) {
                    Alignment.TopCenter, Alignment.TopEnd, Alignment.TopStart -> Icons.Default.KeyboardArrowUp
                    Alignment.BottomCenter -> Icons.Default.KeyboardArrowDown
                    else -> Icons.Default.KeyboardArrowUp
                },
                contentDescription = null,
                tint = NeonGreen,
                modifier = Modifier.size(32.dp)
            )
        }

        // Info Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 64.dp),
            contentAlignment = step.cardAlignment
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                },
                label = "step_card"
            ) { stepIndex ->
                val s = tutorialSteps[stepIndex]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(2.dp, NeonGreen.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    Brush.linearGradient(listOf(NeonGreen.copy(alpha = 0.2f), AccentOrange.copy(alpha = 0.2f))),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(s.icon, null, tint = NeonGreen, modifier = Modifier.size(30.dp))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = s.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = s.description,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress bar-dots
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            tutorialSteps.indices.forEach { i ->
                                Box(
                                    modifier = Modifier
                                        .width(if (i == stepIndex) 20.dp else 6.dp)
                                        .height(6.dp)
                                        .background(
                                            if (i == stepIndex) NeonGreen else TextMuted,
                                            RoundedCornerShape(3.dp)
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isLast) {
                            Button(
                                onClick = onFinish,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                            ) {
                                Text("Ayo Mulai!", fontWeight = FontWeight.Bold, color = DeepNavy, fontSize = 15.sp)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = onFinish) {
                                    Text("Lewati", color = TextMuted)
                                }
                                Text("Tap untuk lanjut", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                TextButton(onClick = { currentStep++ }) {
                                    Text("Lanjut", color = NeonGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
