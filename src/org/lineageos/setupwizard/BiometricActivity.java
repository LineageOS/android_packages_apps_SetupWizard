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

package org.lineageos.setupwizard;

import static org.lineageos.setupwizard.SetupWizardApp.ACTION_SETUP_BIOMETRIC;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_ALLOW_SKIP;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_AUTO_FINISH;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_DETAILS;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_FIRST_RUN;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_MATERIAL_LIGHT;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_THEME;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_TITLE;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_USE_IMMERSIVE;
import static org.lineageos.setupwizard.SetupWizardApp.REQUEST_CODE_SETUP_BIOMETRIC;

import android.content.Intent;
import android.view.View;

import com.google.android.setupcompat.util.WizardManagerHelper;

public class BiometricActivity extends SubBaseActivity {

    public static final String TAG = BiometricActivity.class.getSimpleName();

    @Override
    protected void onStartSubactivity() {
        setNextAllowed(true);
        findViewById(R.id.setup_biometric).setOnClickListener(view -> launchBiometricSetup());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setup_biometric;
    }

    @Override
    protected int getTitleResId() {
        return R.string.biometric_setup_title;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_fingerprint; // TODO?
    }

    private void launchBiometricSetup() {
        Intent intent = new Intent(ACTION_SETUP_BIOMETRIC);
        intent.putExtra(EXTRA_FIRST_RUN, true);
        intent.putExtra(EXTRA_ALLOW_SKIP, true);
        intent.putExtra(EXTRA_USE_IMMERSIVE, true);
        intent.putExtra(EXTRA_THEME, EXTRA_MATERIAL_LIGHT);
        intent.putExtra(EXTRA_AUTO_FINISH, false);
            /*intent.putExtra(LockPatternUtils.LOCKSCREEN_BIOMETRIC_FALLBACK, true);*/
        intent.putExtra(EXTRA_TITLE,
                getString(R.string.settings_biometric_setup_title));
        intent.putExtra(EXTRA_DETAILS,
                getString(R.string.settings_biometric_setup_details));
        intent.putExtra(WizardManagerHelper.EXTRA_IS_SETUP_FLOW, true);
        startSubactivity(intent, REQUEST_CODE_SETUP_BIOMETRIC);
    }

    @Override
    protected int getSubactivityNextTransition() {
        return TRANSITION_ID_SLIDE;
    }

}
