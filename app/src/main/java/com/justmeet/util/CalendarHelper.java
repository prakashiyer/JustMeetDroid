package com.justmeet.util;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

public class CalendarHelper extends AsyncTask<String, String, String> {

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    public static final String[] EVENT_PROJECTION = new String[]{
            Calendars._ID,                           // 0
            Calendars.ACCOUNT_NAME,                  // 1
            Calendars.CALENDAR_DISPLAY_NAME,         // 2
            Calendars.OWNER_ACCOUNT                  // 3
    };


    public String name;
    public String id;
    private Context mContext;
    private ContentResolver cr;
    private ProgressDialog pDlg;
    private static final String DEBUG_TAG = "CalendarActivity";

    public CalendarHelper(Context mContext) {
        this.mContext = mContext;
        cr = mContext.getContentResolver();
    }

    public CalendarHelper(String _name, String _id) {
        name = _name;
        id = _id;
    }

    private void showProgressDialog() {

        pDlg = new ProgressDialog(mContext);
        pDlg.setMessage("Processing ....");
        pDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDlg.setCancelable(false);
        pDlg.show();

    }

    @Override
    protected void onPreExecute() {
        showProgressDialog();

    }

    private CalendarHelper m_calendars[];

    /**
     * Method to return all calendars in your android device.
     *
     * @return
     */
    private CalendarHelper getCalendars() {
        String[] l_projection = new String[]{"_id", "calendar_displayName"};
        Uri l_calendars;
        if (Build.VERSION.SDK_INT >= 8) {
            Log.i(DEBUG_TAG, "Android greater than 8");
            l_calendars = Uri.parse("content://com.android.calendar/calendars");
        } else {
            Log.i(DEBUG_TAG, "Android less than 8");
            l_calendars = Uri.parse("content://calendar/calendars");
        }
        Cursor cursor = cr.query(l_calendars, l_projection, null, null, null);    //all calendars
        if (cursor.moveToFirst()) {
            m_calendars = new CalendarHelper[cursor.getCount()];
            String l_calName;
            String l_calId;
            int l_cnt = 0;
            int l_nameCol = cursor.getColumnIndex(l_projection[1]);
            int l_idCol = cursor.getColumnIndex(l_projection[0]);
            do {
                l_calName = cursor.getString(l_nameCol);
                l_calId = cursor.getString(l_idCol);
                m_calendars[l_cnt] = new CalendarHelper(l_calName, l_calId);
                ++l_cnt;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return m_calendars[0];
    }

    @Override
    protected String doInBackground(String... params) {
        Log.i(DEBUG_TAG, "In doInBackground : ");
        getCalendars();
        long eventID = 0;
        Calendar calendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();


        if (params[3] == "prescription") {
            String date = params[0];
            int year = Integer.valueOf(date.substring(0, 4));
            int month = Integer.valueOf(date.substring(5, 7)) - 1;
            int day = Integer.valueOf(date.substring(8, 10));
            int hour = Integer.valueOf(date.substring(11, 13));
            int min = Integer.valueOf(date.substring(14, 16));
            calendar.set(year, month, day, hour, min);

            long setDate = calendar.getTimeInMillis();
            Log.i(DEBUG_TAG, "DATE: " + setDate);
            calendar.add(Calendar.MINUTE, 15);
            long end = calendar.getTimeInMillis();
            Log.i(DEBUG_TAG, "In addEvent()");
            ContentValues l_event = new ContentValues();
            l_event.put(Events.CALENDAR_ID, getCalendars().id);
            Log.i(DEBUG_TAG, "Calendar ID :" + getCalendars().id);
            l_event.put(Events.TITLE, params[1]);
            l_event.put(Events.DESCRIPTION, params[2]);
            l_event.put(Events.DTSTART, setDate);
            l_event.put(Events.DTEND, end);
            l_event.put("allDay", 0);
            l_event.put("eventStatus", 1);
            l_event.put(Events.RRULE, "FREQ=DAILY;COUNT=" + params[4]);
            l_event.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

            Uri l_eventUri;
            if (Build.VERSION.SDK_INT >= 8) {
                l_eventUri = Uri.parse("content://com.android.calendar/events");
            } else {
                l_eventUri = Uri.parse("content://calendar/events");
            }
            Uri l_uri = cr.insert(l_eventUri, l_event);
            Log.i(DEBUG_TAG, "URI to setCalendar : " + l_uri.toString());
            eventID = Long.parseLong(l_uri.getLastPathSegment());
            ContentValues reminders = new ContentValues();
            reminders.put(Reminders.EVENT_ID, eventID);
            reminders.put(Reminders.METHOD, Reminders.METHOD_ALERT);
            reminders.put(Reminders.MINUTES, 15);
            cr.insert(Reminders.CONTENT_URI, reminders);
        } else if (params[5] == "create") {
            String date = params[0];
            String endTime = params[6];
            String endDate = params[7];

            calendar.set(Integer.valueOf(date.substring(0, 4)),
                    (Integer.valueOf(date.substring(5, 7)) - 1),
                    Integer.valueOf(date.substring(8, 10)),
                    Integer.valueOf(date.substring(11, 13)),
                    Integer.valueOf(date.substring(14, 16)));
            endCalendar.set(Integer.valueOf(endDate.substring(0, 4)),
                    (Integer.valueOf(endDate.substring(5, 7)) - 1),
                    Integer.valueOf(endDate.substring(8, 10)),
                    Integer.valueOf(endTime.substring(0, 2)),
                    Integer.valueOf(endTime.substring(3, 5)));
            long setDate = calendar.getTimeInMillis();
            Log.i(DEBUG_TAG, "DATE: " + setDate);
            calendar.add(Calendar.HOUR, 5);
            long end = endCalendar.getTimeInMillis();
            Log.i(DEBUG_TAG, "In addEvent()");
            ContentValues l_event = new ContentValues();
            l_event.put(Events.CALENDAR_ID, getCalendars().id);
            Log.i(DEBUG_TAG, "Calendar ID :" + getCalendars().id);
            l_event.put(Events.TITLE, params[1]);
            l_event.put(Events.DESCRIPTION, params[2]);
            l_event.put(Events.DTSTART, setDate);
            l_event.put(Events.DTEND, end);
            l_event.put("allDay", 0);
            l_event.put("eventStatus", 1);
            l_event.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
            Uri l_eventUri;
            if (Build.VERSION.SDK_INT >= 8) {
                l_eventUri = Uri.parse("content://com.android.calendar/events");
            } else {
                l_eventUri = Uri.parse("content://calendar/events");
            }
            Uri l_uri = cr.insert(l_eventUri, l_event);
            Log.i(DEBUG_TAG, "URI to setCalendar : " + l_uri.toString());
            eventID = Long.parseLong(l_uri.getLastPathSegment());
            ContentValues reminders = new ContentValues();
            reminders.put(Reminders.EVENT_ID, eventID);
            reminders.put(Reminders.METHOD, Reminders.METHOD_ALERT);
            reminders.put(Reminders.MINUTES, 15);
            cr.insert(Reminders.CONTENT_URI, reminders);
        } else if (params[5] == "delete") {
            Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/events");
            int row = cr.delete(CALENDAR_URI, Events.TITLE + "=?", new String[]{params[1]});
        } else if (params[5] == "update") {
            Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/events");
            ContentValues newValues = new ContentValues();
            String newDate = params[6];
            String newTime = params[0];
            String newEndDate = params[8];
            String newEndTime = params[9];
            calendar.set(Integer.valueOf(newDate.substring(0, 4)),
                    (Integer.valueOf(newDate.substring(5, 7)) - 1),
                    Integer.valueOf(newDate.substring(8, 10)),
                    Integer.valueOf(newTime.substring(0, 2)),
                    Integer.valueOf(newTime.substring(3, 5)));

            endCalendar.set(Integer.valueOf(newEndDate.substring(0, 4)),
                    (Integer.valueOf(newEndDate.substring(5, 7)) - 1),
                    Integer.valueOf(newEndDate.substring(8, 10)),
                    Integer.valueOf(newEndTime.substring(0, 2)),
                    Integer.valueOf(newEndTime.substring(3, 5)));
            long setDate = calendar.getTimeInMillis();
            long end = endCalendar.getTimeInMillis();
            newValues.put(Events.TITLE, params[1]);
            newValues.put(Events.DESCRIPTION, params[2]);
            newValues.put(Events.DTSTART, setDate);
            newValues.put(Events.DTEND, end);
            int row = cr.update(CALENDAR_URI, newValues, Events.TITLE + "=?", new String[]{params[7]});
        }
        return String.valueOf(eventID);
    }

    @Override
    protected void onPostExecute(String response) {
        pDlg.dismiss();
    }
}
