/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.setupwizard

import org.lineageos.setupwizard.SetupWizardApp.ACTION_EMERGENCY_DIAL

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

import com.google.android.setupcompat.template.FooterButtonStyleUtils
import com.google.android.setupcompat.util.SystemBarHelper

import org.lineageos.setupwizard.util.SetupWizardUtils

class WelcomeActivity : SubBaseActivity() {

    override fun onStartSubactivity() {
        // no-op
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onSetupStart()
        SystemBarHelper.setBackButtonVisible(getWindow(), false)

        setNextText(R.string.start)

        val startButton: Button = findViewById(R.id.start)
        val emergButton: Button = findViewById(R.id.emerg_dialer)
        val skipButton: Button = findViewById(R.id.skip)

        startButton.setOnClickListener { onNextPressed() }
        findViewById<View>(R.id.launch_accessibility).setOnClickListener {
            startSubactivity(Intent(ACTION_ACCESSIBILITY_SETTINGS))
        }

        FooterButtonStyleUtils.applyPrimaryButtonPartnerResource(this, startButton, true)

        if (SetupWizardUtils.hasTelephony(this)) {
            setSkipText(R.string.emergency_call)
            emergButton.setOnClickListener {
                startSubactivity(Intent(ACTION_EMERGENCY_DIAL))
            }
            FooterButtonStyleUtils.applySecondaryButtonPartnerResource(this, emergButton, true)
        } else {
            emergButton.setVisibility(View.GONE)
        }

        val welcomeTitle: TextView = findViewById(R.id.welcome_title)
        if (SetupWizardUtils.isManagedProfile(this)) {
            welcomeTitle.text = getString(R.string.setup_managed_profile_welcome_message)
        } else {
            welcomeTitle.text = getString(
                R.string.setup_welcome_message,
                getString(R.string.os_name)
            )
        }

        if (Build.TYPE.equals("eng")) {
            skipButton.visibility = View.VISIBLE
            skipButton.setOnClickListener{
                SetupWizardUtils.finishSetupWizard(this@WelcomeActivity)
            }
        }
    }

    override fun onBackPressed() {
    }

    override fun getLayoutResId(): Int = R.layout.welcome_activity

    override fun getTitleResId(): Int = -1

    companion object {
        private const val ACTION_ACCESSIBILITY_SETTINGS =
            "android.settings.ACCESSIBILITY_SETTINGS_FOR_SUW"
    }
}
