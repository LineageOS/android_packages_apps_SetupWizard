/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.locale

import android.os.Bundle
import android.widget.TextView
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.util.SetupWizardUtils

class WelcomeLocaleActivity : LocaleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<TextView>(R.id.welcome_title).text =
            if (SetupWizardUtils.isManagedProfile(this)) {
                getString(R.string.setup_managed_profile_welcome_message)
            } else {
                getString(R.string.setup_welcome_message, getString(R.string.os_name))
            }
    }

    override val layoutResId = R.layout.welcome_locale_activity

    override val titleResId = -1

    override val iconResId = -1

    override val localeItemLayoutResId = R.layout.tv_list_item

    override val separatesSuggestedLocales = false
}
