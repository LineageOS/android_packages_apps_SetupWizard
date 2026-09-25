/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.base

interface SetupToggle {

    var isChecked: Boolean

    var isVisible: Boolean

    var summary: CharSequence?

    fun setOnCheckedChangeListener(listener: (isChecked: Boolean) -> Unit)
}
