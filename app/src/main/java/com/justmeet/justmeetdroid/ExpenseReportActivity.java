package com.justmeet.justmeetdroid;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.justmeet.entity.ExpenseReport;
import com.justmeet.entity.ExpenseRow;
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

public class ExpenseReportActivity extends Fragment implements OnItemClickListener {

    ListView expenseReportListView;
    ExpenseListAdapter adapter;
    List<ExpenseRow> expenseRows;
    Activity activity;
    View rootView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        activity = this.getActivity();
        if (activity != null && JMUtil.haveInternet(activity)) {
            rootView = inflater.inflate(R.layout.expense_report, container, false);

            SharedPreferences prefs = activity.getSharedPreferences("Prefs",
                    Activity.MODE_PRIVATE);
            String selectedPlanIndex = prefs.getString("selectedPlanIndex", "");

            String searchQuery = "/generateReport?planId=" + selectedPlanIndex;

            adapter = new ExpenseListAdapter(activity, activity);
            expenseReportListView = (ListView) activity.findViewById(R.id.viewexpensereport);
            expenseReportListView.setOnItemClickListener(this);
            ExpenseReportClient restClient = new ExpenseReportClient(activity);
            restClient.execute(new String[]{searchQuery});
        } else {
            Intent intent = new Intent(activity, RetryActivity.class);
            startActivity(intent);
        }
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ExpenseRow expenseRow = expenseRows.get(position);
        SharedPreferences prefs = activity.getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        String phone = prefs.getString("phone", "New User");
        if (phone.equals(expenseRow.getPhone())) {
            //TODO On click open next tab for Add Expense
        } else {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("selectedUser", expenseRow.getName());
            editor.putString("selectedPhone", expenseRow.getPhone());
            editor.apply();
            Intent intent = new Intent(activity, ViewExpenseActivity.class);
            startActivity(intent);
        }
    }

    private class ExpenseReportClient extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private ProgressDialog pDlg;

        public ExpenseReportClient(Context mContext) {
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
                xstream.alias("ExpenseReport", ExpenseReport.class);
                xstream.alias("expenseRows", ExpenseRow.class);
                xstream.addImplicitCollection(ExpenseReport.class, "expenseRows");
                ExpenseReport expenseReport = (ExpenseReport) xstream
                        .fromXML(response);
                if (expenseReport != null) {
                    expenseRows = expenseReport
                            .getExpenseRows();
                    if (expenseRows != null && !expenseRows.isEmpty()) {
                        adapter.setData(expenseRows);
                        expenseReportListView.setAdapter(adapter);
                    }

                }
            }
            pDlg.dismiss();
        }

    }
}
