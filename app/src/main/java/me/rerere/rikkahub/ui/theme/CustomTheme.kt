package me.rerere.rikkahub.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

private fun Color.toHsvValues(): Triple<Float, Float, Float> {
    val r = red
    val g = green
    val b = blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    val hue = when {
        delta == 0f -> 0f
        max == r -> 60f * (((g - b) / delta) % 6f)
        max == g -> 60f * (((b - r) / delta) + 2f)
        else -> 60f * (((r - g) / delta) + 4f)
    }.let { if (it < 0f) it + 360f else if (it >= 360f) it - 360f else it }

    val saturation = if (max == 0f) 0f else delta / max
    val value = max

    return Triple(hue, saturation, value)
}

/**
 * Creates a light tonal palette from a seed color.
 */
private fun generateLightScheme(seed: Color): ColorScheme {
    val (baseHue, baseSat, baseVal) = seed.toHsvValues()

    fun deriveColor(hueShift: Float, satScale: Float, valScale: Float): Color {
        return Color.hsv(
            hue = (baseHue + hueShift).mod(360f),
            saturation = (baseSat * satScale).coerceIn(0f, 1f),
            value = (baseVal * valScale).coerceIn(0f, 1f)
        )
    }

    val primary = seed
    val onPrimary = Color.White
    val primaryContainer = deriveColor(0f, 0.4f, 1.3f)
    val onPrimaryContainer = deriveColor(0f, 1.0f, 0.4f)

    val secondary = deriveColor(30f, 0.6f, 0.9f)
    val onSecondary = Color.White
    val secondaryContainer = deriveColor(30f, 0.3f, 1.2f)
    val onSecondaryContainer = deriveColor(30f, 1.0f, 0.35f)

    val tertiary = deriveColor(60f, 0.7f, 0.85f)
    val onTertiary = Color.White
    val tertiaryContainer = deriveColor(60f, 0.35f, 1.15f)
    val onTertiaryContainer = deriveColor(60f, 1.0f, 0.3f)

    val background = Color(0xFFFBFCF8)
    val onBackground = Color(0xFF1A1C19)
    val surface = Color(0xFFFBFCF8)
    val onSurface = Color(0xFF1A1C19)
    val surfaceVariant = Color(0xFFE0E3DC)
    val onSurfaceVariant = Color(0xFF43483E)
    val surfaceTint = primary
    val inverseSurface = Color(0xFF2F312D)
    val inverseOnSurface = Color(0xFFF1F1EC)
    val inversePrimary = deriveColor(0f, 0.6f, 1.1f)

    val error = Color(0xFFBA1A1A)
    val onError = Color(0xFFFFFFFF)
    val errorContainer = Color(0xFFFFDAD6)
    val onErrorContainer = Color(0xFF410002)

    val outline = Color(0xFF73786E)
    val outlineVariant = Color(0xFFC3C8BC)
    val scrim = Color(0xFF000000)

    val surfaceBright = Color(0xFFFBFCF8)
    val surfaceDim = Color(0xFFDBDDD8)
    val surfaceContainer = Color(0xFFEFF1EB)
    val surfaceContainerHigh = Color(0xFFE9EBE5)
    val surfaceContainerHighest = Color(0xFFE3E5E0)
    val surfaceContainerLow = Color(0xFFF5F7F1)
    val surfaceContainerLowest = Color(0xFFFFFFFF)

    val primaryFixed = primaryContainer
    val primaryFixedDim = deriveColor(0f, 0.25f, 1.0f)
    val onPrimaryFixed = onPrimaryContainer
    val onPrimaryFixedVariant = deriveColor(0f, 1.0f, 0.3f)

    val secondaryFixed = secondaryContainer
    val secondaryFixedDim = deriveColor(30f, 0.2f, 1.0f)
    val onSecondaryFixed = onSecondaryContainer
    val onSecondaryFixedVariant = deriveColor(30f, 1.0f, 0.3f)

    val tertiaryFixed = tertiaryContainer
    val tertiaryFixedDim = deriveColor(60f, 0.2f, 1.0f)
    val onTertiaryFixed = onTertiaryContainer
    val onTertiaryFixedVariant = deriveColor(60f, 1.0f, 0.25f)

    return lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = inversePrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = surfaceTint,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        outline = outline,
        outlineVariant = outlineVariant,
        scrim = scrim,
        surfaceBright = surfaceBright,
        surfaceDim = surfaceDim,
        surfaceContainer = surfaceContainer,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = surfaceContainerHighest,
        surfaceContainerLow = surfaceContainerLow,
        surfaceContainerLowest = surfaceContainerLowest,
        primaryFixed = primaryFixed,
        primaryFixedDim = primaryFixedDim,
        onPrimaryFixed = onPrimaryFixed,
        onPrimaryFixedVariant = onPrimaryFixedVariant,
        secondaryFixed = secondaryFixed,
        secondaryFixedDim = secondaryFixedDim,
        onSecondaryFixed = onSecondaryFixed,
        onSecondaryFixedVariant = onSecondaryFixedVariant,
        tertiaryFixed = tertiaryFixed,
        tertiaryFixedDim = tertiaryFixedDim,
        onTertiaryFixed = onTertiaryFixed,
        onTertiaryFixedVariant = onTertiaryFixedVariant,
    )
}

/**
 * Creates a dark tonal palette from a seed color.
 */
