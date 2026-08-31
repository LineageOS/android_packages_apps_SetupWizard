/*
 * SPDX-FileCopyrightText: 2013 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.util

import android.app.StatusBarManager
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.om.IOverlayManager
import android.content.pm.PackageManager
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
import android.content.pm.PackageManager.DONT_KILL_APP
import android.content.pm.PackageManager.GET_ACTIVITIES
import android.hardware.biometrics.BiometricManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.ServiceManager
import android.os.SystemProperties
import android.os.UserHandle
import android.os.UserManager
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.PHONE_TYPE_GSM
import android.util.Log
import com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP
import java.io.File
import lineageos.hardware.LineageHardwareManager
import lineageos.providers.LineageSettings
import org.lineageos.setupwizard.SetupWizardApp
import org.lineageos.setupwizard.SetupWizardApp.Companion.DISABLE_NAV_KEYS
import org.lineageos.setupwizard.SetupWizardApp.Companion.ENABLE_RECOVERY_UPDATE
import org.lineageos.setupwizard.SetupWizardApp.Companion.KEY_SEND_METRICS
import org.lineageos.setupwizard.SetupWizardApp.Companion.LOGV
import org.lineageos.setupwizard.SetupWizardApp.Companion.NAVIGATION_OPTION_KEY
import org.lineageos.setupwizard.SetupWizardApp.Companion.UPDATE_RECOVERY_PROP
import org.lineageos.setupwizard.base.BaseSetupWizardActivity

object SetupWizardUtils {

    private const val TAG = "SetupWizardUtils"

    private const val GMS_PACKAGE = "com.google.android.gms"
    private const val GMS_SUW_PACKAGE = "com.google.android.setupwizard"
    private const val GMS_TV_SUW_PACKAGE = "com.google.android.tungsten.setupwraith"
    private const val UPDATER_PACKAGE = "org.lineageos.updater"

    private const val UPDATE_RECOVERY_EXEC = "/vendor/bin/install-recovery.sh"
    private const val CONFIG_HIDE_RECOVERY_UPDATE = "config_hideRecoveryUpdate"
    private const val PROP_BUILD_DATE = "ro.build.date.utc"

    fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences("SetupWizardPrefs", MODE_PRIVATE)

    fun hasWifi(context: Context): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI)

    fun hasTelephony(context: Context): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)

    fun hasRecoveryUpdater(context: Context): Boolean {
        if (!File(UPDATE_RECOVERY_EXEC).exists()) {
            return false
        }
        val featureHidden =
            runCatching {
                    val updaterResources =
                        context.packageManager.getResourcesForApplication(UPDATER_PACKAGE)
                    val res =
                        updaterResources.getIdentifier(
                            CONFIG_HIDE_RECOVERY_UPDATE,
                            "bool",
                            UPDATER_PACKAGE,
                        )
                    updaterResources.getBoolean(res)
                }
                .getOrDefault(false)
        return !featureHidden
    }

    fun isOwner(): Boolean = UserHandle.myUserId() == 0

    fun isManagedProfile(context: Context): Boolean =
        context.getSystemService(UserManager::class.java).isManagedProfile()

    fun disableStatusBar(context: Context): StatusBarManager? {
        val statusBarManager = context.getSystemService(StatusBarManager::class.java)
        statusBarManager?.also {
            if (LOGV) {
                Log.v(SetupWizardApp.TAG, "Disabling status bar")
            }
            it.setDisabledForSetup(true)
        } ?: Log.w(SetupWizardApp.TAG, "Skip disabling status bar - could not get StatusBarManager")
        return statusBarManager
    }

    fun enableStatusBar() {
        SetupWizardApp.statusBarManager?.also {
            if (LOGV) {
                Log.v(SetupWizardApp.TAG, "Enabling status bar")
            }
            it.setDisabledForSetup(false)
        } ?: Log.w(SetupWizardApp.TAG, "Skip enabling status bar - could not get StatusBarManager")
    }

    fun hasGMS(context: Context): Boolean {
        val gmsSuwPackage = if (hasLeanback(context)) GMS_TV_SUW_PACKAGE else GMS_SUW_PACKAGE
        if (
            isPackageInstalled(context, GMS_PACKAGE) && isPackageInstalled(context, gmsSuwPackage)
        ) {
            val packageManager = context.packageManager
            if (LOGV) {
                Log.v(
                    TAG,
                    "$GMS_SUW_PACKAGE state = " +
                        packageManager.getApplicationEnabledSetting(gmsSuwPackage),
                )
            }
            return packageManager.getApplicationEnabledSetting(gmsSuwPackage) !=
                COMPONENT_ENABLED_STATE_DISABLED
        }
        return false
    }

    fun isPackageInstalled(context: Context, packageName: String): Boolean =
        runCatching { context.packageManager.getPackageInfo(packageName, GET_ACTIVITIES) }.isSuccess

    fun finishSetupWizard(context: BaseSetupWizardActivity) {
        if (LOGV) {
            Log.v(TAG, "finishSetupWizard")
        }
        val contentResolver = context.contentResolver
        Settings.Global.putInt(contentResolver, Settings.Global.DEVICE_PROVISIONED, 1)
        val userSetupComplete =
            Settings.Secure.getInt(contentResolver, Settings.Secure.USER_SETUP_COMPLETE, 0)
        if (userSetupComplete != 0 && !isManagedProfile(context)) {
            Log.e(
                TAG,
                "finishSetupWizard, but userSetupComplete=$userSetupComplete! " +
                    "This should not happen!",
            )
        }
        Settings.Secure.putInt(contentResolver, Settings.Secure.USER_SETUP_COMPLETE, 1)
        if (hasLeanback(context)) {
            Settings.Secure.putInt(contentResolver, Settings.Secure.TV_USER_SETUP_COMPLETE, 1)
        }

        handleEnableMetrics(context)
        handleNavKeys(context)
        handleRecoveryUpdate()
        handleNavigationOption()
        WallpaperManager.getInstance(context).forgetLoadedWallpaper()
        disableHome(context)
        enableStatusBar()
        context.finishAffinity()
        context.nextAction(RESULT_SKIP)
        Log.i(TAG, "Setup complete!")
    }

    fun isBluetoothDisabled(): Boolean =
        SystemProperties.getBoolean("config.disable_bluetooth", false)

    fun isNetworkConnectedToInternetViaEthernet(context: Context): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val networkCapabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        return networkCapabilities != null &&
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun hasLeanback(context: Context): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)

    fun hasBiometric(context: Context): Boolean {
        val biometricManager = context.getSystemService(BiometricManager::class.java)
        return when (
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        ) {
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /** Disable the Home component, which is presumably SetupWizardActivity at this time. */
    fun disableHome(context: Context) {
        val homeComponent = getHomeComponent(context)
        if (homeComponent != null) {
            setComponentEnabledState(context, homeComponent, COMPONENT_ENABLED_STATE_DISABLED)
        } else {
            Log.w(TAG, "Home component not found. Skipping.")
        }
    }

    private fun getHomeComponent(context: Context): ComponentName? {
        val intent =
            Intent("android.intent.action.MAIN").apply {
                addCategory("android.intent.category.HOME")
                setPackage(context.packageName)
            }
        val comp = intent.resolveActivity(context.packageManager)
        if (LOGV) {
            Log.v(TAG, "resolveActivity for intent=$intent returns $comp")
        }
        return comp
    }

    fun disableComponent(context: Context, cls: Class<*>) {
        setComponentEnabledState(
            context,
            ComponentName(context, cls),
            COMPONENT_ENABLED_STATE_DISABLED,
        )
    }

    fun enableComponent(context: Context, cls: Class<*>) {
        setComponentEnabledState(
            context,
            ComponentName(context, cls),
            COMPONENT_ENABLED_STATE_ENABLED,
        )
    }

    fun setComponentEnabledState(
        context: Context,
        componentName: ComponentName,
        enabledState: Int,
    ) {
        context.packageManager.setComponentEnabledSetting(
            componentName,
            enabledState,
            DONT_KILL_APP,
        )
    }

    private fun handleEnableMetrics(context: Context) {
        val privacyData = SetupWizardApp.settingsBundle
        if (privacyData.containsKey(KEY_SEND_METRICS)) {
            LineageSettings.Secure.putInt(
                context.contentResolver,
                LineageSettings.Secure.STATS_COLLECTION,
                if (privacyData.getBoolean(KEY_SEND_METRICS)) 1 else 0,
            )
        }
    }

    private fun handleNavKeys(context: Context) {
        val settingsBundle = SetupWizardApp.settingsBundle
        if (settingsBundle.containsKey(DISABLE_NAV_KEYS)) {
            writeDisableNavkeysOption(context, settingsBundle.getBoolean(DISABLE_NAV_KEYS))
        }
    }

    private fun handleRecoveryUpdate() {
        val settingsBundle = SetupWizardApp.settingsBundle
        if (settingsBundle.containsKey(ENABLE_RECOVERY_UPDATE)) {
            val update = settingsBundle.getBoolean(ENABLE_RECOVERY_UPDATE)
            SystemProperties.set(UPDATE_RECOVERY_PROP, update.toString())
        }
    }

    private fun handleNavigationOption() {
        val settingsBundle = SetupWizardApp.settingsBundle
        if (settingsBundle.containsKey(NAVIGATION_OPTION_KEY)) {
            val overlayManager =
                IOverlayManager.Stub.asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE))
            val selectedNavMode = settingsBundle.getString(NAVIGATION_OPTION_KEY)
            runCatching {
                overlayManager.setEnabledExclusiveInCategory(
                    selectedNavMode,
                    UserHandle.USER_CURRENT,
                )
            }
        }
    }

    private fun writeDisableNavkeysOption(context: Context, enabled: Boolean) {
        val virtualKeysEnabled =
            LineageSettings.System.getIntForUser(
                context.contentResolver,
                LineageSettings.System.FORCE_SHOW_NAVBAR,
                0,
                UserHandle.USER_CURRENT,
            ) != 0
        if (enabled != virtualKeysEnabled) {
            LineageSettings.System.putIntForUser(
                context.contentResolver,
                LineageSettings.System.FORCE_SHOW_NAVBAR,
                if (enabled) 1 else 0,
                UserHandle.USER_CURRENT,
            )
            val hardware = LineageHardwareManager.getInstance(context)
            hardware.set(LineageHardwareManager.FEATURE_KEY_DISABLE, enabled)
        }
    }

    fun getBuildDateTimestamp(): Long = SystemProperties.getLong(PROP_BUILD_DATE, 0)

    fun simMissing(context: Context): Boolean {
        val tm = context.getSystemService(TelephonyManager::class.java)
        val sm = context.getSystemService(SubscriptionManager::class.java)
        if (tm == null || sm == null) {
            return false
        }
        val hasGsmSim =
            sm.activeSubscriptionInfoList?.any { sub ->
                val simState = tm.getSimState(sub.simSlotIndex)
                if (LOGV) {
                    Log.v(TAG, "getSimState(${sub.subscriptionId}) == $simState")
                }
                simState != -1 &&
                    tm.createForSubscriptionId(sub.subscriptionId).currentPhoneType ==
                        PHONE_TYPE_GSM
            } ?: false
        return !hasGsmSim
    }
}
