#!/bin/bash

adb root
wait ${!}
adb shell pm enable org.lineageos.setupwizard/org.lineageos.setupwizard.SetupWizardExitActivity || true
wait ${!}
adb shell pm enable com.google.android.setupwizard/com.google.android.setupwizard.SetupWizardExitActivity || true
wait ${!}
sleep 1
adb shell am start org.lineageos.setupwizard/org.lineageos.setupwizard.SetupWizardExitActivity || true
wait ${!}
sleep 1
adb shell am start com.google.android.setupwizard/com.google.android.setupwizard.SetupWizardExitActivity
