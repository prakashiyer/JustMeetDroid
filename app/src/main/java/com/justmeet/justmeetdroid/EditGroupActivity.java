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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.justmeet.dao.GroupDAO;
import com.justmeet.entity.Group;
import com.justmeet.entity.Plan;
import com.justmeet.entity.PlanList;
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by praxiyer on 20-03-2015.
 */
public class EditGroupActivity extends FragmentActivity {
    private Context context;
    private String filePath;
    private Bitmap bitmap;
    private ImageView imgView;
    private static final int PICK_IMAGE = 1;
    private static final String TAG = "Edit Group";
    private EditText groupNameField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (JMUtil.haveInternet(this)) {
            setContentView(R.layout.edit_group);
            ActionBar aBar = getActionBar();
            Resources res = getResources();
            Drawable actionBckGrnd = res.getDrawable(R.drawable.actionbar);
            aBar.setBackgroundDrawable(actionBckGrnd);
            aBar.setTitle(" Edit Group Details");
            context = getApplicationContext();
            groupNameField = (EditText) findViewById(R.id.editGroupNameValue);
            imgView = (ImageView) findViewById(R.id.editGroupPic);
            SharedPreferences prefs = getSharedPreferences("Prefs",
                    Activity.MODE_PRIVATE);
            String selectedGroupIndex = prefs.getString("selectedGroupIndex", "");
            GroupDAO groupDAO = new GroupDAO(this);
            Group group = groupDAO.fetchGroup(selectedGroupIndex);
            if(group != null){
                groupNameField.setText(group.getName());
                byte[] image = group.getImage();
                Bitmap img = BitmapFactory.decodeByteArray(image, 0,
                        image.length);
                if (img != null) {
                    imgView.setImageBitmap(img);
                }
            } else {
                Log.i(TAG, "No Group Plans in local DB!");
                String searchQuery1 = "/fetchGroup?groupIndex=" + selectedGroupIndex;
                GroupClient restClient = new GroupClient(this);
                restClient.execute(new String[]{searchQuery1});
            }
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

    public void editGroup(View view) {
        Button button = (Button) findViewById(R.id.editGroupButton);
        button.setTextColor(getResources().getColor(R.color.click_button_2));
        groupNameField = (EditText) findViewById(R.id.editGroupNameValue);
        SharedPreferences prefs = getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        String groupId = prefs.getString("selectedGroupIndex", "");
        String groupName = groupNameField.getText().toString();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("groupName", groupName);

        GroupImageClient imageRestClient = new GroupImageClient(
                this);

        imageRestClient.execute(new String[]{"editGroup", groupId, groupName, filePath});

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

            default:
        }
    }

    public void cropImage(Uri picUri)
    {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        try
        {
            cropIntent.setType("image/*");
            cropIntent.setData(picUri);
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("return-data", true);
            cropIntent.putExtra("aspectX", 300);
            cropIntent.putExtra("aspectY", 300);
            cropIntent.putExtra("outputX", 300);
            cropIntent.putExtra("outputY", 300);
            startActivityForResult(cropIntent, 2);
        }
        catch (ActivityNotFoundException anfe)
        {
            Toast.makeText(getApplicationContext(), "This device does not support cropping",
                    Toast.LENGTH_LONG).show();
            Log.e("Bitmap", "This device does not support cropping");

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
                entity.addPart("groupId", new StringBody(params[1].replace(" ", "%20")));
                entity.addPart("name", new StringBody(params[2].replace(" ", "%20")));
                entity.addPart("image", new FileBody(new File(filePath)));

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
                    groupDAO.editGroup(group.getGroupId(), group.getName(),
                            group.getImage());
                    Toast.makeText(getApplicationContext(),
                            "Your group has been edited.",
                            Toast.LENGTH_LONG).show();
                    //TODO Remove
                    Group dbGroup = groupDAO.fetchGroup(group.getGroupId());
                    if(dbGroup != null){
                        Log.i(TAG, "Group updated: "+dbGroup.getName());
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

    private class GroupClient extends AsyncTask<String, Integer, String> {

        private Context mContext;
        private ProgressDialog pDlg;

        public GroupClient(Context mContext) {
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

            if (response != null && response.contains("<Group>")) {
                Log.i(TAG, response);
                XStream xstream = new XStream();
                xstream.alias("Group", Group.class);

                xstream.alias("members", String.class);
                xstream.addImplicitCollection(Group.class, "members",
                        "members", String.class);
                Group group = (Group) xstream.fromXML(response);
                if (group != null) {
                    groupNameField.setText(group.getName());
                    byte[] image = group.getImage();
                    Bitmap img = BitmapFactory.decodeByteArray(image, 0,
                            image.length);
                    if (img != null) {
                        imgView.setImageBitmap(img);
                    }
                }
            }
            pDlg.dismiss();
        }

    }
}
