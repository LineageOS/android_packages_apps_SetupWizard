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

package org.lineageos.setupwizard.util;

import static android.app.StatusBarManager.DISABLE_NONE;
import static android.app.StatusBarManager.DISABLE_NOTIFICATION_ALERTS;
import static android.app.StatusBarManager.DISABLE_SEARCH;
import static android.content.Context.MODE_PRIVATE;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;
import static android.content.pm.PackageManager.GET_ACTIVITIES;
import static android.content.pm.PackageManager.GET_RECEIVERS;
import static android.content.pm.PackageManager.GET_SERVICES;

import static org.lineageos.setupwizard.SetupWizardApp.KEY_DETECT_CAPTIVE_PORTAL;
import static org.lineageos.setupwizard.SetupWizardApp.LOGV;

import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.hardware.face.FaceManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Binder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.lineageos.setupwizard.BluetoothSetupActivity;
import org.lineageos.setupwizard.ChooseDataSimActivity;
import org.lineageos.setupwizard.BiometricActivity;
import org.lineageos.setupwizard.MobileDataActivity;
import org.lineageos.setupwizard.SetupWizardApp;
import org.lineageos.setupwizard.SimMissingActivity;
import org.lineageos.setupwizard.WifiSetupActivity;
import org.lineageos.setupwizard.wizardmanager.WizardManager;

import org.lineageos.internal.util.PackageManagerUtils;

import java.util.ArrayList;
import java.util.List;

public class SetupWizardUtils {

    private static final String TAG = SetupWizardUtils.class.getSimpleName();

    private static final String GMS_PACKAGE = "com.google.android.gms";
    private static final String GMS_SUW_PACKAGE = "com.google.android.setupwizard";
    private static final String GMS_TV_SUW_PACKAGE = "com.google.android.tungsten.setupwraith";

    private static final String PROP_BUILD_DATE = "ro.build.date.utc";

