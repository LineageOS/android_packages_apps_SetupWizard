/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard;

import android.content.Intent;
import android.provider.Settings;

public class BiometricActivity extends SubBaseActivity {

    @Override
    protected void onStartSubactivity() {
        Intent intent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
        startSubactivity(intent);
    }
}
