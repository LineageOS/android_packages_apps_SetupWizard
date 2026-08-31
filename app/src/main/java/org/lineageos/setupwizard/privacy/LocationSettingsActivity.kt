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

    private lateinit var locationAccess: CheckBox
    private lateinit var locationAgpsAccess: CheckBox

    private lateinit var locationManager: LocationManager
    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNextText(R.string.next)

        locationAccess = findViewById(R.id.location_checkbox)
        locationAgpsAccess = findViewById(R.id.location_agps_checkbox)
        locationManager = getSystemService(LocationManager::class.java)
        userManager = getSystemService(UserManager::class.java)

        val locationAccessView = findViewById<View>(R.id.location)
        locationAccessView.setOnClickListener {
            locationAccess.isChecked = !locationAccess.isChecked
        }

        val locationAgpsAccessView = findViewById<View>(R.id.location_agps)
        if (userManager.isMainUser) {
            locationAgpsAccessView.setOnClickListener {
                locationAgpsAccess.isChecked = !locationAgpsAccess.isChecked
            }
        } else {
            locationAgpsAccessView.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        var checked = locationManager.isLocationEnabled
        if (userManager.isManagedProfile) {
            checked =
                checked and userManager.hasUserRestriction(UserManager.DISALLOW_SHARE_LOCATION)
        }
        locationAccess.isChecked = checked
    }

    override fun onNextPressed() {
        locationManager.setLocationEnabledForUser(locationAccess.isChecked, Process.myUserHandle())
        if (userManager.isManagedProfile) {
            userManager.setUserRestriction(
                UserManager.DISALLOW_SHARE_LOCATION,
                !locationAccess.isChecked,
            )
        }
        Settings.Global.putInt(
            contentResolver,
            Settings.Global.ASSISTED_GPS_ENABLED,
            if (locationAgpsAccess.isChecked) 1 else 0,
        )
        super.onNextPressed()
    }

    override val layoutResId: Int = R.layout.location_settings

    override val titleResId: Int = R.string.setup_location

    override val iconResId: Int = R.drawable.ic_location
}
