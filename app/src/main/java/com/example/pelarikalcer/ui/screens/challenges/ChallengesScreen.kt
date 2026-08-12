package com.example.pelarikalcer.ui.screens.challenges

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pelarikalcer.ui.theme.*

//data class Challenge(
//    val id: Int,
//    val title: String,
//    val description: String,
//    val targetKm: Double,
//    val rewardPoints: Int,
//    val icon: ImageVector,
//    val accentColor: Color,
//    val difficulty: String // "Mudah", "Sedang", "Sulit", "Ekstrem"
//)

//val defaultChallenges = listOf(
//    Challenge(1, "Langkah Pertama", "Lari 1 km untuk pertama kali", 1.0, 50, Icons.Default.DirectionsRun, NeonGreen, "Mudah"),
//    Challenge(2, "Sprint 5K", "Selesaikan lari 5 km dalam satu sesi", 5.0, 150, Icons.Default.Bolt, AccentOrange, "Sedang"),
//    Challenge(3, "Pejuang 10K", "Capai 10 km dalam satu sesi lari", 10.0, 400, Icons.Default.EmojiEvents, GoldStar, "Sulit"),
//    Challenge(4, "Maraton Mini", "Lari 21.1 km (Setengah Maraton) dalam satu sesi", 21.1, 1000, Icons.Default.Whatshot, StreakRed, "Ekstrem"),
//    Challenge(5, "Pelari Konsisten", "Kumpulkan 50 km total sepanjang waktu", 50.0, 500, Icons.Default.Stars, Color(0xFFAB47BC), "Sedang"),
//    Challenge(6, "Legenda Jalanan", "Capai 100 km total sepanjang waktu", 100.0, 2000, Icons.Default.WorkspacePremium, GoldStar, "Ekstrem"),
//    Challenge(7, "Jalan Santai", "Lari 2 km total sepanjang waktu", 2.0, 80, Icons.Default.DirectionsWalk, NeonGreen, "Mudah"),
//    Challenge(8, "Kardio Rutin", "Lari 3 km total sepanjang waktu", 3.0, 100, Icons.Default.FitnessCenter, NeonGreen, "Mudah"),
//    Challenge(9, "Latihan Menengah", "Lari 7 km dalam satu sesi", 7.0, 200, Icons.Default.Speed, AccentOrange, "Sedang"),
//    Challenge(10, "Penjelajah Kota", "Lari 15 km dalam satu sesi", 15.0, 600, Icons.Default.Map, StreakRed, "Sulit"),
//    Challenge(11, "Penyala Streak", "Lari 8 km total sepanjang waktu", 8.0, 180, Icons.Default.LocalFireDepartment, AccentOrange, "Sedang"),
//    Challenge(12, "Pengumpul Energi", "Lari 30 km total sepanjang waktu", 30.0, 350, Icons.Default.BatteryChargingFull, AccentOrange, "Sedang"),
//    Challenge(13, "Ultra Runner", "Lari 42.2 km (Maraton Penuh) dalam satu sesi", 42.2, 3000, Icons.Default.MilitaryTech, GoldStar, "Ekstrem"),
//    Challenge(14, "Puncak Performa", "Lari 75 km total sepanjang waktu", 75.0, 800, Icons.Default.Terrain, StreakRed, "Sulit")
//)

@Composable
fun ChallengesScreen(
    totalDistanceKm: Double,
    completedChallengeIds: List<Int> = emptyList()
) {
    var selectedDifficulty by remember { mutableStateOf("Semua") }
    val difficulties = listOf("Semua", "Mudah", "Sedang", "Sulit", "Ekstrem")

    val filteredChallenges = remember(selectedDifficulty) {
        if (selectedDifficulty == "Semua") defaultChallenges
        else defaultChallenges.filter { it.difficulty == selectedDifficulty }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(CardSurface, DeepNavy)))
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column {
                Text(
                    "Tantangan",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    "Selesaikan tantangan & kumpulkan poin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Horizontal filter chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(difficulties) { diff ->
                ChallengeFilterChip(
                    text = diff,
                    selected = selectedDifficulty == diff,
                    onClick = { selectedDifficulty = diff }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (filteredChallenges.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tidak ada tantangan untuk kategori ini.", color = TextMuted)
                    }
                }
            } else {
                items(filteredChallenges) { challenge ->
                    val progress = (totalDistanceKm / challenge.targetKm).coerceIn(0.0, 1.0)
                    val isCompleted = challenge.id in completedChallengeIds ||
                            (challenge.targetKm <= totalDistanceKm)
                    ChallengeCard(
                        challenge = challenge,
                        progress = progress.toFloat(),
                        isCompleted = isCompleted
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(100.dp)) } // Bottom nav padding
        }
    }
}

@Composable
fun ChallengeFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) NeonGreen.copy(alpha = 0.15f) else CardSurface
    val borderColor = if (selected) NeonGreen else TextMuted.copy(alpha = 0.2f)
    val textColor = if (selected) NeonGreen else TextSecondary

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ChallengeCard(
    challenge: Challenge,
    progress: Float,
    isCompleted: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "progress"
    )

    val difficultyColor = when (challenge.difficulty) {
        "Mudah" -> NeonGreen
        "Sedang" -> AccentOrange
        "Sulit" -> StreakRed
        else -> Color(0xFFAB47BC)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) NeonGreen.copy(alpha = 0.08f) else CardSurface
        ),
        border = if (isCompleted) BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f))
        else BorderStroke(1.dp, challenge.accentColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            if (isCompleted) NeonGreen.copy(alpha = 0.2f)
                            else challenge.accentColor.copy(alpha = 0.15f),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isCompleted) Icons.Default.CheckCircle else challenge.icon,
                        null,
                        tint = if (isCompleted) NeonGreen else challenge.accentColor,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            challenge.title,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) NeonGreen else TextPrimary,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(difficultyColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(challenge.difficulty, color = difficultyColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(challenge.description, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }

                // Reward
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stars, null, tint = GoldStar, modifier = Modifier.size(14.dp))
                        Text("+${challenge.rewardPoints}", fontWeight = FontWeight.Bold, color = GoldStar, fontSize = 13.sp)
                    }
                    Text("poin", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isCompleted) "Selesai!" else String.format("%.1f / %.0f km", minOf(progress * challenge.targetKm, challenge.targetKm), challenge.targetKm),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCompleted) NeonGreen else TextSecondary,
                        fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = if (isCompleted) "100%" else "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCompleted) NeonGreen else TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = if (isCompleted) NeonGreen else challenge.accentColor,
                    trackColor = TextMuted.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}
