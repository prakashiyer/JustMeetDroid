package com.justmeet.justmeetdroid;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.justmeet.dao.GroupDAO;
import com.justmeet.dao.PlanDAO;
import com.justmeet.dao.UserDAO;
import com.justmeet.entity.Group;
import com.justmeet.entity.Plan;
import com.justmeet.entity.User;
import com.justmeet.util.JMConstants;
import com.justmeet.util.JMUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.List;

/**
 * Created by praxiyer on 01-02-2015.
 */
public class HomeGroupActivity extends FragmentActivity {
    ViewPager Tab;
    ActionBar actionBar;
    TabPagerAdapter TabAdapter;
    String selectedGroup;
    String selectedGroupIndex;
    String phone;
    Context context;
    String isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_group);
        TabAdapter = new TabPagerAdapter(getSupportFragmentManager());

        SharedPreferences prefs = getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        context = this;
        selectedGroup = prefs.getString("selectedGroup", "");
        selectedGroupIndex = prefs.getString("selectedGroupIndex", "");
        phone = prefs.getString("phone", "");

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
                        //Fragement for HomePlans
                        actionBar.setTitle("Upcoming Plans");
                        GroupPlansActivity homePlanFragment = new GroupPlansActivity();
                        break;

                    case 1:
                        //Fragment for Groups Tab
                        actionBar.setTitle("Members");
                        GroupMembersActivity grp = new GroupMembersActivity();
                        break;

                    case 2:
                        //Fragment for History
                        actionBar.setTitle("History");
                        GroupHistoryActivity history = new GroupHistoryActivity();
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
        actionBar.addTab(actionBar.newTab().setText("Plans").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Members").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("History").setTabListener(tabListener));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem editGroupItem = menu.findItem(R.id.editGroup);
        editGroupItem.setVisible(true);
        MenuItem leaveGroupItem = menu.findItem(R.id.leaveGroup);
        leaveGroupItem.setVisible(true);
        MenuItem aboutItem = menu.findItem(R.id.aboutUs);
        aboutItem.setVisible(true);
        SharedPreferences prefs = getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        isAdmin = prefs.getString("groupAdmin", "");
        if (isAdmin.equals("true")) {
            MenuItem deleteGroupItem = menu.findItem(R.id.deleteGroup);
            deleteGroupItem.setVisible(true);
            MenuItem inviteGroupItem = menu.findItem(R.id.inviteMembers);
            inviteGroupItem.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case (R.id.inviteMembers):
                Intent inviteIntent = new Intent(this, EditGroupActivity.class);
                startActivity(inviteIntent);
                return true;
            case (R.id.editGroup):
                Intent editPlanIntent = new Intent(this, EditGroupActivity.class);
                startActivity(editPlanIntent);
                return true;
            case (R.id.leaveGroup):

                AlertDialog.Builder ad = new AlertDialog.Builder(this);
                ad.setTitle("Exit group confirmation");
                ad.setMessage("Are you sure about exiting this group?");

                ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String updateQuery = "/leaveGroup?id="
                                + selectedGroupIndex + "&phone=" + phone;
                        GroupsClient restClient = new GroupsClient(context);
                        restClient.execute(new String[]{updateQuery});
                        GroupDAO groupDAO = new GroupDAO(context);
                        Group group = groupDAO.fetchGroup(selectedGroupIndex);
                        List<String> members = group.getMembers();
                        members.remove(phone);

                        groupDAO.updateGroupMembers(selectedGroupIndex,
                                JMUtil.listToCommaDelimitedString(members));
                        if (isAdmin.equals("true")) {
                            String newAdmin = members.get(0);
                            groupDAO.updateGroupAdmin(selectedGroupIndex, newAdmin);
                        }
                        PlanDAO planDAO = new PlanDAO(context);
                        List<Plan> plans = planDAO.fetchGroupPlanHistory(selectedGroupIndex);
                        if (plans != null && !plans.isEmpty()) {
                            for (Plan plan : plans) {
                                List<String> membersAttending = plan.getMembersAttending();
                                List<String> membersInvited = plan.getMembersInvited();
                                if (membersAttending != null && !membersAttending.isEmpty()) {
                                    membersAttending.remove(phone);
                                }
                                if (membersInvited != null && !membersInvited.isEmpty()) {
                                    membersInvited.remove(phone);
                                }

                                planDAO.updateMembers(plan.getId(),
                                        JMUtil.listToCommaDelimitedString(membersAttending),
                                        JMUtil.listToCommaDelimitedString(membersInvited));
                            }
                        }
                        Intent homeIntent = new Intent(context,
                                HomeActivity.class);
                        startActivity(homeIntent);
                    }
                });
                ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });
                ad.show();
                return true;
            case (R.id.deleteGroup):

                AlertDialog.Builder ad1 = new AlertDialog.Builder(this);
                ad1.setTitle("Delete group confirmation");
                ad1.setMessage("Are you sure about deleting this group?");

                ad1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String updateQuery = "/deleteGroup?id="
                                + selectedGroupIndex;
                        GroupsClient restClient = new GroupsClient(context);
                        restClient.execute(new String[]{updateQuery});
                        GroupDAO groupDAO = new GroupDAO(context);
                        Group group = groupDAO.fetchGroup(selectedGroupIndex);
                        if (group != null) {
                            List<String> members = group.getMembers();
                            if (members != null && !members.isEmpty()) {
                                UserDAO userDAO = new UserDAO(context);
                                for (String phone : members) {
                                    User user = userDAO.fetchUser(phone);
                                    List<String> groupIds = user.getGroupIds();
                                    groupIds.remove(selectedGroupIndex);

                                    userDAO.updateUserGroups(phone,
                                            JMUtil.listToCommaDelimitedString(groupIds));
                                }
                            }
                            PlanDAO planDAO = new PlanDAO(context);
                            List<Plan> plans = planDAO.fetchGroupUpcomingPlans(selectedGroupIndex);
                            if (plans != null && !plans.isEmpty()) {
                                for (Plan plan : plans) {
                                    List<String> groupIds = plan.getGroupsInvited();
                                    if (groupIds != null && !groupIds.isEmpty()) {
                                        groupIds.remove(selectedGroupIndex);
                                    }
                                    planDAO.updateGroups(plan.getId(),
                                            JMUtil.listToCommaDelimitedString(groupIds));
                                    List<String> membersInvited = plan.getMembersInvited();
                                    if (membersInvited != null && !membersInvited.isEmpty()) {
                                        membersInvited.removeAll(group.getMembers());
                                    }

                                    List<String> membersAttending = plan.getMembersAttending();
                                    if (membersAttending != null && !membersAttending.isEmpty()) {
                                        membersAttending.removeAll(group.getMembers());
                                    }
                                    planDAO.updateMembers(plan.getId(),
                                            JMUtil.listToCommaDelimitedString(membersAttending),
                                            JMUtil.listToCommaDelimitedString(membersInvited));


                                }

                            }
                            groupDAO.deleteGroup(selectedGroupIndex);
                        }


                        Intent homeIntent = new Intent(context,
                                HomeActivity.class);
                        startActivity(homeIntent);
                    }
                });
                ad1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });
                ad1.show();
                return true;
            case (R.id.aboutUs):
                Intent aboutIntent = new Intent(this, AboutUsActivity.class);
                startActivity(aboutIntent);
                return true;
            default:
                return false;
        }
    }

    private class GroupsClient extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private ProgressDialog pDlg;
        private String query;

        public GroupsClient(Context mContext) {
            this.mContext = mContext;
        }

        private void showProgressDialog() {

            pDlg = new ProgressDialog(mContext);
            pDlg.setMessage("Processing ....");
            pDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDlg.setCancelable(false);
            pDlg.show();

        }

        @Override
        protected void onPreExecute() {

            showProgressDialog();

        }

        @Override
        protected String doInBackground(String... params) {
            query = params[0];
            String path = JMConstants.SERVICE_PATH + query;

            HttpHost target = new HttpHost(JMConstants.TARGET_HOST, 8080);
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(path);
            HttpEntity results = null;

            try {
                HttpResponse response = client.execute(target, get);
                results = response.getEntity();
                String result = EntityUtils.toString(results);
                return result;
            } catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {

            pDlg.dismiss();
        }

    }
}
