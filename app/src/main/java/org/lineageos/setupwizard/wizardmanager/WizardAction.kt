/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.wizardmanager

import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.android.internal.util.XmlUtils
import org.lineageos.setupwizard.SetupWizardApp.Companion.LOGV
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

class WizardAction(val id: String, val uri: String, val transitions: WizardTransitions) :
    Parcelable {

    fun getIntent(): Intent? =
        runCatching {
                Intent.parseUri(uri, Intent.URI_INTENT_SCHEME).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            .getOrElse {
                Log.e(TAG, "Bad URI: $uri")
                null
            }

    fun getNextAction(resultCode: Int): String? = transitions.getAction(resultCode)

    override fun toString(): String =
        "WizardAction{mId='$id', mUri='$uri', mTransitions=$transitions}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WizardAction) return false
        return id == other.id && uri == other.uri && transitions == other.transitions
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + transitions.hashCode()
        return result
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(uri)
        dest.writeParcelable(transitions, flags)
    }

    companion object {
        private const val TAG = "WizardAction"

        @JvmField
        val CREATOR: Parcelable.Creator<WizardAction> =
            object : Parcelable.Creator<WizardAction> {
                override fun createFromParcel(source: Parcel): WizardAction {
                    val id = requireNotNull(source.readString()) { "id missing in parcel" }
                    val uri = requireNotNull(source.readString()) { "uri missing in parcel" }
                    val transitions =
                        requireNotNull(
                            source.readParcelable(
                                WizardTransitions::class.java.classLoader,
                                WizardTransitions::class.java,
                            )
                        ) {
                            "transitions missing in parcel"
                        }
                    return WizardAction(id, uri, transitions)
                }

                override fun newArray(size: Int): Array<WizardAction?> = arrayOfNulls(size)
            }

        private fun parseResult(transitions: WizardTransitions, parser: XmlPullParser) {
            val resultCode =
                parser.getAttributeValue(
                    WizardScript.WIZARD_SCRIPT_NAMESPACE,
                    WizardScript.ATTR_RESULT_CODE,
                )
            val action =
                parser.getAttributeValue(
                    WizardScript.WIZARD_SCRIPT_NAMESPACE,
                    WizardScript.ATTR_ACTION,
                )
            if (LOGV) {
                Log.v(TAG, "parseResult{resultCode='$resultCode', action=$action}")
            }
            if (resultCode == null) {
                transitions.setDefaultAction(action)
            } else {
                transitions.put(resultCode.toInt(), action)
            }
        }

        fun parseWizardAction(parser: XmlPullParser): WizardAction {
            val id =
                parser.getAttributeValue(null, WizardScript.ATTR_ID)
                    ?: throw XmlPullParserException("WizardAction must define an id")
            val uri =
                parser.getAttributeValue(
                    WizardScript.WIZARD_SCRIPT_NAMESPACE,
                    WizardScript.ATTR_URI,
                ) ?: throw XmlPullParserException("WizardAction must define an intent URI")

            if (LOGV) {
                Log.v(TAG, "parseWizardAction{id='$id', uri=$uri}")
            }

            val transitions = WizardTransitions()
            val depth = parser.depth
            var type: Int
            while (
                ((parser.next().also { type = it }) != XmlPullParser.END_TAG ||
                    parser.depth > depth) && type != XmlPullParser.END_DOCUMENT
            ) {
                if (type != XmlPullParser.END_TAG && type != XmlPullParser.TEXT) {
                    if (parser.name == WizardScript.TAG_RESULT) {
                        parseResult(transitions, parser)
                    } else {
                        XmlUtils.skipCurrentTag(parser)
                    }
                }
            }

            return WizardAction(id, uri, transitions)
        }
    }
}
