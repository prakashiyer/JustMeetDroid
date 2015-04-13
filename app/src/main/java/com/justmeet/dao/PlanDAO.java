package com.justmeet.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.justmeet.entity.Plan;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by praxiyer on 08-02-2015.
 */
public class PlanDAO extends JMDatabaseHandler {

    private static final String PLANS_TABLE = "plans";
    public static final String ID = "id";
    public static final String PLAN_ID = "plan_id";
    public static final String TITLE = "title";
    public static final String START_TIME = "start_time";
    public static final String LOCATION = "location";
    public static final String MEMBERS_ATTENDING = "members_attending";
    public static final String CREATOR = "creator";
    public static final String END_TIME = "end_time";
    public static final String GROUPS_INVITED = "groups_invited";
    public static final String MEMBERS_INVITED = "members_invited";

    /**
     * Constructor.
     *
     * @param context
     */
    public PlanDAO(Context context) {
        super(context);
    }

    public boolean addPlan(String planId, String title, String startTime, String location,
                           String membersAttending, String creator, String endTime,
                           String groupsInvited, String membersInvited) {
        String[] result_columns = new String[]{ID, PLAN_ID, TITLE, START_TIME, LOCATION, MEMBERS_ATTENDING,
                CREATOR, END_TIME, GROUPS_INVITED, MEMBERS_INVITED};
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PLAN_ID, planId);
        values.put(TITLE, title);
        values.put(START_TIME, startTime);
        values.put(LOCATION, location);
        values.put(MEMBERS_ATTENDING, membersAttending);
        values.put(CREATOR, creator);
        values.put(END_TIME, endTime);
        values.put(GROUPS_INVITED, groupsInvited);
        values.put(MEMBERS_INVITED, membersInvited);

