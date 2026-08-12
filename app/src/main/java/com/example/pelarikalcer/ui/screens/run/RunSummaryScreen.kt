package com.example.pelarikalcer.ui.screens.run

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pelarikalcer.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun RunSummaryScreen(
    distanceKm: Double,
    durationSeconds: Int,
    paceMinPerKm: Double,
    caloriesBurned: Int,
    elevationGainM: Double = 0.0,
    elevationLossM: Double = 0.0,
    maxAltitudeM: Double = 0.0,
    onDone: () -> Unit
) {
    var showCelebration by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        showCelebration = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "trophy")
    val trophyScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophy_scale"
    )

    val hours = durationSeconds / 3600
    val minutes = (durationSeconds % 3600) / 60
    val seconds = durationSeconds % 60

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0A0F1E), DeepNavy, Color(0xFF0A1628)))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Trophy + title
            AnimatedVisibility(
                visible = showCelebration,
                enter = scaleIn(tween(600, easing = ElasticOutEasing)) + fadeIn()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = GoldStar,
                        modifier = Modifier.size(80.dp).scale(trophyScale)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Lari Selesai!", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = NeonGreen)
                    Text(
                        text = when {
                            distanceKm >= 10 -> "Kamu luar biasa! 10K tuntas! 🏆"
                            distanceKm >= 5 -> "5K selesai! Mantap sekali! 🔥"
                            distanceKm >= 1 -> "Streak bertambah! Terus semangat! ⚡"
                            else -> "Mulai yang kecil, teruslah berlari!"
                        },
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main distance card
            AnimatedVisibility(
                visible = showCelebration,
                enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically { it / 3 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("JARAK TEMPUH", fontSize = 10.sp, color = TextMuted, letterSpacing = 3.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                String.format("%.2f", distanceKm),
                                fontSize = 72.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonGreen,
                                lineHeight = 72.sp
                            )
                            Text(" km", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = NeonGreen.copy(alpha = 0.6f),
                                modifier = Modifier.padding(bottom = 12.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time + Pace row
            AnimatedVisibility(
                visible = showCelebration,
                enter = fadeIn(tween(600, delayMillis = 350)) + slideInVertically { it / 3 }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Timer,
                        iconTint = AccentOrange,
                        label = "WAKTU",
                        value = if (hours > 0)
                            String.format("%d:%02d:%02d", hours, minutes, seconds)
                        else
                            String.format("%02d:%02d", minutes, seconds)
                    )
                    SummaryStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Speed,
                        iconTint = NeonGreen,
                        label = "PACE",
                        value = if (paceMinPerKm > 0 && paceMinPerKm < 99)
                            String.format("%d'%02d\"/km", paceMinPerKm.toInt(), ((paceMinPerKm % 1) * 60).toInt())
                        else "--"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calories + Streak row
            AnimatedVisibility(
                visible = showCelebration,
                enter = fadeIn(tween(600, delayMillis = 450)) + slideInVertically { it / 3 }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.LocalFireDepartment,
                        iconTint = StreakRed,
                        label = "KALORI",
                        value = "$caloriesBurned kcal"
                    )
                    SummaryStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Whatshot,
                        iconTint = StreakOrange,
                        label = "STATUS STREAK",
                        value = if (distanceKm >= 1.0) "Streak +1! 🔥" else "< 1 km"
                    )
                }
            }

            // Elevation section (only show if elevation data available)
            if (elevationGainM > 0 || maxAltitudeM > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                AnimatedVisibility(
                    visible = showCelebration,
                    enter = fadeIn(tween(600, delayMillis = 550)) + slideInVertically { it / 3 }
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentOrange.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Icon(Icons.Default.Landscape, null, tint = AccentOrange, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("DATA ELEVASI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 2.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                ElevationCell(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.TrendingUp,
                                    tint = NeonGreen,
                                    label = "Naik",
                                    value = "+${String.format("%.0f", elevationGainM)} m"
                                )
                                ElevationCell(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.TrendingDown,
                                    tint = DangerRed,
                                    label = "Turun",
                                    value = "-${String.format("%.0f", elevationLossM)} m"
                                )
                                ElevationCell(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Terrain,
                                    tint = Color(0xFF64B5F6),
                                    label = "Tertinggi",
                                    value = "${String.format("%.0f", maxAltitudeM)} mdpl"
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Done button
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("Kembali ke Dashboard", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepNavy)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SummaryStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary, textAlign = TextAlign.Center)
            Text(text = label, fontSize = 9.sp, color = TextMuted, letterSpacing = 1.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ElevationCell(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    tint: Color,
    label: String,
    value: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
        Text(text = label, fontSize = 10.sp, color = TextMuted)
    }
}

@Composable
fun SummaryItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color
) {
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
}

private val ElasticOutEasing = Easing { f ->
    val p = 0.3f
    if (f == 0f || f == 1f) f
    else {
        val s = p / 4
        (Math.pow(2.0, -10.0 * f) * Math.sin(((f - s) * (2 * Math.PI) / p).toDouble())).toFloat() + 1
    }
}
