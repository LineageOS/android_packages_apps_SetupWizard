/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.settings

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.UserHandle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import lineageos.hardware.LineageHardwareManager
import lineageos.providers.LineageSettings
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.SetupWizardApp
import org.lineageos.setupwizard.SetupWizardApp.Companion.DISABLE_NAV_KEYS
import org.lineageos.setupwizard.SetupWizardApp.Companion.KEY_SEND_METRICS
import org.lineageos.setupwizard.base.BaseSetupWizardActivity

class LineageSettingsActivity : BaseSetupWizardActivity() {

    private lateinit var metrics: CheckBox
    private lateinit var navKeys: CheckBox

    private var supportsKeyDisabler = false

    private val metricsClickListener =
        View.OnClickListener {
            val checked = !metrics.isChecked
            metrics.isChecked = checked
            SetupWizardApp.settingsBundle.putBoolean(KEY_SEND_METRICS, checked)
        }

    private val navKeysClickListener =
        View.OnClickListener {
            val checked = !navKeys.isChecked
            navKeys.isChecked = checked
            SetupWizardApp.settingsBundle.putBoolean(DISABLE_NAV_KEYS, checked)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNextText(R.string.next)

        val osName = getString(R.string.os_name)
        val privacyPolicy = getString(R.string.services_pp_explanation, osName)
        val privacyPolicyUri = getString(R.string.services_privacy_policy_uri)
        val policySummary = getString(R.string.services_find_privacy_policy, privacyPolicyUri)
        val servicesFullDescription =
            getString(R.string.services_full_description, privacyPolicy, policySummary)
        glifLayout.setDescriptionText(servicesFullDescription)

        val metricsRow = findViewById<View>(R.id.metrics)
        metricsRow.setOnClickListener(metricsClickListener)
        metricsRow.requestFocus()
        val metricsHelpImproveLineage = getString(R.string.services_help_improve_cm, osName)
        val metricsSummary =
            getString(R.string.services_metrics_label, metricsHelpImproveLineage, osName, osName)
        val metricsSpan = SpannableStringBuilder(metricsSummary)
        metricsSpan.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            metricsHelpImproveLineage.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        findViewById<TextView>(R.id.enable_metrics_summary).text = metricsSpan
        metrics = findViewById(R.id.enable_metrics_checkbox)

        val navKeysRow = findViewById<View>(R.id.nav_keys)
        navKeysRow.setOnClickListener(navKeysClickListener)
        navKeys = findViewById(R.id.nav_keys_checkbox)
        supportsKeyDisabler = isKeyDisablerSupported(this)
        if (supportsKeyDisabler) {
            navKeys.isChecked =
                LineageSettings.System.getIntForUser(
                    contentResolver,
                    LineageSettings.System.FORCE_SHOW_NAVBAR,
                    0,
                    UserHandle.USER_CURRENT,
                ) != 0
        } else {
            navKeysRow.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        updateDisableNavkeysOption()
        updateMetricsOption()
    }

    override val layoutResId: Int = R.layout.setup_lineage_settings

    override val titleResId: Int = R.string.setup_services

    override val iconResId: Int = R.drawable.ic_features

    private fun updateMetricsOption() {
        val myPageBundle = SetupWizardApp.settingsBundle
        val metricsChecked =
            !myPageBundle.containsKey(KEY_SEND_METRICS) || myPageBundle.getBoolean(KEY_SEND_METRICS)
        metrics.isChecked = metricsChecked
        myPageBundle.putBoolean(KEY_SEND_METRICS, metricsChecked)
    }

    private fun updateDisableNavkeysOption() {
        if (supportsKeyDisabler) {
            val myPageBundle = SetupWizardApp.settingsBundle
            val enabled =
                LineageSettings.System.getIntForUser(
                    contentResolver,
                    LineageSettings.System.FORCE_SHOW_NAVBAR,
                    0,
                    UserHandle.USER_CURRENT,
                ) != 0
            val checked =
                if (myPageBundle.containsKey(DISABLE_NAV_KEYS)) {
                    myPageBundle.getBoolean(DISABLE_NAV_KEYS)
                } else {
                    enabled
                }
            navKeys.isChecked = checked
            myPageBundle.putBoolean(DISABLE_NAV_KEYS, checked)
        }
    }

    companion object {
        private fun isKeyDisablerSupported(context: Context): Boolean {
            val hardware = LineageHardwareManager.getInstance(context)
            return hardware.isSupported(LineageHardwareManager.FEATURE_KEY_DISABLE)
        }
    }
}
