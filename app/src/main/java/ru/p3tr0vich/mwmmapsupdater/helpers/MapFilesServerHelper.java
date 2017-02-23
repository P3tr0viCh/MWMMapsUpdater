package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ru.p3tr0vich.mwmmapsupdater.BuildConfig;
import ru.p3tr0vich.mwmmapsupdater.Consts;
import ru.p3tr0vich.mwmmapsupdater.models.FileInfo;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class MapFilesServerHelper {

    private static final String TAG = "MapFilesServerHelper";

    private static final boolean LOG_ENABLED = false;

    private static final boolean DEBUG_DUMMY_FILE_INFO = true;
    private static final boolean DEBUG_FILE_INFO_WAIT_ENABLED = false;
    private static final boolean DEBUG_RETURN_CURRENT_DATE = false;

    private static final String PROTOCOL = "http";
    private static final String HOST = "direct.mapswithme.com";
    private static final String PATH = "regular/daily";

    private static final int DEFAULT_CONNECT_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);

    private MapFilesServerHelper() {
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
    private static FileInfo getFileInfo(@NonNull String mapName) throws IOException {

        if (BuildConfig.DEBUG && DEBUG_FILE_INFO_WAIT_ENABLED) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long lastModified;

        if (BuildConfig.DEBUG && DEBUG_DUMMY_FILE_INFO) {
            Random rand = new Random();

            lastModified = -946771200000L + (Math.abs(rand.nextLong()) % (70L * 365 * 24 * 60 * 60 * 1000));
        } else {
            URL url = getUrl(mapName);

            if (url == null) {
                UtilsLog.e(TAG, "getFileInfo", "url == null");
                return null;
            }

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);

            connection.connect();

            lastModified = connection.getLastModified();

            connection.disconnect();
        }

        if (lastModified == 0) {
            UtilsLog.e(TAG, "getFileInfo", "lastModified == 0");
            return null;
        }

        return new FileInfo(mapName, new Date(lastModified));
    }

    @NonNull
    private static List<FileInfo> getFileInfoList(@NonNull List<String> mapNames) throws IOException {
        List<FileInfo> fileInfoList = new ArrayList<>();

        UtilsLog.d(LOG_ENABLED, TAG, "getFileInfoList", "start");

        for (String mapName : mapNames) {
            FileInfo fileInfo = getFileInfo(mapName);

            UtilsLog.d(LOG_ENABLED, TAG, "getFileInfoList", "fileInfo == " + fileInfo);

            if (fileInfo != null) {
                fileInfoList.add(fileInfo);
            }
        }

        UtilsLog.d(LOG_ENABLED, TAG, "getFileInfoList", "end");

        return fileInfoList;
    }

    public static long getTimestamp(@NonNull List<String> mapNames) throws IOException {
        if (BuildConfig.DEBUG && DEBUG_RETURN_CURRENT_DATE) {
            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            return calendar.getTime().getTime();
        }

        if (BuildConfig.DEBUG && DEBUG_DUMMY_FILE_INFO) {
            Random rand = new Random();

            if (rand.nextBoolean()) {
                return Consts.BAD_DATETIME;
            }
        }

        List<FileInfo> fileInfoList = getFileInfoList(mapNames);

        return MapFilesHelper.getLatestTimestamp(fileInfoList);
    }
}