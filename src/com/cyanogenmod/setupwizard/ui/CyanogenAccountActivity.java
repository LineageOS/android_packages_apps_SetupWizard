/*
 * Copyright (C) 2016 The CyanogenMod Project
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

package com.cyanogenmod.setupwizard.ui;

import com.android.setupwizardlib.util.ResultCodes;
import com.android.setupwizardlib.util.SystemBarHelper;
import com.android.setupwizardlib.util.WizardManagerHelper;
import com.cyanogenmod.setupwizard.SetupWizardApp;
import com.cyanogenmod.setupwizard.cmstats.SetupStats;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;
import com.cyanogenmod.setupwizard.R;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.Nullable;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

public class CyanogenAccountActivity extends Activity {

    public static final String TAG = "CyanogenAccountActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemBarHelper.hideSystemBars(getWindow());
        if (!SetupWizardUtils.accountExists(this,
                getString(R.string.cm_account_type))) {
            launchCyanogenAccountSetup();
        } else {
            done(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SetupWizardApp.REQUEST_CODE_SETUP_CYANOGEN) {
            if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_FIRST_USER) {
                SetupStats.addEvent(SetupStats.Categories.EXTERNAL_PAGE_LOAD,
                        SetupStats.Action.EXTERNAL_PAGE_RESULT,
                        SetupStats.Label.CYANOGEN_ACCOUNT,
                        resultCode == Activity.RESULT_OK ? "success" : "skipped");
                done(true);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                SetupStats.addEvent(SetupStats.Categories.EXTERNAL_PAGE_LOAD,
                        SetupStats.Action.EXTERNAL_PAGE_RESULT,
                        SetupStats.Label.CYANOGEN_ACCOUNT, "canceled");
               done(false);
            }
        }
    }

    private void launchCyanogenAccountSetup() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SetupWizardApp.EXTRA_FIRST_RUN, true);
        bundle.putBoolean(SetupWizardApp.EXTRA_SHOW_BUTTON_BAR, true);
        bundle.putBoolean(SetupWizardApp.EXTRA_USE_IMMERSIVE, true);
        final Context context = getApplicationContext();
        AccountManager.get(context)
                .addAccount(context.getString(R.string.cm_account_type), null, null, bundle,
                        null, new AccountManagerCallback<Bundle>() {
                            @Override
                            public void run(AccountManagerFuture<Bundle> future) {
                                boolean error = false;
                                try {
                                    Bundle result = future.getResult();
                                    Intent intent = result
                                            .getParcelable(AccountManager.KEY_INTENT);
                                    ActivityOptions options =
                                            ActivityOptions.makeCustomAnimation(context,
                                                    android.R.anim.fade_in,
                                                    android.R.anim.fade_out);
                                    SetupStats
                                            .addEvent(SetupStats.Categories.EXTERNAL_PAGE_LOAD,
                                                    SetupStats.Action.EXTERNAL_PAGE_LAUNCH,
                                                    SetupStats.Label.PAGE,
                                                    SetupStats.Label.CYANOGEN_ACCOUNT);
                                    startActivityForResult(intent,
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
                                    if (error) {
                                       done(false);
                                    }
                                }
                            }
                        }, null);
    }

    private void done(boolean success) {
        int resultCode = success ? Activity.RESULT_OK : ResultCodes.RESULT_SKIP;
        Intent intent = WizardManagerHelper.getNextIntent(getIntent(), resultCode);
        startActivityForResult(intent, 1);
        if (success) {
            finish();
        }
    }
}
