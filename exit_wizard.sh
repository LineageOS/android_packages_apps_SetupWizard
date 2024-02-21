#!/bin/bash

# SPDX-FileCopyrightText: 2017-2022 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0

adb root
wait ${!}
has_google_suw=$(adb shell pm list packages com.google.android.setupwizard)
adb shell pm enable org.lineageos.setupwizard/.FinishActivity || true
if [[ ! -z "$has_google_suw" ]]
then
    wait ${!}
    adb shell pm enable com.google.android.setupwizard/.FinishActivity || true
    wait ${!}
fi
sleep 1
adb shell am start org.lineageos.setupwizard/.FinishActivity || true
if [[ ! -z "$has_google_suw" ]]
then
    wait ${!}
    sleep 1
    adb shell am start com.google.android.setupwizard/.FinishActivity
fi
