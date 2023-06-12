/*
 * Copyright (C) 2023 The LineageOS Project
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

import android.app.UiModeManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

public class ThemeSettingsActivity extends BaseSetupWizardActivity {
    public static final String TAG = ThemeSettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getGlifLayout().setDescriptionText(getString(R.string.theme_summary));

        UiModeManager uiModeManager = getSystemService(UiModeManager.class);
        final RadioGroup radioGroup = findViewById(R.id.theme_radio_group);
        radioGroup.check(((getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
                ? R.id.radio_dark : R.id.radio_light);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_dark:
                    uiModeManager.setNightModeActivated(true);
                    break;
                case R.id.radio_light:
                    uiModeManager.setNightModeActivated(false);
                    break;
            }
        });
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setup_theme;
    }

    @Override
    protected int getTitleResId() {
        return R.string.setup_theme;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_theme;
    }
}
