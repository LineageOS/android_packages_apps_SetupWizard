/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.system

import android.content.Intent
import org.lineageos.setupwizard.base.SubBaseActivity
import org.lineageos.setupwizard.util.SetupWizardUtils

class DeviceSpecificActivity : SubBaseActivity() {

    override fun onStartSubactivity() {
        val intent = Intent(ACTION_SETUP_DEVICE)
        if (intent.resolveActivity(packageManager) != null) {
            startSubactivity(intent)
        } else {
            SetupWizardUtils.disableComponent(this, DeviceSpecificActivity::class.java)
            finishAction(RESULT_OK)
        }
    }

    companion object {
        private const val ACTION_SETUP_DEVICE = "org.lineageos.settings.device.SUW_SETTINGS"
    }
}
