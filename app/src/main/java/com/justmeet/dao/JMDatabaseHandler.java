package com.justmeet.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by praxiyer on 08-02-2015.
 */
public class JMDatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "whatsThePlan.db";
    private static final int DATABASE_VERSION = 1;

    // SQL Statement to create Database
    private static final String USER_TABLE_CREATE = "CREATE TABLE user_information ( " +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "  name TEXT NOT NULL, " +
            "  phone TEXT NOT NULL UNIQUE, " +
            "  image BLOB, " +
            "  groups_ids TEXT " +
            "); ";

    private static final String PLANS_TABLE_CREATE = "CREATE TABLE plans ( " +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "  plan_id TEXT NOT NULL, " +
            "  title TEXT NOT NULL, " +
            "  start_time TEXT, " +
            "  location TEXT, " +
            "  members_attending TEXT, " +
            "  creator TEXT, " +
            "  end_time TEXT, " +
            "  groups_invited TEXT, " +
            "  members_invited TEXT " +
            "); ";

    private static final String GROUPS_TABLE_CREATE = "CREATE TABLE groups ( " +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "  group_id TEXT NOT NULL, " +
            "  name TEXT NOT NULL, " +
            "  members TEXT, " +
            "  image BLOB, " +
            "  admin TEXT" +
            "); ";

    private static final String EXPENSES_TABLE_CREATE = "CREATE TABLE expenses ( " +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "  expense_id TEXT NOT NULL, " +
            "  phone TEXT," +
            "  plan_id TEXT, " +
            "  title TEXT NOT NULL, " +
            "  value INTEGER" +
            "); ";

    public JMDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.w("Table Creation", "Creating Table with query " + USER_TABLE_CREATE);
        db.execSQL(USER_TABLE_CREATE);
        Log.w("Table Creation", "Creating Table with query " + PLANS_TABLE_CREATE);
        db.execSQL(PLANS_TABLE_CREATE);
        Log.w("Table Creation", "Creating Table with query " + GROUPS_TABLE_CREATE);
        db.execSQL(GROUPS_TABLE_CREATE);
        Log.w("Table Creation", "Creating Table with query " + EXPENSES_TABLE_CREATE);
        db.execSQL(EXPENSES_TABLE_CREATE);
        Log.w("Table Creation", "Table creation complete... ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Log the version update
        Log.w("TaskDBAdapter", "Upgrading from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");

        //TODO db.execSQL("DROP TABLE IF IT EXISTS " + USER_TABLE);
        onCreate(db);
    }

}
