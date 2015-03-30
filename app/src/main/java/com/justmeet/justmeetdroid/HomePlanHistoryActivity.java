package com.justmeet.justmeetdroid;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

/**
 * Created by praxiyer on 01-02-2015.
 */
public class HomePlanHistoryActivity extends FragmentActivity {
    ViewPager Tab;
    ActionBar actionBar;
    TabPagerAdapter TabAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_plan_history);
        TabAdapter = new TabPagerAdapter(getSupportFragmentManager());

        Tab = (ViewPager) findViewById(R.id.pager2);

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
            public void onTabReselected(ActionBar.Tab tab,
                                        FragmentTransaction ft) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), "Tab selected", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {

                switch (tab.getPosition()) {
                    case 0:
                        //Fragement for View Plan
                        actionBar.setTitle("Plan Details");
                        ViewOldPlanActivity viewPlanFragment = new ViewOldPlanActivity();
                        break;

                    case 1:
                        //Fragment for Members Attending
                        actionBar.setTitle("Attendees");
                        ViewMembersAttendingActivity grp = new ViewMembersAttendingActivity();
                        break;

                    case 2:
                        //Fragment for Expense Report
                        actionBar.setTitle("Expense Report");
                        ExpenseReportActivity er = new ExpenseReportActivity();
                        break;

                    case 3:
                        //Fragment for Expense Report
                        actionBar.setTitle("Add Expense");
                        AddExpenseActivity ae = new AddExpenseActivity();
                        break;

                }
                Tab.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab,
                                        FragmentTransaction ft) {
                // TODO Auto-generated method stub

            }
        };
        //Add New Tabs
        actionBar.addTab(actionBar.newTab().setText("Plan Details").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Attendees").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Report").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Add Expense").setTabListener(tabListener));

    }
}
