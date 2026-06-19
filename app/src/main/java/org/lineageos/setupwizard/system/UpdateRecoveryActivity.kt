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

    private lateinit var mRecoveryUpdateCheckbox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getGlifLayout()
            .setDescriptionText(
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
        mRecoveryUpdateCheckbox = findViewById(R.id.update_recovery_checkbox)

        val cbView: View = findViewById(R.id.update_recovery_checkbox_view)
        cbView.setOnClickListener {
            mRecoveryUpdateCheckbox.isChecked = !mRecoveryUpdateCheckbox.isChecked
        }

        // Allow overriding the default checkbox state
        if (sFirstTime) {
            SetupWizardApp.getSettingsBundle()
                .putBoolean(
                    SetupWizardApp.ENABLE_RECOVERY_UPDATE,
                    SystemProperties.getBoolean(SetupWizardApp.UPDATE_RECOVERY_PROP, true),
                )
        }

        sFirstTime = false
    }

    override fun onResume() {
        super.onResume()
        val myPageBundle = SetupWizardApp.getSettingsBundle()
        val checked = myPageBundle.getBoolean(SetupWizardApp.ENABLE_RECOVERY_UPDATE, true)
        mRecoveryUpdateCheckbox.isChecked = checked
    }

    override fun onNextPressed() {
        SetupWizardApp.getSettingsBundle()
            .putBoolean(SetupWizardApp.ENABLE_RECOVERY_UPDATE, mRecoveryUpdateCheckbox.isChecked)
        super.onNextPressed()
    }

    override fun getLayoutResId(): Int = R.layout.update_recovery_page

    override fun getTitleResId(): Int = R.string.update_recovery_title

    override fun getIconResId(): Int = R.drawable.ic_system_update

    companion object {
        private const val TAG = "UpdateRecoveryActivity"
        private var sFirstTime: Boolean = true
    }
}
