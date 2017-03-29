package ru.p3tr0vich.mwmmapsupdater.utils;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import ru.p3tr0vich.mwmmapsupdater.BuildConfig;
import ru.p3tr0vich.mwmmapsupdater.Consts;

public class UtilsLog {
    private static final String LOG_TAG = "XXX";

    // TODO: отключить после разработки
    private static final boolean IGNORE_DEBUG = true;
    private static final boolean WRITE_TO_FILE = true;

    private static final DateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());

    public static String formatDate(long timestamp) {
        return timestamp != Consts.BAD_DATETIME ? DATETIME_FORMAT.format(timestamp) : "BAD_DATETIME";
    }

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
            String text = formatMsg(tag, msg, extMsg);

            Log.d(LOG_TAG, text);

            writeToFile(text);
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
        String text = formatMsg(tag, msg, extMsg);

        Log.e(LOG_TAG, text);

        writeToFile(text);
    }

    public static void e(@Nullable String tag, @NonNull String msg, @NonNull Exception e) {
        e(tag, msg, "exception == " + e.toString());
    }

    private static void writeToFile(@NonNull String text) {
        if (!WRITE_TO_FILE) return;

        try {
            File logFile = new File(Environment.getExternalStorageDirectory(), BuildConfig.APPLICATION_ID + ".log");

            if (!logFile.exists() || !logFile.isFile()) {
                if (!logFile.createNewFile()) {
                    return;
                }
            }

            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            //noinspection TryFinallyCanBeTryWithResources
            try {
                buf
                        .append(DATETIME_FORMAT.format(System.currentTimeMillis()))
                        .append(": ")
                        .append(text);

                buf.newLine();
            } finally {
                buf.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}