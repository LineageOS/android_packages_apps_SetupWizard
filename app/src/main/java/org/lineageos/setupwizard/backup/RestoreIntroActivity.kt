/*
 * SPDX-FileCopyrightText: 2019-2020 The Calyx Institute
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.backup

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import com.google.android.setupcompat.util.ResultCodes.RESULT_ACTIVITY_NOT_FOUND
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.SetupWizardApp.Companion.ACTION_RESTORE_FROM_BACKUP
import org.lineageos.setupwizard.base.SubBaseActivity

class RestoreIntroActivity : SubBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glifLayout.setDescriptionText(
            getString(R.string.intro_restore_subtitle, getString(R.string.os_name))
        )
    }

    override fun onSubactivityResult(activityResult: ActivityResult) {
        val resultCode = activityResult.resultCode
        val data = activityResult.data
        when {
            resultCode != RESULT_CANCELED -> nextAction(resultCode, data)
            isSubactivityNotFound -> finishAction(RESULT_ACTIVITY_NOT_FOUND)
            data?.getBooleanExtra("onBackPressed", false) == true -> onStartSubactivity()
        }
    }

    override fun onStartSubactivity() {
        setNextAllowed(true)
    }

    override fun onNextPressed() {
        launchRestore()
    }

    override val layoutResId: Int = R.layout.intro_restore_activity

    override val titleResId: Int = R.string.intro_restore_title

    override val iconResId: Int = R.drawable.ic_restore

    override val installFooterBar: Boolean = true

    override val showSkipButton: Boolean = true

    private fun launchRestore() {
        startSubactivity(Intent(ACTION_RESTORE_FROM_BACKUP))
    }
}
