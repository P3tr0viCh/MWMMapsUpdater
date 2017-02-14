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
import java.util.TimeZone;

import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class MapFilesServerHelper {

    private static final String TAG = "MapFilesServerHelper";

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
        URL url = getUrl(mapName);

        if (url == null) {
            return null;
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            long lastModified = 0;

            try {
                lastModified = connection.getLastModified();
            } finally {
                connection.disconnect();
            }

            if (lastModified == 0) {
                return null;
            }

            return new FileInfo(new Date(lastModified));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @NonNull
    private static Map<String, FileInfo> getMapsInfo(@NonNull List<String> mapNames) {
        Map<String, FileInfo> fileInfoMap = new HashMap<>();

        mapNames.clear();
        mapNames.add("Abkhazia");
        mapNames.add("Russia_Orenburg Oblast");
        mapNames.add("Russia_Omsk Oblast");

        for (String mapName : mapNames) {
            FileInfo fileInfo = getFileInfo(mapName);

            if (fileInfo != null) {
                fileInfoMap.put(mapName, fileInfo);
            }
        }

        return fileInfoMap;
    }

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.US);

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static Date getVersion(@NonNull List<String> fileList) {
        Map<String, FileInfo> fileInfoMap = getMapsInfo(fileList);

        List<Date> dates = new ArrayList<>();

        for (Map.Entry<String, FileInfo> entry : fileInfoMap.entrySet()) {
            UtilsLog.d(TAG, "getVersion", entry.getKey() + " = " + DATE_FORMAT.format(entry.getValue().getDate()));

            dates.add(entry.getValue().getDate());
        }

        if (dates.isEmpty()) {
            return null;
        } else {
            Collections.sort(dates);

            UtilsLog.d(TAG, "getVersion", "return " + DATE_FORMAT.format(dates.get(0)));

            return dates.get(0);
        }
    }
}