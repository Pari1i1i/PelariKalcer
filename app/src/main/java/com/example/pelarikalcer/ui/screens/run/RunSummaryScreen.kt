package com.example.pelarikalcer.ui.screens.run

import android.graphics.Bitmap
import android.util.Base64
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pelarikalcer.ui.theme.*
import com.example.pelarikalcer.utils.RunImageExporter
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream

@Composable
fun RunSummaryScreen(
    distanceKm: Double,
    durationSeconds: Int,
    paceMinPerKm: Double,
    caloriesBurned: Int,
    userName: String = "PelariKalcer Runner",
    elevationGainM: Double = 0.0,
    elevationLossM: Double = 0.0,
    maxAltitudeM: Double = 0.0,
    onDone: () -> Unit,
    onPostToFeed: (Double, Double, Int, String) -> Unit = { _, _, _, _ -> }
) {
    val context = LocalContext.current
    var showCelebration by remember { mutableStateOf(false) }

    // Render export summary bitmap lazily
    val summaryBitmap = remember(distanceKm, durationSeconds, paceMinPerKm, caloriesBurned, userName) {
        RunImageExporter.createRunSummaryBitmap(
            distanceKm = distanceKm,
            durationSeconds = durationSeconds,
            paceMinPerKm = paceMinPerKm,
            caloriesBurned = caloriesBurned,
            userName = userName
        )
    }

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
                            distanceKm >= 10 -> "Kamu luar biasa! 10K tuntas!"
                            distanceKm >= 5 -> "5K selesai! Pencapaian hebat!"
                            distanceKm >= 1 -> "Streak bertambah! Terus konsisten!"
                            else -> "Awal yang bagus, teruslah bergerak!"
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
                    border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
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
                        value = if (distanceKm >= 1.0) "Streak +1 Aktif" else "< 1 km"
                    )
                }
            }

            // Export & Share Image Card Preview Section
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SHARE SEBERAPA KALCER LO!", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp, letterSpacing = 1.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Card Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black)
                    ) {
                        Image(
                            bitmap = summaryBitmap.asImageBitmap(),
                            contentDescription = "Preview Kartu Lari",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons: Download, Share, Post to Feed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                RunImageExporter.saveBitmapToGallery(context, summaryBitmap)
                            },
                            modifier = Modifier.weight(1f).height(70.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy, contentColor = NeonGreen),
                            border = BorderStroke(1.dp, NeonGreen),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Download, contentDescription = "Simpan", modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Simpan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                RunImageExporter.shareBitmap(context, summaryBitmap)
                            },
                            modifier = Modifier.weight(1f).height(70.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy, contentColor = AccentOrange),
                            border = BorderStroke(1.dp, AccentOrange),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Share, contentDescription = "Bagikan", modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Share", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                val baos = ByteArrayOutputStream()
                                summaryBitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos)
                                val base64 = "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                                onPostToFeed(distanceKm, paceMinPerKm, durationSeconds, base64)
                                Toast.makeText(context, "Membuka postingan Feed...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(70.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = DeepNavy),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.RssFeed, contentDescription = "Feed", modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Post Feed", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

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

private val ElasticOutEasing = Easing { f ->
    val p = 0.3f
    if (f == 0f || f == 1f) f
    else {
        val s = p / 4
        (Math.pow(2.0, -10.0 * f) * Math.sin(((f - s) * (2 * Math.PI) / p).toDouble())).toFloat() + 1
    }
}
