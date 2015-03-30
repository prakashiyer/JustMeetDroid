package com.justmeet.justmeetdroid;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

@SuppressLint("ValidFragment")
public class DateNewPickerFragment extends DialogFragment implements
        DatePickerDialog.OnDateSetListener {

    private String date;

    public DateNewPickerFragment(String date) {
        this.date = date;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {

        Calendar c = Calendar.getInstance();
        Calendar nc = Calendar.getInstance();
        nc.set(Calendar.YEAR, year);
        nc.set(Calendar.MONTH, month);
        nc.set(Calendar.DAY_OF_MONTH, day);

        if (date.equals("start")) {

            if (nc.before(c)) {
                Toast.makeText(getActivity(), "You can't select past date!",
                        Toast.LENGTH_SHORT).show();
            } else {
                TextView planDateEditText = (TextView) getActivity().findViewById(
                        R.id.appointmentStartDate);
                String mon = String.valueOf(month + 1);
                String date = String.valueOf(day);
                if (month + 1 < 10) {
                    mon = "0" + mon;
                }
                if (day < 10) {
                    date = "0" + date;
                }
                planDateEditText.setText(date + "-" + year + "-" + date);
            }

        }

        if (date.equals("editStart")) {

            if (nc.before(c)) {
                Toast.makeText(getActivity(), "You can't select past date!",
                        Toast.LENGTH_SHORT).show();
            } else {
                TextView planDateEditText = (TextView) getActivity().findViewById(
                        R.id.editStartDate);
                String mon = String.valueOf(month + 1);
                String date = String.valueOf(day);
                if (month + 1 < 10) {
                    mon = "0" + mon;
                }
                if (day < 10) {
                    date = "0" + date;
                }
                planDateEditText.setText(date + "-" + year + "-" + date);
            }

        }

        if (date.equals("end")) {

            TextView planDateEditText = (TextView) getActivity().findViewById(
                    R.id.appointmentStartDate);

            String odate = planDateEditText.getText().toString();

            if (odate != null && !odate.equals("")) {
                String oyear = odate.substring(0, 4);
                String omonth = odate.substring(5, 7);
                String odateStr = odate.substring(8, 10);
                c.set(Calendar.YEAR, Integer.valueOf(oyear));
                c.set(Calendar.MONTH, Integer.valueOf(omonth) - 1);
                c.set(Calendar.DAY_OF_MONTH, Integer.valueOf(odateStr));

                if (nc.before(c)) {
                    Toast.makeText(getActivity(),
                            "You can't select end date earlier than start date", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    TextView planEndDateEditText = (TextView) getActivity()
                            .findViewById(R.id.appointmentEndDate);
                    String mon = String.valueOf(month + 1);
                    String date = String.valueOf(day);
                    if (month + 1 < 10) {
                        mon = "0" + mon;
                    }
                    if (day < 10) {
                        date = "0" + date;
                    }
                    planEndDateEditText.setText(year + "-" + mon + "-" + date);
                }
            } else {
                Toast.makeText(getActivity(),
                        "Please select a start date", Toast.LENGTH_SHORT)
                        .show();
            }
        }

        if (date.equals("editEnd")) {

            TextView planDateEditText = (TextView) getActivity().findViewById(
                    R.id.editStartDate);

            String odate = planDateEditText.getText().toString();

            if (odate != null && !odate.equals("")) {
                String oyear = odate.substring(0, 4);
                String omonth = odate.substring(5, 7);
                String odateStr = odate.substring(8, 10);
                c.set(Calendar.YEAR, Integer.valueOf(oyear));
                c.set(Calendar.MONTH, Integer.valueOf(omonth) - 1);
                c.set(Calendar.DAY_OF_MONTH, Integer.valueOf(odateStr));

                if (nc.before(c)) {
                    Toast.makeText(getActivity(),
                            "You can't select end date earlier than start date", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    TextView planEndDateEditText = (TextView) getActivity()
                            .findViewById(R.id.editEndDate);
                    String mon = String.valueOf(month + 1);
                    String date = String.valueOf(day);
                    if (month + 1 < 10) {
                        mon = "0" + mon;
                    }
                    if (day < 10) {
                        date = "0" + date;
                    }
                    planEndDateEditText.setText(year + "-" + mon + "-" + date);
                }
            } else {
                Toast.makeText(getActivity(),
                        "Please select a start date", Toast.LENGTH_SHORT)
                        .show();
            }
        }

    }
}
