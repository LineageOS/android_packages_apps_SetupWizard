/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import com.google.android.setupcompat.template.FooterButtonStyleUtils
import org.lineageos.setupwizard.R

class NavigationLayout(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    /*
     * An interface to listen to events of the navigation bar,
     * namely when the user clicks on the back or next button.
     */
    interface NavigationBarListener {
        fun onNavigateNext()

        fun onSkip()
    }

    val nextButton: Button
    val skipButton: Button

    init {
        View.inflate(context, R.layout.navigation_layout, this)
        nextButton = findViewById(R.id.navbar_next)
        skipButton = findViewById(R.id.navbar_skip)
        FooterButtonStyleUtils.applyPrimaryButtonPartnerResource(context, nextButton, true)
        FooterButtonStyleUtils.applySecondaryButtonPartnerResource(context, skipButton, true)

        val showSkipButton =
            context.theme.obtainStyledAttributes(attrs, R.styleable.NavigationLayout, 0, 0).use { a
                ->
                a.getBoolean(R.styleable.NavigationLayout_showSkipButton, false)
            }

        if (showSkipButton) {
            skipButton.visibility = View.VISIBLE
        }
    }

    fun setNavigationBarListener(listener: NavigationBarListener) {
        skipButton.setOnClickListener { listener.onSkip() }
        nextButton.setOnClickListener { listener.onNavigateNext() }
    }
}
