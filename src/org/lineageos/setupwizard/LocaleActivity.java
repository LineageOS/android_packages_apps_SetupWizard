/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.util.LocaleUtils;

import com.google.android.setupcompat.util.SystemBarHelper;

import org.lineageos.setupwizard.widget.LocalePicker;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocaleActivity extends BaseSetupWizardActivity {

    private static final String TAG = LocaleActivity.class.getSimpleName();

    private ArrayAdapter<com.android.internal.app.LocalePicker.LocaleInfo> mLocaleAdapter;
    private Locale mCurrentLocale;
    private int[] mAdapterIndices;
    private LocalePicker mLanguagePicker;
    private ExecutorService mFetchUpdateSimLocaleTask;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mPendingLocaleUpdate;
    private boolean mPaused = true;

    private final Runnable mUpdateLocale = new Runnable() {
        public void run() {
            if (mCurrentLocale != null) {
                mLanguagePicker.setEnabled(false);
                com.android.internal.app.LocalePicker.updateLocale(mCurrentLocale);
            }
        }
    };

    private final BroadcastReceiver mSimChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
                fetchAndUpdateSimLocale();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemBarHelper.setBackButtonVisible(getWindow(), true);
        setNextText(R.string.next);
        mLanguagePicker = findViewById(R.id.locale_list);
        mLanguagePicker.setNextRight(getNextButton().getId());
        mLanguagePicker.requestFocus();
        if (getResources().getBoolean(R.bool.config_isLargeNoTouch)) {
            mLanguagePicker.setOnClickListener((View v) -> getNextButton().performClick());
        }
        loadLanguages();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
        unregisterReceiver(mSimChangedReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPaused = false;
        registerReceiver(mSimChangedReceiver,
                new IntentFilter(TelephonyIntents.ACTION_SIM_STATE_CHANGED));
        if (mLanguagePicker != null) {
            mLanguagePicker.setEnabled(true);
        }
        if (mPendingLocaleUpdate) {
            mPendingLocaleUpdate = false;
            fetchAndUpdateSimLocale();
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setup_locale;
    }

    @Override
    protected int getTitleResId() {
        return R.string.setup_locale;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_locale;
    }

    private void loadLanguages() {
        mLocaleAdapter = com.android.internal.app.LocalePicker.constructAdapter(this,
                R.layout.locale_picker_item, R.id.locale);
        mCurrentLocale = Locale.getDefault();
        fetchAndUpdateSimLocale();
        mAdapterIndices = new int[mLocaleAdapter.getCount()];
        int currentLocaleIndex = 0;
        String[] labels = new String[mLocaleAdapter.getCount()];
        for (int i = 0; i < mAdapterIndices.length; i++) {
            com.android.internal.app.LocalePicker.LocaleInfo localLocaleInfo =
                    mLocaleAdapter.getItem(i);
            Locale localLocale = localLocaleInfo.getLocale();
            if (localLocale.equals(mCurrentLocale)) {
                currentLocaleIndex = i;
            }
            mAdapterIndices[i] = i;
            labels[i] = localLocaleInfo.getLabel();
        }
        mLanguagePicker.setDisplayedValues(labels);
        mLanguagePicker.setMaxValue(labels.length - 1);
        mLanguagePicker.setValue(currentLocaleIndex);
        mLanguagePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mLanguagePicker.setOnValueChangedListener((pkr, oldVal, newVal) -> setLocaleFromPicker());

        mLanguagePicker.setOnScrollListener((view, scrollState) -> {
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                ((SetupWizardApp) getApplication()).setIgnoreSimLocale(true);
            }
        });
    }

    private void setLocaleFromPicker() {
        ((SetupWizardApp) getApplication()).setIgnoreSimLocale(true);
        int i = mAdapterIndices[mLanguagePicker.getValue()];
        final com.android.internal.app.LocalePicker.LocaleInfo localLocaleInfo =
                mLocaleAdapter.getItem(i);
        onLocaleChanged(localLocaleInfo.getLocale());
    }

    private void onLocaleChanged(Locale paramLocale) {
        mLanguagePicker.setEnabled(true);
        mHandler.removeCallbacks(mUpdateLocale);
        mCurrentLocale = paramLocale;
        mHandler.postDelayed(mUpdateLocale, 1000);
    }

    private void fetchAndUpdateSimLocale() {
        if (((SetupWizardApp) getApplication()).ignoreSimLocale() || isDestroyed()) {
            return;
        }
        if (mPaused) {
            mPendingLocaleUpdate = true;
            return;
        }
        if (mFetchUpdateSimLocaleTask != null) {
            mFetchUpdateSimLocaleTask.shutdown();
        }
        mFetchUpdateSimLocaleTask = Executors.newSingleThreadExecutor();
        mFetchUpdateSimLocaleTask.execute(() -> {
            Locale locale = null;
            Activity activity = LocaleActivity.this;
            if (!activity.isFinishing() || !activity.isDestroyed()) {
                // If the sim is currently pin locked, return
                TelephonyManager telephonyManager = (TelephonyManager)
                        activity.getSystemService(Context.TELEPHONY_SERVICE);
                int state = telephonyManager.getSimState();
                if (state == TelephonyManager.SIM_STATE_PIN_REQUIRED ||
                        state == TelephonyManager.SIM_STATE_PUK_REQUIRED) {
                    return;
                }

                final SubscriptionManager subscriptionManager =
                        activity.getSystemService(SubscriptionManager.class);
                List<SubscriptionInfo> activeSubs =
                        subscriptionManager.getActiveSubscriptionInfoList();
                if (activeSubs == null || activeSubs.isEmpty()) {
                    return;
                }

                // Fetch locale for active sim's MCC
                final String mccString = activeSubs.get(0).getMccString();
                try {
                    if (mccString != null && !mccString.isEmpty()) {
                        int mcc = Integer.parseInt(mccString);
                        locale = LocaleUtils.getLocaleFromMcc(activity, mcc, null);
                    } else {
                        Log.w(TAG, "Unexpected mccString: '" + mccString + "'");
                    }
                } catch (NumberFormatException e) {
                    Log.w(TAG, "mccString not a number: '" + mccString + "'", e);
                }

                // If that fails, fall back to preferred languages reported
                // by the sim
                if (locale == null) {
                    Locale simLocale = telephonyManager.getSimLocale();
                    if (simLocale != null) {
                        locale = simLocale;
                    }
                }
                Locale finalLocale = locale;
                mHandler.post(() -> {
                    if (finalLocale != null && !finalLocale.equals(mCurrentLocale)) {
                        if (!((SetupWizardApp) getApplication()).ignoreSimLocale()
                                && !isDestroyed()) {
                            String label = getString(R.string.sim_locale_changed,
                                    finalLocale.getDisplayName());
                            Toast.makeText(LocaleActivity.this, label, Toast.LENGTH_SHORT).show();
                            onLocaleChanged(finalLocale);
                            ((SetupWizardApp) getApplication()).setIgnoreSimLocale(true);
                        }
                    }
                });
            }
        });
    }
}
