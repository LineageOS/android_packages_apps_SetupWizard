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

import static com.cyanogenmod.setupwizard.SetupWizardApp.REQUEST_CODE_SETUP_CAPTIVE_PORTAL;

import android.content.Context;
import android.content.Intent;
import android.net.CaptivePortal;
import android.net.ConnectivityManager;
import android.net.ICaptivePortal;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class CaptivePortalSetupActivity extends SubBaseActivity {

    public static final String TAG = CaptivePortalSetupActivity.class.getSimpleName();

    private static final String DEFAULT_SERVER = "clients3.google.com";
    private static final int CAPTIVE_PORTAL_SOCKET_TIMEOUT_MS = 10000;

    private URL mCaptivePortalUrl;

    @Override
    protected void onStartSubactivity() {
        String server = Settings.Global.getString(getContentResolver(), "captive_portal_server");
        if (server == null) server = DEFAULT_SERVER;
        try {
            mCaptivePortalUrl = new URL("http://" + server + "/generate_204");
        } catch (MalformedURLException e) {
            Log.e(TAG, "Not a valid url" + e);
        }
        CheckForCaptivePortalTask
                .checkForCaptivePortal(mCaptivePortalUrl, this, true);
    }

    @Override
    protected int getSubactivityNextTransition() {
        return TRANSITION_ID_FADE;
    }

    private static class CheckForCaptivePortalTask extends AsyncTask<Void, Void, Boolean> {

        private final URL captivePortalUrl;
        private final CaptivePortalSetupActivity captivePortalSetupActivity;
        private static CheckForCaptivePortalTask sTask = null;
        private String responseToken;

        public CheckForCaptivePortalTask(URL captivePortalUrl,
                CaptivePortalSetupActivity captivePortalSetupActivity) {
            this.captivePortalUrl = captivePortalUrl;
            this.captivePortalSetupActivity = captivePortalSetupActivity;
        }

        public static void checkForCaptivePortal(URL captivePortalUrl,
                CaptivePortalSetupActivity captivePortalSetupActivity, boolean cancelAndRecreateIfRunning) {
            if (sTask == null || sTask.getStatus() == Status.FINISHED) {
                sTask = new CheckForCaptivePortalTask(captivePortalUrl, captivePortalSetupActivity);
                sTask.execute();

            } else if (cancelAndRecreateIfRunning) {
                sTask.cancel(true);
                sTask = new CheckForCaptivePortalTask(captivePortalUrl, captivePortalSetupActivity);
                sTask.execute();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (captivePortalUrl == null) return false;
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) captivePortalUrl.openConnection();
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

        @Override
        protected void onPostExecute(Boolean isPortal) {
            if (isPortal) {
                final Context context = captivePortalSetupActivity.getApplicationContext();
                responseToken = String.valueOf(new Random().nextLong());
                final Intent intent = new Intent(
                        ConnectivityManager.ACTION_CAPTIVE_PORTAL_SIGN_IN);
                intent.putExtra(Intent.EXTRA_TEXT, responseToken);
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
                captivePortalSetupActivity.startSubactivity(intent, REQUEST_CODE_SETUP_CAPTIVE_PORTAL);
            } else {
                captivePortalSetupActivity.finishAction(RESULT_OK);
                captivePortalSetupActivity.finish();
            }
        }
    }

}
