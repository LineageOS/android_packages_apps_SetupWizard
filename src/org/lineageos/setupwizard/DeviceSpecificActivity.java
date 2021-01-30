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

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.setupcompat.util.WizardManagerHelper;

import org.lineageos.setupwizard.util.SetupWizardUtils;

public class DeviceSpecificActivity extends BaseSetupWizardActivity {

    private static final String SUW_INTENT = "org.lineageos.settings.device.SUW_SETTINGS";
    private static final int CUSTOM_REQUEST_CODE = 90000;

    private boolean mGoToNextPage;
    private boolean mCameFromNextPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getResources().getBoolean(R.bool.config_hasDeviceSpecificActivity)) {
            goToNextPage();
            finish();
            return;
        }

        setNextText(R.string.next);
    }

    @Override
    public void onResume() {
        super.onResume();

        // After coming back from the called intent, onActivityResult gets called
        // before onResume and if we want to go to the next page the block below this would
        // start the external activity again
        if (mGoToNextPage) {
            mGoToNextPage = false;
            goToNextPage();
            return;
        }

        Intent intent = new Intent(SUW_INTENT);
        ComponentName name = intent.resolveActivity(getPackageManager());
        if (name != null) {
            startActivityForResult(intent, CUSTOM_REQUEST_CODE);
        } else if (!mCameFromNextPage) {
            // We aren't navigating backward, so move on
            goToNextPage();
        } else {
            // Came from the next activity, need to finish directly
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CUSTOM_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mGoToNextPage = true;
            } else {
                finish();
            }
        } else {
            mCameFromNextPage = true;
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setup_device_specific;
    }

    private void goToNextPage() {
        Intent intent = WizardManagerHelper.getNextIntent(getIntent(), RESULT_OK);
        nextAction(NEXT_REQUEST, intent);
    }
}
