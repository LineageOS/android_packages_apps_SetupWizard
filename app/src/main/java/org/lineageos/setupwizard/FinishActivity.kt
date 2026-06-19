/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.setupcompat.util.SystemBarHelper
import kotlin.math.hypot
import org.lineageos.setupwizard.SetupWizardApp.Companion.LOGV
import org.lineageos.setupwizard.base.BaseSetupWizardActivity
import org.lineageos.setupwizard.util.SetupWizardUtils

class FinishActivity : BaseSetupWizardActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private var rootView: View? = null
    private var edgeToEdgeWallpaperBackgroundTheme: Resources.Theme? = null

    private enum class FinishState {
        NONE,
        SHOULD_ANIMATE,
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
        setNextText(R.string.start)

        // Edge-to-edge. Needed for the background view to fill the full screen.
        val window = window
        window.setDecorFitsSystemWindows(false)

        // Make sure 3-button navigation bar is the same color as the rest of the screen.
        window.isNavigationBarContrastEnforced = false

        // Ensure the main layout (not including the background view) does not get obscured by bars.
        rootView = findViewById(R.id.root)
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

        if (finishState != FinishState.NONE) {
            disableNavigation()
        }

        when (finishState) {
            FinishState.NONE -> {}
            FinishState.SHOULD_ANIMATE -> startFinishSequence()
            FinishState.FINISHED -> {
                Log.e(TAG, "Should not start again when finished!")
                finish()
            }
            else -> Log.w(TAG, "Unexpected onCreate state $finishState")
        }
    }

    private fun disableNavigation() {
        hideNextButton()
        SystemBarHelper.setBackButtonVisible(window, false)
    }

    private fun disableActivityTransitions() {
        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
    }

    override fun applyForwardTransition() {
        if (finishState == FinishState.NONE) {
            super.applyForwardTransition()
        }
    }

    override fun applyBackwardTransition() {
        if (finishState == FinishState.NONE) {
            super.applyBackwardTransition()
        }
    }

    override val layoutResId: Int = R.layout.finish_activity

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        if (finishState == FinishState.NONE) {
            return theme
        }
        if (edgeToEdgeWallpaperBackgroundTheme == null) {
            theme.applyStyle(R.style.EdgeToEdgeWallpaperBackground, true)
            edgeToEdgeWallpaperBackgroundTheme = theme
        }
        return edgeToEdgeWallpaperBackgroundTheme!!
    }

    override fun onNavigateNext() {
        when (finishState) {
            FinishState.NONE -> relaunchAndRunAnimation()
            else -> Log.e(TAG, "Unexpected state $finishState when navigating next")
        }
    }

    private fun relaunchAndRunAnimation() {
        finishState = FinishState.SHOULD_ANIMATE
        // Relaunching the activity before finishing is the only way currently known to prevent
        // an out-of-place slide transition from happening, even when disabling transitions, and
        // regardless of when we disable them. This also means we can't simply call recreate(), but
        // another reason is that recreate() doesn't seem to reinitialize the theme, which is the
        // entire point of relaunching - to ensure this activity reveals a wallpaper background.
        // These theme shenanigans and relaunching were not necessary prior to Android 14 QPR3.
        startActivity(intent)
        finish()
        disableActivityTransitions()
    }

    private fun startFinishSequence() {
        finishState = FinishState.ANIMATING
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED)
        disableNavigation()

        // Begin outro animation.
        if (rootView!!.isAttachedToWindow) {
            handler.post { animateOut() }
        } else {
            rootView!!.addOnAttachStateChangeListener(
                object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        handler.post { animateOut() }
                    }

                    override fun onViewDetachedFromWindow(v: View) {
                        // Do nothing
                    }
                }
            )
        }
    }

    private fun animateOut() {
        if (finishState != FinishState.ANIMATING) {
            Log.e(TAG, "animateOut but in $finishState phase. How?")
            return
        }
        val cx = (rootView!!.left + rootView!!.right) / 2
        val cy = (rootView!!.top + rootView!!.bottom) / 2
        val fullRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
        val anim =
            runCatching {
                    ViewAnimationUtils.createCircularReveal(rootView, cx, cy, fullRadius, 0f)
                }
                .getOrElse {
                    Log.e(TAG, "Failed to create finish animation", it)
                    finishAfterAnimation()
                    return
                }
        anim.duration = 900
        anim.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    rootView!!.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    rootView!!.visibility = View.INVISIBLE
                    handler.post {
                        if (LOGV) {
                            Log.v(TAG, "Animation ended")
                        }
                        finishAfterAnimation()
                    }
                }
            }
        )
        anim.start()
    }

    private fun finishAfterAnimation() {
        SetupWizardUtils.finishSetupWizard(this@FinishActivity)
        finishState = FinishState.FINISHED
    }

    companion object {
        val TAG: String = FinishActivity::class.java.simpleName

        // "Why not just start this activity with an Intent extra?" you might ask. Been there.
        // We need this to affect the theme, and even onCreate is not early enough for that,
        // so "@Volatile" it is. Feel free to rework this if you dare.
        @Volatile private var finishState = FinishState.NONE
    }
}
