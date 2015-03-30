package com.justmeet.justmeetdroid;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

/**
 * Created by praxiyer on 01-02-2015.
 */
public class HomeActivity extends FragmentActivity {
    ViewPager Tab;
    ActionBar actionBar;
    TabPagerAdapter TabAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        TabAdapter = new TabPagerAdapter(getSupportFragmentManager());

        Tab = (ViewPager) findViewById(R.id.pager);

        Tab.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    ActionBar actionBar = getActionBar();

                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                });

        Tab.setAdapter(TabAdapter);
        actionBar = getActionBar();
        //Enable Tabs on Action Bar
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        Resources res = getResources();
        Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
        actionBar.setBackgroundDrawable(actionBckGrnd);
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {

            @Override
            public void onTabReselected(android.app.ActionBar.Tab tab,
                                        FragmentTransaction ft) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), "Tab selected", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {

                switch (tab.getPosition()) {
                    case 0:
                        //Fragement for HomePlans
                        actionBar.setTitle("Upcoming Plans");
                        UserPlansActivity homePlanFragment = new UserPlansActivity();
                        break;

                    case 1:
                        //Fragment for Groups Tab
                        actionBar.setTitle("Groups");
                        UserGroupsActivity grp = new UserGroupsActivity();
                        break;

                    case 2:
                        //Fragment for History
                        actionBar.setTitle("My Plans history");
                        UserHistoryActivity history = new UserHistoryActivity();
                        break;

                }
                Tab.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(android.app.ActionBar.Tab tab,
                                        FragmentTransaction ft) {
                // TODO Auto-generated method stub

            }
        };
        //Add New Tabs
        actionBar.addTab(actionBar.newTab().setText("Plans").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Groups").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("History").setTabListener(tabListener));

    }

    /**
     * Called when the user clicks the create home_view_plan button
     */
    public void newPlan(View view) {
        SharedPreferences prefs = getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("action", "newPlan");
        editor.apply();
        Intent intent = new Intent(this, ViewExistingMembersActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the user clicks the new group button
     */
    public void newGroup(View view) {
        SharedPreferences prefs = getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("action", "newGroup");
        editor.apply();
        Intent intent = new Intent(this, ViewExistingMembersActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem viewProfileItem = menu.findItem(R.id.viewProfile);
        viewProfileItem.setVisible(true);
        MenuItem aboutItem = menu.findItem(R.id.aboutUs);
        aboutItem.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case (R.id.viewProfile):
                Intent profileIntent = new Intent(this, EditMemberProfileActivity.class);
                startActivity(profileIntent);
                return true;
            case (R.id.aboutUs):
                Intent aboutIntent = new Intent(this, AboutUsActivity.class);
                startActivity(aboutIntent);
                return true;
            default:
                return false;
        }
    }
}
