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

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.setupcompat.util.SystemBarHelper;

import org.lineageos.setupwizard.util.EnableAccessibilityController;

public class WelcomeActivity extends BaseSetupWizardActivity {

    public static final String TAG = WelcomeActivity.class.getSimpleName();

    private View mRootView;
    private EnableAccessibilityController mEnableAccessibilityController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemBarHelper.setBackButtonVisible(getWindow(), false);
        mRootView = findViewById(R.id.setup_wizard_layout);
        setNextText(R.string.start);
        setSkipText(R.string.emergency_call);
        findViewById(R.id.start).setOnClickListener(view -> onNextPressed());
        findViewById(R.id.emerg_dialer)
                .setOnClickListener(view -> startEmergencyDialer());
        findViewById(R.id.launch_accessibility)
                .setOnClickListener(view -> startAccessibilitySettings());
        mEnableAccessibilityController =
                EnableAccessibilityController.getInstance(getApplicationContext());
        mRootView.setOnTouchListener((v, event) ->
                mEnableAccessibilityController.onTouchEvent(event));

    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.welcome_activity;
    }
}
