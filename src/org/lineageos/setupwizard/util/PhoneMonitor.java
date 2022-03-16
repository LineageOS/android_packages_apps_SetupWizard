/*
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

package org.lineageos.setupwizard.util;

import static android.telephony.PhoneStateListener.LISTEN_DATA_CONNECTION_STATE;
import static android.telephony.PhoneStateListener.LISTEN_NONE;
import static android.telephony.PhoneStateListener.LISTEN_SERVICE_STATE;
import static android.telephony.PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;
import static android.telephony.ServiceState.STATE_EMERGENCY_ONLY;
import static android.telephony.ServiceState.STATE_IN_SERVICE;
import static android.telephony.ServiceState.STATE_OUT_OF_SERVICE;
import static android.telephony.ServiceState.STATE_POWER_OFF;
import static android.telephony.TelephonyManager.DATA_CONNECTED;
import static android.telephony.TelephonyManager.DATA_CONNECTING;
import static android.telephony.TelephonyManager.DATA_DISCONNECTED;
import static android.telephony.TelephonyManager.DATA_SUSPENDED;
import static android.telephony.TelephonyManager.DATA_UNKNOWN;
import static android.telephony.TelephonyManager.PHONE_TYPE_CDMA;
import static android.telephony.TelephonyManager.PHONE_TYPE_GSM;
import static android.telephony.TelephonyManager.PHONE_TYPE_NONE;
import static android.telephony.TelephonyManager.PHONE_TYPE_SIP;
import static android.telephony.TelephonyManager.SIM_STATE_ABSENT;
import static android.telephony.TelephonyManager.SIM_STATE_CARD_IO_ERROR;
import static android.telephony.TelephonyManager.SIM_STATE_NETWORK_LOCKED;
import static android.telephony.TelephonyManager.SIM_STATE_PIN_REQUIRED;
import static android.telephony.TelephonyManager.SIM_STATE_PUK_REQUIRED;
import static android.telephony.TelephonyManager.SIM_STATE_READY;
import static android.telephony.TelephonyManager.SIM_STATE_UNKNOWN;

import static com.android.internal.telephony.PhoneConstants.LTE_ON_CDMA_TRUE;
import static com.android.internal.telephony.PhoneConstants.LTE_ON_CDMA_UNKNOWN;

import static org.lineageos.setupwizard.SetupWizardApp.LOGV;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.Looper;
import android.sysprop.TelephonyProperties;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;

import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class PhoneMonitor {

    public static final String TAG = PhoneMonitor.class.getSimpleName();

    private static PhoneMonitor sInstance;
    private final Context mContext;
    private final TelephonyManager mTelephony;
    private SubscriptionManager mSubscriptionManager;
    private final ArrayList<SubscriptionStateListener> mListeners = new ArrayList<>();
    private final SparseArray<SubscriptionStateTracker> mTrackers = new SparseArray<>();

    private int mChangingToDataSubId = -1;

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
                final int sub = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, -1);
                final int state = mTelephony.getSimState(sub);
                simStateChanged(sub, state);
            } else if (intent.getAction()
                    .equals(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED)) {
                ddsHasChanged(intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, -1));
            }
        }
    };

    private class SubscriptionStateTracker extends PhoneStateListener {

        private ServiceState mServiceState;
        private int mSubId = -1;

        public SubscriptionStateTracker(int subId) {
            super(new HandlerExecutor(new Handler(Looper.myLooper())));
            mSubId = subId;
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            mServiceState = serviceState;
            if (LOGV) {
                logPhoneState("onServiceStateChanged state=\"" + serviceState + "\" ");
            }
            for (SubscriptionStateListener listener : mListeners) {
                listener.onServiceStateChanged(mSubId, serviceState);
            }
        }

        public void onDataConnectionStateChanged(int state, int networkType) {
            for (SubscriptionStateListener listener : mListeners) {
                listener.onDataConnectionStateChanged(mSubId, state, networkType);
            }
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            for (SubscriptionStateListener listener : mListeners) {
                listener.onSignalStrengthsChanged(mSubId, signalStrength);
            }
        }
    }

    private final OnSubscriptionsChangedListener mOnSubscriptionsChangedListener =
            new OnSubscriptionsChangedListener() {
                public void onSubscriptionsChanged() {
                    if (LOGV) {
                        Log.d(TAG, "Subscriptions changed");
                    }
                    super.onSubscriptionsChanged();
                    updatePhoneStateTrackers();
                }
            };

    public static void initInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PhoneMonitor(context.getApplicationContext());
        }
    }

    public static void onSetupFinished() {
        if (sInstance != null) {
            try {
                sInstance.mContext.unregisterReceiver(sInstance.mIntentReceiver);
            } catch (Exception e) {}
        }
    }

    public static PhoneMonitor getInstance() {
        return sInstance;
    }

    public PhoneMonitor(Context context) {
        mContext = context;
        if (LOGV) {
            Log.v(TAG, "Starting PhoneMonitor");
        }
        mTelephony = mContext.getSystemService(TelephonyManager.class);
        if (mTelephony != null) {
            mSubscriptionManager = SubscriptionManager.from(mContext);
            mSubscriptionManager.addOnSubscriptionsChangedListener(mOnSubscriptionsChangedListener);
            updatePhoneStateTrackers();
        }
        // Register for DDS changes
        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
        filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        context.registerReceiver(mIntentReceiver, filter, null, null);
    }

    private void updatePhoneStateTrackers() {
        int i = 0;
        int[] subIds = mSubscriptionManager.getActiveSubscriptionIdList();
        HashSet<Integer> subIdSet = new HashSet(Arrays.asList(subIds));
        if (LOGV) {
            Log.v(TAG, "Register PhoneStateListeners for " + subIdSet);
        }
        for (int i2 = 0; i2 < mTrackers.size(); i2++) {
            if (!subIdSet.contains(Integer.valueOf(mTrackers.keyAt(i2)))) {
                mTelephony.listen(mTrackers.valueAt(i2), LISTEN_NONE);
                mTrackers.removeAt(i2);
            }
        }
        int length = subIds.length;
        while (i < length) {
            int subId = subIds[i];
            if (mTrackers.indexOfKey(subId) < 0) {
                SubscriptionStateTracker tracker = new SubscriptionStateTracker(subId);
                mTrackers.put(subId, tracker);
                mTelephony.createForSubscriptionId(subId).listen(tracker, LISTEN_SERVICE_STATE
                        | LISTEN_SIGNAL_STRENGTHS
                        | LISTEN_DATA_CONNECTION_STATE);
            }
            i++;
        }
    }

    public void addListener(SubscriptionStateListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(SubscriptionStateListener listener) {
        mListeners.remove(listener);
    }

    public SubscriptionInfo getActiveSubscriptionInfo(int subId) {
        return mSubscriptionManager.getActiveSubscriptionInfo(subId);
    }

    public List<SubscriptionInfo> getActiveSubscriptionInfoList() {
        return mSubscriptionManager.getActiveSubscriptionInfoList();
    }

    public String getSimOperatorName(int subId) {
        return mTelephony.createForSubscriptionId(subId).getSimOperatorName();
    }

    public String getNetworkOperatorName(int subId) {
        return mTelephony.createForSubscriptionId(subId).getNetworkOperatorName();
    }

    public ServiceState getServiceStateForSubscriber(int subId) {
        return mTelephony.getServiceStateForSubscriber(subId);
    }

    public void changeDataSub(int subId) {
        if (LOGV) {
            Log.v(TAG, "changeDataSub{" +
                    "subId='" + subId + '\'' +
                    ", mChangingToDataSubId=" + mChangingToDataSubId +
                    '}');
        }
        if (mChangingToDataSubId != subId) {
            mSubscriptionManager.setDefaultDataSubId(subId);
            for (SubscriptionStateListener subscriptionStateListener : mListeners) {
                subscriptionStateListener
                        .onDefaultDataSubscriptionChangeRequested(mChangingToDataSubId, subId);
            }
            mChangingToDataSubId = subId;
        }
    }

    private void ddsHasChanged(int subId) {
        if (subId > -1) {
            for (SubscriptionStateListener subscriptionStateListener : mListeners) {
                subscriptionStateListener.onDefaultDataSubscriptionChanged(subId);
            }
        }
    }

    private void simStateChanged(int subId, int simState) {
        if (LOGV) {
            Log.v(TAG,
                    "simStateChanged(" + subId + ", " + simState + ")");
        }
        for (SubscriptionStateListener subscriptionStateListener : mListeners) {
            subscriptionStateListener.onSimStateChanged(subId, simState);
        }
    }

    public boolean simMissing() {
        if (mTelephony == null) {
            return false;
        }
        List<SubscriptionInfo> subs = mSubscriptionManager.getActiveSubscriptionInfoList();
        if (subs != null) {
            for (SubscriptionInfo sub : subs) {
                int simState = mTelephony.getSimState(sub.getSimSlotIndex());
                if (LOGV) {
                    Log.v(TAG, "getSimState(" + sub.getSubscriptionId() + ") == " + simState);
                }
                int subId = sub.getSubscriptionId();
                boolean isGsm = isGSM(subId);
                boolean isLte = isLte(subId);
                if ((isGsm || isLte) && simState != 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean singleSimInserted() {
        return mSubscriptionManager.getActiveSubscriptionInfoCount() == 1;
    }

    // We only care that each slot has a sim
    public boolean allSimsInserted() {
        int simSlotCount = mTelephony.getSimCount();
        for (int i = 0; i < simSlotCount; i++) {
            int state = mTelephony.getSimState(i);
            if (state == TelephonyManager.SIM_STATE_ABSENT) {
                return false;
            }
        }
        return simSlotCount == mSubscriptionManager.getActiveSubscriptionInfoCount();
    }

    public boolean isMultiSimDevice() {
        return mTelephony.isMultiSimEnabled();
    }

    public boolean isGSM(int subId) {
        return mTelephony.createForSubscriptionId(subId).getCurrentPhoneType() == PHONE_TYPE_GSM;
    }

    public boolean isLte(int subId) {
        return getLteOnCdmaMode(subId) == LTE_ON_CDMA_TRUE;
    }

    public int getLteOnCdmaMode(int subId) {
        if (mTelephony == null || mTelephony.createForSubscriptionId(subId).getLteOnCdmaMode(subId)
                == LTE_ON_CDMA_UNKNOWN) {
            return TelephonyProperties.lte_on_cdma_device().orElse(LTE_ON_CDMA_UNKNOWN);
        }
        return mTelephony.createForSubscriptionId(subId).getLteOnCdmaMode(subId);
    }

    private void logPhoneState(String prefix) {
        if (LOGV) {
            Log.v(TAG, prefix + getPhoneState());
        }
    }

    private String getPhoneTypeName() {
        return getPhoneTypeName(mTelephony != null ? mTelephony.getCurrentPhoneType() : -1);
    }

    private int getMcc() {
        return mContext.getResources().getConfiguration().mcc;
    }

    private int getMnc() {
        return mContext.getResources().getConfiguration().mnc;
    }

    private String getPhoneTypeName(int phoneType) {
        StringBuilder sb = new StringBuilder();
        switch (phoneType) {
            case PHONE_TYPE_NONE:
                sb.append("PHONE_TYPE_NONE");
                break;
            case PHONE_TYPE_GSM:
                sb.append("PHONE_TYPE_GSM");
                break;
            case PHONE_TYPE_CDMA:
                sb.append("PHONE_TYPE_CDMA");
                break;
            case PHONE_TYPE_SIP:
                sb.append("PHONE_TYPE_SIP");
                break;
        }
        sb.append("(").append(phoneType).append(")");
        return sb.toString();
    }

    private String getNetworkTypeName() {
        return mTelephony != null ? mTelephony.getNetworkTypeName() : "";
    }

    private String getSubscriptionSimStateName(int subId) {
        SubscriptionInfo subInfo = mSubscriptionManager.getActiveSubscriptionInfo(subId);
        if (subInfo == null) {
            return "SIM_STATE_UNKNOWN";
        }
        return mTelephony != null ?
                getSimStateName(mTelephony.getSimState(subInfo.getSimSlotIndex())) : "";
    }

    private String getDataStateName() {
        return mTelephony != null ? getDataStateName(mTelephony.getDataState()) : "";
    }

    private String getDataStateName(int dataState) {
        StringBuilder sb = new StringBuilder();
        switch (dataState) {
            case DATA_UNKNOWN:
                sb.append("DATA_UNKNOWN");
                break;
            case DATA_DISCONNECTED:
                sb.append("DATA_DISCONNECTED");
                break;
            case DATA_CONNECTING:
                sb.append("DATA_CONNECTING");
                break;
            case DATA_CONNECTED:
                sb.append("DATA_CONNECTED");
                break;
            case DATA_SUSPENDED:
                sb.append("DATA_SUSPENDED");
                break;
        }
        sb.append("(").append(dataState).append(")");
        return sb.toString();
    }

    private String getSimStateName(int simState) {
        StringBuilder sb = new StringBuilder();
        switch (simState) {
            case SIM_STATE_UNKNOWN:
                sb.append("SIM_STATE_UNKNOWN");
                break;
            case SIM_STATE_ABSENT:
                sb.append("SIM_STATE_ABSENT");
                break;
            case SIM_STATE_PIN_REQUIRED:
                sb.append("SIM_STATE_PIN_REQUIRED");
                break;
            case SIM_STATE_PUK_REQUIRED:
                sb.append("SIM_STATE_PUK_REQUIRED");
                break;
            case SIM_STATE_NETWORK_LOCKED:
                sb.append("SIM_STATE_NETWORK_LOCKED");
                break;
            case SIM_STATE_READY:
                sb.append("SIM_STATE_READY");
                break;
            case SIM_STATE_CARD_IO_ERROR:
                sb.append("SIM_STATE_CARD_IO_ERROR");
                break;
        }
        sb.append("(").append(simState).append(")");
        return sb.toString();
    }

    private String getVoiceServiceStateName(int subId) {
        return getServiceStateName(getVoiceRegState(subId));
    }

    private String getDataServiceStateName(int subId) {
        return getServiceStateName(getDataRegState(subId));
    }

    private int getVoiceRegState(int subId) {
        SubscriptionStateTracker tracker = mTrackers.get(subId);
        ServiceState serviceState = tracker != null ? tracker.mServiceState : null;
        return serviceState != null ? serviceState.getVoiceRegState() : -1;
    }

    private int getDataRegState(int subId) {
        SubscriptionStateTracker tracker = mTrackers.get(subId);
        ServiceState serviceState = tracker != null ? tracker.mServiceState : null;
        return serviceState != null ? serviceState.getDataRegState() : -1;
    }

    private String getServiceStateName(int serviceState) {
        StringBuilder sb = new StringBuilder();
        switch (serviceState) {
            case STATE_IN_SERVICE:
                sb.append("STATE_IN_SERVICE");
                break;
            case STATE_OUT_OF_SERVICE:
                sb.append("STATE_OUT_OF_SERVICE");
                break;
            case STATE_EMERGENCY_ONLY:
                sb.append("STATE_EMERGENCY_ONLY");
                break;
            case STATE_POWER_OFF:
                sb.append("STATE_POWER_OFF");
                break;
        }
        sb.append("(").append(serviceState).append(")");
        return sb.toString();
    }

    private String getPhoneState() {
        StringBuilder states = new StringBuilder();
        for (int subId : mSubscriptionManager.getActiveSubscriptionIdList()) {
            states.append(" ").append(getPhoneState(subId));
        }
        return getPhoneTypeName() + " \"" + getNetworkTypeName() + "\"" + " mcc" + getMcc() +
                "mnc" + getMnc() + " " + getDataStateName() + " " + states.toString();
    }

    private String getPhoneState(int subId) {
        return "{ " + getSubscriptionSimStateName(subId) + " Voice:"
                + getVoiceServiceStateName(subId)
                + " Data:" + getDataServiceStateName(subId) + "  }";
    }

    public interface SubscriptionStateListener {
        void onServiceStateChanged(int subId, ServiceState serviceState);

        void onDataConnectionStateChanged(int subId, int state, int networkType);

        void onDefaultDataSubscriptionChanged(int subId);

        void onDefaultDataSubscriptionChangeRequested(int currentSubId, int newSubId);

        void onSignalStrengthsChanged(int subId, SignalStrength signalStrength);

        void onSimStateChanged(int subId, int simState);
    }

}
