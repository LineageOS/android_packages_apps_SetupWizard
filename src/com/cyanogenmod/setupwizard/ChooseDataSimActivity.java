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
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cyanogenmod.setupwizard.util.PhoneMonitor;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

import java.util.List;

public class ChooseDataSimActivity extends BaseSetupWizardActivity {

    public static final String TAG = ChooseDataSimActivity.class.getSimpleName();

    private ViewGroup mPageView;
    private ProgressBar mProgressBar;
    private SparseArray<TextView> mNameViews;
    private SparseArray<ImageView> mSignalViews;
    private SparseArray<CheckBox> mCheckBoxes;
    private SparseArray<View> mRows;

    private SparseArray<SubscriptionInfo> mSubInfoRecords;
    private SparseArray<SignalStrength> mSignalStrengths;
    private SparseArray<ServiceState> mServiceStates;

    private boolean mIsAttached = false;
    private boolean mRadioReady = false;

    private PhoneMonitor mPhoneMonitor;

    private boolean mDisabledForSwitch = false;

    private final Handler mHandler = new Handler();

    private final Runnable mRadioReadyRunnable = new Runnable() {
        @Override
        public void run() {
            // If we timeout out waiting for the radio, Oh well.
            if (!mRadioReady) {
                mRadioReady = true;
                checkForRadioReady();
            }
        }
    };

