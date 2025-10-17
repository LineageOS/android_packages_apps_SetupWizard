/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.user

import android.content.Intent
import android.provider.Settings
import com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP
import org.lineageos.setupwizard.base.SubBaseActivity
import org.lineageos.setupwizard.util.SetupWizardUtils

class BiometricActivity : SubBaseActivity() {

    override fun onStartSubactivity() {
        if (!SetupWizardUtils.hasBiometric(this)) {
            SetupWizardUtils.enableComponent(this, ScreenLockActivity::class.java)
            finishAction(RESULT_SKIP)
            return
        } else {
            SetupWizardUtils.disableComponent(this, ScreenLockActivity::class.java)
        }
        val intent: Intent = Intent(Settings.ACTION_BIOMETRIC_ENROLL)
        startSubactivity(intent)
    }
}
