/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017-2021 The LineageOS Project
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

import android.graphics.Rect;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.android.setupcompat.util.SystemBarHelper;

import org.lineageos.setupwizard.util.EnableAccessibilityController;
import org.lineageos.setupwizard.util.SetupWizardUtils;

import java.util.concurrent.TimeUnit;

public class WelcomeActivity extends BaseSetupWizardActivity
        implements View.OnClickListener {

    public static final String TAG = WelcomeActivity.class.getSimpleName();

    private Button mStartButton;
    private Button mEmergDialerButton;
    private Button mAccessibilityButton;
    private View mRootView;
    private EnableAccessibilityController mEnableAccessibilityController;
    private GestureDetector mGestureDetector;
    private MotionEvent previousTapEvent;
    private int consecutiveTaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRootView = findViewById(R.id.setup_wizard_layout);
        setNextText(R.string.start);
        setSkipText(R.string.emergency_call);
        mStartButton = findViewById(R.id.start);
        mEmergDialerButton = findViewById(R.id.emerg_dialer);
        mAccessibilityButton = findViewById(R.id.launch_accessibility);
        mStartButton.setOnClickListener(this);
        mEmergDialerButton.setOnClickListener(this);
        mAccessibilityButton.setOnClickListener(this);
        mEnableAccessibilityController =
                EnableAccessibilityController.getInstance(getApplicationContext());
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Rect viewRect = new Rect();
                int[] leftTop = new int[2];
                mRootView.getLocationOnScreen(leftTop);
                viewRect.set(
                        leftTop[0], leftTop[1], leftTop[0] + mRootView.getWidth(), leftTop[1]
                                + mRootView.getHeight());
                if (viewRect.contains((int) e.getX(), (int) e.getY())) {
                    if (isConsecutiveTap(e)) {
                        consecutiveTaps++;
                    } else {
                        consecutiveTaps = 1;
                    }
                    if (consecutiveTaps == 4) {
                        SetupWizardUtils.finishSetupWizard(WelcomeActivity.this);
                    }
                } else {
                    // Touch outside the target view. Reset counter.
                    consecutiveTaps = 0;
                }

                if (previousTapEvent != null) {
                    previousTapEvent.recycle();
                }
                previousTapEvent = MotionEvent.obtain(e);
                return false;
            }
        });
        mRootView.setOnTouchListener((v, event) ->
                mEnableAccessibilityController.onTouchEvent(event));
        mRootView.setOnTouchListener((v, event) ->
                mGestureDetector.onTouchEvent(event));
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onClick(View view) {
        if (view == mStartButton) {
            onNextPressed();
        } else if (view == mEmergDialerButton) {
            startEmergencyDialer();
        } else if (view == mAccessibilityButton) {
            startAccessibilitySettings();
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.welcome_activity;
    }

    private boolean isConsecutiveTap(MotionEvent currentTapEvent) {
        if (previousTapEvent == null) {
            return false;
        }

        double deltaX = previousTapEvent.getX() - currentTapEvent.getX();
        double deltaY = previousTapEvent.getY() - currentTapEvent.getY();
        long deltaTime = currentTapEvent.getEventTime() - previousTapEvent.getEventTime();
        return (deltaX * deltaX + deltaY * deltaY >=
                (mRootView.getWidth() * mRootView.getWidth()) / 2
                && deltaTime < TimeUnit.SECONDS.toMillis(1));
    }
}
