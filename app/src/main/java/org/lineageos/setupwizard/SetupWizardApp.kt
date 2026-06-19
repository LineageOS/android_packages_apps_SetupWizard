/*
 * SPDX-FileCopyrightText: 2013 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard

import android.app.Application
import android.app.StatusBarManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemProperties
import android.provider.Settings
import android.util.Log
import org.lineageos.setupwizard.util.SetupWizardUtils

class SetupWizardApp : Application() {

    var ignoreSimLocale = SystemProperties.getBoolean(IGNORE_SIM_LOCALE_PROP, false)

    private val mHandler = Handler(Looper.getMainLooper())
    private val mRadioTimeoutRunnable = Runnable { isRadioReady = true }

    override fun onCreate() {
        super.onCreate()
        if (LOGV) {
            Log.v(TAG, "onCreate()")
        }
        sStatusBarManager = SetupWizardUtils.disableStatusBar(this)
        mHandler.postDelayed(mRadioTimeoutRunnable, RADIO_READY_TIMEOUT.toLong())
        if (SetupWizardUtils.hasGMS(this)) {
            SetupWizardUtils.disableHome(this)
            if (SetupWizardUtils.isOwner()) {
                Settings.Global.putInt(contentResolver, Settings.Global.ASSISTED_GPS_ENABLED, 1)
            }
        }
    }

    companion object {
        val TAG: String = SetupWizardApp::class.java.simpleName

        // Verbose logging
        val LOGV: Boolean = Log.isLoggable(TAG, Log.VERBOSE)

        const val ACTION_RESTORE_FROM_BACKUP = "com.stevesoltys.seedvault.RESTORE_BACKUP"
        const val ACTION_EMERGENCY_DIAL = "com.android.phone.EmergencyDialer.DIAL"
        const val ACTION_LOAD = "com.android.wizard.LOAD"

        const val EXTRA_WIZARD_BUNDLE = "wizardBundle"
        const val EXTRA_SCRIPT_URI = "scriptUri"
        const val EXTRA_ACTION_ID = "actionId"
        const val EXTRA_RESULT_CODE = "com.android.setupwizard.ResultCode"
        const val EXTRA_PREFS_SHOW_BUTTON_BAR = "extra_prefs_show_button_bar"
        const val EXTRA_PREFS_SHOW_SKIP = "extra_prefs_show_skip"
        const val EXTRA_PREFS_SHOW_SKIP_TV = "extra_show_skip_network"
        const val EXTRA_PREFS_SET_BACK_TEXT = "extra_prefs_set_back_text"
        const val EXTRA_ENABLE_NEXT_ON_CONNECT = "wifi_enable_next_on_connect"

        const val KEY_SEND_METRICS = "send_metrics"
        const val DISABLE_NAV_KEYS = "disable_nav_keys"
        const val ENABLE_RECOVERY_UPDATE = "enable_recovery_update"
        const val UPDATE_RECOVERY_PROP = "persist.vendor.recovery_update"
        const val IGNORE_SIM_LOCALE_PROP = "ro.setupwizard.ignore_sim_locale"

        const val NAVIGATION_OPTION_KEY = "navigation_option"

        const val RADIO_READY_TIMEOUT = 10 * 1000

        @Volatile var isRadioReady: Boolean = false

        private var sStatusBarManager: StatusBarManager? = null

        private val mSettingsBundle = Bundle()

        fun getStatusBarManager(): StatusBarManager? = sStatusBarManager

        fun getSettingsBundle(): Bundle = mSettingsBundle
    }
}
