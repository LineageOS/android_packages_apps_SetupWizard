/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.settings

import android.app.UiModeManager
import android.content.res.Configuration
import android.os.Bundle
import android.widget.RadioGroup
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.base.BaseSetupWizardActivity

class ThemeSettingsActivity : BaseSetupWizardActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getGlifLayout().setDescriptionText(getString(R.string.theme_summary))

        val uiModeManager = getSystemService(UiModeManager::class.java)
        val radioGroup: RadioGroup = findViewById(R.id.theme_radio_group)
        val isNight =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

        radioGroup.check(if (isNight) R.id.radio_dark else R.id.radio_light)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_dark -> uiModeManager.setNightModeActivated(true)
                R.id.radio_light -> uiModeManager.setNightModeActivated(false)
            }
        }
    }

    override fun getLayoutResId(): Int = R.layout.setup_theme

    override fun getTitleResId(): Int = R.string.setup_theme

    override fun getIconResId(): Int = R.drawable.ic_theme
}
