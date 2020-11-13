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

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.CheckBox;

public class UpdateRecoveryActivity extends BaseSetupWizardActivity {

    private CheckBox mRecoveryUpdateCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextText(R.string.next);
        mRecoveryUpdateCheckbox = findViewById(R.id.update_recovery_checkbox);

        // Default the checkbox to true, the effect will be reflected when going next
        mRecoveryUpdateCheckbox.setChecked(
                SystemProperties.getBoolean("persist.vendor.recovery_update", true));
    }

    @Override
    protected void onNextPressed() {
        SystemProperties.set("persist.vendor.recovery_update",
                String.valueOf(mRecoveryUpdateCheckbox.isChecked()));
        super.onNextPressed();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.update_recovery_page;
    }

    @Override
    protected int getTitleResId() {
        return -1;
    }

    @Override
    protected int getIconResId() {
        return -1;
    }
}
