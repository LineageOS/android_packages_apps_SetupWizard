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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.cyanogenmod.setupwizard.R;
import com.cyanogenmod.setupwizard.ui.SetupPageFragment;
import com.cyanogenmod.setupwizard.util.SetupWizardUtils;

public class FinishPage extends SetupPage {

    public static final String TAG = "FinishPage";

    private FinishFragment mFinishFragment;
    private boolean mBluetoothSetupFinished;
    private boolean mUserCalledFinish;

    private BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.STATE_OFF);
            if (state == BluetoothAdapter.STATE_ON) {
                final BluetoothManager bt = mContext.getSystemService(BluetoothManager.class);
                if (bt != null && bt.getAdapter() != null) {
                    bt.getAdapter().disable();
                    return;
                }
            }
            mBluetoothSetupFinished = state == BluetoothAdapter.STATE_OFF;
            if (mFinishFragment != null && mFinishFragment.enabledBlueooth()
                    && mBluetoothSetupFinished && mUserCalledFinish) {
                getCallbacks().onFinish();
            }
        }
    };

    public FinishPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
        final BluetoothManager bt = context.getSystemService(BluetoothManager.class);
        if (bt != null && bt.getAdapter() != null && !bt.getAdapter().isEnabled()) {
            mBluetoothSetupFinished = false;
            context.registerReceiver(mBluetoothStateReceiver,
                    new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        } else {
            mBluetoothSetupFinished = true;
            mBluetoothStateReceiver = null;
        }
    }

    @Override
    public void onFinishSetup() {
        super.onFinishSetup();
        if (mBluetoothStateReceiver != null) {
            mContext.unregisterReceiver(mBluetoothStateReceiver);
            mBluetoothStateReceiver = null;
        }
    }

    @Override
    public Fragment getFragment(FragmentManager fragmentManager, int action) {
        mFinishFragment = (FinishFragment) fragmentManager.findFragmentByTag(getKey());
        if (mFinishFragment == null) {
            Bundle args = new Bundle();
            args.putString(Page.KEY_PAGE_ARGUMENT, getKey());
            args.putInt(Page.KEY_PAGE_ACTION, action);
            mFinishFragment = new FinishFragment();
            mFinishFragment.setArguments(args);
        }
        return mFinishFragment;
    }

    @Override
    public String getKey() {
        return TAG;
    }

    @Override
    public int getTitleResId() {
        return R.string.setup_complete;
    }

    @Override
    public boolean doNextAction() {
        if (mUserCalledFinish) {
            return true;
        }
        mUserCalledFinish = true;
        if (mBluetoothStateReceiver == null
                || mFinishFragment == null || !mFinishFragment.enabledBlueooth()
                || mBluetoothSetupFinished) {
            // if we don't care about BT setup finish
            // if fragment isn't up finish
            // if BT setup finished before user hit next, finish
            getCallbacks().onFinish();
        }
        return true;
    }

    @Override
    public int getNextButtonTitleResId() {
        return R.string.start;
    }

    public static class FinishFragment extends SetupPageFragment {

        private boolean mBluetoothEnabled;

        @Override
        protected void initializePage() {
            final Activity activity = getActivity();
            if (activity != null && SetupWizardUtils.canHasModMOD(activity)) {
                ImageView imageView = (ImageView) mRootView.findViewById(R.id.brand_logo);
                imageView.setImageResource(R.drawable.mod_ready);
                mRootView.findViewById(R.id.mod_welcome).setVisibility(View.VISIBLE);
                mRootView.findViewById(R.id.mod_desc).setVisibility(View.VISIBLE);
            }

            // turn bluetooth on and off to let it initialize its databases
            // and other things that take a _long_ time the first time around
            // this way we are in a state ready to go (and beam!) on the first boot.
            final BluetoothManager bt = getContext().getSystemService(BluetoothManager.class);
            if (bt != null && bt.getAdapter() != null && !bt.getAdapter().isEnabled()) {
                mBluetoothEnabled = bt.getAdapter().enable();
            }
        }

        public boolean enabledBlueooth() {
            return mBluetoothEnabled;
        }

        @Override
        protected int getLayoutResource() {
            return R.layout.setup_finished_page;
        }
    }

}
