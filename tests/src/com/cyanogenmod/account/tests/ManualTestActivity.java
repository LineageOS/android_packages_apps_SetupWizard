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

package com.cyanogenmod.setupwizard.tests;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import cyanogenmod.providers.CMSettings;

public class ManualTestActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cmaccount_test);

        findViewById(R.id.enable_setup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableSetup();
            }
        });
        findViewById(R.id.enable_google_setup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableGoogleSetup();
            }
        });
        findViewById(R.id.setup_complete_flag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSetupComplete();
            }
        });
    }

    private void enableSetup() {
        Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0);
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 0);
        CMSettings.Secure.putInt(getContentResolver(),
                CMSettings.Secure.CM_SETUP_WIZARD_COMPLETED, 0);
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        final PackageManager pm = getPackageManager();
        ComponentName componentName = new ComponentName("com.cyanogenmod.setupwizard",
                "com.cyanogenmod.setupwizard.ui.SetupWizardActivity");
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        componentName = new ComponentName("com.cyanogenmod.setupwizard",
                "com.cyanogenmod.setupwizard.setup.FinishSetupReceiver");
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        pm.clearApplicationUserData("com.cyanogenmod.setupwizard", null);
        ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        am.killBackgroundProcesses("com.cyanogenmod.setupwizard");
        try {
            PackageInfo packageInfo = getPackageManager()
                    .getPackageInfo("com.google.android.setupwizard",
                            PackageManager.GET_ACTIVITIES |
                                    PackageManager.GET_RECEIVERS | PackageManager.GET_SERVICES);
            enableComponentArray(packageInfo.activities);
            enableComponentArray(packageInfo.services);
            enableComponentArray(packageInfo.receivers);
            pm.clearApplicationUserData("com.google.android.setupwizard", null);
        } catch (Exception e) {
            Toast.makeText(this, "GMS not installed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | intent.getFlags());
        startActivity(intent);
        finish();
    }

    private void enableGoogleSetup() {
        try {
            Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0);
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 0);
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.HOME");
            final PackageManager pm = getPackageManager();
            PackageInfo packageInfo = this.getPackageManager()
                    .getPackageInfo("com.google.android.setupwizard",
                            PackageManager.GET_ACTIVITIES |
                                    PackageManager.GET_RECEIVERS | PackageManager.GET_SERVICES);
            enableComponentArray(packageInfo.activities);
            enableComponentArray(packageInfo.services);
            enableComponentArray(packageInfo.receivers);
            pm.clearApplicationUserData("com.google.android.setupwizard", null);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | intent.getFlags());
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "GMS not installed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void setSetupComplete() {
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 1);
        CMSettings.Secure.putInt(getContentResolver(),
                CMSettings.Secure.CM_SETUP_WIZARD_COMPLETED, 1);
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        final PackageManager pm = getPackageManager();
        ComponentName componentName = new ComponentName("com.cyanogenmod.setupwizard",
                "com.cyanogenmod.setupwizard.ui.SetupWizardActivity");
        pm.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        am.killBackgroundProcesses("com.cyanogenmod.setupwizard");
        try {
            PackageInfo packageInfo = this.getPackageManager()
                    .getPackageInfo("com.google.android.setupwizard",
                            PackageManager.GET_ACTIVITIES |
                                    PackageManager.GET_RECEIVERS | PackageManager.GET_SERVICES);
            enableComponentArray(packageInfo.activities);
            enableComponentArray(packageInfo.services);
            enableComponentArray(packageInfo.receivers);
            pm.clearApplicationUserData("com.google.android.setupwizard", null);
        } catch (Exception e) {
            Toast.makeText(this, "GMS not installed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | intent.getFlags());
        startActivity(intent);
        finish();
    }

    private void enableComponentArray(ComponentInfo[] components) {
        if(components != null) {
            ComponentInfo[] componentInfos = components;
            for(int i = 0; i < componentInfos.length; i++) {
                enableComponent(componentInfos[i].packageName, componentInfos[i].name);
            }
        }
    }

    private void enableComponent(String packageName, String name) {
        enableComponent(new ComponentName(packageName, name));
    }

    private void enableComponent(ComponentName component) {
        getPackageManager().setComponentEnabledSetting(component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 1);
    }

}