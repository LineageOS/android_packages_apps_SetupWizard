/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.wizardmanager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import com.google.android.setupcompat.util.ResultCodes
import com.google.android.setupcompat.util.WizardManagerHelper.ACTION_NEXT
import org.lineageos.setupwizard.SetupWizardApp.Companion.ACTION_LOAD
import org.lineageos.setupwizard.SetupWizardApp.Companion.EXTRA_ACTION_ID
import org.lineageos.setupwizard.SetupWizardApp.Companion.EXTRA_RESULT_CODE
import org.lineageos.setupwizard.SetupWizardApp.Companion.EXTRA_SCRIPT_URI
import org.lineageos.setupwizard.SetupWizardApp.Companion.EXTRA_WIZARD_BUNDLE
import org.lineageos.setupwizard.SetupWizardApp.Companion.LOGV
import org.lineageos.setupwizard.util.SetupWizardUtils

class WizardManager : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (LOGV) {
            Log.v(TAG, "onCreate savedInstanceState=$savedInstanceState")
        }

        val intent = intent
        if (intent == null) {
            Log.e(TAG, "ERROR: Intent not available")
            finish()
            return
        }

        val action = intent.action
        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
        val wizardBundle = intent.getBundleExtra(EXTRA_WIZARD_BUNDLE)

        val scriptUri = wizardBundle?.getString(EXTRA_SCRIPT_URI)
        val actionId = wizardBundle?.getString(EXTRA_ACTION_ID)

        if (LOGV) {
            Log.v(
                TAG,
                "  action=$action resultCode=$resultCode scriptUri=$scriptUri actionId=$actionId extras=${intent.extras}",
            )
        }

        when (action) {
            ACTION_LOAD -> {
                if (scriptUri == null) {
                    Log.e(TAG, "ERROR: Missing scriptUri for ACTION_LOAD")
                } else {
                    load(scriptUri, intent)
                }
                finish()
                return
            }
            ACTION_NEXT -> {
                if (scriptUri == null || actionId == null) {
                    Log.e(TAG, "ERROR: Missing scriptUri/actionId for ACTION_NEXT")
                } else {
                    next(scriptUri, actionId, resultCode, intent)
                }
                finish()
                return
            }
            else -> {
                Log.e(TAG, "ERROR: Unknown action")
                finish()
                return
            }
        }
    }

    private fun doAction(scriptUri: String, action: WizardAction, extras: Intent?) {
        val intent = action.getIntent()?.apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }

        if (intent == null) {
            Log.e(TAG, "doAction: could not build Intent from $action")
            exit(scriptUri)
            return
        }

        if (LOGV) {
            Log.v(
                TAG,
                "doAction scriptUri=$scriptUri extras=$extras intent=$intent extras2=${intent.extras} action=$action",
            )
        }

        if (extras != null) {
            intent.putExtras(extras)
        }

        val wizardBundle =
            Bundle().apply {
                putString(EXTRA_SCRIPT_URI, scriptUri)
                putString(EXTRA_ACTION_ID, action.id)
            }
        intent.putExtra(EXTRA_WIZARD_BUNDLE, wizardBundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
        startActivity(intent)
    }

    private fun load(scriptUri: String, extras: Intent?) {
        val wizardScript = getWizardScript(this, scriptUri)
        var wizardAction: WizardAction? = wizardScript.firstAction

        while (wizardAction != null) {
            if (isActionAvailable(this, wizardAction)) {
                break
            }

            if (LOGV) Log.v(TAG, "load action not available $wizardAction")

            wizardAction =
                wizardScript.getNextAction(wizardAction.id, ResultCodes.RESULT_ACTIVITY_NOT_FOUND)
        }

        if (wizardAction != null) {
            doAction(scriptUri, wizardAction, extras)
        } else {
            Log.e(
                TAG,
                "load could not resolve first action scriptUri=$scriptUri actionId=${wizardScript.firstActionId}",
            )
            exit(scriptUri)
        }
    }

    private fun next(scriptUri: String, actionId: String, resultCode: Int, extras: Intent?) {
        if (LOGV) {
            Log.v(TAG, "next actionId=$actionId resultCode=$resultCode")
        }
        val wizardAction = checkNextAction(this, scriptUri, actionId, resultCode)
        if (wizardAction != null) {
            doAction(scriptUri, wizardAction, extras)
        } else {
            exit(scriptUri)
        }
    }

    private fun exit(scriptUri: String) {
        if (LOGV) Log.v(TAG, "exit scriptUri=$scriptUri")
        wizardScripts.remove(scriptUri)
        SetupWizardUtils.disableComponent(this, WizardManager::class.java)
    }

    companion object {
        private const val TAG = "WizardManager"

        private val wizardScripts = mutableMapOf<String, WizardScript>()

        private fun checkNextAction(
            context: Context,
            scriptUri: String,
            actionId: String,
            resultCode: Int,
        ): WizardAction? {
            if (LOGV) {
                Log.v(
                    TAG,
                    "checkNextAction scriptUri=$scriptUri actionId=$actionId resultCode=$resultCode",
                )
            }

            val wizardScript = getWizardScript(context, scriptUri)
            var wizardAction: WizardAction? = wizardScript.getNextAction(actionId, resultCode)

            while (wizardAction != null) {
                if (isActionAvailable(context, wizardAction)) {
                    break
                }
                if (LOGV) Log.v(TAG, "checkNextAction action not available $wizardAction")

                wizardAction =
                    wizardScript.getNextAction(
                        wizardAction.id,
                        ResultCodes.RESULT_ACTIVITY_NOT_FOUND,
                    )
            }

            if (LOGV) Log.v(TAG, "checkNextAction action=$wizardAction")
            return wizardAction
        }

        private fun isActionAvailable(context: Context, action: WizardAction): Boolean =
            isIntentAvailable(context, action.getIntent())

        private fun isIntentAvailable(context: Context, intent: Intent?): Boolean {
            if (intent == null) return false
            return context.packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .isNotEmpty()
        }

        private fun getWizardScript(context: Context, scriptUri: String): WizardScript =
            wizardScripts.getOrPut(scriptUri) {
                WizardScript.loadFromUri(context, scriptUri)
                    ?: run {
                        Log.e(TAG, "Unable to load WizardScript: $scriptUri")
                        throw IllegalStateException("Unable to load WizardScript: $scriptUri")
                    }
            }
    }
}
