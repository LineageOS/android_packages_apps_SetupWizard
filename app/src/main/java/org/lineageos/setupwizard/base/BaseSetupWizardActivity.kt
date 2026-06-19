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

    private var mNavigationBar: NavigationLayout? = null
    private lateinit var mNextIntentResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        if (LOGV) {
            logActivityState("onCreate savedInstanceState=$savedInstanceState")
        }
        super.onCreate(savedInstanceState)
        mNextIntentResultLauncher =
            registerForActivityResult(StartDecoratedActivityForResult(), this::onNextIntentResult)
        initLayout()
        mNavigationBar = getNavigationBar()
        mNavigationBar?.setNavigationBarListener(this)
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
        mNextIntentResultLauncher.unregister()
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

    /**
     * @return The navigation bar instance in the layout, or null if the layout does not have a
     *   navigation bar.
     */
    fun getNavigationBar(): NavigationLayout? =
        findViewById<View>(R.id.navigation_bar) as? NavigationLayout

    fun setNextAllowed(allowed: Boolean) {
        mNavigationBar?.nextButton?.isEnabled = allowed
    }

    protected open fun onNextPressed() {
        nextAction(RESULT_OK)
    }

    protected open fun onSkipPressed() {
        nextAction(RESULT_SKIP)
    }

    protected fun setNextText(resId: Int) {
        mNavigationBar?.nextButton?.setText(resId)
    }

    fun getNextButton(): Button = mNavigationBar!!.nextButton

    protected fun setSkipText(resId: Int) {
        mNavigationBar?.skipButton?.setText(resId)
    }

    protected fun hideNextButton() {
        mNavigationBar?.nextButton?.visibility = INVISIBLE
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
        if (resultCode == RESULT_CANCELED) {
            throw IllegalArgumentException("Cannot call nextAction with RESULT_CANCELED")
        }
        setResult(resultCode, data)
        val intent = WizardManagerHelper.getNextIntent(getIntent(), resultCode, data)
        mNextIntentResultLauncher.launch(intent)
    }

    /** Adorn the Intent with Setup Wizard-related extras. */
    protected open fun decorateIntent(intent: Intent): Intent =
        intent
            .putExtra(WizardManagerHelper.EXTRA_IS_FIRST_RUN, isFirstRun())
            .putExtra(WizardManagerHelper.EXTRA_IS_SETUP_FLOW, true)
            .putExtra(WizardManagerHelper.EXTRA_THEME, ThemeHelper.THEME_GLIF_V4)

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

    protected fun tryEnablingWifi(): Boolean {
        val wifiManager = getSystemService(WifiManager::class.java)
        return wifiManager != null && wifiManager.setWifiEnabled(true)
    }

    private fun isFirstRun(): Boolean = true

    protected fun logActivityState(prefix: String) {
        Log.v(TAG, "$prefix isResumed=$isResumed isFinishing=$isFinishing isDestroyed=$isDestroyed")
    }

    private fun initLayout() {
        if (getLayoutResId() != -1) {
            setContentView(getLayoutResId())
        }
        if (getTitleResId() != -1) {
            val headerText = TextUtils.expandTemplate(getText(getTitleResId()))
            getGlifLayout().setHeaderText(headerText)
        }
        if (getIconResId() != -1) {
            val layout = getGlifLayout()
            val icon: Drawable = getDrawable(getIconResId())!!.mutate()
            icon.setTintList(Utils.getColorAccent(layout.context))
            layout.setIcon(icon)
        }
    }

    protected fun getGlifLayout(): GlifLayout = requireViewById(R.id.setup_wizard_layout)

    protected open fun getLayoutResId(): Int = -1

    protected open fun getTitleResId(): Int = -1

    protected open fun getIconResId(): Int = -1

    protected open fun applyForwardTransition() {
        TransitionHelper.applyForwardTransition(this, DEFAULT_TRANSITION, true)
    }

    protected open fun applyBackwardTransition() {
        TransitionHelper.applyBackwardTransition(this, DEFAULT_TRANSITION, true)
    }

    protected inner class StartDecoratedActivityForResult :
        ActivityResultContract<Intent, ActivityResult>() {

        private val mWrappedContract = StartActivityForResult()

        override fun createIntent(context: Context, input: Intent): Intent =
            decorateIntent(mWrappedContract.createIntent(context, input))

        override fun parseResult(resultCode: Int, intent: Intent?): ActivityResult =
            mWrappedContract.parseResult(resultCode, intent)
    }

    companion object {
        val TAG: String = BaseSetupWizardActivity::class.java.simpleName
        const val DEFAULT_TRANSITION = TransitionHelper.TRANSITION_FADE_THROUGH
    }
}
