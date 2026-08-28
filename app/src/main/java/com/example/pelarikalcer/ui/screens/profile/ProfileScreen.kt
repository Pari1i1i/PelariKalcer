package com.example.pelarikalcer.ui.screens.profile

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pelarikalcer.data.local.entity.UserEntity
import com.example.pelarikalcer.ui.theme.*

@Composable
fun ProfileScreen(
    user: UserEntity?,
    totalDistanceKm: Double,
    totalRuns: Int,
    totalCalories: Int,
    onLogout: () -> Unit,
    onUpdateProfile: (UserEntity) -> Unit
) {
    var showEditSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(CardSurface, DeepNavy)
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            Brush.linearGradient(listOf(NeonGreen, Color(0xFF00C896))),
                            CircleShape
                        )
                        .border(3.dp, NeonGreen.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (user?.username?.firstOrNull()?.uppercaseChar() ?: 'R').toString(),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepNavy
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user?.fullName?.ifEmpty { user.username } ?: user?.username ?: "Runner",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "@${user?.username ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Streak Badge
                if ((user?.currentStreak ?: 0) > 0) {
                    Box(
                        modifier = Modifier
                            .background(StreakOrange.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, null, tint = StreakOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${user?.currentStreak} hari streak",
                                color = StreakOrange,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showEditSheet = true },
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Profil")
                }
            }
        }

        // Stats Summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileStatCard(
                modifier = Modifier.weight(1f),
                value = totalRuns.toString(),
                label = "Total Lari",
                icon = Icons.Default.DirectionsRun,
                color = NeonGreen
            )
            ProfileStatCard(
                modifier = Modifier.weight(1f),
                value = String.format("%.1f", totalDistanceKm),
                label = "Total KM",
                icon = Icons.Default.Route,
                color = AccentOrange
            )
            ProfileStatCard(
                modifier = Modifier.weight(1f),
                value = user?.totalPoints?.toString() ?: "0",
                label = "Poin",
                icon = Icons.Default.Stars,
                color = GoldStar
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Physical Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Data Fisik",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                ProfileInfoRow(icon = Icons.Default.FitnessCenter, label = "Berat Badan", value = "${user?.weightKg ?: 65.0} kg")
                HorizontalDivider(color = TextMuted.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))
                ProfileInfoRow(icon = Icons.Default.Email, label = "Email", value = user?.email ?: "-")
                if (!user?.bio.isNullOrEmpty()) {
                    HorizontalDivider(color = TextMuted.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))
                    ProfileInfoRow(icon = Icons.Default.Info, label = "Bio", value = user?.bio ?: "-")
                }
            }
        }

        // Logout
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .clickable { onLogout() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Logout", tint = DangerRed)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Keluar dari Akun", fontWeight = FontWeight.SemiBold, color = DangerRed)
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    if (showEditSheet && user != null) {
        EditProfileSheet(
            user = user,
            onDismiss = { showEditSheet = false },
            onSave = { updated ->
                onUpdateProfile(updated)
                showEditSheet = false
            }
        )
    }
}

@Composable
fun ProfileStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = TextPrimary)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = TextSecondary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(text = value, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSheet(
    user: UserEntity,
    onDismiss: () -> Unit,
    onSave: (UserEntity) -> Unit
) {
    var fullName by remember { mutableStateOf(user.fullName ?: "") }
    var bio by remember { mutableStateOf(user.bio ?: "") }
    var weight by remember { mutableStateOf(user.weightKg.toString()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(40.dp, 4.dp)
                    .background(TextMuted, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                "Edit Profil",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Nama Lengkap", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = TextMuted,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = NeonGreen
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = TextMuted,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = NeonGreen
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Berat Badan (kg)", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = TextMuted,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = NeonGreen
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val updated = user.copy(
                        fullName = fullName,
                        bio = bio,
                        weightKg = weight.toDoubleOrNull() ?: user.weightKg,
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(updated)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold, color = DeepNavy, fontSize = 16.sp)
            }
        }
    }
}
