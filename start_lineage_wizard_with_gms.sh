#!/bin/bash

# SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0

adb root
wait ${!}
adb shell pm enable org.lineageos.setupwizard || true
wait ${!}
adb shell pm enable org.lineageos.setupwizard/.SetupWizardActivity || true
wait ${!}
adb shell pm enable com.google.android.setupwizard || true
wait ${!}
if adb shell pm list packages | grep com.android.provision; then
  adb shell pm disable com.android.provision || true
  wait ${!}
fi
sleep 1
adb shell am start com.google.android.setupwizard/.SetupWizardTestActivity
