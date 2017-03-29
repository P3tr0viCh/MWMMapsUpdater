package ru.p3tr0vich.mwmmapsupdater.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UtilsDate {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());

    public static String format(@NonNull Date date) {
        return DATE_FORMAT.format(date);
    }

    public static String format(long timestamp) {
        return format(new Date(timestamp));
    }

    public static String dateTimeToString(Context context, long date, boolean withYear, boolean abbrevMonth) {
        int flags = DateUtils.FORMAT_SHOW_DATE;
        flags |= withYear ? DateUtils.FORMAT_SHOW_YEAR : DateUtils.FORMAT_NO_YEAR;
        if (abbrevMonth) flags |= DateUtils.FORMAT_ABBREV_MONTH;

//        flags |= DateUtils.FORMAT_SHOW_TIME;

        return DateUtils.formatDateTime(context, date, flags);
    }

    public static String dateToString(Context context, long date, boolean withYear) {
        return dateTimeToString(context, date, withYear, false);
    }
}