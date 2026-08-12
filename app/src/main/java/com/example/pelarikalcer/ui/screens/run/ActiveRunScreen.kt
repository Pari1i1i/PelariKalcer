package com.example.pelarikalcer.ui.screens.run

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import org.osmdroid.util.GeoPoint

@Composable
fun ActiveRunScreen(
    durationSeconds: Int,
    distanceKm: Double,
    paceMinPerKm: Double,
    calories: Int,
    isRunning: Boolean,
    elevationGainM: Double = 0.0,
    elevationLossM: Double = 0.0,
    currentAltitudeM: Double = 0.0,
    currentLatitude: Double? = null,
    currentLongitude: Double? = null,
    onPauseResume: () -> Unit,
    onFinish: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "run_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 1.06f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = if (isRunning) 0.4f else 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val hours = durationSeconds / 3600
    val minutes = (durationSeconds % 3600) / 60
    val seconds = durationSeconds % 60

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A0F1E), DeepNavy, Color(0xFF0A1628))
                )
            )
    ) {
        // Background live OSM map
        if (currentLatitude != null && currentLongitude != null) {
            val centerPoint = remember(currentLatitude, currentLongitude) {
                GeoPoint(currentLatitude, currentLongitude)
            }
            OsmMap(
                modifier = Modifier.fillMaxSize(),
                centerPoint = centerPoint,
                zoomLevel = 16.5
            )
        }

        // Background dark/navy overlay to ensure high readability of metrics on top of the map
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.4f),
                            DeepNavy.copy(alpha = 0.85f),
                            Color(0xFF0A1628).copy(alpha = 0.95f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Status chip
            Box(
                modifier = Modifier
                    .background(
                        if (isRunning) NeonGreen.copy(alpha = 0.15f)
                        else StreakOrange.copy(alpha = 0.15f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isRunning) NeonGreen else StreakOrange,
                                CircleShape
                            )
                            .scale(pulseScale)
                    )
                    Text(
                        text = if (isRunning) "● SEDANG BERLARI" else "⏸ DIJEDA",
                        color = if (isRunning) NeonGreen else StreakOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Timer display (Strava-style large)
            Text(
                text = if (hours > 0)
                    String.format("%d:%02d:%02d", hours, minutes, seconds)
                else
                    String.format("%02d:%02d", minutes, seconds),
                fontSize = if (hours > 0) 52.sp else 72.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = (-2).sp
            )
            Text(
                text = "WAKTU",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main metric - Distance (Strava-style large center)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.radialGradient(
                            listOf(NeonGreen.copy(alpha = glowAlpha), Color.Transparent)
                        ),
                        CircleShape
                    )
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("%.2f", distanceKm),
                            fontSize = 80.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonGreen,
                            lineHeight = 80.sp
                        )
                        Text(
                            text = " km",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 14.dp)
                        )
                    }
                    Text(
                        text = "JARAK TEMPUH",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        letterSpacing = 3.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Secondary metrics grid (Strava-style 2x2)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface.copy(alpha = 0.8f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, TextMuted.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        RunMetricCell(
                            modifier = Modifier.weight(1f),
                            value = if (paceMinPerKm > 0 && paceMinPerKm < 99)
                                String.format("%d'%02d\"", paceMinPerKm.toInt(), ((paceMinPerKm % 1) * 60).toInt())
                            else "--",
                            label = "PACE",
                            icon = Icons.Default.Speed,
                            iconTint = NeonGreen
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(56.dp)
                                .align(Alignment.CenterVertically)
                                .background(TextMuted.copy(alpha = 0.2f))
                        )
                        RunMetricCell(
                            modifier = Modifier.weight(1f),
                            value = "$calories",
                            unit = "kcal",
                            label = "KALORI",
                            icon = Icons.Default.LocalFireDepartment,
                            iconTint = StreakRed
                        )
                    }
                    HorizontalDivider(
                        color = TextMuted.copy(alpha = 0.15f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        RunMetricCell(
                            modifier = Modifier.weight(1f),
                            value = "+${String.format("%.0f", elevationGainM)}",
                            unit = "m",
                            label = "ELEVASI NAIK",
                            icon = Icons.Default.TrendingUp,
                            iconTint = AccentOrange
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(56.dp)
                                .align(Alignment.CenterVertically)
                                .background(TextMuted.copy(alpha = 0.2f))
                        )
                        RunMetricCell(
                            modifier = Modifier.weight(1f),
                            value = String.format("%.0f", currentAltitudeM),
                            unit = "mdpl",
                            label = "KETINGGIAN",
                            icon = Icons.Default.Landscape,
                            iconTint = Color(0xFF64B5F6)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Control buttons (Strava-style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stop button
                OutlinedButton(
                    onClick = onFinish,
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                    border = androidx.compose.foundation.BorderStroke(2.dp, DangerRed),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Selesai", tint = DangerRed, modifier = Modifier.size(28.dp))
                }

                Spacer(modifier = Modifier.width(48.dp))

                // Pause/Resume button
                Button(
                    onClick = onPauseResume,
                    modifier = Modifier.size(84.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Jeda" else "Lanjutkan",
                        tint = DeepNavy,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RunMetricCell(
    modifier: Modifier = Modifier,
    value: String,
    unit: String = "",
    label: String,
    icon: ImageVector,
    iconTint: Color
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = TextPrimary
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = " $unit",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
        Text(
            text = label,
            fontSize = 9.sp,
            color = TextMuted,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
    }
}

// Keep legacy RunMetric for backward compatibility
@Composable
fun RunMetric(
    value: String,
    unit: String,
    label: String,
    icon: ImageVector,
    valueColor: Color = TextPrimary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Icon(icon, contentDescription = label, tint = TextSecondary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = valueColor)
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = unit, style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
}
