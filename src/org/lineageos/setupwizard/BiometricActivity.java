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

import static org.lineageos.setupwizard.SetupWizardApp.ACTION_SETUP_BIOMETRIC;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_DETAILS;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_TITLE;
import static org.lineageos.setupwizard.SetupWizardApp.REQUEST_CODE_SETUP_BIOMETRIC;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.setupcompat.util.WizardManagerHelper;
import com.google.android.setupdesign.util.ThemeHelper;

import org.lineageos.setupwizard.util.SetupWizardUtils;

public class BiometricActivity extends SubBaseActivity {

    public static final String TAG = BiometricActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final TextView setupBiometricSummary = (TextView) findViewById(
                R.id.setup_biometric_summary);
        final TextView setupAddBiometric = (TextView) findViewById(R.id.setup_add_biometric);
        if (SetupWizardUtils.hasFace(this)) {
            setupBiometricSummary.setText(getString(R.string.face_setup_summary));
            setupAddBiometric.setText(R.string.face_setup_add_face);
        } else {
            setupBiometricSummary.setText(getString(R.string.fingerprint_setup_summary));
            setupAddBiometric.setText(R.string.fingerprint_setup_add_fingerprint);
        }
    }

    @Override
    protected void onNextPressed() {
        launchBiometricSetup();
    }

    @Override
    protected void onStartSubactivity() {
        setNextAllowed(true);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setup_biometric;
    }

    @Override
    protected int getTitleResId() {
        if (SetupWizardUtils.hasFace(this)) {
            return R.string.face_setup_title;
        }
        return R.string.fingerprint_setup_title;
    }

    @Override
    protected int getIconResId() {
        if (SetupWizardUtils.hasFace(this)) {
            return R.drawable.ic_face;
        }
        return R.drawable.ic_fingerprint;
    }

    private void launchBiometricSetup() {
        Intent intent = new Intent(ACTION_SETUP_BIOMETRIC);
        intent.putExtra(WizardManagerHelper.EXTRA_THEME, ThemeHelper.THEME_GLIF_V3_LIGHT);
        intent.putExtra(EXTRA_TITLE,
                getString(getTitleResId()));
        intent.putExtra(EXTRA_DETAILS,
                getString(R.string.settings_biometric_setup_details));
        intent.putExtra(WizardManagerHelper.EXTRA_IS_SETUP_FLOW, true);
        startSubactivity(intent, REQUEST_CODE_SETUP_BIOMETRIC);
    }
}
