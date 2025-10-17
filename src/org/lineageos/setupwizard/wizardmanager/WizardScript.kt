/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.setupwizard.wizardmanager

import org.lineageos.setupwizard.SetupWizardApp.LOGV

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.util.Xml

import com.android.internal.util.XmlUtils

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap

class WizardScript(
    actions: Map<String, WizardAction>,
    firstActionId: String
) : Parcelable {

    private val mActions: Map<String, WizardAction> =
        Collections.unmodifiableMap(actions)
    private val mFirstActionId: String = firstActionId

    fun getAction(actionId: String): WizardAction? = mActions[actionId]

    fun getFirstAction(): WizardAction? = getAction(mFirstActionId)

    fun getFirstActionId(): String = mFirstActionId

    fun getNextAction(currentActionId: String, resultCode: Int): WizardAction? {
        val nextActionId = getNextActionId(currentActionId, resultCode)
        return nextActionId?.let { getAction(it) }
    }

    fun getNextActionId(currentActionId: String, resultCode: Int): String? {
        var nextActionId: String? = null
        if (resultCode != Activity.RESULT_CANCELED) {
            val wizardAction = mActions[currentActionId]
            if (LOGV) {
                val uri = wizardAction?.uri ?: "n/a"
                Log.v(
                    TAG,
                    "getNextActionId($currentActionId,$resultCode) current uri=$uri"
                )
            }
            // Java would NPE here if wizardAction is null; Kotlin guards it.
            nextActionId = wizardAction?.getNextAction(resultCode)
        } else if (LOGV) {
            Log.v(
                TAG,
                "getNextActionId($currentActionId,$resultCode) RESULT_CANCELED not expected; ignored"
            )
        }
        return nextActionId
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(mFirstActionId)
        dest.writeTypedList(ArrayList(mActions.values))
    }

    private constructor(source: Parcel) : this(
        actions = emptyMap(),
        firstActionId = source.readString() ?: ""
    ) {
        // CREATOR will do work.
    }

    companion object {
        private const val TAG = "WizardScript"

        // Namespace and tag/attr names must stay binary-identical for other classes.
        const val WIZARD_SCRIPT_NAMESPACE =
            "http://schemas.android.com/apk/res/com.google.android.setupwizard"

        const val TAG_WIZARD_SCRIPT = "WizardScript"
        const val TAG_WIZARD_ACTION = "WizardAction"
        const val TAG_RESULT = "result"
        const val ATTR_ID = "id"
        const val ATTR_URI = "uri"
        const val ATTR_ACTION = "action"
        const val ATTR_FIRST_ACTION = "firstAction"
        const val ATTR_RESULT_CODE = "resultCode"

        @JvmField
        val CREATOR: Parcelable.Creator<WizardScript> =
            object : Parcelable.Creator<WizardScript> {
                override fun createFromParcel(source: Parcel): WizardScript {
                    val firstActionId = source.readString() ?: ""
                    val actionList = ArrayList<WizardAction>()
                    source.readTypedList(actionList, WizardAction.CREATOR)
                    val actions = HashMap<String, WizardAction>(actionList.size)
                    for (action in actionList) {
                        actions[action.id] = action
                    }
                    return WizardScript(actions, firstActionId)
                }

                override fun newArray(size: Int): Array<WizardScript?> =
                    arrayOfNulls(size)
            }

        @JvmStatic
        fun loadFromUri(context: Context, uriString: String): WizardScript? {
            return try {
                val res: ContentResolver.OpenResourceIdResult =
                    context.contentResolver.getResourceId(Uri.parse(uriString))
                val parser: XmlPullParser = if (res.r.getResourceTypeName(res.id) == "xml") {
                    res.r.getXml(res.id)
                } else {
                    val inputStream: InputStream =
                        res.r.openRawResource(res.id)
                    Xml.newPullParser().apply { setInput(inputStream, null) }
                }

                // Seek to first start tag
                var next: Int
                do {
                    next = parser.next()
                    if (next == XmlPullParser.END_DOCUMENT) break
                } while (next != XmlPullParser.START_TAG)

                parseWizardScript(parser)
            } catch (e: XmlPullParserException) {
                Log.e(TAG, "Ill-formatted wizard_script: $uriString")
                Log.e(TAG, e.message ?: "")
                null
            } catch (fnfe: FileNotFoundException) {
                Log.e(TAG, "Cannot find file: $uriString")
                Log.e(TAG, fnfe.message ?: "")
                null
            } catch (ioe: IOException) {
                Log.e(TAG, "Unable to read wizard_script: $uriString")
                Log.e(TAG, ioe.message ?: "")
                null
            }
        }

        @Throws(XmlPullParserException::class, IOException::class)
        private fun parseWizardScript(parser: XmlPullParser): WizardScript {
            val startTag = parser.name
            if (startTag != TAG_WIZARD_SCRIPT) {
                throw XmlPullParserException(
                    "XML document must start with <WizardScript> tag; found " +
                            "$startTag at ${parser.positionDescription}"
                )
            }

            val firstAction = parser.getAttributeValue(WIZARD_SCRIPT_NAMESPACE, ATTR_FIRST_ACTION)
                ?: throw XmlPullParserException("WizardScript must define a firstAction")

            val wizardActions = HashMap<String, WizardAction>()
            val depth = parser.depth
            var type: Int

            while (((parser.next().also { type = it }) != XmlPullParser.END_TAG || parser.depth > depth)
                && type != XmlPullParser.END_DOCUMENT
            ) {
                // Fixes Java bug: use AND (&&) and do NOT call next() again here.
                if (type != XmlPullParser.END_TAG && type != XmlPullParser.TEXT) {
                    if (parser.name == TAG_WIZARD_ACTION) {
                        val action = WizardAction.parseWizardAction(parser)
                        wizardActions[action.id] = action
                    } else {
                        XmlUtils.skipCurrentTag(parser)
                    }
                }
            }

            return WizardScript(wizardActions, firstAction)
        }
    }
}
