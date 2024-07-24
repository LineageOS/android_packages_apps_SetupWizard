/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard;

import static com.google.android.setupcompat.util.ResultCodes.RESULT_ACTIVITY_NOT_FOUND;
import static com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP;

import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResult;

import org.lineageos.setupwizard.util.SetupWizardUtils;

public class BluetoothSetupActivity extends SubBaseActivity {

    public static final String TAG = BluetoothSetupActivity.class.getSimpleName();

    private static final String ACTION_CONNECT_INPUT =
            "com.google.android.intent.action.CONNECT_INPUT";

    private static final String INTENT_EXTRA_NO_INPUT_MODE = "no_input_mode";

    @Override
    protected void onStartSubactivity() {
        if (!SetupWizardUtils.hasLeanback(this) || SetupWizardUtils.isBluetoothDisabled()) {
            finishAction(RESULT_SKIP);
            return;
        }
        try {
            Intent intent = new Intent(ACTION_CONNECT_INPUT);
            intent.putExtra(INTENT_EXTRA_NO_INPUT_MODE, true);
            startSubactivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting bluetooth setup", e);
            finishAction(RESULT_OK);
            SetupWizardUtils.disableComponent(this, BluetoothSetupActivity.class);
        }
    }

    @Override
    protected void onSubactivityResult(ActivityResult activityResult) {
        Intent data = activityResult.getData();
        if (mIsSubactivityNotFound) {
            finishAction(RESULT_ACTIVITY_NOT_FOUND);
        } else if (data != null && data.getBooleanExtra("onBackPressed", false)) {
            onStartSubactivity();
        } else {
            nextAction(RESULT_OK, data);
        }
    }
}
