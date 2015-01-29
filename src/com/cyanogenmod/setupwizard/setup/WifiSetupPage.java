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
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.SetupWizardApp;
import com.cyanogenmod.setupwizard.ui.LoadingFragment;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

public class WifiSetupPage extends SetupPage {

    public static final String TAG = "WifiSetupPage";

    public WifiSetupPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
    }

    @Override
    public Fragment getFragment(FragmentManager fragmentManager, int action) {
        Fragment fragment = fragmentManager.findFragmentByTag(getKey());
        if (fragment == null) {
            Bundle args = new Bundle();
            args.putString(Page.KEY_PAGE_ARGUMENT, getKey());
            args.putInt(Page.KEY_PAGE_ACTION, action);
            fragment = new LoadingFragment();
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
        return R.string.setup_wifi;
    }

    @Override
    public void doLoadAction(Activity context, int action) {
        super.doLoadAction(context, action);
        SetupWizardUtils.launchWifiSetup(context);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != SetupWizardApp.REQUEST_CODE_SETUP_WIFI) return false;
        if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_FIRST_USER) {
            getCallbacks().onNextPage();
        } else if (resultCode == Activity.RESULT_CANCELED) {
            getCallbacks().onPreviousPage();
        }
        return true;
    }
}
