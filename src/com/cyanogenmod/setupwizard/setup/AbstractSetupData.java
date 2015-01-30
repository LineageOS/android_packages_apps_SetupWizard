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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;

public abstract class AbstractSetupData extends BroadcastReceiver implements SetupDataCallbacks {

    private static final String TAG = AbstractSetupData.class.getSimpleName();

    protected Context mContext;
    private ArrayList<SetupDataCallbacks> mListeners = new ArrayList<SetupDataCallbacks>();
    private PageList mPageList;

    private int mCurrentPageIndex = 0;

    public AbstractSetupData(Context context) {
        mContext = context;
        mPageList = onNewPageList();
    }

    protected abstract PageList onNewPageList();

    @Override
    public void onPageLoaded(Page page) {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onPageLoaded(page);
        }
    }

    @Override
    public void onPageTreeChanged() {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onPageTreeChanged();
        }
    }

    @Override
    public void onFinish() {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onFinish();
        }
    }

    @Override
    public Page getPage(String key) {
        return mPageList.getPage(key);
    }

    @Override
    public Page getPage(int index) {
        return mPageList.getPage(index);
    }

    public Page getCurrentPage() {
        return mPageList.getPage(mCurrentPageIndex);
    }

    public boolean isFirstPage() {
        return mCurrentPageIndex == 0;
    }

    public boolean isLastPage() {
        return mCurrentPageIndex == mPageList.size() - 1;
    }

    @Override
    public void onNextPage() {
        if (getCurrentPage().doNextAction() == false) {
            if (advanceToNextUnhidden()) {
                for (int i = 0; i < mListeners.size(); i++) {
                    mListeners.get(i).onNextPage();
                }
            }
        }
    }

    @Override
    public void onPreviousPage() {
        if (getCurrentPage().doPreviousAction() == false) {
            if (advanceToPreviousUnhidden()) {
                for (int i = 0; i < mListeners.size(); i++) {
                    mListeners.get(i).onPreviousPage();
                }
            }
        }
    }

    private boolean advanceToNextUnhidden() {
        while (mCurrentPageIndex < mPageList.size()) {
            mCurrentPageIndex++;
            if (!getCurrentPage().isHidden()) {
                return true;
            }
        }
        return false;
    }

    private boolean advanceToPreviousUnhidden() {
        while (mCurrentPageIndex > 0) {
            mCurrentPageIndex--;
            if (!getCurrentPage().isHidden()) {
                return true;
            }
        }
        return false;
    }

    public void load(Bundle savedValues) {
        for (String key : savedValues.keySet()) {
            Page page = mPageList.getPage(key);
            if (page != null) {
                page.resetData(savedValues.getBundle(key));
            }
        }
    }

    public Bundle save() {
        Bundle bundle = new Bundle();
        for (Page page : mPageList.values()) {
            bundle.putBundle(page.getKey(), page.getData());
        }
        return bundle;
    }

    public void registerListener(SetupDataCallbacks listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(SetupDataCallbacks listener) {
        mListeners.remove(listener);
    }
}
