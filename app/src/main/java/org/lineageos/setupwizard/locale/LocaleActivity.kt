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

    private lateinit var localeAdapter:
        ArrayAdapter<com.android.internal.app.LocalePicker.LocaleInfo>
    private var currentLocale: Locale? = null
    private lateinit var adapterIndices: IntArray
    private lateinit var languagePicker: LocalePicker
    private var fetchUpdateSimLocaleTask: ExecutorService? = null
    private val handler = Handler(Looper.getMainLooper())
    private var pendingLocaleUpdate = false
    private var paused = true

    private val setupWizardApp: SetupWizardApp by lazy { application as SetupWizardApp }

    private val updateLocale = Runnable {
        val locale = currentLocale ?: return@Runnable
        languagePicker.isEnabled = false
        com.android.internal.app.LocalePicker.updateLocale(locale)
    }

    private val simChangedReceiver =
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
        languagePicker = findViewById(R.id.locale_list)
        languagePicker.setNextRight(getNextButton().id)
        languagePicker.requestFocus()
        if (resources.getBoolean(R.bool.config_isLargeNoTouch)) {
            languagePicker.setOnClickListener { getNextButton().performClick() }
        }
        loadLanguages()
    }

    override fun onPause() {
        super.onPause()
        paused = true
        unregisterReceiver(simChangedReceiver)
    }

    override fun onResume() {
        super.onResume()
        paused = false
        registerReceiver(
            simChangedReceiver,
            IntentFilter(TelephonyIntents.ACTION_SIM_STATE_CHANGED),
        )
        languagePicker.isEnabled = true
        if (pendingLocaleUpdate) {
            pendingLocaleUpdate = false
            fetchAndUpdateSimLocale()
        }
    }

    override val layoutResId: Int = R.layout.setup_locale

    override val titleResId: Int = R.string.setup_locale

    override val iconResId: Int = R.drawable.ic_locale

    private fun loadLanguages() {
        localeAdapter =
            com.android.internal.app.LocalePicker.constructAdapter(
                this,
                R.layout.locale_picker_item,
                R.id.locale,
            )
        currentLocale = Locale.getDefault()
        fetchAndUpdateSimLocale()
        val infos = List(localeAdapter.count) { localeAdapter.getItem(it)!! }
        adapterIndices = IntArray(infos.size) { it }
        val labels = infos.map { it.label }.toTypedArray()
        val currentLocaleIndex = infos.indexOfFirst { it.locale == currentLocale }.coerceAtLeast(0)
        languagePicker.setDisplayedValues(labels)
        languagePicker.maxValue = labels.size - 1
        languagePicker.value = currentLocaleIndex
        languagePicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        languagePicker.setOnValueChangedListener { _, _, _ -> setLocaleFromPicker() }
        languagePicker.setOnScrollListener { _, scrollState ->
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                setupWizardApp.ignoreSimLocale = true
            }
        }
    }

    private fun setLocaleFromPicker() {
        setupWizardApp.ignoreSimLocale = true
        val i = adapterIndices[languagePicker.value]
        val localLocaleInfo = localeAdapter.getItem(i)!!
        onLocaleChanged(localLocaleInfo.locale)
    }

    private fun onLocaleChanged(paramLocale: Locale) {
        languagePicker.isEnabled = true
        handler.removeCallbacks(updateLocale)
        currentLocale = paramLocale
        handler.postDelayed(updateLocale, 1000)
    }

    private fun fetchAndUpdateSimLocale() {
        if (setupWizardApp.ignoreSimLocale || isDestroyed) {
            return
        }
        if (paused) {
            pendingLocaleUpdate = true
            return
        }
        fetchUpdateSimLocaleTask?.shutdown()
        fetchUpdateSimLocaleTask = Executors.newSingleThreadExecutor()
        fetchUpdateSimLocaleTask!!.execute {
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
                handler.post {
                    if (finalLocale != null && finalLocale != currentLocale) {
                        if (!setupWizardApp.ignoreSimLocale && !isDestroyed) {
                            val label =
                                getString(R.string.sim_locale_changed, finalLocale.displayName)
                            Toast.makeText(this@LocaleActivity, label, Toast.LENGTH_SHORT).show()
                            onLocaleChanged(finalLocale)
                            setupWizardApp.ignoreSimLocale = true
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
