
package com.example.smartparking.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ParkwayLightColorScheme = lightColorScheme(
    primary = ParkwayBlack,
    onPrimary = ParkwayWhite,

    secondary = ParkwayLime,
    onSecondary = ParkwayBlack,

    tertiary = ParkwayGray,
    onTertiary = ParkwayWhite,

    background = ParkwayWhite,
    onBackground = ParkwayBlack,

    surface = ParkwayWhite,
    onSurface = ParkwayBlack,

    surfaceVariant = ParkwayLightGray,
    onSurfaceVariant = ParkwayGray,

    outline = ParkwayBorder,
    error = ParkwayError,
    onError = ParkwayWhite
)

private val ParkwayDarkColorScheme = darkColorScheme(
    primary = ParkwayLime,
    onPrimary = ParkwayBlack,

    secondary = ParkwayWhite,
    onSecondary = ParkwayBlack,

    tertiary = ParkwayGray,
    onTertiary = ParkwayWhite,

    background = ParkwayBlack,
    onBackground = ParkwayWhite,

    surface = Color(0xFF151515),
    onSurface = ParkwayWhite,

    surfaceVariant = Color(0xFF242424),
    onSurfaceVariant = Color(0xFFBBBBBB),

    outline = Color(0xFF333333),
    error = ParkwayError,
    onError = ParkwayWhite
)

@Composable
fun SmartParkingTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        ParkwayDarkColorScheme
    } else {
        ParkwayLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