    private View.OnClickListener mSetDataSimClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SubscriptionInfo subInfoRecord = (SubscriptionInfo)view.getTag();
            if (subInfoRecord != null) {
                changeDataSub(subInfoRecord);
            }
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
                    SubscriptionInfo subInfoRecord =
                            mPhoneMonitor.getActiveSubscriptionInfo(subId);
                    mRadioReady = SetupWizardUtils.isRadioReady(
                            ChooseDataSimActivity.this, serviceState);
                    checkForRadioReady();
                    mServiceStates.put(subInfoRecord.getSimSlotIndex(), serviceState);
                    updateSignalStrength(subInfoRecord);
                }

                @Override
                public void onDataConnectionStateChanged(int subId, int state, int networkType) {
                    if (LOGV) {
                        Log.v(TAG, "onDataConnectionStateChanged{" +
                                "subId='" + subId + '\'' +
                                ", state=" + state +
                                '}');
                    }
                    onDefaultDataSubscriptionChanged(subId);
                }

                @Override
                public void onDefaultDataSubscriptionChanged(int subId) {
                    if (LOGV) {
                        Log.v(TAG, "onDefaultDataSubscriptionChanged{" +
                                "subId='" + subId + '\'' +
                                '}');
                    }
                    final int currentDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
                    if (currentDataSubId != subId) {
                        updateCurrentDataSub();
                        hideProgress();
                        enableViews(true);
                    }
                    checkSimChangingState(currentDataSubId, subId);
                }

                @Override
                public void onDefaultDataSubscriptionChangeRequested(int currentSubId,
                        int newSubId) {
                    if (LOGV) {
                        Log.v(TAG, "onDefaultDataSubscriptionChangeRequested{" +
                                "currentSubId='" + currentSubId + '\'' +
                                ", newSubId=" + newSubId +
                                '}');
                    }
                    checkSimChangingState(currentSubId, newSubId);
                }

                @Override
                public void onSignalStrengthsChanged(int subId, SignalStrength signalStrength) {
                    if (LOGV) {
                        Log.v(TAG, "onSignalStrengthsChanged{" +
                                "subId='" + subId + '\'' +
                                ", signalStrength=" + signalStrength.toString() +
                                '}');
                    }
                    SubscriptionInfo subInfoRecord =
                            mPhoneMonitor.getActiveSubscriptionInfo(subId);
                    mSignalStrengths.put(subInfoRecord.getSimSlotIndex(), signalStrength);
                    updateSignalStrength(subInfoRecord);
                }

                @Override
                public void onSimStateChanged(int subId, int simState) {
                    if (LOGV) {
                        Log.v(TAG, "onSimStateChanged{" +
                                "subId='" + subId + '\'' +
                                ", simState=" + simState +
                                '}');
                    }
                    updateSignalStrengths();
                    updateCurrentDataSub();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextText(R.string.next);
        mPhoneMonitor = PhoneMonitor.getInstance();
        mPageView = (ViewGroup) findViewById(R.id.page_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        List<SubscriptionInfo> subInfoRecords = mPhoneMonitor.getActiveSubscriptionInfoList();
        int simCount =
                subInfoRecords != null ? subInfoRecords.size() : 0;
        mSubInfoRecords = new SparseArray<>(simCount);
        for (SubscriptionInfo subInfoRecord : subInfoRecords) {
            mSubInfoRecords.put(subInfoRecord.getSimSlotIndex(), subInfoRecord);
            updateSignalStrength(subInfoRecord);
        }
        mNameViews = new SparseArray<>(simCount);
        mSignalViews = new SparseArray<>(simCount);
        mCheckBoxes = new SparseArray<>(simCount);
        mRows = new SparseArray<>(simCount);
        mServiceStates = new SparseArray<>(simCount);
        mSignalStrengths = new SparseArray<>(simCount);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < simCount; i++) {
            View simRow = inflater.inflate(R.layout.data_sim_row, null);
            mPageView.addView(simRow);
            SubscriptionInfo subInfoRecord = mSubInfoRecords.valueAt(i);
            simRow.setTag(subInfoRecord);
            simRow.setOnClickListener(mSetDataSimClickListener);
            int slot = subInfoRecord.getSimSlotIndex();
            mNameViews.put(slot, (TextView) simRow.findViewById(R.id.sim_title));
            mSignalViews.put(slot, (ImageView) simRow.findViewById(R.id.signal));
            mCheckBoxes.put(slot, (CheckBox) simRow.findViewById(R.id.enable_check));
            mRows.put(slot, simRow);
            mPageView.addView(inflater.inflate(R.layout.divider, null));
        }
        updateSignalStrengths();
        updateCurrentDataSub();

    }

    @Override
    public void onPause() {
        super.onPause();
        mIsAttached = false;
        mPhoneMonitor.removeListener(mSubscriptionStateListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsAttached = true;
        mPhoneMonitor.addListener(mSubscriptionStateListener);
        mRadioReady = SetupWizardUtils.isRadioReady(this, null);
        updateSignalStrengths();
        updateCurrentDataSub();
        checkForRadioReady();
        if (mRadioReady) {
            final int currentDataSub = SubscriptionManager.getDefaultDataSubscriptionId();
            checkSimChangingState(currentDataSub, currentDataSub);
        }
    }

    @Override
    protected int getTransition() {
        return TRANSITION_ID_SLIDE;
    }

    private void checkForRadioReady() {
        if (mRadioReady) {
            mHandler.removeCallbacks(mRadioReadyRunnable);
            showPage();
            final int currentDataSub = SubscriptionManager.getDefaultDataSubscriptionId();
            checkSimChangingState(currentDataSub, currentDataSub);
            return;
        } else {
            enableViews(false);
            showProgress();
            if (!mHandler.hasCallbacks(mRadioReadyRunnable)) {
                mHandler.postDelayed(mRadioReadyRunnable, SetupWizardApp.RADIO_READY_TIMEOUT);
            }
        }
    }

    private void showPage() {
        mPageView.setVisibility(View.VISIBLE);
        if (!mPageView.isShown()) {
            mPageView.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.translucent_enter));
        }
    }

    private void showProgress() {
        if (!mProgressBar.isShown()) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.translucent_enter));
        }
    }

    private void hideProgress() {
        if (mProgressBar.isShown()) {
            mProgressBar.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.translucent_exit));
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void updateSignalStrengths() {
        if (mIsAttached) {
            for (int i = 0; i < mSubInfoRecords.size(); i++) {
                updateSignalStrength(mSubInfoRecords.valueAt(i));
            }
        }
    }

    private void changeDataSub(SubscriptionInfo subInfoRecord) {
        final int currentDataSub = SubscriptionManager.getDefaultDataSubscriptionId();
        final int requestedDataSub = subInfoRecord.getSubscriptionId();
        if (LOGV) {
            Log.v(TAG, "changeDataSub{" +
                    "currentDataSub='" + currentDataSub + '\'' +
                    ", requestedDataSub=" + requestedDataSub +
                    '}');
        }
        if (currentDataSub != requestedDataSub) {
            mPhoneMonitor.changeDataSub(requestedDataSub);
            setDataSubChecked(subInfoRecord);
            checkSimChangingState(currentDataSub, requestedDataSub);
        }
    }

    private void checkSimChangingState(int currentDataSubId, int changingToDataSubId) {
        if (LOGV) {
            Log.v(TAG, "checkSimChangingState{" +
                    "currentDataSubId='" + currentDataSubId + '\'' +
                    "changingToDataSubId='" + changingToDataSubId + '\'' +
                    "mIsAttached='" + mIsAttached + '\'' +
                    ", mRadioReady=" + mRadioReady +
                    '}');
        }
        if (mIsAttached && mRadioReady) {
            if (currentDataSubId != changingToDataSubId) {
                showProgress();
                enableViews(false);
            } else {
                hideProgress();
                enableViews(true);
            }
        }
    }

    private void setDataSubChecked(SubscriptionInfo subInfoRecord) {
        if (mIsAttached) {
            for (int i = 0; i < mCheckBoxes.size(); i++) {
                int key = mCheckBoxes.keyAt(i);
                if (subInfoRecord.getSimSlotIndex() == key) {
                    mCheckBoxes.get(key).setChecked(true);
                } else {
                    mCheckBoxes.get(key).setChecked(false);
                }
            }
        }
    }

    private void updateCurrentDataSub() {
        if (mIsAttached) {
            for (int i = 0; i < mSubInfoRecords.size(); i++) {
                SubscriptionInfo subInfoRecord = mSubInfoRecords.valueAt(i);
                int slot = subInfoRecord.getSimSlotIndex();
                mCheckBoxes.get(slot).setChecked(SubscriptionManager.getDefaultDataSubscriptionId()
                        == subInfoRecord.getSubscriptionId());
                if (LOGV) {
                    Log.v(TAG, "updateCurrentDataSub{" +
                            "currentDataSubId='" + SubscriptionManager
                            .getDefaultDataSubscriptionId() + '\'' +
                            "subInfoRecord.getSubscriptionId()='" +
                            subInfoRecord.getSubscriptionId() +
                            '}');
                }
            }
        }
    }

    private void enableViews(boolean enabled) {
        mDisabledForSwitch = !enabled;
        enableRows(enabled);
        setNextAllowed(enabled);
    }

    private void enableRows(boolean enabled) {
        for (int i = 0; i < mRows.size(); i++) {
            final View v =  mRows.get(mRows.keyAt(i));
            v.setEnabled(enabled);
            final SubscriptionInfo subInfoRecord = (SubscriptionInfo)v.getTag();
            if (subInfoRecord != null) {
                updateCarrierText(subInfoRecord);
            }
        }
    }

    private void updateCarrierText(SubscriptionInfo subInfoRecord) {
        if (mIsAttached) {
            String name = mPhoneMonitor.getSimOperatorName(subInfoRecord.getSubscriptionId());
            if (TextUtils.isEmpty(name)) {
                name = mPhoneMonitor.getNetworkOperatorName(subInfoRecord.getSubscriptionId());
            }
            ServiceState serviceState = mServiceStates.get(subInfoRecord.getSimSlotIndex());
            final int slot = subInfoRecord.getSimSlotIndex();
            final View v = mRows.get(slot);
            if (TextUtils.isEmpty(name)) {
                if (serviceState != null && serviceState.isEmergencyOnly()) {
                    name = getString(R.string.setup_mobile_data_emergency_only);
                } else {
                    name = getString(R.string.setup_mobile_data_no_service);
                }
                if (v != null) {
                    v.setEnabled(false);
                }
            } else {
                if (v != null && !mDisabledForSwitch) {
                    v.setEnabled(true);
                }
            }
            String formattedName =
                    getString(R.string.data_sim_name,
                            slot + 1, name);
            mNameViews.get(slot).setText(formattedName);
        }
    }

    private void updateSignalStrength(SubscriptionInfo subInfoRecord) {
        if (mIsAttached) {
            ImageView signalView = mSignalViews.get(subInfoRecord.getSimSlotIndex());
            SignalStrength signalStrength = mSignalStrengths.get(subInfoRecord.getSimSlotIndex());
            if (LOGV) {
                Log.v(TAG, "updateSignalStrength{" +
                        "signalStrength='" + signalStrength + '\'' +
                        "signalStrengthLevel='" + ((signalStrength != null) ?
                        signalStrength.getLevel() : "null") + '\'' +
                        ", subInfoRecord.getSimSlotIndex() =" + subInfoRecord.getSimSlotIndex()  +
                        '}');
            }
            if (!hasService(subInfoRecord)) {
                signalView.setImageResource(R.drawable.ic_signal_no_signal);
            } else {
                if (signalStrength != null) {
                    int resId;
                    switch (signalStrength.getLevel()) {
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
                    signalView.setImageResource(resId);
                }
            }
            updateCarrierText(subInfoRecord);
        }
    }

    private boolean hasService(SubscriptionInfo subInfoRecord) {
        boolean retVal;
        ServiceState serviceState = mServiceStates.get(subInfoRecord.getSimSlotIndex());
        if (serviceState == null) {
            serviceState  = mPhoneMonitor
                    .getServiceStateForSubscriber(subInfoRecord.getSubscriptionId());
            mServiceStates.put(subInfoRecord.getSimSlotIndex(), serviceState);
        }
        if (serviceState != null) {
            if (LOGV) {
                Log.v(TAG, "hasService{" +
                        "serviceState.getVoiceRegState()='" + serviceState.getVoiceRegState() + '\'' +
                        "serviceState.getVoiceRegState()='" + serviceState.getVoiceRegState() + '\'' +
                        ", subInfoRecord.getSimSlotIndex() =" + subInfoRecord.getSimSlotIndex()  +
                        '}');
            }
            // Consider the device to be in service if either voice or data service is available.
            // Some SIM cards are marketed as data-only and do not support voice service, and on
            // these SIM cards, we want to show signal bars for data service as well as the "no
            // service" or "emergency calls only" text that indicates that voice is not available.
            switch(serviceState.getVoiceRegState()) {
                case ServiceState.STATE_POWER_OFF:
                    retVal = false;
                    break;
                case ServiceState.STATE_OUT_OF_SERVICE:
                case ServiceState.STATE_EMERGENCY_ONLY:
                    retVal = serviceState.getDataRegState() == ServiceState.STATE_IN_SERVICE;
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
        return R.layout.choose_data_sim_page;
    }

    @Override
    protected int getTitleResId() {
        return R.string.setup_choose_data_sim;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_sim;
    }
}
