package com.example.pelarikalcer.ui.screens.shop

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pelarikalcer.data.local.entity.*
import com.example.pelarikalcer.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Dedicated Full Screen Pet Screen:
 * - 1 Full Screen Dedicated for Pet
 * - Renaming Feature (User can name their Pet!)
 * - Leveling system (Level 1-10: Baby, Level 11+: Adult)
 * - Hatching with Live Countdown (Common: 5s, +10s per next rarity)
 * - EXP Progress Bar & Level Up (Pure Gamification, No Pay-to-win stat changes on run)
 * - Clean modern UI with Vector Icons (No Windows emojis)
 * - Integrated Modals for Gacha, Direct Purchase Shop & Inventory Swap
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPetScreen(
    activePet: PetEntity?,
    allPets: List<PetEntity>,
    userPoints: Int,
    onRenamePet: (Long, String) -> Unit,
    onFeedPet: (Long, Int) -> Unit,
    onSwapActivePet: (Long) -> Unit,
    onGachaRoll: suspend () -> PetEntity?,
    onDirectBuy: suspend (PetSpecies) -> PetEntity?
) {
    val scope = rememberCoroutineScope()

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInputText by remember { mutableStateOf("") }

    var showGachaSheet by remember { mutableStateOf(false) }
    var showDirectShopSheet by remember { mutableStateOf(false) }
    var showInventorySheet by remember { mutableStateOf(false) }

    var isGachaRolling by remember { mutableStateOf(false) }
    var gachaResultPet by remember { mutableStateOf<PetEntity?>(null) }
    var selectedSpeciesToBuy by remember { mutableStateOf<PetSpecies?>(null) }

    // Live Clock ticker for smooth second countdown rendering
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(500L)
            currentTime = System.currentTimeMillis()
        }
    }

    val pet = activePet

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF090D1A),
                        DeepNavy,
                        Color(0xFF0F172A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            PetTopBar(
                userPoints = userPoints,
                onOpenGacha = { showGachaSheet = true },
                onOpenShop = { showDirectShopSheet = true },
                onOpenInventory = { showInventorySheet = true }
            )

            if (pet != null) {
                Spacer(modifier = Modifier.height(14.dp))

                // Hero Pet Spotlight Card
                PetSpotlightSection(
                    pet = pet,
                    onOpenRename = {
                        renameInputText = pet.displayName
                        showRenameDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Growth & Status Card
                if (!pet.isHatched) {
                    HatchCountdownCard(pet = pet, currentTime = currentTime)
                } else {
                    PetLevelExpCard(
                        pet = pet,
                        userPoints = userPoints,
                        onFeedExp = { pts -> onFeedPet(pet.id, pts) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Gamification Info Card (Cosmetic Tier & Loyalty Companion)
                PetGamificationInfoCard(pet = pet)

            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Belum ada Pet aktif.", color = TextSecondary, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showGachaSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                        ) {
                            Text("Buka Gacha Telur", color = DeepNavy, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Quick Bar with Clean Spacing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = { showGachaSheet = true },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Casino, null, tint = DeepNavy, modifier = Modifier.size(20.dp))
                        Text("Gacha Telur", color = DeepNavy, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                }

                OutlinedButton(
                    onClick = { showInventorySheet = true },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GoldStar.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldStar)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Inventory2, null, tint = GoldStar, modifier = Modifier.size(20.dp))
                        Text("Koleksi (${allPets.size})", color = GoldStar, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    // Rename Dialog
    if (showRenameDialog && pet != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = DarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Edit, null, tint = NeonGreen)
                    Text("Beri Nama Pet", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column {
                    Text("Panggil pet kesayanganmu dengan nama keren!", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = renameInputText,
                        onValueChange = { renameInputText = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = TextMuted,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = NeonGreen
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInputText.isNotBlank()) {
                            onRenamePet(pet.id, renameInputText)
                        }
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text("Simpan", color = DeepNavy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }

    // Gacha Modal Sheet
    if (showGachaSheet) {
        ModalBottomSheet(
            onDismissRequest = { showGachaSheet = false },
            containerColor = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Casino, null, tint = GoldStar)
                    Text("Gacha Telur Misteri", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = GoldStar)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Dapatkan telur Common (5s), Uncommon (15s), Rare (25s), Epic (35s), Legendary (45s)!", fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGachaRolling) {
                        CircularProgressIndicator(color = NeonGreen, modifier = Modifier.size(100.dp), strokeWidth = 5.dp)
                        Text("Membuka...", fontWeight = FontWeight.Bold, color = NeonGreen)
                    } else {
                        PetAvatarCanvas(
                            speciesId = 9,
                            stage = PetStage.EGG,
                            rarity = Rarity.LEGENDARY,
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val canAfford = userPoints >= GachaConfig.GACHA_COST_POINTS
                Button(
                    onClick = {
                        if (canAfford && !isGachaRolling) {
                            isGachaRolling = true
                            scope.launch {
                                delay(1600)
                                val result = onGachaRoll()
                                isGachaRolling = false
                                showGachaSheet = false
                                gachaResultPet = result
                            }
                        }
                    },
                    enabled = canAfford && !isGachaRolling,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text(
                        if (canAfford) "GACHA SEKARANG (100 Poin)" else "Poin Tidak Cukup (Butuh 100 Pts)",
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepNavy,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Rate details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Rarity.values().forEach { r ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(r.displayName, color = r.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${r.hatchSeconds}s", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }

    // Direct Buy Store Sheet
    if (showDirectShopSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDirectShopSheet = false },
            containerColor = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ShoppingCart, null, tint = TextPrimary)
                    Text("Beli Telur Pilihan", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                }
                Text("Pilih pet tier impianmu langsung dengan poin!", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(14.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(defaultSpeciesCatalog) { species ->
                        val canAfford = userPoints >= species.rarity.directCostPoints
                        Card(
                            onClick = {
                                selectedSpeciesToBuy = species
                            },
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, species.rarity.color.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier.size(56.dp).background(species.rarity.color.copy(0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PetAvatarCanvas(
                                        speciesId = species.id,
                                        stage = PetStage.ADULT,
                                        rarity = species.rarity,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(species.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                                Text("${species.rarity.hatchSeconds}s hatch", color = species.rarity.color, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Stars, null, tint = GoldStar, modifier = Modifier.size(14.dp))
                                    Text("${species.rarity.directCostPoints}", fontWeight = FontWeight.ExtraBold, color = GoldStar, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Direct Buy Confirm Dialog
    selectedSpeciesToBuy?.let { species ->
        val cost = species.rarity.directCostPoints
        val canAfford = userPoints >= cost

        AlertDialog(
            onDismissRequest = { selectedSpeciesToBuy = null },
            containerColor = DarkSurface,
            title = {
                Text("Beli Telur ${species.name}?", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(species.description, color = TextSecondary, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.HourglassBottom, null, tint = GoldStar, modifier = Modifier.size(16.dp))
                        Text("Waktu menetas: ${species.rarity.hatchSeconds} detik", color = GoldStar, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Stars, null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                        Text("Harga: $cost Poin", color = NeonGreen, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                if (canAfford) {
                    Button(
                        onClick = {
                            scope.launch {
                                onDirectBuy(species)
                                selectedSpeciesToBuy = null
                                showDirectShopSheet = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text("Beli Sekarang", color = DeepNavy, fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(onClick = { selectedSpeciesToBuy = null }) {
                        Text("Poin Tidak Cukup", color = DangerRed)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSpeciesToBuy = null }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }

    // Gacha Result Reveal
    gachaResultPet?.let { newPet ->
        AlertDialog(
            onDismissRequest = { gachaResultPet = null },
            containerColor = DarkSurface,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = GoldStar)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TELUR DIDAPATKAN!", fontWeight = FontWeight.ExtraBold, color = GoldStar, fontSize = 18.sp)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.size(110.dp).background(newPet.rarity.color.copy(0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        PetAvatarCanvas(
                            speciesId = newPet.speciesId,
                            stage = PetStage.EGG,
                            rarity = newPet.rarity,
                            modifier = Modifier.size(90.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Telur ${newPet.species.name}", fontWeight = FontWeight.ExtraBold, color = TextPrimary, fontSize = 17.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    RarityBadge(rarity = newPet.rarity)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Waktu menetas: ${newPet.totalHatchSeconds} detik!", color = TextSecondary, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSwapActivePet(newPet.id)
                        gachaResultPet = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text("Jadikan Pet Aktif", color = DeepNavy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { gachaResultPet = null }) {
                    Text("Tutup", color = TextSecondary)
                }
            }
        )
    }

    // Inventory Sheet
    if (showInventorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showInventorySheet = false },
            containerColor = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Inventory2, null, tint = TextPrimary)
                    Text("Koleksi Pet Kamu", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                }
                Text("Pilih 1 pet untuk dijadikan pendamping aktif.", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allPets) { p ->
                        val isActive = p.id == activePet?.id
                        Card(
                            onClick = {
                                onSwapActivePet(p.id)
                                showInventorySheet = false
                            },
                            colors = CardDefaults.cardColors(containerColor = if (isActive) CardSurface else Color(0xFF161F36)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(if (isActive) 2.dp else 1.dp, if (isActive) NeonGreen else p.rarity.color.copy(0.3f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(54.dp).background(p.rarity.color.copy(0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PetAvatarCanvas(
                                        speciesId = p.speciesId,
                                        stage = p.effectiveStage,
                                        rarity = p.rarity,
                                        modifier = Modifier.size(46.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(p.displayName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                                        RarityBadge(rarity = p.rarity)
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    if (!p.isHatched) {
                                        val remSec = p.getRemainingSeconds(currentTime)
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Default.HourglassBottom, null, tint = GoldStar, modifier = Modifier.size(13.dp))
                                            Text("Telur (${remSec}s lagi)", color = GoldStar, fontSize = 11.sp)
                                        }
                                    } else {
                                        Text("Level ${p.level} • ${if (p.level >= 11) "Adult" else "Baby"}", color = NeonGreen, fontSize = 11.sp)
                                    }
                                }
                                if (isActive) {
                                    Box(
                                        modifier = Modifier.background(NeonGreen.copy(0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("AKTIF", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                } else {
                                    Text("Ganti", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Top Bar with Points and Quick Sheet Triggers
 */
@Composable
fun PetTopBar(
    userPoints: Int,
    onOpenGacha: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenInventory: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "My Pet",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                "Sahabat lari & evolusi",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Points Capsule
            Box(
                modifier = Modifier
                    .background(GoldStar.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .border(1.dp, GoldStar.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Stars, null, tint = GoldStar, modifier = Modifier.size(16.dp))
                    Text("$userPoints", color = GoldStar, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }

            // Quick Gacha icon button
            IconButton(
                onClick = onOpenGacha,
                modifier = Modifier.size(38.dp).background(NeonGreen.copy(0.15f), CircleShape)
            ) {
                Icon(Icons.Default.Casino, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
            }

            // Quick Shop icon button
            IconButton(
                onClick = onOpenShop,
                modifier = Modifier.size(38.dp).background(AccentOrange.copy(0.15f), CircleShape)
            ) {
                Icon(Icons.Default.ShoppingCart, null, tint = AccentOrange, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/**
 * Center Spotlight Card: Big Pet Avatar + Nickname + Rename Button
 */
@Composable
fun PetSpotlightSection(
    pet: PetEntity,
    onOpenRename: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.5.dp, pet.rarity.color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Rarity & Stage Capsule
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RarityBadge(rarity = pet.rarity)

                val stageLabel = when {
                    !pet.isHatched -> "State: EGG"
                    pet.level >= 11 -> "Stage: ADULT"
                    else -> "Stage: BABY"
                }

                Box(
                    modifier = Modifier
                        .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(stageLabel, color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Big Spotlight Canvas Avatar
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(pet.rarity.color.copy(alpha = 0.25f), Color.Transparent)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                PetAvatarCanvas(
                    speciesId = pet.speciesId,
                    stage = pet.effectiveStage,
                    rarity = pet.rarity,
                    modifier = Modifier.size(165.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pet Name + Rename Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    pet.displayName,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onOpenRename,
                    modifier = Modifier.size(32.dp).background(TextSecondary.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Rename Pet", tint = TextPrimary, modifier = Modifier.size(16.dp))
                }
            }

            Text(
                pet.species.description,
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * Hatch Countdown Card (Dynamic time-based calculation)
 */
@Composable
fun HatchCountdownCard(pet: PetEntity, currentTime: Long) {
    val remainingSeconds = pet.getRemainingSeconds(currentTime)
    val hatchProgress = pet.getHatchProgress(currentTime)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, GoldStar.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.HourglassBottom, null, tint = GoldStar, modifier = Modifier.size(20.dp))
                    Text("Menetas Otomatis", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                }
                Text(
                    "$remainingSeconds detik lagi",
                    color = GoldStar,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { hatchProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = GoldStar,
                trackColor = TextMuted.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                "Telur sedang dierami secara otomatis. Begitu hitungan mundur selesai, pet akan menetas menjadi BABY.",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun PetLevelExpCard(
    pet: PetEntity,
    userPoints: Int,
    onFeedExp: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.MilitaryTech, null, tint = NeonGreen, modifier = Modifier.size(22.dp))
                        Text("Level ${pet.level}", fontWeight = FontWeight.ExtraBold, color = NeonGreen, fontSize = 20.sp)
                    }
                    Text(
                        if (pet.level >= 11) "Tahap ADULT (Dewasa)" else "Tahap BABY (Capai Lv 11 untuk Adult)",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    "${pet.currentExp} / ${pet.maxExp} EXP",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { pet.expPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = NeonGreen,
                trackColor = TextMuted.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val canFeed = userPoints >= 25
            OutlinedButton(
                onClick = { onFeedExp(25) },
                enabled = canFeed,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (canFeed) GoldStar else TextMuted),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldStar)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Fastfood, null, tint = if (canFeed) GoldStar else TextMuted, modifier = Modifier.size(18.dp))
                    Text(
                        if (canFeed) "Beri Makan (25 Poin = +25 EXP)" else "Poin tidak cukup untuk beri makan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canFeed) GoldStar else TextMuted
                    )
                }
            }
        }
    }
}

/**
 * Pet Gamification & Companion Info (Clean pure gamification, no pay-to-win run effect)
 */
@Composable
fun PetGamificationInfoCard(pet: PetEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Grade, null, tint = pet.rarity.color, modifier = Modifier.size(16.dp))
                    Text("TIER", fontWeight = FontWeight.Bold, color = TextMuted, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(pet.rarity.displayName, fontWeight = FontWeight.ExtraBold, color = pet.rarity.color, fontSize = 16.sp)
                Text("${pet.rarity.hatchSeconds}s hatch", fontSize = 10.sp, color = TextSecondary)
            }

            HorizontalDivider(
                color = TextMuted.copy(alpha = 0.2f),
                modifier = Modifier.height(45.dp).width(1.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Favorite, null, tint = Color(0xFFF43F5E), modifier = Modifier.size(16.dp))
                    Text("LOYALTY", fontWeight = FontWeight.Bold, color = TextMuted, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Companion", fontWeight = FontWeight.ExtraBold, color = NeonGreen, fontSize = 16.sp)
                Text("Gamifikasi Lari", fontSize = 10.sp, color = TextSecondary)
            }

            HorizontalDivider(
                color = TextMuted.copy(alpha = 0.2f),
                modifier = Modifier.height(45.dp).width(1.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.FitnessCenter, null, tint = AccentOrange, modifier = Modifier.size(16.dp))
                    Text("FITUR", fontWeight = FontWeight.Bold, color = TextMuted, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Fair Play", fontWeight = FontWeight.ExtraBold, color = AccentOrange, fontSize = 16.sp)
                Text("100% No P2W", fontSize = 10.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun RarityBadge(rarity: Rarity) {
    Box(
        modifier = Modifier
            .background(rarity.color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            rarity.displayName,
            color = rarity.color,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
