/*
 * Copyright (C) 2016 The CyanogenMod Project
 *               2017-2022 The LineageOS Project
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

import static android.view.View.INVISIBLE;

import static com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP;

import static org.lineageos.setupwizard.SetupWizardApp.ACTION_SETUP_COMPLETE;
import static org.lineageos.setupwizard.SetupWizardApp.LOGV;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.settingslib.Utils;

import com.google.android.setupcompat.util.WizardManagerHelper;
import com.google.android.setupdesign.GlifLayout;

import org.lineageos.setupwizard.NavigationLayout.NavigationBarListener;
import org.lineageos.setupwizard.util.SetupWizardUtils;

import java.util.List;

public abstract class BaseSetupWizardActivity extends AppCompatActivity implements
        NavigationBarListener {

    public static final String TAG = BaseSetupWizardActivity.class.getSimpleName();

    protected static final int TRANSITION_ID_NONE = -1;
    protected static final int TRANSITION_ID_DEFAULT = 1;
    protected static final int TRANSITION_ID_SLIDE = 2;
    protected static final int TRANSITION_ID_FADE = 3;

    private NavigationLayout mNavigationBar;

    private final BroadcastReceiver finishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_SETUP_COMPLETE.equals(intent.getAction())) {
                if (BaseSetupWizardActivity.this instanceof FinishActivity) return;
                if (mNavigationBar != null) {
                    // hide the activity's view, so it does not pop up again
                    mNavigationBar.getRootView().setVisibility(INVISIBLE);
                }
            }
        }
    };
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (LOGV) {
            logActivityState("onCreate savedInstanceState=" + savedInstanceState);
        }
        super.onCreate(savedInstanceState);
        registerReceiver(finishReceiver, new IntentFilter(ACTION_SETUP_COMPLETE));
        initLayout();
        mNavigationBar = getNavigationBar();
        if (mNavigationBar != null) {
            mNavigationBar.setNavigationBarListener(this);
        }
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (LOGV) {
                    Log.v(TAG, "handleOnBackPressed()");
                }
                finishAction(RESULT_CANCELED, new Intent().putExtra("onBackPressed", true));
            }
        });
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                BaseSetupWizardActivity.this::onActivityResult);
    }

    @Override
    protected void onStart() {
        if (LOGV) {
            logActivityState("onStart");
        }
        super.onStart();
        if (!SetupWizardUtils.isManagedProfile(this)) {
            exitIfSetupComplete();
        }
    }

    @Override
    protected void onRestart() {
        if (LOGV) {
            logActivityState("onRestart");
        }
        super.onRestart();
    }

    @Override
    protected void onResume() {
        if (LOGV) {
            logActivityState("onResume");
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (LOGV) {
            logActivityState("onPause");
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (LOGV) {
            logActivityState("onStop");
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (LOGV) {
            logActivityState("onDestroy");
        }
        unregisterReceiver(finishReceiver);
        super.onDestroy();
    }

    @Override
    public void onAttachedToWindow() {
        if (LOGV) {
            logActivityState("onAttachedToWindow");
        }
        super.onAttachedToWindow();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        if (LOGV) {
            Log.v(TAG, "onRestoreInstanceState(" + savedInstanceState + ")");
        }
        super.onRestoreInstanceState(savedInstanceState);
        int currentId = savedInstanceState.getInt("currentFocus", -1);
        if (currentId != -1) {
            View view = findViewById(currentId);
            if (view != null) {
                view.requestFocus();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        View current = getCurrentFocus();
        outState.putInt("currentFocus", current != null ? current.getId() : -1);
        if (LOGV) {
            Log.v(TAG, "onSaveInstanceState(" + outState + ")");
        }
    }

    /**
     * @return The navigation bar instance in the layout, or null if the layout does not have a
     * navigation bar.
     */
    public NavigationLayout getNavigationBar() {
        final View view = findViewById(R.id.navigation_bar);
        return view instanceof NavigationLayout ? (NavigationLayout) view : null;
    }

    public final void setNextAllowed(boolean allowed) {
        if (mNavigationBar != null) {
            mNavigationBar.getNextButton().setEnabled(allowed);
        }
    }

    protected boolean isNextAllowed() {
        if (mNavigationBar != null) {
            mNavigationBar.getNextButton().isEnabled();
        }
        return false;
    }

    protected void onNextPressed() {
        nextAction(RESULT_OK);
    }

    protected void onSkipPressed() {
        nextAction(RESULT_SKIP);
    }

    protected final void setNextText(int resId) {
        if (mNavigationBar != null) {
            mNavigationBar.getNextButton().setText(resId);
        }
    }

    public Button getNextButton() {
        return mNavigationBar.getNextButton();
    }

    protected final void setSkipText(int resId) {
        if (mNavigationBar != null) {
            mNavigationBar.getSkipButton().setText(resId);
        }
    }

    protected final void hideNextButton() {
        if (mNavigationBar != null) {
            final Button next = mNavigationBar.getNextButton();
            next.setVisibility(INVISIBLE);
        }
    }

    public void onNavigateBack() {
        getOnBackPressedDispatcher().onBackPressed();
    }

    public void onNavigateNext() {
        onNextPressed();
    }

    public void onSkip() {
        onSkipPressed();
    }

    protected final void onSetupStart() {
        if (SetupWizardUtils.isOwner()) {
            tryEnablingWifi();
        }
    }

    private void exitIfSetupComplete() {
        if (WizardManagerHelper.isUserSetupComplete(this)) {
            Log.i(TAG, "Starting activity with USER_SETUP_COMPLETE=true");
            startSetupWizardExitActivity();
            setResult(RESULT_CANCELED, null);
            finishAllAppTasks();
        }
    }

    protected final void finishAllAppTasks() {
        List<ActivityManager.AppTask> appTasks =
                getSystemService(ActivityManager.class).getAppTasks();

        for (ActivityManager.AppTask task : appTasks) {
            if (LOGV) {
                Log.v(TAG, "Finishing task=" + task.toString());
            }
            task.finishAndRemoveTask();
        }
        finish();
    }

    public void finish() {
        if (LOGV) {
            Log.v(TAG, "finish");
        }
        super.finish();
    }

    protected final void finishAction(int resultCode) {
        finishAction(resultCode, null);
    }

    protected final void finishAction(int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED) {
            nextAction(resultCode, data);
        } else {
            setResult(resultCode, data);
        }
        finish();
    }

    protected final void nextAction(int resultCode) {
        nextAction(resultCode, null);
    }

    protected final void nextAction(int resultCode, Intent data) {
        if (LOGV) {
            Log.v(TAG, "nextAction resultCode=" + resultCode +
                    " data=" + data + " this=" + this);
        }
        if (resultCode == RESULT_CANCELED) {
            throw new IllegalArgumentException("Cannot call nextAction with RESULT_CANCELED");
        }
        setResult(resultCode, data);
        Intent intent = WizardManagerHelper.getNextIntent(getIntent(), resultCode, data);
        startActivityForResult(intent);
    }

    @Override
    public void startActivity(Intent intent) {
        intent.putExtra(WizardManagerHelper.EXTRA_IS_FIRST_RUN, isFirstRun());
        intent.putExtra(WizardManagerHelper.EXTRA_IS_SETUP_FLOW, true);
        super.startActivity(intent);
    }

    protected final void startActivityForResult(@NonNull Intent intent) {
        intent.putExtra(WizardManagerHelper.EXTRA_IS_FIRST_RUN, isFirstRun());
        intent.putExtra(WizardManagerHelper.EXTRA_IS_SETUP_FLOW, true);
        activityResultLauncher.launch(intent);
    }

    protected void onActivityResult(ActivityResult activityResult) {
        int resultCode = activityResult.getResultCode();
        Intent data = activityResult.getData();
        if (LOGV) {
            StringBuilder append = new StringBuilder().append("onActivityResult(")
                    .append(resultCode).append(", ");
            Bundle extras = null;
            if (data != null) {
                extras = data.getExtras();
            }
            Log.v(TAG, append.append(extras).append(")").toString());
        }
    }

    protected final void applyForwardTransition(int transitionId) {
        if (transitionId == TRANSITION_ID_SLIDE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.sud_slide_next_in,
                    R.anim.sud_slide_next_out);
        } else if (transitionId == TRANSITION_ID_FADE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in,
                    android.R.anim.fade_out);
        } else if (transitionId == TRANSITION_ID_DEFAULT) {
            TypedArray typedArray = obtainStyledAttributes(android.R.style.Animation_Activity,
                    new int[]{android.R.attr.activityOpenEnterAnimation,
                            android.R.attr.activityOpenExitAnimation});
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, typedArray.getResourceId(0, 0),
                    typedArray.getResourceId(1, 0));
            typedArray.recycle();
        } else if (transitionId == TRANSITION_ID_NONE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0);
        }
    }

    protected final void applyBackwardTransition(int transitionId) {
        if (transitionId == TRANSITION_ID_SLIDE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, R.anim.sud_slide_back_in,
                    R.anim.sud_slide_back_out);
        } else if (transitionId == TRANSITION_ID_FADE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in,
                    android.R.anim.fade_out);
        } else if (transitionId == TRANSITION_ID_DEFAULT) {
            TypedArray typedArray = obtainStyledAttributes(android.R.style.Animation_Activity,
                    new int[]{android.R.attr.activityCloseEnterAnimation,
                            android.R.attr.activityCloseExitAnimation});
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, typedArray.getResourceId(0, 0),
                    typedArray.getResourceId(1, 0));
            typedArray.recycle();
        } else if (transitionId == TRANSITION_ID_NONE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0);
        }
    }

    protected final boolean tryEnablingWifi() {
        WifiManager wifiManager = getSystemService(WifiManager.class);
        return wifiManager.setWifiEnabled(true);
    }

    private void startSetupWizardExitActivity() {
        if (LOGV) {
            Log.v(TAG, "startSetupWizardExitActivity()");
        }
        startActivity(new Intent(this, SetupWizardExitActivity.class));
    }

    private boolean isFirstRun() {
        return true;
    }

    protected final void logActivityState(String prefix) {
        Log.v(TAG, prefix + " isResumed=" + isResumed() + " isFinishing=" +
                isFinishing() + " isDestroyed=" + isDestroyed());
    }

    private void initLayout() {
        if (getLayoutResId() != -1) {
            setContentView(getLayoutResId());
        }
        if (getTitleResId() != -1) {
            final CharSequence headerText = TextUtils.expandTemplate(getText(getTitleResId()));
            getGlifLayout().setHeaderText(headerText);
        }
        if (getIconResId() != -1) {
            final GlifLayout layout = getGlifLayout();
            final Drawable icon = getDrawable(getIconResId()).mutate();
            icon.setTintList(Utils.getColorAccent(layout.getContext()));
            layout.setIcon(icon);
        }
    }

    protected GlifLayout getGlifLayout() {
        return requireViewById(R.id.setup_wizard_layout);
    }

    protected int getLayoutResId() {
        return -1;
    }

    protected int getTitleResId() {
        return -1;
    }

    protected int getIconResId() {
        return -1;
    }
}
