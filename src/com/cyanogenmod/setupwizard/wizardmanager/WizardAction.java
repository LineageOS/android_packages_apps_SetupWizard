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

import com.android.internal.util.XmlUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.net.URISyntaxException;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

public class WizardAction {

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

    private static void parseResult(WizardTransitions transitions, XmlPullParser parser) {
        String resultCode = parser.getAttributeValue(WizardScript.WIZARD_SCRIPT_NAMESPACE,
                WizardScript.ATTR_RESULT_CODE);
        String action = parser.getAttributeValue(WizardScript.WIZARD_SCRIPT_NAMESPACE,
                WizardScript.ATTR_ACTION);
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

        int type;
        final int depth = parser.getDepth();
        while (((type = parser.next()) != XmlPullParser.END_TAG ||
                parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
            final int next = parser.next();
            if (next == XmlPullParser.END_TAG || next == XmlPullParser.TEXT) {
                continue;
            }

            if (parser.getName().equals(WizardScript.TAG_RESULT)) {
                parseResult(transitions, parser);
                continue;
            }

            XmlUtils.skipCurrentTag(parser);
            continue;
        }

        return new WizardAction(id, uri, transitions);
    }
}
