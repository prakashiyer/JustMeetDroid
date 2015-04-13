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
import android.widget.Button;
import android.widget.EditText;
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

/**
 * Created by praxiyer on 15-03-2015.
 */
public class AddExpenseActivity extends Fragment {
    private static final String TAG = "Add/Update Expense";
    private String exp1 = "";
    private String exp2 = "";
    private String exp3 = "";
    private String exp4 = "";
    private String exp5 = "";
    Activity activity;
    View rootView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this.getActivity();
        if (activity != null && JMUtil.haveInternet(activity)) {
            rootView = inflater.inflate(R.layout.add_expense, container, false);

            SharedPreferences prefs = activity.getSharedPreferences("Prefs",
                    Activity.MODE_PRIVATE);

            String userName = prefs.getString("userName", "");
            String phone = prefs.getString("phone", "");
            String selectedPlanIndex = prefs.getString("selectedPlanIndex", "");

            TextView addLabel = (TextView) activity.findViewById(R.id.addexpenseLabel);
            addLabel.setText(userName + "'s Expenses");

            String searchQuery = "/fetchExpenses?phone=" + phone + "&planIndex="
                    + selectedPlanIndex;

            AddExpenseClient restClient = new AddExpenseClient(activity);
            restClient.execute(new String[]{searchQuery});
        } else {
            Intent intent = new Intent(activity, RetryActivity.class);
            startActivity(intent);
        }
        return rootView;
    }

    /**
     * Called when the user clicks the Submit Expense button
     */
    public void submitExpense(View view) {

        Button button = (Button) activity.findViewById(R.id.submitExpense);
        button.setTextColor(getResources().getColor(R.color.click_button_1));

        SharedPreferences prefs = activity.getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);

        String phone = prefs.getString("phone", "");
        String selectedPlanIndex = prefs.getString("selectedPlanIndex", "");

        EditText expenseTitle1 = (EditText) activity.findViewById(R.id.addexpense1Title);
        EditText expenseValue1 = (EditText) activity.findViewById(R.id.addexpense1Value);
        String title1 = expenseTitle1.getText().toString();
        String val1 = expenseValue1.getText().toString();

        if (title1 != null && !title1.isEmpty() && val1 != null
                && !val1.isEmpty()) {
            updateExpense(phone, selectedPlanIndex, title1, val1,
                    exp1);
        }

        EditText expenseTitle2 = (EditText) activity.findViewById(R.id.addexpense2Title);
        EditText expenseValue2 = (EditText) activity.findViewById(R.id.addexpense2Value);
        String title2 = expenseTitle2.getText().toString();
        String val2 = expenseValue2.getText().toString();

        if (title2 != null && !title2.isEmpty() && val2 != null
                && !val2.isEmpty()) {
            updateExpense(phone, selectedPlanIndex, title2, val2,
                    exp2);
        }

        EditText expenseTitle3 = (EditText) activity.findViewById(R.id.addexpense3Title);
        EditText expenseValue3 = (EditText) activity.findViewById(R.id.addexpense3Value);
        String title3 = expenseTitle3.getText().toString();
        String val3 = expenseValue3.getText().toString();

        if (title3 != null && !title3.isEmpty() && val3 != null
                && !val3.isEmpty()) {
            updateExpense(phone, selectedPlanIndex, title3, val3,
                    exp3);
        }

        EditText expenseTitle4 = (EditText) activity.findViewById(R.id.addexpense4Title);
        EditText expenseValue4 = (EditText) activity.findViewById(R.id.addexpense4Value);
        String title4 = expenseTitle4.getText().toString();
        String val4 = expenseValue4.getText().toString();

        if (title4 != null && !title4.isEmpty() && val4 != null
                && !val4.isEmpty()) {
            updateExpense(phone, selectedPlanIndex, title4, val4,
                    exp4);
        }

        EditText expenseTitle5 = (EditText) activity.findViewById(R.id.addexpense5Title);
        EditText expenseValue5 = (EditText) activity.findViewById(R.id.addexpense5Value);
        String title5 = expenseTitle5.getText().toString();
        String val5 = expenseValue5.getText().toString();

        if (title5 != null && !title5.isEmpty() && val5 != null
                && !val5.isEmpty()) {
            updateExpense(phone, selectedPlanIndex, title5, val5,
                    exp5);
        }
        Intent intent = new Intent(activity, HomePlanHistoryActivity.class);
        startActivity(intent);

    }

    private void updateExpense(String phone, String selectedPlanIndex, String title, String val, String exp) {
        String query = "";
        if ("".equals(exp)) {
            query = "/addExpense?planIndex=" + selectedPlanIndex + "&phone=" + phone
                    + "&title=" + title + "&value=" + val;

        } else {
            query = "/updateExpense?id=" + exp + "&title=" + title + "&value=" + val;
        }

        AddExpenseClient restClient = new AddExpenseClient(activity);
        restClient.execute(new String[]{query});
    }


    public class AddExpenseClient extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private ProgressDialog pDlg;
        private String query;

        public AddExpenseClient(Context mContext) {
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
            query = params[0];
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

            if (response != null && query.contains("fetchExpense")) {
                XStream xstream = new XStream();
                xstream.alias("ExpenseList", ExpenseList.class);
                xstream.alias("expenses", Expense.class);
                xstream.addImplicitCollection(ExpenseList.class, "expenses");
                ExpenseList expenseList = (ExpenseList) xstream
                        .fromXML(response);
                if (expenseList != null && expenseList.getExpenses() != null) {

                    List<Expense> expenses = expenseList.getExpenses();

                    if (expenses != null && !expenses.isEmpty()) {
                        int size = expenses.size();
                        Expense expense1 = expenses.get(0);
                        if (expense1 != null) {
                            EditText expenseTitle = (EditText) activity.findViewById(R.id.addexpense1Title);
                            EditText expenseValue = (EditText) activity.findViewById(R.id.addexpense1Value);
                            expenseTitle.setText(expense1.getTitle());
                            expenseValue.setText(String.valueOf(expense1
                                    .getValue()));
                            exp1 = expense1.getId();
                        }

                        if (size > 1) {
                            Expense expense2 = expenses.get(1);
                            if (expense2 != null) {
                                EditText expenseTitle = (EditText) activity.findViewById(R.id.addexpense2Title);
                                EditText expenseValue = (EditText) activity.findViewById(R.id.addexpense2Value);
                                expenseTitle.setText(expense2.getTitle());
                                expenseValue.setText(String.valueOf(expense2
                                        .getValue()));
                                exp2 = expense2.getId();
                            }
                        }

                        if (size > 2) {
                            Expense expense3 = expenses.get(2);
                            if (expense3 != null) {
                                EditText expenseTitle = (EditText) activity.findViewById(R.id.addexpense3Title);
                                EditText expenseValue = (EditText) activity.findViewById(R.id.addexpense3Value);
                                expenseTitle.setText(expense3.getTitle());
                                expenseValue.setText(String.valueOf(expense3
                                        .getValue()));
                                exp3 = expense3.getId();
                            }
                        }

                        if (size > 3) {
                            Expense expense4 = expenses.get(3);
                            if (expense4 != null) {
                                EditText expenseTitle = (EditText) activity.findViewById(R.id.addexpense4Title);
                                EditText expenseValue = (EditText) activity.findViewById(R.id.addexpense4Value);
                                expenseTitle.setText(expense4.getTitle());
                                expenseValue.setText(String.valueOf(expense4
                                        .getValue()));
                                exp4 = expense4.getId();
                            }
                        }
                        if (size > 4) {
                            Expense expense5 = expenses.get(4);
                            if (expense5 != null) {
                                EditText expenseTitle = (EditText) activity.findViewById(R.id.addexpense5Title);
                                EditText expenseValue = (EditText) activity.findViewById(R.id.addexpense5Value);
                                expenseTitle.setText(expense5.getTitle());
                                expenseValue.setText(String.valueOf(expense5
                                        .getValue()));
                                exp5 = expense5.getId();
                            }
                        }
                    }

                }
            } else if (response != null && query.contains("addExpense")) {
                XStream xstream = new XStream();
                xstream.alias("Expense", Expense.class);
                Expense expense = (Expense) xstream
                        .fromXML(response);
                if (expense != null) {
                    ExpenseDAO expenseDAO = new ExpenseDAO(mContext);
                    expenseDAO.addExpense(expense.getId(), expense.getPhone(), expense.getPlanId(), expense.getTitle(), Integer.valueOf(expense.getValue()));
                    //TODO Remove
                    List<Expense> dbexpense = expenseDAO.fetchExpense(expense.getPhone(), expense.getPlanId());
                    if(dbexpense != null && !dbexpense.isEmpty()){
                        Log.i(TAG, "Expense added.");
                    }
                }

            } else if (response != null && query.contains("updateExpense")) {
                XStream xstream = new XStream();
                xstream.alias("Expense", Expense.class);
                Expense expense = (Expense) xstream
                        .fromXML(response);
                if (expense != null) {
                    ExpenseDAO expenseDAO = new ExpenseDAO(mContext);
                    expenseDAO.updateExpense(expense.getId(), expense.getTitle(), Integer.valueOf(expense.getValue()));
                    //TODO Remove
                    List<Expense> dbexpense = expenseDAO.fetchExpense(expense.getPhone(), expense.getPlanId());
                    if(dbexpense != null && !dbexpense.isEmpty()){
                        Log.i(TAG, "Expense updated.");
                    }
                }

            }
            pDlg.dismiss();
        }

    }
}
