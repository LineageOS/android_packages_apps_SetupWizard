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
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.SetupWizardApp;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

import java.io.IOException;

public class CyanogenServicesPage extends SetupPage {

    public static final String TAG = "CyangogenServicesPage";

    public CyanogenServicesPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
    }

    @Override
    public Fragment getFragment(FragmentManager fragmentManager, int action) {
        Fragment fragment = fragmentManager.findFragmentByTag(getKey());
        if (fragment == null) {
            Bundle args = new Bundle();
            args.putString(Page.KEY_PAGE_ARGUMENT, getKey());
            args.putInt(Page.KEY_PAGE_ACTION, action);
            fragment = new LoadingFragment();
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
        return R.string.loading;
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
            if (!SetupWizardUtils.accountExists(mContext,
                    mContext.getString(R.string.cm_account_type))) {
                super.doLoadAction(context, action);
                launchCyanogenAccountSetup(context);
            } else {
                getCallbacks().onNextPage();
            }
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SetupWizardApp.REQUEST_CODE_SETUP_CYANOGEN) {
            if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_FIRST_USER) {
                if (SetupWizardUtils.accountExists(mContext, mContext.getString(R.string.cm_account_type))) {
                    setCompleted(true);
                }
                getCallbacks().onNextPage();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                getCallbacks().onPreviousPage();
            }
        }
        return true;
    }


    private void launchCyanogenAccountSetup(final Activity activity) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SetupWizardApp.EXTRA_FIRST_RUN, true);
        bundle.putBoolean(SetupWizardApp.EXTRA_SHOW_BUTTON_BAR, true);
        AccountManager.get(activity)
                .addAccount(activity.getString(R.string.cm_account_type), null, null, bundle,
                        null, new AccountManagerCallback<Bundle>() {
                            @Override
                            public void run(AccountManagerFuture<Bundle> future) {
                                try {
                                    Bundle result = future.getResult();
                                    Intent intent = result
                                            .getParcelable(AccountManager.KEY_INTENT);
                                    ActivityOptions options =
                                            ActivityOptions.makeCustomAnimation(activity,
                                                    android.R.anim.fade_in,
                                                    android.R.anim.fade_out);
                                    activity.startActivityForResult(intent,
                                            SetupWizardApp.REQUEST_CODE_SETUP_CYANOGEN,
                                            options.toBundle());
                                } catch (OperationCanceledException e) {
                                } catch (IOException e) {
                                } catch (AuthenticatorException e) {
                                }
                            }
                        }, null);
    }
}
