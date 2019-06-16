/*
 * Copyright (C) 2019 The LineageOS Project
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

import static org.lineageos.setupwizard.SetupWizardApp.ACTION_SETUP_AUTOBRIGHTNESS;
import static org.lineageos.setupwizard.SetupWizardApp.ACTION_SETUP_NIGHT_LIGHT;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_DETAILS;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_TITLE;
import static org.lineageos.setupwizard.SetupWizardApp.REQUEST_CODE_SETUP_AUTOBRIGHTNESS;
import static org.lineageos.setupwizard.SetupWizardApp.REQUEST_CODE_SETUP_NIGHT_LIGHT;

import android.app.KeyguardManager;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import org.lineageos.setupwizard.util.SetupWizardUtils;

public class DisplayExtrasActivity extends SubBaseActivity {

    public static final String TAG = DisplayExtrasActivity.class.getSimpleName();

    @Override
    protected void onStartSubactivity() {
        if (!hasDisplayExtras()) {
            Log.v(TAG, "No display extras; skipping DisplayExtrasActivity");
            nextAction(RESULT_OK);
            SetupWizardUtils.disableComponent(this, DisplayExtrasActivity.class);
            finish();
            return;
        }
        setNextAllowed(true);
        findViewById(R.id.setup_screen_brightness).setOnClickListener(view -> launchAutoBrightnessSetup());
        findViewById(R.id.setup_night_light).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchNightLightSetup();
            }
        });
    }

    @Override
    protected int getTransition() {
        return TRANSITION_ID_SLIDE;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setup_display;
    }

    @Override
    protected int getTitleResId() {
        return R.string.display_setup_title;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_display;
    }

    private void launchAutoBrightnessSetup() {
        Intent intent = new Intent(ACTION_SETUP_AUTOBRIGHTNESS);
/*
        intent.putExtra(EXTRA_TITLE,
                getString(R.string.settings_display_setup_title));
        intent.putExtra(EXTRA_DETAILS,
                getString(R.string.settings_display_setup_details));
*/
        intent.putExtra(SetupWizardApp.EXTRA_ALLOW_SKIP, true);
        startSubactivity(intent, REQUEST_CODE_SETUP_AUTOBRIGHTNESS);
    }

    private void launchNightLightSetup() {
        Intent intent = new Intent(ACTION_SETUP_NIGHT_LIGHT);
/*
        intent.putExtra(EXTRA_TITLE,
                getString(R.string.settings_display_setup_title));
        intent.putExtra(EXTRA_DETAILS,
                getString(R.string.settings_display_setup_details));
*/
        intent.putExtra(SetupWizardApp.EXTRA_ALLOW_SKIP, true);
        startSubactivity(intent, REQUEST_CODE_SETUP_NIGHT_LIGHT);
    }

    @Override
    protected int getSubactivityNextTransition() {
        return TRANSITION_ID_SLIDE;
    }

    private boolean hasDisplayExtras() {
        return true;
    }
}
