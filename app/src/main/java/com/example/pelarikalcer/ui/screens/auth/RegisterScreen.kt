package com.example.pelarikalcer.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pelarikalcer.ui.theme.*

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    val passwordError = if (confirmPassword.isNotEmpty() && password != confirmPassword)
        "Password tidak cocok" else null

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onRegisterSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(CardSurface, DeepNavy, DeepNavy)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        Brush.linearGradient(listOf(NeonGreen, Color(0xFF00C896))),
                        RoundedCornerShape(22.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PersonAdd, null, tint = DeepNavy, modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Buat Akun", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Text("Bergabung dan mulai berlari!", style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary, modifier = Modifier.padding(bottom = 32.dp))

            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen, unfocusedBorderColor = TextMuted,
                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                cursorColor = NeonGreen, focusedContainerColor = CardSurface,
                unfocusedContainerColor = CardSurface
            )

            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Username", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = TextSecondary) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(16.dp), colors = fieldColors
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = TextSecondary) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(16.dp), colors = fieldColors
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextSecondary) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = TextSecondary)
                    }
                },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp), colors = fieldColors
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it },
                label = { Text("Konfirmasi Password", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.LockOpen, null, tint = TextSecondary) },
                trailingIcon = {
                    IconButton(onClick = { showConfirm = !showConfirm }) {
                        Icon(if (showConfirm) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = TextSecondary)
                    }
                },
                isError = passwordError != null,
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp), colors = fieldColors
            )

            if (passwordError != null) {
                Text(passwordError, color = DangerRed, style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, top = 4.dp))
            }

            AnimatedError(message = (authState as? AuthState.Error)?.message)

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { viewModel.register(username, email, password) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = authState !is AuthState.Loading && username.isNotBlank()
                        && email.isNotBlank() && password.isNotBlank() && passwordError == null,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DeepNavy, strokeWidth = 2.dp)
                } else {
                    Text("Daftar Sekarang", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepNavy)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("Sudah punya akun? Masuk", color = NeonGreen, fontWeight = FontWeight.Medium)
            }
        }
    }
}
