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

package com.cyanogenmod.setupwizard.ui;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.android.setupwizardlib.util.WizardManagerHelper;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.util.EnableAccessibilityController;

public class WelcomeActivity extends BaseSetupWizardActivity {

    public static final String TAG = WelcomeActivity.class.getSimpleName();

    private static final String ACTION_EMERGENCY_DIAL = "com.android.phone.EmergencyDialer.DIAL";

    private View mRootView;
    private EnableAccessibilityController mEnableAccessibilityController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);
        mRootView = findViewById(R.id.root);
        setNextText(R.string.next);
        setBackText(R.string.emergency_call);
        setBackDrawable(null);
        mEnableAccessibilityController =
                EnableAccessibilityController.getInstance(getApplicationContext());
        mRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean consumeIntercept = mEnableAccessibilityController.onInterceptTouchEvent(event);
                boolean consumeTouch = mEnableAccessibilityController.onTouchEvent(event);
                return consumeIntercept && consumeTouch;
            }
        });
    }

    @Override
    public void onNavigateBack() {
        Intent intent = new Intent(ACTION_EMERGENCY_DIAL);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        ActivityOptions options =
                ActivityOptions.makeCustomAnimation(this,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out);
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onNavigateNext() {
        ActivityOptions options =
                ActivityOptions.makeCustomAnimation(this,
                        R.anim.slide_left,
                        0);
        Intent intent = WizardManagerHelper.getNextIntent(getIntent(), Activity.RESULT_OK);
        startActivityForResult(intent, 1, options.toBundle());
    }

}
