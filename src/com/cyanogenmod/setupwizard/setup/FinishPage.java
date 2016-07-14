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

    private static final int WHAT_EXPLORE_MOD_GUIDE = 1;
    private static final String KEY_MESSENGER = "key_messenger";
    private static final String MODGUIDE_PACKAGE_NAME = "com.cyngn.modguide";

    private FinishFragment mFinishFragment;
    private final boolean mShowingModGuide;

    public FinishPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
        mShowingModGuide = SetupWizardUtils.canHasModMOD(context);
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
            final FinishPage page = mPage.get();
            if ((page != null) && (msg.what == WHAT_EXPLORE_MOD_GUIDE)) {
                page.doExploreModGuide();
            }
        }
    }

    @Override
    public String getKey() {
        return TAG;
    }

    @Override
    public int getButtonBarBackgroundColorId() {
        return mShowingModGuide ? R.color.mod_button_bar_background : R.color.primary;
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

    private void doExploreModGuide() {
        final SetupWizardActivity activity =
                (SetupWizardActivity) mFinishFragment.getActivity();
        final Intent intent =
                activity.getPackageManager().getLaunchIntentForPackage(MODGUIDE_PACKAGE_NAME);
        activity.setFinishIntent(intent);
        getCallbacks().onFinish();
    }

    @Override
    public int getNextButtonTitleResId() {
        return mShowingModGuide ? R.string.done : R.string.start;
    }

    public static class FinishFragment extends SetupPageFragment {

        private boolean mShowingModGuide;

        @Override
        protected void initializePage() {
            final Activity activity = getActivity();
            if (!mShowingModGuide || (activity == null)) {
                return;
            }
            mRootView.findViewById(R.id.explore_mod_guide)
                    .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Messenger messenger = getArguments().getParcelable(KEY_MESSENGER);
                    if (messenger == null) {
                        return;
                    }
                    final Message message = Message.obtain();
                    message.what = WHAT_EXPLORE_MOD_GUIDE;
                    try {
                        messenger.send(message);
                    } catch (final RemoteException e) {
                        Log.e(TAG, "Couldn't send message to start MOD Guide", e);
                    }
                }
            });
        }

        @Override
        protected int getLayoutResource() {
            final Context context = getContext();
            mShowingModGuide = (context != null) && SetupWizardUtils.canHasModMOD(context);
            return mShowingModGuide ?
                    R.layout.setup_modguide_page : R.layout.setup_finished_page;
        }
    }

}
