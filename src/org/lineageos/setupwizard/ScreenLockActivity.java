/*
 * Copyright (C) 2016 The CyanogenMod Project
 *               2017-2022 The LineageOS Project
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
<<<<<<< HEAD   (4190f0 Automatic translation import)
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_DETAILS;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_TITLE;
import static org.lineageos.setupwizard.SetupWizardApp.REQUEST_CODE_SETUP_LOCKSCREEN;
=======
>>>>>>> CHANGE (7ef422 Update deprecated code)

import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

<<<<<<< HEAD   (4190f0 Automatic translation import)
import org.lineageos.setupwizard.util.SetupWizardUtils;

=======
>>>>>>> CHANGE (7ef422 Update deprecated code)
public class ScreenLockActivity extends SubBaseActivity {

    public static final String TAG = ScreenLockActivity.class.getSimpleName();

    @Override
    protected void onNextPressed() {
        launchLockscreenSetup();
    }

    @Override
    protected void onStartSubactivity() {
        if (isKeyguardSecure()) {
            Log.v(TAG, "Screen lock already set up; skipping ScreenLockActivity");
            nextAction(RESULT_OK);
            SetupWizardUtils.disableComponent(this, ScreenLockActivity.class);
            finish();
            return;
        }
        getGlifLayout().setDescriptionText(getString(R.string.lockscreen_setup_summary));
        setNextAllowed(true);
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
<<<<<<< HEAD   (4190f0 Automatic translation import)
        intent.putExtra(EXTRA_TITLE,
                getString(R.string.settings_lockscreen_setup_title));
        intent.putExtra(EXTRA_DETAILS,
                getString(R.string.settings_lockscreen_setup_details));
        startSubactivity(intent, REQUEST_CODE_SETUP_LOCKSCREEN);
=======
        startSubactivity(intent);
>>>>>>> CHANGE (7ef422 Update deprecated code)
    }

    private boolean isKeyguardSecure() {
        return getSystemService(KeyguardManager.class).isKeyguardSecure();
    }
}
