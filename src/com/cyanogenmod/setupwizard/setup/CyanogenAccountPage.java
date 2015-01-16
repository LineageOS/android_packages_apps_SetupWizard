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

import com.cyanogenmod.setupwizard.SetupWizardApp;
import com.cyanogenmod.setupwizard.R;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class CyanogenAccountPage extends SetupPage {

    public static final String TAG = "CyanogenAccountPage";

    public CyanogenAccountPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
    }

    @Override
    public int getNextButtonTitleResId() {
        return R.string.skip;
    }

    @Override
    public String getKey() {
        return TAG;
    }

    @Override
    public int getTitleResId() {
        return -1;
    }

    @Override
    public void doLoadAction(Activity context, int action) {
           launchCyanogenAccountSetup(context, action);
    }

    public void launchCyanogenAccountSetup(final Activity activity, final int action) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SetupWizardApp.EXTRA_FIRST_RUN, true);
        AccountManager
                .get(activity).addAccount(SetupWizardApp.ACCOUNT_TYPE_CYANOGEN, null, null, bundle,
                activity, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> bundleAccountManagerFuture) {
                if (activity == null) return; //There is a chance this activity has been torn down.
                if (accountExists(activity, SetupWizardApp.ACCOUNT_TYPE_CYANOGEN)) {
                    setCompleted(true);
                    getCallbacks().onNextPage();
                } else {
                    if (action == Page.ACTION_NEXT) {
                        getCallbacks().onNextPage();
                    } else {
                        getCallbacks().onPreviousPage();
                    }
                }
            }
        }, null);
    }

    private boolean accountExists(Activity activity, String accountType) {
        return AccountManager.get(activity).getAccountsByType(accountType).length > 0;
    }
}
