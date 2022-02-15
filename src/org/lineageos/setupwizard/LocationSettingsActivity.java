/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017-2021 The LineageOS Project
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

import android.location.LocationManager;
import android.os.Bundle;
import android.os.Process;
import android.os.UserManager;
import android.view.View;
import android.widget.CheckBox;

public class LocationSettingsActivity extends BaseSetupWizardActivity {

    public static final String TAG =
            LocationSettingsActivity.class.getSimpleName().substring(0, 22);

    private CheckBox mLocationAccess;

    private LocationManager mLocationManager;

    private UserManager mUserManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextText(R.string.next);

        mLocationAccess = (CheckBox) findViewById(R.id.location_checkbox);
        mLocationManager = getSystemService(LocationManager.class);
        mUserManager = getSystemService(UserManager.class);
        View locationAccessView = findViewById(R.id.location);
        locationAccessView.setOnClickListener(v -> {
            mLocationAccess.setChecked(!mLocationAccess.isChecked());
            mLocationManager.setLocationEnabledForUser(mLocationAccess.isChecked(),
                    Process.myUserHandle());
            if (mUserManager.isManagedProfile()) {
                mUserManager.setUserRestriction(UserManager.DISALLOW_SHARE_LOCATION,
                        !mLocationAccess.isChecked());
            }
        });
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
