/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.system

import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.telephony.euicc.EuiccManager
import android.util.Log
import com.google.android.setupdesign.GlifRecyclerLayout
import com.google.android.setupdesign.items.ItemGroup
import com.google.android.setupdesign.items.RecyclerItemAdapter
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.base.BaseSetupWizardActivity

class DeviceInfoActivity : BaseSetupWizardActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNextText(R.string.ok)

        val itemGroup = ItemGroup()
        for ((label, value) in deviceIdentifiers()) {
            itemGroup.addChild(DeviceInfoItem(label, value))
        }

        val adapter = RecyclerItemAdapter(itemGroup)
        adapter.setOnItemSelectedListener { item -> (item as? DeviceInfoItem)?.toggle() }
        (glifLayout as GlifRecyclerLayout).adapter = adapter
    }

    private fun deviceIdentifiers(): List<Pair<String, String>> {
        val identifiers = mutableListOf<Pair<String, String>>()

        val telephonyManager = getSystemService(TelephonyManager::class.java)
        if (telephonyManager != null) {
            val slotCount = telephonyManager.activeModemCount
            for (slot in 0 until slotCount) {
                val imei = runCatching { telephonyManager.getImei(slot) }.getOrNull()
                if (!imei.isNullOrEmpty()) {
                    val label =
                        if (slotCount > 1) {
                            getString(R.string.device_info_imei_slot, slot + 1)
                        } else {
                            getString(R.string.device_info_imei)
                        }
                    identifiers += label to imei
                }
            }
        }

        val eid = runCatching { getSystemService(EuiccManager::class.java)?.eid }.getOrNull()
        if (!eid.isNullOrEmpty()) {
            identifiers += getString(R.string.device_info_eid) to eid
        }

        val serial = runCatching { Build.getSerial() }.getOrNull()
        if (!serial.isNullOrEmpty() && serial != Build.UNKNOWN) {
            identifiers += getString(R.string.device_info_serial) to serial
        }

        if (identifiers.isEmpty()) {
            Log.w(TAG, "No device identifiers to show")
        }
        return identifiers
    }

    override fun onNextPressed() {
        finish()
    }

    override fun onSkipPressed() {
        finish()
    }

    override val layoutResId: Int = R.layout.device_info_activity

    override val titleResId: Int = R.string.device_information

    override val iconResId: Int = R.drawable.ic_info

    companion object {
        private const val TAG = "DeviceInfoActivity"
    }
}
