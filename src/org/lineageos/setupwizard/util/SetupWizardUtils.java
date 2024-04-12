/*
 * SPDX-FileCopyrightText: 2013 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.util;

import static android.content.Context.MODE_PRIVATE;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;
import static android.content.pm.PackageManager.GET_ACTIVITIES;
import static android.content.pm.PackageManager.GET_RECEIVERS;
import static android.content.pm.PackageManager.GET_SERVICES;
import static android.telephony.TelephonyManager.PHONE_TYPE_GSM;
import static android.telephony.TelephonyManager.SIM_STATE_ABSENT;

import static com.android.internal.telephony.PhoneConstants.LTE_ON_CDMA_TRUE;
import static com.android.internal.telephony.PhoneConstants.LTE_ON_CDMA_UNKNOWN;

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
import android.content.res.Resources;
import android.hardware.biometrics.BiometricManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Binder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.sysprop.TelephonyProperties;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;

import org.lineageos.internal.util.PackageManagerUtils;
import org.lineageos.setupwizard.BiometricActivity;
import org.lineageos.setupwizard.BluetoothSetupActivity;
import org.lineageos.setupwizard.NetworkSetupActivity;
import org.lineageos.setupwizard.ScreenLockActivity;
import org.lineageos.setupwizard.SetupWizardApp;
import org.lineageos.setupwizard.SetupWizardExitWorker;
import org.lineageos.setupwizard.SimMissingActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SetupWizardUtils {

    private static final String TAG = SetupWizardUtils.class.getSimpleName();

    private static final String GMS_PACKAGE = "com.google.android.gms";
    private static final String GMS_SUW_PACKAGE = "com.google.android.setupwizard";
    private static final String GMS_TV_SUW_PACKAGE = "com.google.android.tungsten.setupwraith";
    private static final String UPDATER_PACKAGE = "org.lineageos.updater";

    private static final String UPDATE_RECOVERY_EXEC = "/vendor/bin/install-recovery.sh";
    private static final String CONFIG_HIDE_RECOVERY_UPDATE = "config_hideRecoveryUpdate";
    private static final String PROP_BUILD_DATE = "ro.build.date.utc";

    private SetupWizardUtils() {
    }

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences("SetupWizardPrefs", MODE_PRIVATE);
    }

    public static boolean hasWifi(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI);
    }

    public static boolean hasTelephony(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    public static boolean hasRecoveryUpdater(Context context) {
        boolean fileExists = new File(UPDATE_RECOVERY_EXEC).exists();
        if (!fileExists) {
            return false;
        }

        boolean featureHidden = false;
        try {
            PackageManager pm = context.getPackageManager();
            Resources updaterResources = pm.getResourcesForApplication(UPDATER_PACKAGE);
            int res = updaterResources.getIdentifier(
                    CONFIG_HIDE_RECOVERY_UPDATE, "bool", UPDATER_PACKAGE);
            featureHidden = updaterResources.getBoolean(res);
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException ignored) {
        }
        return !featureHidden;
    }

    public static boolean isOwner() {
        return UserHandle.myUserId() == 0;
    }

    public static boolean isManagedProfile(Context context) {
        return context.getSystemService(UserManager.class).isManagedProfile();
    }

    public static StatusBarManager disableStatusBar(Context context) {
        StatusBarManager statusBarManager = context.getSystemService(StatusBarManager.class);
        if (statusBarManager != null) {
            if (LOGV) {
                Log.v(SetupWizardApp.TAG, "Disabling status bar");
            }
            statusBarManager.setDisabledForSetup(true);
        } else {
            Log.w(SetupWizardApp.TAG,
                    "Skip disabling status bar - could not get StatusBarManager");
        }
        return statusBarManager;
    }

    public static void enableStatusBar() {
        StatusBarManager statusBarManager = SetupWizardApp.getStatusBarManager();
        if (statusBarManager != null) {
            if (LOGV) {
                Log.v(SetupWizardApp.TAG, "Enabling status bar");
            }
            statusBarManager.setDisabledForSetup(false);

            // Session must be destroyed if it's not used anymore
            statusBarManager = null;
        } else {
            Log.w(SetupWizardApp.TAG,
                    "Skip enabling status bar - could not get StatusBarManager");
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

    public static void startSetupWizardExitProcedure(Context context) {
        try {
            WorkManager.getInstance(context).enqueue(new OneTimeWorkRequest.Builder(
                    SetupWizardExitWorker.class).setExpedited(
                    OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).build());
        } catch (IllegalArgumentException e) {
            // finishSetupWizard -- called by the exit worker -- disables components at the end,
            // including the WorkManager component required here, so this is likely an error finding
            // that component. The worker only needs to run once. We can assume it already has.
            Log.w(TAG, "Could not start SetupWizardExitWorker. It has likely already run.", e);
            return;
        }
    }

    public static void finishSetupWizard(Context context) {
        if (LOGV) {
            Log.v(TAG, "finishSetupWizard");
        }
        ContentResolver contentResolver = context.getContentResolver();
        Settings.Global.putInt(contentResolver,
                Settings.Global.DEVICE_PROVISIONED, 1);
        final int userSetupComplete =
                Settings.Secure.getInt(contentResolver, Settings.Secure.USER_SETUP_COMPLETE, 0);
        if (userSetupComplete != 0 && !SetupWizardUtils.isManagedProfile(context)) {
            Log.e(TAG, "finishSetupWizard, but userSetupComplete=" + userSetupComplete + "! "
                    + "This should not happen!");
        }
        Settings.Secure.putInt(contentResolver,
                Settings.Secure.USER_SETUP_COMPLETE, 1);
        if (hasLeanback(context)) {
            Settings.Secure.putInt(contentResolver,
                    Settings.Secure.TV_USER_SETUP_COMPLETE, 1);
        }

        disableComponentsAndSendFinishedBroadcast(context);
    }

    private static void disableComponentsAndSendFinishedBroadcast(Context context) {
        if (LOGV) {
            Log.v(TAG, "Disabling Setup Wizard components and sending FINISHED broadcast.");
        }
        disableHome(context);
        context.sendStickyBroadcastAsUser(
                new Intent(SetupWizardApp.ACTION_FINISHED),
                Binder.getCallingUserHandle());
        disableComponentSets(context, GET_RECEIVERS | GET_SERVICES);
        // Note: The WizardManager component is disabled when the WizardManager exits,
        // which happens when FinishActivity calls nextAction while completing.
    }

    public static boolean isBluetoothDisabled() {
        return SystemProperties.getBoolean("config.disable_bluetooth", false);
    }

    private static boolean isNetworkConnectedToInternetViaEthernet(Context context) {
        ConnectivityManager cm = context.getSystemService(ConnectivityManager.class);
        NetworkCapabilities networkCapabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return networkCapabilities != null &&
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    public static boolean hasLeanback(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK);
    }

    public static boolean hasBiometric(Context context) {
        BiometricManager biometricManager = context.getSystemService(BiometricManager.class);
        int result = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK);
        return switch (result) {
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
                    BiometricManager.BIOMETRIC_SUCCESS -> true;
            default -> false;
        };
    }

    public static void disableComponentsForMissingFeatures(Context context) {
        if (!hasLeanback(context) || isBluetoothDisabled()) {
            disableComponent(context, BluetoothSetupActivity.class);
        }
        if (!hasBiometric(context)) {
            disableComponent(context, BiometricActivity.class);
        } else {
            disableComponent(context, ScreenLockActivity.class);
        }
        if (!hasTelephony(context)) {
            disableComponent(context, SimMissingActivity.class);
        }
        if ((!hasWifi(context) && !hasTelephony(context)) ||
                isNetworkConnectedToInternetViaEthernet(context)) {
            disableComponent(context, NetworkSetupActivity.class);
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
        if (LOGV) {
            Log.v(TAG, "resolveActivity for intent=" + intent + " returns " + comp);
        }
        return comp;
    }

    public static void disableComponentSets(Context context, int flags) {
        setComponentListEnabledState(context, getComponentSets(context, flags),
                COMPONENT_ENABLED_STATE_DISABLED);
    }

    public static void disableComponent(Context context, Class<?> cls) {
        setComponentEnabledState(context, new ComponentName(context, cls),
                COMPONENT_ENABLED_STATE_DISABLED);
    }

    public static void enableComponent(Context context, Class<?> cls) {
        setComponentEnabledState(context, new ComponentName(context, cls),
                COMPONENT_ENABLED_STATE_ENABLED);
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
        List<ComponentName> componentNames = new ArrayList<>();
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
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return componentNames;
    }

    public static final ComponentName sTvWifiSetupSettingsActivity =
            new ComponentName("com.android.tv.settings",
                    "com.android.tv.settings.connectivity.setup.WifiSetupActivity");

    public static final ComponentName sTvAddAccessorySettingsActivity =
            new ComponentName("com.android.tv.settings",
                    "com.android.tv.settings.accessories.AddAccessoryActivity");

    public static long getBuildDateTimestamp() {
        return SystemProperties.getLong(PROP_BUILD_DATE, 0);
    }

    public static boolean simMissing(Context context) {
        TelephonyManager tm = context.getSystemService(TelephonyManager.class);
        SubscriptionManager sm = context.getSystemService(SubscriptionManager.class);
        if (tm == null || sm == null) {
            return false;
        }
        List<SubscriptionInfo> subs = sm.getActiveSubscriptionInfoList();
        if (subs != null) {
            for (SubscriptionInfo sub : subs) {
                int simState = tm.getSimState(sub.getSimSlotIndex());
                if (LOGV) {
                    Log.v(TAG, "getSimState(" + sub.getSubscriptionId() + ") == " + simState);
                }
                if (simState != -1) {
                    final int subId = sub.getSubscriptionId();
                    final TelephonyManager subTm = tm.createForSubscriptionId(subId);
                    if (isGSM(subTm) || isLteOnCdma(subTm, subId)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean isGSM(TelephonyManager subTelephonyManager) {
        return subTelephonyManager.getCurrentPhoneType() == PHONE_TYPE_GSM;
    }

    private static boolean isLteOnCdma(TelephonyManager subTelephonyManager, int subId) {
        final int lteOnCdmaMode = subTelephonyManager.getLteOnCdmaMode(subId);
        if (lteOnCdmaMode == LTE_ON_CDMA_UNKNOWN) {
            return TelephonyProperties.lte_on_cdma_device().orElse(LTE_ON_CDMA_UNKNOWN)
                    == LTE_ON_CDMA_TRUE;
        }
        return lteOnCdmaMode == LTE_ON_CDMA_TRUE;
    }
}
