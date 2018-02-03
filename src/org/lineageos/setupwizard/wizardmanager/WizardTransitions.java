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

import static com.cyanogenmod.setupwizard.SetupWizardApp.LOGV;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;

import com.cyanogenmod.setupwizard.SetupWizardApp;

public class WizardTransitions extends SparseArray<String> implements Parcelable {

    private static final String TAG = "WizardTransitions";

    private String mDefaultAction;

    public static final Creator<WizardTransitions> CREATOR = new Creator<WizardTransitions>() {
        public WizardTransitions createFromParcel(Parcel source) {
            WizardTransitions transitions = new WizardTransitions(source);
            SparseArray<String> actions = source.readSparseArray(null);
            for (int i = 0; i < actions.size(); i++) {
                transitions.put(actions.keyAt(i), actions.valueAt(i));
            }
            return transitions;
        }

        public WizardTransitions[] newArray(int size) {
            return new WizardTransitions[size];
        }
    };

    public WizardTransitions() {}

    public void setDefaultAction(String action) {
        mDefaultAction = action;
    }

    public String getAction(int resultCode) {
        return get(resultCode, mDefaultAction);
    }

    @Override
    public void put(int key, String value) {
        if (LOGV) {
            Log.v(TAG, "put{" +
                    "key='" + key + '\'' +
                    ", value=" + value +
                    '}');
        }
        super.put(key, value);
    }

    public String toString() {
        return super.toString() + " mDefaultAction: " + mDefaultAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WizardTransitions that = (WizardTransitions) o;
        return mDefaultAction != null ? mDefaultAction.equals(that.mDefaultAction) : that.mDefaultAction == null;

    }

    public int hashCode() {
        return  super.hashCode() + mDefaultAction.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDefaultAction);
        int size = size();
        SparseArray sparseArray = new SparseArray<>(size);
        for (int i = 0; i < size; i++) {
            sparseArray.put(keyAt(i), valueAt(i));
        }
        dest.writeSparseArray(sparseArray);
    }

    protected WizardTransitions(Parcel in) {
        mDefaultAction = in.readString();
    }


}
