/*
 * Copyright (C) 2013 The CyanogenMod Project
 * Copyright (C) 2017-2020 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.setupwizard;


import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import org.lineageos.setupwizard.util.NetworkMonitor;
import org.lineageos.setupwizard.util.PhoneMonitor;
import org.lineageos.setupwizard.util.SetupWizardUtils;

public class SetupWizardApp extends Application {

    public static final String TAG = SetupWizardApp.class.getSimpleName();
    // Leave this off for release
    public static final boolean DEBUG = false;
    /* Verbose Logging */
    public static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE);

    public static final String ACTION_SETUP_COMPLETE = "org.lineageos.setupwizard.LINEAGE_SETUP_COMPLETE";
    public static final String ACTION_FINISHED = "org.lineageos.setupwizard.SETUP_FINISHED";
    public static final String ACTION_SETUP_WIFI = "android.net.wifi.PICK_WIFI_NETWORK";
    public static final String ACTION_SETUP_BIOMETRIC = "android.settings.BIOMETRIC_ENROLL";
    public static final String ACTION_SETUP_LOCKSCREEN = "com.android.settings.SETUP_LOCK_SCREEN";
    public static final String ACTION_RESTORE_FROM_BACKUP = "com.stevesoltys.seedvault.RESTORE_BACKUP";
    public static final String ACTION_EMERGENCY_DIAL = "com.android.phone.EmergencyDialer.DIAL";
    public static final String ACTION_NEXT = "com.android.wizard.NEXT";
    public static final String ACTION_LOAD = "com.android.wizard.LOAD";


    public static final String EXTRA_FIRST_RUN = "firstRun";
    public static final String EXTRA_ALLOW_SKIP = "allowSkip";
    public static final String EXTRA_AUTO_FINISH = "wifi_auto_finish_on_connect";
    public static final String EXTRA_USE_IMMERSIVE = "useImmersiveMode";
    public static final String EXTRA_HAS_MULTIPLE_USERS = "hasMultipleUsers";
    public static final String EXTRA_THEME = "theme";
    public static final String EXTRA_MATERIAL_LIGHT = "material_light";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_DETAILS = "details";
    public static final String EXTRA_SCRIPT_URI = "scriptUri";
    public static final String EXTRA_ACTION_ID = "actionId";
    public static final String EXTRA_RESULT_CODE = "com.android.setupwizard.ResultCode";
    public static final String EXTRA_PREFS_SHOW_BUTTON_BAR = "extra_prefs_show_button_bar";
    public static final String EXTRA_PREFS_SET_BACK_TEXT = "extra_prefs_set_back_text";
    public static final String EXTRA_IS_SETUP_FLOW = "isSetupFlow";

    public static final String KEY_DETECT_CAPTIVE_PORTAL = "captive_portal_detection_enabled";
    public static final String KEY_SEND_METRICS = "send_metrics";
    public static final String DISABLE_NAV_KEYS = "disable_nav_keys";
    public static final String ENABLE_RECOVERY_UPDATE = "enable_recovery_update";
    public static final String UPDATE_RECOVERY_PROP = "persist.vendor.recovery_update";

    public static final int REQUEST_CODE_SETUP_WIFI = 0;
    public static final int REQUEST_CODE_SETUP_CAPTIVE_PORTAL= 4;
    public static final int REQUEST_CODE_SETUP_BLUETOOTH= 5;
    public static final int REQUEST_CODE_SETUP_BIOMETRIC = 7;
    public static final int REQUEST_CODE_SETUP_LOCKSCREEN = 9;
    public static final int REQUEST_CODE_RESTORE = 10;

    public static final int RADIO_READY_TIMEOUT = 10 * 1000;

    private boolean mIsRadioReady = false;
    private boolean mIgnoreSimLocale = false;

    private final Bundle mSettingsBundle = new Bundle();
    private final Handler mHandler = new Handler();

    private final Runnable mRadioTimeoutRunnable = () ->  mIsRadioReady = true;

    @Override
    public void onCreate() {
        super.onCreate();
        if (LOGV) {
            Log.v(TAG, "onCreate()");
        }
        NetworkMonitor.initInstance(this);
        PhoneMonitor.initInstance(this);
        SetupWizardUtils.disableComponentsForMissingFeatures(this);
        mHandler.postDelayed(mRadioTimeoutRunnable, SetupWizardApp.RADIO_READY_TIMEOUT);
    }

    public boolean isRadioReady() {
        return mIsRadioReady;
    }

    public void setRadioReady(boolean radioReady) {
        if (!mIsRadioReady && radioReady) {
            mHandler.removeCallbacks(mRadioTimeoutRunnable);
        }
        mIsRadioReady = radioReady;
    }

    public boolean ignoreSimLocale() {
        return mIgnoreSimLocale;
    }

    public void setIgnoreSimLocale(boolean ignoreSimLocale) {
        mIgnoreSimLocale = ignoreSimLocale;
    }

    public Bundle getSettingsBundle() {
        return mSettingsBundle;
    }
}
