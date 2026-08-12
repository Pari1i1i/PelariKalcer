package com.example.pelarikalcer.ui.screens.onboarding

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pelarikalcer.R
import com.example.pelarikalcer.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String,
    val accentColor: Color
)

val onboardingPages = listOf(
    OnboardingPage(
        imageRes = R.drawable.onboard_run,
        title = "Lacak Setiap Langkahmu",
        description = "GPS presisi merekam jarak, waktu, pace, dan kalori larimu secara real-time. Setiap kilometer dihitung!",
        accentColor = NeonGreen
    ),
    OnboardingPage(
        imageRes = R.drawable.onboard_streak,
        title = "Bangun Streak & Raih Poin",
        description = "Berlari setiap hari untuk membangun streak berapi. Kumpulkan poin, beli pet, dan jadilah yang teratas di leaderboard!",
        accentColor = StreakOrange
    ),
    OnboardingPage(
        imageRes = R.drawable.onboard_ai,
        title = "Punya AI Coach Pribadi",
        description = "PelariCoach siap memberi saran latihan, nutrisi, dan motivasi personal 24/7. Gratis, tanpa batas!",
        accentColor = AccentOrange
    )
)

fun setOnboardingDone(context: Context) {
    context.getSharedPreferences("pelarikalcer_prefs", Context.MODE_PRIVATE)
        .edit().putBoolean("onboarding_done", true).apply()
}

fun isOnboardingDone(context: Context): Boolean {
    return context.getSharedPreferences("pelarikalcer_prefs", Context.MODE_PRIVATE)
        .getBoolean("onboarding_done", false)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val pageData = onboardingPages[page]
                OnboardingPageContent(page = pageData)
            }

            // Bottom controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dot Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    onboardingPages.indices.forEach { i ->
                        val isSelected = i == pagerState.currentPage
                        val accentColor = onboardingPages[i].accentColor
                        Box(
                            modifier = Modifier
                                .animateContentSize()
                                .width(if (isSelected) 28.dp else 8.dp)
                                .height(8.dp)
                                .background(
                                    if (isSelected) accentColor else TextMuted,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }

                val isLast = pagerState.currentPage == onboardingPages.size - 1
                val currentAccent = onboardingPages[pagerState.currentPage].accentColor

                if (isLast) {
                    Button(
                        onClick = {
                            setOnboardingDone(context)
                            onFinish()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text(
                            "Mulai Sekarang!",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = DeepNavy
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            setOnboardingDone(context)
                            onFinish()
                        }) {
                            Text("Lewati", color = TextMuted, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = currentAccent),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.ArrowForward, null, tint = DeepNavy, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "float_y"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(y = floatOffset.dp)
                .background(
                    Brush.radialGradient(
                        listOf(page.accentColor.copy(alpha = 0.15f), Color.Transparent)
                    ),
                    CircleShape
                )
                .clip(RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = page.title,
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(28.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = page.title,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
