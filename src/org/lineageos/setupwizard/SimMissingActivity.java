/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard;

import static com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP;

import android.annotation.Nullable;
import android.content.Intent;
import android.os.Bundle;

import org.lineageos.setupwizard.util.SetupWizardUtils;

public class SimMissingActivity extends BaseSetupWizardActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SetupWizardUtils.simMissing(this) || !SetupWizardUtils.hasTelephony(this)) {
            finishAction(RESULT_SKIP);
            return;
        }
        getGlifLayout().setDescriptionText(getString(R.string.sim_missing_summary));
        setNextAllowed(true);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.sim_missing_page;
    }

    @Override
    protected int getTitleResId() {
        return R.string.setup_sim_missing;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_sim;
    }

}
