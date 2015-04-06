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
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.justmeet.dao.GroupDAO;
import com.justmeet.entity.Group;
import com.justmeet.util.JMConstants;
import com.justmeet.util.JMUtil;
import com.thoughtworks.xstream.XStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by praxiyer on 20-03-2015.
 */
public class CreateGroupActivity extends FragmentActivity {

    private Context context;
    private String filePath;
    private Bitmap bitmap;
    private ImageView imgView;
    private static final int PICK_IMAGE = 1;
    private static final String TAG = "New Group";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (JMUtil.haveInternet(this)) {
            setContentView(R.layout.create_group);
            ActionBar aBar = getActionBar();
            Resources res = getResources();
            Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
            aBar.setBackgroundDrawable(actionBckGrnd);
            aBar.setTitle(" New Group Details");
            context = getApplicationContext();
            imgView = (ImageView) findViewById(R.id.groupPic);
        } else {
            Intent intent = new Intent(this, RetryActivity.class);
            startActivity(intent);
        }
    }

    public void selectGroupImage(View view) {
        try {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Image selection failed",
                    Toast.LENGTH_LONG).show();
            Log.e(e.getClass().getName(), e.getMessage(), e);
        }
    }

    public void registerGroup(View view) {
        Button button = (Button) findViewById(R.id.registerGroupButton);
        button.setTextColor(getResources().getColor(R.color.click_button_2));
        EditText groupNameField = (EditText) findViewById(R.id.groupNameValue);
        SharedPreferences prefs = getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        String members = prefs.getString("selectedIndividuals", "");
        String phone = prefs.getString("phone", "");
        String groupName = groupNameField.getText().toString();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("groupName", groupName);

        GroupImageClient imageRestClient = new GroupImageClient(
                this);

        imageRestClient.execute(new String[]{"addGroup", groupName, phone, members});

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
                            cropImage(selectedImageUri);
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
                System.out.println("RESULT CODE " + requestCode);
                Bundle extras = data.getExtras();
                if(extras != null){
                    bitmap = extras.getParcelable("data");
                    decodeFile(null);
                }
                    /*Uri croppedImageUri = data.getData();
                    System.out.println("croppedImageUri " + croppedImageUri);
                    try {
                        // OI FILE Manager
                        String fileManagerString = croppedImageUri.getPath();

                        // MEDIA GALLERY
                        String selectedImagePath = getPath(croppedImageUri);

                        if (selectedImagePath != null) {
                            filePath = selectedImagePath;
                        } else if (fileManagerString != null) {
                            filePath = fileManagerString;
                        } else {
                            Toast.makeText(getApplicationContext(), "Unknown path",
                                    Toast.LENGTH_LONG).show();
                            Log.e("Bitmap", "Unknown path");
                        }

                        if (croppedImageUri != null) {
                            decodeFile(croppedImageUri);
                        } else {
                            bitmap = null;
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Internal error",
                                Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }*/
                break;

            }
            default:
        }
    }

    public void cropImage(Uri picUri)
    {
        System.out.println("IN CROP IMAGE METHOD " + picUri);
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        try
        {
            //cropIntent.setType("image*//*");
            //cropIntent.setDataAndType(picUri, "image*//*");
            //cropIntent.setAction(Intent.ACTION_GET_CONTENT);
            cropIntent.setType("image/*");
            cropIntent.setData(picUri);
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("return-data", true);
            cropIntent.putExtra("aspectX", 300);
            cropIntent.putExtra("aspectY", 300);
            cropIntent.putExtra("outputX", 300);
            cropIntent.putExtra("outputY", 300);
            startActivityForResult(cropIntent, 2);
            //startActivityForResult(
            //      Intent.createChooser(cropIntent, "Select a Picture"), 2);
        }
        catch (ActivityNotFoundException anfe)
        {
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            System.out.println("error occured : " + errorMessage);
            anfe.printStackTrace();

        }
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
        byte[] data = bos.toByteArray();
        System.out.println("BOS Size : After *** " + bos.size()/1024);
        System.out.println("Data Size : After *** " + data.length/1024);
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
            imgView.setImageBitmap(BitmapFactory.decodeByteArray(data,0,data.length));
        }
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        SharedPreferences prefs = getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("regId", regId);
        editor.apply();
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

    public class GroupImageClient extends
            AsyncTask<String, Integer, String> {

        private Context mContext;
        private ProgressDialog pDlg;

        public GroupImageClient(Context mContext) {
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

            // HttpHost target = new HttpHost(TARGET_HOST);
            HttpHost target = new HttpHost(JMConstants.TARGET_HOST, 8080);
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(path);
            HttpEntity results = null;
            try {
                MultipartEntity entity = new MultipartEntity();
                entity.addPart("name", new StringBody(params[1].replace(" ", "%20")));
                entity.addPart("phone", new StringBody(params[2].replace(" ", "%20")));
                entity.addPart("image", new FileBody(new File(filePath)));
                entity.addPart("members", new StringBody(params[3]));

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
                XStream groupXs = new XStream();
                groupXs.alias("Group", Group.class);
                groupXs.alias("members", String.class);
                groupXs.addImplicitCollection(Group.class, "members",
                        "members", String.class);
                Group group = (Group) groupXs.fromXML(response);
                if (group != null) {
                    GroupDAO groupDAO = new GroupDAO(mContext);
                    SharedPreferences prefs = getSharedPreferences("Prefs",
                            Activity.MODE_PRIVATE);
                    String members = prefs.getString("selectedIndividuals", "");
                    groupDAO.addGroup(group.getGroupId(), group.getName(),
                            members, group.getImage(), group.getAdmin());
                    Toast.makeText(getApplicationContext(),
                            "Congratulations! Your group has been created.",
                            Toast.LENGTH_LONG).show();
                    //TODO Remove
                    Group dbgroup = groupDAO.fetchGroup(group.getGroupId());
                    if(dbgroup != null){
                        Log.i(TAG, "Group added: "+dbgroup.getName());
                    }
                    Intent intent = new Intent(mContext,
                            HomeGroupActivity.class);
                    startActivity(intent);

                }
            }
            pDlg.dismiss();
        }

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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
