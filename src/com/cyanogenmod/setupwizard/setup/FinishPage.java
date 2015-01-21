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

import android.animation.Animator;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewAnimationUtils;

public class FinishPage extends SetupPage {

    public static final String TAG = "FinishPage";

    private FinishFragment mFinishFragment;

    public FinishPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
    }

    @Override
    public Fragment getFragment() {
        Bundle args = new Bundle();
        args.putString(SetupPage.KEY_PAGE_ARGUMENT, getKey());

        mFinishFragment = new FinishFragment();
        mFinishFragment.setArguments(args);
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
        mFinishFragment.animateOut(getCallbacks());
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

        private View mReveal;

        private Handler mHandler;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mHandler = new Handler();
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.primary));
        }

        @Override
        protected void initializePage() {
            mReveal = mRootView.findViewById(R.id.reveal);
        }

        private void animateOut(final SetupDataCallbacks callbacks) {
            int cx = (mReveal.getLeft() + mReveal.getRight()) / 2;
            int cy = (mReveal.getTop() + mReveal.getBottom()) / 2;
            int finalRadius = Math.max(mReveal.getWidth(), mReveal.getHeight());
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(mReveal, cx, cy, 0, finalRadius);

            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mReveal.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callbacks.onFinish();
                        }
                    });
                }

                @Override
                public void onAnimationCancel(Animator animation) {}

                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
            anim.start();
        }

        @Override
        protected int getLayoutResource() {
            return R.layout.setup_finished_page;
        }
    }

}
