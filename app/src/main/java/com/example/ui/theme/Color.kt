package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =======================================================================
// HAATHSE ("Made by Hand") — INDIAN CRAFTSMANSHIP DESIGN PALETTE
// =======================================================================

// --- Primary Indian Craft Colors ---
val HaathSeTerracotta = Color(0xFFB83A1B)          // Rich baked terracotta clay
val HaathSeTerracottaDark = Color(0xFF7C230C)      // Deep terracotta
val HaathSeTerracottaLight = Color(0xFFFBECE8)     // Soft terracotta tint
val HaathSeTerracottaSurface = Color(0xFFF6D8D0)

val HaathSeSaffron = Color(0xFFE65100)             // Deep sacred saffron
val HaathSeSaffronLight = Color(0xFFFFF3E0)

val HaathSeMarigold = Color(0xFFF59E0B)            // Festive golden marigold
val HaathSeMarigoldDark = Color(0xFFB45309)
val HaathSeMarigoldLight = Color(0xFFFEF3C7)

val HaathSeIndigo = Color(0xFF1E293B)              // Authentic Ajrakh natural indigo
val HaathSeIndigoDeep = Color(0xFF0F172A)
val HaathSeIndigoContainer = Color(0xFF2E384D)
val HaathSeIndigoLight = Color(0xFFEEF2F6)

val HaathSePeacock = Color(0xFF0284C7)             // Vibrant peacock blue
val HaathSePeacockTeal = Color(0xFF0D9488)         // Rajasthan meenakari teal
val HaathSePeacockLight = Color(0xFFE0F2FE)

val HaathSeForestGreen = Color(0xFF14532D)         // Natural plant dyes deep green
val HaathSeGreenSuccess = Color(0xFF15803D)
val HaathSeGreenLight = Color(0xFFDCFCE7)

val HaathSeBurgundy = Color(0xFF881337)            // Royal Mughal crimson/burgundy
val HaathSeBurgundyLight = Color(0xFFFFE4E6)

val HaathSeGold = Color(0xFFD97706)                // Antique Zari gold
val HaathSeGoldBright = Color(0xFFFBBF24)
val HaathSeGoldLight = Color(0xFFFEF9C3)

// --- Warm Neutral Canvas & Card Surfaces ---
val HaathSeCreamBg = Color(0xFFFAF7F2)             // Handloom raw silk / unbleached cotton
val HaathSeSand = Color(0xFFF4ECE1)                // Desert sand / potter's slip
val HaathSeWhiteSurface = Color(0xFFFFFFFF)
val HaathSeCardBorder = Color(0xFFE8DFD3)
val HaathSeBorderLight = HaathSeCardBorder

// --- Rich Artisan Brown Theme Card Palette (High Contrast Earth Terracotta) ---
val ArtisanCardBrown = Color(0xFF382017)           // Warm rich earthy brown card
val ArtisanCardBrownDark = Color(0xFF26140E)       // Deep clay espresso
val ArtisanCardBrownElevated = Color(0xFF4A2B20)   // Elevated terracotta brown
val ArtisanCardTextPrimary = Color(0xFFFFF7F2)     // Bright warm cream white
val ArtisanCardTextSecondary = Color(0xFFE8D4C8)   // Muted cream subtitle
val ArtisanCardBorder = Color(0xFF5E392B)          // Warm craft border
val ArtisanCardAccentGold = Color(0xFFFFD166)      // Festive gold accent
val ArtisanCardAccentGreen = Color(0xFF86EFAC)     // Fresh herbal green accent

val HaathSeTextPrimary = Color(0xFF1C1917)          // Warm charcoal
val HaathSeTextSecondary = Color(0xFF57534E)        // Earthy gray
val HaathSeTextTertiary = Color(0xFF78716C)         // Subdued clay gray

// --- Dark Craft Mode Surfaces ---
val HaathSeDarkBg = Color(0xFF161412)              // Charcoal woodblock black
val HaathSeDarkCard = Color(0xFF221E1B)            // Warm night studio
val HaathSeDarkElevated = Color(0xFF2E2824)        // Elevated night surface
val HaathSeDarkBorder = Color(0xFF3E3630)
val HaathSeDarkTextPrimary = Color(0xFFF5EFEA)
val HaathSeDarkTextSecondary = Color(0xFFD4C7BC)
val HaathSeDarkTextTertiary = Color(0xFFA8998D)

// --- Brand Gradients ---
val TerracottaGradient = Brush.horizontalGradient(
    colors = listOf(HaathSeTerracotta, Color(0xFFD9532F))
)

val SaffronGoldGradient = Brush.horizontalGradient(
    colors = listOf(HaathSeSaffron, HaathSeMarigold)
)

val IndigoPeacockGradient = Brush.horizontalGradient(
    colors = listOf(HaathSeIndigo, HaathSePeacockTeal)
)

val GoldenZariGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFD97706), Color(0xFFFBBF24), Color(0xFFD97706))
)

// Legacy alias compatibility so all components continue compiling smoothly
val TerracottaPrimary = HaathSeTerracotta
val TerracottaDark = HaathSeTerracottaDark
val TerracottaLight = HaathSeTerracottaLight
val GoldenAmberSecondary = HaathSeMarigold
val GoldenAmberLight = HaathSeMarigoldLight
val SandGoldSecondary = HaathSeGold
val PeacockTealTertiary = HaathSePeacockTeal
val PeacockTealLight = HaathSePeacockLight
val DeepOrangeAccent = HaathSeSaffron
val RoyalBurgundy = HaathSeBurgundy
val WarmBgLight = HaathSeCreamBg
val SurfaceCard = HaathSeWhiteSurface
val SurfaceVariantWarm = HaathSeSand
val TextPrimaryDark = HaathSeTextPrimary
val TextSecondaryMuted = HaathSeTextSecondary
val TextTertiaryMuted = HaathSeTextTertiary
val SuccessGreen = HaathSeGreenSuccess
val SuccessGreenBg = HaathSeGreenLight
val WarningAmber = HaathSeMarigold
val WarningAmberBg = HaathSeMarigoldLight
val VerifiedBadgeBlue = HaathSePeacock
val VerifiedBadgeBg = HaathSePeacockLight
val DarkCanvasBg = HaathSeDarkBg
val DarkSurfaceCard = HaathSeDarkCard
val DarkSurfaceCardElevated = HaathSeDarkElevated
val DarkSurfaceVariant = HaathSeDarkElevated
val DarkBorderOutline = HaathSeDarkBorder
val DarkBg = HaathSeDarkBg
val DarkSurface = HaathSeDarkCard
val WarmOffWhiteCanvas = HaathSeCreamBg
val DeepCharcoalSurface = HaathSeTextPrimary
val IndigoBlue = HaathSeIndigo
val IndigoBlueBg = HaathSeIndigoLight
val WarmBorderBeige = HaathSeCardBorder
