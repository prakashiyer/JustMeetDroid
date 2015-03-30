package com.justmeet.justmeetdroid;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

@SuppressLint("ValidFragment")
public class TimeNewPickerFragment extends DialogFragment implements
        TimePickerDialog.OnTimeSetListener {
    private String time;

    public TimeNewPickerFragment(String time) {
        this.time = time;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, false);
        // DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {


        if (time.equals("start")) {
            Calendar c = Calendar.getInstance();


            Calendar nc = Calendar.getInstance();
            nc.set(Calendar.HOUR_OF_DAY, hourOfDay);
            nc.set(Calendar.MINUTE, minute);
            c.add(Calendar.MINUTE, 15);
            TextView planDateEditText = (TextView) getActivity().findViewById(
                    R.id.appointmentStartDate);

            String odate = planDateEditText.getText().toString();

            if (odate != null && !odate.equals("")) {
                String year = odate.substring(0, 4);
                String month = odate.substring(5, 7);
                String dateStr = odate.substring(8, 10);
                nc.set(Calendar.YEAR, Integer.valueOf(year));
                nc.set(Calendar.MONTH, Integer.valueOf(month) - 1);
                nc.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dateStr));

                if (!nc.before(c)) {
                    TextView planTimeEditText = (TextView) getActivity()
                            .findViewById(R.id.appointmentStartTime);
                    String hour = String.valueOf(hourOfDay);
                    String min = String.valueOf(minute);

                    if (hourOfDay < 10) {
                        hour = "0" + hour;
                    }
                    if (minute < 10) {
                        min = "0" + min;
                    }
                    int hourInt = Integer.valueOf(hour);
                    String ampm = "AM";
                    if (hourInt > 12) {
                        hour = String.valueOf(hourInt - 12);
                        if (Integer.valueOf(hour) < 10) {
                            hour = "0" + hour;
                        }

                        ampm = "PM";
                    }
                    planTimeEditText.setText(hour + ":" + min + " " + ampm);
                } else {
                    Toast.makeText(
                            getActivity(),
                            "Create appointments atleast 15 minutes in future!",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Please select a start date!",
                        Toast.LENGTH_SHORT).show();
            }

        }

        if (time.equals("end")) {

            TextView planDateEditText = (TextView) getActivity().findViewById(
                    R.id.appointmentStartDate);
            String odate = planDateEditText.getText().toString();

            TextView planStartEditText = (TextView) getActivity().findViewById(
                    R.id.appointmentStartTime);
            String otime = planStartEditText.getText().toString();

            TextView planEndEditText = (TextView) getActivity().findViewById(
                    R.id.appointmentEndDate);
            String edate = planEndEditText.getText().toString();

            if (odate != null && !odate.equals("") && otime != null
                    && !otime.equals("") && edate != null && !edate.equals("")) {
                Calendar c = Calendar.getInstance();


                Calendar nc = Calendar.getInstance();

                String oyear = odate.substring(0, 4);
                String omonth = odate.substring(5, 7);
                String odateStr = odate.substring(8, 10);
                String oHr = otime.substring(0, 2);
                String omin = otime.substring(3, 5);
                String oampm = otime.substring(6, 8);
                c.set(Calendar.YEAR, Integer.valueOf(oyear));
                c.set(Calendar.MONTH, Integer.valueOf(omonth) - 1);
                c.set(Calendar.DAY_OF_MONTH, Integer.valueOf(odateStr));
                int oohr = 0;
                if ("am".equalsIgnoreCase(oampm)) {
                    oohr = Integer.valueOf(oHr);
                } else {
                    oohr = Integer.valueOf(oHr) + 12;
                }
                c.set(Calendar.HOUR_OF_DAY, oohr);
                c.set(Calendar.MINUTE, Integer.valueOf(omin));

                String eyear = edate.substring(0, 4);
                String emonth = edate.substring(5, 7);
                String edateStr = edate.substring(8, 10);
                nc.set(Calendar.YEAR, Integer.valueOf(eyear));
                nc.set(Calendar.MONTH, Integer.valueOf(emonth) - 1);
                nc.set(Calendar.DAY_OF_MONTH, Integer.valueOf(edateStr));
                nc.set(Calendar.HOUR_OF_DAY, hourOfDay);
                nc.set(Calendar.MINUTE, minute);
                if (nc.before(c)) {
                    Toast.makeText(getActivity(),
                            "Please select a end date-time later than start date-time!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    TextView planEndTimeEditText = (TextView) getActivity()
                            .findViewById(R.id.appointmentEndTime);
                    String hour = String.valueOf(hourOfDay);
                    String min = String.valueOf(minute);

                    if (hourOfDay < 10) {
                        hour = "0" + hour;
                    }
                    if (minute < 10) {
                        min = "0" + min;
                    }
                    int hourInt = Integer.valueOf(hour);
                    String ampm = "AM";
                    if (hourInt > 12) {
                        hour = String.valueOf(hourInt - 12);
                        if (Integer.valueOf(hour) < 10) {
                            hour = "0" + hour;
                        }

                        ampm = "PM";
                    }
                    planEndTimeEditText.setText(hour + ":" + min + " " + ampm);
                }

            } else {
                Toast.makeText(getActivity(),
                        "Please select a start date-time and end date!",
                        Toast.LENGTH_SHORT).show();
            }

        }
    }
}
