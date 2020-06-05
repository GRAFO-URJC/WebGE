package com.gramevapp.web.other;

import java.util.Calendar;
import java.util.Date;

public class DateFormat {
    public static String formatDate(Date date) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String dateReturn = "";
        dateReturn += cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.DAY_OF_MONTH) +
                " ";

        dateReturn += (cal.get(Calendar.HOUR) < 10 ? "0" : "") + cal.get(Calendar.HOUR) + ":";
        dateReturn += (cal.get(Calendar.MINUTE) < 10 ? "0" : "") + cal.get(Calendar.MINUTE) + ":";
        dateReturn += (cal.get(Calendar.SECOND) < 10 ? "0" : "") + cal.get(Calendar.SECOND);
        return dateReturn;
    }
}
