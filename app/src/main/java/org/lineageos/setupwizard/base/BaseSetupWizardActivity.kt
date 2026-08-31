/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.base

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.android.settingslib.Utils
import com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP
import com.google.android.setupcompat.util.WizardManagerHelper
import com.google.android.setupdesign.GlifLayout
import com.google.android.setupdesign.transition.TransitionHelper
import com.google.android.setupdesign.util.ThemeHelper
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.SetupWizardApp.Companion.LOGV
import org.lineageos.setupwizard.util.SetupWizardUtils
import org.lineageos.setupwizard.widget.NavigationLayout
import org.lineageos.setupwizard.widget.NavigationLayout.NavigationBarListener

abstract class BaseSetupWizardActivity : AppCompatActivity(), NavigationBarListener {

    private var navigationBar: NavigationLayout? = null
    private lateinit var nextIntentResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        if (LOGV) {
            logActivityState("onCreate savedInstanceState=$savedInstanceState")
        }
        super.onCreate(savedInstanceState)
        nextIntentResultLauncher =
            registerForActivityResult(StartDecoratedActivityForResult(), this::onNextIntentResult)
        initLayout()
        navigationBar = findViewById<View>(R.id.navigation_bar) as? NavigationLayout
        navigationBar?.setNavigationBarListener(this)
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (LOGV) {
                        Log.v(TAG, "handleOnBackPressed()")
                    }
                    finishAction(RESULT_CANCELED, Intent().putExtra("onBackPressed", true))
                }
            },
        )
        // Apply default transition, to take effect whenever leaving this activity.
        applyForwardTransition()
    }

    override fun onStart() {
        if (LOGV) logActivityState("onStart")
        super.onStart()
    }

    override fun onRestart() {
        if (LOGV) logActivityState("onRestart")
        super.onRestart()
    }

    override fun onResume() {
        if (LOGV) logActivityState("onResume")
        super.onResume()
    }

    override fun onPause() {
        if (LOGV) logActivityState("onPause")
        super.onPause()
    }

    override fun onStop() {
        if (LOGV) logActivityState("onStop")
        super.onStop()
    }

    override fun onDestroy() {
        if (LOGV) logActivityState("onDestroy")
        super.onDestroy()
        nextIntentResultLauncher.unregister()
    }

    override fun onAttachedToWindow() {
        if (LOGV) logActivityState("onAttachedToWindow")
        super.onAttachedToWindow()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        if (LOGV) {
            Log.v(TAG, "onRestoreInstanceState($savedInstanceState)")
        }
        super.onRestoreInstanceState(savedInstanceState)
        val currentId = savedInstanceState.getInt("currentFocus", -1)
        if (currentId != -1) {
            findViewById<View>(currentId)?.requestFocus()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentFocus", currentFocus?.id ?: -1)
        if (LOGV) {
            Log.v(TAG, "onSaveInstanceState($outState)")
        }
    }

    fun setNextAllowed(allowed: Boolean) {
        navigationBar?.nextButton?.isEnabled = allowed
    }

    protected open fun onNextPressed() {
        nextAction(RESULT_OK)
    }

    protected open fun onSkipPressed() {
        nextAction(RESULT_SKIP)
    }

    protected fun setNextText(resId: Int) {
        navigationBar?.nextButton?.setText(resId)
    }

    val nextButton: Button
        get() = navigationBar!!.nextButton

    protected fun setSkipText(resId: Int) {
        navigationBar?.skipButton?.setText(resId)
    }

    protected fun hideNextButton() {
        navigationBar?.nextButton?.visibility = INVISIBLE
    }

    override fun onNavigateNext() {
        onNextPressed()
    }

    override fun onSkip() {
        onSkipPressed()
    }

    protected fun onSetupStart() {
        if (SetupWizardUtils.isOwner()) {
            tryEnablingWifi()
        }
    }

    override fun finish() {
        if (LOGV) {
            Log.v(TAG, "finish")
        }
        super.finish()
    }

    protected fun finishAction(resultCode: Int, data: Intent? = null) {
        if (resultCode != RESULT_CANCELED) {
            nextAction(resultCode, data)
            finish()
        } else {
            setResult(resultCode, data)
            finish()
            applyBackwardTransition()
        }
    }

    fun nextAction(resultCode: Int, data: Intent? = null) {
        if (LOGV) {
            Log.v(TAG, "nextAction resultCode=$resultCode data=$data this=$this")
        }
        require(resultCode != RESULT_CANCELED) { "Cannot call nextAction with RESULT_CANCELED" }
        setResult(resultCode, data)
        val nextIntent = WizardManagerHelper.getNextIntent(intent, resultCode, data)
        nextIntentResultLauncher.launch(nextIntent)
    }

    /** Adorn the Intent with Setup Wizard-related extras. */
    protected open fun decorateIntent(intent: Intent): Intent =
        intent
            .putExtra(WizardManagerHelper.EXTRA_IS_FIRST_RUN, isFirstRun())
            .putExtra(WizardManagerHelper.EXTRA_IS_SETUP_FLOW, true)
            .putExtra(WizardManagerHelper.EXTRA_THEME, ThemeHelper.THEME_GLIF_EXPRESSIVE)

    override fun startActivity(intent: Intent) {
        super.startActivity(decorateIntent(intent))
    }

    protected open fun onNextIntentResult(activityResult: ActivityResult) {
        val resultCode = activityResult.resultCode
        val data = activityResult.data
        if (LOGV) {
            Log.v(TAG, "onNextIntentResult($resultCode, ${data?.extras})")
        }
    }

    protected fun tryEnablingWifi(): Boolean =
        getSystemService(WifiManager::class.java)?.setWifiEnabled(true) ?: false

    private fun isFirstRun(): Boolean = true

    protected fun logActivityState(prefix: String) {
        Log.v(TAG, "$prefix isResumed=$isResumed isFinishing=$isFinishing isDestroyed=$isDestroyed")
    }

    private fun initLayout() {
        if (layoutResId != -1) {
            setContentView(layoutResId)
        }
        if (titleResId != -1) {
            val headerText = TextUtils.expandTemplate(getText(titleResId))
            glifLayout.setHeaderText(headerText)
        }
        if (iconResId != -1) {
            val layout = glifLayout
            val icon: Drawable = getDrawable(iconResId)!!.mutate()
            icon.setTintList(Utils.getColorAccent(layout.context))
            layout.setIcon(icon)
        }
    }

    protected val glifLayout: GlifLayout
        get() = requireViewById(R.id.setup_wizard_layout)

    protected open val layoutResId: Int = -1

    protected open val titleResId: Int = -1

    protected open val iconResId: Int = -1

    protected open fun applyForwardTransition() {
        TransitionHelper.applyForwardTransition(this, DEFAULT_TRANSITION, true)
    }

    protected open fun applyBackwardTransition() {
        TransitionHelper.applyBackwardTransition(this, DEFAULT_TRANSITION, true)
    }

    protected inner class StartDecoratedActivityForResult :
        ActivityResultContract<Intent, ActivityResult>() {

        private val wrappedContract = StartActivityForResult()

        override fun createIntent(context: Context, input: Intent): Intent =
            decorateIntent(wrappedContract.createIntent(context, input))

        override fun parseResult(resultCode: Int, intent: Intent?): ActivityResult =
            wrappedContract.parseResult(resultCode, intent)
    }

    companion object {
        private const val TAG = "BaseSetupWizardActivity"
        const val DEFAULT_TRANSITION = TransitionHelper.TRANSITION_FADE_THROUGH
    }
}
