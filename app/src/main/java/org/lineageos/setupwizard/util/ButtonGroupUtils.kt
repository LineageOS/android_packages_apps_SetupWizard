/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.util

import androidx.annotation.DrawableRes
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

fun MaterialButtonToggleGroup.updateCheckedIcons(@DrawableRes iconResId: Int) {
    for (index in 0 until childCount) {
        val button = getChildAt(index) as? MaterialButton ?: continue
        if (button.id == checkedButtonId) {
            button.setIconResource(iconResId)
        } else {
            button.icon = null
        }
    }
}
