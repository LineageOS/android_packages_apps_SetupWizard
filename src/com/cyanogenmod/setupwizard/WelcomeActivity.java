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

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.cyanogenmod.setupwizard.util.EnableAccessibilityController;

public class WelcomeActivity extends BaseSetupWizardActivity {

    public static final String TAG = WelcomeActivity.class.getSimpleName();

    private View mRootView;
    private EnableAccessibilityController mEnableAccessibilityController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRootView = findViewById(R.id.root);
        setNextText(R.string.next);
        setBackText(R.string.emergency_call);
        setBackDrawable(null);
        mEnableAccessibilityController =
                EnableAccessibilityController.getInstance(getApplicationContext());
        mRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mEnableAccessibilityController.onTouchEvent(event);
            }
        });
    }

    @Override
    public void onBackPressed() {}

    @Override
    public void onNavigateBack() {
        startEmergencyDialer();
    }

    @Override
    protected int getTransition() {
        return TRANSITION_ID_SLIDE;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.welcome_activity;
    }
}
