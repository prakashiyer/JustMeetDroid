package com.justmeet.justmeetdroid;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.justmeet.entity.Plan;
import com.justmeet.util.JMUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PlanListAdapter extends BaseAdapter {

    private Activity activity;
    private List<Map<String, Plan>> data;

    public List<Map<String, Plan>> getData() {
        return data;
    }

    public void setData(List<Map<String, Plan>> data) {
        this.data = data;
    }

    private static LayoutInflater inflater = null;

    public PlanListAdapter(Activity a) {
        activity = a;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public PlanListAdapter(Activity a, List<Map<String, Plan>> d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            view = inflater.inflate(R.layout.plan_list_row, null);
        }
        TextView planName = (TextView) view.findViewById(R.id.planName);
        TextView planDay = (TextView) view.findViewById(R.id.planDay);
        TextView planTime = (TextView) view.findViewById(R.id.planTime);
        TextView planMems = (TextView) view.findViewById(R.id.planMembAttending);

        Map<String, Plan> selectedMap = data.get(position);
        for (Entry<String, Plan> entry : selectedMap.entrySet()) {

            Plan plan = entry.getValue();
            planName.setText(plan.getTitle());


            String startTime = plan.getStartTime();
            String[] startPlanTime = null;
            if (startTime != null) {
                startPlanTime = JMUtil.createGmtToLocalTime(startTime);
            }

            String date = startPlanTime[0];
            Calendar cal = Calendar.getInstance();
            String year = date.substring(0, 4);
            String month = date.substring(5, 7);
            String dateStr = date.substring(8, 10);
            cal.set(Integer.valueOf(year), Integer.valueOf(month) - 1, Integer.valueOf(dateStr));
            int day = cal.get(Calendar.DAY_OF_WEEK);
            String weekday = retrieveDay(day);
            int mon = cal.get(Calendar.MONTH);
            String monthStr = retrieveMonth(mon);

            String time = startPlanTime[1];
            String hour = time.substring(0, 2);
            String min = time.substring(3, 5);
            int hourInt = Integer.valueOf(hour);
            String ampm = "AM";
            if (hourInt > 12) {
                hour = String.valueOf(hourInt - 12);
                if (Integer.valueOf(hour) < 10) {
                    hour = "0" + hour;
                }
                ampm = "PM";
            }
            planDay.setText(" " + weekday + ", " + monthStr + " " + dateStr + " ");
            planTime.setText(" " + hour + ":" + min + " " + ampm + " ");
            int members = 0;

            if (plan.getMembersAttending() != null) {
                members = plan.getMembersAttending().size();
            }
            planMems.setText(" Members Attending (" + members + ")");
        }
        return view;
    }

    private String retrieveDay(int day) {
        switch (day) {
            case Calendar.SUNDAY: {
                return "Sunday";
            }
            case Calendar.MONDAY: {
                return "Monday";
            }
            case Calendar.TUESDAY: {
                return "Tuesday";
            }
            case Calendar.WEDNESDAY: {
                return "Wednesday";
            }
            case Calendar.THURSDAY: {
                return "Thursday";
            }
            case Calendar.FRIDAY: {
                return "Friday";
            }
            case Calendar.SATURDAY: {
                return "Saturday";
            }
            default: {
                return "";
            }
        }
    }

    private String retrieveMonth(int month) {
        switch (month) {
            case Calendar.JANUARY: {
                return "Jan";
            }
            case Calendar.FEBRUARY: {
                return "Feb";
            }
            case Calendar.MARCH: {
                return "Mar";
            }
            case Calendar.APRIL: {
                return "Apr";
            }
            case Calendar.MAY: {
                return "May";
            }
            case Calendar.JUNE: {
                return "Jun";
            }
            case Calendar.JULY: {
                return "Jul";
            }
            case Calendar.AUGUST: {
                return "Aug";
            }
            case Calendar.SEPTEMBER: {
                return "Sep";
            }
            case Calendar.OCTOBER: {
                return "Oct";
            }
            case Calendar.NOVEMBER: {
                return "Nov";
            }
            case Calendar.DECEMBER: {
                return "Dec";
            }
            default: {
                return "";
            }
        }
    }

}
