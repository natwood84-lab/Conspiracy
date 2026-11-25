package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val ArchiveColorScheme = darkColorScheme(
    primary = SignalAmber,
    onPrimary = ArchiveObsidian,
    primaryContainer = SignalAmberContainer,
    onPrimaryContainer = SignalAmberSoft,
    secondary = VerifyTeal,
    onSecondary = ArchiveObsidian,
    secondaryContainer = VerifyTealContainer,
    onSecondaryContainer = VerifyTeal,
    tertiary = CrimsonThread,
    onTertiary = ParchmentCream,
    tertiaryContainer = CrimsonContainer,
    onTertiaryContainer = CrimsonSoft,
    background = ArchiveObsidian,
    onBackground = ParchmentCream,
    surface = ArchiveSlate,
    onSurface = ParchmentCream,
    surfaceVariant = ArchiveCard,
    onSurfaceVariant = ParchmentSecondary,
    surfaceContainerLow = ArchiveSlate,
    surfaceContainer = ArchiveCard,
    surfaceContainerHigh = ArchiveCardElevated,
    outline = ArchiveBorder,
    outlineVariant = ArchiveBorder.copy(alpha = 0.6f),
    error = CrimsonThread,
    onError = ParchmentCream
)

private val ArchiveShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun CipherFeedTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ArchiveColorScheme,
        typography = CipherFeedTypography,
        shapes = ArchiveShapes,
        content = content
    )
}
