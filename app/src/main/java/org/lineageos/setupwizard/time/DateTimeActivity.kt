/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.time

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.DatePicker
import android.widget.SimpleAdapter
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import com.android.settingslib.datetime.ZoneGetter
import com.google.android.setupdesign.GlifRecyclerLayout
import com.google.android.setupdesign.items.Item
import com.google.android.setupdesign.items.RecyclerItemAdapter
import com.google.android.setupdesign.items.SwitchItem
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.base.BaseSetupWizardActivity
import org.lineageos.setupwizard.util.SetupWizardUtils

class DateTimeActivity :
    BaseSetupWizardActivity(),
    TimePickerDialog.OnTimeSetListener,
    DatePickerDialog.OnDateSetListener {

    private val itemAdapter by lazy {
        (glifLayout as GlifRecyclerLayout).adapter as RecyclerItemAdapter
    }

    private val dateItem by lazy { itemAdapter.findItemById(R.id.date_item) as Item }
    private val dateFormatItem by lazy { itemAdapter.findItemById(R.id.date_format_item) as Item }
    private val timeItem by lazy { itemAdapter.findItemById(R.id.time_item) as Item }
    private val timeZoneItem by lazy { itemAdapter.findItemById(R.id.time_zone_item) as Item }
    private val timeFormatItem by lazy {
        itemAdapter.findItemById(R.id.time_format_item) as SwitchItem
    }

    private val handler = Handler(Looper.getMainLooper())

    private val intentReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                updateTimeAndDateDisplay()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNextText(R.string.next)
        glifLayout.setDescriptionText(getString(R.string.date_time_summary))

        dateFormatItem.summary = dateFormatOrder()
        timeFormatItem.isChecked = DateFormat.is24HourFormat(this)
        timeFormatItem.setOnCheckedChangeListener { _, isChecked -> set24HourFormat(isChecked) }

        itemAdapter.setOnItemSelectedListener { item ->
            when (item) {
                dateItem -> showDatePicker()
                timeItem -> showTimePicker()
                timeZoneItem -> showTimeZonePicker()
                timeFormatItem -> timeFormatItem.isChecked = !timeFormatItem.isChecked
            }
        }

        // Pre-select current/default date if epoch
        handler.post {
            val calendar = Calendar.getInstance()
            val isEpoch = calendar.get(Calendar.YEAR) == 1970
            if (isEpoch) {
                // If epoch, set date to build date
                val timestamp = SetupWizardUtils.getBuildDateTimestamp()
                if (timestamp > 0) {
                    calendar.timeInMillis = timestamp * 1000
                    setDate(
                        this,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                    )
                } else {
                    // no build date available, use a sane default
                    setDate(this, 2017, Calendar.JANUARY, 1)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Register for time ticks and other reasons for time change
        val filter =
            IntentFilter().apply {
                addAction(Intent.ACTION_TIME_TICK)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            }
        registerReceiver(intentReceiver, filter)
        updateTimeAndDateDisplay()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(intentReceiver)
    }

    override val layoutResId: Int = R.layout.setup_datetime_page

    override val titleResId: Int = R.string.setup_datetime

    override val iconResId: Int = R.drawable.ic_datetime

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        setDate(this, year, month, day)
        updateTimeAndDateDisplay()
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        setTime(this, hourOfDay, minute)
        updateTimeAndDateDisplay()
    }

    private fun showDatePicker() {
        DatePickerFragment.newInstance().show(supportFragmentManager, DatePickerFragment.TAG)
    }

    private fun showTimePicker() {
        TimePickerFragment.newInstance().show(supportFragmentManager, TimePickerFragment.TAG)
    }

    private fun showTimeZonePicker() {
        val adapter = constructTimezoneAdapter(this)
        val currentTimeZone = TimeZone.getDefault()
        val dialog =
            AlertDialog.Builder(this)
                .setTitle(R.string.setup_time_zone)
                .setAdapter(adapter) { _, position ->
                    val zone = adapter.getItem(position) as Map<*, *>
                    setTimeZone(this, zone[KEY_ID] as String)
                    updateTimeAndDateDisplay()
                }
                .create()
        dialog.show()
        val selection = getTimeZoneIndex(adapter, currentTimeZone)
        if (selection != -1) {
            dialog.listView.setSelection(selection)
        }
    }

    private fun dateFormatOrder() =
        DateFormat.getDateFormatOrder(this).joinToString(" / ") {
            when (it) {
                'd' -> getString(R.string.date_format_day)
                'M' -> getString(R.string.date_format_month)
                else -> getString(R.string.date_format_year)
            }
        }

    private fun set24HourFormat(is24Hour: Boolean) {
        Settings.System.putString(
            contentResolver,
            Settings.System.TIME_12_24,
            if (is24Hour) HOURS_24 else HOURS_12,
        )
        sendBroadcast(
            Intent(Intent.ACTION_TIME_CHANGED)
                .putExtra(
                    Intent.EXTRA_TIME_PREF_24_HOUR_FORMAT,
                    if (is24Hour) {
                        Intent.EXTRA_TIME_PREF_VALUE_USE_24_HOUR
                    } else {
                        Intent.EXTRA_TIME_PREF_VALUE_USE_12_HOUR
                    },
                )
        )
    }

    private fun updateTimeAndDateDisplay() {
        val now = Calendar.getInstance()
        dateItem.summary = DateFormat.getLongDateFormat(this).format(now.time)
        timeItem.summary = DateFormat.getTimeFormat(this).format(now.time)
        timeZoneItem.summary =
            ZoneGetter.getTimeZoneOffsetAndName(this, TimeZone.getDefault(), Date(now.timeInMillis))
        timeFormatItem.isChecked = DateFormat.is24HourFormat(this)
    }

    private class TimeZoneComparator(private val sortingKey: String) : Comparator<Map<*, *>> {

        @Suppress("UNCHECKED_CAST")
        override fun compare(map1: Map<*, *>, map2: Map<*, *>): Int {
            val value1 = map1[sortingKey]
            val value2 = map2[sortingKey]
            /*
             * This should never happen, but just in-case, put non-comparable
             * items at the end.
             */
            if (value1 !is Comparable<*>) {
                return if (value2 is Comparable<*>) 1 else 0
            } else if (value2 !is Comparable<*>) {
                return -1
            }
            return (value1 as Comparable<Any>).compareTo(value2)
        }
    }

    companion object {
        private const val KEY_ID = "id"
        private const val KEY_DISPLAYNAME = "name"
        private const val KEY_GMT = "gmt"
        private const val KEY_OFFSET = "offset"

        private const val HOURS_12 = "12"
        private const val HOURS_24 = "24"

        private fun constructTimezoneAdapter(context: Context): SimpleAdapter {
            val from = arrayOf(KEY_DISPLAYNAME, KEY_GMT)
            val to = intArrayOf(android.R.id.text1, android.R.id.text2)

            val sortedList = ZoneGetter.getZonesList(context)
            sortedList.sortWith(TimeZoneComparator(KEY_OFFSET))

            return SimpleAdapter(
                context,
                sortedList,
                R.layout.date_time_setup_custom_list_item_2,
                from,
                to,
            )
        }

        private fun getTimeZoneIndex(adapter: SimpleAdapter, tz: TimeZone): Int {
            val defaultId = tz.id
            return (0 until adapter.count).firstOrNull {
                (adapter.getItem(it) as Map<*, *>)[KEY_ID] as String == defaultId
            } ?: -1
        }

        private fun setTimeZone(context: Context, tzId: String) {
            if (TimeZone.getDefault().id != tzId) {
                (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).setTimeZone(tzId)
            }
        }

        private fun setDate(context: Context, year: Int, month: Int, day: Int) {
            val calendar =
                Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                }
            setAlarmManagerTime(context, calendar.timeInMillis)
        }

        private fun setTime(context: Context, hourOfDay: Int, minute: Int) {
            val calendar =
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            setAlarmManagerTime(context, calendar.timeInMillis)
        }

        private fun setAlarmManagerTime(context: Context, whenMillis: Long) {
            if (whenMillis / 1000 < Int.MAX_VALUE) {
                (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).setTime(
                    whenMillis
                )
            }
        }
    }
}
