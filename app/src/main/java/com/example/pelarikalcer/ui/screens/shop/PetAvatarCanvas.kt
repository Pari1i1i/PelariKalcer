package com.example.pelarikalcer.ui.screens.shop

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.example.pelarikalcer.data.local.entity.PetStage
import com.example.pelarikalcer.data.local.entity.Rarity

/**
 * FULL-BODY LIVING CREATURE ANIMATIONS:
 * - Idle breathing (ribcage & chest expansion)
 * - Tail wagging & ear twitches
 * - Wing flapping & foot stepping
 * - Living blink & eye glints
 * - True full body: Head, Torso, Arms/Hands, Feet/Legs, Tail, Aura
 */
@Composable
fun PetAvatarCanvas(
    speciesId: Int,
    stage: PetStage,
    rarity: Rarity,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "living_creature_anim")

    // 1. Natural Breathing & Body Bounce
    val breathScaleY by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    val bodyBounceY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // 2. Tail Wagging & Limbs Sway
    val tailAngle by infiniteTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tailWag"
    )

    val earTwitch by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "earTwitch"
    )

    // 3. Wing Flapping (for winged creatures)
    val wingFlap by infiniteTransition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wingFlap"
    )

    // 4. Egg Jiggle
    val eggJiggle by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eggJiggle"
    )

    // 5. Living Eye Blink
    val blinkScaleY by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3200
                1f at 0
                1f at 2950
                0.08f at 3050
                1f at 3150
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )

    // 6. Cosmic Aura for Legendary / Epic
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val minDim = size.minDimension
            val radius = minDim / 2.3f

            // AURA GLOW
            if (rarity == Rarity.EPIC || rarity == Rarity.LEGENDARY || stage == PetStage.ADULT) {
                scale(auraScale, Offset(cx, cy)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(rarity.color.copy(alpha = 0.35f), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = radius * 1.35f
                        ),
                        radius = radius * 1.3f,
                        center = Offset(cx, cy)
                    )
                }
            }

            // STAGE RENDERING
            if (stage == PetStage.EGG) {
                drawLivingEgg(cx, cy, radius, rarity, eggJiggle, bodyBounceY)
            } else {
                val isAdult = stage == PetStage.ADULT
                val stageScale = if (isAdult) 1.05f else 0.88f

                scale(stageScale, Offset(cx, cy)) {
                    translate(top = bodyBounceY) {
                        scale(scaleX = 1f, scaleY = breathScaleY, pivot = Offset(cx, cy + radius * 0.3f)) {
                            when (speciesId) {
                                1 -> drawFullBodyBunny(cx, cy, radius, isAdult, rarity, tailAngle, earTwitch, blinkScaleY)
                                2 -> drawFullBodyPipit(cx, cy, radius, isAdult, rarity, wingFlap, blinkScaleY)
                                3 -> drawFullBodyNinjaCat(cx, cy, radius, isAdult, rarity, tailAngle, earTwitch, blinkScaleY)
                                4 -> drawFullBodyFroggo(cx, cy, radius, isAdult, rarity, bodyBounceY, blinkScaleY)
                                5 -> drawFullBodyFireFox(cx, cy, radius, isAdult, rarity, tailAngle, blinkScaleY)
                                6 -> drawFullBodyPenguin(cx, cy, radius, isAdult, rarity, wingFlap, blinkScaleY)
                                7 -> drawFullBodyGaruda(cx, cy, radius, isAdult, rarity, wingFlap, blinkScaleY)
                                8 -> drawFullBodyShadowWolf(cx, cy, radius, isAdult, rarity, tailAngle, earTwitch, blinkScaleY)
                                9 -> drawFullBodyCosmicDragon(cx, cy, radius, isAdult, rarity, tailAngle, wingFlap, blinkScaleY)
                                10 -> drawFullBodySunUnicorn(cx, cy, radius, isAdult, rarity, tailAngle, earTwitch, blinkScaleY)
                                else -> drawFullBodyBunny(cx, cy, radius, isAdult, rarity, tailAngle, earTwitch, blinkScaleY)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🥚 Living Egg with Wiggle & Shell Highlights
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLivingEgg(
    cx: Float, cy: Float, radius: Float, rarity: Rarity, jiggle: Float, bounce: Float
) {
    val shellColor = when (rarity) {
        Rarity.COMMON -> Color(0xFFF5EBE0)
        Rarity.UNCOMMON -> Color(0xFFD1FAE5)
        Rarity.RARE -> Color(0xFFE0F2FE)
        Rarity.EPIC -> Color(0xFFF3E8FF)
        Rarity.LEGENDARY -> Color(0xFFFEF3C7)
    }

    rotate(degrees = jiggle, pivot = Offset(cx, cy + radius * 0.3f)) {
        // Ground Shadow
        drawOval(
            Color(0x33000000),
            Offset(cx - radius * 0.55f, cy + radius * 0.65f + bounce * 0.3f),
            Size(radius * 1.1f, radius * 0.28f)
        )

        val eggPath = Path().apply {
            moveTo(cx, cy - radius * 0.92f)
            cubicTo(
                cx + radius * 0.85f, cy - radius * 0.70f,
                cx + radius * 0.85f, cy + radius * 0.78f,
                cx, cy + radius * 0.82f
            )
            cubicTo(
                cx - radius * 0.85f, cy + radius * 0.78f,
                cx - radius * 0.85f, cy - radius * 0.70f,
                cx, cy - radius * 0.92f
            )
            close()
        }

        drawPath(eggPath, shellColor)
        drawCircle(rarity.color, radius * 0.16f, Offset(cx - radius * 0.25f, cy - radius * 0.18f))
        drawCircle(rarity.color, radius * 0.22f, Offset(cx + radius * 0.28f, cy + radius * 0.14f))
        drawCircle(rarity.color, radius * 0.14f, Offset(cx - radius * 0.18f, cy + radius * 0.44f))

        // Bold Outline
        drawPath(eggPath, Color(0xFF1E293B), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

        // Gloss glint
        drawOval(
            Color.White.copy(alpha = 0.6f),
            Offset(cx - radius * 0.45f, cy - radius * 0.68f),
            Size(radius * 0.25f, radius * 0.48f)
        )
    }
}

/**
 * Expressive Shiny Eyes with Realistic Blinking
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLivingEyes(
    cx: Float,
    cy: Float,
    eyeDist: Float,
    eyeRadius: Float,
    blinkScaleY: Float,
    irisColor: Color = Color(0xFF0F172A)
) {
    val leftEye = Offset(cx - eyeDist, cy)
    val rightEye = Offset(cx + eyeDist, cy)

    for (center in listOf(leftEye, rightEye)) {
        scale(scaleX = 1f, scaleY = blinkScaleY, pivot = center) {
            // Main Iris
            drawCircle(irisColor, eyeRadius, center)
            if (blinkScaleY > 0.3f) {
                // Shiny Main Reflection
                drawCircle(
                    Color.White,
                    eyeRadius * 0.42f,
                    Offset(center.x - eyeRadius * 0.25f, center.y - eyeRadius * 0.25f)
                )
                // Sub Glint
                drawCircle(
                    Color.White,
                    eyeRadius * 0.20f,
                    Offset(center.x + eyeRadius * 0.32f, center.y + eyeRadius * 0.30f)
                )
            }
        }
    }
}

/**
 * 🐰 1. FULL BODY BUNNY HOP (Fluffy Ears, Torso, Paws, Feet & Tail)
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFullBodyBunny(
    cx: Float, cy: Float, radius: Float, isAdult: Boolean, rarity: Rarity,
    tailAngle: Float, earTwitch: Float, blinkScaleY: Float
) {
    // 1. Tail (Behind body, wags)
    rotate(degrees = tailAngle, pivot = Offset(cx - radius * 0.45f, cy + radius * 0.45f)) {
        drawCircle(Color(0xFFFFF1F2), radius * 0.16f, Offset(cx - radius * 0.45f, cy + radius * 0.45f))
        drawCircle(Color(0xFF1E293B), radius * 0.16f, Offset(cx - radius * 0.45f, cy + radius * 0.45f), style = Stroke(2.dp.toPx()))
    }

    // 2. Ears (Wiggle/twitch)
    val earHeight = if (isAdult) radius * 0.95f else radius * 0.65f
    rotate(degrees = earTwitch, pivot = Offset(cx - radius * 0.25f, cy - radius * 0.4f)) {
        drawOval(Color(0xFFFFDFE5), Offset(cx - radius * 0.42f, cy - radius * 0.4f - earHeight), Size(radius * 0.28f, earHeight))
        drawOval(rarity.color, Offset(cx - radius * 0.36f, cy - radius * 0.35f - earHeight * 0.85f), Size(radius * 0.16f, earHeight * 0.75f))
        drawOval(Color(0xFF1E293B), Offset(cx - radius * 0.42f, cy - radius * 0.4f - earHeight), Size(radius * 0.28f, earHeight), style = Stroke(2.dp.toPx()))
    }
    rotate(degrees = -earTwitch, pivot = Offset(cx + radius * 0.25f, cy - radius * 0.4f)) {
        drawOval(Color(0xFFFFDFE5), Offset(cx + radius * 0.14f, cy - radius * 0.4f - earHeight), Size(radius * 0.28f, earHeight))
        drawOval(rarity.color, Offset(cx + radius * 0.20f, cy - radius * 0.35f - earHeight * 0.85f), Size(radius * 0.16f, earHeight * 0.75f))
        drawOval(Color(0xFF1E293B), Offset(cx + radius * 0.14f, cy - radius * 0.4f - earHeight), Size(radius * 0.28f, earHeight), style = Stroke(2.dp.toPx()))
    }

    // 3. Torso (Full Body)
    drawOval(Color(0xFFFFF1F2), Offset(cx - radius * 0.45f, cy - radius * 0.05f), Size(radius * 0.90f, radius * 0.75f))
    drawOval(Color(0xFF1E293B), Offset(cx - radius * 0.45f, cy - radius * 0.05f), Size(radius * 0.90f, radius * 0.75f), style = Stroke(2.5.dp.toPx()))

    // Soft Belly
    drawOval(Color(0xFFFFFFFF), Offset(cx - radius * 0.30f, cy + radius * 0.08f), Size(radius * 0.60f, radius * 0.50f))

    // 4. Head
    drawCircle(Color(0xFFFFF1F2), radius * 0.52f, Offset(cx, cy - radius * 0.22f))
    drawCircle(Color(0xFF1E293B), radius * 0.52f, Offset(cx, cy - radius * 0.22f), style = Stroke(2.5.dp.toPx()))

    // Rosy Cheeks
    drawCircle(Color(0xFFFDA4AF), radius * 0.12f, Offset(cx - radius * 0.32f, cy - radius * 0.12f))
    drawCircle(Color(0xFFFDA4AF), radius * 0.12f, Offset(cx + radius * 0.32f, cy - radius * 0.12f))

    // 5. Living Eyes
    drawLivingEyes(cx, cy - radius * 0.26f, radius * 0.18f, radius * 0.085f, blinkScaleY)

    // Nose & Smile
    drawCircle(Color(0xFFFB7185), radius * 0.05f, Offset(cx, cy - radius * 0.16f))

    // 6. Cute Front Paws
    drawOval(Color(0xFFFFFFFF), Offset(cx - radius * 0.28f, cy + radius * 0.15f), Size(radius * 0.18f, radius * 0.24f))
    drawOval(Color(0xFFFFFFFF), Offset(cx + radius * 0.10f, cy + radius * 0.15f), Size(radius * 0.18f, radius * 0.24f))
    drawOval(Color(0xFF1E293B), Offset(cx - radius * 0.28f, cy + radius * 0.15f), Size(radius * 0.18f, radius * 0.24f), style = Stroke(2.dp.toPx()))
    drawOval(Color(0xFF1E293B), Offset(cx + radius * 0.10f, cy + radius * 0.15f), Size(radius * 0.18f, radius * 0.24f), style = Stroke(2.dp.toPx()))

    // 7. Feet / Paws (Bottom)
    drawOval(Color(0xFFFFDFE5), Offset(cx - radius * 0.44f, cy + radius * 0.52f), Size(radius * 0.28f, radius * 0.18f))
    drawOval(Color(0xFFFFDFE5), Offset(cx + radius * 0.16f, cy + radius * 0.52f), Size(radius * 0.28f, radius * 0.18f))
    drawOval(Color(0xFF1E293B), Offset(cx - radius * 0.44f, cy + radius * 0.52f), Size(radius * 0.28f, radius * 0.18f), style = Stroke(2.dp.toPx()))
    drawOval(Color(0xFF1E293B), Offset(cx + radius * 0.16f, cy + radius * 0.52f), Size(radius * 0.28f, radius * 0.18f), style = Stroke(2.dp.toPx()))
}

/**
 * 🐦 2. FULL BODY PIPIT BIRD (Body, Flapping Wings, Beak, Tail Feathers & Feet)
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFullBodyPipit(
    cx: Float, cy: Float, radius: Float, isAdult: Boolean, rarity: Rarity,
    wingFlap: Float, blinkScaleY: Float
) {
    // Tail Feathers
    drawOval(rarity.color, Offset(cx - radius * 0.12f, cy + radius * 0.55f), Size(radius * 0.24f, radius * 0.35f))
    drawOval(Color(0xFF1E293B), Offset(cx - radius * 0.12f, cy + radius * 0.55f), Size(radius * 0.24f, radius * 0.35f), style = Stroke(2.dp.toPx()))

    // Left & Right Wings (Flapping!)
    rotate(degrees = wingFlap, pivot = Offset(cx - radius * 0.45f, cy + radius * 0.1f)) {
        drawOval(rarity.color, Offset(cx - radius * 0.72f, cy - radius * 0.05f), Size(radius * 0.34f, radius * 0.55f))
        drawOval(Color(0xFF1E293B), Offset(cx - radius * 0.72f, cy - radius * 0.05f), Size(radius * 0.34f, radius * 0.55f), style = Stroke(2.dp.toPx()))
    }
    rotate(degrees = -wingFlap, pivot = Offset(cx + radius * 0.45f, cy + radius * 0.1f)) {
        drawOval(rarity.color, Offset(cx + radius * 0.38f, cy - radius * 0.05f), Size(radius * 0.34f, radius * 0.55f))
        drawOval(Color(0xFF1E293B), Offset(cx + radius * 0.38f, cy - radius * 0.05f), Size(radius * 0.34f, radius * 0.55f), style = Stroke(2.dp.toPx()))
    }

    // Body
    drawOval(Color(0xFFBAA378), Offset(cx - radius * 0.46f, cy - radius * 0.30f), Size(radius * 0.92f, radius * 0.90f))
    drawOval(Color(0xFF1E293B), Offset(cx - radius * 0.46f, cy - radius * 0.30f), Size(radius * 0.92f, radius * 0.90f), style = Stroke(2.5.dp.toPx()))

    // Soft Cream Chest
    drawOval(Color(0xFFFFF7ED), Offset(cx - radius * 0.32f, cy - radius * 0.05f), Size(radius * 0.64f, radius * 0.58f))

    // Eyes
    drawLivingEyes(cx, cy - radius * 0.15f, radius * 0.18f, radius * 0.08f, blinkScaleY)

    // Beak
    val beak = Path().apply {
        moveTo(cx - radius * 0.10f, cy - radius * 0.04f)
        lineTo(cx + radius * 0.10f, cy - radius * 0.04f)
        lineTo(cx, cy + radius * 0.14f)
        close()
    }
    drawPath(beak, Color(0xFFF97316))
    drawPath(beak, Color(0xFF1E293B), style = Stroke(2.dp.toPx()))

    // Feet
    drawOval(Color(0xFFF97316), Offset(cx - radius * 0.25f, cy + radius * 0.54f), Size(radius * 0.18f, radius * 0.12f))
    drawOval(Color(0xFFF97316), Offset(cx + radius * 0.07f, cy + radius * 0.54f), Size(radius * 0.18f, radius * 0.12f))
}

/**
 * 🐱 3. FULL BODY NINJA CAT (Ninja Headband, Long Tail, Torso, Paws & Feet)
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFullBodyNinjaCat(
    cx: Float, cy: Float, radius: Float, isAdult: Boolean, rarity: Rarity,
    tailAngle: Float, earTwitch: Float, blinkScaleY: Float
) {
    // 1. Long Cat Tail (Sways left/right)
    rotate(degrees = tailAngle * 1.5f, pivot = Offset(cx - radius * 0.40f, cy + radius * 0.40f)) {
        val tailPath = Path().apply {
            moveTo(cx - radius * 0.35f, cy + radius * 0.35f)
            cubicTo(
                cx - radius * 0.75f, cy + radius * 0.20f,
                cx - radius * 0.85f, cy - radius * 0.20f,
                cx - radius * 0.65f, cy - radius * 0.40f
            )
        }
        drawPath(tailPath, Color(0xFF475569), style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round))
        drawPath(tailPath, Color(0xFF1E293B), style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round), blendMode = androidx.compose.ui.graphics.BlendMode.DstOver)
    }

    // 2. Ears
    val earL = Path().apply {
        moveTo(cx - radius * 0.42f, cy - radius * 0.35f)
        lineTo(cx - radius * 0.55f, cy - radius * 0.85f)
        lineTo(cx - radius * 0.12f, cy - radius * 0.50f)
        close()
    }
    val earR = Path().apply {
        moveTo(cx + radius * 0.42f, cy - radius * 0.35f)
        lineTo(cx + radius * 0.55f, cy - radius * 0.85f)
        lineTo(cx + radius * 0.12f, cy - radius * 0.50f)
        close()
    }
    drawPath(earL, Color(0xFF475569))
    drawPath(earR, Color(0xFF475569))
    drawPath(earL, Color(0xFF1E293B), style = Stroke(2.dp.toPx()))
    drawPath(earR, Color(0xFF1E293B), style = Stroke(2.dp.toPx()))

    // Inner Ears
    drawCircle(Color(0xFFF472B6), radius * 0.09f, Offset(cx - radius * 0.35f, cy - radius * 0.52f))
    drawCircle(Color(0xFFF472B6), radius * 0.09f, Offset(cx + radius * 0.35f, cy - radius * 0.52f))

    // 3. Torso (Body)
    drawOval(Color(0xFF64748B), Offset(cx - radius * 0.42f, cy - radius * 0.05f), Size(radius * 0.84f, radius * 0.70f))
    drawOval(Color(0xFF1E293B), Offset(cx - radius * 0.42f, cy - radius * 0.05f), Size(radius * 0.84f, radius * 0.70f), style = Stroke(2.5.dp.toPx()))

    // White Chest Patch
    drawOval(Color(0xFFF8FAFC), Offset(cx - radius * 0.24f, cy + radius * 0.08f), Size(radius * 0.48f, radius * 0.45f))

    // 4. Head
    drawCircle(Color(0xFF64748B), radius * 0.50f, Offset(cx, cy - radius * 0.22f))
    drawCircle(Color(0xFF1E293B), radius * 0.50f, Offset(cx, cy - radius * 0.22f), style = Stroke(2.5.dp.toPx()))

    // Ninja Band
    drawRect(Color(0xFF0F172A), Offset(cx - radius * 0.48f, cy - radius * 0.42f), Size(radius * 0.96f, radius * 0.22f))
    drawRoundRect(rarity.color, Offset(cx - radius * 0.16f, cy - radius * 0.40f), Size(radius * 0.32f, radius * 0.18f), CornerRadius(4f, 4f))

    // Eyes
    drawLivingEyes(cx, cy - radius * 0.12f, radius * 0.19f, radius * 0.085f, blinkScaleY, irisColor = Color(0xFFFACC15))

    // Nose
    drawCircle(Color(0xFFF472B6), radius * 0.04f, Offset(cx, cy - radius * 0.04f))

    // Whiskers
    drawLine(Color.White, Offset(cx - radius * 0.25f, cy), Offset(cx - radius * 0.55f, cy - radius * 0.05f), strokeWidth = 2.dp.toPx())
    drawLine(Color.White, Offset(cx - radius * 0.25f, cy + radius * 0.08f), Offset(cx - radius * 0.55f, cy + radius * 0.10f), strokeWidth = 2.dp.toPx())
    drawLine(Color.White, Offset(cx + radius * 0.25f, cy), Offset(cx + radius * 0.55f, cy - radius * 0.05f), strokeWidth = 2.dp.toPx())
    drawLine(Color.White, Offset(cx + radius * 0.25f, cy + radius * 0.08f), Offset(cx + radius * 0.55f, cy + radius * 0.10f), strokeWidth = 2.dp.toPx())

    // Front Paws
    drawOval(Color.White, Offset(cx - radius * 0.26f, cy + radius * 0.18f), Size(radius * 0.16f, radius * 0.22f))
    drawOval(Color.White, Offset(cx + radius * 0.10f, cy + radius * 0.18f), Size(radius * 0.16f, radius * 0.22f))
    drawOval(Color(0xFF1E293B), Offset(cx - radius * 0.26f, cy + radius * 0.18f), Size(radius * 0.16f, radius * 0.22f), style = Stroke(2.dp.toPx()))
    drawOval(Color(0xFF1E293B), Offset(cx + radius * 0.10f, cy + radius * 0.18f), Size(radius * 0.16f, radius * 0.22f), style = Stroke(2.dp.toPx()))

    // Feet
    drawOval(Color(0xFF475569), Offset(cx - radius * 0.40f, cy + radius * 0.50f), Size(radius * 0.24f, radius * 0.16f))
    drawOval(Color(0xFF475569), Offset(cx + radius * 0.16f, cy + radius * 0.50f), Size(radius * 0.24f, radius * 0.16f))
}

/**
 * 🐸 4. FULL BODY FROGGO JUMP (Round Body, Big Jumping Legs, Big Happy Smile)
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFullBodyFroggo(
    cx: Float, cy: Float, radius: Float, isAdult: Boolean, rarity: Rarity,
    bounceY: Float, blinkScaleY: Float
) {
    // Big Jumping Legs (Left & Right)
    drawOval(Color(0xFF16A34A), Offset(cx - radius * 0.68f, cy + radius * 0.20f), Size(radius * 0.35f, radius * 0.40f))
    drawOval(Color(0xFF16A34A), Offset(cx + radius * 0.33f, cy + radius * 0.20f), Size(radius * 0.35f, radius * 0.40f))
    drawOval(Color(0xFF14532D), Offset(cx - radius * 0.68f, cy + radius * 0.20f), Size(radius * 0.35f, radius * 0.40f), style = Stroke(2.dp.toPx()))
    drawOval(Color(0xFF14532D), Offset(cx + radius * 0.33f, cy + radius * 0.20f), Size(radius * 0.35f, radius * 0.40f), style = Stroke(2.dp.toPx()))

    // Big Eye Pods (Top)
    drawCircle(Color(0xFF22C55E), radius * 0.24f, Offset(cx - radius * 0.28f, cy - radius * 0.32f))
    drawCircle(Color(0xFF22C55E), radius * 0.24f, Offset(cx + radius * 0.28f, cy - radius * 0.32f))
    drawCircle(Color(0xFF14532D), radius * 0.24f, Offset(cx - radius * 0.28f, cy - radius * 0.32f), style = Stroke(2.5.dp.toPx()))
    drawCircle(Color(0xFF14532D), radius * 0.28f, Offset(cx + radius * 0.28f, cy - radius * 0.32f), style = Stroke(2.5.dp.toPx()))

    // Main Round Body
    drawOval(Color(0xFF4ADE80), Offset(cx - radius * 0.52f, cy - radius * 0.25f), Size(radius * 1.04f, radius * 0.85f))
    drawOval(Color(0xFF14532D), Offset(cx - radius * 0.52f, cy - radius * 0.25f), Size(radius * 1.04f, radius * 0.85f), style = Stroke(2.5.dp.toPx()))

    // Yellow/Cream Belly
    drawOval(Color(0xFFFEF08A), Offset(cx - radius * 0.34f, cy + radius * 0.05f), Size(radius * 0.68f, radius * 0.48f))

    // Big Bulging Eyes
    drawLivingEyes(cx, cy - radius * 0.32f, radius * 0.28f, radius * 0.14f, blinkScaleY)

    // Wide Smile
    val smile = Path().apply {
        moveTo(cx - radius * 0.25f, cy + radius * 0.08f)
        quadraticTo(cx, cy + radius * 0.24f, cx + radius * 0.25f, cy + radius * 0.08f)
    }
    drawPath(smile, Color(0xFF14532D), style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))

    // Webbed Feet
    drawOval(Color(0xFF22C55E), Offset(cx - radius * 0.45f, cy + radius * 0.52f), Size(radius * 0.26f, radius * 0.15f))
    drawOval(Color(0xFF22C55E), Offset(cx + radius * 0.19f, cy + radius * 0.52f), Size(radius * 0.26f, radius * 0.15f))
}

/**
 * 🦊 5. FULL BODY FIRE FOX (Flaming Bushy Tail, Diamond Head, Chest Fur, Paws)
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFullBodyFireFox(
    cx: Float, cy: Float, radius: Float, isAdult: Boolean, rarity: Rarity,
    tailAngle: Float, blinkScaleY: Float
) {
    // 1. Huge Flaming Bushy Tail (Behind body, swaying)
    rotate(degrees = tailAngle * 1.6f, pivot = Offset(cx - radius * 0.35f, cy + radius * 0.25f)) {
        val tailPath = Path().apply {
            moveTo(cx - radius * 0.30f, cy + radius * 0.25f)
            cubicTo(
                cx - radius * 0.85f, cy + radius * 0.40f,
                cx - radius * 1.15f, cy - radius * 0.20f,
                cx - radius * 0.70f, cy - radius * 0.60f
            )
            cubicTo(
                cx - radius * 0.50f, cy - radius * 0.40f,
                cx - radius * 0.50f, cy - radius * 0.10f,
                cx - radius * 0.20f, cy + radius * 0.15f
            )
            close()
        }
        drawPath(tailPath, Color(0xFFEA580C))
        drawPath(tailPath, Color(0xFF7C2D12), style = Stroke(2.5.dp.toPx()))

        // Flame Tip on Tail
        drawCircle(Color(0xFFFDE047), radius * 0.18f, Offset(cx - radius * 0.75f, cy - radius * 0.45f))
    }

    // 2. Fox Ears
    val earL = Path().apply {
        moveTo(cx - radius * 0.45f, cy - radius * 0.30f)
        lineTo(cx - radius * 0.55f, cy - radius * 0.90f)
        lineTo(cx - radius * 0.10f, cy - radius * 0.45f)
        close()
    }
    val earR = Path().apply {
        moveTo(cx + radius * 0.45f, cy - radius * 0.30f)
        lineTo(cx + radius * 0.55f, cy - radius * 0.90f)
        lineTo(cx + radius * 0.10f, cy - radius * 0.45f)
        close()
    }
    drawPath(earL, Color(0xFFEA580C))
    drawPath(earR, Color(0xFFEA580C))
    drawPath(earL, Color(0xFF7C2D12), style = Stroke(2.dp.toPx()))
    drawPath(earR, Color(0xFF7C2D12), style = Stroke(2.dp.toPx()))

    // 3. Torso
    drawOval(Color(0xFFF97316), Offset(cx - radius * 0.40f, cy - radius * 0.05f), Size(radius * 0.80f, radius * 0.70f))
    drawOval(Color(0xFF7C2D12), Offset(cx - radius * 0.40f, cy - radius * 0.05f), Size(radius * 0.80f, radius * 0.70f), style = Stroke(2.5.dp.toPx()))

    // Fluffy White Chest Fur
    drawOval(Color(0xFFFFFFFF), Offset(cx - radius * 0.25f, cy + radius * 0.05f), Size(radius * 0.50f, radius * 0.48f))

    // 4. Head Diamond
    val face = Path().apply {
        moveTo(cx - radius * 0.52f, cy - radius * 0.35f)
        lineTo(cx + radius * 0.52f, cy - radius * 0.35f)
        quadraticTo(cx + radius * 0.40f, cy + radius * 0.15f, cx, cy + radius * 0.30f)
        quadraticTo(cx - radius * 0.40f, cy + radius * 0.15f, cx - radius * 0.52f, cy - radius * 0.35f)
        close()
    }
    drawPath(face, Color(0xFFF97316))
    drawPath(face, Color(0xFF7C2D12), style = Stroke(2.5.dp.toPx()))

    // White Cheeks
    drawCircle(Color.White, radius * 0.16f, Offset(cx - radius * 0.28f, cy - radius * 0.05f))
    drawCircle(Color.White, radius * 0.16f, Offset(cx + radius * 0.28f, cy - radius * 0.05f))

    // Eyes
    drawLivingEyes(cx, cy - radius * 0.14f, radius * 0.20f, radius * 0.085f, blinkScaleY, irisColor = Color(0xFF0284C7))

    // Nose
    drawCircle(Color(0xFF0F172A), radius * 0.05f, Offset(cx, cy + radius * 0.22f))

    // Paws & Feet
    drawOval(Color(0xFF1E293B), Offset(cx - radius * 0.35f, cy + radius * 0.52f), Size(radius * 0.22f, radius * 0.15f))
    drawOval(Color(0xFF1E293B), Offset(cx + radius * 0.13f, cy + radius * 0.52f), Size(radius * 0.22f, radius * 0.15f))
}

/**
 * 🐧 6. FULL BODY PENGUIN JET (Chubby Body, Swimming Flippers, Beak & Orange Feet)
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFullBodyPenguin(
    cx: Float, cy: Float, radius: Float, isAdult: Boolean, rarity: Rarity,
    flipperAngle: Float, blinkScaleY: Float
) {
    // Flippers (Waving)
    rotate(degrees = flipperAngle, pivot = Offset(cx - radius * 0.45f, cy + radius * 0.1f)) {
        drawOval(Color(0xFF0369A1), Offset(cx - radius * 0.70f, cy), Size(radius * 0.28f, radius * 0.50f))
        drawOval(Color(0xFF0C4A6E), Offset(cx - radius * 0.70f, cy), Size(radius * 0.28f, radius * 0.50f), style = Stroke(2.dp.toPx()))
    }
    rotate(degrees = -flipperAngle, pivot = Offset(cx + radius * 0.45f, cy + radius * 0.1f)) {
        drawOval(Color(0xFF0369A1), Offset(cx + radius * 0.42f, cy), Size(radius * 0.28f, radius * 0.50f))
        drawOval(Color(0xFF0C4A6E), Offset(cx + radius * 0.42f, cy), Size(radius * 0.28f, radius * 0.50f), style = Stroke(2.dp.toPx()))
    }

    // Chubby Body
    drawOval(Color(0xFF0284C7), Offset(cx - radius * 0.48f, cy - radius * 0.42f), Size(radius * 0.96f, radius * 1.05f))
    drawOval(Color(0xFF0C4A6E), Offset(cx - radius * 0.48f, cy - radius * 0.42f), Size(radius * 0.96f, radius * 1.05f), style = Stroke(2.5.dp.toPx()))

    // Big White Belly
    drawOval(Color(0xFFF0F9FF), Offset(cx - radius * 0.36f, cy - radius * 0.25f), Size(radius * 0.72f, radius * 0.80f))

    // Eyes
    drawLivingEyes(cx, cy - radius * 0.12f, radius * 0.18f, radius * 0.085f, blinkScaleY)

    // Orange Beak
    drawOval(Color(0xFFF59E0B), Offset(cx - radius * 0.12f, cy + radius * 0.02f), Size(radius * 0.24f, radius * 0.15f))

    // Orange Waddle Feet
    drawOval(Color(0xFFF59E0B), Offset(cx - radius * 0.32f, cy + radius * 0.56f), Size(radius * 0.26f, radius * 0.16f))
    drawOval(Color(0xFFF59E0B), Offset(cx + radius * 0.06f, cy + radius * 0.56f), Size(radius * 0.26f, radius * 0.16f))
}

/**
 * 🦅 7. FULL BODY GARUDA EMAS (Feather Crown, Massive Flapping Wings, Torso, Talons)
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFullBodyGaruda(
    cx: Float, cy: Float, radius: Float, isAdult: Boolean, rarity: Rarity,
    wingFlap: Float, blinkScaleY: Float
) {
    // Massive Golden Wings (Flapping in flight stance)
    rotate(degrees = wingFlap * 1.8f, pivot = Offset(cx - radius * 0.45f, cy)) {
        drawOval(Color(0xFFD97706), Offset(cx - radius * 0.95f, cy - radius * 0.25f), Size(radius * 0.48f, radius * 0.75f))
        drawOval(Color(0xFF78350F), Offset(cx - radius * 0.95f, cy - radius * 0.25f), Size(radius * 0.48f, radius * 0.75f), style = Stroke(2.dp.toPx()))
    }
    rotate(degrees = -wingFlap * 1.8f, pivot = Offset(cx + radius * 0.45f, cy)) {
        drawOval(Color(0xFFD97706), Offset(cx + radius * 0.47f, cy - radius * 0.25f), Size(radius * 0.48f, radius * 0.75f))
        drawOval(Color(0xFF78350F), Offset(cx + radius * 0.47f, cy - radius * 0.25f), Size(radius * 0.48f, radius * 0.75f), style = Stroke(2.dp.toPx()))
    }

    // Crown
    val crown = Path().apply {
        moveTo(cx - radius * 0.32f, cy - radius * 0.35f)
        lineTo(cx - radius * 0.16f, cy - radius * 0.85f)
        lineTo(cx, cy - radius * 0.50f)
        lineTo(cx + radius * 0.16f, cy - radius * 0.85f)
        lineTo(cx + radius * 0.32f, cy - radius * 0.35f)
        close()
    }
    drawPath(crown, Color(0xFFF59E0B))
    drawPath(crown, Color(0xFF78350F), style = Stroke(2.dp.toPx()))

    // Body
    drawOval(Color(0xFFFBBF24), Offset(cx - radius * 0.42f, cy - radius * 0.15f), Size(radius * 0.84f, radius * 0.75f))
    drawOval(Color(0xFF78350F), Offset(cx - radius * 0.42f, cy - radius * 0.15f), Size(radius * 0.84f, radius * 0.75f), style = Stroke(2.5.dp.toPx()))

    // Head
    drawCircle(Color(0xFFFBBF24), radius * 0.48f, Offset(cx, cy - radius * 0.22f))
    drawCircle(Color(0xFF78350F), radius * 0.48f, Offset(cx, cy - radius * 0.22f), style = Stroke(2.5.dp.toPx()))

    // Eyes
    drawLivingEyes(cx, cy - radius * 0.22f, radius * 0.20f, radius * 0.09f, blinkScaleY, irisColor = Color(0xFF7C3AED))

    // Sharp Curved Beak
    val beak = Path().apply {
        moveTo(cx - radius * 0.14f, cy - radius * 0.08f)
        lineTo(cx + radius * 0.14f, cy - radius * 0.08f)
        lineTo(cx, cy + radius * 0.30f)
        close()
    }
    drawPath(beak, Color(0xFFEA580C))
    drawPath(beak, Color(0xFF78350F), style = Stroke(2.dp.toPx()))

    // Sharp Talons
    drawOval(Color(0xFFEA580C), Offset(cx - radius * 0.28f, cy + radius * 0.54f), Size(radius * 0.20f, radius * 0.14f))
    drawOval(Color(0xFFEA580C), Offset(cx + radius * 0.08f, cy + radius * 0.54f), Size(radius * 0.20f, radius * 0.14f))
}

/**
 * 🐺 8. FULL BODY SHADOW WOLF (Mystic Mane, Glowing Purple Tail, Quadruped Body, Paws)
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFullBodyShadowWolf(
    cx: Float, cy: Float, radius: Float, isAdult: Boolean, rarity: Rarity,
    tailAngle: Float, earTwitch: Float, blinkScaleY: Float
) {
    // Mystic Wolf Tail (Sways)
    rotate(degrees = tailAngle * 1.4f, pivot = Offset(cx - radius * 0.38f, cy + radius * 0.30f)) {
        val tail = Path().apply {
            moveTo(cx - radius * 0.30f, cy + radius * 0.30f)
            cubicTo(
                cx - radius * 0.90f, cy + radius * 0.35f,
                cx - radius * 1.05f, cy - radius * 0.20f,
                cx - radius * 0.65f, cy - radius * 0.50f
            )
            cubicTo(
                cx - radius * 0.45f, cy - radius * 0.30f,
                cx - radius * 0.45f, cy + radius * 0.05f,
                cx - radius * 0.20f, cy + radius * 0.20f
            )
            close()
        }
        drawPath(tail, Color(0xFF581C87))
        drawPath(tail, Color(0xFF1E1B4B), style = Stroke(2.dp.toPx()))
    }

    // Ears
    val earL = Path().apply {
        moveTo(cx - radius * 0.42f, cy - radius * 0.30f)
        lineTo(cx - radius * 0.55f, cy - radius * 0.85f)
        lineTo(cx - radius * 0.12f, cy - radius * 0.45f)
        close()
    }
    val earR = Path().apply {
        moveTo(cx + radius * 0.42f, cy - radius * 0.30f)
        lineTo(cx + radius * 0.55f, cy - radius * 0.85f)
        lineTo(cx + radius * 0.12f, cy - radius * 0.45f)
        close()
    }
    drawPath(earL, Color(0xFF581C87))
    drawPath(earR, Color(0xFF581C87))
    drawPath(earL, Color(0xFF1E1B4B), style = Stroke(2.dp.toPx()))
    drawPath(earR, Color(0xFF1E1B4B), style = Stroke(2.dp.toPx()))

    // Body
    drawOval(Color(0xFF7E22CE), Offset(cx - radius * 0.42f, cy - radius * 0.08f), Size(radius * 0.84f, radius * 0.72f))
    drawOval(Color(0xFF1E1B4B), Offset(cx - radius * 0.42f, cy - radius * 0.08f), Size(radius * 0.84f, radius * 0.72f), style = Stroke(2.5.dp.toPx()))

    // Head
    drawCircle(Color(0xFF7E22CE), radius * 0.50f, Offset(cx, cy - radius * 0.20f))
    drawCircle(Color(0xFF1E1B4B), radius * 0.50f, Offset(cx, cy - radius * 0.20f), style = Stroke(2.5.dp.toPx()))

    // Dark Snout
    drawRoundRect(Color(0xFF3B0764), Offset(cx - radius * 0.22f, cy - radius * 0.08f), Size(radius * 0.44f, radius * 0.32f), CornerRadius(10f, 10f))
    drawCircle(Color(0xFF0F172A), radius * 0.05f, Offset(cx, cy - radius * 0.02f))

    // Glowing Eyes
    drawLivingEyes(cx, cy - radius * 0.18f, radius * 0.20f, radius * 0.085f, blinkScaleY, irisColor = Color(0xFFE879F9))

    // Paws
    drawOval(Color(0xFF3B0764), Offset(cx - radius * 0.35f, cy + radius * 0.52f), Size(radius * 0.22f, radius * 0.15f))
    drawOval(Color(0xFF3B0764), Offset(cx + radius * 0.13f, cy + radius * 0.52f), Size(radius * 0.22f, radius * 0.15f))
}

/**
 * 🐉 9. FULL BODY COSMIC DRAGON (Golden Horns, Dragon Wings, Emerald Torso, Tail & Claws)
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFullBodyCosmicDragon(
    cx: Float, cy: Float, radius: Float, isAdult: Boolean, rarity: Rarity,
    tailAngle: Float, wingFlap: Float, blinkScaleY: Float
) {
    // 1. Dragon Spiked Tail (Sways)
    rotate(degrees = tailAngle * 1.5f, pivot = Offset(cx - radius * 0.38f, cy + radius * 0.30f)) {
        val tail = Path().apply {
            moveTo(cx - radius * 0.30f, cy + radius * 0.30f)
            cubicTo(
                cx - radius * 0.85f, cy + radius * 0.35f,
                cx - radius * 1.10f, cy - radius * 0.15f,
                cx - radius * 0.75f, cy - radius * 0.45f
            )
        }
        drawPath(tail, Color(0xFF059669), style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))
        drawPath(tail, Color(0xFF064E3B), style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round), blendMode = androidx.compose.ui.graphics.BlendMode.DstOver)
    }

    // 2. Dragon Wings
    rotate(degrees = wingFlap * 1.6f, pivot = Offset(cx - radius * 0.45f, cy)) {
        drawOval(Color(0xFF34D399), Offset(cx - radius * 0.95f, cy - radius * 0.30f), Size(radius * 0.50f, radius * 0.70f))
        drawOval(Color(0xFF064E3B), Offset(cx - radius * 0.95f, cy - radius * 0.30f), Size(radius * 0.50f, radius * 0.70f), style = Stroke(2.dp.toPx()))
    }
    rotate(degrees = -wingFlap * 1.6f, pivot = Offset(cx + radius * 0.45f, cy)) {
        drawOval(Color(0xFF34D399), Offset(cx + radius * 0.45f, cy - radius * 0.30f), Size(radius * 0.50f, radius * 0.70f))
        drawOval(Color(0xFF064E3B), Offset(cx + radius * 0.45f, cy - radius * 0.30f), Size(radius * 0.50f, radius * 0.70f), style = Stroke(2.dp.toPx()))
    }

    // 3. Golden Horns
    val hornL = Path().apply {
        moveTo(cx - radius * 0.25f, cy - radius * 0.35f)
        quadraticTo(cx - radius * 0.55f, cy - radius * 0.95f, cx - radius * 0.75f, cy - radius * 0.75f)
        quadraticTo(cx - radius * 0.40f, cy - radius * 0.60f, cx - radius * 0.15f, cy - radius * 0.30f)
        close()
    }
    val hornR = Path().apply {
        moveTo(cx + radius * 0.25f, cy - radius * 0.35f)
        quadraticTo(cx + radius * 0.55f, cy - radius * 0.95f, cx + radius * 0.75f, cy - radius * 0.75f)
        quadraticTo(cx + radius * 0.40f, cy - radius * 0.60f, cx + radius * 0.15f, cy - radius * 0.30f)
        close()
    }
    drawPath(hornL, Color(0xFFFBBF24))
    drawPath(hornR, Color(0xFFFBBF24))
    drawPath(hornL, Color(0xFF78350F), style = Stroke(2.dp.toPx()))
    drawPath(hornR, Color(0xFF78350F), style = Stroke(2.dp.toPx()))

    // 4. Body
    drawOval(Color(0xFF059669), Offset(cx - radius * 0.44f, cy - radius * 0.10f), Size(radius * 0.88f, radius * 0.75f))
    drawOval(Color(0xFF064E3B), Offset(cx - radius * 0.44f, cy - radius * 0.10f), Size(radius * 0.88f, radius * 0.75f), style = Stroke(2.5.dp.toPx()))

    // Golden Belly Scales
    drawOval(Color(0xFFFDE68A), Offset(cx - radius * 0.28f, cy + radius * 0.05f), Size(radius * 0.56f, radius * 0.50f))

    // 5. Head
    drawCircle(Color(0xFF059669), radius * 0.52f, Offset(cx, cy - radius * 0.20f))
    drawCircle(Color(0xFF064E3B), radius * 0.52f, Offset(cx, cy - radius * 0.20f), style = Stroke(2.5.dp.toPx()))

    // Snout
    drawRoundRect(Color(0xFF34D399), Offset(cx - radius * 0.34f, cy - radius * 0.08f), Size(radius * 0.68f, radius * 0.35f), CornerRadius(12f, 12f))
    drawCircle(Color(0xFF065F46), radius * 0.04f, Offset(cx - radius * 0.14f, cy + radius * 0.05f))
    drawCircle(Color(0xFF065F46), radius * 0.04f, Offset(cx + radius * 0.14f, cy + radius * 0.05f))

    // Glowing Eyes
    drawLivingEyes(cx, cy - radius * 0.18f, radius * 0.22f, radius * 0.095f, blinkScaleY, irisColor = Color(0xFFFDE047))

    // Claws
    drawOval(Color(0xFF065F46), Offset(cx - radius * 0.36f, cy + radius * 0.54f), Size(radius * 0.24f, radius * 0.15f))
    drawOval(Color(0xFF065F46), Offset(cx + radius * 0.12f, cy + radius * 0.54f), Size(radius * 0.24f, radius * 0.15f))
}

/**
 * 🦄 10. FULL BODY SUN UNICORN (Golden Horn, Rainbow Mane, Pure White Body, Hooves)
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFullBodySunUnicorn(
    cx: Float, cy: Float, radius: Float, isAdult: Boolean, rarity: Rarity,
    tailAngle: Float, earTwitch: Float, blinkScaleY: Float
) {
    // Rainbow Tail (Sways)
    rotate(degrees = tailAngle * 1.5f, pivot = Offset(cx - radius * 0.40f, cy + radius * 0.35f)) {
        drawCircle(Color(0xFFF472B6), radius * 0.16f, Offset(cx - radius * 0.50f, cy + radius * 0.25f))
        drawCircle(Color(0xFFA855F7), radius * 0.16f, Offset(cx - radius * 0.60f, cy + radius * 0.40f))
        drawCircle(Color(0xFF38BDF8), radius * 0.16f, Offset(cx - radius * 0.45f, cy + radius * 0.55f))
    }

    // Golden Horn
    val horn = Path().apply {
        moveTo(cx - radius * 0.09f, cy - radius * 0.45f)
        lineTo(cx + radius * 0.09f, cy - radius * 0.45f)
        lineTo(cx, cy - radius * 1.15f)
        close()
    }
    drawPath(horn, Color(0xFFF59E0B))
    drawPath(horn, Color(0xFF78350F), style = Stroke(2.dp.toPx()))

    // Rainbow Mane (Pink / Purple / Cyan)
    drawCircle(Color(0xFFF472B6), radius * 0.20f, Offset(cx - radius * 0.35f, cy - radius * 0.30f))
    drawCircle(Color(0xFFA855F7), radius * 0.20f, Offset(cx + radius * 0.35f, cy - radius * 0.30f))
    drawCircle(Color(0xFF38BDF8), radius * 0.18f, Offset(cx + radius * 0.38f, cy - radius * 0.05f))

    // Pure White Body
    drawOval(Color(0xFFFFFFFF), Offset(cx - radius * 0.44f, cy - radius * 0.08f), Size(radius * 0.88f, radius * 0.72f))
    drawOval(Color(0xFFCBD5E1), Offset(cx - radius * 0.44f, cy - radius * 0.08f), Size(radius * 0.88f, radius * 0.72f), style = Stroke(2.5.dp.toPx()))

    // Head
    drawCircle(Color(0xFFFFFFFF), radius * 0.50f, Offset(cx, cy - radius * 0.20f))
    drawCircle(Color(0xFFCBD5E1), radius * 0.50f, Offset(cx, cy - radius * 0.20f), style = Stroke(2.5.dp.toPx()))

    // Cheeks
    drawCircle(Color(0xFFF472B6), radius * 0.11f, Offset(cx - radius * 0.28f, cy - radius * 0.08f))
    drawCircle(Color(0xFFF472B6), radius * 0.11f, Offset(cx + radius * 0.28f, cy - radius * 0.08f))

    // Sparkly Magical Eyes
    drawLivingEyes(cx, cy - radius * 0.16f, radius * 0.19f, radius * 0.09f, blinkScaleY, irisColor = Color(0xFF6366F1))

    // Golden Hooves
    drawOval(Color(0xFFF59E0B), Offset(cx - radius * 0.36f, cy + radius * 0.52f), Size(radius * 0.22f, radius * 0.15f))
    drawOval(Color(0xFFF59E0B), Offset(cx + radius * 0.14f, cy + radius * 0.52f), Size(radius * 0.22f, radius * 0.15f))
    drawOval(Color(0xFF78350F), Offset(cx - radius * 0.36f, cy + radius * 0.52f), Size(radius * 0.22f, radius * 0.15f), style = Stroke(2.dp.toPx()))
    drawOval(Color(0xFF78350F), Offset(cx + radius * 0.14f, cy + radius * 0.52f), Size(radius * 0.22f, radius * 0.15f), style = Stroke(2.dp.toPx()))
}
