/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.BlendMode
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.DimenRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.setupcompat.util.SystemBarHelper
import org.lineageos.setupwizard.SetupWizardApp.Companion.LOGV
import org.lineageos.setupwizard.base.BaseSetupWizardActivity
import org.lineageos.setupwizard.util.SetupWizardUtils
import org.lineageos.setupwizard.widget.RevealHoleView

class FinishActivity : BaseSetupWizardActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private var rootView: View? = null
    private var swipeHint: View? = null
    private var swipeHintIcon: View? = null
    private var swipeHintText: View? = null
    private var background: RevealHoleView? = null
    private var brandLogo: View? = null

    private var velocityTracker: VelocityTracker? = null
    private var dragStartY = 0f
    private var dragging = false
    private var revealProgress = 0f
    private var logoPunched = false

    private var edgeToEdgeWallpaperBackgroundTheme: Resources.Theme? = null

    private enum class FinishState {
        NONE,
        ANIMATING,
        FINISHED,
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "onCreate: finishState=$finishState")

        overrideActivityTransition(
            OVERRIDE_TRANSITION_CLOSE,
            R.anim.translucent_enter,
            R.anim.translucent_exit,
        )
        if (LOGV) {
            logActivityState("onCreate savedInstanceState=$savedInstanceState")
        }
        // Edge-to-edge
        val window = window
        window.setDecorFitsSystemWindows(false)

        window.isNavigationBarContrastEnforced = false
        window.isStatusBarContrastEnforced = false

        rootView = findViewById(R.id.root)
        swipeHint = findViewById(R.id.swipe_hint)
        swipeHintIcon = findViewById(R.id.swipe_hint_icon)
        swipeHintText = findViewById(R.id.swipe_hint_text)
        background = findViewById(R.id.background)
        brandLogo = findViewById(R.id.brand_logo)

        rootView?.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // Ensure the main layout (not including the background view) does not get obscured by bars.
        ViewCompat.setOnApplyWindowInsetsListener(rootView!!) { _, windowInsets ->
            val linearLayout = findViewById<View>(R.id.linear_layout)
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = linearLayout.layoutParams as MarginLayoutParams
            params.leftMargin = insets.left
            params.topMargin = insets.top
            params.rightMargin = insets.right
            params.bottomMargin = insets.bottom
            linearLayout.layoutParams = params
            WindowInsetsCompat.CONSUMED
        }

        if (finishState == FinishState.FINISHED) {
            Log.e(TAG, "Should not start again when finished!")
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        velocityTracker?.recycle()
        velocityTracker = null
    }

    private fun punchLogoOutOfBackground() {
        if (logoPunched) {
            return
        }
        val logo = brandLogo ?: return
        logo.setLayerType(View.LAYER_TYPE_HARDWARE, Paint().apply { blendMode = BlendMode.DST_OUT })
        logoPunched = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (finishState != FinishState.NONE) {
            return super.onTouchEvent(event)
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                punchLogoOutOfBackground()
                dragStartY = event.y
                dragging = true
                velocityTracker = VelocityTracker.obtain()
                velocityTracker?.addMovement(event)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!dragging) return super.onTouchEvent(event)
                velocityTracker?.addMovement(event)
                val dragged = (dragStartY - event.y).coerceAtLeast(0f)
                applyRevealProgress((dragged / swipeDistance()).coerceIn(0f, 1f))
                return true
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (!dragging) return super.onTouchEvent(event)
                dragging = false
                val tracker = velocityTracker
                tracker?.addMovement(event)
                tracker?.computeCurrentVelocity(1000)
                val flungUp = -(tracker?.yVelocity ?: 0f) > SWIPE_MIN_VELOCITY
                tracker?.recycle()
                velocityTracker = null
                if (revealProgress >= COMMIT_FRACTION || flungUp) {
                    commitReveal()
                } else {
                    springBack()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun swipeDistance() = (rootView?.height ?: resources.displayMetrics.heightPixels) / 3f

    private fun applyRevealProgress(progress: Float) {
        revealProgress = progress
        val background = background ?: return
        brandLogo?.let { logo ->
            (logo.parent as? View)?.let { parent ->
                val location = IntArray(2)
                parent.getLocationOnScreen(location)
                background.holeCenterX = location[0] + logo.left + logo.pivotX
                background.holeCenterY = location[1] + logo.top + logo.pivotY
            }
        }
        background.holeRadius = background.fullRadius() * REVEAL_OVERSHOOT * progress
        brandLogo?.apply {
            val scale = LOGO_START_SCALE + (LOGO_END_SCALE - LOGO_START_SCALE) * progress
            scaleX = scale
            scaleY = scale
        }
        val fade = 1f - progress
        swipeHintIcon?.let {
            it.translationY = -rise(R.dimen.swipe_hint_icon_rise) * progress
            it.alpha = fade
        }
        swipeHintText?.let {
            it.translationY = -rise(R.dimen.swipe_hint_text_rise) * progress
            it.alpha = fade
        }
    }

    private fun rise(@DimenRes dimen: Int) = resources.getDimensionPixelSize(dimen).toFloat()

    private fun springBack() {
        ValueAnimator.ofFloat(revealProgress, 0f).apply {
            duration = SPRING_BACK_DURATION_MS
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { applyRevealProgress(it.animatedValue as Float) }
            start()
        }
    }

    private fun disableNavigation() {
        swipeHint?.visibility = View.INVISIBLE
        SystemBarHelper.setBackButtonVisible(window, false)
    }

    override fun applyForwardTransition() {
        // no-op
    }

    override val layoutResId: Int = R.layout.finish_activity

    override val installFooterBar: Boolean = false

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        if (edgeToEdgeWallpaperBackgroundTheme == null) {
            theme.applyStyle(R.style.EdgeToEdgeWallpaperBackground, true)
            edgeToEdgeWallpaperBackgroundTheme = theme
        }
        return edgeToEdgeWallpaperBackgroundTheme!!
    }

    override fun onNextPressed() {
        when (finishState) {
            FinishState.NONE -> commitReveal()
            else -> Log.e(TAG, "Unexpected state $finishState when navigating next")
        }
    }

    private fun commitReveal() {
        finishState = FinishState.ANIMATING
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED)
        disableNavigation()
        ValueAnimator.ofFloat(revealProgress, 1f).apply {
            duration = (ANIM_DURATION_MS * (1f - revealProgress)).toLong().coerceAtLeast(200L)
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { applyRevealProgress(it.animatedValue as Float) }
            addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        rootView?.visibility = View.INVISIBLE
                        handler.post {
                            if (LOGV) {
                                Log.v(TAG, "Animation ended")
                            }
                            finishAfterAnimation()
                        }
                    }
                }
            )
            start()
        }
    }

    private fun finishAfterAnimation() {
        SetupWizardUtils.finishSetupWizard(this)
        finishState = FinishState.FINISHED
    }

    companion object {
        private const val TAG = "FinishActivity"

        private const val COMMIT_FRACTION = 0.4f
        private const val SWIPE_MIN_VELOCITY = 600f

        private const val ANIM_DURATION_MS = 900L
        private const val SPRING_BACK_DURATION_MS = 200L

        private const val REVEAL_OVERSHOOT = 1.35f

        private const val LOGO_START_SCALE = 1.2f
        private const val LOGO_END_SCALE = 18f

        // Static so a relaunch after the wizard has finished is recognised and dropped rather
        // than replaying the reveal; @Volatile because it is written from animation callbacks.
        @Volatile private var finishState = FinishState.NONE
    }
}
