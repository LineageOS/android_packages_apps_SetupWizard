/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.settings

import android.app.UiModeManager
import android.content.res.Configuration
import android.os.Bundle
import com.google.android.material.button.MaterialButtonToggleGroup
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.base.BaseSetupWizardActivity
import org.lineageos.setupwizard.util.updateCheckedIcons

class ThemeSettingsActivity : BaseSetupWizardActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glifLayout.setDescriptionText(getString(R.string.theme_summary))

        val uiModeManager = getSystemService(UiModeManager::class.java)
        val modeGroup = findViewById<MaterialButtonToggleGroup>(R.id.theme_mode_group)
        val isNight =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

        modeGroup.check(if (isNight) R.id.mode_dark else R.id.mode_light)
        modeGroup.updateCheckedIcons(R.drawable.ic_check)

        modeGroup.addOnButtonCheckedListener { _, checkedId, checked ->
            if (!checked) {
                return@addOnButtonCheckedListener
            }
            modeGroup.updateCheckedIcons(R.drawable.ic_check)
            uiModeManager.setNightModeActivated(checkedId == R.id.mode_dark)
        }
    }

    override val layoutResId = R.layout.setup_theme

    override val titleResId = R.string.setup_theme

    override val iconResId = R.drawable.ic_theme
}
