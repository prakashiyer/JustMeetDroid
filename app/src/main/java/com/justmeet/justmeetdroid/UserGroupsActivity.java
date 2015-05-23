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
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.justmeet.dao.GroupDAO;
import com.justmeet.dao.UserDAO;
import com.justmeet.entity.Group;
import com.justmeet.entity.GroupList;
import com.justmeet.entity.User;
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
public class UserGroupsActivity extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "User Groups Activity";
    private Activity activity;
    private GridView gridView;
    private GroupListAdapter adapter;
    private List<Map<String, Group>> groupsList;
    private String phone;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this.getActivity();
        if (activity != null && JMUtil.haveInternet(activity)) {

            SharedPreferences prefs = activity.getSharedPreferences("Prefs",
                    Activity.MODE_PRIVATE);
            rootView = inflater.inflate(R.layout.user_groups, container, false);
            adapter = new GroupListAdapter(activity);
            gridView = (GridView) rootView.findViewById(R.id.groupList);
            gridView.setOnItemClickListener(this);

            TextView groupsLabel = (TextView) rootView.findViewById(R.id.welcomeListGroupsLabel);
            groupsLabel.setText("Groups");
            phone = prefs.getString("phone", "");
            List<Group> groups = fetchGroupsFromDB();
            if (groups != null && !groups.isEmpty()) {
                populateGroupDetails(groups);
            } else {
                Log.i(TAG, "No Groups in local DB!");
                String searchQuery = "/fetchExistingGroups?phone=" + phone;
                GroupsClient restClient = new GroupsClient(activity);
                restClient.execute(new String[]{searchQuery});
            }
        } else {
            Intent intent = new Intent(activity, RetryActivity.class);
            startActivity(intent);
        }
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SharedPreferences prefs = activity.getSharedPreferences(
                "Prefs", Activity.MODE_PRIVATE);
        String selectedGroupPhone = "";
        if (groupsList != null && !groupsList.isEmpty()) {
            Map<String, Group> selectedMap = groupsList.get(position);
            for (Map.Entry<String, Group> entry : selectedMap.entrySet()) {
                SharedPreferences.Editor editor = prefs.edit();
                selectedGroupPhone = entry.getValue().getAdmin();
                editor.putString("selectedGroupPhone", selectedGroupPhone);
                editor.putString("selectedGroupName", entry.getValue().getName());
                editor.putString("selectedGroupId", entry.getValue().getId());
                String creator = entry.getValue().getAdmin();
                if (phone.equals(creator)) {
                    editor.putString("groupAdmin", "true");
                } else {
                    editor.putString("groupAdmin", "false");
                }
                editor.apply();
                break;
            }

            Intent intent = new Intent(activity, HomeGroupActivity.class);
            startActivity(intent);
        }
    }

    private void setEmptyMessage() {
        gridView.setVisibility(ListView.INVISIBLE);
        TextView groupsLabel = (TextView) rootView.findViewById(R.id.welcomeListGroupsLabel);
        groupsLabel.setText("You are not a part of any group.");
    }

    private void populateGroupDetails(List<Group> groups) {
        groupsList = new ArrayList<Map<String, Group>>();
        for (Group group : groups) {
            Map<String, Group> groupDetails = new HashMap<String, Group>();
            groupDetails.put(group.getId(), group);
            groupsList.add(groupDetails);
        }
        if (!groupsList.isEmpty()) {
            adapter.setData(groupsList);
            gridView.setAdapter(adapter);
        }
    }

    private List<Group> fetchGroupsFromDB() {
        UserDAO userDAO = new UserDAO(activity);
        User user = userDAO.fetchUser(phone);
        if (user != null) {
            List<String> groupIds = user.getGroupIds();

            if (groupIds != null && !groupIds.isEmpty()) {
                Log.i(TAG, "User Groups size: "+groupIds.size());
                Log.i(TAG, "User Groups: "+JMUtil.listToCommaDelimitedString(groupIds));
                GroupDAO groupDAO = new GroupDAO(activity);
                return groupDAO.fetchGroups(JMUtil.listToCommaDelimitedString(groupIds));
            }
        }
        return null;
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

            if (response != null) {
                Log.i(TAG, "RESPONSE: "+response);
                XStream groupsXs = new XStream();
                groupsXs.alias("GroupList", GroupList.class);
                groupsXs.addImplicitCollection(GroupList.class, "groups");
                groupsXs.alias("groups", Group.class);
                groupsXs.alias("members", String.class);
                groupsXs.addImplicitCollection(Group.class, "members",
                        "members", String.class);
                GroupList groupList = (GroupList) groupsXs.fromXML(response);
                if (groupList != null) {
                    groupsList = new ArrayList<Map<String, Group>>();
                    List<Group> groups = groupList.getGroups();
                    if (groups != null && !groups.isEmpty()) {
                        populateGroupDetails(groups);
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
