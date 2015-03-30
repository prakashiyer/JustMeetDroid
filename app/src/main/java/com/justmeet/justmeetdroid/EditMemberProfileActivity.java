package com.justmeet.justmeetdroid;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;


/**
 * Created by praxiyer on 11-03-2015.
 */
public class EditMemberProfileActivity extends Activity {
    private static final String TAG = "Just Meet/EditProfileActivity";
    private static final int PICK_IMAGE = 1;
    private ImageView imgView;
    private String filePath;
    Context context;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (JMUtil.haveInternet(this)) {
            SharedPreferences prefs = getSharedPreferences("Prefs",
                    Activity.MODE_PRIVATE);
            setContentView(R.layout.edit_profile);
            ActionBar aBar = getActionBar();
            Resources res = getResources();
            Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
            aBar.setBackgroundDrawable(actionBckGrnd);
            aBar.setTitle(" Profile Details");

            String phone = prefs.getString("phone", "");

            UserDAO userDAO = new UserDAO(this);
            User user = userDAO.fetchUser(phone);
            if (user != null && user.getName() != null) {
                populateDetails(user);
            } else {
                String userQuery = "/fetchUser?phone=" + phone;
                MemberProfileClient userRestClient = new MemberProfileClient(this);
                userRestClient.execute(new String[]{userQuery});
            }
        } else {
            Intent intent = new Intent(this, RetryActivity.class);
            startActivity(intent);
        }
    }

    public void editProfile(View view) {
        if (bitmap == null) {
            Toast.makeText(getApplicationContext(), "Please select image",
                    Toast.LENGTH_SHORT).show();
        } else {
            SharedPreferences prefs = getSharedPreferences("Prefs",
                    Activity.MODE_PRIVATE);
            String phone = prefs.getString("phone", "");
            addToServer(phone);

        }
    }

    private byte[] addToServer(String phone) {
        EditProfileClient restClient = new EditProfileClient(this);
        EditText userNameValue = (EditText) findViewById(R.id.editName);
        String name = userNameValue.getText().toString();
        restClient.execute(new String[]{"editUser", phone, filePath, name});
        return null;
    }

    private void addToPhoneDB(String phone, byte[] image, String name) {
        UserDAO userDAO = new UserDAO(this);
        userDAO.updateUser(phone, image, name);
    }

    public void editImage(View view) {
        try {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, "Select an image"), PICK_IMAGE);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Image selection failed",
                    Toast.LENGTH_LONG).show();
            Log.e(e.getClass().getName(), e.getMessage(), e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImageUri = data.getData();

                    try {
                        // OI FILE Manager
                        String filemanagerstring = selectedImageUri.getPath();

                        // MEDIA GALLERY
                        String selectedImagePath = getPath(selectedImageUri);

                        if (selectedImagePath != null) {
                            filePath = selectedImagePath;
                        } else if (filemanagerstring != null) {
                            filePath = filemanagerstring;
                        } else {
                            Toast.makeText(getApplicationContext(), "Unknown path",
                                    Toast.LENGTH_LONG).show();
                            Log.e("Bitmap", "Unknown path");
                        }

                        if (filePath != null) {
                            decodeFile(filePath);
                        } else {
                            bitmap = null;
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Internal error",
                                Toast.LENGTH_LONG).show();
                        Log.e(e.getClass().getName(), e.getMessage(), e);
                    }
                }
                break;
            default:
        }
    }

    @SuppressWarnings("deprecation")
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    public void decodeFile(String filePath) {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 1024;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        bitmap = BitmapFactory.decodeFile(filePath, o2);

        imgView.setImageBitmap(bitmap);
    }

    private void populateDetails(User user) {
        TextView phoneValue = (TextView) findViewById(R.id.editPhone);
        phoneValue.setText(user.getPhone());
        EditText userNameValue = (EditText) findViewById(R.id.editName);
        userNameValue.setText(user.getName());
        ImageView imgView = (ImageView) findViewById(R.id.editProfilePicThumbnail);
        byte[] image = user.getImage();
        if (image != null) {
            Bitmap img = BitmapFactory.decodeByteArray(image, 0,
                    image.length);
            imgView.setImageBitmap(img);
        }
    }

    private class MemberProfileClient extends
            AsyncTask<String, Integer, String> {

        private Context mContext;
        private ProgressDialog pDlg;
        private String query;

        public MemberProfileClient(Context mContext) {
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
            if (response != null && query.contains("fetchUser")) {
                Log.i(TAG, response);
                XStream userXs = new XStream();
                userXs.alias("UserInformation", User.class);
                userXs.alias("groupIds", String.class);
                userXs.addImplicitCollection(User.class, "groupIds", "groupIds",
                        String.class);
                User user = (User) userXs.fromXML(response);
                if (user != null && user.getName() != null) {
                    populateDetails(user);
                }
            }
            pDlg.dismiss();
        }

    }

    private class EditProfileClient extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private ProgressDialog pDlg;

        public EditProfileClient(Context mContext) {
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

            String method = params[0];
            String path = JMConstants.SERVICE_PATH + "/" + method;

            //HttpHost target = new HttpHost(TARGET_HOST);
            HttpHost target = new HttpHost(JMConstants.TARGET_HOST, 8080);
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(path);

            HttpEntity results = null;
            try {
                MultipartEntity entity = new MultipartEntity();
                entity.addPart("phone", new StringBody(params[1]));
                entity.addPart("image", new FileBody(new File(params[2])));
                entity.addPart("name", new StringBody(params[2]));
                post.setEntity(entity);

                HttpResponse response = client.execute(target, post);
                results = response.getEntity();
                return EntityUtils.toString(results);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            Button button = (Button) findViewById(R.id.userImageButton);
            if (response != null) {
                Log.i(TAG, response);
                XStream userXs = new XStream();
                userXs.alias("UserInformation", User.class);
                userXs.alias("groupIds", String.class);
                userXs.addImplicitCollection(User.class, "groupIds", "groupIds",
                        String.class);
                User user = (User) userXs.fromXML(response);
                if (user != null && user.getName() != null) {
                    addToPhoneDB(user.getPhone(), user.getImage(), user.getName());
                }
            } else {
                Toast.makeText(getApplicationContext(),
                        "Edit Failed.", Toast.LENGTH_LONG)
                        .show();
            }
            button.setTextColor(getResources().getColor(R.color.button_text));
            pDlg.dismiss();
        }
    }


}
