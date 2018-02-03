/*
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

package com.cyanogenmod.setupwizard;

import static android.content.pm.PackageManager.GET_ACTIVITIES;
import static android.content.pm.PackageManager.GET_RECEIVERS;
import static android.content.pm.PackageManager.GET_SERVICES;
import static android.content.pm.PackageManager.MATCH_DISABLED_COMPONENTS;

import static com.cyanogenmod.setupwizard.SetupWizardApp.LOGV;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

import java.util.List;

public class SetupWizardTestActivity extends Activity {

    private static final String TAG = SetupWizardTestActivity.class.getSimpleName();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LOGV) {
            Log.v(TAG, "onCreate savedInstanceState=" + savedInstanceState);
        }
        if (SetupWizardUtils.isOwner()) {
            Settings.Global.putInt(getContentResolver(), "device_provisioned", 0);
        }
        Settings.Secure.putInt(getContentResolver(), "user_setup_complete", 0);
        SetupWizardUtils.resetComponentSets(this, GET_ACTIVITIES |
                GET_RECEIVERS | GET_SERVICES | MATCH_DISABLED_COMPONENTS);
        forgetAllWifi();
        Intent setupIntent = new Intent("android.intent.action.MAIN")
                .addCategory("android.intent.category.HOME")
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        SetupWizardUtils.disableComponentsForMissingFeatures(this);
        startActivity(setupIntent);
        finish();
    }

    private void forgetAllWifi() {
        WifiManager wm = getSystemService(WifiManager.class);
        if (wm != null) {
            List<WifiConfiguration> configs = wm.getConfiguredNetworks();
            if (configs != null) {
                for (WifiConfiguration config : configs) {
                    wm.forget(config.networkId, null);
                }
            }
        }
    }
}
