/*
 * SPDX-FileCopyrightText: 2024 The Android Open Source Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.network

import android.content.Context
import android.os.SystemProperties
import android.telephony.TelephonyManager
import android.telephony.euicc.EuiccManager
import android.util.Log
import org.lineageos.setupwizard.util.SetupWizardUtils

class EuiccRepository @JvmOverloads constructor(private val context: Context) {

    private val euiccManager = context.getSystemService(EuiccManager::class.java)
    private val telephonyManager = context.getSystemService(TelephonyManager::class.java)

    /**
     * Whether to show the entry point to eUICC settings.
     *
     * We show the entry point on any device which supports eUICC as long as either the eUICC was
     * ever provisioned (that is, at least one profile was ever downloaded onto it), or if the user
     * has enabled development mode.
     */
    fun showEuiccSettings(): Boolean {
        if (!SetupWizardUtils.hasTelephony(context)) {
            Log.i(TAG, "showEuiccSettings: no telephony voice/data capabilities.")
            return false
        }
        if (euiccManager == null || !euiccManager.isEnabled) {
            Log.w(TAG, "EuiccManager is not enabled.")
            return false
        }
        val ignoredCids =
            SystemProperties.get(KEY_ESIM_CID_IGNORE).split(',').filter { it.isNotEmpty() }
        val cid = SystemProperties.get(KEY_CID)
        if (cid in ignoredCids) {
            Log.i(TAG, "showEuiccSettings: cid ignored")
            return false
        }
        val enabledEsimUiByDefault =
            SystemProperties.getBoolean(KEY_ENABLE_ESIM_UI_BY_DEFAULT, true)
        Log.i(TAG, "showEuiccSettings: enabledEsimUiByDefault=$enabledEsimUiByDefault")
        return enabledEsimUiByDefault && isCurrentCountrySupported()
    }

    /**
     * Loop through all the device logical slots to check whether the user's current country
     * supports eSIM.
     */
    private fun isCurrentCountrySupported(): Boolean {
        val euiccManager = euiccManager ?: return false
        val telephonyManager = telephonyManager ?: return false
        val visitedCountrySet = mutableSetOf<String>()
        for (slotIndex in 0 until telephonyManager.getActiveModemCount()) {
            val countryCode = telephonyManager.getNetworkCountryIso(slotIndex)
            if (
                countryCode.isNotEmpty() &&
                    visitedCountrySet.add(countryCode) &&
                    euiccManager.isSupportedCountry(countryCode)
            ) {
                Log.i(TAG, "isCurrentCountrySupported: $countryCode is supported")
                return true
            }
        }
        Log.i(TAG, "isCurrentCountrySupported: no country is supported")
        return false
    }

    companion object {
        private const val TAG = "EuiccRepository"

        /** CID of the device. */
        private const val KEY_CID: String = "ro.boot.cid"

        /** CIDs of devices which should not show anything related to eSIM. */
        private const val KEY_ESIM_CID_IGNORE: String = "ro.setupwizard.esim_cid_ignore"

        /**
         * System Property which is used to decide whether the default eSIM UI will be shown, the
         * default value is false.
         */
        private const val KEY_ENABLE_ESIM_UI_BY_DEFAULT: String =
            "esim.enable_esim_system_ui_by_default"
    }
}
