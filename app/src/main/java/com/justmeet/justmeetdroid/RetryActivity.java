package com.justmeet.justmeetdroid;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

/**
 * Created by praxiyer on 03-02-2015.
 */
public class RetryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.retry);
        ActionBar aBar = getActionBar();
        Resources res = getResources();
        Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
        aBar.setBackgroundDrawable(actionBckGrnd);
        aBar.setTitle(" No network found ");

    }

    /**
     * Called when the user clicks the retry
     */
    public void goToMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // do nothing.
    }

}
