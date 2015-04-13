package com.justmeet.justmeetdroid;

import android.accounts.Account;
import android.accounts.AccountManager;
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
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.justmeet.dao.UserDAO;
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

import java.io.IOException;

/**
 * Created by praxiyer on 01-02-2015.
 */
public class NewUserActivity extends Activity {
    private static final String TAG = "New User";
    private SmsManager smsManager;
    private Context context;
    private String code;
    private GoogleCloudMessaging gcm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (JMUtil.haveInternet(this)) {
            setContentView(R.layout.new_user);
            ActionBar aBar = getActionBar();
            Resources res = getResources();
            Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
            aBar.setBackgroundDrawable(actionBckGrnd);
            aBar.setTitle(" Sign Up Form");
            gcm = GoogleCloudMessaging.getInstance(this);
            context = this;
            smsManager = SmsManager.getDefault();
        } else {
            Intent intent = new Intent(this, RetryActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Called when the user checks sms validation
     *
     * @param view
     */
    public void enterCodeCheck(View view) {
        CheckBox checkBox = (CheckBox) findViewById(R.id.smsCheckBox);
        EditText phoneText = (EditText) findViewById(R.id.newUserPhone);
        String phone = phoneText.getText().toString();
        EditText newPassword = (EditText) findViewById(R.id.newUserCode);
        Button regButton = (Button) findViewById(R.id.registerButton);
        if (checkBox.isChecked()) {
            if (!TextUtils.isEmpty(phone)) {
                code = phone.substring(3, 7);
                String message = "Welcome to Just Meet. Please enter the code " + code + " to validate this phone number.";
                smsManager.sendTextMessage(phone, null, message, null, null);
                newPassword.setVisibility(EditText.VISIBLE);
                regButton.setVisibility(Button.VISIBLE);
            } else {
                Toast.makeText(getApplicationContext(),
                        "Please provide a valid phone number.", Toast.LENGTH_LONG).show();
            }
        } else {
            newPassword.setVisibility(EditText.INVISIBLE);
            regButton.setVisibility(Button.INVISIBLE);
        }
    }

    /**
     * Called when the user clicks the New User Register button
     *
     * @param view
     */
    public void logNewUser(View view) {
        Button button = (Button) findViewById(R.id.registerButton);
        button.setTextColor(getResources().getColor(R.color.click_button_1));
        EditText userNameEditText = (EditText) findViewById(R.id.newUserName);
        String userName = userNameEditText.getText().toString();

        EditText phoneText = (EditText) findViewById(R.id.newUserPhone);
        String phone = phoneText.getText().toString();

        EditText passwordEditText = (EditText) findViewById(R.id.newUserCode);
        String password = passwordEditText.getText().toString();

        boolean validUser = isUserValid(userName, phone, password);
        if (validUser) {
            addToServer(userName, phone);
            button.setTextColor(getResources().getColor(
                    R.color.button_text));
        }
    }

    private void goToNextPage() {
        Intent intent = new Intent(this,
                UserImageActivity.class);
        startActivity(intent);
    }

    private void addPhoneAccount(String userName, String phone) {
        AccountManager am = AccountManager.get(this);
        final Account account = new Account(phone,
                JMConstants.ACCOUNT_ADDRESS);
        final Bundle bundle = new Bundle();
        bundle.putString("userName", userName);
        bundle.putString("phone", phone);
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME,
                account.name);
        am.addAccountExplicitly(account, phone, bundle);
        am.setAuthToken(account, "Full Access", phone);
    }

    private void addToServer(String userName, String phone) {
        String insertQuery = "/addUser?name="
                + userName.replace(" ", "%20") + "&phone=" + phone;
        NewUserClient restClient = new NewUserClient(this);
        restClient.execute(new String[]{insertQuery});
    }

    /**
     * Checks if user is valid.
     *
     * @param userName
     * @param phone
     * @param password
     * @return
     */
    private boolean isUserValid(String userName, String phone, String password) {
        boolean validUser = true;
        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(getApplicationContext(), "Enter a valid Name",
                    Toast.LENGTH_LONG).show();
            validUser = false;
        } else if (TextUtils.isEmpty(phone)) {
            Toast.makeText(getApplicationContext(),
                    "Enter a valid mobile number", Toast.LENGTH_LONG).show();
            validUser = false;
        } else if (TextUtils.isEmpty(password) && !TextUtils.equals(password, code)) {
            Toast.makeText(getApplicationContext(),
                    "The code entered is invalid.", Toast.LENGTH_LONG)
                    .show();
            validUser = false;
        } else {
            Asyncer syncer = new Asyncer();
            syncer.execute(new String[]{phone});
        }
        return validUser;
    }

    /**
     * For GCM registration and storage
     *
     * @author praxiyer
     */
    private class Asyncer extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {
            String msg = "";
            String regid = "";
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(context);
            }
            try {
                Log.i(TAG, "Registering GCM");
                regid = gcm.register(JMConstants.SENDER_ID);
            } catch (IOException ex) {
                msg = "Error :" + ex.getMessage();
                Log.e(TAG, msg);


                // If there is an error, don't just keep trying to register.
                // Require the user to click a button again, or perform
                // exponential back-off.
            }
            msg = "Device registered, registration ID=" + regid;
            Log.i(TAG, msg);

            if (regid != null && regid != "") {
                // Persist the regID - no need to register again.
                storeRegistrationId(regid);

                // Store the reg id in server

                String path = JMConstants.SERVICE_PATH + "/addRegId?regId="
                        + regid + "&phone=" + params[0];

                HttpHost target = new HttpHost(JMConstants.TARGET_HOST, 8080);
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(path);
                try {
                    client.execute(target, get);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) {

        }

    }

    /**
     * REST Client to add a new user.
     */
    private class NewUserClient extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private ProgressDialog pDlg;

        public NewUserClient(Context mContext) {
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
                Log.e(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                XStream xstream = new XStream();
                xstream.alias(JMConstants.XS_USER, User.class);
                xstream.alias(JMConstants.XS_GROUP_IDS, String.class);
                xstream.addImplicitCollection(User.class, JMConstants.XS_GROUP_IDS, JMConstants.XS_GROUP_IDS,
                        String.class);
                User user = (User) xstream
                        .fromXML(response);
                if (user != null) {
                    String name = user.getName();
                    String phone = user.getPhone();
                    addCache(name, phone);
                    addPhoneAccount(name, phone);
                    addToPhoneDB(name, phone);
                    pDlg.dismiss();
                    goToNextPage();
                } else {
                    Log.e(TAG, "User is null");
                }
            }
            pDlg.dismiss();
        }

        private void addCache(String inName, String inPhone) {
            SharedPreferences prefs = getSharedPreferences("Prefs",
                    Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("phone", inPhone);
            editor.putString("userName", inName);
            editor.apply();
        }

    }

    /**
     * Add user details to phone DB
     *
     * @param name
     * @param phone
     */
    private void addToPhoneDB(String name, String phone) {
        UserDAO userDAO = new UserDAO(context);
        userDAO.addUser(name, phone);
        //TODO Remove
        User user = userDAO.fetchUser(phone);
        if(user != null){
            String dbname = user.getName();
            Log.i(TAG, "User added: "+dbname);
        }
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param regId registration ID
     */
    private void storeRegistrationId(String regId) {
        SharedPreferences prefs = getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("regId", regId);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case (R.id.aboutUs):
                Intent intent = new Intent(this, AboutUsActivity.class);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }
}
