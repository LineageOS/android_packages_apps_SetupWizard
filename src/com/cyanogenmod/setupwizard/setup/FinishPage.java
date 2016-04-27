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
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.ui.SetupPageFragment;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

public class FinishPage extends SetupPage {

    public static final String TAG = "FinishPage";

    private FinishFragment mFinishFragment;

    public FinishPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
    }

    @Override
    public Fragment getFragment(FragmentManager fragmentManager, int action) {
        mFinishFragment = (FinishFragment)fragmentManager.findFragmentByTag(getKey());
        if (mFinishFragment == null) {
            Bundle args = new Bundle();
            args.putString(Page.KEY_PAGE_ARGUMENT, getKey());
            args.putInt(Page.KEY_PAGE_ACTION, action);
            mFinishFragment = new FinishFragment();
            mFinishFragment.setArguments(args);
        }
        return mFinishFragment;
    }

    @Override
    public String getKey() {
        return TAG;
    }

    @Override
    public int getTitleResId() {
        return R.string.setup_complete;
    }

    @Override
    public boolean doNextAction() {
        getCallbacks().onFinish();
        return true;
    }

    @Override
    public int getNextButtonTitleResId() {
        return R.string.start;
    }

    public static class FinishFragment extends SetupPageFragment {

        @Override
        protected void initializePage() {
            final Activity activity = getActivity();
            if (activity != null && SetupWizardUtils.canHasModMOD(activity)) {
                ImageView imageView = (ImageView) mRootView.findViewById(R.id.brand_logo);
                imageView.setImageResource(R.drawable.mod_ready);
                mRootView.findViewById(R.id.mod_welcome).setVisibility(View.VISIBLE);
                mRootView.findViewById(R.id.mod_desc).setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected int getLayoutResource() {
            return R.layout.setup_finished_page;
        }
    }

}
