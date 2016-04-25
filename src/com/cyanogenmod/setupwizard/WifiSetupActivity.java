/*
 * Copyright (C) 2016 The CyanogenMod Project
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

package com.cyanogenmod.setupwizard;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.CaptivePortal;
import android.net.ConnectivityManager;
import android.net.ICaptivePortal;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import com.android.setupwizardlib.util.ResultCodes;
import com.android.setupwizardlib.util.WizardManagerHelper;
import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class WifiSetupActivity extends BaseSetupWizardActivity {

    public static final String TAG = WifiSetupActivity.class.getSimpleName();

    private static final String DEFAULT_SERVER = "clients3.google.com";
    private static final int CAPTIVE_PORTAL_SOCKET_TIMEOUT_MS = 10000;

    private URL mCaptivePortalUrl;

    private boolean mIsCaptivePortal = false;

    private final Handler mHandler = new Handler();

    private String mResponseToken;

    private Runnable mFinishCaptivePortalCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if (mIsCaptivePortal) {
                try {
                    final Context context = WifiSetupActivity.this;
                    mResponseToken = String.valueOf(new Random().nextLong());
                    final Intent intent = new Intent(
                            ConnectivityManager.ACTION_CAPTIVE_PORTAL_SIGN_IN);
                    intent.putExtra(Intent.EXTRA_TEXT, mResponseToken);
                    intent.putExtra(ConnectivityManager.EXTRA_NETWORK,
                            ConnectivityManager.from(context)
                                    .getNetworkForType(ConnectivityManager.TYPE_WIFI));
                    intent.putExtra(ConnectivityManager.EXTRA_CAPTIVE_PORTAL,
                            new CaptivePortal(new ICaptivePortal.Stub() {
                                @Override
                                public void appResponse(int response) {}
                            }));
                    intent.putExtra("status_bar_color",
                            context.getResources().getColor(R.color.primary_dark));
                    intent.putExtra("action_bar_color", context.getResources().getColor(
                            R.color.primary_dark));
                    intent.putExtra("progress_bar_color", context.getResources().getColor(
                            R.color.accent));
                    ActivityOptions options =
                            ActivityOptions.makeCustomAnimation(context,
                                    android.R.anim.fade_in,
                                    android.R.anim.fade_out);
                    startActivityForResult(intent,
                            SetupWizardApp.REQUEST_CODE_SETUP_CAPTIVE_PORTAL,
                            options.toBundle());
                } catch (Exception e) {
                    //Oh well
                    Log.e(TAG, "No captive portal activity found" + e);
                    onNavigateNext();
                }
            } else {
                onNavigateNext();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String server = Settings.Global.getString(getContentResolver(), "captive_portal_server");
        if (server == null) server = DEFAULT_SERVER;
        try {
            mCaptivePortalUrl = new URL("http://" + server + "/generate_204");
        } catch (MalformedURLException e) {
            Log.e(TAG, "Not a valid url" + e);
        }
        setContentView(R.layout.setup_loading_page);
        TextView title = (TextView) findViewById(android.R.id.title);
        title.setText(R.string.loading);
        setNextText(R.string.skip);

        launchWifiSetup();
    }

    @Override
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        int result = Activity.RESULT_OK;
        if (!((SetupWizardApp)getApplication()).isSimInserted()) {
            result = ResultCodes.RESULT_SKIP;
        }
        Intent intent = WizardManagerHelper.getNextIntent(getIntent(), result);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SetupWizardApp.REQUEST_CODE_SETUP_WIFI) {
            if (resultCode == Activity.RESULT_CANCELED) {
                onNavigateBack();
            } else if (resultCode == Activity.RESULT_OK) {
                checkForCaptivePortal();
            } else {
                onNavigateNext();
                finish();
            }
        } else if (requestCode == SetupWizardApp.REQUEST_CODE_SETUP_CAPTIVE_PORTAL) {
            if (data == null) {
                launchWifiSetup();
            }
            String token = data.getStringExtra("response_token");
            if (token != null && !token.equals(mResponseToken)) {
                launchWifiSetup();
            } else {
                if (resultCode == Activity.RESULT_CANCELED) {
                    launchWifiSetup();
                } else {
                    onNavigateNext();
                    finish();
                }
            }
        }
    }

    private void checkForCaptivePortal() {
        new Thread() {
            @Override
            public void run() {
                mIsCaptivePortal = isCaptivePortal();
                mHandler.post(mFinishCaptivePortalCheckRunnable);
            }
        }.start();
    }

    // Don't run on UI thread
    private boolean isCaptivePortal() {
        if (mCaptivePortalUrl == null) return false;
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) mCaptivePortalUrl.openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setConnectTimeout(CAPTIVE_PORTAL_SOCKET_TIMEOUT_MS);
            urlConnection.setReadTimeout(CAPTIVE_PORTAL_SOCKET_TIMEOUT_MS);
            urlConnection.setUseCaches(false);
            urlConnection.getInputStream();
            // We got a valid response, but not from the real google
            final int responseCode = urlConnection.getResponseCode();
            if (responseCode == 408 || responseCode == 504) {
                // If we timeout here, we'll try and go through captive portal login
                return true;
            }
            return urlConnection.getResponseCode() != 204;
        } catch (IOException e) {
            Log.e(TAG, "Captive portal check - probably not a portal: exception "
                    + e);
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private void launchWifiSetup() {
        SetupWizardUtils.tryEnablingWifi(this);
        Intent intent = new Intent(SetupWizardApp.ACTION_SETUP_WIFI);
        if (SetupWizardUtils.hasLeanback(this)) {
            intent.setComponent(SetupWizardUtils.mTvwifisettingsActivity);
        }
        intent.putExtra(SetupWizardApp.EXTRA_FIRST_RUN, true);
        intent.putExtra(SetupWizardApp.EXTRA_ALLOW_SKIP, true);
        intent.putExtra(SetupWizardApp.EXTRA_USE_IMMERSIVE, true);
        intent.putExtra(SetupWizardApp.EXTRA_THEME, SetupWizardApp.EXTRA_MATERIAL_LIGHT);
        intent.putExtra(SetupWizardApp.EXTRA_AUTO_FINISH, false);
        ActivityOptions options =
                ActivityOptions.makeCustomAnimation(this,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out);
        startActivityForResult(intent,
                SetupWizardApp.REQUEST_CODE_SETUP_WIFI, options.toBundle());
    }

}
