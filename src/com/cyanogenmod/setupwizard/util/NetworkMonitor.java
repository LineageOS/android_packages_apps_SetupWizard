/*
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

package com.cyanogenmod.setupwizard.util;

import static com.cyanogenmod.setupwizard.SetupWizardApp.LOGV;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import com.cyanogenmod.setupwizard.SetupWizardApp;

public class NetworkMonitor {

    public static final String TAG = NetworkMonitor.class.getSimpleName();

    private static NetworkMonitor sInstance;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (LOGV) {
                Log.v(TAG, intent.toString());
            }
            NetworkMonitor.this.updateNetworkStatus(context);
        }
    };
    private Context mContext = null;
    private boolean mNetworkConnected = false;
    private NetworkInfo mNetworkInfo = null;

    public static void initInstance(Context context) {
        if (sInstance == null) {
            sInstance = new NetworkMonitor(context.getApplicationContext());
        }
    }

    public static NetworkMonitor getInstance() {
        return sInstance;
    }

    public NetworkMonitor(Context context) {
        mContext = context;
        if (LOGV) {
            Log.v(TAG, "Starting NetworkMonitor");
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        context.registerReceiver(mBroadcastReceiver, filter);
        new Handler().post(new Runnable() {
            public void run() {
                updateNetworkStatus(mContext);
            }
        });
    }

    public boolean isNetworkConnected() {
        if (LOGV) {
            Log.v(TAG, "isNetworkConnected() returns " + mNetworkConnected);
        }
        return mNetworkConnected;
    }

    public boolean isWifiConnected() {
        boolean wifiConnected = (!mNetworkConnected || mNetworkInfo == null) ?
                false :
                mNetworkInfo.getType() == 1;
        if (LOGV) {
            Log.v(TAG, "isWifiConnected() returns " + wifiConnected);
        }
        return wifiConnected;
    }

    public boolean checkIsNetworkConnected() {
        updateNetworkStatus(mContext);
        return isNetworkConnected();
    }

    private void onNetworkConnected(NetworkInfo ni) {
        if (LOGV) {
            Log.v(TAG, "onNetworkConnected()");
        }
        mNetworkConnected = true;
        mNetworkInfo = ni;
    }

    private void onNetworkDisconnected() {
        if (LOGV) {
            Log.v(TAG, "onNetworkDisconnected()");
        }
        mNetworkConnected = false;
        mNetworkInfo = null;
    }

    private boolean updateNetworkStatus(Context context) {
        ConnectivityManager cm = context.getSystemService(ConnectivityManager.class);
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            boolean isConnected = ni != null ? ni.isConnected() : false;
            if (isConnected && !mNetworkConnected) {
                onNetworkConnected(ni);
            } else if (!isConnected && mNetworkConnected) {
                onNetworkDisconnected();
            }
        }
        return mNetworkConnected;
    }
}
