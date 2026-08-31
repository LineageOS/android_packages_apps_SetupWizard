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
import android.text.format.DateFormat
import android.view.View
import android.widget.AdapterView
import android.widget.DatePicker
import android.widget.SimpleAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import com.android.settingslib.datetime.ZoneGetter
import java.util.Calendar
import java.util.TimeZone
import org.lineageos.setupwizard.R
import org.lineageos.setupwizard.base.BaseSetupWizardActivity
import org.lineageos.setupwizard.util.SetupWizardUtils

class DateTimeActivity :
    BaseSetupWizardActivity(),
    TimePickerDialog.OnTimeSetListener,
    DatePickerDialog.OnDateSetListener {

    private var currentTimeZone: TimeZone? = null
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView

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

        val spinner = findViewById<Spinner>(R.id.timezone_list)
        val adapter = constructTimezoneAdapter(this)
        currentTimeZone = TimeZone.getDefault()

        findViewById<View>(R.id.date_item).setOnClickListener { showDatePicker() }
        findViewById<View>(R.id.time_item).setOnClickListener { showTimePicker() }
        dateTextView = findViewById(R.id.date_text)
        timeTextView = findViewById(R.id.time_text)

        // Pre-select current/default timezone
        handler.post {
            val tzIndex = getTimeZoneIndex(adapter, currentTimeZone!!)
            spinner.adapter = adapter
            if (tzIndex != -1) {
                spinner.setSelection(tzIndex)
            }
            spinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        adapterView: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long,
                    ) {
                        val map = adapterView.getItemAtPosition(position) as Map<*, *>
                        val tzId = map[KEY_ID] as String
                        if (currentTimeZone != null && currentTimeZone!!.id != tzId) {
                            // Update the system timezone value
                            val alarm = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            alarm.setTimeZone(tzId)
                            currentTimeZone = TimeZone.getTimeZone(tzId)
                        }
                    }

                    override fun onNothingSelected(adapterView: AdapterView<*>) {}
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

    private fun updateTimeAndDateDisplay() {
        val shortDateFormat: java.text.DateFormat = DateFormat.getDateFormat(this)
        val now = Calendar.getInstance()
        timeTextView.text = DateFormat.getTimeFormat(this).format(now.time)
        dateTextView.text = shortDateFormat.format(now.time)
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
