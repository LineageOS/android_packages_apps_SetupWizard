/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard

import com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP

import org.lineageos.setupwizard.SetupWizardApp.EXTRA_ENABLE_NEXT_ON_CONNECT
import org.lineageos.setupwizard.SetupWizardApp.EXTRA_PREFS_SET_BACK_TEXT
import org.lineageos.setupwizard.SetupWizardApp.EXTRA_PREFS_SHOW_BUTTON_BAR
import org.lineageos.setupwizard.SetupWizardApp.EXTRA_PREFS_SHOW_SKIP
import org.lineageos.setupwizard.SetupWizardApp.EXTRA_PREFS_SHOW_SKIP_TV

import android.content.Intent

import org.lineageos.setupwizard.util.SetupWizardUtils

class NetworkSetupActivity : SubBaseActivity() {

    override fun onStartSubactivity() {
        if ((!SetupWizardUtils.hasWifi(this) && !SetupWizardUtils.hasTelephony(this)) ||
            SetupWizardUtils.isNetworkConnectedToInternetViaEthernet(this)
        ) {
            finishAction(RESULT_SKIP)
            return
        }

        if (SetupWizardUtils.isOwner()) {
            tryEnablingWifi()
        }

        val intent = Intent(ACTION_SETUP_NETWORK).apply {
            putExtra(EXTRA_PREFS_SHOW_BUTTON_BAR, true)
            putExtra(EXTRA_PREFS_SHOW_SKIP, true)
            putExtra(EXTRA_PREFS_SHOW_SKIP_TV, true)
            // Explicitly pass null String to mirror the Java semantics
            putExtra(EXTRA_PREFS_SET_BACK_TEXT, null as String?)
            putExtra(EXTRA_ENABLE_NEXT_ON_CONNECT, true)
        }
        startSubactivity(intent)
    }

    companion object {
        private const val ACTION_SETUP_NETWORK = "android.settings.NETWORK_PROVIDER_SETUP"
    }
}
