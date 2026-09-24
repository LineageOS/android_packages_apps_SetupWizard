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
import android.util.TypedValue
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_GESTURAL_OVERLAY
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.setupcompat.util.SystemBarHelper
import kotlin.math.min
import org.lineageos.setupwizard.base.BaseSetupWizardActivity
import org.lineageos.setupwizard.util.SetupWizardUtils
import org.lineageos.setupwizard.widget.RevealHoleView

class FinishActivity : BaseSetupWizardActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var rootView: View
    private lateinit var swipeHint: View
    private lateinit var swipeHintIcon: ImageView
    private lateinit var swipeHintText: TextView
    private lateinit var background: RevealHoleView
    private lateinit var brandLogo: ImageView

    private var velocityTracker: VelocityTracker? = null
    private var dragStartY = 0f
    private var dragging = false
    private var revealProgress = 0f
    private var logoPunched = false

    private var edgeToEdgeWallpaperBackgroundTheme: Resources.Theme? = null
    private val revealWallpaper by lazy { !SetupWizardUtils.hasLeanback(this) }

    private val logoStartScale by lazy {
        ResourcesCompat.getFloat(resources, R.dimen.finish_logo_scale)
    }

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

        rootView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        brandLogo.scaleX = logoStartScale
        brandLogo.scaleY = logoStartScale

        applyHomeAffordance()

        // Ensure the main layout (not including the background view) does not get obscured by bars.
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, windowInsets ->
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

        if (savedInstanceState == null && finishState == FinishState.NONE) {
            playEntranceAnimation()
        }

        if (finishState == FinishState.FINISHED) {
            Log.e(TAG, "Should not start again when finished!")
            finish()
        }
    }

    private fun applyHomeAffordance() {
        if (usesGestureNavigation()) {
            return
        }
        val hintResId =
            if (SetupWizardUtils.hasLeanback(this)) {
                R.string.press_center_to_go_home
            } else {
                R.string.tap_home_to_go_home
            }
        swipeHintIcon.setImageResource(R.drawable.ic_nav_home)
        swipeHintText.setText(hintResId)

        swipeHintIcon.contentDescription = getText(hintResId)
        borderlessRippleResId()?.let { swipeHintIcon.setBackgroundResource(it) }
        swipeHintIcon.setOnClickListener {
            if (finishState != FinishState.NONE) {
                return@setOnClickListener
            }
            punchLogoOutOfBackground()
            commitReveal()
        }
    }

    private fun borderlessRippleResId(): Int? {
        val value = TypedValue()
        val resolved =
            theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, value, true)
        return value.resourceId.takeIf { resolved && it != 0 }
    }

    private fun usesGestureNavigation(): Boolean {
        if (SetupWizardUtils.hasLeanback(this)) {
            return false
        }
        val selected = SetupWizardApp.settingsBundle.getString(NAVIGATION_OPTION_KEY) ?: return true
        return selected == NAV_BAR_MODE_GESTURAL_OVERLAY
    }

    override fun onDestroy() {
        super.onDestroy()
        velocityTracker?.recycle()
        velocityTracker = null
    }

    private fun playEntranceAnimation() {
        val offset = rise(R.dimen.swipe_hint_icon_rise)
        listOf(swipeHintIcon, swipeHintText).forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = offset
            view
                .animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(ENTRANCE_HINT_DELAY_MS + index * ENTRANCE_STAGGER_MS)
                .setDuration(ENTRANCE_DURATION_MS)
        }

        brandLogo.alpha = 0f
        brandLogo
            .animate()
            .alpha(1f)
            .setStartDelay(ENTRANCE_LOGO_DELAY_MS)
            .setDuration(ENTRANCE_LOGO_DURATION_MS)
    }

    private fun endEntranceAnimation() {
        listOf(swipeHintIcon, swipeHintText, brandLogo).forEach {
            it.animate().cancel()
            it.alpha = 1f
            it.translationY = 0f
        }
    }

    private fun punchLogoOutOfBackground() {
        if (logoPunched || !revealWallpaper) {
            return
        }
        brandLogo.setLayerType(
            View.LAYER_TYPE_HARDWARE,
            Paint().apply { blendMode = BlendMode.DST_OUT },
        )
        logoPunched = true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (finishState != FinishState.NONE || event.repeatCount != 0) {
            return super.onKeyDown(keyCode, event)
        }
        if (keyCode !in COMMIT_KEY_CODES) {
            return super.onKeyDown(keyCode, event)
        }
        punchLogoOutOfBackground()
        commitReveal()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (finishState != FinishState.NONE) {
            return super.onTouchEvent(event)
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                endEntranceAnimation()
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

    private fun swipeDistance() = rootView.height / 3f

    private fun updateLogoPivot() {
        val drawable = brandLogo.drawable ?: return

        val availableWidth = brandLogo.width - brandLogo.paddingLeft - brandLogo.paddingRight
        val availableHeight = brandLogo.height - brandLogo.paddingTop - brandLogo.paddingBottom
        if (availableWidth <= 0 || availableHeight <= 0) {
            return
        }

        val scale =
            min(
                availableWidth / drawable.intrinsicWidth.toFloat(),
                availableHeight / drawable.intrinsicHeight.toFloat(),
            )
        val width = drawable.intrinsicWidth * scale
        val height = drawable.intrinsicHeight * scale
        val left = brandLogo.paddingLeft + (availableWidth - width) / 2f
        val top = brandLogo.paddingTop + (availableHeight - height) / 2f

        brandLogo.pivotX = left + width * MARK_CENTER_X
        brandLogo.pivotY = top + height * MARK_CENTER_Y
    }

    private fun applyRevealProgress(progress: Float) {
        revealProgress = progress
        updateLogoPivot()
        (brandLogo.parent as? View)?.let {
            val parentLocation = IntArray(2)
            val backgroundLocation = IntArray(2)
            it.getLocationOnScreen(parentLocation)
            background.getLocationOnScreen(backgroundLocation)
            background.holeCenterX =
                parentLocation[0] - backgroundLocation[0] + brandLogo.left + brandLogo.pivotX
            background.holeCenterY =
                parentLocation[1] - backgroundLocation[1] + brandLogo.top + brandLogo.pivotY
        }
        background.holeRadius = background.fullRadius * REVEAL_OVERSHOOT * progress
        brandLogo.apply {
            val scale = logoStartScale + (LOGO_END_SCALE - logoStartScale) * progress
            scaleX = scale
            scaleY = scale
        }
        val fade = 1f - progress
        swipeHintIcon.translationY = -rise(R.dimen.swipe_hint_icon_rise) * progress
        swipeHintIcon.alpha = fade
        swipeHintText.translationY = -rise(R.dimen.swipe_hint_text_rise) * progress
        swipeHintText.alpha = fade
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
        swipeHint.visibility = View.INVISIBLE
        SystemBarHelper.setBackButtonVisible(window, false)
    }

    override fun applyForwardTransition() {
        // no-op
    }

    override val layoutResId = R.layout.finish_activity

    override val installFooterBar = false

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        if (!revealWallpaper) {
            return theme
        }
        return edgeToEdgeWallpaperBackgroundTheme
            ?: theme
                .apply { applyStyle(R.style.EdgeToEdgeWallpaperBackground, true) }
                .also { edgeToEdgeWallpaperBackgroundTheme = it }
    }

    override fun onNextPressed() {
        when (finishState) {
            FinishState.NONE -> commitReveal()
            else -> Log.e(TAG, "Unexpected state $finishState when navigating next")
        }
    }

    private fun commitReveal() {
        endEntranceAnimation()
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
                        rootView.visibility = View.INVISIBLE
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

        private const val MARK_CENTER_X = 256f / 512f
        private const val MARK_CENTER_Y = 128f / 260f

        private const val ENTRANCE_HINT_DELAY_MS = 150L
        private const val ENTRANCE_STAGGER_MS = 80L
        private const val ENTRANCE_DURATION_MS = 350L
        private const val ENTRANCE_LOGO_DELAY_MS = 400L
        private const val ENTRANCE_LOGO_DURATION_MS = 450L

        private const val COMMIT_FRACTION = 0.4f
        private const val SWIPE_MIN_VELOCITY = 600f

        private const val ANIM_DURATION_MS = 900L
        private const val SPRING_BACK_DURATION_MS = 200L

        private val COMMIT_KEY_CODES =
            setOf(
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_NUMPAD_ENTER,
                KeyEvent.KEYCODE_BUTTON_A,
            )

        private const val REVEAL_OVERSHOOT = 1.35f

        private const val LOGO_END_SCALE = 18f

        // Static so a relaunch after the wizard has finished is recognised and dropped rather
        // than replaying the reveal; @Volatile because it is written from animation callbacks.
        @Volatile private var finishState = FinishState.NONE
    }
}
