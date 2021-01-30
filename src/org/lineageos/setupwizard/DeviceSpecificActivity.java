/*
 * Copyright (C) 2021 The LineageOS Project
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
 * limitations under the License
 */

package org.lineageos.setupwizard;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.setupcompat.util.WizardManagerHelper;

import org.lineageos.setupwizard.util.SetupWizardUtils;

public class DeviceSpecificActivity extends BaseSetupWizardActivity {

    private static final String ACTION_SETUP_DEVICE = "org.lineageos.settings.device.SUW_SETTINGS";
    private static final int REQUEST_CODE_SETUP_DEVICE = 90000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onStartSubactivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SETUP_DEVICE) {
            if (resultCode == RESULT_OK) {
                goToNextPage();
            } else {
                finish();
            }
        } else if (resultCode == RESULT_CANCELED) {
            onStartSubactivity();
            applyBackwardTransition(TRANSITION_ID_NONE);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setup_device_specific;
    }

    private void onStartSubactivity() {
        Intent intent = new Intent(ACTION_SETUP_DEVICE);
        ComponentName name = intent.resolveActivity(getPackageManager());
        if (name != null) {
            applyForwardTransition(TRANSITION_ID_SLIDE);
            startActivityForResult(intent, REQUEST_CODE_SETUP_DEVICE);
        } else {
            SetupWizardUtils.disableComponent(this, DeviceSpecificActivity.class);
            goToNextPage();
            finish();
        }
    }

    private void goToNextPage() {
        applyForwardTransition(TRANSITION_ID_SLIDE);
        Intent intent = WizardManagerHelper.getNextIntent(getIntent(), Activity.RESULT_OK);
        nextAction(NEXT_REQUEST, intent);
    }
}
