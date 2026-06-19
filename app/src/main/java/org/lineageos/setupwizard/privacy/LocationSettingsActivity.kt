/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.privacy

import android.location.LocationManager
import android.os.Bundle
import android.os.Process
import android.os.UserManager
import android.provider.Settings
import android.view.View
import android.widget.CheckBox
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.base.BaseSetupWizardActivity

class LocationSettingsActivity : BaseSetupWizardActivity() {

    private lateinit var mLocationAccess: CheckBox
    private lateinit var mLocationAgpsAccess: CheckBox

    private lateinit var mLocationManager: LocationManager
    private lateinit var mUserManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNextText(R.string.next)

        mLocationAccess = findViewById(R.id.location_checkbox)
        mLocationAgpsAccess = findViewById(R.id.location_agps_checkbox)
        mLocationManager = getSystemService(LocationManager::class.java)
        mUserManager = getSystemService(UserManager::class.java)

        val locationAccessView = findViewById<View>(R.id.location)
        locationAccessView.setOnClickListener {
            mLocationAccess.isChecked = !mLocationAccess.isChecked
        }

        val locationAgpsAccessView = findViewById<View>(R.id.location_agps)
        if (mUserManager.isMainUser) {
            locationAgpsAccessView.setOnClickListener {
                mLocationAgpsAccess.isChecked = !mLocationAgpsAccess.isChecked
            }
        } else {
            locationAgpsAccessView.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        var checked = mLocationManager.isLocationEnabled
        if (mUserManager.isManagedProfile) {
            checked =
                checked and mUserManager.hasUserRestriction(UserManager.DISALLOW_SHARE_LOCATION)
        }
        mLocationAccess.isChecked = checked
    }

    override fun onNextPressed() {
        mLocationManager.setLocationEnabledForUser(
            mLocationAccess.isChecked,
            Process.myUserHandle(),
        )
        if (mUserManager.isManagedProfile) {
            mUserManager.setUserRestriction(
                UserManager.DISALLOW_SHARE_LOCATION,
                !mLocationAccess.isChecked,
            )
        }
        Settings.Global.putInt(
            contentResolver,
            Settings.Global.ASSISTED_GPS_ENABLED,
            if (mLocationAgpsAccess.isChecked) 1 else 0,
        )
        super.onNextPressed()
    }

    override fun getLayoutResId(): Int = R.layout.location_settings

    override fun getTitleResId(): Int = R.string.setup_location

    override fun getIconResId(): Int = R.drawable.ic_location
}
