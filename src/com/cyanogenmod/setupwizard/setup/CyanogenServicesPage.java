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
import android.util.Log;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.SetupWizardApp;
import com.cyanogenmod.setupwizard.cmstats.SetupStats;
import com.cyanogenmod.setupwizard.ui.LoadingFragment;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

import java.io.IOException;

public class CyanogenServicesPage extends SetupPage {

    public static final String TAG = "CyanogenServicesPage";

    private Fragment mFragment;

    public CyanogenServicesPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
    }

    @Override
    public Fragment getFragment(FragmentManager fragmentManager, int action) {
        mFragment = fragmentManager.findFragmentByTag(getKey());
        if (mFragment == null) {
            Bundle args = new Bundle();
            args.putString(Page.KEY_PAGE_ARGUMENT, getKey());
            args.putInt(Page.KEY_PAGE_ACTION, action);
            mFragment = new LoadingFragment();
            mFragment.setArguments(args);
        }
        return mFragment;
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
    public void doLoadAction(FragmentManager fragmentManager, int action) {
        if (action == Page.ACTION_PREVIOUS) {
            getCallbacks().onPreviousPage();
        } else {
            if (!SetupWizardUtils.accountExists(mContext,
                    mContext.getString(R.string.cm_account_type))) {
                super.doLoadAction(fragmentManager, action);
                launchCyanogenAccountSetup();
            } else {
                getCallbacks().onNextPage();
            }
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SetupWizardApp.REQUEST_CODE_SETUP_CYANOGEN) {
            if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_FIRST_USER) {
                SetupStats.addEvent(SetupStats.Categories.EXTERNAL_PAGE_LOAD,
                        SetupStats.Action.EXTERNAL_PAGE_RESULT,
                        SetupStats.Label.CYANOGEN_ACCOUNT,
                        resultCode == Activity.RESULT_OK ? "success" : "skipped");
                if (SetupWizardUtils.accountExists(mContext,
                        mContext.getString(R.string.cm_account_type))) {
                    if (SetupWizardUtils.isDeviceLocked()) {
                        ((SetupWizardApp) mContext.getApplicationContext()).setIsAuthorized(true);
                    }
                    setHidden(true);
                }
                getCallbacks().onNextPage();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                SetupStats.addEvent(SetupStats.Categories.EXTERNAL_PAGE_LOAD,
                        SetupStats.Action.EXTERNAL_PAGE_RESULT,
                        SetupStats.Label.CYANOGEN_ACCOUNT, "canceled");
                getCallbacks().onPreviousPage();
            }
        }
        return true;
    }

    private void launchCyanogenAccountSetup() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SetupWizardApp.EXTRA_FIRST_RUN, true);
        bundle.putBoolean(SetupWizardApp.EXTRA_SHOW_BUTTON_BAR, true);
        bundle.putBoolean(SetupWizardApp.EXTRA_USE_IMMERSIVE, true);
        AccountManager.get(mContext)
                .addAccount(mContext.getString(R.string.cm_account_type), null, null, bundle,
                        null, new AccountManagerCallback<Bundle>() {
                            @Override
                            public void run(AccountManagerFuture<Bundle> future) {
                                boolean error = false;
                                try {
                                    Bundle result = future.getResult();
                                    Intent intent = result
                                            .getParcelable(AccountManager.KEY_INTENT);
                                    ActivityOptions options =
                                            ActivityOptions.makeCustomAnimation(mContext,
                                                    android.R.anim.fade_in,
                                                    android.R.anim.fade_out);
                                    SetupStats
                                            .addEvent(SetupStats.Categories.EXTERNAL_PAGE_LOAD,
                                                    SetupStats.Action.EXTERNAL_PAGE_LAUNCH,
                                                    SetupStats.Label.PAGE,
                                                    SetupStats.Label.CYANOGEN_ACCOUNT);
                                    mFragment.startActivityForResult(intent,
                                            SetupWizardApp.REQUEST_CODE_SETUP_CYANOGEN,
                                            options.toBundle());
                                } catch (OperationCanceledException e) {
                                    error = true;
                                } catch (IOException e) {
                                    error = true;
                                } catch (AuthenticatorException e) {
                                    Log.e(TAG, "Error launching cm account", e);
                                    error = true;
                                } finally {
                                    if (error && getCallbacks().
                                            isCurrentPage(CyanogenServicesPage.this)) {
                                        getCallbacks().onNextPage();
                                    }
                                }
                            }
                        }, null);
    }
}
