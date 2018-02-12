package com.example.admin.myproject;

/**
 * Created by Admin on 04-07-2017.
 */
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class GetDate {

    public static String getDate(String milliSeconds) {
        if (milliSeconds.equals(""))
            return "time not known";
        else {
            String dateFormat = "dd/MM/yyyy\nhh:mm a";
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.US);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(milliSeconds));
            return formatter.format(calendar.getTime());
        }
    }
}