        Log.i("Inserting New Plan", "Details: " + title);
        // Inserting Row
        long id = db.insert(PLANS_TABLE, null, values);
        db.close(); // Closing database connection
        if (id > -1) {
            Log.w("Inserting New Plan", "New Plan added successfully.");
            return true;
        }
        Log.w("Inserting New plan", "New Plan addition failed.");
        return false;
    }

    public boolean editPlan(String planId, String title, String startTime, String location,
                            String endTime) {
        String where = PLAN_ID + "=?";
        String[] whereArgs = {planId};
        ContentValues values = new ContentValues();
        values.put(TITLE, title);
        values.put(START_TIME, startTime);
        values.put(LOCATION, location);
        values.put(END_TIME, endTime);

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Updating Plan", "Details: " + planId);
        int rows = db.update(PLANS_TABLE, values, where, whereArgs);
        if (rows != 1) {
            Log.w("Plan", "Update failed");
            return false;
        }
        return true;
    }

    public boolean deletePlan(String planId) {
        String where = PLAN_ID + "=?";
        String[] whereArgs = {planId};
        ContentValues values = new ContentValues();

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Updating Plan rsvp", "Details: " + planId);
        int rows = db.delete(PLANS_TABLE, where, whereArgs);
        if (rows != 1) {
            Log.w("Plan rsvp", "User Image upload has failed");
            return false;
        }
        return true;
    }

    public void rsvpPlan(String planId, String phone, String rsvp) {

        Plan plan = fetchPlan(planId);
        List<String> membersAttendingList = plan.getMembersAttending();
        if (membersAttendingList != null && !membersAttendingList.isEmpty()) {
            if (rsvp.equals("yes")) {
                membersAttendingList.add(phone);
            } else {
                membersAttendingList.remove(phone);
            }
        } else {
            membersAttendingList = new ArrayList<String>();
            if (rsvp.equals("yes")) {
                membersAttendingList.add(phone);
            }
        }
        StringBuffer stringBuff = new StringBuffer();
        int size = membersAttendingList.size();

        for (int i = 0; i < size; i++) {
            stringBuff.append(membersAttendingList.get(i));
            if (i != size - 1) {
                stringBuff.append(",");
            }
        }


        String where = PLAN_ID + "=?";
        String[] whereArgs = {planId};
        String groupBy = null;
        String having = null;
        String order = null;
        ContentValues values = new ContentValues();
        values.put(MEMBERS_ATTENDING, stringBuff.toString());

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Updating Plan rsvp", "Details: " + planId);
        int rows = db.update(PLANS_TABLE, values, where, whereArgs);
        if (rows != 1) {
            Log.w("Plan rsvp", "User Image upload has failed");
        }
    }

    public Plan fetchPlan(String planId) {
        String[] result_columns = new String[]{ID, PLAN_ID, TITLE, START_TIME, LOCATION, MEMBERS_ATTENDING,
                CREATOR, END_TIME, GROUPS_INVITED, MEMBERS_INVITED};


        String where = PLAN_ID + " = ? ";
        String[] whereArgs = {planId};
        String groupBy = null;
        String having = null;
        String order = null;

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Checking Plan", "Details: " + planId);
        Cursor cursor = db.query(PLANS_TABLE, result_columns, where,
                whereArgs, groupBy, having, order);
        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(PLAN_ID);
            int titleIndex = cursor.getColumnIndex(TITLE);
            int startIndex = cursor.getColumnIndex(START_TIME);
            int locationIndex = cursor.getColumnIndex(LOCATION);
            int membersAttendingIndex = cursor.getColumnIndex(MEMBERS_ATTENDING);
            int creatorIndex = cursor.getColumnIndex(CREATOR);
            int endIndex = cursor.getColumnIndex(END_TIME);
            int groupsInvitedIndex = cursor.getColumnIndex(GROUPS_INVITED);
            int membersInvitedIndex = cursor.getColumnIndex(MEMBERS_INVITED);

            String id = cursor.getString(idIndex);
            String title = cursor.getString(titleIndex);
            String start_time = cursor.getString(startIndex);
            String location = cursor.getString(locationIndex);
            String membersAttending = cursor.getString(membersAttendingIndex);
            List<String> membersAttendingList = Arrays.asList(StringUtils.split(membersAttending, ","));
            String creator = cursor.getString(creatorIndex);
            String end_time = cursor.getString(endIndex);
            String groupsInvited = cursor.getString(groupsInvitedIndex);
            List<String> groupsInvitedList = Arrays.asList(StringUtils.split(groupsInvited, ","));
            String membersInvited = cursor.getString(membersInvitedIndex);
            List<String> membersInvitedList = Arrays.asList(StringUtils.split(membersInvited, ","));

            return new Plan(id, title, start_time, location, membersAttendingList,
                    creator, end_time, groupsInvitedList, membersInvitedList);
        }
        cursor.close();
        db.close();
        Log.w("Checking Plan", "Plan null");
        return null;
    }

    public List<Plan> fetchUpcomingPlans(String phone) {
        String[] result_columns = new String[]{ID, PLAN_ID, TITLE, START_TIME, LOCATION, MEMBERS_ATTENDING,
                CREATOR, END_TIME, GROUPS_INVITED, MEMBERS_INVITED};

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int date = calendar.get(Calendar.DATE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        String strMon = String.valueOf(month);
        if (month < 10) {
            strMon = "0" + strMon;
        }
        String strdt = String.valueOf(date);
        if (date < 10) {
            strdt = "0" + strdt;
        }
        String strhr = String.valueOf(hour);
        if (hour < 10) {
            strhr = "0" + strhr;
        }
        String strMin = String.valueOf(min);
        if (min < 10) {
            strMin = "0" + strMin;
        }

        String startTime = String.valueOf(calendar.get(Calendar.YEAR)) + "-"
                + strMon + "-" + strdt + " " + strhr + ":" + strMin + ":00";

        String where = MEMBERS_ATTENDING + " like ? and " + START_TIME + " > ?";
        String[] whereArgs = {"%"+phone+"%", startTime};
        String groupBy = null;
        String having = null;
        String order = START_TIME + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Checking User", "Details: " + phone);
        Cursor cursor = db.query(PLANS_TABLE, result_columns, where,
                whereArgs, groupBy, having, order);
        List<Plan> plans = new ArrayList<Plan>();
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int idIndex = cursor.getColumnIndex(PLAN_ID);
                int titleIndex = cursor.getColumnIndex(TITLE);
                int startIndex = cursor.getColumnIndex(START_TIME);
                int locationIndex = cursor.getColumnIndex(LOCATION);
                int membersAttendingIndex = cursor.getColumnIndex(MEMBERS_ATTENDING);
                int creatorIndex = cursor.getColumnIndex(CREATOR);
                int endIndex = cursor.getColumnIndex(END_TIME);
                int groupsInvitedIndex = cursor.getColumnIndex(GROUPS_INVITED);
                int membersInvitedIndex = cursor.getColumnIndex(MEMBERS_INVITED);
                String id = cursor.getString(idIndex);
                Log.i("Plan", "id: " + id);
                String title = cursor.getString(titleIndex);
                Log.i("Plan", "title: " + title);
                String start_time = cursor.getString(startIndex);
                String location = cursor.getString(locationIndex);
                String membersAttending = cursor.getString(membersAttendingIndex);
                List<String> membersAttendingList = Arrays.asList(StringUtils.split(membersAttending, ","));
                String creator = cursor.getString(creatorIndex);
                String end_time = cursor.getString(endIndex);
                String groupsInvited = cursor.getString(groupsInvitedIndex);
                List<String> groupsInvitedList = Arrays.asList(StringUtils.split(groupsInvited, ","));
                String membersInvited = cursor.getString(membersInvitedIndex);
                List<String> membersInvitedList = Arrays.asList(StringUtils.split(membersInvited, ","));

                Plan plan = new Plan(id, title, start_time, location, membersAttendingList,
                        creator, end_time, groupsInvitedList, membersInvitedList);
                plans.add(plan);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        Log.w("Plans List", "No plans");
        return plans;
    }

    public List<Plan> fetchGroupUpcomingPlans(String groupId) {
        String[] result_columns = new String[]{ID, PLAN_ID, TITLE, START_TIME, LOCATION, MEMBERS_ATTENDING,
                CREATOR, END_TIME, GROUPS_INVITED, MEMBERS_INVITED};

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int date = calendar.get(Calendar.DATE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        String strMon = String.valueOf(month);
        if (month < 10) {
            strMon = "0" + strMon;
        }
        String strdt = String.valueOf(date);
        if (date < 10) {
            strdt = "0" + strdt;
        }
        String strhr = String.valueOf(hour);
        if (hour < 10) {
            strhr = "0" + strhr;
        }
        String strMin = String.valueOf(min);
        if (min < 10) {
            strMin = "0" + strMin;
        }

        String startTime = String.valueOf(calendar.get(Calendar.YEAR)) + "-"
                + strMon + "-" + strdt + " " + strhr + ":" + strMin + ":00";

        String where = GROUPS_INVITED + " like ? and " + START_TIME + " > ?";
        String[] whereArgs = {"%"+groupId+"%", startTime};
        String groupBy = null;
        String having = null;
        String order = START_TIME + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Checking Group", "Details: " + groupId);
        Cursor cursor = db.query(PLANS_TABLE, result_columns, where,
                whereArgs, groupBy, having, order);
        List<Plan> plans = new ArrayList<Plan>();
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int idIndex = cursor.getColumnIndex(PLAN_ID);
                int titleIndex = cursor.getColumnIndex(TITLE);
                int startIndex = cursor.getColumnIndex(START_TIME);
                int locationIndex = cursor.getColumnIndex(LOCATION);
                int membersAttendingIndex = cursor.getColumnIndex(MEMBERS_ATTENDING);
                int creatorIndex = cursor.getColumnIndex(CREATOR);
                int endIndex = cursor.getColumnIndex(END_TIME);
                int groupsInvitedIndex = cursor.getColumnIndex(GROUPS_INVITED);
                int membersInvitedIndex = cursor.getColumnIndex(MEMBERS_INVITED);
                String id = cursor.getString(idIndex);
                String title = cursor.getString(titleIndex);
                String start_time = cursor.getString(startIndex);
                String location = cursor.getString(locationIndex);
                String membersAttending = cursor.getString(membersAttendingIndex);
                List<String> membersAttendingList = Arrays.asList(StringUtils.split(membersAttending, ","));
                String creator = cursor.getString(creatorIndex);
                String end_time = cursor.getString(endIndex);
                String groupsInvited = cursor.getString(groupsInvitedIndex);
                List<String> groupsInvitedList = Arrays.asList(StringUtils.split(groupsInvited, ","));
                String membersInvited = cursor.getString(membersInvitedIndex);
                List<String> membersInvitedList = Arrays.asList(StringUtils.split(membersInvited, ","));

                Plan plan = new Plan(id, title, start_time, location, membersAttendingList,
                        creator, end_time, groupsInvitedList, membersInvitedList);
                plans.add(plan);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        Log.w("Plans List", "No plans");
        return plans;
    }

    public List<Plan> fetchPlanHistory(String phone) {
        String[] result_columns = new String[]{ID, PLAN_ID, TITLE, START_TIME, LOCATION, MEMBERS_ATTENDING,
                CREATOR, END_TIME, GROUPS_INVITED, MEMBERS_INVITED};

        Calendar endCal = Calendar.getInstance();
        int month = endCal.get(Calendar.MONTH) + 1;
        int date = endCal.get(Calendar.DATE);
        int hour = endCal.get(Calendar.HOUR_OF_DAY);
        int min = endCal.get(Calendar.MINUTE);
        String strMon = String.valueOf(month);
        if (month < 10) {
            strMon = "0" + strMon;
        }
        String strdt = String.valueOf(date);
        if (date < 10) {
            strdt = "0" + strdt;
        }
        String strhr = String.valueOf(hour);
        if (hour < 10) {
            strhr = "0" + strhr;
        }
        String strMin = String.valueOf(min);
        if (min < 10) {
            strMin = "0" + strMin;
        }
        String endTime = String.valueOf(endCal.get(Calendar.YEAR))
                + "-" + strMon
                + "-" + strdt
                + " " + strhr
                + ":" + strMin
                + ":00";
        endCal.add(Calendar.DATE, -14);

        int smonth = endCal.get(Calendar.MONTH) + 1;
        int sdate = endCal.get(Calendar.DATE);
        int shour = endCal.get(Calendar.HOUR_OF_DAY);
        int smin = endCal.get(Calendar.MINUTE);
        String strsMon = String.valueOf(smonth);
        if (smonth < 10) {
            strsMon = "0" + strsMon;
        }
        String strsdt = String.valueOf(sdate);
        if (sdate < 10) {
            strsdt = "0" + strsdt;
        }
        String strshr = String.valueOf(shour);
        if (hour < 10) {
            strshr = "0" + strshr;
        }
        String strsMin = String.valueOf(smin);
        if (min < 10) {
            strsMin = "0" + strsMin;
        }
        String startTime = String.valueOf(endCal.get(Calendar.YEAR))
                + "-" + strsMon
                + "-" + strsdt
                + " " + strshr
                + ":" + strsMin
                + ":00";

        String where = MEMBERS_ATTENDING + " like ? and " + START_TIME + " > ? and " + START_TIME + " < ?";
        String[] whereArgs = {"%"+phone+"%", startTime, endTime};
        String groupBy = null;
        String having = null;
        String order = START_TIME + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Checking User", "Details: " + phone);
        Cursor cursor = db.query(PLANS_TABLE, result_columns, where,
                whereArgs, groupBy, having, order);
        List<Plan> plans = new ArrayList<Plan>();
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int idIndex = cursor.getColumnIndex(PLAN_ID);
                int titleIndex = cursor.getColumnIndex(TITLE);
                int startIndex = cursor.getColumnIndex(START_TIME);
                int locationIndex = cursor.getColumnIndex(LOCATION);
                int membersAttendingIndex = cursor.getColumnIndex(MEMBERS_ATTENDING);
                int creatorIndex = cursor.getColumnIndex(CREATOR);
                int endIndex = cursor.getColumnIndex(END_TIME);
                int groupsInvitedIndex = cursor.getColumnIndex(GROUPS_INVITED);
                int membersInvitedIndex = cursor.getColumnIndex(MEMBERS_INVITED);
                String id = cursor.getString(idIndex);
                String title = cursor.getString(titleIndex);
                String start_time = cursor.getString(startIndex);
                String location = cursor.getString(locationIndex);
                String membersAttending = cursor.getString(membersAttendingIndex);
                List<String> membersAttendingList = Arrays.asList(StringUtils.split(membersAttending, ","));
                String creator = cursor.getString(creatorIndex);
                String end_time = cursor.getString(endIndex);
                String groupsInvited = cursor.getString(groupsInvitedIndex);
                List<String> groupsInvitedList = Arrays.asList(StringUtils.split(groupsInvited, ","));
                String membersInvited = cursor.getString(membersInvitedIndex);
                List<String> membersInvitedList = Arrays.asList(StringUtils.split(membersInvited, ","));

                Plan plan = new Plan(id, title, start_time, location, membersAttendingList,
                        creator, end_time, groupsInvitedList, membersInvitedList);
                plans.add(plan);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        Log.w("Plan List", "No List");
        return plans;
    }

    public List<Plan> fetchGroupPlanHistory(String groupId) {
        String[] result_columns = new String[]{ID, PLAN_ID, TITLE, START_TIME, LOCATION, MEMBERS_ATTENDING,
                CREATOR, END_TIME, GROUPS_INVITED, MEMBERS_INVITED};

        Calendar endCal = Calendar.getInstance();
        int month = endCal.get(Calendar.MONTH) + 1;
        int date = endCal.get(Calendar.DATE);
        int hour = endCal.get(Calendar.HOUR_OF_DAY);
        int min = endCal.get(Calendar.MINUTE);
        String strMon = String.valueOf(month);
        if (month < 10) {
            strMon = "0" + strMon;
        }
        String strdt = String.valueOf(date);
        if (date < 10) {
            strdt = "0" + strdt;
        }
        String strhr = String.valueOf(hour);
        if (hour < 10) {
            strhr = "0" + strhr;
        }
        String strMin = String.valueOf(min);
        if (min < 10) {
            strMin = "0" + strMin;
        }
        String endTime = String.valueOf(endCal.get(Calendar.YEAR))
                + "-" + strMon
                + "-" + strdt
                + " " + strhr
                + ":" + strMin
                + ":00";
        endCal.add(Calendar.DATE, -14);

        int smonth = endCal.get(Calendar.MONTH) + 1;
        int sdate = endCal.get(Calendar.DATE);
        int shour = endCal.get(Calendar.HOUR_OF_DAY);
        int smin = endCal.get(Calendar.MINUTE);
        String strsMon = String.valueOf(smonth);
        if (smonth < 10) {
            strsMon = "0" + strsMon;
        }
        String strsdt = String.valueOf(sdate);
        if (sdate < 10) {
            strsdt = "0" + strsdt;
        }
        String strshr = String.valueOf(shour);
        if (hour < 10) {
            strshr = "0" + strshr;
        }
        String strsMin = String.valueOf(smin);
        if (min < 10) {
            strsMin = "0" + strsMin;
        }
        String startTime = String.valueOf(endCal.get(Calendar.YEAR))
                + "-" + strsMon
                + "-" + strsdt
                + " " + strshr
                + ":" + strsMin
                + ":00";

        String where = GROUPS_INVITED + " like ? and " + START_TIME + " > ? and " + START_TIME + " < ?";
        String[] whereArgs = {"%"+groupId+"%", startTime, endTime};
        String groupBy = null;
        String having = null;
        String order = START_TIME + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Checking Plans History", "Group Id: " + groupId);
        Cursor cursor = db.query(PLANS_TABLE, result_columns, where,
                whereArgs, groupBy, having, order);
        List<Plan> plans = new ArrayList<Plan>();
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int idIndex = cursor.getColumnIndex(PLAN_ID);
                int titleIndex = cursor.getColumnIndex(TITLE);
                int startIndex = cursor.getColumnIndex(START_TIME);
                int locationIndex = cursor.getColumnIndex(LOCATION);
                int membersAttendingIndex = cursor.getColumnIndex(MEMBERS_ATTENDING);
                int creatorIndex = cursor.getColumnIndex(CREATOR);
                int endIndex = cursor.getColumnIndex(END_TIME);
                int groupsInvitedIndex = cursor.getColumnIndex(GROUPS_INVITED);
                int membersInvitedIndex = cursor.getColumnIndex(MEMBERS_INVITED);
                String id = cursor.getString(idIndex);
                String title = cursor.getString(titleIndex);
                String start_time = cursor.getString(startIndex);
                String location = cursor.getString(locationIndex);
                String membersAttending = cursor.getString(membersAttendingIndex);
                List<String> membersAttendingList = Arrays.asList(StringUtils.split(membersAttending, ","));
                String creator = cursor.getString(creatorIndex);
                String end_time = cursor.getString(endIndex);
                String groupsInvited = cursor.getString(groupsInvitedIndex);
                List<String> groupsInvitedList = Arrays.asList(StringUtils.split(groupsInvited, ","));
                String membersInvited = cursor.getString(membersInvitedIndex);
                List<String> membersInvitedList = Arrays.asList(StringUtils.split(membersInvited, ","));

                Plan plan = new Plan(id, title, start_time, location, membersAttendingList,
                        creator, end_time, groupsInvitedList, membersInvitedList);
                plans.add(plan);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        Log.w("Plan List", "No List");
        return plans;
    }

    public boolean updateMembers(String id, String membersAttending, String membersInvited) {
        String where = PLAN_ID + "=?";
        String[] whereArgs = {id};
        ContentValues values = new ContentValues();
        values.put(MEMBERS_ATTENDING, membersAttending);
        values.put(MEMBERS_INVITED, membersInvited);

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Updating Plan", "Details: " + id);
        int rows = db.update(PLANS_TABLE, values, where, whereArgs);
        if (rows != 1) {
            Log.w("Plan", "Update failed");
            return false;
        }
        return true;
    }

    public boolean updateGroups(String id, String groups) {
        String where = PLAN_ID + "=?";
        String[] whereArgs = {id};
        ContentValues values = new ContentValues();
        values.put(GROUPS_INVITED, groups);

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Updating Plan", "Details: " + id);
        int rows = db.update(PLANS_TABLE, values, where, whereArgs);
        if (rows != 1) {
            Log.w("Plan", "Update failed");
            return false;
        }
        return true;
    }
}
