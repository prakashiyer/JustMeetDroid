package com.justmeet.justmeetdroid;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

/**
 * About us
 */
public class AboutUsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us);
        ActionBar aBar = getActionBar();
        Resources res = getResources();
        Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
        aBar.setBackgroundDrawable(actionBckGrnd);
        aBar.setTitle(" About Us");
    }

}
