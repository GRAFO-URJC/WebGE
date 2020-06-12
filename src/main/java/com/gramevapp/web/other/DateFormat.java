package com.gramevapp.web.other;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class DateFormat {
    public static String formatDate(Timestamp timestamp) {

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss") ;
        return df.format(timestamp);
    }
}
