package com.justmeet.justmeetdroid;

/**
 * Created by Girish on 26-01-2015.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TabPagerAdapter extends FragmentStatePagerAdapter {
    private String tag;
    public static final String HOME = "Home";
    public static final String VIEW_PLAN = "View Plan";
    public static final String VIEW_GROUP = "View Group";
    public static final String VIEW_HISTORY = "View History";

    public TabPagerAdapter(FragmentManager fm, String tag) {
        super(fm);
        this.tag = tag;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                //Fragement for HomePlans
                if(tag.equals(HOME)){
                    return new UserPlansActivity();
                } else if (tag.equals(VIEW_GROUP)) {
                    return new GroupPlansActivity();
                } else if (tag.equals(VIEW_HISTORY)) {
                    return new ViewOldPlanActivity();
                } else if (tag.equals(VIEW_PLAN)) {
                    return new ViewPlanActivity();
                }
                break;
            case 1:
                if(tag.equals(HOME)){
                    return new UserGroupsActivity();
                } else if (tag.equals(VIEW_GROUP)) {
                    return new GroupMembersActivity();
                } else if (tag.equals(VIEW_HISTORY) || tag.equals(VIEW_PLAN)) {
                    return new ViewMembersAttendingActivity();
                }
                break;
            case 2:
                if(tag.equals(HOME)){
                    return new UserHistoryActivity();
                } else if (tag.equals(VIEW_GROUP)) {
                    return new GroupHistoryActivity();
                } else if (tag.equals(VIEW_HISTORY)) {
                    return new ExpenseReportActivity();
                }
                break;
            case 3:
                if (tag.equals(VIEW_HISTORY)) {
                    return new AddExpenseActivity();
                }
                break;
        }
        return null;

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return 3; //No of Tabs you can give your number of tabs
    }


}
