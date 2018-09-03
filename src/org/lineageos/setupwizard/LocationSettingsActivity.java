/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017-2018 The LineageOS Project
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

package org.lineageos.setupwizard;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.lineageos.setupwizard.util.SetupWizardUtils;

public class LocationSettingsActivity extends BaseSetupWizardActivity {

    public static final String TAG =
            LocationSettingsActivity.class.getSimpleName().substring(0, 22);

    private View mLocationRow;
    private View mBatteryRow;
    private View mNetworkRow;
    private CheckBox mNetwork;
    private CheckBox mBattery;
    private CheckBox mLocationAccess;

    private ContentResolver mContentResolver;


    /** Broadcast intent action when the location mode is about to change. */
    private static final String MODE_CHANGING_ACTION =
            "com.android.settings.location.MODE_CHANGING";
    private static final String CURRENT_MODE_KEY = "CURRENT_MODE";
    private static final String NEW_MODE_KEY = "NEW_MODE";

    private int mCurrentMode = Settings.Secure.LOCATION_MODE_OFF;

    private View.OnClickListener mLocationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onToggleLocationAccess(!mLocationAccess.isChecked());
        }
    };

    private View.OnClickListener mBatteryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onToggleBatterySaving(!mBattery.isChecked());
        }
    };

    private View.OnClickListener mNetworkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onToggleNetwork(!mNetwork.isChecked());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextText(R.string.next);

        mContentResolver = getContentResolver();
        TextView summaryView = (TextView) findViewById(android.R.id.summary);
        summaryView.setText(R.string.location_services_summary);
        mLocationRow = findViewById(R.id.location);
        mLocationRow.setOnClickListener(mLocationClickListener);
        mLocationAccess = (CheckBox) findViewById(R.id.location_checkbox);
        mBatteryRow = findViewById(R.id.battery_saving);
        mBatteryRow.setOnClickListener(mBatteryClickListener);
        mBattery = (CheckBox) findViewById(R.id.battery_saving_checkbox);
        mNetworkRow = findViewById(R.id.network);
        mNetworkRow.setOnClickListener(mNetworkClickListener);
        mNetwork = (CheckBox) findViewById(R.id.network_checkbox);
        TextView networkSummary = (TextView) findViewById(R.id.network_summary);
        final boolean hasTelephony = SetupWizardUtils.hasTelephony(this);
        if (hasTelephony) {
            networkSummary.setText(R.string.location_network_telephony);
        } else {
            networkSummary.setText(R.string.location_network);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshLocationMode();
    }

    @Override
    protected int getTransition() {
        return TRANSITION_ID_SLIDE;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.location_settings;
    }

    @Override
    protected int getTitleResId() {
        return R.string.setup_location;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_location;
    }

    private void setLocationMode(int mode) {
        Intent intent = new Intent(MODE_CHANGING_ACTION);
        intent.putExtra(CURRENT_MODE_KEY, mCurrentMode);
        intent.putExtra(NEW_MODE_KEY, mode);
        sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT),
                android.Manifest.permission.WRITE_SECURE_SETTINGS);
        Settings.Secure.putInt(mContentResolver, Settings.Secure.LOCATION_MODE, mode);
        refreshLocationMode();
    }

    private void refreshLocationMode() {
        int mode = Settings.Secure.getInt(mContentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF);

        if (mCurrentMode != mode) {
            mCurrentMode = mode;
            if (Log.isLoggable(TAG, Log.INFO)) {
                Log.i(TAG, "Location mode has been changed");
            }
            updateLocationToggles(mode);
        }
    }

    private void updateLocationToggles(int mode) {
        switch (mode) {
            case Settings.Secure.LOCATION_MODE_OFF:
                mLocationAccess.setChecked(false);
                mBattery.setChecked(false);
                mBattery.setEnabled(false);
                mBatteryRow.setEnabled(false);
                mNetwork.setChecked(false);
                mNetwork.setEnabled(false);
                mNetworkRow.setEnabled(false);
                break;
            case Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
                mLocationAccess.setChecked(true);
                mBattery.setChecked(false);
                mBattery.setEnabled(true);
                mBatteryRow.setEnabled(true);
                mNetwork.setChecked(false);
                mNetwork.setEnabled(true);
                mNetworkRow.setEnabled(true);
                break;
            case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
                mLocationAccess.setChecked(true);
                mBattery.setChecked(true);
                mNetwork.setChecked(false);
                mNetwork.setEnabled(false);
                mNetworkRow.setEnabled(false);
                break;
            case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
                mLocationAccess.setChecked(true);
                mNetwork.setChecked(true);
                mBattery.setChecked(false);
                mBattery.setEnabled(false);
                mBatteryRow.setEnabled(false);
                break;
            default:
                mLocationAccess.setChecked(false);
                mBattery.setChecked(false);
                mBattery.setEnabled(false);
                mBatteryRow.setEnabled(false);
                mNetwork.setChecked(false);
                mNetwork.setEnabled(false);
                mNetworkRow.setEnabled(false);
                break;
        }
    }

    private void onToggleLocationAccess(boolean checked) {
        if (checked) {
            setLocationMode(Settings.Secure.LOCATION_MODE_SENSORS_ONLY);
        } else {
            setLocationMode(Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    private void onToggleBatterySaving(boolean checked) {
        if (checked) {
            setLocationMode(Settings.Secure.LOCATION_MODE_BATTERY_SAVING);
        } else {
            setLocationMode(Settings.Secure.LOCATION_MODE_SENSORS_ONLY);
        }
    }

    private void onToggleNetwork(boolean checked) {
        if (checked) {
            setLocationMode(Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
        } else {
            setLocationMode(Settings.Secure.LOCATION_MODE_SENSORS_ONLY);
        }
    }

}
