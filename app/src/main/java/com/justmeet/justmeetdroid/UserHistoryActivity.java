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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.justmeet.dao.PlanDAO;
import com.justmeet.entity.Plan;
import com.justmeet.entity.PlanList;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by praxiyer on 08-03-2015.
 */
public class UserHistoryActivity extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "User History Activity";
    private Activity activity;
    private ListView planListView;
    private PlanListAdapter adapter;
    private View rootView;
    private List<Map<String, Plan>> plansResult;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        activity = this.getActivity();
        if (activity != null && JMUtil.haveInternet(activity)) {
            rootView = inflater.inflate(R.layout.user_history, container, false);
            TextView planHistoryLabel = (TextView) rootView.findViewById(R.id.planHistoryLabel);
            planHistoryLabel.setText("History");

            SharedPreferences prefs = activity.getSharedPreferences("Prefs",
                    Activity.MODE_PRIVATE);
            String phone = prefs.getString("phone", "");

            adapter = new PlanListAdapter(activity);
            planListView = (ListView) rootView
                    .findViewById(R.id.planHistoryList);
            planListView.setOnItemClickListener(this);

            PlanDAO planDAO = new PlanDAO(activity);
            List<Plan> plans = planDAO.fetchPlanHistory(phone);

            if (plans != null && !plans.isEmpty()) {
                Log.i(TAG, "Getting History from local DB!");
                populatePlanDetails(plans);
            } else {
                Log.i(TAG, "No History in local DB!");
                String searchQuery = "/fetchPlanHistory?phone=" + phone;
                UserHistoryClient restClient = new UserHistoryClient(activity);
                restClient.execute(new String[]{searchQuery});
            }
            return rootView;
        } else {
            Intent intent = new Intent(activity, RetryActivity.class);
            startActivity(intent);
        }
        return null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        SharedPreferences prefs = activity.getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        String selectedPlan = "";
        String selectedPlanIndex = "";
        if (plansResult != null && !plansResult.isEmpty()) {
            Map<String, Plan> selectedMap = plansResult.get(position);
            for (Map.Entry<String, Plan> entry : selectedMap.entrySet()) {

                SharedPreferences.Editor editor = prefs.edit();
                selectedPlan = entry.getValue().getTitle();
                selectedPlanIndex = entry.getKey();
                editor.putString("selectedPlan", selectedPlan);
                editor.putString("selectedPlanIndex", selectedPlanIndex);
                editor.apply();
                break;
            }
            Intent intent = new Intent(activity, HomePlanHistoryActivity.class);
            startActivity(intent);
        }
    }

    private void populatePlanDetails(List<Plan> plans) {
        plansResult = new ArrayList<Map<String, Plan>>();
        for (Plan plan : plans) {
            Map<String, Plan> planMap = new HashMap<String, Plan>();
            planMap.put(String.valueOf(plan.getId()), plan);
            plansResult.add(planMap);

        }
        if (!plansResult.isEmpty()) {
            planListView.setVisibility(ListView.VISIBLE);
            adapter.setData(plansResult);
            planListView.setAdapter(adapter);
            // Click event for single list row
        } else {
            setEmptyMessage();
        }
    }

    private void setEmptyMessage() {
        planListView.setVisibility(ListView.INVISIBLE);
        TextView planLabel = (TextView) rootView
                .findViewById(R.id.planHistoryLabel);
        planLabel.setText("No plans available in history");
    }

    private class UserHistoryClient extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private ProgressDialog pDlg;

        public UserHistoryClient(Context mContext) {
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

            //showProgressDialog();

        }

        @Override
        protected String doInBackground(String... params) {
            String path = JMConstants.SERVICE_PATH + params[0];

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
            if (response != null && response.contains("PlanList")) {
                Log.i(TAG, "RESPONSE: "+response);
                XStream xstream = new XStream();
                xstream.alias("PlanList", PlanList.class);
                xstream.alias("plans", Plan.class);
                xstream.addImplicitCollection(PlanList.class, "plans");
                xstream.addImplicitCollection(Plan.class, "membersAttending",
                        "membersAttending", String.class);
                xstream.addImplicitCollection(Plan.class, "membersInvited",
                        "membersInvited", String.class);
                xstream.addImplicitCollection(Plan.class, "groupsInvited",
                        "groupsInvited", String.class);
                PlanList planList = (PlanList) xstream.fromXML(response);

                if (planList != null && planList.getPlans() != null) {

                    List<Plan> plans = planList.getPlans();

                    if (plans != null && !plans.isEmpty()) {
                        populatePlanDetails(plans);
                    } else {
                        setEmptyMessage();
                    }
                } else {
                    setEmptyMessage();
                }
            } else {
                setEmptyMessage();
            }
            //pDlg.dismiss();
        }
    }
}
