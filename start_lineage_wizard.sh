#!/bin/bash

adb root
wait ${!}
adb shell pm enable org.lineageos.setupwizard || true
wait ${!}
adb shell pm enable org.lineageos.setupwizard/.SetupWizardActivity || true
wait ${!}
if adb shell pm list packages | grep com.google.android.setupwizard; then
  adb shell pm disable com.google.android.setupwizard || true
  wait ${!}
fi
if adb shell pm list packages | grep com.android.provision; then
  adb shell pm disable com.android.provision || true
  wait ${!}
fi
adb shell am start -c android.intent.category.HOME org.lineageos.setupwizard/.SetupWizardActivity
