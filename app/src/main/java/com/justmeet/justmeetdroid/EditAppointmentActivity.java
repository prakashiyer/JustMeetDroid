package com.justmeet.justmeetdroid;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class EditAppointmentActivity extends FragmentActivity {
    private static final String TAG = "Edit Appointment";
    private String selectedPlan;
    private String selectedPlanIndex;
    private Menu menu;
    private Plan plan;
    private String phone;
    String planEndTime = null;
    String planEndDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (JMUtil.haveInternet(this)) {
            setContentView(R.layout.edit_plan);
            ActionBar aBar = getActionBar();
            Resources res = getResources();
            Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
            aBar.setBackgroundDrawable(actionBckGrnd);
            aBar.setTitle(" Edit Appointment");

            SharedPreferences prefs = getSharedPreferences("Prefs",
                    Activity.MODE_PRIVATE);
            phone = prefs.getString("phone", "");
            selectedPlan = prefs.getString("selectedPlan", "New User");
            selectedPlanIndex = prefs.getString("selectedPlanIndex", "");

            TextView planTitle = (TextView) findViewById(R.id.editTitle);
            planTitle.setText(selectedPlan);

            PlanDAO planDAO = new PlanDAO(this);
            Plan plan = planDAO.fetchPlan(selectedPlanIndex);
            if (plan != null) {
                populatePlanInformation(phone, plan);
            } else {
                Log.i(TAG, "No plan found in DB");
                String searchQuery = "/fetchPlan?planIndex=" + selectedPlanIndex;
                EditPlanClient restClient = new EditPlanClient(this);
                restClient.execute(new String[]{searchQuery, phone});
            }
        } else {
            Intent intent = new Intent(this, RetryActivity.class);
            startActivity(intent);
        }

    }

    private void populatePlanInformation(String phone, Plan plan) {
        //TODO Add Delete item in menu for Admin
        TextView planLocation = (TextView) findViewById(R.id.editLocation);
        planLocation.setText(plan.getLocation());

        TextView planTimeValue = (TextView) findViewById(R.id.editStartTime);
        TextView planEndTimeValue = (TextView) findViewById(R.id.editEndTime);

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

    }

    /**
     * Called when the user clicks the edit Plan button
     */
    public void editAppointment(View view) {

        EditText planNameEditText = (EditText) findViewById(R.id.editTitle);
        String planName = planNameEditText.getText().toString();

        SharedPreferences prefs = getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        String phone = prefs.getString("phone", "");

        TextView planDateEditText = (TextView) findViewById(R.id.editStartDate);
        String planDate = planDateEditText.getText().toString();

        TextView planEndDateEditText = (TextView) findViewById(R.id.editEndDate);
        planEndDate = planEndDateEditText.getText().toString();

        TextView planTimeEditText = (TextView) findViewById(R.id.editStartTime);
        String planTime = planTimeEditText.getText().toString();

        String hour = planTime.substring(0, 2);
        String min = planTime.substring(3, 5);
        if (planTime.contains("PM")) {
            hour = String.valueOf((Integer.valueOf(hour) + 12));
        }
        planTime = hour + ":" + min;

        TextView planEndTimeEditText = (TextView) findViewById(R.id.editEndTime);
        planEndTime = planEndTimeEditText.getText().toString();

        String endHour = planEndTime.substring(0, 2);
        String endMin = planEndTime.substring(3, 5);
        if (planEndTime.contains("PM")) {
            endHour = String.valueOf((Integer.valueOf(endHour) + 12));
        }
        planEndTime = endHour + ":" + endMin;

        EditText planLocationEditText = (EditText) findViewById(R.id.editLocation);
        String planLocation = planLocationEditText.getText().toString();

        String[] planDates = JMUtil.createLocalToGmtTime(planDate + " " + planTime + ":00");
        String[] planEndDates = JMUtil.createLocalToGmtTime(planEndDate + " " + planEndTime + ":00");

        String insertQuery = "/editPlan?name=" + planName.replace(" ", "%20")
                + "&phone=" + phone + "&date=" + planDates[0] + "&time="
                + planDates[1] + "&location=" + planLocation.replace(" ", "%20")
                + "&endDate=" + planEndDates[0] + "&endTime="
                + planEndDates[1];

        EditPlanClient restClient = new EditPlanClient(this);
        restClient.execute(new String[]{insertQuery, phone});

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("selectedPlan", planName);
        editor.apply();
    }


    public void setTime(View v) {
        Button button = (Button) findViewById(R.id.appointmentStartTimeButton);
        button.setTextColor(getResources().getColor(R.color.click_button_1));
        DialogFragment newFragment = new TimeNewPickerFragment("editStart");
        newFragment.show(getSupportFragmentManager(), "timePicker");
        button.setTextColor(getResources().getColor(R.color.button_text));
    }

    public void setEndTime(View v) {
        Button button = (Button) findViewById(R.id.appointmentEndTimebutton);
        button.setTextColor(getResources().getColor(R.color.click_button_1));
        DialogFragment newFragment = new TimeNewPickerFragment("editEnd");
        newFragment.show(getSupportFragmentManager(), "timePicker");
        button.setTextColor(getResources().getColor(R.color.button_text));
    }

    public void setDate(View v) {
        Button button = (Button) findViewById(R.id.appointmentStartDateButton);
        button.setTextColor(getResources().getColor(R.color.click_button_1));
        DialogFragment newFragment = new DateNewPickerFragment("editStart");
        newFragment.show(getSupportFragmentManager(), "datePicker");
        button.setTextColor(getResources().getColor(R.color.button_text));
    }

    public void setEndDate(View v) {
        Button button = (Button) findViewById(R.id.appointmentEndDatebutton);
        button.setTextColor(getResources().getColor(R.color.click_button_1));
        DialogFragment newFragment = new DateNewPickerFragment("editEnd");
        newFragment.show(getSupportFragmentManager(), "datePicker");
        button.setTextColor(getResources().getColor(R.color.button_text));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeViewPlanActivity.class);
        startActivity(intent);
    }


    private class EditPlanClient extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private ProgressDialog pDlg;
        private String method;

        public EditPlanClient(Context mContext) {
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
            method = params[0];
            String path = JMConstants.SERVICE_PATH + params[0];
            //HttpHost target = new HttpHost(TARGET_HOST);
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
            if (response != null && method.equals("fetchPlan")) {
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
            if (response != null && method.equals("editPlan")) {
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
                    CalendarHelper calendarHelper = new CalendarHelper(mContext);
                    String startTime = plan.getStartTime();
                    String[] startPlanTime = null;
                    if (startTime != null) {
                        startPlanTime = JMUtil.createGmtToLocalTime(startTime);
                    }
                    String endTime = plan.getEndTime();
                    String[] endPlanTime = null;
                    if (endTime != null) {
                        endPlanTime = JMUtil.createGmtToLocalTime(endTime);
                    }
                    calendarHelper.execute(new String[]{startPlanTime[0] + " " + startPlanTime[1],
                            String.valueOf(plan.getTitle()), plan.getLocation(), "", "", "create", endPlanTime[0], endPlanTime[1]});
                    PlanDAO planDAO = new PlanDAO(mContext);
                    SharedPreferences prefs = getSharedPreferences("Prefs",
                            Activity.MODE_PRIVATE);
                    planDAO.editPlan(String.valueOf(plan.getId()), plan.getTitle(),
                            startTime, plan.getLocation(),
                            endTime);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("selectedPlanIndex", String.valueOf(plan.getId()));
                    editor.apply();
                    //TODO Remove
                    Plan dbplan = planDAO.fetchPlan(String.valueOf(plan.getId()));
                    if(dbplan != null){
                        Log.i(TAG, "Plan updated: " +dbplan.getTitle());
                    }
                    pDlg.dismiss();
                    Intent intent = new Intent(mContext, HomeViewPlanActivity.class);
                    startActivity(intent);
                } else {
                    pDlg.dismiss();
                    Toast.makeText(mContext, "Plan creation failed", Toast.LENGTH_SHORT).show();
                }
            }

        }

    }


}
