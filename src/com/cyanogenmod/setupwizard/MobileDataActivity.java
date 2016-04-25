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

package com.cyanogenmod.setupwizard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.android.setupwizardlib.util.WizardManagerHelper;
import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

public class MobileDataActivity extends BaseSetupWizardActivity {

    public static final String TAG = MobileDataActivity.class.getSimpleName();

    private static final int DC_READY_TIMEOUT = 20 * 1000;

    private ViewGroup mPageView;
    private ProgressBar mProgressBar;
    private View mEnableDataRow;
    private Switch mEnableMobileData;
    private ImageView mSignalView;
    private TextView mNameView;

    private TelephonyManager mPhone;
    private SignalStrength mSignalStrength;
    private ServiceState mServiceState;

    private boolean mIsAttached = false;

    private final Handler mHandler = new Handler();

    private final Runnable mRadioReadyRunnable = new Runnable() {
        @Override
        public void run() {
            hideWaitForRadio();
        }
    };

    private final Runnable mDataConnectionReadyRunnable = new Runnable() {
        @Override
        public void run() {
            onDataStateReady();
        }
    };

    private PhoneStateListener mPhoneStateListener =
            new PhoneStateListener(SubscriptionManager.getDefaultSubscriptionId()) {

                @Override
                public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                    mSignalStrength = signalStrength;
                    updateSignalStrength();
                }

                @Override
                public void onServiceStateChanged(ServiceState state) {
                    if (SetupWizardUtils.isRadioReady(MobileDataActivity.this, state)) {
                        hideWaitForRadio();
                    }
                    mServiceState = state;
                    updateSignalStrength();
                }

                @Override
                public void onDataConnectionStateChanged(int state) {
                    if (state == TelephonyManager.DATA_CONNECTED) {
                        onDataStateReady();
                    }
                }

            };

    private View.OnClickListener mEnableDataClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = !mEnableMobileData.isChecked();
            SetupWizardUtils.setMobileDataEnabled(MobileDataActivity.this, checked);
            mEnableMobileData.setChecked(checked);
            if (checked && !SetupWizardUtils.isWifiConnected(MobileDataActivity.this)) {
                waitForData();
            } else {
                onDataStateReady();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobile_data_settings);
        TextView title = (TextView) findViewById(android.R.id.title);
        title.setText(R.string.setup_mobile_data);
        ImageView icon = (ImageView) findViewById(R.id.header_icon);
        icon.setImageResource(R.drawable.ic_mobile_data);
        icon.setVisibility(View.VISIBLE);
        setNextText(R.string.next);

        mPageView = (ViewGroup) findViewById(R.id.page_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mEnableDataRow = findViewById(R.id.data);
        mEnableDataRow.setOnClickListener(mEnableDataClickListener);
        mEnableMobileData = (Switch) findViewById(R.id.data_switch);
        mSignalView =  (ImageView) findViewById(R.id.signal);
        mNameView =  (TextView) findViewById(R.id.enable_data_title);
        updateDataConnectionStatus();
        updateSignalStrength();

    }

    @Override
    public void onPause() {
        super.onPause();
        mIsAttached = false;
        mPhone.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsAttached = true;
        mPhone = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mPhone.listen(mPhoneStateListener,
                PhoneStateListener.LISTEN_SERVICE_STATE
                        | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                        | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        updateDataConnectionStatus();
        updateSignalStrength();
        if (SetupWizardUtils.isRadioReady(this, null)) {
            hideWaitForRadio();
        } else {
            mHandler.postDelayed(mRadioReadyRunnable, SetupWizardApp.RADIO_READY_TIMEOUT);
        }
    }

    @Override
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        Intent intent = WizardManagerHelper.getNextIntent(getIntent(), Activity.RESULT_OK);
        startActivityForResult(intent, 1);
    }

    private void hideWaitForRadio() {
        if (mProgressBar.isShown()) {
            mHandler.removeCallbacks(mRadioReadyRunnable);
            // Something else, like data enablement, may have grabbed
            // the "hold" status. Kill it only if "Next" is active
            if (isNextAllowed()) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void waitForData() {
        if (!mProgressBar.isShown()) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.translucent_enter));
            mEnableDataRow.setEnabled(false);
            setNextAllowed(false);
            mHandler.postDelayed(mDataConnectionReadyRunnable, DC_READY_TIMEOUT);
        }
    }

    private void onDataStateReady() {
        mHandler.removeCallbacks(mDataConnectionReadyRunnable);
        if ((mProgressBar.isShown()) ||
                !isNextAllowed()) {
            mProgressBar.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.translucent_exit));
            mProgressBar.setVisibility(View.INVISIBLE);
            mEnableDataRow.setEnabled(true);
            setNextAllowed(true);
        }
    }

    private void updateCarrierText() {
        if (mIsAttached) {
            String name = mPhone.getSimOperatorName(SubscriptionManager.getDefaultSubscriptionId());
            if (TextUtils.isEmpty(name)) {
                name = mPhone.getNetworkOperatorName(SubscriptionManager.getDefaultSubscriptionId());
            }
            if (TextUtils.isEmpty(name)) {
                if (mServiceState != null && mServiceState.isEmergencyOnly()) {
                    name = getString(R.string.setup_mobile_data_emergency_only);
                } else {
                    name = getString(R.string.setup_mobile_data_no_service);
                }
            }
            mNameView.setText(name);
        }
    }

    private void updateSignalStrength() {
        if (mIsAttached) {
            if (!hasService()) {
                mSignalView.setImageResource(R.drawable.ic_signal_no_signal);
            } else {
                if (mSignalStrength != null) {
                    int resId;
                    switch (mSignalStrength.getLevel()) {
                        case 4:
                            resId = R.drawable.ic_signal_4;
                            break;
                        case 3:
                            resId = R.drawable.ic_signal_3;
                            break;
                        case 2:
                            resId = R.drawable.ic_signal_2;
                            break;
                        case 1:
                            resId = R.drawable.ic_signal_1;
                            break;
                        default:
                            resId = R.drawable.ic_signal_0;
                            break;
                    }
                    mSignalView.setImageResource(resId);
                }
            }
            updateCarrierText();
        }
    }

    private void updateDataConnectionStatus() {
        mEnableMobileData.setChecked(SetupWizardUtils.isMobileDataEnabled(this));
    }

    private boolean hasService() {
        boolean retVal;
        if (mServiceState != null) {
            // Consider the device to be in service if either voice or data service is available.
            // Some SIM cards are marketed as data-only and do not support voice service, and on
            // these SIM cards, we want to show signal bars for data service as well as the "no
            // service" or "emergency calls only" text that indicates that voice is not available.
            switch(mServiceState.getVoiceRegState()) {
                case ServiceState.STATE_POWER_OFF:
                    retVal = false;
                    break;
                case ServiceState.STATE_OUT_OF_SERVICE:
                case ServiceState.STATE_EMERGENCY_ONLY:
                    retVal = mServiceState.getDataRegState() == ServiceState.STATE_IN_SERVICE;
                    break;
                default:
                    retVal = true;
            }
        } else {
            retVal = false;
        }
        return retVal;
    }

}
