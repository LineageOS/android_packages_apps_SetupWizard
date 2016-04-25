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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.setupwizardlib.util.WizardManagerHelper;
import com.cyanogenmod.setupwizard.R;

public class ScreenLockActivity extends BaseSetupWizardActivity {

    public static final String TAG = ScreenLockActivity.class.getSimpleName();

    private Button mSetupLockscreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_lockscreen);
        TextView title = (TextView) findViewById(android.R.id.title);
        title.setText(R.string.lockscreen_setup_title);
        ImageView icon = (ImageView) findViewById(R.id.header_icon);
        icon.setImageResource(R.drawable.ic_lock_screen);
        icon.setVisibility(View.VISIBLE);
        setNextText(R.string.skip);
        mSetupLockscreen = (Button) findViewById(R.id.setup_lockscreen);
        mSetupLockscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLockscreenSetup();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (SetupWizardApp.REQUEST_CODE_SETUP_LOCKSCREEN == requestCode) {
            if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_FIRST_USER) {
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

    private void launchLockscreenSetup() {
        Intent intent = new Intent(SetupWizardApp.ACTION_SETUP_LOCKSCREEN);
        intent.putExtra(SetupWizardApp.EXTRA_TITLE,
                getString(R.string.settings_lockscreen_setup_title));
        intent.putExtra(SetupWizardApp.EXTRA_DETAILS,
                getString(R.string.settings_lockscreen_setup_details));
        ActivityOptions options =
                ActivityOptions.makeCustomAnimation(this,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out);
        startActivityForResult(intent, SetupWizardApp.REQUEST_CODE_SETUP_LOCKSCREEN,
                options.toBundle());
    }

}
