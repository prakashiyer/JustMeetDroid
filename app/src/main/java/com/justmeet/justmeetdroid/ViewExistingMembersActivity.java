package com.justmeet.justmeetdroid;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.justmeet.dao.UserDAO;
import com.justmeet.entity.User;
import com.justmeet.entity.UserList;
import com.justmeet.util.JMConstants;
import com.justmeet.util.JMUtil;
import com.thoughtworks.xstream.XStream;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class ViewExistingMembersActivity extends Activity implements
        OnItemClickListener {
    private static final String TAG = "View Existing Members Activity";
    GridView membersGridView;
    MemberGridAdapter adapter;
    List<Map<String, User>> membersList;
    Context context;
    List<Map<String, User>> filteredList;
    String selectedIndividuals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (JMUtil.haveInternet(this)) {
            setContentView(R.layout.existing_members_grid);
            ActionBar aBar = getActionBar();
            Resources res = getResources();
            Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
            aBar.setBackgroundDrawable(actionBckGrnd);
            aBar.setTitle(" Existing Members");

            membersList = new ArrayList<Map<String, User>>();
            filteredList = new ArrayList<Map<String, User>>();
            membersGridView = (GridView) findViewById(R.id.viewexistingmemberGrid);
            adapter = new MemberGridAdapter(this);
            membersGridView.setOnItemClickListener(this);
            selectedIndividuals = "";
            context = this;

            SharedPreferences prefs = getSharedPreferences("Prefs",
                    Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("selectedIndividuals", selectedIndividuals);
            editor.apply();

            Cursor phones = getContentResolver().query(
                    Phone.CONTENT_URI, null,
                    null, null, null);
            List<String> phoneList = new ArrayList<String>();

            while (phones.moveToNext()) {
                int phoneType = phones
                        .getInt(phones
                                .getColumnIndex(Phone.TYPE));

                String phoneNumber = phones
                        .getString(
                                phones.getColumnIndex(Phone.NUMBER))
                        .trim();
                String[] source = new String[]{"(", ")", "+", "-", ".", " "};
                String[] replace = new String[]{"", "", "", "", "", ""};
                phoneNumber = StringUtils.replaceEach(phoneNumber, source, replace);
                int len = phoneNumber.length();
                if (len >= 10 && StringUtils.isNumeric(phoneNumber)) {
                    phoneNumber = phoneNumber.substring(len - 10);
                    switch (phoneType) {
                        case Phone.TYPE_MOBILE:
                            phoneList.add(phoneNumber);
                            break;
                        case Phone.TYPE_HOME:
                            phoneList.add(phoneNumber);
                            break;
                        case Phone.TYPE_WORK:
                            phoneList.add(phoneNumber);
                            break;
                        case Phone.TYPE_OTHER:
                            phoneList.add(phoneNumber);
                            break;
                        default:
                            break;
                    }
                }

            }
            phones.close();

            if(!phoneList.isEmpty()){
                String phoneNumbers = org.springframework.util.StringUtils.collectionToCommaDelimitedString(phoneList);
                UserDAO userDAO = new UserDAO(context);
                String[] phoneArray = new String[phoneList.size()];
                List<User> users = userDAO.fetchUsers(phoneList.toArray(phoneArray));
                if (users != null && !users.isEmpty()) {
                    populateUserList(users);
                } else {
                    Log.i(TAG, "No Members in local DB!");
                    String searchQuery = "/fetchExistingUsers";
                    ExistingMembersClient restClient = new ExistingMembersClient(this);
                    restClient.execute(new String[]{searchQuery, phoneNumbers});
                }
            }
        } else {
            Intent intent = new Intent(this, RetryActivity.class);
            startActivity(intent);
        }

        SearchView searchView = (SearchView) findViewById(R.id.memSearchView);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!membersList.isEmpty()) {

                    filteredList = new ArrayList<Map<String, User>>();
                    for (Map<String, User> member : membersList) {
                        for (Entry<String, User> entry : member.entrySet()) {
                            User user = entry.getValue();
                            if (user.getName().toLowerCase(Locale.ENGLISH).contains(query.toLowerCase(Locale.ENGLISH))) {
                                filteredList.add(member);
                            }
                        }
                    }

                    adapter.setData(filteredList);
                    membersGridView.setAdapter(adapter);
                    //memberListLabel.setVisibility(TextView.VISIBLE);
                    membersGridView.setVisibility(GridView.VISIBLE);


                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!membersList.isEmpty()) {

                    filteredList = new ArrayList<Map<String, User>>();
                    for (Map<String, User> member : membersList) {
                        for (Entry<String, User> entry : member.entrySet()) {
                            User user = entry.getValue();
                            if (user.getName().toLowerCase(Locale.ENGLISH).contains(newText.toLowerCase(Locale.ENGLISH))) {
                                filteredList.add(member);
                            }
                        }
                    }

                    adapter.setData(filteredList);
                    membersGridView.setAdapter(adapter);
                    //memberListLabel.setVisibility(TextView.VISIBLE);
                    membersGridView.setVisibility(GridView.VISIBLE);


                }
                return true;
            }
        });


    }

    /**
     * Called when the user clicks the see members button
     */
    public void goToGroupsSelection(View view) {
        SharedPreferences prefs = getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        String action = prefs.getString("action", "newPlan");
        if (action.equals("newPlan")) {
            Button button = (Button) findViewById(R.id.goToGroupsSelectionButton);
            button.setTextColor(getResources().getColor(R.color.click_button_2));
            Intent intent = new Intent(this, ViewExistingGroupsActivity.class);
            startActivity(intent);
        } else if (action.equals("newGroup")) {
            Button button = (Button) findViewById(R.id.goToGroupsSelectionButton);
            button.setTextColor(getResources().getColor(R.color.click_button_2));
            Intent intent = new Intent(this, CreateGroupActivity.class);
            startActivity(intent);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if (filteredList != null && !filteredList.isEmpty()) {
            Map<String, User> selectedMap = filteredList.get(position);

            for (Entry<String, User> entry : selectedMap.entrySet()) {
                SharedPreferences prefs = getSharedPreferences("Prefs",
                        Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                String selectedMember = entry.getKey();
                User user = entry.getValue();
                if (user.isSelected()) {
                    user.setSelected(false);
                    selectedIndividuals = selectedIndividuals.replace(selectedMember + ",", "");
                    editor.putString("selectedIndividuals", selectedIndividuals);
                    editor.apply();
                    adapter.setData(filteredList);
                    membersGridView.setAdapter(adapter);
                    membersGridView.setVisibility(GridView.VISIBLE);
                } else {
                    user.setSelected(true);
                    selectedIndividuals = selectedIndividuals + selectedMember + ",";
                    editor.putString("selectedIndividuals", selectedIndividuals);
                    System.out.println("selectedIndividuals " + selectedIndividuals);
                    editor.apply();
                    adapter.setData(filteredList);
                    membersGridView.setAdapter(adapter);
                    membersGridView.setVisibility(GridView.VISIBLE);
                }

                break;
            }
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (!membersList.isEmpty()) {

                List<Map<String, User>> filteredList = new ArrayList<Map<String, User>>();
                for (Map<String, User> member : membersList) {
                    for (Entry<String, User> entry : member.entrySet()) {
                        User user = entry.getValue();
                        if (user.getName().toLowerCase(Locale.ENGLISH).contains(query.toLowerCase(Locale.ENGLISH))) {
                            filteredList.add(member);
                        }
                    }
                }

                adapter.setData(filteredList);
                membersGridView.setAdapter(adapter);
                //memberListLabel.setVisibility(TextView.VISIBLE);
                membersGridView.setVisibility(GridView.VISIBLE);


            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private class ExistingMembersClient extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private ProgressDialog pDlg;

        public ExistingMembersClient(Context mContext) {
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
            HttpPost post = new HttpPost(path);
            HttpEntity results = null;

            try {
                MultipartEntity entity = new MultipartEntity();
                entity.addPart("phoneList", new StringBody(params[1]));
                post.setEntity(entity);
                HttpResponse response = client.execute(target, post);
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
                Log.d(TAG, response);
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
                        populateUserList(users);
                    }
                }
            }
            pDlg.dismiss();
        }

    }

    private void populateUserList(List<User> users) {
        for (User user : users) {
            Map<String, User> memberMap = new HashMap<String, User>();
            memberMap.put(user.getPhone(), user);
            membersList.add(memberMap);
        }
        if (!membersList.isEmpty()) {
            filteredList = new ArrayList<Map<String, User>>();
            filteredList.addAll(membersList);
            adapter.setData(filteredList);
            membersGridView.setAdapter(adapter);
            membersGridView.setVisibility(GridView.VISIBLE);
        }
    }


}
