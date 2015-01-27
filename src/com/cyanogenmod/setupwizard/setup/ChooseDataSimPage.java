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
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubInfoRecord;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.ui.SetupPageFragment;

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

    @Override
    public int getNextButtonTitleResId() {
        return R.string.skip;
    }


    public static class ChooseDataSimFragment extends SetupPageFragment {

        private ViewGroup mPageView;
        private SparseArray<TextView> mNameViews;
        private SparseArray<ImageView> mSignalViews;
        private SparseArray<CheckBox> mCheckBoxes;

        private TelephonyManager mPhone;
        private List<SubInfoRecord> mSubInfoRecords;
        private SparseArray<SignalStrength> mSignalStrengths;
        private SparseArray<ServiceState> mServiceStates;
        private SparseArray<PhoneStateListener> mPhoneStateListeners;

        private View.OnClickListener mSetDataSimClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubInfoRecord subInfoRecord = (SubInfoRecord)view.getTag();
                if (subInfoRecord != null) {
                    SubscriptionManager.setDefaultDataSubId(subInfoRecord.subId);
                    setDataSubChecked(subInfoRecord);
                }
            }
        };

        @Override
        protected void initializePage() {
            mPageView = (ViewGroup)mRootView.findViewById(R.id.page_view);
            mSubInfoRecords =  SubscriptionManager.getActiveSubInfoList();
            int simCount = mSubInfoRecords.size();
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
                SubInfoRecord subInfoRecord = mSubInfoRecords.get(i);
                simRow.setTag(subInfoRecord);
                simRow.setOnClickListener(mSetDataSimClickListener);
                mNameViews.put(i, (TextView) simRow.findViewById(R.id.sim_title));
                mSignalViews.put(i, (ImageView) simRow.findViewById(R.id.signal));
                mCheckBoxes.put(i, (CheckBox) simRow.findViewById(R.id.enable_check));
                mPhoneStateListeners.put(i, createPhoneStateListener(subInfoRecord));
                mPageView.addView(inflater.inflate(R.layout.divider, null));
            }
            mPhone = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            for (int i = 0; i < mPhoneStateListeners.size(); i++) {
                mPhone.listen(mPhoneStateListeners.get(i),
                        PhoneStateListener.LISTEN_SERVICE_STATE
                                | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            }
            updateSignalStrengths();
            updateCurrentDataSub();
        }

        @Override
        protected int getLayoutResource() {
            return R.layout.choose_data_sim_page;
        }

        @Override
        public void onResume() {
            super.onResume();
            updateSignalStrengths();
            updateCurrentDataSub();
        }

        @Override
        public void onDetach() {
            for (int i = 0; i < mPhoneStateListeners.size(); i++) {
                mPhone.listen(mPhoneStateListeners.get(i), PhoneStateListener.LISTEN_NONE);
            }
            super.onDetach();
        }

        private PhoneStateListener createPhoneStateListener(final SubInfoRecord subInfoRecord) {
            return new PhoneStateListener(subInfoRecord.subId) {

                @Override
                public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                    if (isDetached()) return;
                    mSignalStrengths.put(subInfoRecord.slotId, signalStrength);
                    updateSignalStrength(subInfoRecord);
                }

                @Override
                public void onServiceStateChanged(ServiceState state) {
                    if (isDetached()) return;
                    mServiceStates.put(subInfoRecord.slotId, state);
                    updateSignalStrength(subInfoRecord);
                }
            };
        }

        private void updateSignalStrengths() {
            if (isDetached()) return;
            for (int i = 0; i < mSubInfoRecords.size(); i++) {
                updateSignalStrength(mSubInfoRecords.get(i));
            }
        }

        private void setDataSubChecked(SubInfoRecord subInfoRecord) {
            if (isDetached()) return;
            for (int i = 0; i < mCheckBoxes.size(); i++) {
                mCheckBoxes.get(i).setChecked(subInfoRecord.slotId == i);

            }
        }

        private void updateCurrentDataSub() {
            if (isDetached()) return;
            for (int i = 0; i < mSubInfoRecords.size(); i++) {
                SubInfoRecord subInfoRecord = mSubInfoRecords.get(i);
                mCheckBoxes.get(i).setChecked(SubscriptionManager.getDefaultDataSubId()
                        == subInfoRecord.subId);

            }
        }

        private void updateCarrierText(SubInfoRecord subInfoRecord) {
            if (isDetached()) return;
            String name = mPhone.getNetworkOperatorName(subInfoRecord.subId);
            ServiceState serviceState = mServiceStates.get(subInfoRecord.slotId);
            if (TextUtils.isEmpty(name)) {
                if (serviceState != null && serviceState.isEmergencyOnly()) {
                    name = getString(R.string.setup_mobile_data_emergency_only);
                } else {
                    name = getString(R.string.setup_mobile_data_no_service);
                }
            }
            String formattedName =
                    getString(R.string.data_sim_name, subInfoRecord.slotId + 1, name);
            mNameViews.get(subInfoRecord.slotId).setText(formattedName);
        }

        private void updateSignalStrength(SubInfoRecord subInfoRecord) {
            if (isDetached()) return;
            ImageView signalView = mSignalViews.get(subInfoRecord.slotId);
            SignalStrength signalStrength = mSignalStrengths.get(subInfoRecord.slotId);
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

        private boolean hasService(SubInfoRecord subInfoRecord) {
            boolean retVal;
            ServiceState serviceState = mServiceStates.get(subInfoRecord.slotId);
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
