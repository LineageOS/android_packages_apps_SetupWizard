#!/bin/bash

adb root
wait ${!}
adb shell pm disable com.google.android.setupwizard || true
wait ${!}
adb shell pm disable com.android.provision || true
wait ${!}
adb shell am start org.lineageos.setupwizard/org.lineageos.setupwizard.SetupWizardTestActivity
