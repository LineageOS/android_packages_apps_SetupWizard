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
import com.google.android.setupdesign.GlifRecyclerLayout
import com.google.android.setupdesign.items.RecyclerItemAdapter
import com.google.android.setupdesign.items.SwitchItem
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.base.BaseSetupWizardActivity

class LocationSettingsActivity : BaseSetupWizardActivity() {

    private val itemAdapter by lazy {
        (glifLayout as GlifRecyclerLayout).adapter as RecyclerItemAdapter
    }

    private val locationAccess by lazy {
        itemAdapter.findItemById(R.id.location_item) as SwitchItem
    }
    private val locationAgpsAccess by lazy {
        itemAdapter.findItemById(R.id.location_agps_item) as SwitchItem
    }

    private lateinit var locationManager: LocationManager
    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNextText(R.string.next)
        glifLayout.setDescriptionText(getString(R.string.location_summary))

        locationManager = getSystemService(LocationManager::class.java)
        userManager = getSystemService(UserManager::class.java)

        locationAgpsAccess.isVisible = userManager.isMainUser

        itemAdapter.setOnItemSelectedListener { item ->
            if (item is SwitchItem) {
                item.isChecked = !item.isChecked
            }
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
