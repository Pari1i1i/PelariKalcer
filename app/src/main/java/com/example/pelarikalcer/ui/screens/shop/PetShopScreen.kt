package com.example.pelarikalcer.ui.screens.shop

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.airbnb.lottie.compose.*
import com.example.pelarikalcer.ui.theme.*
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.VerticalAlignmentLine

data class PetItem(
    val id: Int,
    val name: String,
    val description: String,
    val costPoints: Int,
    val lottieUrl: String,
    val color: Color,
    val rarity: String // "Umum", "Langka", "Epik", "Legendaris"
)

val availablePets = listOf(
    // Kelinci = rabbit/bunny
    PetItem(1, "Kelinci", "Kelinci lincah penambah semangat larimu!", 100,
        "https://assets6.lottiefiles.com/packages/lf20_ysas7q2i.json", Color(0xFFFF9999), "Umum"),
    // Kucing = cat
    PetItem(2, "Kucing", "Misterius, gesit, dan setia menemani.", 250,
        "https://assets10.lottiefiles.com/packages/lf20_n28lkm3z.json", Color(0xFF9999FF), "Langka"),
    // Rubah = fox
    PetItem(3, "Rubah", "Membakar semangat lari dengan kobaran api!", 500,
        "https://assets2.lottiefiles.com/packages/lf20_jbb3ix4l.json", StreakOrange, "Epik"),
    // Elang = eagle/bird
    PetItem(4, "Elang", "Terbang tinggi di langit mendampingi performamu.", 750,
        "https://assets3.lottiefiles.com/packages/lf20_UJNc2t.json", GoldStar, "Epik"),
    // Naga = dragon
    PetItem(5, "Naga", "Legenda pelari sejati pemecah rekor!", 2000,
        "https://assets9.lottiefiles.com/packages/lf20_qn2qEP.json", NeonGreen, "Legendaris"),
    // Unicorn = unicorn/horse
    PetItem(6, "Unicorn", "Unik, magis, dan bersinar sepanjang jalan.", 1500,
        "https://assets4.lottiefiles.com/packages/lf20_rovzcmyh.json", Color(0xFFFF69B4), "Legendaris"),
)
@Composable
fun LottiePet(url: String, modifier: Modifier = Modifier) {
    val petId = when {
        url.contains("ysas7q2i") || url.contains("kelinci") || url.contains("w51pcehl") -> 1
        url.contains("n28lkm3z") || url.contains("kucing") || url.contains("myejrn5g") -> 2
        url.contains("jbb3ix4l") || url.contains("rubah") || url.contains("vyx34qyq") -> 3
        url.contains("UJNc2t") || url.contains("elang") || url.contains("tpa5y3zp") -> 4
        url.contains("qn2qEP") || url.contains("naga") || url.contains("9w7k8gux") -> 5
        url.contains("rovzcmyh") || url.contains("unicorn") || url.contains("v7g8l5ze") -> 6
        else -> 1
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,

    ) {
        AnimatedPetAvatar(petId = petId, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun AnimatedPetAvatar(petId: Int, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pet_anim")

    // Shared animations
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val blinkScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                1f at 0
                1f at 2800
                0.1f at 2900
                1f at 3000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )

    val rotAngle by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotate"
    )

    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle"
    )

    Canvas(modifier = modifier ) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = size.minDimension / 2.3f

        when (petId) {
            1 -> { // Kelinci Lari (Rabbit)
                // Hopping / bouncing animation + shift down: telinga panjang ke atas bikin body keliatan naik
                translate(top = bounceY.dp.toPx()) {                    // Outer Ears
                    drawOval(
                        color = Color(0xFFFFCCCC),
                        topLeft = Offset(cx - radius * 0.45f, cy - radius * 1.25f),
                        size = Size(radius * 0.35f, radius * 0.8f)
                    )
                    drawOval(
                        color = Color(0xFFFFCCCC),
                        topLeft = Offset(cx + radius * 0.1f, cy - radius * 1.25f),
                        size = Size(radius * 0.35f, radius * 0.8f)
                    )

                    // Inner Ears
                    drawOval(
                        color = Color(0xFFFF9999),
                        topLeft = Offset(cx - radius * 0.37f, cy - radius * 1.15f),
                        size = Size(radius * 0.2f, radius * 0.6f)
                    )
                    drawOval(
                        color = Color(0xFFFF9999),
                        topLeft = Offset(cx + radius * 0.17f, cy - radius * 1.15f),
                        size = Size(radius * 0.2f, radius * 0.6f)
                    )

                    // Head
                    drawCircle(
                        color = Color.White,
                        radius = radius * 0.7f,
                        center = Offset(cx, cy)
                    )

                    // Cheeks
                    drawCircle(
                        color = Color(0xFFFFB3B3),
                        radius = radius * 0.15f,
                        center = Offset(cx - radius * 0.4f, cy + radius * 0.1f)
                    )
                    drawCircle(
                        color = Color(0xFFFFB3B3),
                        radius = radius * 0.15f,
                        center = Offset(cx + radius * 0.4f, cy + radius * 0.1f)
                    )

                    // Eyes
                    drawCircle(color = Color.Black, radius = radius * 0.08f, center = Offset(cx - radius * 0.22f, cy - radius * 0.1f))
                    drawCircle(color = Color.Black, radius = radius * 0.08f, center = Offset(cx + radius * 0.22f, cy - radius * 0.1f))

                    // Nose / Mouth
                    val nosePath = Path().apply {
                        moveTo(cx, cy)
                        lineTo(cx - radius * 0.08f, cy + radius * 0.08f)
                        lineTo(cx + radius * 0.08f, cy + radius * 0.08f)
                        close()
                    }
                    drawPath(path = nosePath, color = Color(0xFFFF6666))
                }
            }

            2 -> { // Kucing Ninja (Cat) — shift down dikit, telinga+ninja band bikin condong ke atas
                translate(top = 0f) {                    // Ears
                    val earL = Path().apply {
                        moveTo(cx - radius * 0.6f, cy - radius * 0.3f)
                        lineTo(cx - radius * 0.65f, cy - radius * 0.9f)
                        lineTo(cx - radius * 0.1f, cy - radius * 0.6f)
                        close()
                    }
                    val earR = Path().apply {
                        moveTo(cx + radius * 0.6f, cy - radius * 0.3f)
                        lineTo(cx + radius * 0.65f, cy - radius * 0.9f)
                        lineTo(cx + radius * 0.1f, cy - radius * 0.6f)
                        close()
                    }
                    drawPath(earL, Color(0xFF666699))
                    drawPath(earR, Color(0xFF666699))

                    // Inner ears
                    val innerEarL = Path().apply {
                        moveTo(cx - radius * 0.52f, cy - radius * 0.35f)
                        lineTo(cx - radius * 0.57f, cy - radius * 0.8f)
                        lineTo(cx - radius * 0.18f, cy - radius * 0.55f)
                        close()
                    }
                    val innerEarR = Path().apply {
                        moveTo(cx + radius * 0.52f, cy - radius * 0.35f)
                        lineTo(cx + radius * 0.57f, cy - radius * 0.8f)
                        lineTo(cx + radius * 0.18f, cy - radius * 0.55f)
                        close()
                    }
                    drawPath(innerEarL, Color(0xFFFF99C2))
                    drawPath(innerEarR, Color(0xFFFF99C2))

                    // Head
                    drawCircle(color = Color(0xFF8080C0), radius = radius * 0.7f, center = Offset(cx, cy))

                    // Ninja Band
                    drawRect(
                        color = Color(0xFF1A1A3A),
                        topLeft = Offset(cx - radius * 0.67f, cy - radius * 0.25f),
                        size = Size(radius * 1.34f, radius * 0.35f)
                    )
                    // Metal plate
                    drawRoundRect(
                        color = Color(0xFFCCCCCC),
                        topLeft = Offset(cx - radius * 0.2f, cy - radius * 0.2f),
                        size = Size(radius * 0.4f, radius * 0.25f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                    )

                    // Blinking Eyes
                    scale(scaleX = 1f, scaleY = blinkScale, pivot = Offset(cx, cy)) {
                        drawCircle(color = Color(0xFFFFD700), radius = radius * 0.08f, center = Offset(cx - radius * 0.35f, cy - radius * 0.08f))
                        drawCircle(color = Color(0xFFFFD700), radius = radius * 0.08f, center = Offset(cx + radius * 0.35f, cy - radius * 0.08f))
                    }

                    // Whiskers
                    drawLine(Color.White, Offset(cx - radius * 0.5f, cy + radius * 0.1f), Offset(cx - radius * 0.8f, cy + radius * 0.05f), strokeWidth = 3f)
                    drawLine(Color.White, Offset(cx - radius * 0.5f, cy + radius * 0.2f), Offset(cx - radius * 0.8f, cy + radius * 0.23f), strokeWidth = 3f)
                    drawLine(Color.White, Offset(cx + radius * 0.5f, cy + radius * 0.1f), Offset(cx + radius * 0.8f, cy + radius * 0.05f), strokeWidth = 3f)
                    drawLine(Color.White, Offset(cx + radius * 0.5f, cy + radius * 0.2f), Offset(cx + radius * 0.8f, cy + radius * 0.23f), strokeWidth = 3f)
                }
            }

            3 -> { // Rubah Api (Fox) — shift down, telinga + partikel api bikin condong ke atas
                translate(top = radius * 0.13f) {
                    // Fire Particles rising
                    val pX = floatArrayOf(-12f, 15f, -2f)
                    val pY = floatArrayOf(0f, 10f, 25f)
                    val pR = floatArrayOf(8f, 12f, 6f)
                    val pColors = listOf(Color(0xFFFFA500), Color(0xFFFF4500), Color(0xFFFFD700))
                    pColors.indices.forEach { i ->
                        val progress = (particleOffset + pY[i]) % 60f
                        val alpha = (1f - (progress / -60f)).coerceIn(0f, 1f)
                        drawCircle(
                            color = pColors[i].copy(alpha = alpha),
                            radius = pR[i].dp.toPx(),
                            center = Offset(cx + pX[i].dp.toPx(), cy - radius * 0.5f + (progress).dp.toPx())
                        )
                    }

                    // Fox Ears
                    val earL = Path().apply {
                        moveTo(cx - radius * 0.6f, cy - radius * 0.2f)
                        lineTo(cx - radius * 0.65f, cy - radius * 0.95f)
                        lineTo(cx - radius * 0.15f, cy - radius * 0.5f)
                        close()
                    }
                    val earR = Path().apply {
                        moveTo(cx + radius * 0.6f, cy - radius * 0.2f)
                        lineTo(cx + radius * 0.65f, cy - radius * 0.95f)
                        lineTo(cx + radius * 0.15f, cy - radius * 0.5f)
                        close()
                    }
                    drawPath(earL, Color(0xFFE65C00))
                    drawPath(earR, Color(0xFFE65C00))

                    // Fox Face Path (Pointy chin)
                    val facePath = Path().apply {
                        moveTo(cx - radius * 0.7f, cy - radius * 0.3f)
                        lineTo(cx + radius * 0.7f, cy - radius * 0.3f)
                        quadraticTo(cx + radius * 0.6f, cy + radius * 0.3f, cx, cy + radius * 0.7f)
                        quadraticTo(cx - radius * 0.6f, cy + radius * 0.3f, cx - radius * 0.7f, cy - radius * 0.3f)
                        close()
                    }
                    drawPath(facePath, Color(0xFFFF701A))

                    // Cheeks (White parts)
                    val cheekL = Path().apply {
                        moveTo(cx - radius * 0.6f, cy)
                        quadraticTo(cx - radius * 0.4f, cy + radius * 0.3f, cx, cy + radius * 0.7f)
                        quadraticTo(cx - radius * 0.5f, cy + radius * 0.2f, cx - radius * 0.6f, cy)
                        close()
                    }
                    val cheekR = Path().apply {
                        moveTo(cx + radius * 0.6f, cy)
                        quadraticTo(cx + radius * 0.4f, cy + radius * 0.3f, cx, cy + radius * 0.7f)
                        quadraticTo(cx + radius * 0.5f, cy + radius * 0.2f, cx + radius * 0.6f, cy)
                        close()
                    }
                    drawPath(cheekL, Color.White)
                    drawPath(cheekR, Color.White)

                    // Snout/Nose
                    drawCircle(color = Color.Black, radius = radius * 0.08f, center = Offset(cx, cy + radius * 0.62f))

                    // Eyes
                    drawCircle(color = Color.Black, radius = radius * 0.06f, center = Offset(cx - radius * 0.25f, cy + radius * 0.1f))
                    drawCircle(color = Color.Black, radius = radius * 0.06f, center = Offset(cx + radius * 0.25f, cy + radius * 0.1f))
                }
            }

            4 -> { // Elang Emas (Eagle) — shift down dikit, sayap yang flap bikin sedikit condong ke atas
                translate(top = radius * 0.05f) {
                    // Flapping Wings
                    rotate(degrees = rotAngle * 2.5f, pivot = Offset(cx - radius * 0.8f, cy)) {
                        drawOval(
                            color = Color(0xFFCC9900),
                            topLeft = Offset(cx - radius * 1.3f, cy - radius * 0.3f),
                            size = Size(radius * 0.6f, radius * 0.4f)
                        )
                    }
                    rotate(degrees = -rotAngle * 2.5f, pivot = Offset(cx + radius * 0.8f, cy)) {
                        drawOval(
                            color = Color(0xFFCC9900),
                            topLeft = Offset(cx + radius * 0.7f, cy - radius * 0.3f),
                            size = Size(radius * 0.6f, radius * 0.4f)
                        )
                    }

                    // Head
                    drawCircle(color = Color(0xFFFFD700), radius = radius * 0.65f, center = Offset(cx, cy))

                    // Eyes
                    drawCircle(color = Color.White, radius = radius * 0.14f, center = Offset(cx - radius * 0.22f, cy - radius * 0.1f))
                    drawCircle(color = Color.White, radius = radius * 0.14f, center = Offset(cx + radius * 0.22f, cy - radius * 0.1f))
                    drawCircle(color = Color.Black, radius = radius * 0.07f, center = Offset(cx - radius * 0.2f, cy - radius * 0.08f))
                    drawCircle(color = Color.Black, radius = radius * 0.07f, center = Offset(cx + radius * 0.2f, cy - radius * 0.08f))

                    // Sharp Beak
                    val beak = Path().apply {
                        moveTo(cx - radius * 0.15f, cy + radius * 0.08f)
                        lineTo(cx + radius * 0.15f, cy + radius * 0.08f)
                        lineTo(cx, cy + radius * 0.55f)
                        close()
                    }
                    drawPath(beak, Color(0xFFFF6600))
                }
            }

            5 -> { // Naga Kilat (Dragon) — shift down, tanduk tinggi bikin condong ke atas
                translate(top = radius * 0.20f) {
                    scale(scalePulse, scalePulse, Offset(cx, cy)) {
                        // Horns
                        val hornL = Path().apply {
                            moveTo(cx - radius * 0.3f, cy - radius * 0.5f)
                            quadraticTo(cx - radius * 0.5f, cy - radius * 1.1f, cx - radius * 0.7f, cy - radius * 0.9f)
                            quadraticTo(cx - radius * 0.4f, cy - radius * 0.8f, cx - radius * 0.2f, cy - radius * 0.4f)
                            close()
                        }
                        val hornR = Path().apply {
                            moveTo(cx + radius * 0.3f, cy - radius * 0.5f)
                            quadraticTo(cx + radius * 0.5f, cy - radius * 1.1f, cx + radius * 0.7f, cy - radius * 0.9f)
                            quadraticTo(cx + radius * 0.4f, cy - radius * 0.8f, cx + radius * 0.2f, cy - radius * 0.4f)
                            close()
                        }
                        drawPath(hornL, Color(0xFFFFD700))
                        drawPath(hornR, Color(0xFFFFD700))

                        // Main Dragon Head
                        drawCircle(color = Color(0xFF00B386), radius = radius * 0.7f, center = Offset(cx, cy))

                        // Snout
                        drawRoundRect(
                            color = Color(0xFF00FFCC),
                            topLeft = Offset(cx - radius * 0.45f, cy + radius * 0.1f),
                            size = Size(radius * 0.9f, radius * 0.45f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx(), 10.dp.toPx())
                        )
                        // Nostrils
                        drawCircle(color = Color(0xFF00664F), radius = radius * 0.05f, center = Offset(cx - radius * 0.15f, cy + radius * 0.3f))
                        drawCircle(color = Color(0xFF00664F), radius = radius * 0.05f, center = Offset(cx + radius * 0.15f, cy + radius * 0.3f))

                        // Glowing eyes
                        drawCircle(color = Color.Black, radius = radius * 0.12f, center = Offset(cx - radius * 0.25f, cy - radius * 0.15f))
                        drawCircle(color = Color.Black, radius = radius * 0.12f, center = Offset(cx + radius * 0.25f, cy - radius * 0.15f))
                        drawCircle(color = Color(0xFF00FFCC), radius = radius * 0.05f, center = Offset(cx - radius * 0.22f, cy - radius * 0.15f))
                        drawCircle(color = Color(0xFF00FFCC), radius = radius * 0.05f, center = Offset(cx + radius * 0.22f, cy - radius * 0.15f))
                    }
                }
            }

            6 -> { // Unicorn Pelangi (Unicorn) — shift down, tanduk paling tinggi + mane sebelah kanan bikin condong
                translate(top = radius * 0.23f) {
                    // Rainbow Aura
                    val colors = listOf(
                        Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Magenta, Color.Red
                    )
                    drawCircle(
                        brush = Brush.sweepGradient(colors, Offset(cx, cy)),
                        radius = radius * 0.85f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 4.dp.toPx())
                    )

                    // Golden Horn
                    val horn = Path().apply {
                        moveTo(cx - radius * 0.12f, cy - radius * 0.5f)
                        lineTo(cx + radius * 0.12f, cy - radius * 0.5f)
                        lineTo(cx, cy - radius * 1.3f)
                        close()
                    }
                    drawPath(horn, Color(0xFFFFA500))

                    // Unicorn Head (White)
                    drawCircle(color = Color.White, radius = radius * 0.65f, center = Offset(cx, cy))

                    // Pink cheeks
                    drawCircle(color = Color(0xFFFFB3D9), radius = radius * 0.12f, center = Offset(cx - radius * 0.3f, cy + radius * 0.2f))
                    drawCircle(color = Color(0xFFFFB3D9), radius = radius * 0.12f, center = Offset(cx + radius * 0.3f, cy + radius * 0.2f))

                    // Mane (Pink/Purple) — sekarang simetris kiri-kanan biar bounding shape balance
                    drawCircle(color = Color(0xFFFF80DF), radius = radius * 0.18f, center = Offset(cx + radius * 0.45f, cy - radius * 0.3f))
                    drawCircle(color = Color(0xFFD980FF), radius = radius * 0.18f, center = Offset(cx + radius * 0.45f, cy))
                    drawCircle(color = Color(0xFFFF80DF).copy(alpha = 0.7f), radius = radius * 0.15f, center = Offset(cx - radius * 0.45f, cy - radius * 0.25f))
                    drawCircle(color = Color(0xFFD980FF).copy(alpha = 0.7f), radius = radius * 0.15f, center = Offset(cx - radius * 0.45f, cy))

                    // Sparkly eyes
                    drawCircle(color = Color(0xFF4A148C), radius = radius * 0.08f, center = Offset(cx - radius * 0.22f, cy - radius * 0.08f))
                    drawCircle(color = Color(0xFF4A148C), radius = radius * 0.08f, center = Offset(cx + radius * 0.22f, cy - radius * 0.08f))
                    drawCircle(color = Color.White, radius = radius * 0.03f, center = Offset(cx - radius * 0.25f, cy - radius * 0.1f))
                    drawCircle(color = Color.White, radius = radius * 0.03f, center = Offset(cx + radius * 0.25f, cy - radius * 0.1f))
                }
            }
        }
    }
}


