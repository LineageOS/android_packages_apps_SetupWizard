/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard;

import static android.view.View.INVISIBLE;

import static com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP;

import static org.lineageos.setupwizard.SetupWizardApp.ACTION_SETUP_COMPLETE;
import static org.lineageos.setupwizard.SetupWizardApp.LOGV;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.google.android.setupdesign.transition.TransitionHelper;
import com.google.android.setupdesign.util.ThemeHelper;

import org.lineageos.setupwizard.NavigationLayout.NavigationBarListener;
import org.lineageos.setupwizard.util.SetupWizardUtils;

public abstract class BaseSetupWizardActivity extends AppCompatActivity implements
        NavigationBarListener {

    public static final String TAG = BaseSetupWizardActivity.class.getSimpleName();

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
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            BaseSetupWizardActivity.this::onActivityResult);

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
                TransitionHelper.applyBackwardTransition(BaseSetupWizardActivity.this,
                        TransitionHelper.TRANSITION_FADE_THROUGH, true);
            }
        });
    }

    @Override
    protected void onStart() {
        if (LOGV) {
            logActivityState("onStart");
        }
        super.onStart();
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
        intent.putExtra(WizardManagerHelper.EXTRA_THEME, ThemeHelper.THEME_GLIF_V4);
        super.startActivity(intent);
        TransitionHelper.applyForwardTransition(this,
                TransitionHelper.TRANSITION_FADE_THROUGH, true);
    }

    protected final void startActivityForResult(@NonNull Intent intent) {
        intent.putExtra(WizardManagerHelper.EXTRA_IS_FIRST_RUN, isFirstRun());
        intent.putExtra(WizardManagerHelper.EXTRA_IS_SETUP_FLOW, true);
        intent.putExtra(WizardManagerHelper.EXTRA_THEME, ThemeHelper.THEME_GLIF_V4);
        activityResultLauncher.launch(intent);
        TransitionHelper.applyForwardTransition(this,
                TransitionHelper.TRANSITION_FADE_THROUGH, true);
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

    protected final boolean tryEnablingWifi() {
        WifiManager wifiManager = getSystemService(WifiManager.class);
        return wifiManager != null && wifiManager.setWifiEnabled(true);
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
