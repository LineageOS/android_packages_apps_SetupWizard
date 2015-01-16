/*
 * Copyright (C) 2014 The CyanogenMod Project
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

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.ui.SetupPageFragment;

public class SimCardMissingPage extends SetupPage {

    public static final String TAG = "SimCardMissingPage";

    public SimCardMissingPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
    }

    @Override
    public Fragment getFragment() {
        Bundle args = new Bundle();
        args.putString(SetupPage.KEY_PAGE_ARGUMENT, getKey());

        FinishFragment fragment = new FinishFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getKey() {
        return TAG;
    }

    @Override
    public int getTitleResId() {
        return R.string.setup_sim_missing;
    }

    @Override
    public int getNextButtonTitleResId() {
        return R.string.skip;
    }


    public static class FinishFragment extends SetupPageFragment {

        @Override
        protected void initializePage() {}

        @Override
        protected int getLayoutResource() {
            return R.layout.sim_missing_page;
        }

    }

}
