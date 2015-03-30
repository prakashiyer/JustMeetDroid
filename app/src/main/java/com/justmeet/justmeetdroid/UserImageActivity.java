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
import android.widget.ImageView;
import android.widget.Toast;

import com.justmeet.dao.UserDAO;
import com.justmeet.entity.User;
import com.justmeet.util.JMConstants;
import com.justmeet.util.JMUtil;

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
import java.util.concurrent.ExecutionException;

/**
 * Created by praxiyer on 04-02-2015.
 */
public class UserImageActivity extends Activity {

    private static final String TAG = "User Image Activity";
    private static final int PICK_IMAGE = 1;
    private ImageView imgView;
    private String filePath;
    Context context;
    private Bitmap bitmap;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (JMUtil.haveInternet(this)) {
            setContentView(R.layout.profile_image_upload);
            ActionBar aBar = getActionBar();
            Resources res = getResources();
            Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
            aBar.setBackgroundDrawable(actionBckGrnd);
            aBar.setTitle(" Profile Photo Selection");
            SharedPreferences prefs = getSharedPreferences("Prefs",
                    Activity.MODE_PRIVATE);
            String phone = prefs.getString("phone", "");
            context = getApplicationContext();
            imgView = (ImageView) findViewById(R.id.profilePicView);

            UserDAO userDAO = new UserDAO(this);
            User user = userDAO.fetchUser(phone);
            byte[] image = user.getImage();

            if (user != null) {
                if (image != null) {
                    Bitmap img = BitmapFactory.decodeByteArray(image, 0,
                            image.length);
                    imgView.setImageBitmap(img);
                }
            } else {
                UserImageFetchClient imageFetchClient = new UserImageFetchClient(this);
                imageFetchClient.execute(
                        new String[]{"fetchUserImage", phone});
                Toast.makeText(getApplicationContext(),
                        "Touch the Image area to set a Profile image.", Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            Intent intent = new Intent(this, RetryActivity.class);
            startActivity(intent);
        }
    }

    public void uploadImage(View view) {
        Button button = (Button) findViewById(R.id.userImageButton);
        button.setTextColor(getResources().getColor(R.color.click_button_2));
        if (bitmap == null) {
            Toast.makeText(getApplicationContext(), "Please select image",
                    Toast.LENGTH_SHORT).show();
            button.setTextColor(getResources().getColor(R.color.button_text));
        } else {
            SharedPreferences prefs = getSharedPreferences("Prefs",
                    Activity.MODE_PRIVATE);
            String phone = prefs.getString("phone", "");
            byte[] image = addToServer(phone);
            if (image != null) {
                addToPhoneDB(phone, image);
                Toast.makeText(getApplicationContext(),
                        "You can use the menu to change image later.", Toast.LENGTH_LONG)
                        .show();
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
            }
        }
    }

    private byte[] addToServer(String phone) {
        UserImageUploadClient restClient = new UserImageUploadClient(this);
        try {
            return restClient.execute(new String[]{"uploadUserImage", phone, filePath}).get();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        } catch (ExecutionException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    private void addToPhoneDB(String phone, byte[] image) {
        UserDAO userDAO = new UserDAO(this);
        userDAO.updateUserImage(phone, image);
    }

    public void selectImage(View view) {
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

    private class UserImageFetchClient extends AsyncTask<String, Integer, byte[]> {

        private Context mContext;
        private ProgressDialog pDlg;

        public UserImageFetchClient(Context mContext) {
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
        protected byte[] doInBackground(String... params) {
            String method = params[0];
            String phone = params[1];
            String path = JMConstants.SERVICE_PATH + "/" + method + "?phone=" + phone;
            //HttpHost target = new HttpHost(TARGET_HOST);
            HttpHost target = new HttpHost(JMConstants.TARGET_HOST, 8080);
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(path);
            HttpEntity results = null;

            try {

                HttpResponse response = client.execute(target, get);
                results = response.getEntity();
                return EntityUtils.toByteArray(results);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(byte[] response) {
            if (response != null) {
                Bitmap img = BitmapFactory.decodeByteArray(response, 0,
                        response.length);
                if (img != null) {
                    imgView.setImageBitmap(img);
                }
            } else {
                imgView.setImageResource(R.drawable.ic_launcher);
            }
            pDlg.dismiss();
        }

    }

    private class UserImageUploadClient extends AsyncTask<String, Integer, byte[]> {

        private Context mContext;
        private ProgressDialog pDlg;

        public UserImageUploadClient(Context mContext) {
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
        protected byte[] doInBackground(String... params) {

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
                post.setEntity(entity);

                HttpResponse response = client.execute(target, post);
                results = response.getEntity();
                return EntityUtils.toByteArray(results);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(byte[] response) {
            Button button = (Button) findViewById(R.id.userImageButton);
            if (response != null) {
                Bitmap img = BitmapFactory.decodeByteArray(response, 0,
                        response.length);
                imgView.setImageBitmap(img);
                Toast.makeText(getApplicationContext(),
                        "Selected Photo has been set", Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Image upload failed. Please try again later.", Toast.LENGTH_LONG)
                        .show();
            }
            button.setTextColor(getResources().getColor(R.color.button_text));
            pDlg.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(),
                "You can use the menu to change image later.", Toast.LENGTH_LONG)
                .show();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}
