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
import android.content.Context;
import android.content.Intent;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.SetupWizardApp;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

public class WifiSetupPage extends SetupPage {

    public static final String TAG = "WifiSetupPage";

    public WifiSetupPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
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
        return R.string.existing;
    }

    @Override
    public void doLoadAction(Activity context, int action) {
        if (action == Page.ACTION_PREVIOUS) {
            getCallbacks().onPreviousPage();
        } else {
            SetupWizardUtils.launchWifiSetup(context);
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != SetupWizardApp.REQUEST_CODE_SETUP_WIFI) return false;
        getCallbacks().onNextPage();
        return true;
    }
}
