package com.example.pelarikalcer.data.local.entity

import androidx.compose.ui.graphics.Color

/**
 * Pet Rarity Tier
 * Hatch time: Common = 5 detik, +10 detik untuk tiap tier berikutnya:
 * - Common: 5 detik
 * - Uncommon: 15 detik
 * - Rare: 25 detik
 * - Epic: 35 detik
 * - Legendary: 45 detik
 */
enum class Rarity(
    val displayName: String,
    val hatchSeconds: Int,
    val directCostPoints: Int,
    val badgeColorHex: Long,
    val color: Color
) {
    COMMON(
        displayName = "Common",
        hatchSeconds = 5,
        directCostPoints = 150,
        badgeColorHex = 0xFFA8A29E,
        color = Color(0xFFA8A29E) // Abu-abu / coklat muda
    ),
    UNCOMMON(
        displayName = "Uncommon",
        hatchSeconds = 15,
        directCostPoints = 350,
        badgeColorHex = 0xFF4ADE80,
        color = Color(0xFF4ADE80) // Hijau
    ),
    RARE(
        displayName = "Rare",
        hatchSeconds = 25,
        directCostPoints = 750,
        badgeColorHex = 0xFF38BDF8,
        color = Color(0xFF38BDF8) // Biru
    ),
    EPIC(
        displayName = "Epic",
        hatchSeconds = 35,
        directCostPoints = 1500,
        badgeColorHex = 0xFFA855F7,
        color = Color(0xFFA855F7) // Ungu
    ),
    LEGENDARY(
        displayName = "Legendary",
        hatchSeconds = 45,
        directCostPoints = 3000,
        badgeColorHex = 0xFFF59E0B,
        color = Color(0xFFFFB703) // Emas / oranye menyala
    )
}

/**
 * Pet Stage Evolution:
 * EGG (Telur) -> BABY (Level 1-10) -> ADULT (Level 11+)
 */
enum class PetStage(val displayName: String) {
    EGG("Telur"),
    BABY("Baby (Lv 1-10)"),
    ADULT("Adult (Lv 11+)");

    companion object {
        fun fromLevel(isHatched: Boolean, level: Int): PetStage {
            if (!isHatched) return EGG
            return if (level >= 11) ADULT else BABY
        }
    }
}

/**
 * Pet Species definition / catalog
 */
data class PetSpecies(
    val id: Int,
    val name: String,
    val description: String,
    val rarity: Rarity,
    val eggColor: Color,
    val primaryColor: Color,
    val speedBase: Int = 10,
    val staminaBase: Int = 10
)

