/*
 * Copyright (C) 2021 The LineageOS Project
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

package org.lineageos.setupwizard;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class NavigationLayout extends RelativeLayout {
    /*
     * An interface to listen to events of the navigation bar,
     * namely when the user clicks on the back or next button.
     */
    public interface NavigationBarListener {
        View.OnClickListener onNavigateBack();
        View.OnClickListener onNavigateNext();
        View.OnClickListener onSkip();
    }

    private Button mNextButton;
    private Button mSkipButton;

    public NavigationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(getContext(), R.layout.navigation_layout, this);
        mNextButton = (Button) findViewById(R.id.navbar_next);
        mSkipButton = (Button) findViewById(R.id.navbar_skip);

        TypedArray a = context.getTheme().obtainStyledAttributes(
             attrs, R.styleable.NavigationLayout, 0, 0);
        final boolean showSkipButton;
        try {
            showSkipButton = a.getBoolean(
                    R.styleable.NavigationLayout_showSkipButton, false);
        } finally {
            a.recycle();
        }

        if (showSkipButton) {
            mSkipButton.setVisibility(View.VISIBLE);
        }
    }

    public Button getSkipButton() {
        return mSkipButton;
    }

    public Button getNextButton() {
        return mNextButton;
    }

    public void setNavigationBarListener(NavigationBarListener listener) {
        getSkipButton().setOnClickListener(listener.onSkip());
        getNextButton().setOnClickListener(listener.onNavigateNext());
    }
}
