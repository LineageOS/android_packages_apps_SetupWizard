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
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.ui.SetupPageFragment;
import com.cyanogenmod.setupwizard.ui.SetupWizardActivity;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

import java.lang.ref.WeakReference;

public class FinishPage extends SetupPage {

    public static final String TAG = "FinishPage";

    private static final String KEY_MESSENGER = "key_messenger";

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
            args.putParcelable(KEY_MESSENGER, new Messenger(mHandler));
            mFinishFragment = new FinishFragment();
            mFinishFragment.setArguments(args);
        }
        return mFinishFragment;
    }

    private final PageHandler mHandler = new PageHandler(this);

    private static class PageHandler extends Handler {

        private final WeakReference<FinishPage> mPage;

        private PageHandler(final FinishPage page) {
            mPage = new WeakReference<>(page);
        }

        @Override
        public void handleMessage(final Message msg) {
        }
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
    public int getIconResId() {
        return -1;
    }

    @Override
    public boolean doNextAction() {
        getCallbacks().onFinish();
        return true;
    }

    @Override
    public int getNextButtonTitleResId() {
        return  R.string.start;
    }

    public static class FinishFragment extends SetupPageFragment {

        @Override
        protected void initializePage() {
            final Activity activity = getActivity();
            if (activity == null) {
                return;
            }
        }

        @Override
        protected int getLayoutResource() {
            return R.layout.setup_finished_page;
        }
    }

}
