/*
 * Copyright (C) 2014 The CyanogenMod Project
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

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.SetupWizardApp;
import com.cyanogenmod.setupwizard.cmstats.SetupStats;
import com.cyanogenmod.setupwizard.ui.SetupPageFragment;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

import java.util.List;

public class ChooseDataSimPage extends SetupPage {

    public static final String TAG = "ChooseDataSimPage";

    public ChooseDataSimPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
    }

    @Override
    public Fragment getFragment(FragmentManager fragmentManager, int action) {
        Fragment fragment = fragmentManager.findFragmentByTag(getKey());
        if (fragment == null) {
            Bundle args = new Bundle();
            args.putString(Page.KEY_PAGE_ARGUMENT, getKey());
            args.putInt(Page.KEY_PAGE_ACTION, action);
            fragment = new ChooseDataSimFragment();
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public String getKey() {
        return TAG;
    }

    @Override
    public int getTitleResId() {
        return R.string.setup_choose_data_sim;
    }


    public static class ChooseDataSimFragment extends SetupPageFragment {

        private ViewGroup mPageView;
        private ProgressBar mProgressBar;
        private SparseArray<TextView> mNameViews;
        private SparseArray<ImageView> mSignalViews;
        private SparseArray<CheckBox> mCheckBoxes;

        private TelephonyManager mPhone;
        private SparseArray<SubscriptionInfo> mSubInfoRecords;
        private SparseArray<SignalStrength> mSignalStrengths;
        private SparseArray<ServiceState> mServiceStates;
        private SparseArray<PhoneStateListener> mPhoneStateListeners;

        private boolean mIsAttached = false;

        private Context mContext;
        private SubscriptionManager mSubscriptionManager;

        private final Handler mHandler = new Handler();

        private final Runnable mRadioReadyRunnable = new Runnable() {
            @Override
            public void run() {
                hideWaitForRadio();
            }
        };

        private View.OnClickListener mSetDataSimClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubscriptionInfo subInfoRecord = (SubscriptionInfo)view.getTag();
                if (subInfoRecord != null) {
                    mSubscriptionManager.setDefaultDataSubId(subInfoRecord.getSubscriptionId());
                    setDataSubChecked(subInfoRecord);
                }
            }
        };

        @Override
        protected void initializePage() {
            mPageView = (ViewGroup)mRootView.findViewById(R.id.page_view);
            mProgressBar = (ProgressBar) mRootView.findViewById(R.id.progress);
            List<SubscriptionInfo> subInfoRecords = mSubscriptionManager.getActiveSubscriptionInfoList();
            int simCount =
                    subInfoRecords != null ? subInfoRecords.size() : 0;
            mSubInfoRecords = new SparseArray<SubscriptionInfo>(simCount);
            for (SubscriptionInfo subInfoRecord : subInfoRecords) {
                mSubInfoRecords.put(subInfoRecord.getSimSlotIndex(), subInfoRecord);
            }
            mNameViews = new SparseArray<TextView>(simCount);
            mSignalViews = new SparseArray<ImageView>(simCount);
            mCheckBoxes = new SparseArray<CheckBox>(simCount);
            mServiceStates = new SparseArray<ServiceState>(simCount);
            mSignalStrengths = new SparseArray<SignalStrength>(simCount);
            mPhoneStateListeners = new SparseArray<PhoneStateListener>(simCount);
            LayoutInflater inflater = LayoutInflater.from(getActivity());
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
                mPhoneStateListeners.put(slot, createPhoneStateListener(subInfoRecord));
                mPageView.addView(inflater.inflate(R.layout.divider, null));
            }
            updateSignalStrengths();
            updateCurrentDataSub();
        }

        @Override
        protected int getLayoutResource() {
            return R.layout.choose_data_sim_page;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mContext = getActivity().getApplicationContext();
            mSubscriptionManager = SubscriptionManager.from(mContext);
        }

        @Override
        public void onResume() {
            super.onResume();
            mIsAttached = true;
            mPhone = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            for (int i = 0; i < mPhoneStateListeners.size(); i++) {
                mPhone.listen(mPhoneStateListeners.valueAt(i),
                        PhoneStateListener.LISTEN_SERVICE_STATE
                                | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            }
            updateSignalStrengths();
            updateCurrentDataSub();
            if (SetupWizardUtils.isRadioReady(mContext, null)) {
                hideWaitForRadio();
            } else if (mTitleView != null) {
                mTitleView.setText(R.string.loading);
                mHandler.postDelayed(mRadioReadyRunnable, SetupWizardApp.RADIO_READY_TIMEOUT);
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            mIsAttached = false;
            for (int i = 0; i < mPhoneStateListeners.size(); i++) {
                mPhone.listen(mPhoneStateListeners.valueAt(i), PhoneStateListener.LISTEN_NONE);
            }
        }

        private PhoneStateListener createPhoneStateListener(final SubscriptionInfo subInfoRecord) {
            return new PhoneStateListener(subInfoRecord.getSubscriptionId()) {

                @Override
                public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                    mSignalStrengths.put(subInfoRecord.getSimSlotIndex(), signalStrength);
                    updateSignalStrength(subInfoRecord);
                }

                @Override
                public void onServiceStateChanged(ServiceState state) {
                    if (SetupWizardUtils.isRadioReady(mContext, state)) {
                        hideWaitForRadio();
                    }
                    mServiceStates.put(subInfoRecord.getSimSlotIndex(), state);
                    updateSignalStrength(subInfoRecord);
                }
            };
        }

        private void hideWaitForRadio() {
            if (getUserVisibleHint() && mProgressBar.isShown()) {
                mHandler.removeCallbacks(mRadioReadyRunnable);
                if (mTitleView != null) {
                    mTitleView.setText(mPage.getTitleResId());
                }
                mProgressBar.setVisibility(View.GONE);
                mPageView.setVisibility(View.VISIBLE);
                mPageView.startAnimation(
                        AnimationUtils.loadAnimation(getActivity(), R.anim.translucent_enter));
            }
        }

        private void updateSignalStrengths() {
            if (mIsAttached) {
                for (int i = 0; i < mSubInfoRecords.size(); i++) {
                    updateSignalStrength(mSubInfoRecords.valueAt(i));
                }
            }
        }

        private void setDataSubChecked(SubscriptionInfo subInfoRecord) {
            if (mIsAttached) {
                for (int i = 0; i < mCheckBoxes.size(); i++) {
                    if (subInfoRecord.getSimSlotIndex() == i) {
                        mCheckBoxes.get(i).setChecked(true);
                        SetupStats.addEvent(SetupStats.Categories.SETTING_CHANGED,
                                SetupStats.Action.PREFERRED_DATA_SIM,
                                SetupStats.Label.SLOT, String.valueOf(i + 1));
                    } else {
                        mCheckBoxes.get(i).setChecked(false);
                    }

                }
            }
        }

        private void updateCurrentDataSub() {
            if (mIsAttached) {
                for (int i = 0; i < mSubInfoRecords.size(); i++) {
                    SubscriptionInfo subInfoRecord = mSubInfoRecords.valueAt(i);
                    mCheckBoxes.get(i).setChecked(mSubscriptionManager.getDefaultDataPhoneId()
                            == subInfoRecord.getSimSlotIndex());
                }
            }
        }

        private void updateCarrierText(SubscriptionInfo subInfoRecord) {
            if (mIsAttached) {
                String name = mPhone.getNetworkOperatorName(subInfoRecord.getSimSlotIndex());
                ServiceState serviceState = mServiceStates.get(subInfoRecord.getSimSlotIndex());
                if (TextUtils.isEmpty(name)) {
                    if (serviceState != null && serviceState.isEmergencyOnly()) {
                        name = getString(R.string.setup_mobile_data_emergency_only);
                    } else {
                        name = getString(R.string.setup_mobile_data_no_service);
                    }
                }
                String formattedName =
                        getString(R.string.data_sim_name,
                                  subInfoRecord.getSimSlotIndex() + 1, name);
                mNameViews.get(subInfoRecord.getSimSlotIndex()).setText(formattedName);
            }
        }

        private void updateSignalStrength(SubscriptionInfo subInfoRecord) {
            if (mIsAttached) {
                ImageView signalView = mSignalViews.get(subInfoRecord.getSimSlotIndex());
                SignalStrength signalStrength = mSignalStrengths.get(subInfoRecord.getSimSlotIndex());
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
            if (serviceState != null) {
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
    }

}
