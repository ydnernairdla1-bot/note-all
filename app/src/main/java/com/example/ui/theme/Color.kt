package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Deep Emerald-Teal & Warm Paper Palette
val DeepTeal = Color(0xFF15504A)
val SoftTealContainer = Color(0xFFD0EBE7)
val OnSoftTealContainer = Color(0xFF082825)

val WarmAmber = Color(0xFFD97706)
val WarmAmberContainer = Color(0xFFFEF3C7)
val OnWarmAmberContainer = Color(0xFF451A03)

val WarmIvoryBackground = Color(0xFFFAF7F2)
val WarmPaperSurface = Color(0xFFFFFFFF)
val WarmSandContainer = Color(0xFFF2EDE4)
val WarmSandHigh = Color(0xFFE8E1D5)
val InkCharcoal = Color(0xFF1C1F1E)
val InkMuted = Color(0xFF5A6360)
val OutlineWarm = Color(0xFFD7CFC2)

// Dark Mode Tokens
val DarkSlateBackground = Color(0xFF111615)
val DarkSlateSurface = Color(0xFF18201E)
val DarkSlateContainer = Color(0xFF222B29)
val DarkSlateHigh = Color(0xFF2C3735)
val TealBright = Color(0xFF7FD4C8)
val DarkTealContainer = Color(0xFF184843)
val WarmSandLight = Color(0xFFECE6DC)
val DarkOutline = Color(0xFF3B4744)

enum class AppThemeMode(val key: String, val label: String) {
    SYSTEM("SYSTEM", "Sistem"),
    LIGHT("LIGHT", "Terang"),
    DARK("DARK", "Gelap");

    companion object {
        fun fromKey(key: String): AppThemeMode =
            entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}

enum class AppThemePreset(
    val key: String,
    val label: String,
    val lightPrimary: Color,
    val lightPrimaryContainer: Color,
    val lightOnPrimaryContainer: Color,
    val lightSecondary: Color,
    val darkPrimary: Color,
    val darkPrimaryContainer: Color,
    val darkOnPrimaryContainer: Color,
    val swatchColor: Color
) {
    EMERALD(
        key = "EMERALD",
        label = "Zamrud",
        lightPrimary = Color(0xFF15504A),
        lightPrimaryContainer = Color(0xFFD0EBE7),
        lightOnPrimaryContainer = Color(0xFF082825),
        lightSecondary = Color(0xFFD97706),
        darkPrimary = Color(0xFF7FD4C8),
        darkPrimaryContainer = Color(0xFF184843),
        darkOnPrimaryContainer = Color(0xFFB3EAE2),
        swatchColor = Color(0xFF15504A)
    ),
    INDIGO(
        key = "INDIGO",
        label = "Samudra",
        lightPrimary = Color(0xFF1E40AF),
        lightPrimaryContainer = Color(0xFFDBEAFE),
        lightOnPrimaryContainer = Color(0xFF172554),
        lightSecondary = Color(0xFF0284C7),
        darkPrimary = Color(0xFF93C5FD),
        darkPrimaryContainer = Color(0xFF1E3A8A),
        darkOnPrimaryContainer = Color(0xFFDBEAFE),
        swatchColor = Color(0xFF1E40AF)
    ),
    TERRACOTTA(
        key = "TERRACOTTA",
        label = "Terakota",
        lightPrimary = Color(0xFF9A3412),
        lightPrimaryContainer = Color(0xFFFFEDD5),
        lightOnPrimaryContainer = Color(0xFF431407),
        lightSecondary = Color(0xFFD97706),
        darkPrimary = Color(0xFFFDBA74),
        darkPrimaryContainer = Color(0xFF7C2D12),
        darkOnPrimaryContainer = Color(0xFFFFEDD5),
        swatchColor = Color(0xFF9A3412)
    ),
    BOTANICAL(
        key = "BOTANICAL",
        label = "Hutan",
        lightPrimary = Color(0xFF2D5A27),
        lightPrimaryContainer = Color(0xFFDCFCE7),
        lightOnPrimaryContainer = Color(0xFF143512),
        lightSecondary = Color(0xFF65A30D),
        darkPrimary = Color(0xFF86EFAC),
        darkPrimaryContainer = Color(0xFF1B4332),
        darkOnPrimaryContainer = Color(0xFFDCFCE7),
        swatchColor = Color(0xFF2D5A27)
    ),
    PLUM(
        key = "PLUM",
        label = "Lembayung",
        lightPrimary = Color(0xFF6B21A8),
        lightPrimaryContainer = Color(0xFFF3E8FF),
        lightOnPrimaryContainer = Color(0xFF3B0764),
        lightSecondary = Color(0xFFDB2777),
        darkPrimary = Color(0xFFD8B4FE),
        darkPrimaryContainer = Color(0xFF581C87),
        darkOnPrimaryContainer = Color(0xFFF3E8FF),
        swatchColor = Color(0xFF6B21A8)
    );

    companion object {
        fun fromKey(key: String): AppThemePreset =
            entries.firstOrNull { it.key == key } ?: EMERALD
    }
}

enum class BackgroundMode(val key: String, val label: String) {
    DEFAULT("DEFAULT", "Default"),
    GRADIENT("GRADIENT", "Gradasi"),
    SOLID("SOLID", "Warna"),
    IMAGE("IMAGE", "Foto Galeri");

