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
import com.google.android.setupdesign.GlifRecyclerLayout
import com.google.android.setupdesign.items.RecyclerItemAdapter
import com.google.android.setupdesign.items.SwitchItem
import lineageos.hardware.LineageHardwareManager
import lineageos.providers.LineageSettings
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.SetupWizardApp
import org.lineageos.setupwizard.SetupWizardApp.Companion.DISABLE_NAV_KEYS
import org.lineageos.setupwizard.SetupWizardApp.Companion.KEY_SEND_METRICS
import org.lineageos.setupwizard.base.BaseSetupWizardActivity

class LineageSettingsActivity : BaseSetupWizardActivity() {

    private val itemAdapter by lazy {
        (glifLayout as GlifRecyclerLayout).adapter as RecyclerItemAdapter
    }

    private val metricsItem by lazy { itemAdapter.findItemById(R.id.metrics_item) as SwitchItem }
    private val navKeysItem by lazy { itemAdapter.findItemById(R.id.nav_keys_item) as SwitchItem }

    private var supportsKeyDisabler = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNextText(R.string.next)

        val osName = getString(R.string.os_name)
        glifLayout.setDescriptionText(buildDescription(osName))

        val metricsHelpImproveLineage = getString(R.string.services_help_improve_cm, osName)
        metricsItem.summary =
            getString(R.string.services_metrics_label, metricsHelpImproveLineage, osName, osName)
        metricsItem.setOnCheckedChangeListener { _, isChecked ->
            SetupWizardApp.settingsBundle.putBoolean(KEY_SEND_METRICS, isChecked)
        }

        supportsKeyDisabler = isKeyDisablerSupported(this)
        navKeysItem.isVisible = supportsKeyDisabler
        navKeysItem.setOnCheckedChangeListener { _, isChecked ->
            SetupWizardApp.settingsBundle.putBoolean(DISABLE_NAV_KEYS, isChecked)
        }

        itemAdapter.setOnItemSelectedListener { item ->
            if (item is SwitchItem) {
                item.isChecked = !item.isChecked
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateDisableNavkeysOption()
        updateMetricsOption()
    }

    override val layoutResId: Int = R.layout.setup_lineage_settings

    override val titleResId: Int = R.string.setup_services

    override val iconResId: Int = R.drawable.logo

    private fun buildDescription(osName: String): CharSequence {
        val privacyPolicyUri = getString(R.string.services_privacy_policy_uri)
        val policySummary = getString(R.string.services_find_privacy_policy)
        val privacyPolicy = getString(R.string.services_pp_explanation, osName)

        return SpannableStringBuilder(
                getString(R.string.services_full_description, privacyPolicy, policySummary)
            )
            .apply {
                append("\n\n")
                val uriStart = length
                append(privacyPolicyUri)
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    uriStart,
                    length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
    }

    private fun updateMetricsOption() {
        val myPageBundle = SetupWizardApp.settingsBundle
        val metricsChecked =
            !myPageBundle.containsKey(KEY_SEND_METRICS) || myPageBundle.getBoolean(KEY_SEND_METRICS)
        metricsItem.isChecked = metricsChecked
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
            navKeysItem.isChecked = checked
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
