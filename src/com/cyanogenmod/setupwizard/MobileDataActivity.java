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

import static com.cyanogenmod.setupwizard.SetupWizardApp.LOGV;

import android.os.Bundle;
import android.os.Handler;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.cyanogenmod.setupwizard.util.NetworkMonitor;
import com.cyanogenmod.setupwizard.util.PhoneMonitor;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

public class MobileDataActivity extends BaseSetupWizardActivity {

    public static final String TAG = MobileDataActivity.class.getSimpleName();

    private static final int DC_READY_TIMEOUT = 20 * 1000;

    private ProgressBar mProgressBar;
    private View mEnableDataRow;
    private Switch mEnableMobileData;
    private ImageView mSignalView;
    private TextView mNameView;

    private TelephonyManager mPhone;
    private SignalStrength mSignalStrength;
    private ServiceState mServiceState;
    private PhoneMonitor mPhoneMonitor;
    private NetworkMonitor mNetworkMonitor;

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

    private PhoneMonitor.SubscriptionStateListener mSubscriptionStateListener =
            new PhoneMonitor.SubscriptionStateListener() {
                @Override
                public void onServiceStateChanged(int subId, ServiceState serviceState) {
                    if (LOGV) {
                        Log.v(TAG, "onServiceStateChanged{" +
                                "subId='" + subId + '\'' +
                                ", serviceState=" + serviceState.toString() +
                                '}');
                    }
                    if (SetupWizardUtils.isRadioReady(MobileDataActivity.this, serviceState)) {
                        hideWaitForRadio();
                    }
                    mServiceState = serviceState;
                    updateSignalStrength();
                }

                @Override
                public void onDataConnectionStateChanged(int subId, int state, int networkType) {
                    if (LOGV) {
                        Log.v(TAG, "onDataConnectionStateChanged{" +
                                "subId='" + subId + '\'' +
                                ", state=" + state +
                                '}');
                    }
                    if (state == TelephonyManager.DATA_CONNECTED) {
                        onDataStateReady();
                    }
                }

                @Override
                public void onDefaultDataSubscriptionChanged(int subId) {}

                @Override
                public void onDefaultDataSubscriptionChangeRequested(int currentSubId,
                        int newSubId) {}

                @Override
                public void onSignalStrengthsChanged(int subId, SignalStrength signalStrength) {
                    if (LOGV) {
                        Log.v(TAG, "onSignalStrengthsChanged{" +
                                "subId='" + subId + '\'' +
                                ", signalStrength=" + signalStrength.toString() +
                                '}');
                    }
                    mSignalStrength = signalStrength;
                    updateSignalStrength();
                }

                @Override
                public void onSimStateChanged(int subId, int simState) {
                    if (LOGV) {
                        Log.v(TAG, "onSimStateChanged{" +
                                "subId='" + subId + '\'' +
                                ", simState=" + simState +
                                '}');
                    }
                }
            };

    private View.OnClickListener mEnableDataClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = !mEnableMobileData.isChecked();
            SetupWizardUtils.setMobileDataEnabled(MobileDataActivity.this, checked);
            mEnableMobileData.setChecked(checked);
            if (checked && !mNetworkMonitor.isWifiConnected()) {
                waitForData();
            } else {
                onDataStateReady();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhoneMonitor = PhoneMonitor.getInstance();
        mNetworkMonitor = NetworkMonitor.getInstance();
        setNextText(R.string.next);

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
    public void onResume() {
        super.onResume();
        mIsAttached = true;
        mPhone = getSystemService(TelephonyManager.class);
        mPhoneMonitor.addListener(mSubscriptionStateListener);
        updateDataConnectionStatus();
        updateSignalStrength();
        if (SetupWizardUtils.isRadioReady(this, null)) {
            hideWaitForRadio();
        } else {
            mHandler.postDelayed(mRadioReadyRunnable, SetupWizardApp.RADIO_READY_TIMEOUT);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsAttached = false;
        mPhoneMonitor.removeListener(mSubscriptionStateListener);
    }

    @Override
    protected int getTransition() {
        return TRANSITION_ID_SLIDE;
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
            if (LOGV) {
                Log.v(TAG, "updateSignalStrength{" +
                        "signalStrength='" + mSignalStrength + '\'' +
                        "signalStrengthLevel='" + ((mSignalStrength != null) ?
                        mSignalStrength.getLevel() : "null") + '\'' +
                        '}');
            }
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
        if (mServiceState == null) {
            mServiceState  =  TelephonyManager.from(this)
                    .getServiceStateForSubscriber(SubscriptionManager.getDefaultSubscriptionId());
        }
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

    @Override
    protected int getLayoutResId() {
        return R.layout.mobile_data_settings;
    }

    @Override
    protected int getTitleResId() {
        return R.string.setup_mobile_data;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_mobile_data;
    }

}
