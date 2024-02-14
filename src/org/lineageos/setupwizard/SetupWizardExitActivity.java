/*
 * SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard;

import static org.lineageos.setupwizard.SetupWizardApp.LOGV;

import android.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;

import org.lineageos.setupwizard.util.SetupWizardUtils;

public class SetupWizardExitActivity extends BaseSetupWizardActivity {

    private static final String TAG = SetupWizardExitActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LOGV) {
            Log.v(TAG, "onCreate savedInstanceState=" + savedInstanceState);
        }
        SetupWizardUtils.startSetupWizardExitProcedure(this);
        finish();
    }

}
