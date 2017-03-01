package ru.p3tr0vich.mwmmapsupdater.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import ru.p3tr0vich.mwmmapsupdater.BuildConfig;

public class UtilsLog {
    private static final String LOG_TAG = "XXX";

    // TODO: отключить после разработки
    private static final boolean IGNORE_DEBUG = true;

    public static final DateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());

    private static String formatMsg(@Nullable String tag, @NonNull String msg, @Nullable String extMsg) {
        if (!TextUtils.isEmpty(tag)) {
            msg = tag + " -- " + msg;
        }

        if (!TextUtils.isEmpty(extMsg)) {
            msg = msg + ": " + extMsg;
        }

        return msg;
    }

    public static void d(boolean show, @Nullable String tag, @NonNull String msg, @Nullable String extMsg) {
        if ((BuildConfig.DEBUG || IGNORE_DEBUG) && show) {
            Log.d(LOG_TAG, formatMsg(tag, msg, extMsg));
        }
    }

    public static void d(boolean show, @NonNull String tag, @NonNull String msg) {
        d(show, tag, msg, null);
    }

    public static void d(boolean show, @NonNull Object o, @NonNull String msg, @Nullable String extMsg) {
        d(show, o.getClass(), msg, extMsg);
    }

    public static void d(boolean show, @NonNull Object o, @NonNull String msg) {
        d(show, o, msg, null);
    }

    public static void d(boolean show, @NonNull Class aClass, @NonNull String msg, @Nullable String extMsg) {
        d(show, aClass.getSimpleName(), msg, extMsg);
    }

    public static void e(@Nullable String tag, @NonNull String msg, @Nullable String extMsg) {
        Log.e(LOG_TAG, formatMsg(tag, msg, extMsg));
    }

    public static void e(@Nullable String tag, @NonNull String msg, @NonNull Exception e) {
        e(tag, msg, "exception == " + e.toString());
    }
}