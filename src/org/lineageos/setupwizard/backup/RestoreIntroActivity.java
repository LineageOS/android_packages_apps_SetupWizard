/*
 * Copyright (C) 2019-2020 The Calyx Institute
 * Copyright (C) 2020 The LineageOS Project
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

import android.app.Activity;
import android.content.Intent;

import com.google.android.setupcompat.util.WizardManagerHelper;

import org.lineageos.setupwizard.R;
import org.lineageos.setupwizard.SubBaseActivity;

import static org.lineageos.setupwizard.SetupWizardApp.ACTION_RESTORE_FROM_BACKUP;
import static org.lineageos.setupwizard.SetupWizardApp.REQUEST_CODE_RESTORE;

public class RestoreIntroActivity extends SubBaseActivity {

    @Override
    protected void onStartSubactivity() {
        setNextAllowed(true);

        findViewById(R.id.intro_restore_button).setOnClickListener(v -> launchRestore());
    }

    @Override
    protected void onNextPressed() {
        Intent intent = WizardManagerHelper.getNextIntent(getIntent(), Activity.RESULT_OK);
        nextAction(NEXT_REQUEST, intent);
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

    private void launchRestore() {
        Intent intent = new Intent(ACTION_RESTORE_FROM_BACKUP);
        startSubactivity(intent, REQUEST_CODE_RESTORE);
    }

}
