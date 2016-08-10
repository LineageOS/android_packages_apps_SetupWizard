/*
 * Copyright (C) 2014 The CyanogenMod Project
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

package com.cyanogenmod.setupwizard.cmstats;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;

import java.util.LinkedList;


public class SetupStats {

    private static final String TAG = SetupStats.class.getSimpleName();

    private static final String ANALYTIC_INTENT = "com.cyngn.stats.action.SEND_ANALYTIC_EVENT";
    private static final String ANALYTIC_PERMISSION = "com.cyngn.stats.SEND_ANALYTICS";

    public static final String TRACKING_ID = "tracking_id";

    private final LinkedList<Event> mEvents = new LinkedList<Event>();

    private static final SetupStats sInstance = new SetupStats();

    private static final boolean DEBUG = false;

    private SetupStats() {}

    public static void addEvent(String category, String action,
            String label, String value) {
        sInstance.mEvents.add(new Event(category, action, label, value));
    }

    public static void addEvent(String category, String action) {
        sInstance.mEvents.add(new Event(category, action, null, null));
    }

    public static void sendEvents(Context context) {
        while (!sInstance.mEvents.isEmpty()) {
            sInstance.sendEvent(context, sInstance.mEvents.remove());
        }
    }

    private void sendEvent(Context context, Event event) {

        if (!StatsUtils.isStatsPackageInstalled(context)
                || !StatsUtils.isStatsCollectionEnabled(context)) {
            return;
        }

        // Create new intent
        Intent intent = new Intent();
        intent.setAction(ANALYTIC_INTENT);

        // add tracking id
        intent.putExtra(TRACKING_ID, context.getPackageName());
        // append
        intent.putExtra(Fields.EVENT_CATEGORY, event.category);
        if (DEBUG) Log.d(TAG, Fields.EVENT_CATEGORY + "=" + event.category);
        intent.putExtra(Fields.EVENT_ACTION, event.action);
        if (DEBUG) Log.d(TAG, Fields.EVENT_ACTION + "=" + event.action);
        // check if exist
        if (event.label != null) {
            intent.putExtra(Fields.EVENT_LABEL, event.label);
            if (DEBUG) Log.d(TAG, Fields.EVENT_LABEL + "=" + event.label);
        }

        if (event.value != null) {
            intent.putExtra(Fields.EVENT_VALUE, event.value);
            if (DEBUG) Log.d(TAG, Fields.EVENT_VALUE + "=" + event.value);
        }

        // broadcast for internal package
        context.sendBroadcastAsUser(intent,
                new UserHandle(UserHandle.USER_CURRENT), ANALYTIC_PERMISSION);
    }

    private static final class Event {
        private final String category;
        private final String action;
        private final String label;
        private final String value;

        public Event(String category, String action, String label, String value) {
            this.action = action;
            this.category = category;
            this.label = label;
            this.value = value;
        }
    }

    public static final class Fields {
        public static final String EVENT_CATEGORY = "category";
        public static final String EVENT_ACTION = "action";
        public static final String EVENT_LABEL = "label";
        public static final String EVENT_VALUE = "value";
    }

    public static final class Categories {
        public static final String APP_LAUNCH = "app_launch";
        public static final String APP_FINISHED = "app_finish";
        public static final String PAGE_LOAD = "page_load";
        public static final String EXTERNAL_PAGE_LOAD = "external_page_load";
        public static final String BUTTON_CLICK = "button_click";
        public static final String SETTING_CHANGED = "setting_changed";
    }

    public static final class Action {
        public static final String PAGE_LOADED = "page_loaded";
        public static final String PREVIOUS_BUTTON = "previous_button";
        public static final String NEXT_BUTTON = "next_button";
        public static final String CHANGE_LOCALE = "change_local";
        public static final String EXTERNAL_PAGE_LAUNCH = "external_page_launch";
        public static final String EXTERNAL_PAGE_RESULT = "external_page_result";
        public static final String ENABLE_MOBILE_DATA = "enable_mobile_data";
        public static final String PREFERRED_DATA_SIM = "preferred_data_sim";
        public static final String APPLY_CUSTOM_THEME = "apply_custom_theme";
        public static final String USE_SECURE_SMS = "use_secure_sms";
        public static final String ENABLE_BACKUP = "enable_backup";
        public static final String ENABLE_NAV_KEYS = "enable_nav_keys";
        public static final String ENABLE_LOCATION = "enable_location";
        public static final String ENABLE_NETWORK_LOCATION = "enable_network_location";
        public static final String ENABLE_GPS_LOCATION = "enable_gps_location";
        public static final String DATE_CHANGED = "date_changed";
        public static final String TIME_CHANGED = "time_changed";
        public static final String TIMEZONE_CHANGED = "timezone_changed";
    }

    public static final class Label {
        public static final String PAGE = "page";
        public static final String LOCALE = "local";
        public static final String RESULT = "result";
        public static final String WIFI_SETUP = "wifi_setup";
        public static final String BLUETOOTH_SETUP = "bluetooth_setup";
        public static final String CYANOGEN_ACCOUNT = "cyanogen_account_setup";
        public static final String CAPTIVE_PORTAL_LOGIN = "captive_portal_login";
        public static final String EMERGENCY_CALL = "emergency_call";
        public static final String GMS_ACCOUNT = "gms_account";
        public static final String RESTORE = "restore";
        public static final String CHECKED = "checked";
        public static final String VALUE = "value";
        public static final String SLOT = "slot";
        public static final String TOTAL_TIME = "total_time";
        public static final String FINGERPRINT_SETUP = "fingerprint_setup";
        public static final String LOCKSCREEN_SETUP = "lockscreen_setup";
    }

}