val defaultSpeciesCatalog: List<PetSpecies> = listOf(
    // Common (5s hatch)
    PetSpecies(
        id = 1,
        name = "Bunny Hop",
        description = "Kelinci lincah bertubuh bulat, melompat riang dan mengibaskan telinganya!",
        rarity = Rarity.COMMON,
        eggColor = Color(0xFFD7CCC8),
        primaryColor = Color(0xFFFFB6C1),
        speedBase = 12,
        staminaBase = 8
    ),
    PetSpecies(
        id = 2,
        name = "Pipit Piko",
        description = "Burung mungil dengan sayap lincah yang selalu mengepak gembira di sampingmu!",
        rarity = Rarity.COMMON,
        eggColor = Color(0xFFCFD8DC),
        primaryColor = Color(0xFFA1887F),
        speedBase = 10,
        staminaBase = 10
    ),

    // Uncommon (15s hatch)
    PetSpecies(
        id = 3,
        name = "Kucing Ninja",
        description = "Kucing ninja gesit dengan ekor lentur yang selalu sigap berlatih jurus!",
        rarity = Rarity.UNCOMMON,
        eggColor = Color(0xFFA7F3D0),
        primaryColor = Color(0xFF34D399),
        speedBase = 18,
        staminaBase = 14
    ),
    PetSpecies(
        id = 4,
        name = "Froggo Jump",
        description = "Katak sporty kaki panjang yang gemar melompat tinggi memecahkan rekor!",
        rarity = Rarity.UNCOMMON,
        eggColor = Color(0xFF86EFAC),
        primaryColor = Color(0xFF22C55E),
        speedBase = 14,
        staminaBase = 18
    ),

    // Rare (25s hatch)
    PetSpecies(
        id = 5,
        name = "Rubah Bara",
        description = "Rubah berekor api lebat yang berkobar dinamis penuh kehangatan!",
        rarity = Rarity.RARE,
        eggColor = Color(0xFFBAE6FD),
        primaryColor = Color(0xFF0284C7),
        speedBase = 28,
        staminaBase = 24
    ),
    PetSpecies(
        id = 6,
        name = "Penguin Jet",
        description = "Penguin peselancar dengan sayap dan kaki yang berayun lincah di salju!",
        rarity = Rarity.RARE,
        eggColor = Color(0xFF7DD3FC),
        primaryColor = Color(0xFF0EA5E9),
        speedBase = 26,
        staminaBase = 26
    ),

    // Epic (35s hatch)
    PetSpecies(
        id = 7,
        name = "Garuda Emas",
        description = "Burung elang agung bersayap lebar yang mengepak gagah menembus badai!",
        rarity = Rarity.EPIC,
        eggColor = Color(0xFFE9D5FF),
        primaryColor = Color(0xFFA855F7),
        speedBase = 42,
        staminaBase = 38
    ),
    PetSpecies(
        id = 8,
        name = "Serigala Bayang",
        description = "Serigala malam berekor lebat dan bermata kosmik yang melolong berwibawa!",
        rarity = Rarity.EPIC,
        eggColor = Color(0xFFDDD6FE),
        primaryColor = Color(0xFF9333EA),
        speedBase = 40,
        staminaBase = 40
    ),

    // Legendary (45s hatch)
    PetSpecies(
        id = 9,
        name = "Naga Asteroid",
        description = "Naga kosmik bersayap agung dan bermahkota tanduk emas penjaga galaksi!",
        rarity = Rarity.LEGENDARY,
        eggColor = Color(0xFFFDE68A),
        primaryColor = Color(0xFFF59E0B),
        speedBase = 65,
        staminaBase = 60
    ),
    PetSpecies(
        id = 10,
        name = "Unicorn Surya",
        description = "Kuda tanduk emas murni dengan surai berkilau pelangi yang melangkah anggun!",
        rarity = Rarity.LEGENDARY,
        eggColor = Color(0xFFFEF08A),
        primaryColor = Color(0xFFFFB703),
        speedBase = 62,
        staminaBase = 64
    )
)

/**
 * Gacha Configuration & Rate Tables
 */
object GachaConfig {
    const val GACHA_COST_POINTS = 100 // Gacha cost
    const val EXP_PER_KM = 25 // 1 km = 25 EXP
    const val EXP_PER_LEVEL = 100 // 100 EXP to level up

    // Drop rates (Total 100%)
    val DROP_RATES: Map<Rarity, Double> = mapOf(
        Rarity.COMMON to 50.0,     // 50%
        Rarity.UNCOMMON to 30.0,   // 30%
        Rarity.RARE to 15.0,       // 15%
        Rarity.EPIC to 4.0,        // 4%
        Rarity.LEGENDARY to 1.0    // 1%
    )

    fun rollRarity(): Rarity {
        val rand = Math.random() * 100.0
        var cumulative = 0.0
        for ((rarity, weight) in DROP_RATES) {
            cumulative += weight
            if (rand <= cumulative) {
                return rarity
            }
        }
        return Rarity.COMMON
    }

    fun rollSpecies(): PetSpecies {
        val rarity = rollRarity()
        val pool = defaultSpeciesCatalog.filter { it.rarity == rarity }
        return if (pool.isNotEmpty()) pool.random() else defaultSpeciesCatalog.first()
    }
}
