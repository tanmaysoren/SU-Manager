package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Responsive scale calculation based on phone resolution, smallest width (swDp), and pixel density.
 * Ensures typography and iconography dynamically scale cleanly across different Android screen sizes.
 */
data class ResponsiveScale(
    val scaleFactor: Float = 1.0f,
    val textScaleFactor: Float = 1.0f,
    val iconScaleFactor: Float = 1.0f,
    val smallestWidthDp: Int = 360,
    val screenWidthDp: Int = 360,
    val screenHeightDp: Int = 640,
    val density: Float = 1.0f
) {
    fun scaleDp(dp: Dp): Dp = (dp.value * scaleFactor).dp
    fun scaleIcon(dp: Dp): Dp = (dp.value * iconScaleFactor).dp
    fun scaleSp(sp: TextUnit): TextUnit = (sp.value * textScaleFactor).sp
}

val LocalResponsiveScale = staticCompositionLocalOf { ResponsiveScale() }

@Composable
fun rememberResponsiveScale(): ResponsiveScale {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density
    val sw = configuration.smallestScreenWidthDp
    val w = configuration.screenWidthDp
    val h = configuration.screenHeightDp

    // Android native DP and SP already scale density-independently based on DPI.
    // Keeping scale factors at 1.0f ensures crisp, standard proportional layout
    // without artificial stretching or text deformation across different screen resolutions.
    val generalScale = 1.0f
    val textScale = 1.0f
    val iconScale = 1.0f

    return ResponsiveScale(
        scaleFactor = generalScale,
        textScaleFactor = textScale,
        iconScaleFactor = iconScale,
        smallestWidthDp = sw,
        screenWidthDp = w,
        screenHeightDp = h,
        density = density
    )
}

// Inline extension helpers for Jetpack Compose UI
val Int.rDp: Dp
    @Composable
    @ReadOnlyComposable
    get() = LocalResponsiveScale.current.scaleDp(this.dp)

val Double.rDp: Dp
    @Composable
    @ReadOnlyComposable
    get() = LocalResponsiveScale.current.scaleDp(this.dp)

val Float.rDp: Dp
    @Composable
    @ReadOnlyComposable
    get() = LocalResponsiveScale.current.scaleDp(this.dp)

val Int.rSp: TextUnit
    @Composable
    @ReadOnlyComposable
    get() = LocalResponsiveScale.current.scaleSp(this.sp)

val Float.rSp: TextUnit
    @Composable
    @ReadOnlyComposable
    get() = LocalResponsiveScale.current.scaleSp(this.sp)

val Int.rIconDp: Dp
    @Composable
    @ReadOnlyComposable
    get() = LocalResponsiveScale.current.scaleIcon(this.dp)
