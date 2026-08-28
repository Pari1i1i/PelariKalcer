package com.example.pelarikalcer.ui.screens.dashboard

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
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pelarikalcer.data.local.entity.RunEntity
import com.example.pelarikalcer.data.local.entity.UserEntity
import com.example.pelarikalcer.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    user: UserEntity?,
    recentRuns: List<RunEntity>,
    totalDistanceKm: Double,
    totalCalories: Int,
    onStartRun: () -> Unit,
    onOpenAiCoach: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0A0F1E), DeepNavy))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Hero Header with gradient overlay
            DashboardHeader(user = user, onOpenAiCoach = onOpenAiCoach)

            // Weekly progress ring + streak
            WeeklyStreakSection(
                streak = user?.currentStreak ?: 0,
                totalPoints = user?.totalPoints ?: 0
            )

            // Stats cards (Strava-style horizontal scrollable)
            StatsSection(
                totalKm = totalDistanceKm,
                totalCalories = totalCalories,
                totalPoints = user?.totalPoints ?: 0,
                totalRuns = recentRuns.size
            )

            // Start Run CTA
            StartRunButton(onClick = onStartRun)

            // Recent Runs
            if (recentRuns.isNotEmpty()) {
                RecentRunsSection(runs = recentRuns)
            } else {
                EmptyRunsHint()
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun DashboardHeader(user: UserEntity?, onOpenAiCoach: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "header_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        NeonGreen.copy(alpha = glowAlpha * 0.15f),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val greeting = when {
                    hour < 11 -> "Selamat Pagi"
                    hour < 15 -> "Selamat Siang"
                    hour < 18 -> "Selamat Sore"
                    else -> "Selamat Malam"
                }
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = user?.fullName?.takeIf { it.isNotBlank() } ?: user?.username ?: "Runner",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Coach button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            Brush.linearGradient(listOf(AccentOrange, Color(0xFFFF6B00))),
                            CircleShape
                        )
                        .clickable(onClick = onOpenAiCoach),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Sports, contentDescription = "Coach Lari", tint = Color.White, modifier = Modifier.size(24.dp))
                }

                // Avatar
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            Brush.linearGradient(listOf(NeonGreen, Color(0xFF00C896))),
                            CircleShape
                        )
                        .border(2.dp, NeonGreen.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (user?.username?.firstOrNull()?.uppercaseChar() ?: 'R').toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepNavy
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyStreakSection(streak: Int, totalPoints: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "streak")
    val fireScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (streak > 0) 1.12f else 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "fire"
    )
    val sweepAngle by animateFloatAsState(
        targetValue = (streak.coerceAtMost(30) / 30f) * 360f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "sweep"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = if (streak > 0) BorderStroke(1.dp, StreakOrange.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = StreakOrange, modifier = Modifier.size(16.dp))
                    Text("Streak Lari", style = MaterialTheme.typography.labelLarge, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$streak",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (streak > 0) StreakOrange else TextMuted
                    )
                    Text(
                        text = " hari",
                        fontSize = 18.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Text(
                    text = if (streak == 0) "Mulai lari hari ini!"
                    else if (streak >= 7) "Streak luar biasa!"
                    else "Pertahankan konsistensimu!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            // Circular progress ring
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    // Track
                    drawArc(
                        color = TextMuted.copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Progress
                    if (streak > 0) {
                        drawArc(
                            brush = Brush.linearGradient(listOf(StreakOrange, GoldStar)),
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = if (streak > 0) StreakOrange else TextMuted,
                    modifier = Modifier.size(32.dp).scale(fireScale)
                )
            }
        }
    }
}

@Composable
fun StatsSection(totalKm: Double, totalCalories: Int, totalPoints: Int, totalRuns: Int) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
        Text(
            "Statistikmu",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard2(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Route,
                iconTint = NeonGreen,
                value = String.format("%.1f", totalKm),
                unit = "km",
                label = "Total Jarak",
                gradient = Brush.linearGradient(listOf(NeonGreen.copy(0.12f), Color.Transparent))
            )
            StatCard2(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.LocalFireDepartment,
                iconTint = StreakRed,
                value = "$totalCalories",
                unit = "kcal",
                label = "Kalori",
                gradient = Brush.linearGradient(listOf(StreakRed.copy(0.12f), Color.Transparent))
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard2(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Stars,
                iconTint = GoldStar,
                value = "$totalPoints",
                unit = "pts",
                label = "Total Poin",
                gradient = Brush.linearGradient(listOf(GoldStar.copy(0.12f), Color.Transparent))
            )
            StatCard2(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.DirectionsRun,
                iconTint = AccentOrange,
                value = "$totalRuns",
                unit = "sesi",
                label = "Total Lari",
                gradient = Brush.linearGradient(listOf(AccentOrange.copy(0.12f), Color.Transparent))
            )
        }
    }
}

@Composable
fun StatCard2(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    value: String,
    unit: String,
    label: String,
    gradient: Brush
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = value,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = " $unit",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun StartRunButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "button_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.02f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .scale(scale)
                .background(NeonGreen.copy(alpha = glowAlpha * 0.25f), RoundedCornerShape(34.dp))
        )

        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(60.dp).scale(scale),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(listOf(NeonGreen, Color(0xFF00D4A8), Color(0xFF00C896))),
                        RoundedCornerShape(30.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(26.dp))
                    Text(
                        text = "MULAI LARI",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = DeepNavy,
                        letterSpacing = 3.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RecentRunsSection(runs: List<RunEntity>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Lari Terakhir", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("${runs.size} sesi", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
        runs.forEach { run ->
            RunHistoryCard(run = run)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun EmptyRunsHint() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.DirectionsRun, null, tint = NeonGreen.copy(0.5f), modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Belum ada riwayat lari", fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Text("Tekan MULAI LARI untuk memulai aktivitasmu!", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun RunHistoryCard(run: RunEntity) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id"))
    val date = dateFormat.format(Date(run.createdAt))
    val hasElevation = run.elevationGainM > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(NeonGreen.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = String.format("%.2f km", run.distanceKm),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
                Text(text = date, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                if (hasElevation) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, null, tint = AccentOrange, modifier = Modifier.size(12.dp))
                        Text(
                            " +${String.format("%.0f", run.elevationGainM)}m elev",
                            fontSize = 11.sp,
                            color = AccentOrange
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                val h = run.durationSeconds / 3600
                val m = (run.durationSeconds % 3600) / 60
                val s = run.durationSeconds % 60
                Text(
                    text = if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s),
                    fontWeight = FontWeight.SemiBold,
                    color = NeonGreen,
                    fontSize = 15.sp
                )
                if (run.avgPaceMinutesPerKm > 0) {
                    Text(
                        text = String.format("%.1f min/km", run.avgPaceMinutesPerKm),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    unit: String,
    label: String,
    iconTint: Color = NeonGreen
) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardSurface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                Text(text = value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                Spacer(modifier = Modifier.width(2.dp))
                Text(text = unit, style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.padding(bottom = 2.dp))
            }
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun StreakCard(streak: Int) {
    WeeklyStreakSection(streak = streak, totalPoints = 0)
}

@Composable
fun StatsRow(totalKm: Double, totalCalories: Int, totalPoints: Int) {
    StatsSection(totalKm = totalKm, totalCalories = totalCalories, totalPoints = totalPoints, totalRuns = 0)
}
