/*
 * Copyright (C) 2013 The CyanogenMod Project
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

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import com.cyanogenmod.setupwizard.SetupWizardApp;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.List;

public class SetupWizardUtils {

    private static final String TAG = SetupWizardUtils.class.getSimpleName();

    private static final String GOOGLE_SETUPWIZARD_PACKAGE = "com.google.android.setupwizard";

    private SetupWizardUtils(){}

    public static void tryEnablingWifi(Context context) {
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    public static void launchWifiSetup(Activity context) {
        SetupWizardUtils.tryEnablingWifi(context);
        Intent intent = new Intent(SetupWizardApp.ACTION_SETUP_WIFI);
        intent.putExtra(SetupWizardApp.EXTRA_FIRST_RUN, true);
        intent.putExtra(SetupWizardApp.EXTRA_ALLOW_SKIP, true);
        intent.putExtra(SetupWizardApp.EXTRA_USE_IMMERSIVE, true);
        intent.putExtra(SetupWizardApp.EXTRA_THEME, SetupWizardApp.EXTRA_MATERIAL_LIGHT);
        intent.putExtra(SetupWizardApp.EXTRA_AUTO_FINISH, false);
        ActivityOptions options =
                ActivityOptions.makeCustomAnimation(context,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out);
        context.startActivityForResult(intent,
                SetupWizardApp.REQUEST_CODE_SETUP_WIFI, options.toBundle());
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
            int phoneId = SubscriptionManager.getDefaultDataPhoneId();
            android.provider.Settings.Global.putInt(context.getContentResolver(),
                    android.provider.Settings.Global.MOBILE_DATA + phoneId, enabled ? 1 : 0);
            long subId = SubscriptionManager.getDefaultDataSubId();
            tm.setDataEnabledUsingSubId(subId, enabled);
        } else {
            android.provider.Settings.Global.putInt(context.getContentResolver(),
                    android.provider.Settings.Global.MOBILE_DATA, enabled ? 1 : 0);
            tm.setDataEnabled(enabled);
        }
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
        int simCount = SubscriptionManager.getActiveSubInfoCount();
        for (int i = 0; i < simCount; i++) {
            int simState = tm.getSimState(i);
            if (simState != TelephonyManager.SIM_STATE_ABSENT &&
                    simState != TelephonyManager.SIM_STATE_UNKNOWN) {
                return false;
            }
        }
        return true;
    }

    public static boolean isGuestUser(Context context) {
        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        return userManager.isGuestUser();
    }

    public static boolean isOwner() {
        return UserHandle.getCallingUserHandle().isOwner();
    }

    public static boolean hasGMS(Context context) {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) !=
                ConnectionResult.SERVICE_MISSING;
    }

    public static boolean accountExists(Context context, String accountType) {
        return AccountManager.get(context).getAccountsByType(accountType).length > 0;
    }

    public static void disableSetupWizards(Activity context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final PackageManager pm = context.getPackageManager();
        final List<ResolveInfo> resolveInfos =
                pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : resolveInfos) {
            if (GOOGLE_SETUPWIZARD_PACKAGE.equals(info.activityInfo.packageName)) {
                final ComponentName componentName =
                        new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
                pm.setComponentEnabledSetting(componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        0);
            }
        }
        pm.setComponentEnabledSetting(context.getComponentName(),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
