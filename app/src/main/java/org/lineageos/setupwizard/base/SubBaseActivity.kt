/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.base

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import com.google.android.setupcompat.util.ResultCodes.RESULT_ACTIVITY_NOT_FOUND
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.SetupWizardApp

abstract class SubBaseActivity : BaseSetupWizardActivity() {

    protected var mIsSubactivityNotFound: Boolean = false

    protected abstract fun onStartSubactivity()

    private lateinit var subactivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        if (SetupWizardApp.LOGV) {
            Log.d(TAG, "onCreate savedInstanceState=" + savedInstanceState)
        }
        super.onCreate(savedInstanceState)

        subactivityResultLauncher =
            registerForActivityResult(StartDecoratedActivityForResult(), this::onSubactivityResult)

        setNextAllowed(false)

        if (savedInstanceState == null) {
            onStartSubactivity()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        subactivityResultLauncher.unregister()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    protected fun startSubactivity(subactivityIntent: Intent) {
        val parentIntent = intent
        val wizardBundle = parentIntent.getBundleExtra(SetupWizardApp.EXTRA_WIZARD_BUNDLE)
        wizardBundle?.let {
            if (it.containsKey(SetupWizardApp.EXTRA_SCRIPT_URI)) {
                subactivityIntent.putExtra(SetupWizardApp.EXTRA_WIZARD_BUNDLE, it)
            }
        }
        runCatching { subactivityResultLauncher.launch(subactivityIntent) }
            .onFailure {
                Log.w(TAG, "activity not found; start next screen and finish; intent=$parentIntent")
                mIsSubactivityNotFound = true
                finishAction(RESULT_ACTIVITY_NOT_FOUND)
            }
    }

    override fun onNextIntentResult(activityResult: ActivityResult) {
        super.onNextIntentResult(activityResult)
        val data = activityResult.data
        if (data?.getBooleanExtra("onBackPressed", false) == true) {
            onStartSubactivity()
        }
    }

    protected open fun onSubactivityResult(activityResult: ActivityResult) {
        val resultCode = activityResult.resultCode
        val data = activityResult.data
        when {
            resultCode != RESULT_CANCELED -> nextAction(resultCode, data)
            mIsSubactivityNotFound -> finishAction(RESULT_ACTIVITY_NOT_FOUND)
            data?.getBooleanExtra("onBackPressed", false) == true -> onStartSubactivity()
            else -> finishAction(RESULT_CANCELED)
        }
    }

    override fun getLayoutResId(): Int = R.layout.setup_loading_page

    override fun getTitleResId(): Int = R.string.loading

    companion object {
        private const val TAG = "SubBaseActivity"
    }
}
