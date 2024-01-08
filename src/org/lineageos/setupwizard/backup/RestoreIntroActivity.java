/*
 * Copyright (C) 2019-2020 The Calyx Institute
 *               2020-2024 The LineageOS Project
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

package org.lineageos.setupwizard.backup;

import static org.lineageos.setupwizard.SetupWizardApp.ACTION_RESTORE_FROM_BACKUP;
import static org.lineageos.setupwizard.SetupWizardApp.REQUEST_CODE_RESTORE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
<<<<<<< PATCH SET (889b49 SetupWizard: do not crash if seedvault is missing)
import android.widget.TextView;
import android.widget.Toast;
=======
>>>>>>> BASE      (5b44b2 Only finish Setup Wizard Activity when fully done)

import org.lineageos.setupwizard.R;
import org.lineageos.setupwizard.SubBaseActivity;

public class RestoreIntroActivity extends SubBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getGlifLayout().setDescriptionText(getString(R.string.intro_restore_subtitle,
                getString(R.string.os_name)));
    }

    @Override
    protected void onStartSubactivity() {
        setNextAllowed(true);
    }

    @Override
    protected void onNextPressed() {
        launchRestore();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.intro_restore_activity;
    }

    @Override
    protected int getTitleResId() {
        return R.string.intro_restore_title;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_restore;
    }

    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void launchRestore() {
        PackageManager pm = getPackageManager();
        boolean is_Restore_installed = isPackageInstalled("org.stevesoltys.seedvault", pm);
        if (is_Restore_installed) {
            Intent intent = new Intent(ACTION_RESTORE_FROM_BACKUP);
            startSubactivity(intent, REQUEST_CODE_RESTORE);
        } else {
            Toast.makeText(RestoreIntroActivity.this, "Seedvault app not found", Toast.LENGTH_LONG).show();
            onSkipPressed();
        }
    }

}
