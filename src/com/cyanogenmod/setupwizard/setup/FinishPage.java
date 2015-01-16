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

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.ui.SetupPageFragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;

public class FinishPage extends SetupPage {

    public static final String TAG = "FinishPage";

    public FinishPage(Context context, SetupDataCallbacks callbacks) {
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

    @Override
    public int getPrevButtonTitleResId() {
        return -1;
    }

    public static class FinishFragment extends SetupPageFragment {

        @Override
        protected void initializePage() {}

        @Override
        protected int getLayoutResource() {
            return R.layout.setup_finished_page;
        }

        @Override
        protected int getHeaderLayoutResource() {
            return -1;
        }
    }

}
