/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.base

import android.graphics.drawable.Drawable
import com.google.android.setupdesign.items.RecyclerItemAdapter

interface SetupTemplate {

    val hasFooterBar: Boolean

    fun setBackButtonVisible(visible: Boolean)

    fun setHeaderText(text: CharSequence)

    fun setDescriptionText(text: CharSequence)

    fun setIcon(icon: Drawable)

    var itemAdapter: RecyclerItemAdapter

    fun scrollItemsToTop()

    fun toggle(id: Int): SetupToggle

    fun setItems(entriesResId: Int)
}
