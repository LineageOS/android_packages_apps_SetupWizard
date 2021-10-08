/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017-2021 The LineageOS Project
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

package org.lineageos.setupwizard;

import static org.lineageos.setupwizard.SetupWizardApp.ACTION_SETUP_NETWORK;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_ENABLE_NEXT_ON_CONNECT;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_PREFS_SET_BACK_TEXT;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_PREFS_SHOW_BUTTON_BAR;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_PREFS_SHOW_SKIP;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_PREFS_SHOW_SKIP_TV;
import static org.lineageos.setupwizard.SetupWizardApp.REQUEST_CODE_SETUP_NETWORK;

import android.content.Intent;

import com.google.android.setupcompat.util.WizardManagerHelper;

import org.lineageos.setupwizard.util.SetupWizardUtils;

public class NetworkSetupActivity extends WrapperSubBaseActivity {

    public static final String TAG = NetworkSetupActivity.class.getSimpleName();

    @Override
    protected void onStartSubactivity() {
        tryEnablingWifi();
        Intent intent = new Intent(ACTION_SETUP_NETWORK);
        if (SetupWizardUtils.hasLeanback(this)) {
            intent.setComponent(SetupWizardUtils.sTvWifiSetupSettingsActivity);
        }
        intent.putExtra(WizardManagerHelper.EXTRA_IS_SETUP_FLOW, true);
        intent.putExtra(EXTRA_PREFS_SHOW_BUTTON_BAR, true);
        intent.putExtra(EXTRA_PREFS_SHOW_SKIP, true);
        intent.putExtra(EXTRA_PREFS_SHOW_SKIP_TV, true);
        intent.putExtra(EXTRA_PREFS_SET_BACK_TEXT, (String) null);
        intent.putExtra(EXTRA_ENABLE_NEXT_ON_CONNECT, true);
        startSubactivity(intent, REQUEST_CODE_SETUP_NETWORK);
    }
}
