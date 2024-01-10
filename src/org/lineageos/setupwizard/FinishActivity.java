/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017-2024 The LineageOS Project
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

package org.lineageos.setupwizard;

import static android.os.Binder.getCallingUserHandle;
import static android.os.UserHandle.USER_CURRENT;

import static org.lineageos.setupwizard.Manifest.permission.FINISH_SETUP;
import static org.lineageos.setupwizard.SetupWizardApp.ACTION_FINISHED;
import static org.lineageos.setupwizard.SetupWizardApp.ACTION_SETUP_COMPLETE;
import static org.lineageos.setupwizard.SetupWizardApp.DISABLE_NAV_KEYS;
import static org.lineageos.setupwizard.SetupWizardApp.ENABLE_RECOVERY_UPDATE;
import static org.lineageos.setupwizard.SetupWizardApp.KEY_SEND_METRICS;
import static org.lineageos.setupwizard.SetupWizardApp.LOGV;
import static org.lineageos.setupwizard.SetupWizardApp.NAVIGATION_OPTION_KEY;
import static org.lineageos.setupwizard.SetupWizardApp.UPDATE_RECOVERY_PROP;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.om.IOverlayManager;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;

import org.lineageos.setupwizard.util.SetupWizardUtils;

import lineageos.providers.LineageSettings;

public class FinishActivity extends BaseSetupWizardActivity {

    public static final String TAG = FinishActivity.class.getSimpleName();

    private ImageView mReveal;

    private SetupWizardApp mSetupWizardApp;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean mIsFinishing;

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (LOGV) {
                Log.v(TAG, "onReceive intent=" + intent);
            }
            if (intent != null && intent.getAction().equals(ACTION_FINISHED)) {
                unregisterReceiver(mIntentReceiver);
                completeSetup();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LOGV) {
            logActivityState("onCreate savedInstanceState=" + savedInstanceState);
        }
        mSetupWizardApp = (SetupWizardApp) getApplication();
        mReveal = findViewById(R.id.reveal);
        setNextText(R.string.start);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.finish_activity;
    }

    @Override
    public void onNavigateNext() {
        startFinishSequence();
    }

    private void startFinishSequence() {
        if (mIsFinishing) {
            return;
        }
        mIsFinishing = true;

        // Listen for completion from the exit service.
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_FINISHED);
        registerReceiver(mIntentReceiver, filter, null, null);

        Intent i = new Intent(ACTION_SETUP_COMPLETE);
        i.setPackage(getPackageName());
        sendBroadcastAsUser(i, getCallingUserHandle(), FINISH_SETUP);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        hideNextButton();

        // Begin outro animation.
        setupRevealImage();
    }

    private void setupRevealImage() {
        Rect rect = getWindowManager().getCurrentWindowMetrics().getBounds();
        final Point p = new Point(rect.width(), rect.height());
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
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mReveal.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mHandler.post(() -> {
                    if (LOGV) {
                        Log.v(TAG, "Animation ended");
                    }
                    // Start exit procedures, including the exit service.
                    SetupWizardUtils.startSetupWizardExitProcedure(FinishActivity.this);
                });
            }
        });
        anim.start();
    }

    private void completeSetup() {
        Log.i(TAG, "Setup complete!");
        handleEnableMetrics(mSetupWizardApp);
        handleNavKeys(mSetupWizardApp);
        handleRecoveryUpdate(mSetupWizardApp);
        handleNavigationOption(mSetupWizardApp);
        final WallpaperManager wallpaperManager =
                WallpaperManager.getInstance(mSetupWizardApp);
        wallpaperManager.forgetLoadedWallpaper();
        finishAllAppTasks();
        SetupWizardUtils.enableStatusBar();
    }

    private static void handleEnableMetrics(SetupWizardApp setupWizardApp) {
        Bundle privacyData = setupWizardApp.getSettingsBundle();
        if (privacyData != null
                && privacyData.containsKey(KEY_SEND_METRICS)) {
            LineageSettings.Secure.putInt(setupWizardApp.getContentResolver(),
                    LineageSettings.Secure.STATS_COLLECTION,
                    privacyData.getBoolean(KEY_SEND_METRICS)
                            ? 1 : 0);
        }
    }

    private static void handleNavKeys(SetupWizardApp setupWizardApp) {
        if (setupWizardApp.getSettingsBundle().containsKey(DISABLE_NAV_KEYS)) {
            writeDisableNavkeysOption(setupWizardApp,
                    setupWizardApp.getSettingsBundle().getBoolean(DISABLE_NAV_KEYS));
        }
    }

    private static void handleRecoveryUpdate(SetupWizardApp setupWizardApp) {
        if (setupWizardApp.getSettingsBundle().containsKey(ENABLE_RECOVERY_UPDATE)) {
            boolean update = setupWizardApp.getSettingsBundle()
                    .getBoolean(ENABLE_RECOVERY_UPDATE);

            SystemProperties.set(UPDATE_RECOVERY_PROP, String.valueOf(update));
        }
    }

    private void handleNavigationOption(Context context) {
        Bundle settingsBundle = mSetupWizardApp.getSettingsBundle();
        if (settingsBundle.containsKey(NAVIGATION_OPTION_KEY)) {
            IOverlayManager overlayManager = IOverlayManager.Stub.asInterface(
                    ServiceManager.getService(Context.OVERLAY_SERVICE));
            String selectedNavMode = settingsBundle.getString(NAVIGATION_OPTION_KEY);

            try {
                overlayManager.setEnabledExclusiveInCategory(selectedNavMode, USER_CURRENT);
            } catch (Exception ignored) {
            }
        }
    }

    private static void writeDisableNavkeysOption(Context context, boolean enabled) {
        final boolean virtualKeysEnabled = LineageSettings.System.getIntForUser(
                context.getContentResolver(), LineageSettings.System.FORCE_SHOW_NAVBAR, 0,
                UserHandle.USER_CURRENT) != 0;
        if (enabled != virtualKeysEnabled) {
            LineageSettings.System.putIntForUser(context.getContentResolver(),
                    LineageSettings.System.FORCE_SHOW_NAVBAR, enabled ? 1 : 0,
                    UserHandle.USER_CURRENT);
        }
    }
}
