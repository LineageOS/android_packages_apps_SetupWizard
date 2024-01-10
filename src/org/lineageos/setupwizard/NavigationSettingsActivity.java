/*
 * Copyright (C) 2022-2023 The LineageOS Project
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

import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_2BUTTON_OVERLAY;
import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_3BUTTON_OVERLAY;
import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_GESTURAL_OVERLAY;

import static com.android.systemui.shared.recents.utilities.Utilities.isLargeScreen;

import static org.lineageos.internal.util.DeviceKeysConstants.KEY_MASK_APP_SWITCH;
import static org.lineageos.setupwizard.SetupWizardApp.DISABLE_NAV_KEYS;
import static org.lineageos.setupwizard.SetupWizardApp.NAVIGATION_OPTION_KEY;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.airbnb.lottie.LottieAnimationView;

import org.lineageos.setupwizard.util.SetupWizardUtils;

import lineageos.providers.LineageSettings;

public class NavigationSettingsActivity extends BaseSetupWizardActivity {

    private SetupWizardApp mSetupWizardApp;

    private boolean mIsTaskbarEnabled;

    private String mSelection = NAV_BAR_MODE_GESTURAL_OVERLAY;

    private CheckBox mHideGesturalHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSetupWizardApp = (SetupWizardApp) getApplication();
        boolean navBarEnabled = false;
        if (mSetupWizardApp.getSettingsBundle().containsKey(DISABLE_NAV_KEYS)) {
            navBarEnabled = mSetupWizardApp.getSettingsBundle().getBoolean(DISABLE_NAV_KEYS);
        }
        mIsTaskbarEnabled = LineageSettings.System.getInt(getContentResolver(),
                LineageSettings.System.ENABLE_TASKBAR, isLargeScreen(this) ? 1 : 0) == 1;

        int deviceKeys = getResources().getInteger(
                org.lineageos.platform.internal.R.integer.config_deviceHardwareKeys);
        boolean hasHomeKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;

        getGlifLayout().setDescriptionText(getString(R.string.navigation_summary));
        setNextText(R.string.next);

        int available = 3;
        // Hide unavailable navigation modes
        if (!SetupWizardUtils.isPackageInstalled(this, NAV_BAR_MODE_GESTURAL_OVERLAY)) {
            findViewById(R.id.radio_gesture).setVisibility(View.GONE);
            ((RadioButton) findViewById(R.id.radio_sw_keys)).setChecked(true);
            available--;
        }

        if (!SetupWizardUtils.isPackageInstalled(this, NAV_BAR_MODE_2BUTTON_OVERLAY)) {
            findViewById(R.id.radio_two_button).setVisibility(View.GONE);
            available--;
        }

        if (!SetupWizardUtils.isPackageInstalled(this, NAV_BAR_MODE_3BUTTON_OVERLAY)) {
            findViewById(R.id.radio_sw_keys).setVisibility(View.GONE);
            available--;
        }

        // Hide this page if the device has hardware keys but didn't enable navbar
        // or if there's <= 1 available navigation modes
        if (!navBarEnabled && hasHomeKey || available <= 1) {
            mSetupWizardApp.getSettingsBundle().putString(NAVIGATION_OPTION_KEY,
                    NAV_BAR_MODE_3BUTTON_OVERLAY);
            finishAction(RESULT_OK);
        }

        final LottieAnimationView navigationIllustration =
                findViewById(R.id.navigation_illustration);
        final RadioGroup radioGroup = findViewById(R.id.navigation_radio_group);
        mHideGesturalHint = findViewById(R.id.hide_navigation_hint);

        // Hide navigation hint checkbox when taskbar is enabled
        if (mIsTaskbarEnabled) {
            mHideGesturalHint.setVisibility(View.GONE);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_gesture:
                    mSelection = NAV_BAR_MODE_GESTURAL_OVERLAY;
                    navigationIllustration
                            .setAnimation(R.raw.lottie_system_nav_fully_gestural);
                    revealHintCheckbox();
                    break;
                case R.id.radio_two_button:
                    mSelection = NAV_BAR_MODE_2BUTTON_OVERLAY;
                    navigationIllustration.setAnimation(R.raw.lottie_system_nav_2_button);
                    hideHintCheckBox();
                    break;
                case R.id.radio_sw_keys:
                    mSelection = NAV_BAR_MODE_3BUTTON_OVERLAY;
                    navigationIllustration.setAnimation(R.raw.lottie_system_nav_3_button);
                    hideHintCheckBox();
                    break;
            }

            navigationIllustration.playAnimation();
        });
    }

    private void revealHintCheckbox() {
        if (mIsTaskbarEnabled) {
            return;
        }

        mHideGesturalHint.animate().cancel();

        if (mHideGesturalHint.getVisibility() == View.VISIBLE) {
            return;
        }

        mHideGesturalHint.setVisibility(View.VISIBLE);
        mHideGesturalHint.setAlpha(0.0f);
        mHideGesturalHint.animate()
                .translationY(0)
                .alpha(1.0f)
                .setListener(null);
    }

    private void hideHintCheckBox() {
        if (mIsTaskbarEnabled) {
            return;
        }

        if (mHideGesturalHint.getVisibility() == View.INVISIBLE) {
            return;
        }

        mHideGesturalHint.animate()
                .translationY(-mHideGesturalHint.getHeight())
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mHideGesturalHint.setVisibility(View.INVISIBLE);
                    }
                });
    }

    @Override
    protected void onNextPressed() {
        mSetupWizardApp.getSettingsBundle().putString(NAVIGATION_OPTION_KEY, mSelection);
        if (!mIsTaskbarEnabled) {
            boolean hideHint = mHideGesturalHint.isChecked();
            LineageSettings.System.putIntForUser(getContentResolver(),
                    LineageSettings.System.NAVIGATION_BAR_HINT, hideHint ? 0 : 1,
                    UserHandle.USER_CURRENT);
        }
        super.onNextPressed();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setup_navigation;
    }

    @Override
    protected int getTitleResId() {
        return R.string.setup_navigation;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_navigation;
    }
}
