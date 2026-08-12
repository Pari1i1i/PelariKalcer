package com.example.pelarikalcer.ui.screens.challenges

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.pelarikalcer.ui.theme.*

// Dipindah keluar dari ChallengesScreen.kt supaya MainViewModel bisa pakai
// catalog yang SAMA PERSIS buat ngecek target & reward poin — biar gak ada
// dua sumber data (UI vs logic poin) yang bisa kebablasan gak sinkron.
data class Challenge(
    val id: Int,
    val title: String,
    val description: String,
    val targetKm: Double,
    val rewardPoints: Int,
    val icon: ImageVector,
    val accentColor: Color,
    val difficulty: String // "Mudah", "Sedang", "Sulit", "Ekstrem"
)

val defaultChallenges = listOf(
    Challenge(1, "Langkah Pertama", "Lari 1 km untuk pertama kali", 1.0, 50, Icons.Default.DirectionsRun, NeonGreen, "Mudah"),
    Challenge(2, "Sprint 5K", "Selesaikan lari 5 km dalam satu sesi", 5.0, 150, Icons.Default.Bolt, AccentOrange, "Sedang"),
    Challenge(3, "Pejuang 10K", "Capai 10 km dalam satu sesi lari", 10.0, 400, Icons.Default.EmojiEvents, GoldStar, "Sulit"),
    Challenge(4, "Maraton Mini", "Lari 21.1 km (Setengah Maraton) dalam satu sesi", 21.1, 1000, Icons.Default.Whatshot, StreakRed, "Ekstrem"),
    Challenge(5, "Pelari Konsisten", "Kumpulkan 50 km total sepanjang waktu", 50.0, 500, Icons.Default.Stars, Color(0xFFAB47BC), "Sedang"),
    Challenge(6, "Legenda Jalanan", "Capai 100 km total sepanjang waktu", 100.0, 2000, Icons.Default.WorkspacePremium, GoldStar, "Ekstrem"),
    Challenge(7, "Jalan Santai", "Lari 2 km total sepanjang waktu", 2.0, 80, Icons.Default.DirectionsWalk, NeonGreen, "Mudah"),
    Challenge(8, "Kardio Rutin", "Lari 3 km total sepanjang waktu", 3.0, 100, Icons.Default.FitnessCenter, NeonGreen, "Mudah"),
    Challenge(9, "Latihan Menengah", "Lari 7 km dalam satu sesi", 7.0, 200, Icons.Default.Speed, AccentOrange, "Sedang"),
    Challenge(10, "Penjelajah Kota", "Lari 15 km dalam satu sesi", 15.0, 600, Icons.Default.Map, StreakRed, "Sulit"),
    Challenge(11, "Penyala Streak", "Lari 8 km total sepanjang waktu", 8.0, 180, Icons.Default.LocalFireDepartment, AccentOrange, "Sedang"),
    Challenge(12, "Pengumpul Energi", "Lari 30 km total sepanjang waktu", 30.0, 350, Icons.Default.BatteryChargingFull, AccentOrange, "Sedang"),
    Challenge(13, "Ultra Runner", "Lari 42.2 km (Maraton Penuh) dalam satu sesi", 42.2, 3000, Icons.Default.MilitaryTech, GoldStar, "Ekstrem"),
    Challenge(14, "Puncak Performa", "Lari 75 km total sepanjang waktu", 75.0, 800, Icons.Default.Terrain, StreakRed, "Sulit")
)