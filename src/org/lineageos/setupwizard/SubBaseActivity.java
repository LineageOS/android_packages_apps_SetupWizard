/*
 * Copyright (C) 2017-2024 The LineageOS Project
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

import static com.google.android.setupcompat.util.ResultCodes.RESULT_ACTIVITY_NOT_FOUND;

import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_ACTION_ID;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_SCRIPT_URI;
import static org.lineageos.setupwizard.SetupWizardApp.LOGV;

import android.annotation.NonNull;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResult;

public abstract class SubBaseActivity extends BaseSetupWizardActivity {

    public static final String TAG = SubBaseActivity.class.getSimpleName();

    private boolean mIsSubactivityNotFound = false;

    protected abstract void onStartSubactivity();

    protected void onSubactivityCanceled(Intent data) {
        // Do nothing.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (LOGV) {
            Log.d(TAG, "onCreate savedInstanceState=" + savedInstanceState);
        }
        super.onCreate(savedInstanceState);
        setNextAllowed(false);
        if (savedInstanceState == null) {
            onStartSubactivity();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    protected void startSubactivity(Intent subactivityIntent) {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_SCRIPT_URI)) {
            subactivityIntent.putExtra(EXTRA_SCRIPT_URI, intent.getStringExtra(EXTRA_SCRIPT_URI));
            subactivityIntent.putExtra(EXTRA_ACTION_ID, intent.getStringExtra(EXTRA_ACTION_ID));
        }
        try {
            startActivityForResult(subactivityIntent);
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "activity not found; start next screen and finish; intent="
                    + intent);
            mIsSubactivityNotFound = true;
            finishAction(RESULT_ACTIVITY_NOT_FOUND);
        }
    }

    @Override
    protected void onActivityResult(ActivityResult activityResult) {
        super.onActivityResult(activityResult);
        int resultCode = activityResult.getResultCode();
        Intent data = activityResult.getData();
        if (resultCode != RESULT_CANCELED) {
            nextAction(resultCode, data);
        } else if (mIsSubactivityNotFound) {
            finishAction(RESULT_ACTIVITY_NOT_FOUND);
        } else if (data != null && data.getBooleanExtra("onBackPressed", false)) {
            onStartSubactivity();
        } else {
            finishAction(RESULT_CANCELED);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setup_loading_page;
    }

    @Override
    protected int getTitleResId() {
        return R.string.loading;
    }
}