    private SetupWizardUtils(){}

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences("SetupWizardPrefs", MODE_PRIVATE);
    }

    public static boolean isMobileDataEnabled(Context context) {
        try {
            TelephonyManager tm = context.getSystemService(TelephonyManager.class);
            return tm.getDataEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public static void setMobileDataEnabled(Context context, boolean enabled) {
        TelephonyManager tm = context.getSystemService(TelephonyManager.class);
        if (tm.isMultiSimEnabled()) {
            int phoneId = SubscriptionManager.from(context).getDefaultDataPhoneId();
            android.provider.Settings.Global.putInt(context.getContentResolver(),
                    android.provider.Settings.Global.MOBILE_DATA + phoneId, enabled ? 1 : 0);
            int subId = SubscriptionManager.getDefaultDataSubscriptionId();
            tm.createForSubscriptionId(subId).setDataEnabled(enabled);
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

    public static boolean isOwner() {
        return UserHandle.myUserId() == 0;
    }

    public static void disableCaptivePortalDetection(Context context) {
        Settings.Global.putInt(context.getContentResolver(), KEY_DETECT_CAPTIVE_PORTAL, 0);
    }

    public static void enableCaptivePortalDetection(Context context) {
        Settings.Global.putInt(context.getContentResolver(), KEY_DETECT_CAPTIVE_PORTAL, 1);
    }

    public static void disableStatusBar(Context context) {
        StatusBarManager statusBarManager = context.getSystemService(StatusBarManager.class);
        if (statusBarManager != null) {
            statusBarManager.disable(DISABLE_NOTIFICATION_ALERTS | DISABLE_SEARCH
            );
        } else {
            Log.w(SetupWizardApp.TAG,
                    "Skip disabling notfications - could not get StatusBarManager");
        }
    }

    public static void enableStatusBar(Context context) {
        StatusBarManager statusBarManager = context.getSystemService(StatusBarManager.class);
        if(statusBarManager != null) {
            Log.i(SetupWizardApp.TAG, "Enabling notfications - StatusBarManager");
            statusBarManager.disable(DISABLE_NONE);
        } else {
            Log.i(SetupWizardApp.TAG, "Skip enabling notfications - StatusBarManager is null");
        }
    }

    public static boolean hasGMS(Context context) {
        String gmsSuwPackage = hasLeanback(context) ? GMS_TV_SUW_PACKAGE : GMS_SUW_PACKAGE;

        if (PackageManagerUtils.isAppInstalled(context, GMS_PACKAGE) &&
                PackageManagerUtils.isAppInstalled(context, gmsSuwPackage)) {
            PackageManager packageManager = context.getPackageManager();
            if (LOGV) {
                Log.v(TAG, GMS_SUW_PACKAGE + " state = " +
                        packageManager.getApplicationEnabledSetting(gmsSuwPackage));
            }
            return packageManager.getApplicationEnabledSetting(gmsSuwPackage) !=
                    COMPONENT_ENABLED_STATE_DISABLED;
        }
        return false;
    }

    public static boolean isPackageInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void finishSetupWizard(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Settings.Global.putInt(contentResolver,
                Settings.Global.DEVICE_PROVISIONED, 1);
        Settings.Secure.putInt(contentResolver,
                Settings.Secure.USER_SETUP_COMPLETE, 1);
        if (hasLeanback(context)) {
            Settings.Secure.putInt(contentResolver,
                    Settings.Secure.TV_USER_SETUP_COMPLETE, 1);
        }

        disableComponent(context, WizardManager.class);
        disableHome(context);
        context.sendStickyBroadcastAsUser(
                new Intent(SetupWizardApp.ACTION_FINISHED),
                Binder.getCallingUserHandle());
        disableComponentSets(context, GET_RECEIVERS | GET_SERVICES);
    }

    public static boolean isEthernetConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.
            getSystemService(Context.CONNECTIVITY_SERVICE);

        return (cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_ETHERNET);
    }

    public static boolean hasLeanback(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK);
    }

    public static boolean hasBiometric(Context context) {
        return hasFingerprint(context) || hasFace(context);
    }

    public static boolean hasFingerprint(Context context) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            FingerprintManager fingerprintManager = (FingerprintManager)
                    context.getSystemService(Context.FINGERPRINT_SERVICE);
            return fingerprintManager.isHardwareDetected();
        } else {
            return false;
        }
    }

    public static boolean hasFace(Context context) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_FACE)) {
            FaceManager faceManager = (FaceManager)
                    context.getSystemService(Context.FACE_SERVICE);
            return faceManager.isHardwareDetected();
        } else {
            return false;
        }
    }

    public static boolean simMissing() {
        return PhoneMonitor.getInstance().simMissing();
    }

    public static void disableComponentsForMissingFeatures(Context context) {
        if (!hasLeanback(context)) {
            disableComponent(context, BluetoothSetupActivity.class);
        }
        if (!hasBiometric(context)) {
            disableComponent(context, BiometricActivity.class);
        }
        if (!hasTelephony(context)) {
            disableComponent(context, MobileDataActivity.class);
            disableComponent(context, SimMissingActivity.class);
            disableComponent(context, ChooseDataSimActivity.class);
        }
        if (!SetupWizardUtils.isMultiSimDevice(context)) {
            disableComponent(context, ChooseDataSimActivity.class);
        } else if (simMissing()) {
            disableComponent(context, MobileDataActivity.class);
            disableComponent(context, ChooseDataSimActivity.class);
        }
        if (!SetupWizardUtils.hasWifi(context) ||
            isEthernetConnected(context)) {
            disableComponent(context, WifiSetupActivity.class);
        }
    }

    public static void disableHome(Context context) {
        ComponentName homeComponent = getHomeComponent(context);
        if (homeComponent != null) {
            setComponentEnabledState(context, homeComponent, COMPONENT_ENABLED_STATE_DISABLED);
        } else {
            Log.w(TAG, "Home component not found. Skipping.");
        }
    }

    public static ComponentName getHomeComponent(Context context) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.setPackage(context.getPackageName());
        ComponentName comp = intent.resolveActivity(context.getPackageManager());
        if (SetupWizardApp.LOGV) {
            Log.v(TAG, "resolveActivity for intent=" + intent + " returns " + comp);
        }
        return comp;
    }

    public static void disableComponentSets(Context context, int flags) {
        setComponentListEnabledState(context, getComponentSets(context, flags),
                COMPONENT_ENABLED_STATE_DISABLED);
    }

    public static void disableComponent(Context context, Class cls) {
        setComponentEnabledState(context, new ComponentName(context, cls),
                COMPONENT_ENABLED_STATE_DISABLED);
    }

    public static void enableComponent(Context context, Class<?> cls) {
        setComponentEnabledState(context, new ComponentName(context, cls),
                COMPONENT_ENABLED_STATE_ENABLED);
    }

    public static void resetComponentSets(Context context, int flags) {
        setComponentListEnabledState(context, getComponentSets(context, flags),
                COMPONENT_ENABLED_STATE_DEFAULT);
    }

    public static void resetComponent(Context context, Class<?> cls) {
        setComponentEnabledState(context, new ComponentName(context, cls),
                COMPONENT_ENABLED_STATE_DEFAULT);
    }


    public static void setComponentEnabledState(Context context, ComponentName componentName,
            int enabledState) {
        context.getPackageManager().setComponentEnabledSetting(componentName,
                enabledState, DONT_KILL_APP);
    }

    public static void setComponentListEnabledState(Context context,
            List<ComponentName> componentNames, int enabledState) {
        for (ComponentName componentName : componentNames) {
            setComponentEnabledState(context, componentName, enabledState);
        }
    }

    public static List<ComponentName> getComponentSets(Context context, int flags) {
        int i = 0;
        List<ComponentName> componentNames = new ArrayList();
        try {
            PackageInfo allInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), flags);
            if (allInfo != null) {
                if (allInfo.activities != null && (flags & GET_ACTIVITIES) != 0) {
                    for (ComponentInfo info : allInfo.activities) {
                        componentNames.add(new ComponentName(context, info.name));
                    }
                }
                if (allInfo.receivers != null && (flags & GET_RECEIVERS) != 0) {
                    for (ComponentInfo info2 : allInfo.receivers) {
                        componentNames.add(new ComponentName(context, info2.name));
                    }
                }
                if (allInfo.services != null && (flags & GET_SERVICES) != 0) {
                    ServiceInfo[] serviceInfoArr = allInfo.services;
                    int length = serviceInfoArr.length;
                    while (i < length) {
                        componentNames.add(new ComponentName(context, serviceInfoArr[i].name));
                        i++;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return componentNames;
    }


    public static final ComponentName mTvwifisettingsActivity =
            new ComponentName("com.android.tv.settings",
                    "com.android.tv.settings.connectivity.setup.WifiSetupActivity");

    public static final ComponentName mTvAddAccessorySettingsActivity =
            new ComponentName("com.android.tv.settings",
                    "com.android.tv.settings.accessories.AddAccessoryActivity");

    public static long getBuildDateTimestamp() {
        return SystemProperties.getLong(PROP_BUILD_DATE, 0);
    }
}
