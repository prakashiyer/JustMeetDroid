package com.justmeet.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.justmeet.entity.Group;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by praxiyer on 08-02-2015.
 */
public class GroupDAO extends JMDatabaseHandler {

    private static final String GROUPS_TABLE = "groups";
    public static final String ID = "id";
    public static final String GROUP_ID = "group_id";
    public static final String NAME = "name";
    public static final String MEMBERS = "members";
    public static final String IMAGE = "image";
    public static final String ADMIN = "admin";

    /**
     * Constructor.
     *
     * @param context
     */
    public GroupDAO(Context context) {
        super(context);
    }

    public boolean addGroup(String groupId, String name, String members, byte[] image, String admin) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(GROUP_ID, groupId);
        values.put(NAME, name);
        values.put(MEMBERS, members);
        values.put(IMAGE, image);
        values.put(ADMIN, admin);

        Log.i("Inserting New Group", "Details: " + name + ", " + admin);
        // Inserting Row
        long id = db.insert(GROUPS_TABLE, null, values);
        db.close(); // Closing database connection
        if (id > -1) {
            Log.w("Inserting New Group", "New Group added successfully.");
            return true;
        }
        Log.w("Inserting New Group", "New Group addition failed.");
        return false;
    }

    public Group fetchGroup(String groupId) {
        String[] result_columns = new String[]{ID, GROUP_ID, NAME, MEMBERS, IMAGE, ADMIN};

        String where = GROUP_ID + "=?";
        String[] whereArgs = {groupId};
        String groupBy = null;
        String having = null;
        String order = null;

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Fetching Groups: ", "Group: " + groupId);
        Cursor cursor = db.query(GROUPS_TABLE, result_columns, where,
                whereArgs, groupBy, having, order);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(3);
            String members = cursor.getString(4);
            List<String> membersList = Arrays.asList(StringUtils.split(members, ","));
            byte[] image = cursor.getBlob(5);
            String admin = cursor.getString(6);

            return new Group(groupId, name, membersList, admin, image, false);
        }
        cursor.close();
        db.close();
        return null;
    }

    public List<Group> fetchGroups(String groupIds) {
        String[] result_columns = new String[]{ID, GROUP_ID, NAME, MEMBERS, IMAGE, ADMIN};

        String where = GROUP_ID + " in (?)";
        String[] whereArgs = {groupIds};
        String groupBy = null;
        String having = null;
        String order = null;

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Fetching Groups: ", "Size: " + groupIds);
        Cursor cursor = db.query(GROUPS_TABLE, result_columns, where,
                whereArgs, groupBy, having, order);
        List<Group> groups = new ArrayList<Group>();
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String groupId = cursor.getString(2);
                String name = cursor.getString(3);
                String members = cursor.getString(4);
                List<String> membersList = Arrays.asList(StringUtils.split(members, ","));
                byte[] image = cursor.getBlob(5);
                String admin = cursor.getString(6);

                Group group = new Group(groupId, name, membersList, admin, image, false);
                groups.add(group);
            }
        }
        cursor.close();
        db.close();
        return groups;
    }


    public boolean editGroup(String groupId, String name, byte[] image) {
        String where = GROUP_ID + "=?";
        String[] whereArgs = {groupId};
        ContentValues values = new ContentValues();
        values.put(NAME, name);
        values.put(IMAGE, image);

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Updating Group", "Details: " + groupId);
        int rows = db.update(GROUPS_TABLE, values, where, whereArgs);
        if (rows != 1) {
            Log.w("Group", "Update failed");
            return false;
        }
        return true;
    }

    public boolean updateGroupMembers(String groupId, String members) {
        String where = GROUP_ID + "=?";
        String[] whereArgs = {groupId};
        ContentValues values = new ContentValues();
        values.put(MEMBERS, members);

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Updating Group", "Details: " + groupId);
        int rows = db.update(GROUPS_TABLE, values, where, whereArgs);
        if (rows != 1) {
            Log.w("Group", "Update failed");
            return false;
        }
        return true;
    }

    public boolean updateGroupAdmin(String groupId, String phone) {
        String where = GROUP_ID + "=?";
        String[] whereArgs = {groupId};
        ContentValues values = new ContentValues();
        values.put(ADMIN, phone);

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Updating Group", "Details: " + groupId);
        int rows = db.update(GROUPS_TABLE, values, where, whereArgs);
        if (rows != 1) {
            Log.w("Group", "Update failed");
            return false;
        }
        return true;
    }

    public boolean deleteGroup(String groupId) {
        String where = GROUP_ID + "=?";
        String[] whereArgs = {groupId};
        ContentValues values = new ContentValues();

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Delete Group", "Details: " + groupId);
        int rows = db.delete(GROUPS_TABLE, where, whereArgs);
        if (rows != 1) {
            Log.w("Delete Group", "failed");
            return false;
        }
        return true;
    }
}
