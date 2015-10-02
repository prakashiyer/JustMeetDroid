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

import com.justmeet.dao.GroupDAO;
import com.justmeet.dao.UserDAO;
import com.justmeet.entity.Group;
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

public class GroupMembersActivity extends Fragment implements
        AdapterView.OnItemClickListener {

    private static final String TAG = "GroupMembersActivity";

    private GridView memberListView;
    private MemberGridAdapter adapter;
    private List<Map<String, User>> membersList;
    private Activity activity;
    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this.getActivity();

        if (JMUtil.haveInternet(activity)) {
            rootView = inflater.inflate(R.layout.group_member_grid, container, false);

            SharedPreferences prefs = activity.getSharedPreferences("Prefs",
                    Activity.MODE_PRIVATE);
            membersList = new ArrayList<Map<String, User>>();
            memberListView = (GridView) rootView.findViewById(R.id.viewgroupMemberList);
            adapter = new MemberGridAdapter(activity);
            String selectedGroupIndex = prefs.getString("selectedGroupIndex", "");

            fetchGroupMembersFromPhoneDB(selectedGroupIndex);
            if (membersList.isEmpty()) {
                //Log.i(TAG, "No group members in local DB!");
                String searchQuery = "/fetchGroupUsers?groupIndex=" + selectedGroupIndex;
                GroupMembersClient restClient = new GroupMembersClient(activity);
                restClient.execute(new String[]{searchQuery});
            }
        } else {
            Intent intent = new Intent(activity, RetryActivity.class);
            startActivity(intent);
        }
        return rootView;
    }

    private void fetchGroupMembersFromPhoneDB(String selectedGroupIndex) {
        GroupDAO groupDAO = new GroupDAO(activity);
        Group group = groupDAO.fetchGroup(selectedGroupIndex);
        if (group != null) {
            List<String> members = group.getMembers();
            if (members != null && !members.isEmpty()) {
                UserDAO userDAO = new UserDAO(activity);
                String[] membersArray = new String[members.size()];
                List<User> users = userDAO.fetchUsers(members.toArray(membersArray));
                if (users != null && !users.isEmpty()) {
                    for (User user : users) {
                        Map<String, User> memberMap = new HashMap<String, User>();
                        memberMap.put(user.getName(), user);
                        membersList.add(memberMap);
                    }
                }
                if (!membersList.isEmpty()) {
                    adapter.setData(membersList);
                    memberListView.setAdapter(adapter);
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if (membersList != null && !membersList.isEmpty()) {
            Map<String, User> selectedMap = membersList.get(position);

            for (Map.Entry<String, User> entry : selectedMap.entrySet()) {
                SharedPreferences prefs = activity.getSharedPreferences(
                        "Prefs", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                String selectedMember = entry.getKey();
                editor.putString("memberPhone", selectedMember);
                editor.apply();
                break;
            }

            Intent intent = new Intent(activity,
                    ViewMemberProfileActivity.class);
            startActivity(intent);
        }
    }

    private class GroupMembersClient extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private ProgressDialog pDlg;

        public GroupMembersClient(Context mContext) {
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
                Log.i(TAG, response);
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
                        Log.i(TAG, "Got User list " + users.size());
                        for (User user : users) {
                            Map<String, User> memberMap = new HashMap<String, User>();
                            memberMap.put(user.getPhone(), user);
                            membersList.add(memberMap);
                        }
                    }
                    if (!membersList.isEmpty()) {
                        adapter.setData(membersList);
                        memberListView.setAdapter(adapter);
                    }
                }
            }
            pDlg.dismiss();
        }
    }

}
