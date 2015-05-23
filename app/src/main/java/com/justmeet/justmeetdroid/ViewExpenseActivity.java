package com.justmeet.justmeetdroid;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.justmeet.dao.ExpenseDAO;
import com.justmeet.entity.Expense;
import com.justmeet.entity.ExpenseList;
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

public class ViewExpenseActivity extends Activity {
    private static final String TAG = "View Expense";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (JMUtil.haveInternet(this)) {
            setContentView(R.layout.view_expense);
            ActionBar aBar = getActionBar();
            Resources res = getResources();
            Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
            aBar.setBackgroundDrawable(actionBckGrnd);
            aBar.setTitle(" My Expense List");

            SharedPreferences prefs = getSharedPreferences("Prefs",
                    Activity.MODE_PRIVATE);

            String userName = prefs.getString("selectedUser", "");
            String phone = prefs.getString("selectedPhone", "");
            String selectedPlanIndex = prefs.getString("selectedPlanIndex", "");

            TextView addLabel = (TextView) findViewById(R.id.viewexpenseLabel);
            addLabel.setText(userName + "'s Expenses:");

            ExpenseDAO expenseDAO = new ExpenseDAO(this);
            List<Expense> expenses = expenseDAO.fetchExpense(phone, selectedPlanIndex);
            if (expenses != null && !expenses.isEmpty()) {
                populateExpenseDetails(expenses);
            } else {
                Log.i(TAG, "No expense found in local DB!");
                String searchQuery = "/fetchExpenses?phone=" + phone + "&planIndex=" + selectedPlanIndex;
                ViewExpenseClient restClient = new ViewExpenseClient(this);
                restClient.execute(new String[]{searchQuery});
            }
        } else {
            Intent intent = new Intent(this, RetryActivity.class);
            startActivity(intent);
        }

    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomePlanHistoryActivity.class);
        startActivity(intent);
    }

    public class ViewExpenseClient extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private ProgressDialog pDlg;

        public ViewExpenseClient(Context mContext) {
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
            if (response != null && response.contains("ExpenseList")) {
                XStream xstream = new XStream();
                xstream.alias("ExpenseList", ExpenseList.class);
                xstream.alias("expenses", Expense.class);
                xstream.addImplicitCollection(ExpenseList.class, "expenses");
                ExpenseList expenseList = (ExpenseList) xstream
                        .fromXML(response);
                if (expenseList != null && expenseList.getExpenses() != null) {
                    List<Expense> expenses = expenseList.getExpenses();
                    if (expenses != null && !expenses.isEmpty()) {
                        populateExpenseDetails(expenses);
                    }
                }
            }
            pDlg.dismiss();
        }

    }

    private void populateExpenseDetails(List<Expense> expenses) {
        int size = expenses.size();
        Expense expense1 = expenses.get(0);
        if (expense1 != null) {
            if (expense1 != null) {
                TextView expenseTitle = (TextView) findViewById(R.id.viewexpense1Title);
                TextView expenseValue = (TextView) findViewById(R.id.viewexpense1Value);
                expenseTitle.setText(expense1.getTitle());
                expenseValue.setText(String.valueOf(expense1
                        .getValue()));
            }
        }
        if (size > 1) {
            Expense expense2 = expenses.get(1);
            if (expense2 != null) {
                TextView expenseTitle = (TextView) findViewById(R.id.viewexpense2Title);
                TextView expenseValue = (TextView) findViewById(R.id.viewexpense2Value);
                expenseTitle.setText(expense2.getTitle());
                expenseValue.setText(String.valueOf(expense2
                        .getValue()));
            }
        }
        if (size > 2) {
            Expense expense3 = expenses.get(2);
            if (expense3 != null) {
                TextView expenseTitle = (TextView) findViewById(R.id.viewexpense3Title);
                TextView expenseValue = (TextView) findViewById(R.id.viewexpense3Value);
                expenseTitle.setText(expense3.getTitle());
                expenseValue.setText(String.valueOf(expense3
                        .getValue()));
            }
        }
        if (size > 3) {
            Expense expense4 = expenses.get(3);
            if (expense4 != null) {
                TextView expenseTitle = (TextView) findViewById(R.id.viewexpense4Title);
                TextView expenseValue = (TextView) findViewById(R.id.viewexpense4Value);
                expenseTitle.setText(expense4.getTitle());
                expenseValue.setText(String.valueOf(expense4
                        .getValue()));
            }
        }
        if (size > 4) {
            Expense expense5 = expenses.get(4);
            if (expense5 != null) {
                TextView expenseTitle = (TextView) findViewById(R.id.viewexpense5Title);
                TextView expenseValue = (TextView) findViewById(R.id.viewexpense5Value);
                expenseTitle.setText(expense5.getTitle());
                expenseValue.setText(String.valueOf(expense5
                        .getValue()));
            }
        }
    }
}

