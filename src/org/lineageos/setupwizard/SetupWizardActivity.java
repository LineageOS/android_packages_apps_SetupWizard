/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017,2019 The LineageOS Project
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

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

import static org.lineageos.setupwizard.SetupWizardApp.ACTION_LOAD;
import static org.lineageos.setupwizard.SetupWizardApp.EXTRA_SCRIPT_URI;
import static org.lineageos.setupwizard.SetupWizardApp.LOGV;

import android.annotation.Nullable;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.android.setupcompat.util.WizardManagerHelper;

import org.lineageos.setupwizard.util.SetupWizardUtils;
import org.lineageos.setupwizard.wizardmanager.WizardManager;

public class SetupWizardActivity extends BaseSetupWizardActivity {
    private static final String TAG = SetupWizardActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LOGV) {
            Log.v(TAG, "onCreate savedInstanceState=" + savedInstanceState);
        }
        if (SetupWizardUtils.hasGMS(this)) {
            SetupWizardUtils.disableHome(this);
            if (SetupWizardUtils.isOwner()) {
                Settings.Global.putInt(getContentResolver(),
                        Settings.Global.ASSISTED_GPS_ENABLED, 1);
            }
            finish();
        } else if (WizardManagerHelper.isUserSetupComplete(this)
                && !SetupWizardUtils.isManagedProfile(this)) {
            SetupWizardUtils.finishSetupWizard(this);
            finish();
        } else {
            onSetupStart();
            SetupWizardUtils.enableComponent(this, WizardManager.class);
            Intent intent = new Intent(ACTION_LOAD);
            if (SetupWizardUtils.isOwner()) {
                intent.putExtra(EXTRA_SCRIPT_URI, getString(R.string.lineage_wizard_script_uri));
            } else if (SetupWizardUtils.isManagedProfile(this)) {
                intent.putExtra(EXTRA_SCRIPT_URI, getString(
                        R.string.lineage_wizard_script_managed_profile_uri));
            } else {
                intent.putExtra(EXTRA_SCRIPT_URI,
                        getString(R.string.lineage_wizard_script_user_uri));
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | FLAG_GRANT_READ_URI_PERMISSION);
            intent.setPackage(getPackageName());
            startActivity(intent);
            finish();
        }
    }
}
