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
import android.content.Intent;
import android.os.Bundle;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.SetupWizardApp;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

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
        if (action == Page.ACTION_PREVIOUS) {
            getCallbacks().onPreviousPage();
        } else {
            launchGmsAccountSetup(context);
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SetupWizardApp.REQUEST_CODE_SETUP_GMS) {
            if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_FIRST_USER) {
                if (SetupWizardUtils.accountExists(mContext, SetupWizardApp.ACCOUNT_TYPE_GMS)) {
                    setCompleted(true);
                }
                getCallbacks().onNextPage();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                getCallbacks().onPreviousPage();
            }
        }
        return true;
    }

    public void launchGmsAccountSetup(final Activity activity) {
        /*
         * XXX: The AccountIntro intent is now public and therefore likely to change.
         * The only way to catch whether the user pressed skip of back if via startActivityForResult.
         * If this fails, fall back to the old method, but it is not ideal because only a
         * OperationCanceledException is thrown regardless of skipping or pressing back.
         */
        try {
            Intent intent = new Intent("com.google.android.accounts.AccountIntro");
            intent.putExtra(SetupWizardApp.EXTRA_FIRST_RUN, true);
            intent.putExtra(SetupWizardApp.EXTRA_ALLOW_SKIP, true);
            intent.putExtra(SetupWizardApp.EXTRA_USE_IMMERSIVE, true);
            activity.startActivityForResult(intent, SetupWizardApp.REQUEST_CODE_SETUP_GMS);
        } catch (Exception e) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(SetupWizardApp.EXTRA_FIRST_RUN, true);
            bundle.putBoolean(SetupWizardApp.EXTRA_ALLOW_SKIP, true);
            bundle.putBoolean(SetupWizardApp.EXTRA_USE_IMMERSIVE, true);
            AccountManager
                    .get(activity).addAccount(SetupWizardApp.ACCOUNT_TYPE_GMS, null, null,
                    bundle, activity, new AccountManagerCallback<Bundle>() {
                        @Override
                        public void run(AccountManagerFuture<Bundle> bundleAccountManagerFuture) {
                            try {
                                if (bundleAccountManagerFuture.getResult()
                                        .getString(AccountManager.KEY_AUTHTOKEN) != null) {
                                    setCompleted(true);
                                }
                            } catch (OperationCanceledException e) {
                                if (activity != null && activity.isResumed()) {
                                    getCallbacks().onNextPage();
                                }
                            } catch (IOException e) {
                            } catch (AuthenticatorException e) {
                            }
                        }
                    }, null);
        }
    }
}
