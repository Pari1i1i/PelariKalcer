package com.example.pelarikalcer.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pelarikalcer.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(CardSurface, DeepNavy, DeepNavy)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
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
                text = "PelariKalcer",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "Lacak lari, raih prestasi",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = TextSecondary) },
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

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextSecondary) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null,
                            tint = TextSecondary
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
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

            // Error
            AnimatedError(message = (authState as? AuthState.Error)?.message)

            Spacer(modifier = Modifier.height(28.dp))

            // Login Button
            Button(
                onClick = { viewModel.login(username, password) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = authState !is AuthState.Loading && username.isNotBlank() && password.isNotBlank(),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DeepNavy, strokeWidth = 2.dp)
                } else {
                    Text("Masuk", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepNavy)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = "Belum punya akun? Daftar di sini",
                    color = NeonGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AnimatedError(message: String?) {
    if (!message.isNullOrEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DangerRed.copy(alpha = 0.15f))
                .padding(12.dp)
        ) {
            Text(
                text = message,
                color = DangerRed,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
