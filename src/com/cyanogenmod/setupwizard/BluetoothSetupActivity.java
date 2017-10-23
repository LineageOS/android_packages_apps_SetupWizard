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

import static com.cyanogenmod.setupwizard.SetupWizardApp.REQUEST_CODE_SETUP_BLUETOOTH;

import android.content.Intent;
import android.util.Log;

import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

public class BluetoothSetupActivity extends SubBaseActivity {

    public static final String TAG = BluetoothSetupActivity.class.getSimpleName();

    private static final String ACTION_CONNECT_INPUT =
            "com.google.android.intent.action.CONNECT_INPUT";

    private static final String INTENT_EXTRA_NO_INPUT_MODE = "no_input_mode";

    @Override
    protected void onStartSubactivity() {
        try {
            Intent intent = new Intent();
            intent.setComponent(SetupWizardUtils.mTvAddAccessorySettingsActivity);
            intent.setAction(ACTION_CONNECT_INPUT);
            intent.putExtra(INTENT_EXTRA_NO_INPUT_MODE, true);
            startActivityForResult(intent, REQUEST_CODE_SETUP_BLUETOOTH);
        } catch (Exception e) {
            Log.e(TAG, "Error starting bluetooth setup", e);
            nextAction(RESULT_OK);
            SetupWizardUtils.disableComponent(this, BluetoothSetupActivity.class);
            finish();
        }
    }

    @Override
    protected int getSubactivityNextTransition() {
        nextAction(RESULT_OK);
        return TRANSITION_ID_SLIDE;
    }
}
