/*
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
import android.content.res.Resources;
import android.os.Bundle;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.SetupWizardApp;
import com.cyanogenmod.setupwizard.cmstats.SetupStats;
import com.cyanogenmod.setupwizard.ui.SetupPageFragment;

public class WelcomePage extends SetupPage {

    public static final String TAG = "WelcomePage";

    private static final String ACTION_EMERGENCY_DIAL = "com.android.phone.EmergencyDialer.DIAL";

    private WelcomeFragment mWelcomeFragment;

    public WelcomePage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
    }

    @Override
    public Fragment getFragment(FragmentManager fragmentManager, int action) {
        mWelcomeFragment = (WelcomeFragment)fragmentManager.findFragmentByTag(getKey());
        if (mWelcomeFragment == null) {
            Bundle args = new Bundle();
            args.putString(Page.KEY_PAGE_ARGUMENT, getKey());
            args.putInt(Page.KEY_PAGE_ACTION, action);
            mWelcomeFragment = new WelcomeFragment();
            mWelcomeFragment.setArguments(args);
        }
        return mWelcomeFragment;
    }

    @Override
    public int getTitleResId() {
        return R.string.os_name;
    }

    @Override
    public int getIconResId() {
        return -1;
    }

    @Override
    public boolean doNextAction() {
        return super.doNextAction();
    }

    @Override
    public boolean doPreviousAction() {
        Intent intent = new Intent(ACTION_EMERGENCY_DIAL);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        ActivityOptions options =
                ActivityOptions.makeCustomAnimation(mContext,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out);
        SetupStats.addEvent(SetupStats.Categories.BUTTON_CLICK, SetupStats.Label.EMERGENCY_CALL);
        SetupStats.addEvent(SetupStats.Categories.EXTERNAL_PAGE_LOAD,
                SetupStats.Action.EXTERNAL_PAGE_LAUNCH,
                SetupStats.Label.PAGE,  SetupStats.Label.EMERGENCY_CALL);
        mContext.startActivity(intent, options.toBundle());
        return true;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SetupWizardApp.REQUEST_CODE_UNLOCK) {
            if (resultCode == Activity.RESULT_OK) {
                ((SetupWizardApp) mContext.getApplicationContext()).setIsAuthorized(true);
                getCallbacks().onNextPage();
                return true;
            }
        }
        return false;
    }

    @Override
    public String getKey() {
        return TAG;
    }

    @Override
    public int getNextButtonTitleResId() {
        return R.string.next;
    }

    @Override
    public int getPrevButtonTitleResId() {
        return R.string.emergency_call;
    }

    public static class WelcomeFragment extends SetupPageFragment {

        @Override
        protected void initializePage() {
        }

        @Override
        protected int getLayoutResource() {
            return R.layout.setup_welcome_page;
        }
    }

}
