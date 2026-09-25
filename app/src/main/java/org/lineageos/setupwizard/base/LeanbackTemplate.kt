/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.base

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.leanback.widget.VerticalGridView
import com.google.android.setupdesign.items.ItemGroup
import com.google.android.setupdesign.items.ItemInflater
import com.google.android.setupdesign.items.RecyclerItemAdapter
import com.google.android.setupdesign.items.SwitchItem
import org.lineageos.setupwizard.R

class LeanbackTemplate(private val root: View) : SetupTemplate {

    override val hasFooterBar = false

    override fun setBackButtonVisible(visible: Boolean) = Unit

    override fun setHeaderText(text: CharSequence) {
        root.findViewById<TextView>(R.id.setup_header)?.text = text
    }

    override fun setDescriptionText(text: CharSequence) {
        root.findViewById<TextView>(R.id.setup_description)?.text = text
    }

    override fun setIcon(icon: Drawable) {
        root.findViewById<ImageView>(R.id.setup_icon)?.setImageDrawable(icon)
    }

    private val itemsView: VerticalGridView
        get() =
            root.findViewById(R.id.setup_items)
                ?: error("This leanback layout declares no setup_items list")

    override var itemAdapter: RecyclerItemAdapter
        get() = itemsView.adapter as RecyclerItemAdapter
        set(value) {
            itemsView.adapter = value
            itemsView.post { itemsView.requestFocus() }
        }

    override fun scrollItemsToTop() {
        itemsView.selectedPosition = 0
    }

    override fun toggle(id: Int): SetupToggle {
        if (!rowsToggleOnSelect) {
            SwitchItemToggle.selectToToggle(itemAdapter)
            rowsToggleOnSelect = true
        }
        return SwitchItemToggle(itemAdapter.findItemById(id) as SwitchItem)
    }

    override fun setItems(entriesResId: Int) {
        val items = ItemInflater(root.context).inflate(entriesResId) as ItemGroup
        itemAdapter = RecyclerItemAdapter(items)
    }

    private var rowsToggleOnSelect = false
}