@Composable
fun PetShopScreen(
    userPoints: Int,
    ownedPetIds: List<Int>,
    equippedPetId: Int?,
    onBuyPet: (PetItem) -> Unit,
    onEquipPet: (Int) -> Unit
) {
    var selectedPet by remember { mutableStateOf<PetItem?>(null) }

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
                Text("Toko Pet", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Stars, null, tint = GoldStar, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$userPoints poin tersedia", style = MaterialTheme.typography.bodyMedium, color = GoldStar, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(availablePets) { pet ->
                val isOwned = pet.id in ownedPetIds
                val isEquipped = pet.id == equippedPetId

                PetCard(
                    pet = pet,
                    isOwned = isOwned,
                    isEquipped = isEquipped,
                    canAfford = userPoints >= pet.costPoints,
                    onClick = { selectedPet = pet }
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Detail Dialog
    selectedPet?.let { pet ->
        val isOwned = pet.id in ownedPetIds
        val isEquipped = pet.id == equippedPetId
        val canAfford = userPoints >= pet.costPoints

        AlertDialog(
            onDismissRequest = { selectedPet = null },
            containerColor = DarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(56.dp).background(pet.color.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        LottiePet(url = pet.lottieUrl, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(pet.name, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Text(pet.rarity, color = pet.color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            },
            text = {
                Column {
                    Text(pet.description, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (!isOwned) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stars, null, tint = GoldStar, modifier = Modifier.size(16.dp))
                            Text(" ${pet.costPoints} poin", color = GoldStar, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                when {
                    isEquipped -> TextButton(onClick = { selectedPet = null }) { Text("Sudah Dipakai", color = NeonGreen) }
                    isOwned -> Button(
                        onClick = { onEquipPet(pet.id); selectedPet = null },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) { Text("Pakai Sekarang", color = DeepNavy, fontWeight = FontWeight.Bold) }
                    canAfford -> Button(
                        onClick = { onBuyPet(pet); selectedPet = null },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldStar)
                    ) { Text("Beli Pet!", color = DeepNavy, fontWeight = FontWeight.Bold) }
                    else -> TextButton(onClick = { selectedPet = null }) { Text("Poin Tidak Cukup", color = DangerRed) }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPet = null }) { Text("Tutup", color = TextSecondary) }
            }
        )
    }
}

@Composable
fun PetCard(
    pet: PetItem,
    isOwned: Boolean,
    isEquipped: Boolean,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isEquipped -> NeonGreen
        isOwned -> pet.color.copy(alpha = 0.6f)
        canAfford -> pet.color.copy(alpha = 0.3f)
        else -> TextMuted.copy(alpha = 0.2f)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEquipped) NeonGreen.copy(alpha = 0.1f) else CardSurface
        ),
        border = BorderStroke(if (isEquipped) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Rarity badge
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .background(pet.color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(pet.rarity, color = pet.color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Animated Lottie Pet!
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(pet.color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                LottiePet(url = pet.lottieUrl, modifier = Modifier.size(72.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(pet.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(8.dp))

            when {
                isEquipped -> {
                    Box(
                        modifier = Modifier.background(NeonGreen.copy(alpha = 0.2f), RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Dipakai", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                isOwned -> {
                    Box(
                        modifier = Modifier.background(TextSecondary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Dimiliki", color = TextSecondary, fontSize = 11.sp)
                    }
                }
                else -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stars, null, tint = if (canAfford) GoldStar else TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            "${pet.costPoints}",
                            fontWeight = FontWeight.Bold,
                            color = if (canAfford) GoldStar else TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}