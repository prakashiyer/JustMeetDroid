package com.justmeet.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by praxiyer on 04-02-2015.
 */
public class JMUtil {

    @SuppressLint("SimpleDateFormat")
    public static String[] createLocalToGmtTime(String dateTime) {
        String[] dateStr = new String[2];
        try {
            SimpleDateFormat gmtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateTOConvert = gmtFormat.parse(dateTime);
            TimeZone gmtTime = TimeZone.getTimeZone("GMT");
            gmtFormat.setTimeZone(gmtTime);
            String gmtDate = gmtFormat.format(dateTOConvert);
            dateStr = gmtDate.split(" ");
        } catch (ParseException e) {
        }
        return dateStr;
    }

    @SuppressLint("SimpleDateFormat")
    public static String[] createGmtToLocalTime(String dateTime) {
        String[] dateStr = new String[2];
        try {
            SimpleDateFormat gmtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            gmtFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date dateTOConvert = gmtFormat.parse(dateTime);

            SimpleDateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            localFormat.setTimeZone(TimeZone.getDefault());
            String localDate = localFormat.format(dateTOConvert);
            System.out.println("Local Date " + localDate);
            dateStr = localDate.split(" ");
        } catch (ParseException e) {
        }
        return dateStr;
    }

    public static String listToCommaDelimitedString(List<String> list) {
        StringBuffer stringBuff2 = new StringBuffer();
        int size2 = list.size();

        for (int i = 0; i < size2; i++) {
            stringBuff2.append("'");
            stringBuff2.append(list.get(i));
            stringBuff2.append("'");
            if (i != size2 - 1) {
                stringBuff2.append(",");
            }
        }
        return stringBuff2.toString();
    }

    /**
     * Checks if we have a valid Internet Connection on the device.
     *
     * @param ctx
     * @return True if device has internet
     * <p/>
     * Code from: http://www.androidsnippets.org/snippets/131/
     */
    public static boolean haveInternet(Context ctx) {

        NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();

        if (info == null || !info.isConnected()) {
            return false;
        }

        return true;
    }
}
