/*
 * Copyright (C) 2013 The CyanogenMod Project
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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.TelephonyIntents;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

import java.util.ArrayList;

public class CMSetupWizardData extends AbstractSetupData {

    private static final String TAG = CMSetupWizardData.class.getSimpleName();

    private final TelephonyManager mTelephonyManager;

    private boolean mTimeSet = false;
    private boolean mTimeZoneSet = false;

    private final int mSimSlotCount;
    private final int[] mSimStates;

    public CMSetupWizardData(Context context) {
        super(context);
        mTelephonyManager = TelephonyManager.from(context);
        mSimSlotCount = mTelephonyManager.getPhoneCount();
        mSimStates = new int[mSimSlotCount];
        for (int i = 0; i < mSimSlotCount; i++) {
            mSimStates[i] = TelephonyManager.SIM_STATE_ABSENT;
        }
    }

    @Override
    protected PageList onNewPageList() {
        ArrayList<Page> pages = new ArrayList<Page>();
        pages.add(new WelcomePage(mContext, this));
        pages.add(new WifiSetupPage(mContext, this));
        if (SetupWizardUtils.hasTelephony(mContext)) {
            pages.add(new SimCardMissingPage(mContext, this).setHidden(true));
        }
        if (SetupWizardUtils.isMultiSimDevice(mContext)) {
            pages.add(new ChooseDataSimPage(mContext, this));
        }
        if (SetupWizardUtils.hasTelephony(mContext) &&
                !SetupWizardUtils.isMobileDataEnabled(mContext)) {
            pages.add(new MobileDataPage(mContext, this));
        }
        if (SetupWizardUtils.hasGMS(mContext)) {
            pages.add(new GmsAccountPage(mContext, this));
        }
        pages.add(new CyanogenServicesPage(mContext, this));
        pages.add(new CyanogenSettingsPage(mContext, this));
        pages.add(new LocationSettingsPage(mContext, this));
        pages.add(new DateTimePage(mContext, this));
        pages.add(new FinishPage(mContext, this));
        return new PageList(pages.toArray(new SetupPage[pages.size()]));
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
           int slot = intent.getIntExtra("slot", -1);
           if (slot != -1 && mSimStates.length > 0) {
               mSimStates[slot] = mTelephonyManager.getSimState(slot);
           }
        } else if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
            mTimeZoneSet = true;
        } else if (intent.getAction().equals(Intent.ACTION_TIME_CHANGED)) {
            mTimeSet = true;
        }
        DateTimePage dateTimePage = (DateTimePage) getPage(DateTimePage.TAG);
        dateTimePage.setHidden(mTimeZoneSet & mTimeSet);

        SimCardMissingPage simCardMissingPage =
                (SimCardMissingPage) getPage(SimCardMissingPage.TAG);
        if (simCardMissingPage != null) {
            simCardMissingPage.setHidden(isSimInserted());
        }

        ChooseDataSimPage chooseDataSimPage =
                (ChooseDataSimPage) getPage(ChooseDataSimPage.TAG);
        if (chooseDataSimPage != null) {
            chooseDataSimPage.setHidden(!allSimsInserted());
        }
    }

    public IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        return filter;
    }

    // We only care that one sim is inserted
    private boolean isSimInserted() {
        for (int state : mSimStates) {
            if (state != TelephonyManager.SIM_STATE_ABSENT
                    && state != TelephonyManager.SIM_STATE_UNKNOWN) {
                 return true;
            }
        }
        return false;
    }

    // We only care the each slot has a sim
    private boolean allSimsInserted() {
        for (int state : mSimStates) {
            if (state == TelephonyManager.SIM_STATE_ABSENT) {
                return false;
            }
        }
        return true;
    }

}