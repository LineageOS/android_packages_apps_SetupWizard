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


import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;
import static android.content.pm.PackageManager.GET_ACTIVITIES;
import static android.content.pm.PackageManager.GET_RECEIVERS;
import static android.content.pm.PackageManager.GET_SERVICES;
import static android.content.pm.PackageManager.MATCH_DISABLED_COMPONENTS;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
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
        try {
            Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0);
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 0);
            final Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.HOME");
            resetComponentSets("com.cyanogenmod.setupwizard", GET_ACTIVITIES |
                    GET_RECEIVERS | GET_SERVICES | MATCH_DISABLED_COMPONENTS);
            resetComponentSets("com.google.android.setupwizard", GET_ACTIVITIES |
                    GET_RECEIVERS | GET_SERVICES | MATCH_DISABLED_COMPONENTS);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | intent.getFlags());
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error enabling setup " + e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    private void enableGoogleSetup() {
        try {
            Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0);
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 0);
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.HOME");
            resetComponentSets("com.google.android.setupwizard", GET_ACTIVITIES |
                    GET_RECEIVERS | GET_SERVICES | MATCH_DISABLED_COMPONENTS);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | intent.getFlags());
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "GMS not installed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void setSetupComplete() {
        Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 1);
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        disableComponentSets("com.cyanogenmod.setupwizard",  GET_ACTIVITIES |
                GET_RECEIVERS | GET_SERVICES);
        try {
            disableComponentSets("com.google.android.setupwizard",  GET_ACTIVITIES |
                    GET_RECEIVERS | GET_SERVICES);
        } catch (Exception e) {
            Toast.makeText(this, "GMS not installed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | intent.getFlags());
        startActivity(intent);
        finish();
    }

    private void disableComponentSets(String packageName, int flags) {
        setComponentListEnabledState(getComponentSets(packageName, flags),
                COMPONENT_ENABLED_STATE_DISABLED);
    }

    private void resetComponentSets(String packageName, int flags) {
        setComponentListEnabledState(getComponentSets(packageName, flags),
                COMPONENT_ENABLED_STATE_DEFAULT);
    }

    private void setComponentListEnabledState(List<ComponentName> componentNames,
                                                    int enabledState) {
        for (ComponentName componentName : componentNames) {
            Log.i("ManualTestActivity", " Changing component state " +
                    componentName.flattenToString() + " state=" + enabledState);
            setComponentEnabledState(componentName, enabledState);
        }
    }

    private void setComponentEnabledState(ComponentName componentName,
                                                int enabledState) {
        getPackageManager().setComponentEnabledSetting(componentName,
                enabledState, DONT_KILL_APP);
    }

    private List<ComponentName> getComponentSets(String packageName, int flags) {
        int i = 0;
        List<ComponentName> componentNames = new ArrayList();
        try {
            PackageInfo allInfo = getPackageManager()
                    .getPackageInfo(packageName, flags);
            if (allInfo != null) {
                if (allInfo.activities != null && (flags & GET_ACTIVITIES) != 0) {
                    for (ComponentInfo info : allInfo.activities) {
                        componentNames.add(new ComponentName(packageName, info.name));
                    }
                }
                if (allInfo.receivers != null && (flags & GET_RECEIVERS) != 0) {
                    for (ComponentInfo info2 : allInfo.receivers) {
                        componentNames.add(new ComponentName(packageName, info2.name));
                    }
                }
                if (allInfo.services != null && (flags & GET_SERVICES) != 0) {
                    ServiceInfo[] serviceInfoArr = allInfo.services;
                    int length = serviceInfoArr.length;
                    while (i < length) {
                        componentNames.add(new ComponentName(packageName, serviceInfoArr[i].name));
                        i++;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return componentNames;
    }

}