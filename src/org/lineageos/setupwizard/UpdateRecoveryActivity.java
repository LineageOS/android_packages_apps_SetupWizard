/*
 * SPDX-FileCopyrightText: 2020-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard;

import static com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP;

import static org.lineageos.setupwizard.SetupWizardApp.ENABLE_RECOVERY_UPDATE;
import static org.lineageos.setupwizard.SetupWizardApp.UPDATE_RECOVERY_PROP;

import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import org.lineageos.setupwizard.util.SetupWizardUtils;

public class UpdateRecoveryActivity extends BaseSetupWizardActivity {

    private CheckBox mRecoveryUpdateCheckbox;
    private SetupWizardApp mSetupWizardApp;
    private static boolean sFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSetupWizardApp = (SetupWizardApp) getApplication();
        getGlifLayout().setDescriptionText(getString(R.string.update_recovery_full_description,
                getString(R.string.update_recovery_description),
                getString(R.string.update_recovery_warning)));

        if (!SetupWizardUtils.hasRecoveryUpdater(this)) {
            Log.v(TAG, "No recovery updater, skipping UpdateRecoveryActivity");
            finishAction(RESULT_SKIP);
            return;
        }

        setNextText(R.string.next);
        mRecoveryUpdateCheckbox = findViewById(R.id.update_recovery_checkbox);

        View cbView = findViewById(R.id.update_recovery_checkbox_view);
        cbView.setOnClickListener(
                v -> mRecoveryUpdateCheckbox.setChecked(!mRecoveryUpdateCheckbox.isChecked()));

        // Allow overriding the default checkbox state
        if (sFirstTime) {
            mSetupWizardApp.getSettingsBundle().putBoolean(ENABLE_RECOVERY_UPDATE,
                    SystemProperties.getBoolean(UPDATE_RECOVERY_PROP, false));
        }

        sFirstTime = false;
    }

    @Override
    public void onResume() {
        super.onResume();

        final Bundle myPageBundle = mSetupWizardApp.getSettingsBundle();
        final boolean checked = myPageBundle.getBoolean(ENABLE_RECOVERY_UPDATE, false);
        mRecoveryUpdateCheckbox.setChecked(checked);
    }

    @Override
    protected void onNextPressed() {
        mSetupWizardApp.getSettingsBundle().putBoolean(ENABLE_RECOVERY_UPDATE,
                mRecoveryUpdateCheckbox.isChecked());
        super.onNextPressed();
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
}
