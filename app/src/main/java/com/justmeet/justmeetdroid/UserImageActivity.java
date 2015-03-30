package com.justmeet.justmeetdroid;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;


public class UserImageActivity extends ActionBarActivity {
    private static final int PICK_IMAGE = 1;
    private Bitmap bitmap;
    Context context;
    private ImageView imgView;
    private String filePath;
    //private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = getSharedPreferences("Prefs",
                Activity.MODE_PRIVATE);
        String phone = prefs.getString("phone", "");
        context = getApplicationContext();
        imgView = (ImageView) findViewById(R.id.profilePicView);
    }
    public void uploadImage(View view) {
        Button button = (Button) findViewById(R.id.userImageButton);
        button.setTextColor(getResources().getColor(R.color.abc_primary_text_material_dark));
        try {
            //cropImage(null);
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            System.out.println("ABC");

            startActivityForResult(
                    Intent.createChooser(intent, "Select a Picture"), PICK_IMAGE);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Image selection failed",
                    Toast.LENGTH_LONG).show();
            Log.e(e.getClass().getName(), e.getMessage(), e);
        }
        button.setTextColor(getResources().getColor(R.color.abc_background_cache_hint_selector_material_light));
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

    public void decodeFile(Uri imageUri) {
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
