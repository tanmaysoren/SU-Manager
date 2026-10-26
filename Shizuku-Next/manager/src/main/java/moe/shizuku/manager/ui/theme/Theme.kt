package moe.shizuku.manager.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import moe.shizuku.manager.utils.EnvironmentUtils

/**
 * GPS Mocker cyber palette. A pure black page, graphite surfaces and white-on-black
 * accents - the same palette the mocker embedded in SU-Manager ships (its
 * ui/theme/Color.kt / Theme.kt), so the two tools read as one product.
 */
private val CyberColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1E1E1E),
    onPrimaryContainer = Color(0xFFFFFFFF),
    inversePrimary = Color(0xFF1E1E1E),
    secondary = Color(0xFFE0E0E0),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF1E1E1E),
    onSecondaryContainer = Color(0xFFE0E0E0),
    tertiary = Color(0xFFA0A0A0),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF1E1E1E),
    onTertiaryContainer = Color(0xFFA0A0A0),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFAFAFA),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFFAFAFA),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFF888888),
    surfaceDim = Color(0xFF0D0D0D),
    surfaceBright = Color(0xFF1E1E1E),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF121212),
    surfaceContainer = Color(0xFF161616),
    surfaceContainerHigh = Color(0xFF1A1A1A),
    surfaceContainerHighest = Color(0xFF1E1E1E),
    outline = Color(0xFF333333),
    outlineVariant = Color(0xFF2A2A2A),
    error = Color(0xFFE53935),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF2B0E0E),
    onErrorContainer = Color(0xFFE53935),
)

/**
 * True while the pure black (AMOLED) scheme is in use. Cards and the page are the
 * same colour in that scheme, so surfaces need something else to stay visible see
 * [moe.shizuku.manager.ui.component.SegmentedColumn].
 */
val LocalAmoledTheme = staticCompositionLocalOf { false }

/**
 * True on a television. A TV is driven by a remote's D-pad rather than a finger, so the few
 * places that lay themselves out for a thumb - the floating tab bar above all - read this and
 * do something a remote can reach instead. Everything else is the phone layout as it is.
 */
val LocalTelevision = staticCompositionLocalOf { false }

/**
 * The state layers on a TV. With a remote, focus is the only cursor there is, and the stock
 * focused layer - a tenth of the content colour - is close to invisible from a sofa: on a
 * black page the focused button was a shade of grey away from the rest. Pressed and dragged
 * stay as they are; only focus is raised to something that can be seen across a room.
 */
private val TelevisionRipple = RippleConfiguration(
    rippleAlpha = RippleAlpha(
        draggedAlpha = 0.16f,
        focusedAlpha = 0.32f,
        hoveredAlpha = 0.08f,
        pressedAlpha = 0.10f
    )
)

/** Bumped when a theme preference changes so the theme re-reads the prefs. */
object ThemeState {
    var version by mutableIntStateOf(0)
        private set

    fun refresh() {
        version++
    }
}

@Composable
fun ShizukuTheme(content: @Composable () -> Unit) {
    // The theme is fixed - always the dark cyber scheme, exactly like the mocker's own
    // composable, which ignores the system setting and paints black full time.
    val darkTheme = true

    // Match the status/navigation bar icons to the app's theme, not the system's;
    // on a black page the bars need light icons.
    val view = LocalView.current
    if (!view.isInEditMode) {
        LaunchedEffect(darkTheme) {
            val window = view.context.findActivity()?.window ?: return@LaunchedEffect
            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val television = remember { EnvironmentUtils.isTelevision() }

    CompositionLocalProvider(
        LocalAmoledTheme provides false,
        LocalTelevision provides television,
        LocalRippleConfiguration provides
            if (television) TelevisionRipple else LocalRippleConfiguration.current
    ) {
        MaterialTheme(
            colorScheme = CyberColorScheme,
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    }
}

private fun Context.findActivity(): Activity? {
    var context: Context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return context as? Activity
}
