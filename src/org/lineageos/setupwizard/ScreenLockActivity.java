/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017-2020 The LineageOS Project
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

import static org.lineageos.setupwizard.SetupWizardApp.ACTION_SETUP_LOCKSCREEN;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_DETAILS;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_TITLE;
import static org.lineageos.setupwizard.SetupWizardApp.REQUEST_CODE_SETUP_LOCKSCREEN;

import android.app.KeyguardManager;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import org.lineageos.setupwizard.util.SetupWizardUtils;

public class ScreenLockActivity extends SubBaseActivity {

    public static final String TAG = ScreenLockActivity.class.getSimpleName();

    @Override
    protected void onStartSubactivity() {
        if (isKeyguardSecure()) {
            Log.v(TAG, "Screen lock already set up; skipping ScreenLockActivity");
            nextAction(RESULT_OK);
            SetupWizardUtils.disableComponent(this, ScreenLockActivity.class);
            finish();
            return;
        }
        setNextAllowed(true);
        findViewById(R.id.setup_lockscreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLockscreenSetup();
            }
        });
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

    private boolean isKeyguardSecure() {
        return getSystemService(KeyguardManager.class).isKeyguardSecure();
    }
}
