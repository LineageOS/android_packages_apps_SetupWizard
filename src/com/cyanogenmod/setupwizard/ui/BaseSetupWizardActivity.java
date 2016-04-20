/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017 The LineageOS Project
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

package com.cyanogenmod.setupwizard.ui;

import android.annotation.Nullable;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.android.setupwizard.navigationbar.SetupWizardNavBar;
import com.android.setupwizard.navigationbar.SetupWizardNavBar.NavigationBarListener;
import com.android.setupwizardlib.util.SystemBarHelper;

public abstract class BaseSetupWizardActivity extends Activity implements NavigationBarListener {

    /* Logging */
    public static final boolean LOGV = true;

    public static final String TAG = BaseSetupWizardActivity.class.getSimpleName();

    private SetupWizardNavBar mNavigationBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemBarHelper.hideSystemBars(getWindow());
    }

    @Override
    public void onNavigationBarCreated(SetupWizardNavBar bar) {
        mNavigationBar = bar;
        bar.setUseImmersiveMode(true);
        bar.getView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                view.requestApplyInsets();
            }
        });
    }

    protected void setBackDrawable(Drawable drawable) {
        if (mNavigationBar != null) {
            mNavigationBar.getBackButton().setCompoundDrawables(drawable, null, null, null);
        }
    }

    protected void setNextDrawable(Drawable drawable) {
        if (mNavigationBar != null) {
            mNavigationBar.getBackButton().setCompoundDrawables(null, null, drawable, null);
        }
    }

    protected void setBackAllowed(boolean allowed) {
        // Enable / disable navigation bar back button
        if (mNavigationBar != null) {
            mNavigationBar.getBackButton().setEnabled(allowed);
        }
    }

    protected void setNextAllowed(boolean allowed) {
        if (mNavigationBar != null) {
            mNavigationBar.getNextButton().setEnabled(allowed);
        }
    }

    protected void setNextText(int resId) {
        if (mNavigationBar != null) {
            mNavigationBar.getNextButton().setText(resId);
        }
    }

    protected void setBackText(int resId) {
        if (mNavigationBar != null) {
            mNavigationBar.getBackButton().setText(resId);
        }
    }

    protected void hideNextButton() {
        if (mNavigationBar != null) {
            Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
            final Button next = mNavigationBar.getNextButton();
            next.startAnimation(fadeOut);
            next.setVisibility(View.INVISIBLE);
        }
    }

    protected void hideBackButton() {
        if (mNavigationBar != null) {
            Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
            final Button back = mNavigationBar.getBackButton();
            back.startAnimation(fadeOut);
            back.setVisibility(View.INVISIBLE);
        }
    }


}
