package com.justmeet.justmeetdroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.justmeet.dao.PlanDAO;
import com.justmeet.dao.UserDAO;
import com.justmeet.entity.Plan;
import com.justmeet.entity.PlanList;
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
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by praxiyer on 08-03-2015.
 */
public class UserPlansActivity extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "User Plans Activity";
    private Activity activity;
    private ListView planListView;
    private PlanListAdapter adapter;
    private View rootView;
    private String phone;
    private List<Map<String, Plan>> plansResult;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        activity = this.getActivity();
        if (activity != null && JMUtil.haveInternet(activity)) {
            rootView = inflater.inflate(R.layout.user_plans, container, false);
            TextView plan_text = (TextView) rootView.findViewById(R.id.upcomingPlanListLabel);
            plan_text.setText("Plans");

            SharedPreferences prefs = activity.getSharedPreferences("Prefs",
                    Activity.MODE_PRIVATE);
            phone = prefs.getString("phone", "");

            adapter = new PlanListAdapter(activity);
            planListView = (ListView) rootView
                    .findViewById(R.id.viewupcomingplansList);
            planListView.setOnItemClickListener(this);

            Cursor phones = activity.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    null, null, null);
            List<String> phoneList = new ArrayList<String>();

            while (phones.moveToNext()) {
                int phoneType = phones
                        .getInt(phones
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                String phoneNumber = phones
                        .getString(
                                phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        .trim();
                String[] source = new String[]{"(", ")", "+", "-", ".", " "};
                String[] replace = new String[]{"", "", "", "", "", ""};
                phoneNumber = StringUtils.replaceEach(phoneNumber, source, replace);
                int len = phoneNumber.length();
                if (len >= 10 && StringUtils.isNumeric(phoneNumber)) {
                    phoneNumber = phoneNumber.substring(len - 10);
                    Log.i(TAG, phoneNumber);
                    switch (phoneType) {
                        case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                            phoneList.add(phoneNumber);
                            break;
                        case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                            phoneList.add(phoneNumber);
                            break;
                        case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                            phoneList.add(phoneNumber);
                            break;
                        case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                            phoneList.add(phoneNumber);
                            break;
                        default:
                            break;
                    }
                }

            }
            phones.close();

            if(!phoneList.isEmpty()) {
                String phoneNumbers = org.springframework.util.StringUtils.collectionToCommaDelimitedString(phoneList);
                phoneNumbers.replaceAll(" ","");
                String searchQuery = "/fetchExistingUsers";
                ExistingMembersClient restClient = new ExistingMembersClient(activity);
                restClient.execute(new String[]{searchQuery, phoneNumbers});
            }

            PlanDAO planDAO = new PlanDAO(activity);
            List<Plan> plans = planDAO.fetchUpcomingPlans(phone);

            if (plans != null && !plans.isEmpty()) {
                populatePlanDetails(plans);
            } else {
                Log.i(TAG, "No Plans in local DB!");
                setEmptyMessage();
                String searchQuery = "/fetchUpcomingPlans?phone=" + phone;
                PlansClient restClient = new PlansClient(activity);
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

    private class PlansClient extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private ProgressDialog pDlg;

        public PlansClient(Context mContext) {
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
            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is established.
            // The default value is zero, that means the timeout is not used.
            int timeoutConnection = 0;
            //HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            // Set the default socket timeout (SO_TIMEOUT)
            // in milliseconds which is the timeout for waiting for data.gr
            int timeoutSocket = 0;
            //HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpClient client = new DefaultHttpClient(httpParameters);

            HttpGet get = new HttpGet(path);
            HttpEntity results = null;

            try {
                HttpResponse response = client.execute(target, get);
                results = response.getEntity();
                String result = EntityUtils.toString(results);
                return result;
            } catch (Exception e) {
                Log.e(TAG, "Operation time out " +e);
                //Toast.makeText(mContext, "Operation timed out", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null && response.contains("PlanList")) {
                Log.i(TAG, "Response:" +response);
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

    private class ExistingMembersClient extends AsyncTask<String, Integer, String> {

        private Context mContext;

        public ExistingMembersClient(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected void onPreExecute() {


        }

        @Override
        protected String doInBackground(String... params) {

            String path = JMConstants.SERVICE_PATH + params[0];
            Log.i(TAG, path);

            // HttpHost target = new HttpHost(TARGET_HOST);
            HttpHost target = new HttpHost(JMConstants.TARGET_HOST, 8080);
            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is established.
            // The default value is zero, that means the timeout is not used.
            //int timeoutConnection = 3000;
            //HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            // Set the default socket timeout (SO_TIMEOUT)
            // in milliseconds which is the timeout for waiting for data.gr
            //int timeoutSocket = 5000;
            //HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpClient client = new DefaultHttpClient(httpParameters);
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
                e.printStackTrace();
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
                        UserDAO userDAO = new UserDAO(activity);
                        for(User user: users){
                            String groupIdStr = null;
                            List<String> groupIds = user.getGroupIds();
                            if(groupIds != null && !groupIds.isEmpty()){
                                groupIdStr = JMUtil.listToCommaDelimitedString(groupIds);
                            }
                            userDAO.addOtherUsers(user.getName(), user.getPhone(),
                                    user.getImage(), groupIdStr);
                        }
                    }
                }
            }
        }

    }

    private void setEmptyMessage() {
        planListView.setVisibility(ListView.INVISIBLE);
        TextView planLabel = (TextView) rootView
                .findViewById(R.id.upcomingPlanListLabel);
        planLabel.setText("No upcoming appointments");
    }
}
