/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.locale

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.Toast
import com.android.internal.telephony.TelephonyIntents
import com.android.internal.telephony.util.LocaleUtils
import com.google.android.setupcompat.util.SystemBarHelper
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.SetupWizardApp
import org.lineageos.setupwizard.base.BaseSetupWizardActivity
import org.lineageos.setupwizard.widget.LocalePicker

class LocaleActivity : BaseSetupWizardActivity() {

    private lateinit var mLocaleAdapter:
        ArrayAdapter<com.android.internal.app.LocalePicker.LocaleInfo>
    private var mCurrentLocale: Locale? = null
    private lateinit var mAdapterIndices: IntArray
    private lateinit var mLanguagePicker: LocalePicker
    private var mFetchUpdateSimLocaleTask: ExecutorService? = null
    private val mHandler = Handler(Looper.getMainLooper())
    private var mPendingLocaleUpdate = false
    private var mPaused = true

    private val mSetupWizardApp: SetupWizardApp by lazy { application as SetupWizardApp }

    private val mUpdateLocale = Runnable {
        val locale = mCurrentLocale ?: return@Runnable
        mLanguagePicker.isEnabled = false
        com.android.internal.app.LocalePicker.updateLocale(locale)
    }

    private val mSimChangedReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == TelephonyIntents.ACTION_SIM_STATE_CHANGED) {
                    fetchAndUpdateSimLocale()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemBarHelper.setBackButtonVisible(window, true)
        setNextText(R.string.next)
        mLanguagePicker = findViewById(R.id.locale_list)
        mLanguagePicker.setNextRight(nextButton.id)
        mLanguagePicker.requestFocus()
        if (resources.getBoolean(R.bool.config_isLargeNoTouch)) {
            mLanguagePicker.setOnClickListener { nextButton.performClick() }
        }
        loadLanguages()
    }

    override fun onPause() {
        super.onPause()
        mPaused = true
        unregisterReceiver(mSimChangedReceiver)
    }

    override fun onResume() {
        super.onResume()
        mPaused = false
        registerReceiver(
            mSimChangedReceiver,
            IntentFilter(TelephonyIntents.ACTION_SIM_STATE_CHANGED),
        )
        mLanguagePicker.isEnabled = true
        if (mPendingLocaleUpdate) {
            mPendingLocaleUpdate = false
            fetchAndUpdateSimLocale()
        }
    }

    override fun getLayoutResId(): Int = R.layout.setup_locale

    override fun getTitleResId(): Int = R.string.setup_locale

    override fun getIconResId(): Int = R.drawable.ic_locale

    private fun loadLanguages() {
        mLocaleAdapter =
            com.android.internal.app.LocalePicker.constructAdapter(
                this,
                R.layout.locale_picker_item,
                R.id.locale,
            )
        mCurrentLocale = Locale.getDefault()
        fetchAndUpdateSimLocale()
        mAdapterIndices = IntArray(mLocaleAdapter.count)
        var currentLocaleIndex = 0
        val labels = Array(mLocaleAdapter.count) { "" }
        for (i in mAdapterIndices.indices) {
            val localLocaleInfo = mLocaleAdapter.getItem(i)!!
            val localLocale = localLocaleInfo.locale
            if (localLocale == mCurrentLocale) {
                currentLocaleIndex = i
            }
            mAdapterIndices[i] = i
            labels[i] = localLocaleInfo.label
        }
        mLanguagePicker.setDisplayedValues(labels)
        mLanguagePicker.maxValue = labels.size - 1
        mLanguagePicker.value = currentLocaleIndex
        mLanguagePicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        mLanguagePicker.setOnValueChangedListener { _, _, _ -> setLocaleFromPicker() }
        mLanguagePicker.setOnScrollListener { _, scrollState ->
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                mSetupWizardApp.setIgnoreSimLocale(true)
            }
        }
    }

    private fun setLocaleFromPicker() {
        mSetupWizardApp.setIgnoreSimLocale(true)
        val i = mAdapterIndices[mLanguagePicker.value]
        val localLocaleInfo = mLocaleAdapter.getItem(i)!!
        onLocaleChanged(localLocaleInfo.locale)
    }

    private fun onLocaleChanged(paramLocale: Locale) {
        mLanguagePicker.isEnabled = true
        mHandler.removeCallbacks(mUpdateLocale)
        mCurrentLocale = paramLocale
        mHandler.postDelayed(mUpdateLocale, 1000)
    }

    private fun fetchAndUpdateSimLocale() {
        if (mSetupWizardApp.ignoreSimLocale() || isDestroyed) {
            return
        }
        if (mPaused) {
            mPendingLocaleUpdate = true
            return
        }
        mFetchUpdateSimLocaleTask?.shutdown()
        mFetchUpdateSimLocaleTask = Executors.newSingleThreadExecutor()
        mFetchUpdateSimLocaleTask!!.execute {
            var locale: Locale? = null
            if (!isFinishing || !isDestroyed) {
                // If the sim is currently pin locked, return
                val telephonyManager =
                    getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val state = telephonyManager.simState
                if (
                    state == TelephonyManager.SIM_STATE_PIN_REQUIRED ||
                        state == TelephonyManager.SIM_STATE_PUK_REQUIRED
                ) {
                    return@execute
                }

                val subscriptionManager = getSystemService(SubscriptionManager::class.java)
                val activeSubs = subscriptionManager.activeSubscriptionInfoList
                if (activeSubs == null || activeSubs.isEmpty()) {
                    return@execute
                }

                // Fetch locale for active sim's MCC
                val mccString = activeSubs[0].mccString
                if (!mccString.isNullOrEmpty()) {
                    runCatching { mccString.toInt() }
                        .onSuccess { mcc ->
                            locale = LocaleUtils.getLocaleFromMcc(this@LocaleActivity, mcc, null)
                        }
                        .onFailure { e -> Log.w(TAG, "mccString not a number: '$mccString'", e) }
                } else {
                    Log.w(TAG, "Unexpected mccString: '$mccString'")
                }

                // If that fails, fall back to preferred languages reported by the sim
                if (locale == null) {
                    locale = telephonyManager.simLocale
                }

                val finalLocale = locale
                mHandler.post {
                    if (finalLocale != null && finalLocale != mCurrentLocale) {
                        if (!mSetupWizardApp.ignoreSimLocale() && !isDestroyed) {
                            val label =
                                getString(R.string.sim_locale_changed, finalLocale.displayName)
                            Toast.makeText(this@LocaleActivity, label, Toast.LENGTH_SHORT).show()
                            onLocaleChanged(finalLocale)
                            mSetupWizardApp.setIgnoreSimLocale(true)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "LocaleActivity"
    }
}
