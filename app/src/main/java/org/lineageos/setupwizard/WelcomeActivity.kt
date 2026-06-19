/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.setupwizard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.setupcompat.util.SystemBarHelper
import org.lineageos.setupwizard.SetupWizardApp.Companion.ACTION_EMERGENCY_DIAL
import org.lineageos.setupwizard.base.SubBaseActivity
import org.lineageos.setupwizard.util.SetupWizardUtils

class WelcomeActivity : SubBaseActivity() {

    override fun onStartSubactivity() {
        // no-op
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onSetupStart()
        SystemBarHelper.setBackButtonVisible(window, false)

        val startButton: Button = findViewById(R.id.start)
        val emergButton: Button = findViewById(R.id.emerg_dialer)
        val skipButton: Button = findViewById(R.id.skip)

        startButton.setOnClickListener { onNextPressed() }
        findViewById<View>(R.id.launch_accessibility).setOnClickListener {
            startSubactivity(Intent(ACTION_ACCESSIBILITY_SETTINGS))
        }

        if (SetupWizardUtils.hasTelephony(this)) {
            emergButton.setOnClickListener { startSubactivity(Intent(ACTION_EMERGENCY_DIAL)) }
        } else {
            emergButton.visibility = View.GONE
        }

        val welcomeTitle: TextView = findViewById(R.id.welcome_title)
        welcomeTitle.text =
            if (SetupWizardUtils.isManagedProfile(this)) {
                getString(R.string.setup_managed_profile_welcome_message)
            } else {
                getString(R.string.setup_welcome_message, getString(R.string.os_name))
            }

        if (Build.TYPE == "eng") {
            skipButton.visibility = View.VISIBLE
            skipButton.setOnClickListener {
                SetupWizardUtils.finishSetupWizard(this@WelcomeActivity)
            }
        }
    }

    override fun onBackPressed() {}

    override val layoutResId: Int = R.layout.welcome_activity

    override val titleResId: Int = -1

    companion object {
        private const val ACTION_ACCESSIBILITY_SETTINGS =
            "android.settings.ACCESSIBILITY_SETTINGS_FOR_SUW"
    }
}
