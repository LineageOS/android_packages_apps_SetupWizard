/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.base

import android.graphics.drawable.Drawable
import android.view.View
import com.android.settingslib.Utils
import com.google.android.setupdesign.GlifLayout
import com.google.android.setupdesign.GlifRecyclerLayout
import com.google.android.setupdesign.items.RecyclerItemAdapter
import com.google.android.setupdesign.items.SwitchItem
import com.google.android.setupdesign.template.FloatingBackButtonMixin
import com.google.android.setupdesign.template.IconMixin

class GlifTemplate(private val layout: GlifLayout) : SetupTemplate {

    override val hasFooterBar = true

    private val recyclerLayout
        get() = layout as GlifRecyclerLayout

    override fun setBackButtonVisible(visible: Boolean) {
        layout
            .getMixin(FloatingBackButtonMixin::class.java)
            ?.setVisibility(if (visible) View.VISIBLE else View.GONE)
    }

    override fun setHeaderText(text: CharSequence) {
        layout.setHeaderText(text)
    }

    override fun setDescriptionText(text: CharSequence) {
        layout.setDescriptionText(text)
    }

    override fun setIcon(icon: Drawable) {
        icon.setTintList(Utils.getColorAccent(layout.context))
        layout.setIcon(icon)
        layout.getMixin(IconMixin::class.java)?.setUpscaleIcon(true)
    }

    override var itemAdapter: RecyclerItemAdapter
        get() = recyclerLayout.adapter as RecyclerItemAdapter
        set(value) {
            recyclerLayout.adapter = value
        }

    override fun scrollItemsToTop() {
        recyclerLayout.recyclerView.scrollToPosition(0)
    }

    override fun toggle(id: Int): SetupToggle {
        if (!rowsToggleOnSelect) {
            SwitchItemToggle.selectToToggle(itemAdapter)
            rowsToggleOnSelect = true
        }
        return SwitchItemToggle(itemAdapter.findItemById(id) as SwitchItem)
    }

    override fun setItems(entriesResId: Int) = Unit

    private var rowsToggleOnSelect = false
}
