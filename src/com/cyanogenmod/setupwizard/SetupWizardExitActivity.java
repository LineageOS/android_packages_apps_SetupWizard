package com.cyanogenmod.setupwizard;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import static com.cyanogenmod.setupwizard.SetupWizardApp.LOGV;

import android.annotation.Nullable;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.setupwizardlib.util.SystemBarHelper;

import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

public class SetupWizardExitActivity extends BaseSetupWizardActivity {

    private static final String TAG = SetupWizardExitActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LOGV) {
            Log.v(TAG, "onCreate savedInstanceState=" + savedInstanceState);
        }
        SystemBarHelper.showSystemBars(getWindow(), this);
        SetupWizardUtils.enableStatusBar(this);
        SetupWizardUtils.enableCaptivePortalDetection(this);
        SetupWizardUtils.finishSetupWizard(this);
        launchHome();
        finishAllAppTasks();
        applyForwardTransition(TRANSITION_ID_FADE);
    }

    private void launchHome() {
        startActivity(new Intent("android.intent.action.MAIN")
                .addCategory("android.intent.category.HOME")
                .addFlags(FLAG_ACTIVITY_NEW_TASK));
    }

}
