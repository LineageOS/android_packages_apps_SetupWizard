/*
 * Copyright (C) 2016 The CyanogenMod Project
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

package com.cyanogenmod.setupwizard.wizardmanager;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;
import com.cyanogenmod.setupwizard.wizardmanager.WizardManager;

import android.annotation.Nullable;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import static com.cyanogenmod.setupwizard.wizardmanager.WizardManager.EXTRA_SCRIPT_URI;

import com.android.setupwizardlib.util.SystemBarHelper;

public class WizardActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            onSetupStart();
            Intent intent = new Intent(WizardManager.ACTION_LOAD);
            if (SetupWizardUtils.isOwner()) {
                intent.putExtra(EXTRA_SCRIPT_URI, getString(R.string.cm_wizard_script_uri));
            } else {
                intent.putExtra(EXTRA_SCRIPT_URI, getString(R.string.cm_wizard_script_user_uri));
            }
            startActivity(intent);
            finish();
        }
    }

    private void onSetupStart() {
        SystemBarHelper.hideSystemBars(getWindow());
        SetupWizardUtils.disableCaptivePortalDetection(getApplicationContext());
        SetupWizardUtils.disableNotifications(getApplicationContext());
        SetupWizardUtils.tryEnablingWifi(getApplicationContext());
    }
}
