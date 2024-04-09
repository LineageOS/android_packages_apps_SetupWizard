/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.network

import android.content.Intent
import android.os.Bundle
import android.os.SystemProperties.getBoolean
import android.service.euicc.EuiccService.ACTION_PROVISION_EMBEDDED_SUBSCRIPTION
import android.telephony.euicc.EuiccManager.EXTRA_FORCE_PROVISION
import androidx.activity.result.ActivityResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP
import com.google.android.setupdesign.util.ThemeHelper
import com.google.android.setupcompat.util.WizardManagerHelper
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.base.SubBaseActivity
import org.lineageos.setupwizard.util.SetupWizardUtils

class SimMissingActivity : SubBaseActivity() {

    private val euiccAvailable: Boolean
        get() = SetupWizardUtils.hasEuicc(this) && getBoolean(KEY_ENABLE_ESIM_UI_BY_DEFAULT, true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (euiccAvailable) {
            setDescriptionText(getString(R.string.esim_support_summary))
            setNextText(R.string.setup_euicc)
        } else {
            setDescriptionText(getString(R.string.sim_missing_summary))
        }
    }

    override fun onNextIntentResult(activityResult: ActivityResult) {
        if (!SetupWizardUtils.simMissing(this) || !SetupWizardUtils.hasTelephony(this)) {
            val resultCode = activityResult.resultCode
            val data = activityResult.data
            if (resultCode == RESULT_CANCELED && data!!.getBooleanExtra("onBackPressed", false)) {
                finishAction(resultCode, data)
            }
        }
    }

    override fun onSubactivityResult(activityResult: ActivityResult) {
        // We don't really care about the result, but if a SIM is no longer missing, we're done.
        if (!SetupWizardUtils.simMissing(this)) {
            // See comments in onStartSubactivity for an explanation.
            finishAction(RESULT_SKIP, Intent().putExtra("onBackPressed", true))
        }
    }

    override fun onStartSubactivity() {
        if (!SetupWizardUtils.simMissing(this) || !SetupWizardUtils.hasTelephony(this)) {
            // NetworkSetupActivity comes before us. DateTimeActivity comes after.
            // If the user presses the back button on DateTimeActivity, we can only pass along
            // that information to NetworkSetupActivity if we are still around. But if we finish
            // here, we're gone, and NetworkSetupActivity will get whatever result we give here.
            // We can't predict the future, but we can reasonably assume that the only way for
            // NetworkSetupActivity to be reached later is if the user went backwards. So, we
            // finish this activity faking that the user pressed the back button, which is required
            // for subactivities like NetworkSetupActivity to work properly on backward navigation.
            // See also onSubactivityResult.
            // TODO: Resolve all this.
            finishAction(RESULT_SKIP, Intent().putExtra("onBackPressed", true))
        }

        setNextAllowed(euiccAvailable)
    }

    override fun onNextPressed() {
        launchEuiccSetup()
    }

    override fun onSkipPressed() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sim_missing_skip_title)
            .setMessage(R.string.sim_missing_skip_message)
            .setPositiveButton(R.string.skip) { _, _ ->
                finishAction(RESULT_SKIP, Intent().putExtra("onBackPressed", true))
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun decorateIntent(intent: Intent): Intent =
        intent
            .putExtra(WizardManagerHelper.EXTRA_THEME, ThemeHelper.THEME_GLIF_EXPRESSIVE)

    override val layoutResId = R.layout.sim_missing_page

    override val titleResId = R.string.setup_sim_missing

    override val iconResId = R.drawable.ic_sim

    override val installFooterBar = true

    override val showSkipButton = true

    private fun launchEuiccSetup() {
        startSubactivity(
            Intent(ACTION_PROVISION_EMBEDDED_SUBSCRIPTION).apply {
                putExtra(EXTRA_FORCE_PROVISION, true)
            }
        )
    }

    companion object {
        // From com.android.settings.network.telephony.MobileNetworkUtils
        // System Property which is used to decide whether the default eSIM UI will be shown,
        // the default value is false.
        private const val KEY_ENABLE_ESIM_UI_BY_DEFAULT = "esim.enable_esim_system_ui_by_default"
    }
}
