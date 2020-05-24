package com.gramevapp.web.other;

import java.util.Date;
import java.util.Calendar;

public class DateFormat {
    public static String formatDate(Date date) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String dateReturn = new String();
        dateReturn += cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.DAY_OF_MONTH) +
                " ";

        dateReturn += (cal.get(Calendar.HOUR) < 10 ? "0" : "") + cal.get(Calendar.HOUR) + ":";
        dateReturn += (cal.get(Calendar.MINUTE) < 10 ? "0" : "") + cal.get(Calendar.MINUTE) + ":";
        dateReturn += (cal.get(Calendar.SECOND) < 10 ? "0" : "") + cal.get(Calendar.SECOND);
        return dateReturn;
    }
}
