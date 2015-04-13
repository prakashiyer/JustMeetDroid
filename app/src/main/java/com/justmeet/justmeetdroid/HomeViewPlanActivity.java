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
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.justmeet.dao.PlanDAO;
import com.justmeet.entity.Plan;
import com.justmeet.util.CalendarHelper;
import com.justmeet.util.JMConstants;
import com.justmeet.util.JMUtil;
import com.thoughtworks.xstream.XStream;

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
public class HomeViewPlanActivity extends FragmentActivity {
    private static final String TAG = "Home View Plan";
    private ViewPager Tab;
    private ActionBar actionBar;
    private TabPagerAdapter TabAdapter;
    private String selectedPlanIndex;
    private String selectedPlan;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_view_plan);
        TabAdapter = new TabPagerAdapter(getSupportFragmentManager(),TabPagerAdapter.VIEW_PLAN);
        context = this;

        SharedPreferences prefs = getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        selectedPlan = prefs.getString("selectedPlan", "");
        selectedPlanIndex = prefs.getString("selectedPlanIndex", "");

        Tab = (ViewPager) findViewById(R.id.pager1);

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
                        break;

                    case 1:
                        //Fragment for Members Attending
                        actionBar.setTitle("Attendees");
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

    }

    /**
     * Called when the user clicks the rsvp home_view_plan button
     */
    public void rsvpPlan(View view) {
        TextView rsvpLabel = (TextView) this.findViewById(R.id.rsvpLabel);
        Button rsvpPlanButton = (Button) this.findViewById(R.id.rsvpPlanButton);
        rsvpPlanButton.setTextColor(getResources().getColor(
                R.color.click_button_2));
        SharedPreferences prefs = this.getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        String phone = prefs.getString("phone", "");
        String selectedPlan = prefs.getString("selectedPlan", "");
        String selectedPlanIndex = prefs.getString("selectedPlanIndex", "");
        String rsvp = "no";

        if (rsvpPlanButton.getText().equals("Say Yes")) {
            rsvp = "yes";
        }

        String updateQuery = "/rsvpPlan?planIndex=" + selectedPlanIndex + "&phone=" + phone
                + "&rsvp=" + rsvp;
        if (rsvp.equals("no")) {
            CalendarHelper calendarHelper = new CalendarHelper(this);
            calendarHelper.execute(new String[]{"",
                    selectedPlan, "", "", "", "delete"});
            Log.i("Plan", "Plan deleted.....");
            rsvpPlanButton.setVisibility(Button.VISIBLE);
            rsvpLabel.setVisibility(View.VISIBLE);
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else {
            CalendarHelper calendarHelper = new CalendarHelper(this);
            String[] startPlanTime = null;
            PlanDAO planDAO = new PlanDAO(this);
            Plan plan = planDAO.fetchPlan(selectedPlanIndex);
            String planTime = plan.getStartTime();
            if (planTime != null) {
                startPlanTime = JMUtil.createGmtToLocalTime(planTime);
            }
            String[] endPlanTime = null;
            String endTime = plan.getEndTime();
            if (endTime != null) {
                endPlanTime = JMUtil.createGmtToLocalTime(planTime);
            }
            calendarHelper.execute(new String[]{startPlanTime[0] + " " + startPlanTime[1],
                    selectedPlan, plan.getLocation(),
                    String.valueOf(plan.getId()), phone, "create", endPlanTime[1], endPlanTime[0]});
        }
        ViewPlanClient restClient = new ViewPlanClient(this);
        restClient.execute(new String[]{updateQuery, phone});
        PlanDAO planDAO = new PlanDAO(this);
        planDAO.rsvpPlan(selectedPlanIndex, phone, rsvp);
    }

    private class ViewPlanClient extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private ProgressDialog pDlg;
        private String phone;
        private String query;

        public ViewPlanClient(Context mContext) {
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
            if (query.contains("fetchPlan") || query.contains("rsvpPlan")) {
                Log.i("REQUEST", query);
                phone = params[1];
            }

            // HttpHost target = new HttpHost(TARGET_HOST);
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
            if (response != null && query.equals("rsvpPlan")) {
                XStream xstream = new XStream();
                xstream.alias("Plan", Plan.class);
                xstream.alias("membersAttending", String.class);
                xstream.addImplicitCollection(Plan.class, "membersAttending", "membersAttending", String.class);
                xstream.alias("membersInvited", String.class);
                xstream.addImplicitCollection(Plan.class, "membersInvited", "membersInvited", String.class);
                xstream.alias("groupsInvited", String.class);
                xstream.addImplicitCollection(Plan.class, "groupsInvited", "groupsInvited", String.class);
                Plan plan = (Plan) xstream.fromXML(response);
                if (plan != null) {
                    populatePlanInformation(phone, plan);
                }
            }
            pDlg.dismiss();
        }
    }

    private void populatePlanInformation(String phone, Plan plan) {
        TextView planLocation = (TextView) this.findViewById(R.id.viewPlanLocation);
        planLocation.setText(plan.getLocation());

        TextView planTimeValue = (TextView) this.findViewById(R.id.viewPlanTime);
        TextView planEndTimeValue = (TextView) this.findViewById(R.id.viewPlanEndTime);

        String date = plan.getStartTime().substring(0, 10);
        String time = plan.getStartTime().substring(11, 16);
        String[] postedLocalDate = JMUtil
                .createGmtToLocalTime(date + " " + time + ":00");
        date = postedLocalDate[0];
        time = postedLocalDate[1];
        String hour = time.substring(0, 2);
        String min = time.substring(3, 5);
        String endDate = plan.getEndTime().substring(0, 10);
        String endTime = plan.getEndTime().substring(11, 16);
        String[] endLocalDate = JMUtil
                .createGmtToLocalTime(endDate + " " + endTime
                        + ":00");
        endDate = endLocalDate[0];
        endTime = endLocalDate[1];
        String endHour = endTime.substring(0, 2);
        String endMin = endTime.substring(3, 5);
        int hourInt = Integer.valueOf(hour);
        String ampm = "AM";
        if (hourInt > 12) {
            hour = String.valueOf(hourInt - 12);
            if (Integer.valueOf(hour) < 10) {
                hour = "0" + hour;
            }
            ampm = "PM";
        }
        int endHourInt = Integer.valueOf(endHour);
        String endAmPm = "AM";
        if (endHourInt > 12) {
            endHour = String.valueOf(endHourInt - 12);
            if (Integer.valueOf(endHour) < 10) {
                endHour = "0" + endHour;
            }
            endAmPm = "PM";
        }
        planTimeValue.setText(" " + date + " " + hour + ":" + min
                + " " + ampm);

        planEndTimeValue.setText(" " + endDate + " " + endHour
                + ":" + endMin + " " + endAmPm);

        List<String> membersAttending = plan.getMembersAttending();
        Button rsvpPlanButton = (Button) this.findViewById(R.id.rsvpPlanButton);
        TextView rsvpLabel = (TextView) this.findViewById(R.id.rsvpLabel);
        if (membersAttending != null && !membersAttending.isEmpty()
                && membersAttending.contains(phone)) {
            rsvpPlanButton.setVisibility(Button.VISIBLE);
            rsvpLabel
                    .setText("You are going, Click here to");
            rsvpPlanButton.setText("Say No");
        } else {
            rsvpPlanButton.setVisibility(Button.VISIBLE);
            rsvpLabel
                    .setText("Are you attending? Click here to");
            rsvpPlanButton.setText("Say Yes");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem editPlanItem = menu.findItem(R.id.editPlan);
        editPlanItem.setVisible(true);
        MenuItem aboutItem = menu.findItem(R.id.aboutUs);
        aboutItem.setVisible(true);
        SharedPreferences prefs = getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        String isAdmin = prefs.getString("planAdmin", "");
        if (isAdmin.equals("true")) {
            MenuItem deletePlanItem = menu.findItem(R.id.deletePlan);
            deletePlanItem.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case (R.id.editPlan):
                Intent editPlanIntent = new Intent(this, EditAppointmentActivity.class);
                startActivity(editPlanIntent);
                return true;
            case (R.id.deletePlan):

                AlertDialog.Builder ad = new AlertDialog.Builder(this);
                ad.setTitle("Delete Appointment confirmation");
                ad.setMessage("Are you sure about deleting this appointment?");

                ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String updateQuery = "/deletePlan?id="
                                + selectedPlanIndex;
                        ViewPlanClient restClient = new ViewPlanClient(context);
                        restClient.execute(new String[]{updateQuery});
                        CalendarHelper calendarHelper = new CalendarHelper(context);
                        calendarHelper.execute(new String[]{"", selectedPlan, "",
                                "", "", "delete"});
                        PlanDAO planDAO = new PlanDAO(context);
                        planDAO.deletePlan(selectedPlanIndex);
                        //TODO Remove
                        Plan plan = planDAO.fetchPlan(selectedPlanIndex);
                        if(plan == null){
                            Log.i(TAG, "Plan deleted.");
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
            case (R.id.aboutUs):
                Intent aboutIntent = new Intent(this, AboutUsActivity.class);
                startActivity(aboutIntent);
                return true;
            default:
                return false;
        }
    }

}
