/*
 * Copyright (C) 2019-2020 The Calyx Institute
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

import static org.lineageos.setupwizard.SetupWizardApp.ENABLE_RECOVERY_UPDATE;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import com.google.android.setupcompat.util.WizardManagerHelper;
import org.lineageos.setupwizard.util.SetupWizardUtils;


public class UpdateRecoveryActivity extends BaseSetupWizardActivity {

    private CheckBox mRecoveryUpdateCheckbox;
    private SetupWizardApp mSetupWizardApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSetupWizardApp = (SetupWizardApp) getApplication();

        if (!SetupWizardUtils.hasRecoveryUpdater(this)) {
            Log.v(TAG, "No recovery updater, skipping UpdateRecoveryActivity");

            Intent intent = WizardManagerHelper.getNextIntent(getIntent(), Activity.RESULT_OK);
            nextAction(NEXT_REQUEST, intent);
            finish();
            return;
        }

        setNextText(R.string.next);
        mRecoveryUpdateCheckbox = findViewById(R.id.update_recovery_checkbox);

        View cbView = findViewById(R.id.update_recovery_checkbox_view);
        cbView.setOnClickListener(v -> {
            mRecoveryUpdateCheckbox.setChecked(!mRecoveryUpdateCheckbox.isChecked());
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRecoveryUpdateOption();
    }

    @Override
    protected void onNextPressed() {
        mSetupWizardApp.getSettingsBundle().putBoolean(ENABLE_RECOVERY_UPDATE,
                mRecoveryUpdateCheckbox.isChecked());

        Intent intent = WizardManagerHelper.getNextIntent(getIntent(), Activity.RESULT_OK);
        nextAction(NEXT_REQUEST, intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.update_recovery_page;
    }

    @Override
    protected int getTitleResId() {
        return R.string.update_recovery_title;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_system_update;
    }

    private void updateRecoveryUpdateOption() {
        final Bundle myPageBundle = mSetupWizardApp.getSettingsBundle();
        // Default the checkbox to true
        boolean checked = myPageBundle.containsKey(ENABLE_RECOVERY_UPDATE) ?
                myPageBundle.getBoolean(ENABLE_RECOVERY_UPDATE) :
                true;

        mRecoveryUpdateCheckbox.setChecked(checked);
    }
}
