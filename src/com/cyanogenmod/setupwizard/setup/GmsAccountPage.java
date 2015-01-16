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

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.SetupWizardApp;

import java.io.IOException;

public class GmsAccountPage extends SetupPage {

    public static final String TAG = "GmsAccountPage";

    public GmsAccountPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
    }

    @Override
    public String getKey() {
        return TAG;
    }

    @Override
    public int getTitleResId() {
        return R.string.setup_gms_account;
    }

    @Override
    public int getNextButtonTitleResId() {
        return R.string.skip;
    }

    @Override
    public void doLoadAction(Activity context, int action) {
        launchGmsAccountSetup(context, action);
    }

    public void launchGmsAccountSetup(final Activity activity, final int action) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SetupWizardApp.EXTRA_FIRST_RUN, true);
        bundle.putBoolean(SetupWizardApp.EXTRA_ALLOW_SKIP, true);
        AccountManager
                .get(activity).addAccount(SetupWizardApp.ACCOUNT_TYPE_GMS, null, null,
                bundle, activity, new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> bundleAccountManagerFuture) {
                        //There is a chance this activity has been torn down.
                        if (activity == null) return;
                        String token = null;
                        try {
                            token = bundleAccountManagerFuture.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                        } catch (OperationCanceledException e) {
                        } catch (IOException e) {
                        } catch (AuthenticatorException e) {
                        }
                        if (token != null) {
                            setCompleted(true);
                        }
                        if (action == Page.ACTION_NEXT) {
                            getCallbacks().onNextPage();
                        } else {
                            getCallbacks().onPreviousPage();
                        }
                    }
                }, null);
    }
}
