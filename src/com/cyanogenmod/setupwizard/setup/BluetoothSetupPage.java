/*
 * Copyright (C) 2015 The CyanogenMod Project
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
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.SetupWizardApp;
import com.cyanogenmod.setupwizard.cmstats.SetupStats;
import com.cyanogenmod.setupwizard.ui.LoadingFragment;
import com.cyanogenmod.setupwizard.ui.SetupPageFragment;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class BluetoothSetupPage extends SetupPage {

    public static final String TAG = "BluetoothSetupPage";

    private static final String ACTION_CONNECT_INPUT =
            "com.google.android.intent.action.CONNECT_INPUT";

    private static final String INTENT_EXTRA_NO_INPUT_MODE = "no_input_mode";

    private LoadingFragment mLoadingFragment;

    public BluetoothSetupPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
    }

    @Override
    public SetupPageFragment getFragment(FragmentManager fragmentManager, int action) {
        mLoadingFragment = (LoadingFragment)fragmentManager.findFragmentByTag(getKey());
        if (mLoadingFragment == null) {
            Bundle args = new Bundle();
            args.putString(Page.KEY_PAGE_ARGUMENT, getKey());
            args.putInt(Page.KEY_PAGE_ACTION, action);
            mLoadingFragment = new LoadingFragment();
            mLoadingFragment.setArguments(args);
        }
        return mLoadingFragment;
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
        return R.string.loading;
    }

    @Override
    public int getIconResId() {
        return -1;
    }

    @Override
    public void doLoadAction(FragmentManager fragmentManager, int action) {
        super.doLoadAction(fragmentManager, action);
        launchConnectInput();
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SetupWizardApp.REQUEST_CODE_SETUP_BLUETOOTH) {
            SetupStats.addEvent(SetupStats.Categories.EXTERNAL_PAGE_LOAD,
                    SetupStats.Action.EXTERNAL_PAGE_RESULT,
                    SetupStats.Label.BLUETOOTH_SETUP, "success");
            getCallbacks().onNextPage();
        }  else {
            return false;
        }
        return true;
    }

    private void launchConnectInput() {
        Intent intent = new Intent();
        intent.setComponent(SetupWizardUtils.mTvAddAccessorySettingsActivity);
        intent.setAction(ACTION_CONNECT_INPUT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(INTENT_EXTRA_NO_INPUT_MODE, true);
        ActivityOptions options =
                ActivityOptions.makeCustomAnimation(mContext,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out);
        SetupStats.addEvent(SetupStats.Categories.EXTERNAL_PAGE_LOAD,
                SetupStats.Action.EXTERNAL_PAGE_LAUNCH,
                SetupStats.Label.PAGE,  SetupStats.Label.BLUETOOTH_SETUP);
        mLoadingFragment.startActivityForResult(intent,
                SetupWizardApp.REQUEST_CODE_SETUP_BLUETOOTH, options.toBundle());
    }
}