    companion object {
        fun fromKey(key: String): BackgroundMode =
            entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}

enum class BackgroundGradientPreset(
    val key: String,
    val label: String,
    val colors: List<Color>,
    val isDarkBackground: Boolean = false
) {
    WARM_PAPER(
        key = "WARM_PAPER",
        label = "Krem Hangat",
        colors = listOf(Color(0xFFFAF7F2), Color(0xFFF3E7D3), Color(0xFFEBE4D8))
    ),
    EMERALD_MIST(
        key = "EMERALD_MIST",
        label = "Embun Zamrud",
        colors = listOf(Color(0xFFE8F5F1), Color(0xFFD4ECE5), Color(0xFFF4F8F6))
    ),
    SUNSET_DUSK(
        key = "SUNSET_DUSK",
        label = "Senja Lembut",
        colors = listOf(Color(0xFFFFF1E6), Color(0xFFFDE2E4), Color(0xFFEDE7F6))
    ),
    OCEAN_BREEZE(
        key = "OCEAN_BREEZE",
        label = "Angin Laut",
        colors = listOf(Color(0xFFE6F2FA), Color(0xFFD7E9F7), Color(0xFFF0F7FA))
    ),
    NIGHT_AURORA(
        key = "NIGHT_AURORA",
        label = "Malam Aurora",
        colors = listOf(Color(0xFF0F172A), Color(0xFF132A2E), Color(0xFF1E1B4B)),
        isDarkBackground = true
    );

    companion object {
        fun fromKey(key: String): BackgroundGradientPreset =
            entries.firstOrNull { it.key == key } ?: WARM_PAPER
    }
}

enum class BackgroundSolidPreset(
    val key: String,
    val label: String,
    val color: Color,
    val isDarkBackground: Boolean = false
) {
    IVORY("IVORY", "Gading", Color(0xFFFAF7F2)),
    WARM_SAND("WARM_SAND", "Pasir", Color(0xFFF1E8D9)),
    MINT_SOFT("MINT_SOFT", "Mint", Color(0xFFE6F3EC)),
    SKY_SOFT("SKY_SOFT", "Langit", Color(0xFFE7F0FA)),
    BLUSH_SOFT("BLUSH_SOFT", "Mawar", Color(0xFFFAEBEE)),
    LAVENDER_SOFT("LAVENDER_SOFT", "Lavender", Color(0xFFF0EAF8)),
    SLATE_DARK("SLATE_DARK", "Arang", Color(0xFF181C1B), isDarkBackground = true),
    MIDNIGHT_BLUE("MIDNIGHT_BLUE", "Malam", Color(0xFF121A2B), isDarkBackground = true);

    companion object {
        fun fromKey(key: String): BackgroundSolidPreset =
            entries.firstOrNull { it.key == key } ?: IVORY
    }
}

enum class NotePaperColor(
    val key: String,
    val label: String,
    val lightBg: Color,
    val darkBg: Color,
    val accentDot: Color
) {
    DEFAULT(
        key = "DEFAULT",
        label = "Kertas",
        lightBg = Color(0xFFFFFFFF),
        darkBg = Color(0xFF1C2422),
        accentDot = Color(0xFFE5DFD5)
    ),
    CREAM(
        key = "CREAM",
        label = "Krem",
        lightBg = Color(0xFFFFF6DC),
        darkBg = Color(0xFF332C1A),
        accentDot = Color(0xFFF4D57D)
    ),
    SAGE(
        key = "SAGE",
        label = "Sage",
        lightBg = Color(0xFFE8F3EA),
        darkBg = Color(0xFF1B2E22),
        accentDot = Color(0xFF9CD0A8)
    ),
    SKY(
        key = "SKY",
        label = "Langit",
        lightBg = Color(0xFFE7F1FA),
        darkBg = Color(0xFF1A2938),
        accentDot = Color(0xFF9AC5EE)
    ),
    CORAL(
        key = "CORAL",
        label = "Koral",
        lightBg = Color(0xFFFDECE8),
        darkBg = Color(0xFF38201C),
        accentDot = Color(0xFFF3ABA0)
    ),
    LAVENDER(
        key = "LAVENDER",
        label = "Lavender",
        lightBg = Color(0xFFF1EBFA),
        darkBg = Color(0xFF292138),
        accentDot = Color(0xFFC7B2EA)
    );

    companion object {
        fun fromKey(key: String): NotePaperColor =
            entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}
