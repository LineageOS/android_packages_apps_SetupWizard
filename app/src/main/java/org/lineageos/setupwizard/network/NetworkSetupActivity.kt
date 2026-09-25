/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.network

import android.content.Intent
import androidx.activity.result.ActivityResult
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper
import com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP
import org.lineageos.setupwizard.EXTRA_ENABLE_NEXT_ON_CONNECT
import org.lineageos.setupwizard.EXTRA_PREFS_SET_BACK_TEXT
import org.lineageos.setupwizard.EXTRA_PREFS_SHOW_BUTTON_BAR
import org.lineageos.setupwizard.EXTRA_PREFS_SHOW_SKIP
import org.lineageos.setupwizard.EXTRA_PREFS_SHOW_SKIP_TV
import org.lineageos.setupwizard.base.SubBaseActivity
import org.lineageos.setupwizard.util.SetupWizardUtils

class NetworkSetupActivity : SubBaseActivity() {

    private var leftNetworkToTvSettings = false

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
            Intent(networkSetupAction()).apply {
                if (!PartnerConfigHelper.isGlifExpressiveEnabled(this@NetworkSetupActivity)) {
                    putExtra(EXTRA_PREFS_SHOW_BUTTON_BAR, true)
                    putExtra(EXTRA_PREFS_SHOW_SKIP, true)
                }
                putExtra(EXTRA_PREFS_SHOW_SKIP_TV, true)
                putExtra(EXTRA_PREFS_SET_BACK_TEXT, null as String?)
                putExtra(EXTRA_ENABLE_NEXT_ON_CONNECT, true)
            }
        startSubactivity(intent)
    }

    private fun networkSetupAction(): String {
        if (Intent(ACTION_SETUP_NETWORK).resolveActivity(packageManager) != null) {
            return ACTION_SETUP_NETWORK
        }
        leftNetworkToTvSettings = true
        return ACTION_SETUP_WIFI_LEANBACK
    }

    override fun onSubactivityResult(activityResult: ActivityResult) {
        if (leftNetworkToTvSettings && !isSubactivityNotFound) {
            nextAction(RESULT_OK)
            return
        }
        super.onSubactivityResult(activityResult)
    }

    companion object {
        private const val ACTION_SETUP_NETWORK = "android.settings.NETWORK_PROVIDER_SETUP"
        private const val ACTION_SETUP_WIFI_LEANBACK = "com.android.net.wifi.SETUP_WIFI_NETWORK"
    }
}
