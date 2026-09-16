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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.android.internal.app.LocaleHelper
import com.android.internal.app.LocaleStore
import com.android.internal.telephony.TelephonyIntents
import com.android.internal.telephony.util.LocaleUtils
import com.google.android.setupcompat.util.SystemBarHelper
import com.google.android.setupdesign.GlifRecyclerLayout
import com.google.android.setupdesign.items.Item
import com.google.android.setupdesign.items.ItemGroup
import com.google.android.setupdesign.items.RecyclerItemAdapter
import com.google.android.setupdesign.items.SectionItem
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.SetupWizardApp
import org.lineageos.setupwizard.base.BaseSetupWizardActivity

class LocaleActivity : BaseSetupWizardActivity() {

    private val recyclerLayout by lazy { glifLayout as GlifRecyclerLayout }

    private var parentLanguage: LocaleStore.LocaleInfo? = null

    private val regionsBackCallback =
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                showLanguages()
            }
        }

    private val handler = Handler(Looper.getMainLooper())
    private val fetchSimLocaleExecutor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }
    private var pendingLocaleUpdate = false
    private var paused = true

    private val setupWizardApp: SetupWizardApp by lazy { application as SetupWizardApp }

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

        onBackPressedDispatcher.addCallback(this, regionsBackCallback)

        showLanguages()
        fetchAndUpdateSimLocale()
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
        if (pendingLocaleUpdate) {
            pendingLocaleUpdate = false
            fetchAndUpdateSimLocale()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        fetchSimLocaleExecutor.shutdownNow()
    }

    override val layoutResId = R.layout.setup_locale

    override val titleResId = R.string.setup_locale

    override val iconResId = R.drawable.ic_locale

    override val installFooterBar = false

    private fun showLanguages() {
        parentLanguage = null
        regionsBackCallback.isEnabled = false
        show(levelLocales(parent = null), countryMode = false)
    }

    private fun showRegions(language: LocaleStore.LocaleInfo) {
        parentLanguage = language
        regionsBackCallback.isEnabled = true
        show(levelLocales(parent = language), countryMode = true)
    }

    private fun levelLocales(parent: LocaleStore.LocaleInfo?): List<LocaleStore.LocaleInfo> =
        LocaleStore.getLevelLocales(this, emptySet(), parent, /* translatedOnly= */ true)
            .sortedWith(LocaleHelper.LocaleInfoComparator(Locale.getDefault(), parent != null))

    private fun show(locales: List<LocaleStore.LocaleInfo>, countryMode: Boolean) {
        val (suggested, remaining) = locales.partition { it.isSuggested }

        val root = ItemGroup()
        suggested.forEach { root.addChild(localeItem(it, countryMode)) }

        if (remaining.isNotEmpty()) {
            val section = SectionItem()
            remaining.forEach { section.addChild(localeItem(it, countryMode)) }
            if (suggested.isNotEmpty()) {
                section.setHeaderTitle("")
            }
            root.addChild(section)
        }

        val adapter = RecyclerItemAdapter(root)
        adapter.setOnItemSelectedListener { item ->
            (item as? LocaleItem)?.let { onItemSelected(it) }
        }
        recyclerLayout.adapter = adapter
        recyclerLayout.recyclerView.scrollToPosition(0)
    }

    private fun localeItem(localeInfo: LocaleStore.LocaleInfo, countryMode: Boolean) =
        LocaleItem(localeInfo).apply {
            title =
                if (countryMode) {
                    localeInfo.fullCountryNameNative
                } else {
                    localeInfo.fullNameNative
                }
        }

    private fun onItemSelected(item: LocaleItem) {
        val localeInfo = item.localeInfo
        if (parentLanguage == null && levelLocales(parent = localeInfo).size > 1) {
            showRegions(localeInfo)
            return
        }
        setupWizardApp.ignoreSimLocale = true
        applyLocale(localeInfo.locale)
    }

    private fun applyLocale(locale: Locale) {
        com.android.internal.app.LocalePicker.updateLocale(locale)
        nextAction(RESULT_OK)
    }

    private fun fetchAndUpdateSimLocale() {
        if (setupWizardApp.ignoreSimLocale || isDestroyed) {
            return
        }
        if (paused) {
            pendingLocaleUpdate = true
            return
        }
        fetchSimLocaleExecutor.execute {
            val locale = simLocale() ?: return@execute
            handler.post {
                if (
                    locale == Locale.getDefault() || setupWizardApp.ignoreSimLocale || isDestroyed
                ) {
                    return@post
                }
                Toast.makeText(
                        this,
                        getString(R.string.sim_locale_changed, locale.displayName),
                        Toast.LENGTH_SHORT,
                    )
                    .show()
                setupWizardApp.ignoreSimLocale = true
                com.android.internal.app.LocalePicker.updateLocale(locale)
            }
        }
    }

    private fun simLocale(): Locale? {
        if (isFinishing || isDestroyed) {
            return null
        }
        val telephonyManager = getSystemService(TelephonyManager::class.java) ?: return null

        val state = telephonyManager.simState
        if (
            state == TelephonyManager.SIM_STATE_PIN_REQUIRED ||
                state == TelephonyManager.SIM_STATE_PUK_REQUIRED
        ) {
            return null
        }

        val subscriptionManager = getSystemService(SubscriptionManager::class.java)
        val activeSub =
            subscriptionManager?.activeSubscriptionInfoList?.firstOrNull() ?: return null

        val mccString = activeSub.mccString
        val mcc = mccString?.toIntOrNull()
        if (mcc == null) {
            Log.w(TAG, "Unexpected mccString: '$mccString'")
        } else {
            LocaleUtils.getLocaleFromMccMnc(this, mcc, null, null)?.let {
                return it
            }
        }

        return telephonyManager.simLocale
    }

    private class LocaleItem(val localeInfo: LocaleStore.LocaleInfo) : Item()

    companion object {
        private const val TAG = "LocaleActivity"
    }
}
