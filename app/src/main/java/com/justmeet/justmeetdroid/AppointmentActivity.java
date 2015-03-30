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

public class AppointmentActivity extends FragmentActivity {
    String planEndTime = null;
    String planEndDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (JMUtil.haveInternet(this)) {
            setContentView(R.layout.new_plan);
            ActionBar aBar = getActionBar();
            Resources res = getResources();
            Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
            aBar.setBackgroundDrawable(actionBckGrnd);
            aBar.setTitle(" New Appointment");
        } else {
            Intent intent = new Intent(this, RetryActivity.class);
            startActivity(intent);
        }

    }

    /**
     * Called when the user clicks the Register Plan button
     */
    public void goFromCreateToViewAppointments(View view) {

        Button button = (Button) findViewById(R.id.registerAppointmentButton);
        button.setTextColor(getResources().getColor(R.color.click_button_2));

        EditText planNameEditText = (EditText) findViewById(R.id.appointmentTitle);
        String planName = planNameEditText.getText().toString();

        SharedPreferences prefs = getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        String phone = prefs.getString("phone", "");

        TextView planDateEditText = (TextView) findViewById(R.id.appointmentStartDate);
        String planDate = planDateEditText.getText().toString();

        TextView planEndDateEditText = (TextView) findViewById(R.id.appointmentEndDate);
        planEndDate = planEndDateEditText.getText().toString();

        TextView planTimeEditText = (TextView) findViewById(R.id.appointmentStartTime);
        String planTime = planTimeEditText.getText().toString();

        String hour = planTime.substring(0, 2);
        String min = planTime.substring(3, 5);
        if (planTime.contains("PM")) {
            hour = String.valueOf((Integer.valueOf(hour) + 12));
        }
        planTime = hour + ":" + min;

        TextView planEndTimeEditText = (TextView) findViewById(R.id.appointmentEndTime);
        planEndTime = planEndTimeEditText.getText().toString();

        String endHour = planEndTime.substring(0, 2);
        String endMin = planEndTime.substring(3, 5);
        if (planEndTime.contains("PM")) {
            endHour = String.valueOf((Integer.valueOf(endHour) + 12));
        }
        planEndTime = endHour + ":" + endMin;

        EditText planLocationEditText = (EditText) findViewById(R.id.appointmentLocation);
        String planLocation = planLocationEditText.getText().toString();

        String groupList = prefs.getString("selectedGroups", "");
        String phoneList = prefs.getString("selectedIndividuals", "");

        String[] planDates = JMUtil.createLocalToGmtTime(planDate + " " + planTime + ":00");
        String[] planEndDates = JMUtil.createLocalToGmtTime(planEndDate + " " + planEndTime + ":00");

        String insertQuery = "/newPlan?name=" + planName.replace(" ", "%20")
                + "&phone=" + phone + "&date=" + planDates[0] + "&time="
                + planDates[1] + "&location=" + planLocation.replace(" ", "%20")
                + "&phoneList=" + phoneList
                + "&groupList=" + groupList.replace(" ", "%20")
                + "&creator=" + phone + "&endDate=" + planEndDates[0] + "&endTime="
                + planEndDates[1];

        AppointmentClient restClient = new AppointmentClient(this);
        restClient.execute(new String[]{insertQuery, phone});

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("selectedPlan", planName);
        editor.apply();
    }


    public void setTime(View v) {
        Button button = (Button) findViewById(R.id.appointmentStartTimeButton);
        button.setTextColor(getResources().getColor(R.color.click_button_1));
        DialogFragment newFragment = new TimeNewPickerFragment("start");
        newFragment.show(getSupportFragmentManager(), "timePicker");
        button.setTextColor(getResources().getColor(R.color.button_text));
    }

    public void setEndTime(View v) {
        Button button = (Button) findViewById(R.id.appointmentEndTimebutton);
        button.setTextColor(getResources().getColor(R.color.click_button_1));
        DialogFragment newFragment = new TimeNewPickerFragment("end");
        newFragment.show(getSupportFragmentManager(), "timePicker");
        button.setTextColor(getResources().getColor(R.color.button_text));
    }

    public void setDate(View v) {
        Button button = (Button) findViewById(R.id.appointmentStartDateButton);
        button.setTextColor(getResources().getColor(R.color.click_button_1));
        DialogFragment newFragment = new DateNewPickerFragment("start");
        newFragment.show(getSupportFragmentManager(), "datePicker");
        button.setTextColor(getResources().getColor(R.color.button_text));
    }

    public void setEndDate(View v) {
        Button button = (Button) findViewById(R.id.appointmentEndDatebutton);
        button.setTextColor(getResources().getColor(R.color.click_button_1));
        DialogFragment newFragment = new DateNewPickerFragment("end");
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
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }


    private class AppointmentClient extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private ProgressDialog pDlg;

        public AppointmentClient(Context mContext) {
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

            if (response != null) {
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
                    String groupList = prefs.getString("selectedGroups", "");
                    String phoneList = prefs.getString("selectedIndividuals", "");
                    planDAO.addPlan(String.valueOf(plan.getId()), plan.getTitle(),
                            startPlanTime[0] + " " + startPlanTime[1], plan.getLocation(),
                            plan.getCreator(), plan.getCreator(), endPlanTime[0] + " " + endPlanTime[1],
                            groupList, phoneList);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("selectedPlanIndex", String.valueOf(plan.getId()));
                    editor.apply();
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
