package com.cyanogenmod.setupwizard.setup;


import android.util.SparseArray;

public class WizardTransitions extends SparseArray<String> {

    private String mDefaultAction;

    public void setDefaultAction(String action) {
        mDefaultAction = action;
    }

    public String getAction(int resultCode) {
        return this.get(resultCode, this.mDefaultAction);
    }

    public String toString() {
        return super.toString() + " mDefaultAction: " + this.mDefaultAction;
    }


}
