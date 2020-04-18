/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017-2020 The LineageOS Project
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
            if (!mLocationAccess.isChecked()) {
                mLocationAccess.setChecked(true);
                setLocationMode(Settings.Secure.LOCATION_MODE_ON);
            } else {
                mLocationAccess.setChecked(false);
                setLocationMode(Settings.Secure.LOCATION_MODE_OFF);
            }
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
            mLocationAccess.setChecked(mode == Settings.Secure.LOCATION_MODE_ON);
        }
    }
}
