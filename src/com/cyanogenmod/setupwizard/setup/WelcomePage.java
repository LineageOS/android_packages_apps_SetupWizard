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

package com.cyanogenmod.setupwizard.setup;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.ui.LocalePicker;
import com.cyanogenmod.setupwizard.ui.SetupPageFragment;

import java.util.Locale;

public class WelcomePage extends SetupPage {

    public static final String TAG = "WelcomePage";

    public WelcomePage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
    }

    @Override
    public Fragment getFragment() {
        Bundle args = new Bundle();
        args.putString(SetupPage.KEY_PAGE_ARGUMENT, getKey());

        WelcomeFragment fragment = new WelcomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getTitleResId() {
        return R.string.setup_welcome;
    }

    @Override
    public String getKey() {
        return TAG;
    }

    @Override
    public int getPrevButtonTitleResId() {
        return R.string.emergency_call;
    }

    public static class WelcomeFragment extends SetupPageFragment {

        private ArrayAdapter<com.android.internal.app.LocalePicker.LocaleInfo> mLocaleAdapter;
        private Locale mInitialLocale;
        private Locale mCurrentLocale;
        private int[] mAdapterIndices;

        private LocalePicker mLanguagePicker;

        private final Handler mHandler = new Handler();

        private final Runnable mUpdateLocale = new Runnable() {
            public void run() {
                if (mCurrentLocale != null) {
                    com.android.internal.app.LocalePicker.updateLocale(mCurrentLocale);
                }
            }
        };

        @Override
        protected void initializePage() {
            mLanguagePicker = (LocalePicker) mRootView.findViewById(R.id.locale_list);
            loadLanguages();
        }

        private void loadLanguages() {
            mLocaleAdapter = com.android.internal.app.LocalePicker.constructAdapter(getActivity(), R.layout.locale_picker_item, R.id.locale);
            mInitialLocale = Locale.getDefault();
            mCurrentLocale = mInitialLocale;
            mAdapterIndices = new int[mLocaleAdapter.getCount()];
            int currentLocaleIndex = 0;
            String [] labels = new String[mLocaleAdapter.getCount()];
            for (int i=0; i<mAdapterIndices.length; i++) {
                com.android.internal.app.LocalePicker.LocaleInfo localLocaleInfo = mLocaleAdapter.getItem(i);
                Locale localLocale = localLocaleInfo.getLocale();
                if (localLocale.equals(mCurrentLocale)) {
                    currentLocaleIndex = i;
                }
                mAdapterIndices[i] = i;
                labels[i] = localLocaleInfo.getLabel();
            }
            mLanguagePicker.setDisplayedValues(labels);
            mLanguagePicker.setMaxValue(labels.length - 1);
            mLanguagePicker.setValue(currentLocaleIndex);
            mLanguagePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            mLanguagePicker.setOnValueChangedListener(new LocalePicker.OnValueChangeListener() {
                public void onValueChange(LocalePicker picker, int oldVal, int newVal) {
                    setLocaleFromPicker();
                }
            });
        }

        private void setLocaleFromPicker() {
            int i = mAdapterIndices[mLanguagePicker.getValue()];
            final com.android.internal.app.LocalePicker.LocaleInfo localLocaleInfo = mLocaleAdapter.getItem(i);
            onLocaleChanged(localLocaleInfo.getLocale());
        }

        private void onLocaleChanged(Locale paramLocale) {
            Resources localResources = getActivity().getResources();
            Configuration localConfiguration1 = localResources.getConfiguration();
            Configuration localConfiguration2 = new Configuration();
            localConfiguration2.locale = paramLocale;
            localResources.updateConfiguration(localConfiguration2, null);
            localResources.updateConfiguration(localConfiguration1, null);
            mHandler.removeCallbacks(mUpdateLocale);
            mCurrentLocale = paramLocale;
            mHandler.postDelayed(mUpdateLocale, 1000);
        }

        @Override
        protected int getLayoutResource() {
            return R.layout.setup_welcome_page;
        }

        @Override
        protected int getHeaderLayoutResource() {
            return R.layout.logo_header;
        }
    }

}
