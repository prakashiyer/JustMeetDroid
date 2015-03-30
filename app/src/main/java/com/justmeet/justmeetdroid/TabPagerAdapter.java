package com.justmeet.justmeetdroid;

/**
 * Created by Girish on 26-01-2015.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TabPagerAdapter extends FragmentStatePagerAdapter {
    public TabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                //Fragement for HomePlans
                return new UserPlansActivity();
            case 1:
                //Fragment for Groups Tab
                return new UserGroupsActivity();
            case 2:
                //Fragment for History
                return new UserHistoryActivity();
        }
        return null;

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return 3; //No of Tabs you can give your number of tabs
    }


}
