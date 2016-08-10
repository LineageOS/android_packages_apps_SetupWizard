/*
 * Copyright (C) 2013 The CyanogenMod Project
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
import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.SetupWizardApp;
import com.cyanogenmod.setupwizard.cmstats.SetupStats;
import com.cyanogenmod.setupwizard.ui.SetupPageFragment;

public class ScreenLockSetupPage extends SetupPage {

    private static final String TAG = "ScreenLockSetupPage";

    public ScreenLockSetupPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
    }

    @Override
    public Fragment getFragment(FragmentManager fragmentManager, int action) {
        Fragment fragment = fragmentManager.findFragmentByTag(getKey());
        if (fragment == null) {
            Bundle args = new Bundle();
            args.putString(Page.KEY_PAGE_ARGUMENT, getKey());
            args.putInt(Page.KEY_PAGE_ACTION, action);
            fragment = new LockscreenSetupFragment();
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
        return R.string.lockscreen_setup_title;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (SetupWizardApp.REQUEST_CODE_SETUP_LOCKSCREEN == requestCode) {
            if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_FIRST_USER) {
                getCallbacks().onNextPage();
            }
        }
        return true;
    }

    public static class LockscreenSetupFragment extends SetupPageFragment {

        private TextView mSetupLockscreen;

        @Override
        protected void initializePage() {
            mSetupLockscreen = (TextView) mRootView.findViewById(R.id.setup_lockscreen);
            mSetupLockscreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchLockscreenSetup();
                }
            });
        }

        @Override
        protected int getLayoutResource() {
            return R.layout.setup_lockscreen;
        }

        private void launchLockscreenSetup() {
            Intent intent = new Intent(SetupWizardApp.ACTION_SETUP_LOCKSCREEN);
            intent.putExtra(SetupWizardApp.EXTRA_TITLE,
                    getString(R.string.settings_lockscreen_setup_title));
            intent.putExtra(SetupWizardApp.EXTRA_DETAILS,
                    getString(R.string.settings_lockscreen_setup_details));
            ActivityOptions options =
                    ActivityOptions.makeCustomAnimation(getActivity(),
                            android.R.anim.fade_in,
                            android.R.anim.fade_out);
            SetupStats.addEvent(SetupStats.Categories.EXTERNAL_PAGE_LOAD,
                    SetupStats.Action.EXTERNAL_PAGE_LAUNCH,
                    SetupStats.Label.PAGE,  SetupStats.Label.LOCKSCREEN_SETUP);
            startActivityForResult(intent, SetupWizardApp.REQUEST_CODE_SETUP_LOCKSCREEN,
                    options.toBundle());
        }
    }
}
