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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.justmeet.dao.GroupDAO;
import com.justmeet.dao.PlanDAO;
import com.justmeet.entity.Group;
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
import java.util.Map.Entry;

public class GroupPlansActivity extends Fragment implements OnItemClickListener {

    private static final String TAG = "GroupPlansActivity";

    private Activity activity;
    PlanListAdapter adapter;
    ListView planListView;
    List<Map<String, Plan>> plansResult;
    View rootView;
    String selectedGroup;
    private String phone;
    private boolean isAdmin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this.getActivity();

        if (JMUtil.haveInternet(activity)) {
            rootView = inflater.inflate(R.layout.group_upcoming_plans, container, false);

            SharedPreferences prefs = activity.getSharedPreferences("Prefs",
                    Activity.MODE_PRIVATE);
            selectedGroup = prefs.getString("selectedGroup", "");
            String selectedGroupIndex = prefs.getString("selectedGroupIndex", "");
            phone = prefs.getString("phone", "");

            adapter = new PlanListAdapter(activity);
            planListView = (ListView) rootView.findViewById(R.id.viewGroupUpcomingplansList);
            planListView.setOnItemClickListener(this);
            GroupDAO groupDAO = new GroupDAO(activity);
            Group group = groupDAO.fetchGroup(selectedGroupIndex);
            if(group != null) {
                if (phone.equals(group.getAdmin())) {
                    isAdmin = true;
                }
            } else {
                Log.i(TAG, "No Group in local DB!");
                String searchQuery1 = "/fetchGroup?groupIndex=" + selectedGroupIndex;
                GroupPlansClient restClient1 = new GroupPlansClient(activity);
                restClient1.execute(new String[]{searchQuery1});
            }

            PlanDAO planDAO = new PlanDAO(activity);
            List<Plan> plans = planDAO.fetchGroupUpcomingPlans(selectedGroupIndex);
            if (plans != null && !plans.isEmpty()) {
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
                }
            } else {
                Log.i(TAG, "No Group Plans in local DB! " + selectedGroupIndex);
                String searchQuery2 = "/fetchGroupPlans?groupIndex=" + selectedGroupIndex;
                GroupPlansClient restClient2 = new GroupPlansClient(activity);
                restClient2.execute(new String[]{searchQuery2});
            }
        } else {
            Intent intent = new Intent(activity, RetryActivity.class);
            startActivity(intent);
        }
        return rootView;
    }

    private class GroupPlansClient extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private ProgressDialog pDlg;

        public GroupPlansClient(Context mContext) {
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

            if (response != null && response.contains("<Group>")) {
                Log.i(TAG, response);
                XStream xstream = new XStream();
                xstream.alias("Group", Group.class);

                xstream.alias("members", String.class);
                xstream.addImplicitCollection(Group.class, "members",
                        "members", String.class);
                Group group = (Group) xstream.fromXML(response);
                if (group != null && selectedGroup.equals(group.getName())) {
                    if (phone.equals(group.getAdmin())) {
                        isAdmin = true;
                    } else {
                        isAdmin = false;
                    }

                }
            }

            if (response != null && response.contains("PlanList")) {
                Log.i(TAG, response);
                XStream xstream = new XStream();
                xstream.alias("PlanList", PlanList.class);
                xstream.alias("plans", Plan.class);
                xstream.addImplicitCollection(PlanList.class, "plans");
                xstream.alias("membersAttending", String.class);
                xstream.addImplicitCollection(Plan.class, "membersAttending", "membersAttending", String.class);
                xstream.alias("membersInvited", String.class);
                xstream.addImplicitCollection(Plan.class, "membersInvited", "membersInvited", String.class);
                xstream.alias("groupsInvited", String.class);
                xstream.addImplicitCollection(Plan.class, "groupsInvited", "groupsInvited", String.class);
                PlanList planList = (PlanList) xstream.fromXML(response);
                if (planList != null && planList.getPlans() != null) {
                    List<Plan> plans = planList.getPlans();
                    if (plans != null && !plans.isEmpty()) {
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
                        }
                    }
                } else {
                    planListView.setVisibility(ListView.INVISIBLE);
                    TextView planLabel = (TextView) rootView.findViewById(R.id.upcomingGroupPlanListLabel);
                    planLabel.setText("No upcoming plans in this group");
                }
            }
            //pDlg.dismiss();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SharedPreferences prefs = activity.getSharedPreferences(
                "Prefs", Activity.MODE_PRIVATE);
        String selectedPlan = "";
        String selectedPlanIndex = "";
        if (plansResult != null && !plansResult.isEmpty()) {
            Map<String, Plan> selectedMap = plansResult.get(position);
            for (Entry<String, Plan> entry : selectedMap.entrySet()) {

                SharedPreferences.Editor editor = prefs.edit();
                selectedPlan = entry.getValue().getTitle();
                selectedPlanIndex = entry.getKey();
                editor.putString("selectedPlan", selectedPlan);
                editor.putString("selectedPlanIndex", selectedPlanIndex);
                String creator = entry.getValue().getCreator();
                if (phone.equals(creator)) {
                    editor.putString("planAdmin", "true");
                } else {
                    editor.putString("planAdmin", "false");
                }
                editor.apply();
                break;
            }
            Intent intent = new Intent(activity, HomeViewPlanActivity.class);
            startActivity(intent);
        }

    }

}
