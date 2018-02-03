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

package com.cyanogenmod.setupwizard.wizardmanager;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

import static com.cyanogenmod.setupwizard.SetupWizardApp.LOGV;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.android.internal.util.XmlUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URISyntaxException;

public class WizardAction implements Parcelable {

    private static final String TAG = "WizardAction";

    private final String mId;
    private final String mUri;
    private final WizardTransitions mTransitions;

    public WizardAction(String id, String uri, WizardTransitions transitions) {
        if (transitions == null) {
            throw new IllegalArgumentException("WizardTransitions cannot be null");
        }
        mId = id;
        mUri = uri;
        mTransitions = transitions;
    }

    public String getId() {
        return mId;
    }

    public String getUri() {
        return mUri;
    }

    public Intent getIntent() {
        Intent intent = null;
        try {
            intent = Intent.parseUri(mUri, FLAG_GRANT_READ_URI_PERMISSION);
        } catch(URISyntaxException e) {
            Log.e(TAG, "Bad URI: " + mUri);
        }
        return intent;
    }

    public String getNextAction(int resultCode) {
        return mTransitions.getAction(resultCode);
    }

    @Override
    public String toString() {
        return "WizardAction{" +
                "mId='" + mId + '\'' +
                ", mUri='" + mUri + '\'' +
                ", mTransitions=" + mTransitions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WizardAction)) return false;

        WizardAction that = (WizardAction) o;

        if (mId != null ? !mId.equals(that.mId) : that.mId != null) return false;
        if (mUri != null ? !mUri.equals(that.mUri) : that.mUri != null) return false;
        return mTransitions != null ?
                mTransitions.equals(that.mTransitions) :
                that.mTransitions == null;

    }

    @Override
    public int hashCode() {
        int result = mId != null ? mId.hashCode() : 0;
        result = 31 * result + (mUri != null ? mUri.hashCode() : 0);
        result = 31 * result + (mTransitions != null ? mTransitions.hashCode() : 0);
        return result;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUri);
        dest.writeString(mId);
        dest.writeParcelable(mTransitions, flags);
    }

    public static final Creator<WizardAction> CREATOR = new Creator<WizardAction>() {
        public WizardAction createFromParcel(Parcel source) {
            return new WizardAction(source.readString(),
                    source.readString(),
                    source.readParcelable(WizardTransitions.class.getClassLoader()));
        }

        public WizardAction[] newArray(int size) {
            return new WizardAction[size];
        }
    };

    private static void parseResult(WizardTransitions transitions, XmlPullParser parser) {
        String resultCode = parser.getAttributeValue(WizardScript.WIZARD_SCRIPT_NAMESPACE,
                WizardScript.ATTR_RESULT_CODE);
        String action = parser.getAttributeValue(WizardScript.WIZARD_SCRIPT_NAMESPACE,
                WizardScript.ATTR_ACTION);
        if (LOGV) {
            Log.v(TAG, "parseResult{" +
                    "resultCode='" + resultCode + '\'' +
                    ", action=" + action +
                    '}');
        }
        if (resultCode == null) {
            transitions.setDefaultAction(action);
        } else {
            transitions.put(Integer.valueOf(resultCode).intValue(), action);
        }
    }

    public static WizardAction parseWizardAction(XmlPullParser parser) throws XmlPullParserException,
            IOException {
        String id = parser.getAttributeValue(null, WizardScript.ATTR_ID);
        String uri = parser.getAttributeValue(WizardScript.WIZARD_SCRIPT_NAMESPACE,
                WizardScript.ATTR_URI);
        WizardTransitions transitions = new WizardTransitions();
        if (id == null) {
            throw new XmlPullParserException("WizardAction must define an id");
        }

        if (uri == null) {
            throw new XmlPullParserException("WizardAction must define an intent URI");
        }
        if (LOGV) {
            Log.v(TAG, "parseWizardAction{" +
                    "id='" + id + '\'' +
                    ", uri=" + uri +
                    '}');
        }
        int type;
        final int depth = parser.getDepth();
        while (((type = parser.next()) != XmlPullParser.END_TAG ||
                parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
            if (!(type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT)) {
                if (parser.getName().equals(WizardScript.TAG_RESULT)) {
                    parseResult(transitions, parser);
                } else {
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }

        return new WizardAction(id, uri, transitions);
    }
}
