package com.justmeet.justmeetdroid;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.justmeet.dao.ExpenseDAO;
import com.justmeet.dao.GroupDAO;
import com.justmeet.dao.PlanDAO;
import com.justmeet.entity.Expense;
import com.justmeet.entity.Group;
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

public class GCMIntentService extends GCMBaseIntentService {

    private static final String PROJECT_ID = "358164918628";

    private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super(PROJECT_ID);
        Log.d(TAG, "GCMIntentService init");
    }


    @Override
    protected void onError(Context ctx, String sError) {
        // TODO Auto-generated method stub
        Log.d(TAG, "Error: " + sError);

    }

    @Override
    protected void onMessage(Context ctx, Intent intent) {
        String message = intent.getStringExtra("message");
        Log.d(TAG, "Message Received: " + message);
        sendNotification(message);
        //sendGCMIntent(ctx, message);

    }


    @Override
    protected void onRegistered(Context ctx, String regId) {
        // TODO Auto-generated method stub
        // send regId to your server
        Log.d(TAG, regId);

    }

    @Override
    protected void onUnregistered(Context ctx, String regId) {
        // TODO Auto-generated method stub
        // send notification to your server to remove that regId

    }

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = null;

        String temp[] = msg.split(",");
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Just Meet")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(temp[0]))
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                        .setAutoCancel(true).setContentText(temp[0]);
        SharedPreferences prefs = getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        String phone = prefs.getString("phone", "");

        if (temp[1].equals("NewPlan")) {
            SharedPreferences.Editor editor = prefs.edit();
            String selectedPlanIndex = temp[2];
            editor.putString("selectedPlanIndex", selectedPlanIndex);
            editor.apply();
            String searchQuery = "/fetchPlan?planIndex=" + selectedPlanIndex;
            SyncClient restClient = new SyncClient(this);
            restClient.execute(new String[]{searchQuery, temp[1], phone});
            contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, HomeViewPlanActivity.class), 0);
        } else if (temp[1].equals("EditPlan")) {
            SharedPreferences.Editor editor = prefs.edit();
            String selectedPlanIndex = temp[2];
            editor.putString("selectedPlanIndex", selectedPlanIndex);
            editor.apply();
            String searchQuery = "/fetchPlan?planIndex=" + selectedPlanIndex;
            SyncClient restClient = new SyncClient(this);
            restClient.execute(new String[]{searchQuery, temp[1], phone});
            contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, HomeViewPlanActivity.class), 0);
        } else if (temp[1].equals("Rsvp")) {
            SharedPreferences.Editor editor = prefs.edit();
            String selectedPlanIndex = temp[2];
            editor.putString("selectedPlanIndex", selectedPlanIndex);
            editor.apply();
            String searchQuery = "/fetchPlan?planIndex=" + selectedPlanIndex;
            SyncClient restClient = new SyncClient(this);
            restClient.execute(new String[]{searchQuery, temp[1], phone});
            contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, HomeViewPlanActivity.class), 0);
        } else if (temp[1].equals("DeletePlan")) {
            PlanDAO planDAO = new PlanDAO(this);
            String selectedPlanIndex = temp[2];
            planDAO.deletePlan(selectedPlanIndex);
            contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, HomeActivity.class), 0);
        } else if (temp[1].equals("NewGroup")) {
            SharedPreferences.Editor editor = prefs.edit();
            String selectedGroupIndex = temp[2];
            editor.putString("selectedGroupIndex", selectedGroupIndex);
            editor.apply();
            String searchQuery = "/fetchGroup?groupIndex=" + selectedGroupIndex;
            SyncClient restClient = new SyncClient(this);
            restClient.execute(new String[]{searchQuery, temp[1], phone});
            contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, HomeGroupActivity.class), 0);
        } else if (temp[1].equals("NewMembers")) {
            SharedPreferences.Editor editor = prefs.edit();
            String selectedGroupIndex = temp[2];
            editor.putString("selectedGroupIndex", selectedGroupIndex);
            editor.apply();
            String searchQuery = "/fetchGroup?groupIndex=" + selectedGroupIndex;
            SyncClient restClient = new SyncClient(this);
            restClient.execute(new String[]{searchQuery, temp[1], phone});
            contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, HomeGroupActivity.class), 0);
        } else if (temp[1].equals("DeleteGroup")) {
            String selectedGroupIndex = temp[2];
            GroupDAO groupDAO = new GroupDAO(this);
            groupDAO.deleteGroup(selectedGroupIndex);
            contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, HomeActivity.class), 0);
        } else if (temp[1].equals("NewExpense")) {
            SharedPreferences.Editor editor = prefs.edit();
            String selectedPlanIndex = temp[3];
            String expenseId = temp[2];
            editor.putString("selectedPlanIndex", selectedPlanIndex);
            editor.apply();
            String searchQuery = "/fetchExpense?expenseId=" + expenseId;
            SyncClient restClient = new SyncClient(this);
            restClient.execute(new String[]{searchQuery, temp[1], phone});
            contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, HomePlanHistoryActivity.class), 0);
        } else if (temp[1].equals("UpdateExpense")) {
            SharedPreferences.Editor editor = prefs.edit();
            String selectedPlanIndex = temp[3];
            String expenseId = temp[2];
            editor.putString("selectedPlanIndex", selectedPlanIndex);
            editor.apply();
            String searchQuery = "/fetchExpense?expenseId=" + expenseId;
            SyncClient restClient = new SyncClient(this);
            restClient.execute(new String[]{searchQuery, temp[1], phone});
            contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, HomePlanHistoryActivity.class), 0);
        } else if (temp[1].equals("DeleteExpense")) {
            SharedPreferences.Editor editor = prefs.edit();
            String expenseId = temp[2];
            ExpenseDAO expenseDAO = new ExpenseDAO(this);
            expenseDAO.deleteExpense(expenseId);
            contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, HomeActivity.class), 0);
        }

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private class SyncClient extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private String phone;
        private String query;
        private String gcmType;

        public SyncClient(Context mContext) {
            this.mContext = mContext;
        }


        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {
            query = params[0];
            gcmType = params[1];
            String path = JMConstants.SERVICE_PATH + query;
            phone = params[2];

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
            if (response != null && gcmType.equals("NewPlan")) {
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
                    if (!phone.equals(plan.getCreator())) {
                        PlanDAO planDAO = new PlanDAO(mContext);
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
                        List<String> groups = plan.getGroupsInvited();
                        List<String> phones = plan.getMembersInvited();
                        planDAO.addPlan(String.valueOf(plan.getId()), plan.getTitle(),
                                startTime, plan.getLocation(),
                                plan.getCreator(), plan.getCreator(), endTime,
                                JMUtil.listToCommaDelimitedString(groups),
                                JMUtil.listToCommaDelimitedString(phones));

                        //TODO Remove
                        Plan dbplan = planDAO.fetchPlan(String.valueOf(plan.getId()));
                        if(dbplan != null){
                            Log.i(TAG, "Plan added: "+dbplan.getTitle());
                        }
                    }
                }
            }

            if (response != null && gcmType.equals("EditPlan")) {
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
                    if (!phone.equals(plan.getCreator())) {
                        PlanDAO planDAO = new PlanDAO(mContext);
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
                        planDAO.editPlan(String.valueOf(plan.getId()), plan.getTitle(),
                                startTime, plan.getLocation(),
                                endTime);

                        //TODO Remove
                        Plan dbplan = planDAO.fetchPlan(String.valueOf(plan.getId()));
                        if(dbplan != null){
                            Log.i(TAG, "Plan updated: "+dbplan.getTitle());
                        }
                    }
                }
            }

            if (response != null && gcmType.equals("Rsvp")) {
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
                    if (!phone.equals(plan.getCreator())) {
                        PlanDAO planDAO = new PlanDAO(mContext);
                        List<String> membersAttending = plan.getMembersAttending();
                        List<String> membersInvited = plan.getMembersInvited();
                        planDAO.updateMembers(String.valueOf(plan.getId()),
                                JMUtil.listToCommaDelimitedString(membersAttending),
                                JMUtil.listToCommaDelimitedString(membersInvited));

                        //TODO Remove
                        Plan dbplan = planDAO.fetchPlan(String.valueOf(plan.getId()));
                        if(dbplan != null){
                            List<String> dbMembersAttending = dbplan.getMembersAttending();
                            if(dbMembersAttending != null && !dbMembersAttending.isEmpty()){
                                Log.i(TAG, "Plan member updated: "+dbMembersAttending.size());
                            }

                        }
                    }
                }
            }

            if (response != null && gcmType.equals("NewGroup")) {
                XStream xstream = new XStream();
                xstream.alias("Group", Group.class);
                xstream.alias("members", String.class);
                xstream.addImplicitCollection(Group.class, "members",
                        "members", String.class);
                Group group = (Group) xstream.fromXML(response);

                if (group != null) {

                    if (!phone.equals(group.getAdmin())) {
                        Log.i(TAG, "Group addition GCM ");
                        GroupDAO groupDAO = new GroupDAO(mContext);
                        SharedPreferences prefs = getSharedPreferences("Prefs",
                                Activity.MODE_PRIVATE);
                        String members = JMUtil.listToCommaDelimitedString(group.getMembers());
                        groupDAO.addGroup(group.getId(), group.getName(),
                                members, group.getImage(), group.getAdmin(), phone);

                        //TODO Remove
                        Group dbgroup = groupDAO.fetchGroup(group.getId());
                        if(dbgroup != null){
                            if(dbgroup != null){
                                Log.i(TAG, "Group added: "+dbgroup.getName());
                            }

                        }
                    }
                }
            }

            if (response != null && gcmType.equals("NewMembers")) {
                XStream xstream = new XStream();
                xstream.alias("Group", Group.class);
                Group group = (Group) xstream.fromXML(response);
                xstream.alias("members", String.class);
                xstream.addImplicitCollection(Group.class, "members",
                        "members", String.class);
                if (group != null) {
                    if (!phone.equals(group.getAdmin())) {
                        GroupDAO groupDAO = new GroupDAO(mContext);
                        SharedPreferences prefs = getSharedPreferences("Prefs",
                                Activity.MODE_PRIVATE);
                        String members = JMUtil.listToCommaDelimitedString(group.getMembers());
                        groupDAO.updateGroupMembers(group.getId(),
                                members);
                        //TODO Remove
                        Group dbgroup = groupDAO.fetchGroup(group.getId());
                        if(dbgroup != null){
                            List<String> dbMembers = dbgroup.getMembers();
                            if(dbgroup != null){
                                Log.i(TAG, "Group members: "+dbMembers.size());
                            }

                        }
                    }
                }
            }

            if (response != null && gcmType.equals("NewExpense")) {
                XStream xstream = new XStream();
                xstream.alias("Expense", Expense.class);
                Expense expense = (Expense) xstream.fromXML(response);
                if (expense != null) {
                    if (!phone.equals(expense.getPhone())) {
                        ExpenseDAO expenseDAO = new ExpenseDAO(mContext);
                        expenseDAO.addExpense(expense.getId(), expense.getPhone(), expense.getPlanId(), expense.getTitle(), Integer.valueOf(expense.getValue()));
                        //TODO Remove
                        List<Expense> dbexpenses = expenseDAO.fetchExpense(expense.getPhone(), expense.getPlanId());
                        if(dbexpenses != null){
                            Log.i(TAG, "Expense added: "+dbexpenses.size());
                        }
                    }
                }
            }

            if (response != null && gcmType.equals("UpdateExpense")) {
                XStream xstream = new XStream();
                xstream.alias("Expense", Expense.class);
                Expense expense = (Expense) xstream.fromXML(response);
                if (expense != null) {
                    if (!phone.equals(expense.getPhone())) {
                        ExpenseDAO expenseDAO = new ExpenseDAO(mContext);
                        expenseDAO.updateExpense(expense.getId(), expense.getTitle(), Integer.valueOf(expense.getValue()));
                        //TODO Remove
                        List<Expense> dbexpenses = expenseDAO.fetchExpense(expense.getPhone(), expense.getPlanId());
                        if(dbexpenses != null){
                            Log.i(TAG, "Expense added: "+dbexpenses.size());
                        }
                    }
                }
            }
        }
    }
}
