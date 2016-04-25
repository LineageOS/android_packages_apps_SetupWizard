#!/bin/bash

adb root
wait ${!}
adb shell pm enable com.google.android.setupwizard || true
wait ${!}
adb shell pm disable com.android.provision || true
wait ${!}
adb shell am start com.cyanogenmod.setupwizard/com.cyanogenmod.setupwizard.SetupWizardTestActivity
wait ${!}
sleep 1
adb shell am start com.google.android.setupwizard/com.google.android.setupwizard.SetupWizardTestActivity
