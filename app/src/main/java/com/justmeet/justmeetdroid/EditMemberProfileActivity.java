package com.justmeet.justmeetdroid;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
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

import com.google.android.gms.drive.internal.bi;
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
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;


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
    private byte[] croppedImage;
    Uri passedUri;

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

            imgView = (ImageView) findViewById(R.id.editProfilePicThumbnail);

            String phone = prefs.getString("phone", "");

            UserDAO userDAO = new UserDAO(this);
            User user = userDAO.fetchUser(phone);
            if (user != null && user.getName() != null) {
                populateDetails(user);
            } else {
                Log.i(TAG, "No user found in local DB!");
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
        SharedPreferences prefs = getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        String phone = prefs.getString("phone", "");
        addToServer(phone);
    }

    private byte[] addToServer(String phone) {
        EditProfileClient restClient = new EditProfileClient(this);
        EditText userNameValue = (EditText) findViewById(R.id.editName);
        String name = userNameValue.getText().toString();
        Log.i(TAG, "Name: "+name+" : "+name.length());
        restClient.execute(new String[]{"editUser", phone, filePath, name});
        return null;
    }

    private void addToPhoneDB(String phone, byte[] image, String name) {
        UserDAO userDAO = new UserDAO(this);
        userDAO.updateUser(phone, image, name);
        //TODO Remove
        User user = userDAO.fetchUser(phone);
        if(user != null){
            String dbname = user.getName();
            Log.i(TAG, "User updated: "+dbname);
        }
    }

    public void editImage(View view) {
        try {

            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.putExtra("crop", true);
            startActivityForResult(Intent.createChooser(galleryIntent, "Select an image"), PICK_IMAGE);

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
                    System.out.println("selectedImageUri" + selectedImageUri);
                    try {
                        // OI FILE Manager
                        String fileManagerString = selectedImageUri.getPath();

                        // MEDIA GALLERY
                        String selectedImagePath = getPath(selectedImageUri);

                        if (selectedImagePath != null) {
                            filePath = selectedImagePath;
                        } else if (fileManagerString != null) {
                            filePath = fileManagerString;
                        } else {
                            Toast.makeText(getApplicationContext(), "Unknown path",
                                    Toast.LENGTH_LONG).show();
                            Log.e("Bitmap", "Unknown path");
                        }

                        if (selectedImageUri != null) {
                            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));
                            decodeFile(null);
                            //cropImage(selectedImageUri);
                        } else {
                            bitmap = null;
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Internal error",
                                Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
                break;
            case 2: {
                System.out.println("Reached case 2");
                if(data != null) {
                    Bundle extras = data.getExtras();
                    if(extras != null){
                        bitmap = extras.getParcelable("data");
                        decodeFile(null);
                        break;
                    }
                } else {
                    BitmapFactory.Options o2 = new BitmapFactory.Options();
                    System.out.println("PASSED URI: " +passedUri);
                    System.out.println("ENC PATH : " +passedUri.getEncodedPath());
                    System.out.println("PATH: " +passedUri.getPath());
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(passedUri));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    decodeFile(null);
                    break;
                }
                Toast.makeText(getApplicationContext(), "Please select an image.",
                        Toast.LENGTH_LONG).show();
                break;

            }
            default:
        }
    }

    public void cropImage(Uri picUri)
    {
        System.out.println("IN CROP EDIT IMAGE METHOD " + picUri);
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        try
        {
            passedUri = picUri;
            String newPath = picUri.getPath() + "_1";
            //passedUri = Uri.parse(newPath);
            cropIntent.setDataAndNormalize(picUri);
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("return-data", true);
            cropIntent.putExtra("aspectX", 300);
            cropIntent.putExtra("aspectY", 300);
            cropIntent.putExtra("outputX", 300);
            cropIntent.putExtra("outputY", 300);
            cropIntent.putExtra("output", picUri);
            System.out.println("Starting Crop Image Activity ");
            startActivityForResult(cropIntent, 2);
            System.out.println("Done cropping ");
        }
        catch (Exception anfe)
        {
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            System.out.println("error occured : " + errorMessage);
            anfe.printStackTrace();

        }
    }

    @SuppressWarnings("deprecation")
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
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
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        //bitmap = BitmapFactory.decodeFile(imageUri.getPath(), o2);
        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos1);
        byte[] data1 = bos1.toByteArray();
        System.out.println("Size : Before *** " + bitmap.getByteCount());
        System.out.println("BOS Size : Before *** " + bos1.size()/1024);
        System.out.println("Data Size : Before *** " + data1.length/1024);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, bos);
        croppedImage = bos.toByteArray();
        System.out.println("BOS Size : After *** " + bos.size()/1024);
        System.out.println("Data Size : After *** " + croppedImage.length/1024);
        //ByteArrayBody bab = new ByteArrayBody(data, imageUri.getPath());
        //System.out.println("BAB Size : *** " + bab.getContentLength());
        if (bos.size()/1024 > 4096) {
            Toast.makeText(getApplicationContext(),
                    "Please select a smaller image!!", Toast.LENGTH_LONG)
                    .show();
        } else {
            Toast.makeText(getApplicationContext(),
                    "Selected image has been set!!", Toast.LENGTH_LONG)
                    .show();
            imgView.setImageBitmap(BitmapFactory.decodeByteArray(croppedImage,0,croppedImage.length));
        }
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
            croppedImage = image;
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
            String phone = params[1];
            String path = JMConstants.SERVICE_PATH + "/" + method;

            //HttpHost target = new HttpHost(TARGET_HOST);
            HttpHost target = new HttpHost(JMConstants.TARGET_HOST, 8080);
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(path);

            HttpEntity results = null;
            try {
                MultipartEntity entity = new MultipartEntity();
                entity.addPart("phone", new StringBody(phone));
                if(croppedImage != null){
                    entity.addPart("image", new ByteArrayBody(croppedImage, phone+ ".jpg"));
                }
                entity.addPart("name", new StringBody(params[3]));
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
            pDlg.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

}
