package com.justmeet.justmeetdroid;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.justmeet.dao.PlanDAO;
import com.justmeet.dao.UserDAO;
import com.justmeet.entity.Plan;
import com.justmeet.entity.User;
import com.justmeet.entity.UserList;
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

public class ViewMembersAttendingActivity extends Fragment implements
        OnItemClickListener {
    private static final String TAG = "View Members Attending";
    GridView membersGridView;
    MemberGridAdapter adapter;
    List<Map<String, User>> membersList;
    Activity activity;
    View rootView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        activity = this.getActivity();
        if (activity != null && JMUtil.haveInternet(activity)) {
            rootView = inflater.inflate(R.layout.members_attending_grid, container, false);

            SharedPreferences prefs = activity.getSharedPreferences("Prefs",
                    Activity.MODE_PRIVATE);
            membersList = new ArrayList<Map<String, User>>();
            membersGridView = (GridView) rootView.findViewById(R.id.viewattendingmemberGrid);
            adapter = new MemberGridAdapter(activity);
            membersGridView.setOnItemClickListener(this);
            String selectedPlanIndex = prefs.getString("selectedPlanIndex", "");

            boolean isDataFromDB = populateDataFromDB(selectedPlanIndex);

            if (!isDataFromDB) {
                Log.i(TAG, "No members in Local DB");
                String searchQuery = "/fetchPlanUsers?id=" + selectedPlanIndex;
                MembersAttendingClient restClient = new MembersAttendingClient(activity);
                restClient.execute(new String[]{searchQuery});
            }
        } else {
            Intent intent = new Intent(activity, RetryActivity.class);
            startActivity(intent);
        }
        return rootView;
    }

    private boolean populateDataFromDB(String selectedPlanIndex) {
        PlanDAO planDAO = new PlanDAO(activity);
        Plan plan = planDAO.fetchPlan(selectedPlanIndex);
        if (plan != null) {
            List<String> membersAttending = plan.getMembersAttending();
            if (membersAttending != null && !membersAttending.isEmpty()) {
                UserDAO userDAO = new UserDAO(activity);
                List<User> users = new ArrayList<User>();
                for (String memberPhone : membersAttending) {
                    User user = userDAO.fetchUser(memberPhone);
                    Map<String, User> memberMap = new HashMap<String, User>();
                    memberMap.put(user.getPhone(), user);
                    membersList.add(memberMap);
                }
            }
        }
        if (!membersList.isEmpty()) {
            adapter.setData(membersList);
            membersGridView.setAdapter(adapter);
            membersGridView.setVisibility(GridView.VISIBLE);
            TextView label = (TextView) rootView.findViewById(R.id.viewMembersAttendingListLabel);
            label.setVisibility(TextView.INVISIBLE);
            return true;
        }
        return false;
    }

    private void setEmptyMessage() {
        membersGridView.setVisibility(ListView.INVISIBLE);
        TextView label = (TextView) rootView.findViewById(R.id.viewMembersAttendingListLabel);
        label.setText("No members have RSVPed Yes.");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if (membersList != null && !membersList.isEmpty()) {
            Map<String, User> selectedMap = membersList.get(position);

            for (Entry<String, User> entry : selectedMap.entrySet()) {
                SharedPreferences prefs = activity.getSharedPreferences("Prefs",
                        Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                String selectedMember = entry.getKey();
                editor.putString("memberPhone", selectedMember);
                editor.apply();
                break;
            }

            Intent intent = new Intent(activity, ViewMemberProfileActivity.class);
            startActivity(intent);
        }
    }

    private class MembersAttendingClient extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private ProgressDialog pDlg;

        public MembersAttendingClient(Context mContext) {
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
                XStream userXstream = new XStream();
                userXstream.alias("UserList", UserList.class);
                userXstream.addImplicitCollection(UserList.class, "users");
                userXstream.alias("users", User.class);
                userXstream.alias("groupIds", String.class);
                userXstream.addImplicitCollection(User.class, "groupIds",
                        "groupIds", String.class);
                UserList userList = (UserList) userXstream.fromXML(response);
                if (userList != null) {

                    List<User> users = userList.getUsers();
                    if (users != null && !users.isEmpty()) {
                        for (User user : users) {
                            Map<String, User> memberMap = new HashMap<String, User>();
                            memberMap.put(user.getPhone(), user);
                            membersList.add(memberMap);

                        }

                        if (!membersList.isEmpty()) {
                            adapter.setData(membersList);
                            membersGridView.setAdapter(adapter);
                            membersGridView.setVisibility(GridView.VISIBLE);
                            TextView label = (TextView) rootView.findViewById(R.id.viewMembersAttendingListLabel);
                            label.setVisibility(TextView.INVISIBLE);

                        } else {
                            setEmptyMessage();
                        }
                    } else {
                        setEmptyMessage();
                    }

                } else {
                    setEmptyMessage();
                }
            } else {
                setEmptyMessage();
            }
            pDlg.dismiss();
        }

    }

}
