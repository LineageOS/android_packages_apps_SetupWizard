/*
 * Copyright (C) 2013 The CyanogenMod Project
 * Copyright (C) 2017 The LineageOS Project
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

package com.cyanogenmod.setupwizard.util;

import static android.app.StatusBarManager.DISABLE_MASK;
import static android.app.StatusBarManager.DISABLE_NONE;
import static android.content.res.ThemeConfig.SYSTEM_DEFAULT;

import static com.cyanogenmod.setupwizard.SetupWizardApp.KEY_DETECT_CAPTIVE_PORTAL;

import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.UserManager;
import android.provider.Settings;
import android.service.persistentdata.PersistentDataBlockManager;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.cyanogenmod.setupwizard.SetupWizardApp;
import com.cyanogenmod.setupwizard.FinishActivity;
import com.cyanogenmod.setupwizard.LineageSettingsActivity;
import com.cyanogenmod.setupwizard.LocaleActivity;
import com.cyanogenmod.setupwizard.WelcomeActivity;
import com.cyanogenmod.setupwizard.wizardmanager.WizardActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import cyanogenmod.providers.CMSettings;

public class SetupWizardUtils {

    private static final String TAG = SetupWizardUtils.class.getSimpleName();

    private SetupWizardUtils(){}

    public static void tryEnablingWifi(Context context) {
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi != null && mWifi.isConnected();
    }

    public static boolean isMobileDataEnabled(Context context) {
        try {
            TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getDataEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public static void setMobileDataEnabled(Context context, boolean enabled) {
        TelephonyManager tm =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.isMultiSimEnabled()) {
            int phoneId = SubscriptionManager.from(context).getDefaultDataPhoneId();
            android.provider.Settings.Global.putInt(context.getContentResolver(),
                    android.provider.Settings.Global.MOBILE_DATA + phoneId, enabled ? 1 : 0);
            int subId = SubscriptionManager.getDefaultDataSubscriptionId();
            tm.setDataEnabled(subId, enabled);
        } else {
            android.provider.Settings.Global.putInt(context.getContentResolver(),
                    android.provider.Settings.Global.MOBILE_DATA, enabled ? 1 : 0);
            tm.setDataEnabled(enabled);
        }
    }

    public static boolean hasWifi(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI);
    }

    public static boolean hasTelephony(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    public static boolean isMultiSimDevice(Context context) {
        TelephonyManager tm =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.isMultiSimEnabled();
    }

    public static boolean isGSMPhone(Context context) {
        TelephonyManager tm =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int phoneType = tm.getPhoneType();
        return phoneType == TelephonyManager.PHONE_TYPE_GSM;
    }

    public static boolean isSimMissing(Context context) {
        TelephonyManager tm =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int simCount = SubscriptionManager.from(context).getDefaultDataPhoneId();
        for (int i = 0; i < simCount; i++) {
            int simState = tm.getSimState(i);
            if (simState != TelephonyManager.SIM_STATE_ABSENT &&
                    simState != TelephonyManager.SIM_STATE_UNKNOWN) {
                return false;
            }
        }
        return true;
    }

    public static boolean frpEnabled(Context context) {
        final PersistentDataBlockManager pdbManager = (PersistentDataBlockManager)
                context.getSystemService(Context.PERSISTENT_DATA_BLOCK_SERVICE);
        return pdbManager != null
                && pdbManager.getDataBlockSize() > 0
                && !pdbManager.getOemUnlockEnabled();
    }



    public static boolean isRadioReady(Context context, ServiceState state) {
        final SetupWizardApp setupWizardApp = (SetupWizardApp)context.getApplicationContext();
        if (setupWizardApp.isRadioReady()) {
            return true;
        } else {
            final boolean ready = state != null
                    && state.getState() != ServiceState.STATE_POWER_OFF;
            setupWizardApp.setRadioReady(ready);
            return ready;
        }

    }

    public static boolean isGuestUser(Context context) {
        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        return userManager.isGuestUser();
    }

    public static boolean isOwner() {
        return Binder.getCallingUserHandle().isOwner();
    }

    public static void disableCaptivePortalDetection(Context context) {
        Settings.Global.putInt(context.getContentResolver(), KEY_DETECT_CAPTIVE_PORTAL, 0);
    }

    public static void enableCaptivePortalDetection(Context context) {
        Settings.Global.putInt(context.getContentResolver(), KEY_DETECT_CAPTIVE_PORTAL, 1);
    }

    public static void disableNotifications(Context context) {
        StatusBarManager statusBarManager = context.getSystemService(StatusBarManager.class);
        if (statusBarManager != null) {
            statusBarManager.disable(DISABLE_MASK);
        } else {
            Log.w(SetupWizardApp.TAG,
                    "Skip disabling notfications - could not get StatusBarManager");
        }
    }

    public static void enableNotifications(Context context) {
        StatusBarManager statusBarManager = context.getSystemService(StatusBarManager.class);
        if(statusBarManager != null) {
            statusBarManager.disable(DISABLE_NONE);
        } else {
            Log.i(SetupWizardApp.TAG, "Skip enabling notfications - StatusBarManager is null");
        }
    }

    public static boolean hasGMS(Context context) {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) !=
                ConnectionResult.SERVICE_MISSING;
    }

    // We only care that one sim is inserted
    public static boolean isSimInserted(Context context) {
        TelephonyManager tm = TelephonyManager.from(context);
        int simSlotCount = tm.getSimCount();
        for (int i = 0; i < simSlotCount; i++) {
            int state;
            try {
                state = tm.getSimState(i);
            } catch (IllegalStateException ise) {
                Log.e(TAG, "Unable to get sim state from TelephonyManager");
                continue;
            }
            if (state != TelephonyManager.SIM_STATE_ABSENT
                    && state != TelephonyManager.SIM_STATE_UNKNOWN
                    && state != TelephonyManager.SIM_STATE_NOT_READY) {
                return true;
            }
        }
        return false;
    }

    // We only care that each slot has a sim
    public static boolean allSimsInserted(Context context) {
        TelephonyManager tm = TelephonyManager.from(context);
        int simSlotCount = tm.getSimCount();
        for (int i = 0; i < simSlotCount; i++) {
            int state = tm.getSimState(i);
            if (state == TelephonyManager.SIM_STATE_ABSENT) {
                return false;
            }
        }
        return simSlotCount == SubscriptionManager.from(context).getActiveSubscriptionInfoCount();
    }

    public static boolean isPackageInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void disableSetupWizard(Context context) {
        disableComponent(context, context.getPackageName(),
                WizardActivity.class.getName());
        disableComponent(context, context.getPackageName(),
                WelcomeActivity.class.getName());
        disableComponent(context, context.getPackageName(),
                LocaleActivity.class.getName());
        disableComponent(context, context.getPackageName(),
                LineageSettingsActivity.class.getName());
        disableComponent(context, context.getPackageName(),
                FinishActivity.class.getName());
    }

    public static void disableComponent(Context context, String packageName, String name) {
        disableComponent(context, new ComponentName(packageName, name));
    }

    public static void disableComponent(Context context, ComponentName component) {
        context.getPackageManager().setComponentEnabledSetting(component,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    public static boolean hasLeanback(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK);
    }

    public static boolean hasFingerprint(Context context) {
        FingerprintManager fingerprintManager = (FingerprintManager)
                context.getSystemService(Context.FINGERPRINT_SERVICE);
        return fingerprintManager.isHardwareDetected();
    }

    public static String getDefaultThemePackageName(Context context) {
        final String defaultThemePkg = CMSettings.Secure.getString(context.getContentResolver(),
                CMSettings.Secure.DEFAULT_THEME_PACKAGE);
        if (!TextUtils.isEmpty(defaultThemePkg)) {
            PackageManager pm = context.getPackageManager();
            try {
                if (pm.getPackageInfo(defaultThemePkg, 0) != null) {
                    return defaultThemePkg;
                }
            } catch (PackageManager.NameNotFoundException e) {
                // doesn't exist so system will be default
                Log.w(TAG, "Default theme " + defaultThemePkg + " not found");
            }
        }

        return SYSTEM_DEFAULT;
    }

    public static final ComponentName mTvwifisettingsActivity =
            new ComponentName("com.android.tv.settings",
                    "com.android.tv.settings.connectivity.setup.WifiSetupActivity");

    public static final ComponentName mTvAddAccessorySettingsActivity =
            new ComponentName("com.android.tv.settings",
                    "com.android.tv.settings.accessories.AddAccessoryActivity");
}
