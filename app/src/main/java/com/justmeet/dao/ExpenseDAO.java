package com.justmeet.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.justmeet.entity.Expense;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by praxiyer on 08-02-2015.
 */
public class ExpenseDAO extends JMDatabaseHandler {

    private static final String EXP_TABLE = "expenses";
    public static final String ID = "id";
    public static final String EXP_ID = "expense_id";
    public static final String EXP_TITLE = "title";
    public static final String EXP_VALUE = "value";
    public static final String EXP_PLAN_ID = "plan_id";
    public static final String EXP_PHONE = "phone";

    /**
     * Constructor.
     *
     * @param context
     */
    public ExpenseDAO(Context context) {
        super(context);
    }

    public boolean addExpense(String expenseId, String phone, String planIndex, String title,
                              int value) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(EXP_ID, expenseId);
        values.put(EXP_PHONE, phone);
        values.put(EXP_PLAN_ID, planIndex);
        values.put(EXP_TITLE, title);
        values.put(EXP_VALUE, value);

        Log.i("Inserting New Expense", "Details: " + planIndex + ", " + phone);
        // Inserting Row
        long id = db.insert(EXP_TABLE, null, values);
        db.close(); // Closing database connection
        if (id > -1) {
            Log.w("Inserting New Expense", "New Expense added successfully.");
            return true;
        }
        Log.w("Inserting New User", "New User addition failed.");
        return false;
    }

    public List<Expense> fetchExpense(String phone, String planId) {
        String[] result_columns = new String[]{ID, EXP_ID, EXP_PHONE, EXP_PLAN_ID, EXP_TITLE,
                EXP_VALUE};


        String where = EXP_PLAN_ID + "=? and " + EXP_PHONE + "=?";
        String[] whereArgs = {planId};
        String groupBy = null;
        String having = null;
        String order = null;

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Checking Expense", "Expense List: " + planId);
        Cursor cursor = db.query(EXP_TABLE, result_columns, where,
                whereArgs, groupBy, having, order);
        List<Expense> expenses = new ArrayList<Expense>();
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String expenseId = cursor.getString(2);
                String title = cursor.getString(5);
                String value = cursor.getString(6);
                Expense expense = new Expense(expenseId, phone, planId, title, value);
                expenses.add(expense);
            }
        }
        cursor.close();
        db.close();
        Log.w("Checking Expenses", "for plan");
        return expenses;
    }

    public void updateExpense(String expenseId, String title, int value) {
        String where = EXP_ID + "=?";
        String[] whereArgs = {expenseId};
        String groupBy = null;
        String having = null;
        String order = null;
        ContentValues values = new ContentValues();
        values.put(EXP_TITLE, title);
        values.put(EXP_VALUE, value);

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Updating Expense", "Details: " + expenseId);
        int rows = db.update(EXP_TABLE, values, where, whereArgs);
        if (rows != 1) {
            Log.w("Update Expense", "failed");
        }
    }

    public boolean deleteExpense(String expenseId) {
        String where = EXP_ID + "=?";
        String[] whereArgs = {expenseId};
        ContentValues values = new ContentValues();

        SQLiteDatabase db = this.getWritableDatabase();
        Log.w("Delete Expense", "Details: " + expenseId);
        int rows = db.delete(EXP_TABLE, where, whereArgs);
        if (rows != 1) {
            Log.w("Delete Expense", "failed");
            return false;
        }
        return true;
    }
}
