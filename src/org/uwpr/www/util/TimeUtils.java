package org.uwpr.www.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by vsharma on 1/29/2016.
 */
public class TimeUtils
{
    public static int MILLIS_IN_HOUR = 60 * 60 * 1000;
    public static int MILLIS_IN_DAY = 24 * 60 * 60 * 1000;

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy hh:mm a");

    private TimeUtils(){}

    public static Date makeBeginningOfDay(Date date)
    {
        if(date == null)
        {
            return null;
        }

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(date);
        startCal.set(Calendar.MILLISECOND, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.HOUR_OF_DAY, 0); // 12:00 am
        return startCal.getTime();
    }

    public static Date makeEndOfDay(Date date)
    {
        if(date == null)
        {
            return null;
        }
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(date);
        endCal.set(Calendar.MILLISECOND, 0);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.HOUR_OF_DAY, 0); // 12:00 am
        return new Date(endCal.getTime().getTime() + MILLIS_IN_DAY - 1);
    }

    public static String format(Date date)
    {
        return dateFormat.format(date);
    }
}
