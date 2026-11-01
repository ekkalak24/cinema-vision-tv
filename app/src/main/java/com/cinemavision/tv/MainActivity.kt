package com.cinemavision.tv

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.ui.PlayerView
import com.cinemavision.tv.ui.hud.CalibrationHudOverlay
import com.cinemavision.tv.ui.hud.HudState
import com.cinemavision.tv.ui.pattern.PatternSurfaceView
import com.cinemavision.tv.ui.player.JudderPlayerManager

/**
 * Main Android TV Activity for CinemaVision TV.
 *
 * Coordinates the dual-layer display system:
 * - Layer 0 (Bottom): [PatternSurfaceView] rendering bit-accurate Native 4K Canvas patterns for Steps 1–6.
 * - Layer 1 (Middle): [PlayerView] playing 24p judder video via Media3 ExoPlayer for Step 7.
 * - Layer 2 (Top): [ComposeView] displaying the interactive [CalibrationHudOverlay].
 *
 * Manages full-screen immersive sticky display mode, wake lock flags, remote D-pad key dispatching,
 * and Android TV lifecycle hygiene.
 */
class MainActivity : ComponentActivity() {

    internal lateinit var coordinator: MainActivityCoordinator
    internal lateinit var hudState: HudState
    internal lateinit var judderPlayerManager: JudderPlayerManager
    internal lateinit var patternSurfaceView: PatternSurfaceView
    internal lateinit var playerView: PlayerView
    internal lateinit var composeView: ComposeView

    override fun attachBaseContext(newBase: android.content.Context) {
        val locale = java.util.Locale("th", "TH")
        java.util.Locale.setDefault(locale)
        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Full-Screen Immersive Setup
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        applyImmersiveStickyMode()

        // Initialize state and dependencies
        hudState = HudState()
        judderPlayerManager = JudderPlayerManager()
        judderPlayerManager.initialize(this)

        // 2. Dual-Layer Architecture
        val rootLayout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Layer 0: PatternSurfaceView (Bottom)
        patternSurfaceView = PatternSurfaceView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Layer 1: PlayerView (Middle)
        playerView = PlayerView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            useController = false
            setBackgroundColor(Color.TRANSPARENT)
            setShutterBackgroundColor(Color.TRANSPARENT)
            visibility = View.GONE
        }

        // Layer 2: ComposeView (Top)
        composeView = ComposeView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setContent {
                CalibrationHudOverlay(hudState = hudState)
            }
        }

        rootLayout.addView(patternSurfaceView)
        rootLayout.addView(playerView)
        rootLayout.addView(composeView)
        setContentView(rootLayout)

        // Initialize display coordinator
        coordinator = MainActivityCoordinator(
            hudState = hudState,
            playerManager = judderPlayerManager,
            patternSurfaceView = patternSurfaceView,
            playerView = playerView
        )
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (::coordinator.isInitialized && coordinator.dispatchKeyEvent(event)) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onResume() {
        super.onResume()
        applyImmersiveStickyMode()
        if (::coordinator.isInitialized) {
            coordinator.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::coordinator.isInitialized) {
            coordinator.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::coordinator.isInitialized) {
            coordinator.onDestroy()
        }
    }

    private fun applyImmersiveStickyMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
