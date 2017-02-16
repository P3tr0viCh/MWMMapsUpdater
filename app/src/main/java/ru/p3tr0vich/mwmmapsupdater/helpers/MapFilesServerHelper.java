package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import ru.p3tr0vich.mwmmapsupdater.BuildConfig;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class MapFilesServerHelper {

    private static final String TAG = "MapFilesServerHelper";

    private static final boolean LOG_ENABLED = true;

    private static final boolean DUMMY_FILE_INFO = false;
    private static final boolean FILE_INFO_WAIT_ENABLED = false;

    private static final String PROTOCOL = "http";
    private static final String HOST = "direct.mapswithme.com";
    private static final String PATH = "regular/daily";

    private static class FileInfo {
        private final Date mDate;

        public FileInfo(@NonNull Date date) {
            mDate = date;
        }

        @NonNull
        public Date getDate() {
            return mDate;
        }

        @Override
        public String toString() {
            return "FileInfo{" +
                    "mDate=" + mDate +
                    '}';
        }
    }

    @Nullable
    private static URL getUrl(@NonNull String mapName) {
        try {
            return new URL(PROTOCOL, HOST, PATH + '/' + mapName + ".mwm");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    private static FileInfo getFileInfo(@NonNull String mapName) {

        if (BuildConfig.DEBUG && FILE_INFO_WAIT_ENABLED) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long lastModified;

        if (BuildConfig.DEBUG && DUMMY_FILE_INFO) {
            Random rand = new Random();

            lastModified = -946771200000L + (Math.abs(rand.nextLong()) % (70L * 365 * 24 * 60 * 60 * 1000));
        } else {
            URL url = getUrl(mapName);

            if (url == null) {
                UtilsLog.e(TAG, "getFileInfo", "url == null");
                return null;
            }

            HttpURLConnection connection;
            try {
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("HEAD");
            } catch (IOException e) {
                e.printStackTrace();
                UtilsLog.e(TAG, "getFileInfo", "openConnection IOException == " + e.toString());
                return null;
            }

            try {
                connection.connect();

                lastModified = connection.getLastModified();

                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                UtilsLog.e(TAG, "getFileInfo", "connect IOException == " + e.toString());
                return null;
            }

        }

        if (lastModified == 0) {
            UtilsLog.e(TAG, "getFileInfo", "lastModified == 0");
            return null;
        }

        return new FileInfo(new Date(lastModified));
    }

    @NonNull
    private static Map<String, FileInfo> getMapsInfo(@NonNull List<String> mapNames) {
        Map<String, FileInfo> fileInfoMap = new HashMap<>();

        UtilsLog.d(LOG_ENABLED, TAG, "getMapsInfo", "start");

        for (String mapName : mapNames) {
            FileInfo fileInfo = getFileInfo(mapName);

            UtilsLog.d(LOG_ENABLED, TAG, "getMapsInfo", "fileInfo == " + fileInfo);

            if (fileInfo != null) {
                fileInfoMap.put(mapName, fileInfo);
            }
        }

        UtilsLog.d(LOG_ENABLED, TAG, "getMapsInfo", "end");

        return fileInfoMap;
    }

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.US);

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Nullable
    public static Date getVersion(@NonNull List<String> mapNames) {
        if (BuildConfig.DEBUG && DUMMY_FILE_INFO) {
            Random rand = new Random();

            if (rand.nextBoolean()) {
                return null;
            }
        }

        Map<String, FileInfo> fileInfoMap = getMapsInfo(mapNames);

        List<Date> dates = new ArrayList<>();

        for (Map.Entry<String, FileInfo> entry : fileInfoMap.entrySet()) {
            dates.add(entry.getValue().getDate());
        }

        if (dates.isEmpty()) {
            UtilsLog.d(LOG_ENABLED, TAG, "getVersion", "return null");

            return null;
        } else {
            Collections.sort(dates);

            UtilsLog.d(LOG_ENABLED, TAG, "getVersion", "return " + dates.get(0));

            return dates.get(0);
        }
    }
}