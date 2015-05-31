package com.justmeet.justmeetdroid;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.justmeet.entity.User;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MemberGridAdapter extends BaseAdapter {

    private Activity activity;
    private List<Map<String, User>> data;

    private static LayoutInflater inflater = null;

    public MemberGridAdapter(Activity a, List<Map<String, User>> d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public MemberGridAdapter(Activity a) {
        activity = a;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public List<Map<String, User>> getData() {
        return data;
    }

    public void setData(List<Map<String, User>> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.member_grid_row, null);
        }

        ImageView imgView = (ImageView) view
                .findViewById(R.id.memberGridPicThumbnail);
        TextView textView = (TextView) view
                .findViewById(R.id.memberGridNameField);

        Map<String, User> selectedMap = data.get(position);
        for (Entry<String, User> entry : selectedMap.entrySet()) {

            User user = entry.getValue();
            textView.setText(user.getName());
            if (user.isSelected()) {
                imgView.setBackgroundResource(R.drawable.selected_border);
                textView.setBackgroundResource(R.drawable.selected_border);
            } else {
                imgView.setBackgroundResource(R.drawable.image_border);
                textView.setBackgroundResource(R.drawable.image_border);
            }


            byte[] image = user.getImage();
            if (image != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                options.inPurgeable = true;
                options.inInputShareable = true;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                WeakReference<Bitmap> imageBitmapReference = new WeakReference<Bitmap>(BitmapFactory.decodeByteArray(image, 0,
                        image.length, options));

                /*Bitmap img = BitmapFactory.decodeByteArray(image, 0,
                        image.length);*/

                imgView.setImageBitmap(imageBitmapReference.get());
            } else {
                imgView.setImageDrawable(activity.getResources()
                        .getDrawable(R.drawable.ic_launcher));
            }
        }


        return view;
    }

}
