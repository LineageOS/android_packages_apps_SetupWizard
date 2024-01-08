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

import android.content.ComponentName;
import android.content.Intent;

import org.lineageos.setupwizard.util.SetupWizardUtils;

public class DeviceSpecificActivity extends WrapperSubBaseActivity {

    private static final String ACTION_SETUP_DEVICE = "org.lineageos.settings.device.SUW_SETTINGS";

    protected void onStartSubactivity() {
        Intent intent = new Intent(ACTION_SETUP_DEVICE);
        ComponentName name = intent.resolveActivity(getPackageManager());
        if (name != null) {
            startSubactivity(intent);
        } else {
            SetupWizardUtils.disableComponent(this, DeviceSpecificActivity.class);
            finishAction(RESULT_OK);
        }
    }
}
