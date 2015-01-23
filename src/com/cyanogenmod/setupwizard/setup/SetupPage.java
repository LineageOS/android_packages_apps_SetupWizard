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

import com.cyanogenmod.setupwizard.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;


public abstract class SetupPage implements Page {

    private final SetupDataCallbacks mCallbacks;

    private Bundle mData = new Bundle();
    private boolean mRequired = false;
    private boolean mCompleted = false;

    protected final Context mContext;

    protected SetupPage(Context context, SetupDataCallbacks callbacks) {
        mContext = context;
        mCallbacks = callbacks;
    }

    @Override
    public Fragment getFragment(FragmentManager fragmentManager, int action) {
        return null;
    }

    @Override
    public int getPrevButtonTitleResId() {
        return -1;
    }

    @Override
    public int getNextButtonTitleResId() {
        return R.string.next;
    }

    @Override
    public boolean doNextAction() {
        return false;
    }

    @Override
    public boolean doPreviousAction() {
        return false;
    }

    @Override
    public void doLoadAction(Activity context, int action) {
        if (context == null || context.isFinishing()) { return; }
        final FragmentManager fragmentManager = context.getFragmentManager();
        Fragment fragment = getFragment(fragmentManager, action);
        if (action == Page.ACTION_NEXT) {
            Transition t = new Slide(Gravity.RIGHT);
            fragment.setEnterTransition(t);
            fragmentManager.beginTransaction()
                    .replace(R.id.content,fragment, getKey())
                    .commit();
        } else {
            Transition t = new Slide(Gravity.LEFT);
            fragment.setEnterTransition(t);
            fragmentManager.beginTransaction()
                    .replace(R.id.content, fragment, getKey())
                    .commit();
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }

    @Override
    public boolean isRequired() {
        return mRequired;
    }

    @Override
    public Page setRequired(boolean required) {
        mRequired = required;
        return this;
    }

    @Override
    public boolean isCompleted() {
        return mCompleted;
    }

    @Override
    public void setCompleted(boolean completed) {
        mCompleted = completed;
        mCallbacks.onNextPage();
    }

    @Override
    public Bundle getData() {
        return mData;
    }

    @Override
    public void resetData(Bundle data) {
        mData = data;
        mCallbacks.onPageLoaded(this);
    }

    public SetupDataCallbacks getCallbacks() {
        return mCallbacks;
    }
}
