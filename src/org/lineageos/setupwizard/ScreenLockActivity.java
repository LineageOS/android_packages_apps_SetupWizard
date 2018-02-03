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

import static com.cyanogenmod.setupwizard.SetupWizardApp.ACTION_SETUP_LOCKSCREEN;
import static com.cyanogenmod.setupwizard.SetupWizardApp.EXTRA_DETAILS;
import static com.cyanogenmod.setupwizard.SetupWizardApp.EXTRA_TITLE;
import static com.cyanogenmod.setupwizard.SetupWizardApp.REQUEST_CODE_SETUP_LOCKSCREEN;

import android.content.Intent;
import android.view.View;

public class ScreenLockActivity extends SubBaseActivity {

    public static final String TAG = ScreenLockActivity.class.getSimpleName();

    @Override
    protected void onStartSubactivity() {
        setNextAllowed(true);
        findViewById(R.id.setup_lockscreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLockscreenSetup();
            }
        });
    }

    @Override
    protected int getTransition() {
        return TRANSITION_ID_SLIDE;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setup_lockscreen;
    }

    @Override
    protected int getTitleResId() {
        return R.string.lockscreen_setup_title;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_lock_screen;
    }

    private void launchLockscreenSetup() {
        Intent intent = new Intent(ACTION_SETUP_LOCKSCREEN);
        intent.putExtra(EXTRA_TITLE,
                getString(R.string.settings_lockscreen_setup_title));
        intent.putExtra(EXTRA_DETAILS,
                getString(R.string.settings_lockscreen_setup_details));
        intent.putExtra(SetupWizardApp.EXTRA_ALLOW_SKIP, true);
        startSubactivity(intent, REQUEST_CODE_SETUP_LOCKSCREEN);
    }

    @Override
    protected int getSubactivityNextTransition() {
        return TRANSITION_ID_SLIDE;
    }

}
