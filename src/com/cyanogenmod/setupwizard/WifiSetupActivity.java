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

package com.cyanogenmod.setupwizard;

import static com.cyanogenmod.setupwizard.SetupWizardApp.ACTION_SETUP_WIFI;
import static com.cyanogenmod.setupwizard.SetupWizardApp.EXTRA_MATERIAL_LIGHT;
import static com.cyanogenmod.setupwizard.SetupWizardApp.REQUEST_CODE_SETUP_WIFI;

import android.content.Intent;

import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

public class WifiSetupActivity extends SubBaseActivity {

    public static final String TAG = WifiSetupActivity.class.getSimpleName();


    @Override
    protected void onStartSubactivity() {
        tryEnablingWifi();
        Intent intent = new Intent(ACTION_SETUP_WIFI);
        if (SetupWizardUtils.hasLeanback(this)) {
            intent.setComponent(SetupWizardUtils.mTvwifisettingsActivity);
        }
        intent.putExtra(SetupWizardApp.EXTRA_FIRST_RUN, true);
        intent.putExtra(SetupWizardApp.EXTRA_ALLOW_SKIP, true);
        intent.putExtra(SetupWizardApp.EXTRA_USE_IMMERSIVE, true);
        intent.putExtra(SetupWizardApp.EXTRA_THEME, EXTRA_MATERIAL_LIGHT);
        intent.putExtra(SetupWizardApp.EXTRA_AUTO_FINISH, false);
        startSubactivity(intent, REQUEST_CODE_SETUP_WIFI);
    }

    @Override
    protected int getSubactivityNextTransition() {
        return TRANSITION_ID_SLIDE;
    }

}
