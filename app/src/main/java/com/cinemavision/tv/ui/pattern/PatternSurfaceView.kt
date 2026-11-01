package com.cinemavision.tv.ui.pattern

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import com.cinemavision.tv.domain.ColorRange
import com.cinemavision.tv.domain.PatternStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Custom SurfaceView dedicated to bit-accurate, unscaled pattern generation.
 * Enforces Native 4K (3840x2160) buffer size when display hardware supports 4K,
 * or 1080p (1920x1080) buffer fallback.
 *
 * Runs an asynchronous rendering coroutine on [Dispatchers.Default] rendering at display
 * refresh rate with a 1 Hz flashing state cycle (500ms toggle) for PLUGE and White Clipping bars.
 */
class PatternSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    val targetWidth: Int
    val targetHeight: Int

    var currentStep: PatternStep = PatternStep.BLACK_LEVEL_PLUGE
        set(value) {
            field = value
            requestRender()
        }

    var colorRange: ColorRange = ColorRange.LIMITED
        set(value) {
            field = value
            requestRender()
        }

    @Volatile
    private var isSurfaceAvailable = false

    @Volatile
    private var flashState: Boolean = false

    private var renderJob: Job? = null
    private val engine = PatternCanvasEngine()

    init {
        val is4K = isDisplay4KCapable(context)
        targetWidth = if (is4K) 3840 else 1920
        targetHeight = if (is4K) 2160 else 1080

        holder.addCallback(this)
        holder.setFixedSize(targetWidth, targetHeight)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isSurfaceAvailable = true
        startRenderLoop()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        holder.setFixedSize(targetWidth, targetHeight)
        requestRender()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isSurfaceAvailable = false
        stopRenderLoop()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRenderLoop()
    }

    /**
     * Requests an immediate single-frame redraw on [Dispatchers.Default].
     */
    fun requestRender() {
        if (!isSurfaceAvailable) return
        CoroutineScope(Dispatchers.Default).launch {
            renderFrame()
        }
    }

    private fun startRenderLoop() {
        renderJob?.cancel()
        renderJob = CoroutineScope(Dispatchers.Default).launch {
            var lastFlashToggle = System.currentTimeMillis()
            while (isActive && isSurfaceAvailable) {
                val now = System.currentTimeMillis()
                if (now - lastFlashToggle >= 500L) {
                    flashState = !flashState
                    lastFlashToggle = now
                }
                renderFrame()
                // Render pacing aligned with ~60Hz refresh rate (16ms)
                delay(16L)
            }
        }
    }

    private fun stopRenderLoop() {
        renderJob?.cancel()
        renderJob = null
    }

    private fun renderFrame() {
        if (!isSurfaceAvailable) return
        val canvas = holder.lockCanvas() ?: return
        try {
            engine.drawPattern(canvas, targetWidth, targetHeight, currentStep, colorRange, flashState)
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun isDisplay4KCapable(context: Context): Boolean {
        return try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            val display = wm?.defaultDisplay
            val modes = display?.supportedModes
            if (modes != null && modes.any { it.physicalWidth >= 3840 && it.physicalHeight >= 2160 }) {
                true
            } else {
                val metrics = context.resources.displayMetrics
                metrics.widthPixels >= 3840 && metrics.heightPixels >= 2160
            }
        } catch (_: Throwable) {
            false
        }
    }
}
