package ru.p3tr0vich.mwmmapsupdater.utils;

import android.support.annotation.NonNull;

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
}