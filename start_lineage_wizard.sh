#!/bin/bash

adb root
wait ${!}
if adb shell pm list packages | grep com.google.android.setupwizard; then
  adb shell pm disable com.google.android.setupwizard || true
  wait ${!}
fi
if adb shell pm list packages | grep com.android.provision; then
  adb shell pm disable com.android.provision || true
  wait ${!}
fi
adb shell am start org.lineageos.setupwizard/org.lineageos.setupwizard.SetupWizardTestActivity
