/*
 * Copyright (C) 2015 The CyanogenMod Project
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

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.cmstats.SetupStats;
import com.cyanogenmod.setupwizard.ui.SetupPageFragment;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class OtherSettingsPage extends SetupPage {

    public static final String TAG = "OtherSettingsPage";

    public OtherSettingsPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
    }

    @Override
    public Fragment getFragment(FragmentManager fragmentManager, int action) {
        Fragment fragment = fragmentManager.findFragmentByTag(getKey());
        if (fragment == null) {
            Bundle args = new Bundle();
            args.putString(Page.KEY_PAGE_ARGUMENT, getKey());
            args.putInt(Page.KEY_PAGE_ACTION, action);
            fragment = new OtherSettingsFragment();
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
        if (SetupWizardUtils.hasGMS(mContext)) {
            return R.string.setup_other;
        } else {
            return R.string.setup_location;
        }
    }

    public static class OtherSettingsFragment extends SetupPageFragment {

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
        private BroadcastReceiver mReceiver;


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
        protected void initializePage() {
            final boolean hasTelephony = SetupWizardUtils.hasTelephony(getActivity());
            mContentResolver = getActivity().getContentResolver();
            TextView summaryView = (TextView) mRootView.findViewById(android.R.id.summary);
            summaryView.setText(R.string.location_services_summary);
            mLocationRow = mRootView.findViewById(R.id.location);
            mLocationRow.setOnClickListener(mLocationClickListener);
            mLocationAccess = (CheckBox) mRootView.findViewById(R.id.location_checkbox);
            mBatteryRow = mRootView.findViewById(R.id.battery_saving);
            mBatteryRow.setOnClickListener(mBatteryClickListener);
            mBattery = (CheckBox) mRootView.findViewById(R.id.battery_saving_checkbox);
            mNetworkRow = mRootView.findViewById(R.id.network);
            mNetworkRow.setOnClickListener(mNetworkClickListener);
            mNetwork = (CheckBox) mRootView.findViewById(R.id.network_checkbox);
            TextView networkSummary = (TextView) mRootView.findViewById(R.id.network_summary);
            if (hasTelephony) {
                networkSummary.setText(R.string.location_network_telephony);
            } else {
                networkSummary.setText(R.string.location_network);
            }
        }

        @Override
        protected int getLayoutResource() {
            return R.layout.location_settings;
        }

        @Override
        public void onResume() {
            super.onResume();
            refreshLocationMode();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        Log.d(TAG, "Received location mode change intent: " + intent);
                    }
                    refreshLocationMode();
                }
            };
        }

        private void setLocationMode(int mode) {
            Intent intent = new Intent(MODE_CHANGING_ACTION);
            intent.putExtra(CURRENT_MODE_KEY, mCurrentMode);
            intent.putExtra(NEW_MODE_KEY, mode);
            getActivity().sendBroadcast(intent, android.Manifest.permission.WRITE_SECURE_SETTINGS);
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
            SetupStats.addEvent(SetupStats.Categories.SETTING_CHANGED,
                    SetupStats.Action.ENABLE_LOCATION,
                    SetupStats.Label.CHECKED, String.valueOf(checked));

            if (checked) {
                setLocationMode(Settings.Secure.LOCATION_MODE_SENSORS_ONLY);
            } else {
                setLocationMode(Settings.Secure.LOCATION_MODE_OFF);
            }
        }

        private void onToggleBatterySaving(boolean checked) {
            /* SetupStats.addEvent(SetupStats.Categories.SETTING_CHANGED,
                    SetupStats.Action.ENABLE_BATTERY_SAVING_LOCATION,
                    SetupStats.Label.CHECKED, String.valueOf(checked)); */

            if (checked) {
                setLocationMode(Settings.Secure.LOCATION_MODE_BATTERY_SAVING);
            } else {
                setLocationMode(Settings.Secure.LOCATION_MODE_SENSORS_ONLY);
            }
        }

        private void onToggleNetwork(boolean checked) {
            SetupStats.addEvent(SetupStats.Categories.SETTING_CHANGED,
                    SetupStats.Action.ENABLE_NETWORK_LOCATION,
                    SetupStats.Label.CHECKED, String.valueOf(checked));

            if (checked) {
                setLocationMode(Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
            } else {
                setLocationMode(Settings.Secure.LOCATION_MODE_SENSORS_ONLY);
            }
        }
    }
}
