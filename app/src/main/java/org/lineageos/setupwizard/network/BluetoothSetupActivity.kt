/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.network

import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResult
import com.google.android.setupcompat.util.ResultCodes.RESULT_ACTIVITY_NOT_FOUND
import com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP
import org.lineageos.setupwizard.base.SubBaseActivity
import org.lineageos.setupwizard.util.SetupWizardUtils

class BluetoothSetupActivity : SubBaseActivity() {

    override fun onStartSubactivity() {
        if (!SetupWizardUtils.hasLeanback(this) || SetupWizardUtils.isBluetoothDisabled()) {
            finishAction(RESULT_SKIP)
            return
        }

        runCatching {
                val intent =
                    Intent(ACTION_CONNECT_INPUT).apply {
                        putExtra(INTENT_EXTRA_NO_INPUT_MODE, true)
                    }
                startSubactivity(intent)
            }
            .onFailure {
                Log.e(TAG, "Error starting bluetooth setup", it)
                finishAction(RESULT_OK)
                SetupWizardUtils.disableComponent(this, BluetoothSetupActivity::class.java)
            }
    }

    override fun onSubactivityResult(activityResult: ActivityResult) {
        val data = activityResult.data
        when {
            isSubactivityNotFound -> finishAction(RESULT_ACTIVITY_NOT_FOUND)
            data?.getBooleanExtra("onBackPressed", false) == true -> onStartSubactivity()
            else -> nextAction(RESULT_OK, data)
        }
    }

    companion object {
        const val TAG: String = "BluetoothSetupActivity"
        private const val ACTION_CONNECT_INPUT = "com.google.android.intent.action.CONNECT_INPUT"
        private const val INTENT_EXTRA_NO_INPUT_MODE = "no_input_mode"
    }
}
