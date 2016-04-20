/*
 * Copyright (C) 2016 The CyanogenMod Project
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

package com.cyanogenmod.setupwizard.ui;

import static com.cyanogenmod.setupwizard.SetupWizardApp.DISABLE_NAV_KEYS;
import static com.cyanogenmod.setupwizard.SetupWizardApp.KEY_APPLY_DEFAULT_THEME;
import static com.cyanogenmod.setupwizard.SetupWizardApp.KEY_BUTTON_BACKLIGHT;
import static com.cyanogenmod.setupwizard.SetupWizardApp.KEY_PRIVACY_GUARD;
import static com.cyanogenmod.setupwizard.SetupWizardApp.KEY_SEND_METRICS;

import android.animation.Animator;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ThemeConfig;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.setupwizardlib.util.WizardManagerHelper;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.SetupWizardApp;
import com.cyanogenmod.setupwizard.cmstats.SetupStats;
import com.cyanogenmod.setupwizard.util.EnableAccessibilityController;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

import cyanogenmod.hardware.CMHardwareManager;
import cyanogenmod.providers.CMSettings;
import cyanogenmod.themes.ThemeManager;

public class FinishActivity extends BaseSetupWizardActivity
        implements ThemeManager.ThemeChangeListener {

    public static final String TAG = FinishActivity.class.getSimpleName();

    private ImageView mReveal;
    private ProgressBar mFinishingProgressBar;

    private EnableAccessibilityController mEnableAccessibilityController;

    private SetupWizardApp mSetupWizardApp;

    private final Handler mHandler = new Handler();

    private volatile boolean mIsFinishing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSetupWizardApp = (SetupWizardApp) getApplication();
        setContentView(R.layout.finish_activity);
        mReveal = (ImageView) findViewById(R.id.reveal);
        mFinishingProgressBar = (ProgressBar)findViewById(R.id.finishing_bar);
        mEnableAccessibilityController =
                EnableAccessibilityController.getInstance(getApplicationContext());
        setNextText(R.string.start);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.translucent_enter, R.anim.translucent_exit);
    }

    @Override
    public void onNavigateBack() {
       onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        startFinishSequence();
    }

    private void finishSetup() {
        if (!mIsFinishing) {
            mSetupWizardApp.sendStickyBroadcastAsUser(
                    new Intent(SetupWizardApp.ACTION_FINISHED),
                    Binder.getCallingUserHandle());
            mIsFinishing = true;
            setupRevealImage();
        }
    }

    private void startFinishSequence() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        hideBackButton();
        hideNextButton();
        mSetupWizardApp.enableCaptivePortalDetection();
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        mFinishingProgressBar.setVisibility(View.VISIBLE);
        mFinishingProgressBar.setIndeterminate(true);
        mFinishingProgressBar.startAnimation(fadeIn);
        final ThemeManager tm = ThemeManager.getInstance(this);
        try {
            tm.registerThemeChangeListener(this);
        } catch (Exception e) {
            Log.w(TAG, "ThemeChangeListener already registered");
        }
        handleDefaultThemeSetup(this);
    }

    @Override
    public void onFinish(boolean isSuccess) {
        if (isResumed()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    startFinishSequence();
                }
            });
        }
    }

    @Override
    public void onProgress(int progress) {
        if (progress > 0) {
            mFinishingProgressBar.setIndeterminate(false);
            mFinishingProgressBar.setProgress(progress);
        }
    }

    private void setupRevealImage() {
        mFinishingProgressBar.setProgress(100);
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        mFinishingProgressBar.startAnimation(fadeOut);
        mFinishingProgressBar.setVisibility(View.INVISIBLE);

        final Point p = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(p);
        final WallpaperManager wallpaperManager =
                WallpaperManager.getInstance(this);
        wallpaperManager.forgetLoadedWallpaper();
        final Bitmap wallpaper = wallpaperManager.getBitmap();
        Bitmap cropped = null;
        if (wallpaper != null) {
            cropped = Bitmap.createBitmap(wallpaper, 0,
                    0, Math.min(p.x, wallpaper.getWidth()),
                    Math.min(p.y, wallpaper.getHeight()));
        }
        if (cropped != null) {
            mReveal.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mReveal.setImageBitmap(cropped);
        } else {
            mReveal.setBackground(wallpaperManager
                    .getBuiltInDrawable(p.x, p.y, false, 0, 0));
        }
        animateOut();
    }

    private void animateOut() {
        int cx = (mReveal.getLeft() + mReveal.getRight()) / 2;
        int cy = (mReveal.getTop() + mReveal.getBottom()) / 2;
        int finalRadius = Math.max(mReveal.getWidth(), mReveal.getHeight());
        Animator anim =
                ViewAnimationUtils.createCircularReveal(mReveal, cx, cy, 0, finalRadius);
        anim.setDuration(900);
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
                        completeSetup();
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

    private void completeSetup() {
        if (mEnableAccessibilityController != null) {
            mEnableAccessibilityController.onDestroy();
        }
        new FinishTask(this).execute();
    }

    private static void handleEnableMetrics(SetupWizardApp setupWizardApp) {
        Bundle privacyData = setupWizardApp.getSettingsBundle();
        if (privacyData != null
                && privacyData.containsKey(KEY_SEND_METRICS)) {
            CMSettings.Secure.putInt(setupWizardApp.getContentResolver(),
                    CMSettings.Secure.STATS_COLLECTION, privacyData.getBoolean(KEY_SEND_METRICS)
                            ? 1 : 0);
        }
    }

    private static void handleDefaultThemeSetup(FinishActivity finishActivity) {
        Bundle privacyData = finishActivity.mSetupWizardApp.getSettingsBundle();
        if (!SetupWizardUtils.getDefaultThemePackageName(finishActivity.mSetupWizardApp)
                .equals(ThemeConfig.SYSTEM_DEFAULT) && privacyData != null &&
                privacyData.getBoolean(KEY_APPLY_DEFAULT_THEME)) {
            Log.i(TAG, "Applying default theme");
            final ThemeManager tm = ThemeManager.getInstance(finishActivity.mSetupWizardApp);
            tm.applyDefaultTheme();
        } else {
            finishActivity.finishSetup();
        }
    }

    private static void handlePrivacyGuard(SetupWizardApp setupWizardApp) {
        Bundle mPrivacyData = setupWizardApp.getSettingsBundle();
        if (mPrivacyData != null && mPrivacyData.containsKey(KEY_PRIVACY_GUARD)) {
            CMSettings.Secure.putInt(setupWizardApp.getContentResolver(),
                    CMSettings.Secure.PRIVACY_GUARD_DEFAULT,
                    mPrivacyData.getBoolean(KEY_PRIVACY_GUARD) ? 1 : 0);
        }
    }

    private static void handleNavKeys(SetupWizardApp setupWizardApp) {
        if (setupWizardApp.getSettingsBundle().containsKey(DISABLE_NAV_KEYS)) {
            SetupStats.addEvent(SetupStats.Categories.SETTING_CHANGED,
                    SetupStats.Action.ENABLE_NAV_KEYS,
                    SetupStats.Label.CHECKED,
                    String.valueOf(setupWizardApp.getSettingsBundle()
                            .getBoolean(DISABLE_NAV_KEYS)));
            writeDisableNavkeysOption(setupWizardApp,
                    setupWizardApp.getSettingsBundle().getBoolean(DISABLE_NAV_KEYS));
        }
    }

    private static void writeDisableNavkeysOption(Context context, boolean enabled) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        CMSettings.Global.putInt(context.getContentResolver(),
                CMSettings.Global.DEV_FORCE_SHOW_NAVBAR, enabled ? 1 : 0);
        CMHardwareManager hardware = CMHardwareManager.getInstance(context);
        hardware.set(CMHardwareManager.FEATURE_KEY_DISABLE, enabled);

        /* Save/restore button timeouts to disable them in softkey mode */
        if (enabled) {
            CMSettings.Secure.putInt(context.getContentResolver(),
                    CMSettings.Secure.BUTTON_BRIGHTNESS, 0);
        } else {
            int currentBrightness = CMSettings.Secure.getInt(context.getContentResolver(),
                    CMSettings.Secure.BUTTON_BRIGHTNESS, 100);
            int oldBright = prefs.getInt(KEY_BUTTON_BACKLIGHT,
                    currentBrightness);
            CMSettings.Secure.putInt(context.getContentResolver(),
                    CMSettings.Secure.BUTTON_BRIGHTNESS, oldBright);
        }
    }


    private static class FinishTask extends AsyncTask<Void, Void, Boolean> {

        private final FinishActivity mActivity;

        public FinishTask(FinishActivity activity) {
            mActivity = activity;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            SetupWizardApp app = (SetupWizardApp)mActivity.getApplication();
            handlePrivacyGuard(app);
            handleEnableMetrics(app);
            handleNavKeys(app);
            Settings.Global.putInt(mActivity.getContentResolver(),
                    Settings.Global.DEVICE_PROVISIONED, 1);
            Settings.Secure.putInt(mActivity.getContentResolver(),
                    Settings.Secure.USER_SETUP_COMPLETE, 1);
            CMSettings.Secure.putInt(mActivity.getContentResolver(),
                    CMSettings.Secure.CM_SETUP_WIZARD_COMPLETED, 1);
            final ThemeManager tm = ThemeManager.getInstance(app);
            tm.unregisterThemeChangeListener(mActivity);
            final WallpaperManager wallpaperManager =
                    WallpaperManager.getInstance(app);
            wallpaperManager.forgetLoadedWallpaper();
            SetupWizardUtils.disableSetupWizard(app);
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Intent intent = WizardManagerHelper.getNextIntent(mActivity.getIntent(),
                    Activity.RESULT_OK);
            mActivity.startActivityForResult(intent, 1);
        }
    }
}
