/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard

import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.lineageos.setupwizard.SetupWizardApp.Companion.ACTION_LOAD
import org.lineageos.setupwizard.SetupWizardApp.Companion.EXTRA_SCRIPT_URI
import org.lineageos.setupwizard.SetupWizardApp.Companion.EXTRA_WIZARD_BUNDLE
import org.lineageos.setupwizard.SetupWizardApp.Companion.LOGV
import org.lineageos.setupwizard.util.SetupWizardUtils
import org.lineageos.setupwizard.wizardmanager.WizardManager

class SetupWizardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (LOGV) {
            Log.v(TAG, "onCreate savedInstanceState=$savedInstanceState")
        }
        if (SetupWizardUtils.hasLeanback(this) && SetupWizardUtils.hasGMS(this)) {
            finish()
            return
        }
        SetupWizardUtils.enableComponent(this, WizardManager::class.java)
        val scriptUri =
            when {
                SetupWizardUtils.isOwner() -> R.string.lineage_wizard_script_uri
                SetupWizardUtils.isManagedProfile(this) ->
                    R.string.lineage_wizard_script_managed_profile_uri
                else -> R.string.lineage_wizard_script_user_uri
            }
        val wizardBundle = Bundle().apply { putString(EXTRA_SCRIPT_URI, getString(scriptUri)) }
        val intent =
            Intent(ACTION_LOAD).apply {
                putExtra(EXTRA_WIZARD_BUNDLE, wizardBundle)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or FLAG_GRANT_READ_URI_PERMISSION
                setPackage(packageName)
            }
        startActivity(intent)
        finish()
    }

    companion object {
        private val TAG: String = SetupWizardActivity::class.java.simpleName
    }
}
