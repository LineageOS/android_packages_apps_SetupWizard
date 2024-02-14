/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard;

import android.location.LocationManager;
import android.os.Bundle;
import android.os.Process;
import android.os.UserManager;
import android.provider.Settings;
import android.view.View;
import android.widget.CheckBox;

public class LocationSettingsActivity extends BaseSetupWizardActivity {

    private CheckBox mLocationAccess;
    private CheckBox mLocationAgpsAccess;

    private LocationManager mLocationManager;

    private UserManager mUserManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextText(R.string.next);

        mLocationAccess = findViewById(R.id.location_checkbox);
        mLocationAgpsAccess = findViewById(R.id.location_agps_checkbox);
        mLocationManager = getSystemService(LocationManager.class);
        mUserManager = getSystemService(UserManager.class);
        View locationAccessView = findViewById(R.id.location);
        locationAccessView.setOnClickListener(
                v -> mLocationAccess.setChecked(!mLocationAccess.isChecked()));
        View locationAgpsAccessView = findViewById(R.id.location_agps);
        if (mUserManager.isMainUser()) {
            locationAgpsAccessView.setOnClickListener(
                    v -> mLocationAgpsAccess.setChecked(!mLocationAgpsAccess.isChecked()));
        } else {
            locationAgpsAccessView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean checked = mLocationManager.isLocationEnabled();
        if (mUserManager.isManagedProfile()) {
            checked &= mUserManager.hasUserRestriction(UserManager.DISALLOW_SHARE_LOCATION);
        }
        mLocationAccess.setChecked(checked);
    }

    @Override
    protected void onNextPressed() {
        mLocationManager.setLocationEnabledForUser(mLocationAccess.isChecked(),
                Process.myUserHandle());
        if (mUserManager.isManagedProfile()) {
            mUserManager.setUserRestriction(UserManager.DISALLOW_SHARE_LOCATION,
                    !mLocationAccess.isChecked());
        }
        Settings.Global.putInt(getContentResolver(), Settings.Global.ASSISTED_GPS_ENABLED,
                mLocationAgpsAccess.isChecked() ? 1 : 0);
        super.onNextPressed();
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

}
