/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.system

import android.os.Bundle
import android.os.SystemProperties
import android.util.Log
import android.view.View
import android.widget.CheckBox
import com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.SetupWizardApp
import org.lineageos.setupwizard.base.BaseSetupWizardActivity
import org.lineageos.setupwizard.util.SetupWizardUtils

class UpdateRecoveryActivity : BaseSetupWizardActivity() {

    private lateinit var recoveryUpdateCheckbox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glifLayout.setDescriptionText(
            getString(
                R.string.update_recovery_full_description,
                getString(R.string.update_recovery_description),
                getString(R.string.update_recovery_warning),
            )
        )

        if (!SetupWizardUtils.hasRecoveryUpdater(this)) {
            Log.v(TAG, "No recovery updater, skipping UpdateRecoveryActivity")
            finishAction(RESULT_SKIP)
            return
        }

        setNextText(R.string.next)
        recoveryUpdateCheckbox = findViewById(R.id.update_recovery_checkbox)

        findViewById<View>(R.id.update_recovery_checkbox_view).setOnClickListener {
            recoveryUpdateCheckbox.isChecked = !recoveryUpdateCheckbox.isChecked
        }

        // Allow overriding the default checkbox state
        if (firstTime) {
            SetupWizardApp.settingsBundle.putBoolean(
                SetupWizardApp.ENABLE_RECOVERY_UPDATE,
                SystemProperties.getBoolean(SetupWizardApp.UPDATE_RECOVERY_PROP, true),
            )
        }

        firstTime = false
    }

    override fun onResume() {
        super.onResume()
        recoveryUpdateCheckbox.isChecked =
            SetupWizardApp.settingsBundle.getBoolean(SetupWizardApp.ENABLE_RECOVERY_UPDATE, true)
    }

    override fun onNextPressed() {
        SetupWizardApp.settingsBundle.putBoolean(
            SetupWizardApp.ENABLE_RECOVERY_UPDATE,
            recoveryUpdateCheckbox.isChecked,
        )
        super.onNextPressed()
    }

    override val layoutResId: Int = R.layout.update_recovery_page

    override val titleResId: Int = R.string.update_recovery_title

    override val iconResId: Int = R.drawable.ic_system_update

    companion object {
        private const val TAG = "UpdateRecoveryActivity"
        private var firstTime: Boolean = true
    }
}
