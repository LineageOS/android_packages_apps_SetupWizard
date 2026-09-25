/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.base

import com.google.android.setupdesign.items.RecyclerItemAdapter
import com.google.android.setupdesign.items.SwitchItem

internal class SwitchItemToggle(private val item: SwitchItem) : SetupToggle {

    override var isChecked: Boolean
        get() = item.isChecked
        set(value) {
            item.isChecked = value
        }

    override var isVisible: Boolean
        get() = item.isVisible
        set(value) {
            item.isVisible = value
        }

    override var summary: CharSequence?
        get() = item.summary
        set(value) {
            item.summary = value
        }

    override fun setOnCheckedChangeListener(listener: (isChecked: Boolean) -> Unit) {
        item.setOnCheckedChangeListener { _, isChecked -> listener(isChecked) }
    }

    internal companion object {
        fun selectToToggle(adapter: RecyclerItemAdapter) {
            adapter.setOnItemSelectedListener { item ->
                (item as? SwitchItem)?.let { it.isChecked = !it.isChecked }
            }
        }
    }
}
