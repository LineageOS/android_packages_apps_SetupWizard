/*
 * Copyright (C) 2016 The CyanogenMod Project
 *               2017-2022 The LineageOS Project
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

import android.os.Bundle;
import android.widget.ImageView;

import com.google.android.setupcompat.util.ResultCodes;

import org.lineageos.setupwizard.util.PhoneMonitor;

public class SimMissingActivity extends BaseSetupWizardActivity {

    public static final String TAG = SimMissingActivity.class.getSimpleName();

    private static final int SIM_DEFAULT = 0;
    private static final int SIM_SIDE = 1;
    private static final int SIM_BACK = 2;

    private PhoneMonitor mPhoneMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getGlifLayout().setDescriptionText(getString(R.string.sim_missing_summary));
        mPhoneMonitor = PhoneMonitor.getInstance();
        if (!mPhoneMonitor.simMissing()) {
            finishAction(RESULT_OK);
        }
        final int simLocation = getResources().getInteger(
                R.integer.sim_image_type);
        ImageView simLogo = ((ImageView) findViewById(R.id.sim_slot_image));
        switch (simLocation) {
            case SIM_SIDE:
                simLogo.setImageResource(R.drawable.sim_side);
                break;
            case SIM_BACK:
                simLogo.setImageResource(R.drawable.sim_back);
                break;
            default:
                simLogo.setImageResource(R.drawable.sim);
                simLogo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
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
