/*
 * Copyright (C) 2013 The CyanogenMod Project
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

import android.app.Activity;
import android.app.AppGlobals;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.SetupWizardApp;
import com.cyanogenmod.setupwizard.setup.AbstractSetupData;
import com.cyanogenmod.setupwizard.setup.CMSetupWizardData;
import com.cyanogenmod.setupwizard.setup.CyanogenServicesPage;
import com.cyanogenmod.setupwizard.setup.Page;
import com.cyanogenmod.setupwizard.setup.SetupDataCallbacks;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;
import com.cyanogenmod.setupwizard.util.WhisperPushUtils;


public class SetupWizardActivity extends Activity implements SetupDataCallbacks {

    private static final String TAG = SetupWizardActivity.class.getSimpleName();

    private View mRootView;
    private View mPageView;
    private Button mNextButton;
    private Button mPrevButton;
    private TextView mTitleView;
    private ViewGroup mHeaderView;

    private AbstractSetupData mSetupData;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_main);
        mRootView = findViewById(R.id.root);
        mPageView = findViewById(R.id.page);
        ((SetupWizardApp)getApplicationContext()).disableStatusBar();
        mSetupData = (AbstractSetupData)getLastNonConfigurationInstance();
        if (mSetupData == null) {
            mSetupData = new CMSetupWizardData(this);
        }
        mHeaderView = (ViewGroup)findViewById(R.id.header);
        mNextButton = (Button) findViewById(R.id.next_button);
        mPrevButton = (Button) findViewById(R.id.prev_button);
        mSetupData.registerListener(this);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSetupData.onNextPage();
            }
        });
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSetupData.onPreviousPage();
            }
        });
        if (savedInstanceState == null) {
            Page page = mSetupData.getCurrentPage();
            page.doLoadAction(this, Page.ACTION_NEXT);
        }
        if (savedInstanceState != null && savedInstanceState.containsKey("data")) {
            mSetupData.load(savedInstanceState.getBundle("data"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onPageTreeChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSetupData.unregisterListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSetupData.getCurrentPage().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mSetupData;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("data", mSetupData.save());
    }

    @Override
    public void onBackPressed() {
        mSetupData.onPreviousPage();
    }

    @Override
    public void onPageViewCreated(LayoutInflater inflater, Bundle savedInstanceState,
            int layoutResource) {
        if (layoutResource != -1) {
            mHeaderView.setVisibility(View.VISIBLE);
            mHeaderView.removeAllViews();
            inflater.inflate(layoutResource, mHeaderView, true);
        } else {
            mHeaderView.setVisibility(View.GONE);
        }
        mTitleView = (TextView) findViewById(android.R.id.title);
        if (mTitleView != null) {
            Page page = mSetupData.getCurrentPage();
            mTitleView.setText(page.getTitleResId());
        }
    }

    @Override
    public void onNextPage() {
        Page page = mSetupData.getCurrentPage();
        page.doLoadAction(this, Page.ACTION_NEXT);
    }

    @Override
    public void onPreviousPage() {
        Page page = mSetupData.getCurrentPage();
        page.doLoadAction(this, Page.ACTION_PREVIOUS);
    }

    @Override
    public void onPageLoaded(Page page) {
        updateButtonBar();
    }

    @Override
    public void onPageTreeChanged() {
        updateButtonBar();
    }

    private void updateButtonBar() {
        Page page = mSetupData.getCurrentPage();
        mNextButton.setText(page.getNextButtonTitleResId());
        if (page.getPrevButtonTitleResId() != -1) {
            mPrevButton.setText(page.getPrevButtonTitleResId());
        } else {
            mPrevButton.setText("");
        }
        if (mSetupData.isFirstPage()) {
            mPrevButton.setCompoundDrawables(null, null, null, null);
        } else {
            mPrevButton.setCompoundDrawablesWithIntrinsicBounds(
                    getDrawable(R.drawable.ic_chevron_left_dark),
                    null, null, null);
        }
        final Resources resources = getResources();
        if (mSetupData.isLastPage()) {
            mPrevButton.setVisibility(View.INVISIBLE);
            mRootView.setBackgroundColor(resources.getColor(R.color.primary));
            mPageView.setBackgroundColor(resources.getColor(R.color.primary));
            mNextButton.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    getDrawable(R.drawable.ic_chevron_right_wht), null);
            mNextButton.setTextColor(resources.getColor(R.color.white));
        } else {
            mPrevButton.setVisibility(View.VISIBLE);
            mPageView.setBackgroundColor(resources.getColor(R.color.page_background));
            if (mSetupData.isFirstPage()) {
                mRootView.setBackgroundColor(resources.getColor(R.color.page_background));
            } else {
                mRootView.setBackgroundColor(resources.getColor(R.color.window_background));
            }
            mNextButton.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    getDrawable(R.drawable.ic_chevron_right_dark), null);
            mNextButton.setTextColor(resources.getColor(R.color.primary_text));
        }
    }

    @Override
    public Page getPage(String key) {
        return mSetupData.getPage(key);
    }

    @Override
    public Page getPage(int key) {
        return mSetupData.getPage(key);
    }

    @Override
    public void onFinish() {
        finishSetup();
    }

    private void handleWhisperPushRegistration() {
        Page page = getPage(CyanogenServicesPage.TAG);
        if (page == null) {
            return;
        }
        Bundle privacyData = page.getData();
        if (privacyData != null && privacyData.getBoolean(CyanogenServicesPage.KEY_REGISTER_WHISPERPUSH)) {
            Log.d(TAG, "Registering with WhisperPush");
            WhisperPushUtils.startRegistration(this);
        }
    }

    public void handleEnableMetrics() {
        Page page = getPage(CyanogenServicesPage.TAG);
        if (page == null) {
            return;
        }
        Bundle privacyData = page.getData();
        if (privacyData != null
                && privacyData.getBoolean(CyanogenServicesPage.KEY_SEND_METRICS)) {
            Settings.System.putInt(getContentResolver(), CyanogenServicesPage.SETTING_METRICS,
                    privacyData.getBoolean(CyanogenServicesPage.KEY_SEND_METRICS) ? 1 : 0);
        }
    }

    private void finishSetup() {
        getApplication().sendBroadcast(new Intent(SetupWizardApp.ACTION_FINISHED));
        handleWhisperPushRegistration();
        handleEnableMetrics();
        Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 1);
        ((SetupWizardApp)AppGlobals.getInitialApplication()).enableStatusBar();
        SetupWizardUtils.disableSetupWizards(this);
        finish();
    }
}
