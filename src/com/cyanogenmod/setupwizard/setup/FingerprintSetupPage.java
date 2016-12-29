/*
 * Copyright (C) 2013 The CyanogenMod Project
 * Copyright (C) 2017 The LineageOS Project
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

package com.cyanogenmod.setupwizard.setup;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.SetupWizardApp;
import com.cyanogenmod.setupwizard.cmstats.SetupStats;
import com.cyanogenmod.setupwizard.ui.SetupPageFragment;

public class FingerprintSetupPage extends SetupPage {

    private static final String TAG = "FingerprintSetupPage";

    public FingerprintSetupPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
    }

    @Override
    public Fragment getFragment(FragmentManager fragmentManager, int action) {
        Fragment fragment = fragmentManager.findFragmentByTag(getKey());
        if (fragment == null) {
            Bundle args = new Bundle();
            args.putString(Page.KEY_PAGE_ARGUMENT, getKey());
            args.putInt(Page.KEY_PAGE_ACTION, action);
            fragment = new FingerprintSetupFragment();
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public int getNextButtonTitleResId() {
        return R.string.skip;
    }

    @Override
    public String getKey() {
        return TAG;
    }

    @Override
    public int getTitleResId() {
        return R.string.fingerprint_setup_title;
    }

    @Override
    public int getIconResId() {
        return R.drawable.ic_fingerprint;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (SetupWizardApp.REQUEST_CODE_SETUP_FINGERPRINT == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                getCallbacks().onNextPage();
            }
        }
        return true;
    }

    public static class FingerprintSetupFragment extends SetupPageFragment {

        private TextView mSetupFingerprint;

        @Override
        protected void initializePage() {
            mSetupFingerprint = (TextView) mRootView.findViewById(R.id.setup_fingerprint);
            mSetupFingerprint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchFingerprintSetup();
                }
            });
        }

        @Override
        protected int getLayoutResource() {
            return R.layout.setup_fingerprint;
        }

        private void launchFingerprintSetup() {
            Intent intent = new Intent(SetupWizardApp.ACTION_SETUP_FINGERPRINT);
            intent.putExtra(SetupWizardApp.EXTRA_FIRST_RUN, true);
            intent.putExtra(SetupWizardApp.EXTRA_ALLOW_SKIP, true);
            intent.putExtra(SetupWizardApp.EXTRA_USE_IMMERSIVE, true);
            intent.putExtra(SetupWizardApp.EXTRA_THEME, SetupWizardApp.EXTRA_MATERIAL_LIGHT);
            intent.putExtra(SetupWizardApp.EXTRA_AUTO_FINISH, false);
            /*intent.putExtra(LockPatternUtils.LOCKSCREEN_FINGERPRINT_FALLBACK, true);*/
            intent.putExtra(SetupWizardApp.EXTRA_TITLE,
                    getString(R.string.settings_fingerprint_setup_title));
            intent.putExtra(SetupWizardApp.EXTRA_DETAILS,
                    getString(R.string.settings_fingerprint_setup_details));
            ActivityOptions options =
                    ActivityOptions.makeCustomAnimation(getActivity(),
                            android.R.anim.fade_in,
                            android.R.anim.fade_out);
            SetupStats.addEvent(SetupStats.Categories.EXTERNAL_PAGE_LOAD,
                    SetupStats.Action.EXTERNAL_PAGE_LAUNCH,
                    SetupStats.Label.PAGE,  SetupStats.Label.FINGERPRINT_SETUP);
            startActivityForResult(intent, SetupWizardApp.REQUEST_CODE_SETUP_FINGERPRINT,
                    options.toBundle());
        }
    }
}
