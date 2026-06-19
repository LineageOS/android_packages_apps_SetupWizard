/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.settings

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.os.UserHandle
import android.view.View
import android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_3BUTTON_OVERLAY
import android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_GESTURAL_OVERLAY
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import com.airbnb.lottie.LottieAnimationView
import lineageos.providers.LineageSettings
import org.lineageos.internal.util.DeviceKeysConstants.KEY_MASK_APP_SWITCH
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.SetupWizardApp
import org.lineageos.setupwizard.SetupWizardApp.DISABLE_NAV_KEYS
import org.lineageos.setupwizard.SetupWizardApp.NAVIGATION_OPTION_KEY
import org.lineageos.setupwizard.base.BaseSetupWizardActivity
import org.lineageos.setupwizard.util.SetupWizardUtils

class NavigationSettingsActivity : BaseSetupWizardActivity() {

    private var mSelection: String = NAV_BAR_MODE_GESTURAL_OVERLAY

    private lateinit var mHideGesturalHint: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var navBarEnabled = false
        if (SetupWizardApp.getSettingsBundle().containsKey(DISABLE_NAV_KEYS)) {
            navBarEnabled = SetupWizardApp.getSettingsBundle().getBoolean(DISABLE_NAV_KEYS)
        }

        val deviceKeys =
            resources.getInteger(
                org.lineageos.platform.internal.R.integer.config_deviceHardwareKeys
            )
        val hasHomeKey = (deviceKeys and KEY_MASK_APP_SWITCH) != 0

        getGlifLayout().setDescriptionText(getString(R.string.navigation_summary))
        setNextText(R.string.next)

        var available = 3
        if (!SetupWizardUtils.isPackageInstalled(this, NAV_BAR_MODE_GESTURAL_OVERLAY)) {
            findViewById<View>(R.id.radio_gesture).visibility = View.GONE
            findViewById<RadioButton>(R.id.radio_sw_keys).isChecked = true
            available--
        }

        if (!SetupWizardUtils.isPackageInstalled(this, NAV_BAR_MODE_3BUTTON_OVERLAY)) {
            findViewById<View>(R.id.radio_sw_keys).visibility = View.GONE
            available--
        }

        if (!navBarEnabled && hasHomeKey || available <= 1) {
            SetupWizardApp.getSettingsBundle()
                .putString(NAVIGATION_OPTION_KEY, NAV_BAR_MODE_3BUTTON_OVERLAY)
            finishAction(RESULT_OK)
        }

        val navigationIllustration = findViewById<LottieAnimationView>(R.id.navigation_illustration)
        val radioGroup = findViewById<RadioGroup>(R.id.navigation_radio_group)
        mHideGesturalHint = findViewById(R.id.hide_navigation_hint)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_gesture -> {
                    mSelection = NAV_BAR_MODE_GESTURAL_OVERLAY
                    navigationIllustration.setAnimation(R.raw.lottie_system_nav_fully_gestural)
                    revealHintCheckbox()
                }

                R.id.radio_sw_keys -> {
                    mSelection = NAV_BAR_MODE_3BUTTON_OVERLAY
                    navigationIllustration.setAnimation(R.raw.lottie_system_nav_3_button)
                    hideHintCheckBox()
                }
            }
            navigationIllustration.playAnimation()
        }
    }

    private fun revealHintCheckbox() {
        mHideGesturalHint.animate().cancel()

        if (mHideGesturalHint.visibility == View.VISIBLE) {
            return
        }

        mHideGesturalHint.visibility = View.VISIBLE
        mHideGesturalHint.alpha = 0.0f
        mHideGesturalHint.animate().translationY(0f).alpha(1.0f).setListener(null)
    }

    private fun hideHintCheckBox() {
        if (mHideGesturalHint.visibility == View.INVISIBLE) {
            return
        }

        mHideGesturalHint
            .animate()
            .translationY(-mHideGesturalHint.height.toFloat())
            .alpha(0.0f)
            .setListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        mHideGesturalHint.visibility = View.INVISIBLE
                    }
                }
            )
    }

    override fun onNextPressed() {
        SetupWizardApp.getSettingsBundle().putString(NAVIGATION_OPTION_KEY, mSelection)
        val hideHint = mHideGesturalHint.isChecked
        LineageSettings.System.putIntForUser(
            contentResolver,
            LineageSettings.System.NAVIGATION_BAR_HINT,
            if (hideHint) 0 else 1,
            UserHandle.USER_CURRENT,
        )
        super.onNextPressed()
    }

    override fun getLayoutResId(): Int = R.layout.setup_navigation

    override fun getTitleResId(): Int = R.string.setup_navigation

    override fun getIconResId(): Int = R.drawable.ic_navigation
}
