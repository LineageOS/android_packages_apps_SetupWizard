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

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.setupwizardlib.util.WizardManagerHelper;
import com.cyanogenmod.setupwizard.R;

public class FingerprintActivity extends BaseSetupWizardActivity {

    public static final String TAG = FingerprintActivity.class.getSimpleName();

    private TextView mSetupFingerprint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_fingerprint);
        TextView title = (TextView) findViewById(android.R.id.title);
        title.setText(R.string.fingerprint_setup_title);
        ImageView icon = (ImageView) findViewById(R.id.header_icon);
        icon.setImageResource(R.drawable.ic_fingerprint);
        icon.setVisibility(View.VISIBLE);
        setNextText(R.string.skip);
        mSetupFingerprint = (TextView) findViewById(R.id.setup_fingerprint);
        mSetupFingerprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchFingerprintSetup();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (SetupWizardApp.REQUEST_CODE_SETUP_FINGERPRINT == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                onNavigateNext();
            }
        }
    }

    @Override
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        Intent intent = WizardManagerHelper.getNextIntent(getIntent(), Activity.RESULT_OK);
        startActivityForResult(intent, 1);
    }

    private void launchFingerprintSetup() {
        Intent intent = new Intent(SetupWizardApp.ACTION_SETUP_FINGERPRINT);
        intent.putExtra(SetupWizardApp.EXTRA_FIRST_RUN, true);
        intent.putExtra(SetupWizardApp.EXTRA_ALLOW_SKIP, true);
        intent.putExtra(SetupWizardApp.EXTRA_USE_IMMERSIVE, true);
        intent.putExtra(SetupWizardApp.EXTRA_THEME, SetupWizardApp.EXTRA_MATERIAL_LIGHT);
        intent.putExtra(SetupWizardApp.EXTRA_AUTO_FINISH, false);
            /*intent.putExtra(LockPatternUtils.LOCKSCREEN_FINGERPRINT_FALLBACK, true);*/
        intent.putExtra(SetupWizardApp.EXTRA_TITLE,
                getString(R.string.settings_fingerprint_setup_title));
        intent.putExtra(SetupWizardApp.EXTRA_DETAILS,
                getString(R.string.settings_fingerprint_setup_details));
        ActivityOptions options =
                ActivityOptions.makeCustomAnimation(this,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out);
        startActivityForResult(intent, SetupWizardApp.REQUEST_CODE_SETUP_FINGERPRINT,
                options.toBundle());
    }

}
