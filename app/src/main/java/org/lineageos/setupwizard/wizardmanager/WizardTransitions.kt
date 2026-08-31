/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.wizardmanager

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.util.SparseArray
import org.lineageos.setupwizard.SetupWizardApp.Companion.LOGV

class WizardTransitions() : SparseArray<String?>(), Parcelable {

    private var defaultAction: String? = null

    fun setDefaultAction(action: String?) {
        defaultAction = action
    }

    fun getAction(resultCode: Int): String? = get(resultCode, defaultAction)

    override fun put(key: Int, value: String?) {
        if (LOGV) {
            Log.v(TAG, "put{key='$key', value=$value}")
        }
        super.put(key, value)
    }

    override fun toString(): String = "${super.toString()} defaultAction: $defaultAction"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        val that = other as WizardTransitions
        return defaultAction == that.defaultAction
    }

    override fun hashCode(): Int {
        return super.hashCode() + (defaultAction?.hashCode() ?: 0)
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(defaultAction)
        val n = size()
        val sparse = SparseArray<String>(n)
        for (i in 0 until n) {
            sparse.put(keyAt(i), valueAt(i))
        }
        dest.writeSparseArray(sparse)
    }

    private constructor(source: Parcel) : this() {
        defaultAction = source.readString()
        val actions: SparseArray<String>? = source.readSparseArray(null, String::class.java)
        if (actions != null) {
            for (i in 0 until actions.size()) {
                put(actions.keyAt(i), actions.valueAt(i))
            }
        }
    }

    companion object {
        private const val TAG = "WizardTransitions"

        @JvmField
        val CREATOR: Parcelable.Creator<WizardTransitions> =
            object : Parcelable.Creator<WizardTransitions> {
                override fun createFromParcel(source: Parcel): WizardTransitions =
                    WizardTransitions(source)

                override fun newArray(size: Int): Array<WizardTransitions?> = arrayOfNulls(size)
            }
    }
}
