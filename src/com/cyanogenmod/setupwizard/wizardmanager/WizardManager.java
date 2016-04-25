/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.setupwizard.wizardmanager;


import static com.cyanogenmod.setupwizard.SetupWizardApp.ACTION_LOAD;
import static com.cyanogenmod.setupwizard.SetupWizardApp.ACTION_NEXT;
import static com.cyanogenmod.setupwizard.SetupWizardApp.EXTRA_ACTION_ID;
import static com.cyanogenmod.setupwizard.SetupWizardApp.EXTRA_FIRST_RUN;
import static com.cyanogenmod.setupwizard.SetupWizardApp.EXTRA_RESULT_CODE;
import static com.cyanogenmod.setupwizard.SetupWizardApp.EXTRA_SCRIPT_URI;
import static com.cyanogenmod.setupwizard.SetupWizardApp.LOGV;

import com.android.setupwizardlib.util.ResultCodes;
import com.android.setupwizardlib.util.WizardManagerHelper;

import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

import android.annotation.Nullable;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;


public class WizardManager extends Activity {

    private static final String TAG = WizardManager.class.getSimpleName();

    private static HashMap<String, WizardScript> sWizardScripts = new HashMap();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LOGV) {
            Log.v(TAG, "onCreate savedInstanceState=" + savedInstanceState);
        }
        Intent intent = this.getIntent();
        if (intent != null) {
            String action = intent.getAction();
            int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
            String scriptUri = intent.getStringExtra(EXTRA_SCRIPT_URI);
            String actionId = intent.getStringExtra(EXTRA_ACTION_ID);
            if (LOGV) {
                Log.v(TAG, "  action=" + action + " resultCode=" + resultCode + " scriptUri="
                        + scriptUri + " actionId=" + actionId + " extras=" + intent.getExtras());
            }

            if (ACTION_LOAD.equals(action)) {
                load(scriptUri, intent);
                finish();
                return;
            }

            if (ACTION_NEXT.equals(action)) {
                next(scriptUri, actionId, resultCode, intent);
                finish();
                return;
            }

            Log.e(TAG, "ERROR: Unknown action");
        } else {
            Log.e(TAG, "ERROR: Intent not available");
        }
        finish();
    }

    private void addExtras(Intent intent) {
        intent.putExtra(WizardManagerHelper.EXTRA_USE_IMMERSIVE_MODE, true);
        intent.putExtra(EXTRA_FIRST_RUN, true);
        intent.putExtra(WizardManagerHelper.EXTRA_THEME, WizardManagerHelper.THEME_MATERIAL_LIGHT);
    }

    private void doAction(String scriptUri, WizardAction action, Intent extras) {
        Intent intent = action.getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if(LOGV) {
            Log.v(TAG, "doAction scriptUri=" + scriptUri + " extras=" + extras
                    + " intent=" + intent + " extras2=" + intent.getExtras() + " action=" + action);
        }

        addExtras(intent);
        if(extras != null) {
            intent.putExtras(extras);
        }

        intent.putExtra(EXTRA_SCRIPT_URI, scriptUri);
        intent.putExtra(EXTRA_ACTION_ID, action.getId());
        startActivity(intent);
    }

    private void load(String scriptUri, Intent extras) {
        WizardScript wizardScript = getWizardScript(this, scriptUri);
        WizardAction wizardAction;
        for(wizardAction = wizardScript.getFirstAction();
            wizardAction != null;
            wizardAction = wizardScript.getNextAction(wizardAction.getId(),
                    ResultCodes.RESULT_ACTIVITY_NOT_FOUND)) {
            if (isActionAvailable(this, wizardAction)) {
                break;
            }

            if(LOGV) {
                Log.v(TAG, "load action not available " + wizardAction);
            }
        }

        if (wizardAction != null) {
            doAction(scriptUri, wizardAction, extras);
        } else {
            Log.e(TAG, "load could not resolve first action scriptUri=" +
                    scriptUri + " actionId=" + wizardScript.getFirstActionId());
            exit(scriptUri);
        }
    }

    private void next(String scriptUri, String actionId, int resultCode, Intent extras) {
        if(LOGV) {
            Log.v(TAG, "next actionId=" + actionId + " resultCode=" + resultCode);
        }
        WizardAction wizardAction = checkNextAction(this, scriptUri,
                actionId, resultCode);
        if (wizardAction != null) {
            doAction(scriptUri, wizardAction, extras);
        }  else {
            exit(scriptUri);
        }
    }

    private void exit(String scriptUri) {
        if(LOGV) {
            Log.v(TAG, "exit scriptUri=" + scriptUri);
        }
        WizardManager.sWizardScripts.remove(scriptUri);
        SetupWizardUtils.disableComponent(this, WizardManager.class);
    }

    private static WizardAction checkNextAction(Context context, String scriptUri, String actionId,
            int resultCode) {
        if(LOGV) {
            Log.v(TAG, "checkNextAction scriptUri=" + scriptUri + " actionId="
                    + actionId + " resultCode=" + resultCode);
        }

        WizardScript wizardScript = getWizardScript(context, scriptUri);
        WizardAction wizardAction;
        for ( wizardAction = wizardScript.getNextAction(actionId, resultCode);
             wizardAction != null;
             wizardAction = wizardScript.getNextAction(wizardAction.getId(),
                     ResultCodes.RESULT_ACTIVITY_NOT_FOUND)) {
            if (WizardManager.isActionAvailable(context, wizardAction)) {
                break;
            }

            if(LOGV) {
                Log.v(TAG, "checkNextAction action not available " + wizardAction);
            }
        }

        if (LOGV) {
            Log.v(TAG, "checkNextAction action=" + wizardAction);
        }

        return wizardAction;
    }

    private static boolean isActionAvailable(Context context, WizardAction action) {
        return isIntentAvailable(context, action.getIntent());
    }

    private static boolean isIntentAvailable(Context context, Intent intent) {
        if (context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
            return true;
        }
        return false;
    }

    private static WizardScript getWizardScript(Context context, String scriptUri) {
        WizardScript wizardScript = sWizardScripts.get(scriptUri);
        if (wizardScript == null) {
            wizardScript = WizardScript.loadFromUri(context, scriptUri);
            sWizardScripts.put(scriptUri, wizardScript);
        }
        return wizardScript;
    }

}
