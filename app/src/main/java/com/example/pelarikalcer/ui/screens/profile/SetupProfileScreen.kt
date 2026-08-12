package com.example.pelarikalcer.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pelarikalcer.ui.theme.*

@Composable
fun SetupProfileScreen(
    onSaveSetup: (weight: Double, height: Double) -> Unit
) {
    var weightInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(CardSurface, DeepNavy, DeepNavy)
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.linearGradient(listOf(NeonGreen, Color(0xFF00C896))),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DirectionsRun,
                    contentDescription = null,
                    tint = DeepNavy,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Sedikit Lagi!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "Masukkan berat & tinggi badanmu untuk menghitung kalori dengan akurat.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // Weight Field
            OutlinedTextField(
                value = weightInput,
                onValueChange = {
                    weightInput = it
                    errorMessage = null
                },
                label = { Text("Berat Badan (kg)", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.FitnessCenter, null, tint = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = TextMuted,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = NeonGreen,
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Height Field
            OutlinedTextField(
                value = heightInput,
                onValueChange = {
                    heightInput = it
                    errorMessage = null
                },
                label = { Text("Tinggi Badan (cm)", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Height, null, tint = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = TextMuted,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = NeonGreen,
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface
                )
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = DangerRed,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val w = weightInput.toDoubleOrNull()
                    val h = heightInput.toDoubleOrNull()
                    if (w == null || w <= 0) {
                        errorMessage = "Masukkan berat badan yang valid!"
                        return@Button
                    }
                    if (h == null || h <= 0) {
                        errorMessage = "Masukkan tinggi badan yang valid!"
                        return@Button
                    }
                    onSaveSetup(w, h)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text(
                    "Simpan & Lanjutkan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DeepNavy
                )
            }
        }
    }
}
