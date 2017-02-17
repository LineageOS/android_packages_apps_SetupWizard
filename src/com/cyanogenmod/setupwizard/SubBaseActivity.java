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


import static android.content.Intent.FLAG_ACTIVITY_FORWARD_RESULT;

import static com.android.setupwizardlib.util.ResultCodes.RESULT_ACTIVITY_NOT_FOUND;

import static com.cyanogenmod.setupwizard.SetupWizardApp.EXTRA_ACTION_ID;
import static com.cyanogenmod.setupwizard.SetupWizardApp.EXTRA_SCRIPT_URI;
import static com.cyanogenmod.setupwizard.SetupWizardApp.LOGV;

import android.annotation.NonNull;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public abstract class SubBaseActivity extends BaseSetupWizardActivity {

    public static final String TAG = SubBaseActivity.class.getSimpleName();

    private boolean mIsSubactivityNotFound = false;
    private int mRequestCode;

    protected abstract void onStartSubactivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (LOGV) {
            Log.d(TAG, "onCreate savedInstanceState=" + savedInstanceState);
        }
        super.onCreate(savedInstanceState);
        setNextText(R.string.skip);
        setNextAllowed(false);
        if (savedInstanceState == null) {
            onStartSubactivity();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mIsActivityVisible = true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("request_code", mRequestCode);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mRequestCode = savedInstanceState.getInt("request_code");
    }

    protected void startSubactivity(Intent subactivityIntent, int requestCode) {
        mRequestCode = requestCode;
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_SCRIPT_URI)) {
            subactivityIntent.putExtra(EXTRA_SCRIPT_URI, intent.getStringExtra(EXTRA_SCRIPT_URI));
            subactivityIntent.putExtra(EXTRA_ACTION_ID, intent.getStringExtra(EXTRA_ACTION_ID));
        }
        boolean activityForwardsResult =
                (subactivityIntent.getFlags() & FLAG_ACTIVITY_FORWARD_RESULT) != 0;
        if (activityForwardsResult) {
            try {
                startFirstRunActivity(subactivityIntent);
                setResultCode(RESULT_OK);
                finish();
            } catch (ActivityNotFoundException e) {
                Log.w(TAG, "activity not found; start next screen and finish; intent="
                        + intent);
                mIsSubactivityNotFound = true;
                nextAction(RESULT_ACTIVITY_NOT_FOUND);
                finish();
                return;
            }
        }
        startFirstRunActivityForResult(subactivityIntent, requestCode);
        mIsSubactivityNotFound = false;
        applyForwardTransition(getSubactivityPreviousTransition());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle extras = null;
        if (LOGV) {
            Log.v(TAG, "onActivityResult(" + getRequestName(requestCode) +
                    ", " + getResultName(requestCode, resultCode));
        }
        if (requestCode == mRequestCode) {
            StringBuilder append = new StringBuilder().append("subactivity result {")
                    .append(getRequestName(requestCode)).append(", ")
                    .append(getResultName(mRequestCode, resultCode)).append(", ");
            if (data != null) {
                extras = data.getExtras();
            }
            Log.i(TAG, append.append(extras).append("}").toString());
            onSubactivityResult(requestCode, resultCode, data);
        } else if (resultCode == RESULT_CANCELED) {
            onStartSubactivity();
            mIsGoingBack = true;
            applyBackwardTransition(getSubactivityNextTransition());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected void onSubactivityResult(int requestCode, int resultCode, Intent data) {
        if (LOGV) {
            StringBuilder append = new StringBuilder().append("onSubactivityResult(")
                    .append(getRequestName(requestCode)).append(", ")
                    .append(getResultName(requestCode, resultCode)).append(", ");
            Bundle extras = null;
            if (data != null) {
                extras = data.getExtras();
            }
            Log.v(TAG, append.append(extras).append(")").toString());
        }
        if (resultCode != RESULT_CANCELED) {
            applyForwardTransition(getSubactivityNextTransition());
            nextAction(resultCode, data);
        } else if (mIsSubactivityNotFound) {
            nextAction(RESULT_ACTIVITY_NOT_FOUND);
            finish();
        } else {
            applyBackwardTransition(getSubactivityPreviousTransition());
            finishAction(RESULT_CANCELED, data);
        }
    }

    protected int getSubactivityPreviousTransition() {
        return TRANSITION_ID_DEFAULT;
    }

    protected int getSubactivityNextTransition() {
        return TRANSITION_ID_DEFAULT;
    }

    @Override
    protected int getTransition() {
        return TRANSITION_ID_FADE;
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
