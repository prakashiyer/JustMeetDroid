package com.justmeet.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.justmeet.entity.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by praxiyer on 08-02-2015.
 */
public class UserDAO extends JMDatabaseHandler {

    private static final String USER_TABLE = "user_information";
    public static final String USER_ID = "id";
    public static final String USER_NAME = "name";
    public static final String USER_PHONE = "phone";
    public static final String USER_IMAGE = "image";
    public static final String USER_GROUPS_IDS = "groups_ids";

    /**
     * Constructor.
     *
     * @param context
     */
    public UserDAO(Context context) {
        super(context);
    }

    /**
     * Add user details
     *
     * @param inName
     * @param inPhone
     * @return
     */
    public boolean addUser(String inName, String inPhone) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USER_NAME, inName);
        values.put(USER_PHONE, inPhone);

        Log.i("Inserting New User", "Details: " + inName + ", " + inPhone);
        // Inserting Row
        long id = db.insert(USER_TABLE, null, values);
        db.close(); // Closing database connection
        if (id > -1) {
            Log.w("Inserting New User", "New User added successfully.");
            return true;
        }
        Log.w("Inserting New User", "New User addition failed.");
        return false;
    }

    public User fetchUser(String phone) {
        String[] result_columns = new String[]{USER_ID, USER_NAME, USER_PHONE,
                USER_IMAGE, USER_GROUPS_IDS};

        String where = USER_PHONE + "=?";
        String[] whereArgs = {phone};
        String groupBy = null;
        String having = null;
        String order = null;

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Checking User", "Details: " + phone);
        Cursor cursor = db.query(USER_TABLE, result_columns, where,
                whereArgs, groupBy, having, order);
        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(USER_ID);
            int nameIndex = cursor.getColumnIndex(USER_NAME);
            int imageIndex = cursor.getColumnIndex(USER_IMAGE);
            int groupIdsIndex = cursor.getColumnIndex(USER_GROUPS_IDS);
            int id = cursor.getInt(idIndex);
            String name = cursor.getString(nameIndex);
            byte[] image = cursor.getBlob(imageIndex);
            String groupIds = cursor.getString(groupIdsIndex);
            Log.w("Checking Groups", "Details: "+groupIds);
            List<String> groups = null;
            if (groupIds != null && !groupIds.equals("")) {
                groups = Arrays.asList(groupIds.split(","));
            }
            return new User(id, name, phone, groups, image, false);

        }

        cursor.close();
        db.close();
        Log.w("Checking Old User", "User Not authorized.");
        return null;
    }

    public void updateUserImage(String phone, byte[] image) {
        String where = USER_PHONE + "=?";
        String[] whereArgs = {phone};
        String groupBy = null;
        String having = null;
        String order = null;
        ContentValues values = new ContentValues();
        values.put(USER_IMAGE, image);

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Updating Image User", "Details: " + phone);
        int rows = db.update(USER_TABLE, values, where, whereArgs);
        if (rows != 1) {
            Log.w("User Image Upload", "User Image upload has failed");
        }
        db.close();
    }

    public void updateUserGroups(String phone, String groups) {
        String where = USER_PHONE + "=?";
        String[] whereArgs = {phone};
        String groupBy = null;
        String having = null;
        String order = null;
        ContentValues values = new ContentValues();
        values.put(USER_GROUPS_IDS, groups);

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Updating Image User", "Details: " + phone);
        int rows = db.update(USER_TABLE, values, where, whereArgs);
        if (rows != 1) {
            Log.w("User Image Upload", "User Image upload has failed");
        }
        db.close();
    }

    public void updateUser(String phone, byte[] image, String name) {
        String where = USER_PHONE + "=?";
        String[] whereArgs = {phone};
        String groupBy = null;
        String having = null;
        String order = null;
        ContentValues values = new ContentValues();
        values.put(USER_IMAGE, image);
        values.put(USER_NAME, name);

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Updating Image User", "Details: " + phone);
        int rows = db.update(USER_TABLE, values, where, whereArgs);
        if (rows != 1) {
            Log.w("User Image Upload", "User Image upload has failed");
        }
        db.close();
    }

    public List<User> fetchUsers(List<String> phoneList) {
        String[] result_columns = new String[]{USER_ID, USER_NAME, USER_PHONE,
                USER_IMAGE, USER_GROUPS_IDS};

        StringBuffer stringBuff = new StringBuffer();
        int size = phoneList.size();

        for (int i = 0; i < size; i++) {
            stringBuff.append("'");
            stringBuff.append(phoneList.get(i));
            stringBuff.append("'");
            if (i != size - 1) {
                stringBuff.append(",");
            }
        }

        String where = USER_PHONE + " in (?)";
        String[] whereArgs = {stringBuff.toString()};
        String groupBy = null;
        String having = null;
        String order = null;

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Checking User", "Users List: " + phoneList.size());
        Cursor cursor = db.query(USER_TABLE, result_columns, where,
                whereArgs, groupBy, having, order);
        List<User> users = new ArrayList<User>();
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int idIndex = cursor.getColumnIndex(USER_ID);
                int nameIndex = cursor.getColumnIndex(USER_NAME);
                int phoneIndex = cursor.getColumnIndex(USER_PHONE);
                int imageIndex = cursor.getColumnIndex(USER_IMAGE);
                int groupIdsIndex = cursor.getColumnIndex(USER_GROUPS_IDS);
                int id = cursor.getInt(idIndex);
                String name = cursor.getString(nameIndex);
                String phone = cursor.getString(phoneIndex);
                byte[] image = cursor.getBlob(imageIndex);
                String groupIds = cursor.getString(groupIdsIndex);
                List<String> groups = null;
                if (!groupIds.equals("")) {
                    groups = Arrays.asList(groupIds.split(","));
                }
                User user = new User(id, name, phone, groups, image, false);
                users.add(user);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        Log.w("Checking Users", "User Not authorized.");
        return users;
    }

}
