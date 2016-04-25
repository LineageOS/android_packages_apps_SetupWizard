/*
 * Copyright (C) 2016 The CyanogenMod Project
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

package com.cyanogenmod.setupwizard.ui;

import com.android.setupwizard.navigationbar.SetupWizardNavBar;
import com.android.setupwizardlib.util.SystemBarHelper;
import com.android.setupwizardlib.view.BottomScrollView;
import com.cyanogenmod.setupwizard.cmstats.SetupStats;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.view.View;

import java.util.LinkedList;

public abstract class SetupActivity extends Activity
        implements BottomScrollView.BottomScrollListener, SetupWizardNavBar.NavigationBarListener,
        cyanogenmod.themes.ThemeManager.ThemeChangeListener{

    private static final String ACTION_EMERGENCY_DIAL = "com.android.phone.EmergencyDialer.DIAL";

    private boolean mBackAllowed;
    private View mDefaultButton;
    private boolean mDefaultButtonShouldScroll;

    protected boolean mIsActivityVisible;
    protected boolean mIsExiting;
    protected boolean mIsGoingBack;
    private boolean mIsPrimaryUser;
    private SetupWizardNavBar mNavigationBar;
    private boolean mNextAllowed;
    protected BottomScrollView mScrollView;

    private static final LinkedList<Runnable> sFinishRunnables = new LinkedList<>();



    protected void onSetupStart() {
        SystemBarHelper.hideSystemBars(getWindow());
        SetupWizardUtils.disableCaptivePortalDetection(getApplicationContext());
        SetupWizardUtils.disableNotifications(getApplicationContext());
        SetupWizardUtils.tryEnablingWifi(getApplicationContext());
    }

    @Override
    public void onScrolledToBottom() {

    }

    @Override
    public void onRequiresScroll() {

    }

    @Override
    public void onNavigateBack() {

    }

    protected void setBackAllowed(boolean allowed) {
        // Enable / disable navigation bar back button
        if (mNavigationBar != null) {
            mNavigationBar.getBackButton().setEnabled(allowed);
        }
    }

    protected void setNextAllowed(boolean allowed) {
        if (mNavigationBar != null) {
            mNavigationBar.getNextButton().setEnabled(allowed);
        }
    }

    protected void setNextText(int resId) {
        if (mNavigationBar != null) {
            mNavigationBar.getNextButton().setText(resId);
        }
    }

    @Override
    public void onNavigateNext() {

    }

    @Override
    public void onNavigationBarCreated(SetupWizardNavBar bar) {
        mNavigationBar = bar;
        bar.setUseImmersiveMode(true);
        bar.getView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                view.requestApplyInsets();
            }
        });
    }

    protected void finalizeSetup() {
        sFinishRunnables.add(new Runnable() {
            @Override
            public void run() {
                SetupWizardUtils.enableNotifications(SetupActivity.this.getApplicationContext());
                Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
                Settings.Secure.putInt(getContentResolver(),
                        Settings.Secure.USER_SETUP_COMPLETE, 1);
//                if (mEnableAccessibilityController != null) {
//                    mEnableAccessibilityController.onDestroy();
//                }
                final cyanogenmod.themes.ThemeManager tm =
                        cyanogenmod.themes.ThemeManager.getInstance(SetupActivity.this);
                tm.removeClient(SetupActivity.this);
                SetupStats.sendEvents(SetupActivity.this);
                SetupWizardUtils.disableGMSSetupWizard(SetupActivity.this);
                final WallpaperManager wallpaperManager =
                        WallpaperManager.getInstance(SetupActivity.this);
                wallpaperManager.forgetLoadedWallpaper();
            }
        });
        new FinishTask(this, sFinishRunnables).execute();
    }

    @Override
    public void onFinish(boolean isSuccess) {
        if (isResumed()) {
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    finishSetup();
//                }
//            });
        }
    }

    @Override
    public void onProgress(int progress) {
        if (progress > 0) {
//            mFinishingProgressBar.setIndeterminate(false);
//            mFinishingProgressBar.setProgress(progress);
        }
    }


    private static class FinishTask extends AsyncTask<Void, Void, Boolean> {

        private final SetupActivity mActivity;
        private final LinkedList<Runnable> mFinishRunnables;

        public FinishTask(SetupActivity activity,
                LinkedList<Runnable> finishRunnables) {
            mActivity = activity;
            mFinishRunnables = finishRunnables;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            for (Runnable runnable : mFinishRunnables) {
                runnable.run();
            }
            SetupWizardUtils.disableSetupWizard(mActivity);
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            mActivity.startActivity(intent);
            mActivity.finish();
        }
    }
}