private fun generateDarkScheme(seed: Color): ColorScheme {
    val (baseHue, baseSat, baseVal) = seed.toHsvValues()

    fun deriveColor(hueShift: Float, satScale: Float, valScale: Float): Color {
        return Color.hsv(
            hue = (baseHue + hueShift).mod(360f),
            saturation = (baseSat * satScale).coerceIn(0f, 1f),
            value = (baseVal * valScale).coerceIn(0f, 1f)
        )
    }

    val primary = deriveColor(0f, 0.7f, 0.95f)
    val onPrimary = deriveColor(0f, 1.0f, 0.25f)
    val primaryContainer = deriveColor(0f, 0.8f, 0.4f)
    val onPrimaryContainer = deriveColor(0f, 0.4f, 1.2f)

    val secondary = deriveColor(30f, 0.5f, 0.85f)
    val onSecondary = deriveColor(30f, 1.0f, 0.2f)
    val secondaryContainer = deriveColor(30f, 0.6f, 0.35f)
    val onSecondaryContainer = deriveColor(30f, 0.3f, 1.15f)

    val tertiary = deriveColor(60f, 0.6f, 0.8f)
    val onTertiary = deriveColor(60f, 1.0f, 0.2f)
    val tertiaryContainer = deriveColor(60f, 0.7f, 0.3f)
    val onTertiaryContainer = deriveColor(60f, 0.35f, 1.1f)

    val background = Color(0xFF1A1C19)
    val onBackground = Color(0xFFE2E3DD)
    val surface = Color(0xFF1A1C19)
    val onSurface = Color(0xFFE2E3DD)
    val surfaceVariant = Color(0xFF43483E)
    val onSurfaceVariant = Color(0xFFC3C8BC)
    val surfaceTint = primary
    val inverseSurface = Color(0xFFE2E3DD)
    val inverseOnSurface = Color(0xFF1A1C19)
    val inversePrimary = deriveColor(0f, 0.5f, 0.5f)

    val error = Color(0xFFFFB4AB)
    val onError = Color(0xFF690005)
    val errorContainer = Color(0xFF93000A)
    val onErrorContainer = Color(0xFFFFDAD6)

    val outline = Color(0xFF8D9287)
    val outlineVariant = Color(0xFF43483E)
    val scrim = Color(0xFF000000)

    val surfaceBright = Color(0xFF3F413D)
    val surfaceDim = Color(0xFF1A1C19)
    val surfaceContainer = Color(0xFF272924)
    val surfaceContainerHigh = Color(0xFF32342F)
    val surfaceContainerHighest = Color(0xFF3D3F3A)
    val surfaceContainerLow = Color(0xFF1F211D)
    val surfaceContainerLowest = Color(0xFF141613)

    val primaryFixed = deriveColor(0f, 0.4f, 1.2f)
    val primaryFixedDim = deriveColor(0f, 0.3f, 1.0f)
    val onPrimaryFixed = deriveColor(0f, 1.0f, 0.2f)
    val onPrimaryFixedVariant = deriveColor(0f, 0.8f, 0.35f)

    val secondaryFixed = deriveColor(30f, 0.3f, 1.15f)
    val secondaryFixedDim = deriveColor(30f, 0.2f, 0.95f)
    val onSecondaryFixed = deriveColor(30f, 1.0f, 0.2f)
    val onSecondaryFixedVariant = deriveColor(30f, 0.6f, 0.3f)

    val tertiaryFixed = deriveColor(60f, 0.35f, 1.1f)
    val tertiaryFixedDim = deriveColor(60f, 0.25f, 0.95f)
    val onTertiaryFixed = deriveColor(60f, 1.0f, 0.2f)
    val onTertiaryFixedVariant = deriveColor(60f, 0.7f, 0.3f)

    return darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = inversePrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = surfaceTint,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        outline = outline,
        outlineVariant = outlineVariant,
        scrim = scrim,
        surfaceBright = surfaceBright,
        surfaceDim = surfaceDim,
        surfaceContainer = surfaceContainer,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = surfaceContainerHighest,
        surfaceContainerLow = surfaceContainerLow,
        surfaceContainerLowest = surfaceContainerLowest,
        primaryFixed = primaryFixed,
        primaryFixedDim = primaryFixedDim,
        onPrimaryFixed = onPrimaryFixed,
        onPrimaryFixedVariant = onPrimaryFixedVariant,
        secondaryFixed = secondaryFixed,
        secondaryFixedDim = secondaryFixedDim,
        onSecondaryFixed = onSecondaryFixed,
        onSecondaryFixedVariant = onSecondaryFixedVariant,
        tertiaryFixed = tertiaryFixed,
        tertiaryFixedDim = tertiaryFixedDim,
        onTertiaryFixed = onTertiaryFixed,
        onTertiaryFixedVariant = onTertiaryFixedVariant,
    )
}

@Serializable
data class CustomTheme(
    val id: String = Uuid.random().toString(),
    val name: String = "",
    val primaryColorArgb: Long = 0xFF6750A4,
    val secondaryColorArgb: Long? = null,
    val tertiaryColorArgb: Long? = null,
) {
    fun generateColorScheme(dark: Boolean): ColorScheme {
        val seedColor = Color(primaryColorArgb.toInt())
        return if (dark) generateDarkScheme(seedColor)
        else generateLightScheme(seedColor)
    }
}
