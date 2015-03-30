package com.justmeet.justmeetdroid;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.justmeet.util.JMConstants;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccountManager am = AccountManager.get(this);
        setContentView(R.layout.activity_main);

        setTheme(R.style.AppTheme);
        ActionBar aBar = getActionBar();
        Log.i(TAG, "In Main Activity");
        Resources res = getResources();
        Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
        aBar.setBackgroundDrawable(actionBckGrnd);

        Account[] accounts = am.getAccountsByType(JMConstants.ACCOUNT_ADDRESS);
        if (accounts != null && accounts.length > 0) {
            Account account = accounts[0];
            SharedPreferences prefs = getSharedPreferences("Prefs", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("userName", am.getUserData(account, "userName"));
            editor.putString("phone", account.name);
            editor.apply();
            setTheme(R.style.AppTheme);
            Log.i(TAG, "Logging as an existing user: " + account.name);
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else {
            Log.i(TAG, "New User logs in");
            Intent intent = new Intent(this, NewUserActivity.class);
            startActivity(intent);
        }
    }
}
