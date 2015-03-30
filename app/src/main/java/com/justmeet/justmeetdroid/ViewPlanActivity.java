package com.justmeet.justmeetdroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.justmeet.dao.PlanDAO;
import com.justmeet.entity.Plan;
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
 * Created by praxiyer on 10-03-2015.
 */
public class ViewPlanActivity extends Fragment {

    private String selectedPlan;
    private String selectedPlanIndex;
    private Menu menu;
    private Plan plan;
    Activity activity;
    View rootView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        activity = this.getActivity();
        if (activity != null && JMUtil.haveInternet(activity)) {
            rootView = inflater.inflate(R.layout.view_plan, container, false);

            SharedPreferences prefs = activity.getSharedPreferences("Prefs",
                    Activity.MODE_PRIVATE);
            String phone = prefs.getString("phone", "");
            selectedPlan = prefs.getString("selectedPlan", "New User");
            selectedPlanIndex = prefs.getString("selectedPlanIndex", "");

            TextView planTitle = (TextView) rootView.findViewById(R.id.viewPlanTitle);
            planTitle.setText(selectedPlan);

            PlanDAO planDAO = new PlanDAO(activity);
            Plan plan = planDAO.fetchPlan(selectedPlanIndex);
            if (plan != null) {
                populatePlanInformation(phone, plan);
            } else {
                String searchQuery = "/fetchPlan?planIndex=" + selectedPlanIndex;
                ViewPlanClient restClient = new ViewPlanClient(activity);
                restClient.execute(new String[]{searchQuery, phone});
            }
        } else {
            Intent intent = new Intent(activity, RetryActivity.class);
            startActivity(intent);
        }
        return rootView;
    }

    private void populatePlanInformation(String phone, Plan plan) {
        //TODO Add Delete item in menu for Admin
        TextView planLocation = (TextView) activity.findViewById(R.id.viewPlanLocation);
        planLocation.setText(plan.getLocation());

        TextView planTimeValue = (TextView) activity.findViewById(R.id.viewPlanTime);
        TextView planEndTimeValue = (TextView) activity.findViewById(R.id.viewPlanEndTime);

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
        Button rsvpPlanButton = (Button) activity.findViewById(R.id.rsvpPlanButton);
        TextView rsvpLabel = (TextView) activity.findViewById(R.id.rsvpLabel);
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
                    populatePlanInformation(phone, plan);
                }
            }
            pDlg.dismiss();
        }
    }
}
