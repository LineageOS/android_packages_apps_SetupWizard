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
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import java.util.List;

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
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final PackageManager pm = getPackageManager();
        ComponentName componentName = new ComponentName("com.cyanogenmod.setupwizard", "com.cyanogenmod.setupwizard.ui.SetupWizardActivity");
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
        try {
            final List<ResolveInfo> resolveInfos =
                    pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo info : resolveInfos) {
                if ("com.google.android.setupwizard".equals(info.activityInfo.packageName)) {
                    final ComponentName cn =
                            new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
                    pm.setComponentEnabledSetting(cn,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            0);
                }
            }
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
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            final PackageManager pm = getPackageManager();
            try {
                final List<ResolveInfo> resolveInfos =
                        pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo info : resolveInfos) {
                    if ("com.google.android.setupwizard".equals(info.activityInfo.packageName)) {
                        final ComponentName cn =
                                new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
                        pm.setComponentEnabledSetting(cn,
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                0);
                    }
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | intent.getFlags());
                startActivity(intent);
                finish();
            } catch (Exception e) {
                Toast.makeText(this, "GMS not installed", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setSetupComplete() {
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 1);
        Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final PackageManager pm = getPackageManager();
        ComponentName componentName = new ComponentName("com.cyanogenmod.setupwizard", "com.cyanogenmod.setupwizard.ui.SetupWizardActivity");
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        try {
            final List<ResolveInfo> resolveInfos =
                    pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo info : resolveInfos) {
                if ("com.google.android.setupwizard".equals(info.activityInfo.packageName)) {
                    final ComponentName cn =
                            new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
                    pm.setComponentEnabledSetting(cn,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            0);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "GMS not installed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | intent.getFlags());
        startActivity(intent);
        finish();
    }

}