/*
 * Copyright (C) 2019-2020 The Calyx Institute
 *               2020-2022 The LineageOS Project
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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.setupcompat.util.WizardManagerHelper;
import static com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP;

import org.lineageos.setupwizard.NavigationLayout;
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
    protected void onSkipPressed() {
        Intent intent = WizardManagerHelper.getNextIntent(getIntent(), Activity.RESULT_OK);
        nextAction(NEXT_REQUEST, intent);
    }

    protected void onSubactivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_SKIP) {
            onSkipPressed();
        } else {
            super.onSubactivityResult(requestCode, resultCode, data);
        }
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
        startSubactivity(intent);
    }

}
