/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.system

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.google.android.setupdesign.R as SudR
import com.google.android.setupdesign.items.Item
import com.google.zxing.BarcodeFormat
import com.google.zxing.oned.Code128Writer
import org.lineageos.setupwizard.R

class DeviceInfoItem(label: CharSequence, private val value: String) : Item() {

    private var expanded = false

    private val barcode: Bitmap? by lazy { encodeBarcode(value) }

    init {
        title = label
        summary = value
        setLayoutResource(R.layout.device_info_item)
    }

    override fun onBindView(view: View) {
        super.onBindView(view)

        val barcodeView = view.findViewById<ImageView>(R.id.device_info_barcode)
        val expandButton = view.findViewById<ImageView>(R.id.device_info_expand)
        val barcode = barcode

        if (barcode == null) {
            barcodeView.visibility = View.GONE
            expandButton.visibility = View.GONE
            return
        }

        expandButton.visibility = View.VISIBLE
        expandButton.setImageResource(
            if (expanded) {
                SudR.drawable.sud_items_collapse_button_icon
            } else {
                SudR.drawable.sud_items_expand_button_icon
            }
        )
        expandButton.setOnClickListener { toggle() }

        barcodeView.setImageBitmap(barcode)
        barcodeView.visibility = if (expanded) View.VISIBLE else View.GONE
    }

    fun toggle() {
        if (barcode == null) {
            return
        }
        expanded = !expanded
        notifyItemChanged()
    }

    private fun encodeBarcode(value: String): Bitmap? =
        runCatching {
                val matrix = Code128Writer().encode(value, BarcodeFormat.CODE_128, BARCODE_WIDTH, 1)
                val pixels =
                    IntArray(BARCODE_WIDTH) { x ->
                        if (matrix.get(x, 0)) Color.BLACK else Color.WHITE
                    }
                Bitmap.createBitmap(pixels, BARCODE_WIDTH, 1, Bitmap.Config.ARGB_8888)
            }
            .onFailure { Log.w(TAG, "Could not encode $value", it) }
            .getOrNull()

    companion object {
        private const val TAG = "DeviceInfoItem"

        private const val BARCODE_WIDTH = 1024
    }
}
