/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.setupwizard.wizardmanager;

import android.util.SparseArray;

public class WizardTransitions extends SparseArray<String> {

    private String mDefaultAction;

    public void setDefaultAction(String action) {
        mDefaultAction = action;
    }

    public String getAction(int resultCode) {
        return this.get(resultCode, this.mDefaultAction);
    }

    public String toString() {
        return super.toString() + " mDefaultAction: " + this.mDefaultAction;
    }


}
