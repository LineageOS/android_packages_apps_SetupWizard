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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class WizardScript {

    private static final String TAG = "WizardScript";

    private static final boolean LOGV = true;

    public static final String WIZARD_SCRIPT_NAMESPACE =
            "http://schemas.android.com/apk/res/com.google.android.setupwizard";

    public static final String TAG_WIZARD_SCRIPT = "WizardScript";
    public static final String TAG_WIZARD_ACTION = "WizardAction";
    public static final String TAG_RESULT = "result";
    public static final String ATTR_ID = "id";
    public static final String ATTR_URI = "uri";
    public static final String ATTR_ACTION = "action";
    public static final String ATTR_FIRST_ACTION = "firstAction";
    public static final String ATTR_RESULT_CODE = "resultCode";

    private final Map<String, WizardAction> mActions;
    private final String mFirstActionId;

    public WizardScript(Map<String, WizardAction> actions, String firstActionId) {
        mActions = actions;
        mFirstActionId = firstActionId;
    }

    public WizardAction getAction(String actionId) {
        return mActions.get(actionId);
    }

    public WizardAction getFirstAction() {
        return getAction(mFirstActionId);
    }

    public String getFirstActionId() {
        return mFirstActionId;
    }

    public WizardAction getNextAction(String currentActionId, int resultCode) {
        WizardAction wizardAction = null;
        String nextActionId = getNextActionId(currentActionId, resultCode);
        if (nextActionId != null) {
            wizardAction = getAction(nextActionId);
        }
        return wizardAction;
    }

    public String getNextActionId(String currentActionId, int resultCode) {
        String nextActionId = null;
        if(resultCode != Activity.RESULT_CANCELED) {
            WizardAction wizardAction = mActions.get(currentActionId);
            if (LOGV) {
                StringBuilder currentAction =
                        new StringBuilder().append("getNextActionId(").append(currentActionId)
                        .append(",").append(resultCode).append(")").append(" current uri=");
                String uri = wizardAction == null ? "n/a" : wizardAction.getUri();
                Log.v(TAG, currentAction.append(uri).toString());
            }
            nextActionId = wizardAction.getNextAction(resultCode);
        } else {
            if (LOGV) {
                Log.v(TAG, "getNextActionId(" + currentActionId + "," + resultCode
                        + ")" + " RESULT_CANCELED not expected; ignored");
            }
        }
        return nextActionId;
    }

    public static WizardScript loadFromUri(Context context, String uriString) {
        XmlPullParser xmlPullParser;
        WizardScript wizardScript = null;
        try {
            ContentResolver.OpenResourceIdResult openResourceIdResult =
                    context.getContentResolver().getResourceId(Uri
                    .parse(uriString));
            if("xml".equals(openResourceIdResult.r.getResourceTypeName(openResourceIdResult.id))) {
                xmlPullParser =
                        openResourceIdResult.r.getXml(openResourceIdResult.id);
            } else {
                InputStream inputStream =
                        openResourceIdResult.r.openRawResource(openResourceIdResult.id);
                xmlPullParser = Xml.newPullParser();
                xmlPullParser.setInput(inputStream, null);
            }

            int next;
            do {
                next = xmlPullParser.next();
                if (next == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            }
            while (next != XmlPullParser.START_TAG);

            return parseWizardScript(xmlPullParser);
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Ill-formatted wizard_script: " + uriString);
            Log.e(TAG, e.getMessage());
            return wizardScript;
        } catch(FileNotFoundException fnfe) {
            Log.e(TAG, "Cannot find file: " + uriString);
            Log.e(TAG, fnfe.getMessage());
            return wizardScript;
        } catch(IOException ioe) {
            Log.e(TAG, "Unable to read wizard_script: " + uriString);
            Log.e(TAG, ioe.getMessage());
            return wizardScript;
        }
    }

    private static WizardScript parseWizardScript(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        String startTag = parser.getName();
        if(!TAG_WIZARD_SCRIPT.equals(startTag)) {
            throw new XmlPullParserException("XML document must start with " +
                    "<WizardScript> tag; found "
                    + startTag + " at " + parser.getPositionDescription());
        }

        String firstAction = parser.getAttributeValue(WIZARD_SCRIPT_NAMESPACE, ATTR_FIRST_ACTION);
        if(firstAction == null) {
            throw new XmlPullParserException("WizardScript must define a firstAction");
        }

        HashMap wizardActions = new HashMap();
        int type;
        final int depth = parser.getDepth();
        while (((type = parser.next()) != XmlPullParser.END_TAG ||
                parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
            final int next = parser.next();
            if (next == XmlPullParser.END_TAG || next == XmlPullParser.TEXT) {
                continue;
            }

            if (TAG_WIZARD_ACTION.equals(parser.getName())) {
                WizardAction wizardAction = WizardAction.parseWizardAction(parser);
                if (wizardAction == null) {
                    continue;
                }
                wizardActions.put(wizardAction.getId(), wizardAction);
                continue;
            }

            XmlUtils.skipCurrentTag(parser);
            continue;
        }

        return new WizardScript(wizardActions, firstAction);
    }
}
