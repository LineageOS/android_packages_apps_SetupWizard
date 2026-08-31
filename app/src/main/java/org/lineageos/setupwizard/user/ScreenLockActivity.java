/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.user;

import android.content.Intent;

import org.lineageos.setupwizard.base.SubBaseActivity;

public class ScreenLockActivity extends SubBaseActivity {

    private static final String ACTION_SETUP_LOCKSCREEN = "com.android.settings.SETUP_LOCK_SCREEN";

    @Override
    protected void onStartSubactivity() {
        Intent intent = new Intent(ACTION_SETUP_LOCKSCREEN);
        startSubactivity(intent);
    }
}
