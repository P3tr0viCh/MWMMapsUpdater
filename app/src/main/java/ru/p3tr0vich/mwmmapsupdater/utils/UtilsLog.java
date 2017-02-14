package ru.p3tr0vich.mwmmapsupdater.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import ru.p3tr0vich.mwmmapsupdater.BuildConfig;

public class UtilsLog {
    private static final String LOG_TAG = "XXX";

    private static String formatMsg(@Nullable String tag, @NonNull String msg, @Nullable String extMsg) {
        if (!TextUtils.isEmpty(tag)) {
            msg = tag + " -- " + msg;
        }

        if (!TextUtils.isEmpty(extMsg)) {
            msg = msg + ": " + extMsg;
        }

        return msg;
    }

    public static void d(@Nullable String tag, @NonNull String msg, @Nullable String extMsg) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, formatMsg(tag, msg, extMsg));
        }
    }

    public static void e(@Nullable String tag, @NonNull String msg, @Nullable String extMsg) {
        Log.e(LOG_TAG, formatMsg(tag, msg, extMsg));
    }

    public static void d(@NonNull String tag, @NonNull String msg) {
        d(tag, msg, null);
    }

    public static void d(@NonNull Object o, @NonNull String msg, @Nullable String extMsg) {
        d(o.getClass(), msg, extMsg);
    }

    public static void d(@NonNull Object o, @NonNull String msg) {
        d(o, msg, null);
    }

    public static void d(@NonNull Class aClass, @NonNull String msg, @Nullable String extMsg) {
        d(aClass.getSimpleName(), msg, extMsg);
    }
}