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

import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

import android.content.Context;
import android.telephony.SubscriptionManager;

import java.util.ArrayList;

public class CMSetupWizardData extends AbstractSetupData {

    public CMSetupWizardData(Context context) {
        super(context);
    }

    @Override
    protected PageList onNewPageList() {
        ArrayList<SetupPage> pages = new ArrayList<SetupPage>();
        pages.add(new WelcomePage(mContext, this));
        pages.add(new WifiSetupPage(mContext, this));
        if (SetupWizardUtils.isGSMPhone(mContext) && SetupWizardUtils.isSimMissing(mContext)) {
            pages.add(new SimCardMissingPage(mContext, this));
        }
        if (SetupWizardUtils.isMultiSimDevice(mContext)
                && SubscriptionManager.getActiveSubInfoCount() > 1) {
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


}