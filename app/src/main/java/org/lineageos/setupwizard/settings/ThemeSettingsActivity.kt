/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.settings

import android.app.UiModeManager
import android.content.res.Configuration
import android.graphics.Outline
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.materialswitch.MaterialSwitch
import lineageos.providers.LineageSettings
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.base.BaseSetupWizardActivity
import org.lineageos.setupwizard.util.updateCheckedIcons

class ThemeSettingsActivity : BaseSetupWizardActivity() {

    private lateinit var blackThemeCard: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glifLayout.setDescriptionText(getString(R.string.theme_summary))

        val uiModeManager = getSystemService(UiModeManager::class.java)
        val modeGroup = findViewById<MaterialButtonToggleGroup>(R.id.theme_mode_group)
        val blackTheme = findViewById<MaterialSwitch>(R.id.black_theme)
        blackThemeCard = findViewById(R.id.black_theme_card)
        roundPreviewWallpaper(findViewById(R.id.theme_preview_wallpaper))
        val isNight =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

        blackTheme.isChecked =
            LineageSettings.Secure.getInt(
                contentResolver,
                LineageSettings.Secure.BERRY_BLACK_THEME,
                0,
            ) != 0
        blackTheme.setOnCheckedChangeListener { _, checked ->
            LineageSettings.Secure.putInt(
                contentResolver,
                LineageSettings.Secure.BERRY_BLACK_THEME,
                if (checked) 1 else 0,
            )
        }

        modeGroup.check(if (isNight) R.id.mode_dark else R.id.mode_light)
        modeGroup.updateCheckedIcons(R.drawable.ic_check)

        modeGroup.addOnButtonCheckedListener { _, checkedId, checked ->
            if (!checked) {
                return@addOnButtonCheckedListener
            }
            val night = checkedId == R.id.mode_dark
            modeGroup.updateCheckedIcons(R.drawable.ic_check)
            setBlackThemeRevealed(night)
            uiModeManager.setNightModeActivated(night)
        }

        setBlackThemeRevealed(isNight)
    }

    private fun roundPreviewWallpaper(wallpaper: ImageView) {
        wallpaper.outlineProvider =
            object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    val radius = view.width * PREVIEW_CORNER_RADIUS / PREVIEW_WIDTH
                    outline.setRoundRect(0, 0, view.width, view.height + radius.toInt(), radius)
                }
            }
        wallpaper.clipToOutline = true
    }

    private fun setBlackThemeRevealed(revealed: Boolean) {
        blackThemeCard.visibility = if (revealed) View.VISIBLE else View.GONE
    }

    override val layoutResId = R.layout.setup_theme

    override val titleResId = R.string.setup_theme

    override val iconResId = R.drawable.ic_palette

    companion object {
        // Both taken from the preview artwork's viewport, see theme_preview_qs.xml.
        private const val PREVIEW_WIDTH = 412f
        private const val PREVIEW_CORNER_RADIUS = 28f
    }
}
