/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.network

import android.content.Intent
import com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP
import org.lineageos.setupwizard.SetupWizardApp
import org.lineageos.setupwizard.base.SubBaseActivity
import org.lineageos.setupwizard.util.SetupWizardUtils

class NetworkSetupActivity : SubBaseActivity() {

    override fun onStartSubactivity() {
        if (
            (!SetupWizardUtils.hasWifi(this) && !SetupWizardUtils.hasTelephony(this)) ||
                SetupWizardUtils.isNetworkConnectedToInternetViaEthernet(this)
        ) {
            finishAction(RESULT_SKIP)
            return
        }

        if (SetupWizardUtils.isOwner()) {
            tryEnablingWifi()
        }

        val intent =
            Intent(ACTION_SETUP_NETWORK).apply {
                putExtra(SetupWizardApp.EXTRA_PREFS_SHOW_BUTTON_BAR, true)
                putExtra(SetupWizardApp.EXTRA_PREFS_SHOW_SKIP, true)
                putExtra(SetupWizardApp.EXTRA_PREFS_SHOW_SKIP_TV, true)
                putExtra(SetupWizardApp.EXTRA_PREFS_SET_BACK_TEXT, null as String?)
                putExtra(SetupWizardApp.EXTRA_ENABLE_NEXT_ON_CONNECT, true)
            }
        startSubactivity(intent)
    }

    companion object {
        private const val ACTION_SETUP_NETWORK = "android.settings.NETWORK_PROVIDER_SETUP"
    }
}
