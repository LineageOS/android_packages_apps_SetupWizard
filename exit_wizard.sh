#!/bin/bash

adb root
wait ${!}
adb shell pm enable com.cyanogenmod.setupwizard/com.cyanogenmod.setupwizard.SetupWizardExitActivity || true
wait ${!}
adb shell pm enable com.google.android.setupwizard/com.google.android.setupwizard.SetupWizardExitActivity || true
wait ${!}
sleep 1
adb shell am start com.cyanogenmod.setupwizard/com.cyanogenmod.setupwizard.SetupWizardExitActivity || true
wait ${!}
sleep 1
adb shell am start com.google.android.setupwizard/com.google.android.setupwizard.SetupWizardExitActivity
