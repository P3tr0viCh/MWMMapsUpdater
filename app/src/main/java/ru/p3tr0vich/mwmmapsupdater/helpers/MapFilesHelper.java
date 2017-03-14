package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import ru.p3tr0vich.mwmmapsupdater.BuildConfig;
import ru.p3tr0vich.mwmmapsupdater.Consts;
import ru.p3tr0vich.mwmmapsupdater.models.FileInfo;
import ru.p3tr0vich.mwmmapsupdater.models.MapFiles;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsFiles;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class MapFilesHelper {

    private static final String TAG = "MapFilesHelper";

    private static final boolean LOG_ENABLED = true;

    private static final boolean DEBUG_DUMMY_FILE_INFO = false;
    private static final boolean DEBUG_WAIT_ENABLED = false;

    private static final Comparator<String> MAP_SUB_DIR_COMPARATOR = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o2.compareTo(o1);
        }
    };

    private static final Comparator<Long> LATEST_TIMESTAMP_COMPARATOR = new Comparator<Long>() {
        @Override
        public int compare(Long o1, Long o2) {
            return o2.compareTo(o1);
        }
    };

    private static final String MAPS_INFO_FILE_NAME = "maps_info.json";

    /**
     * Имя каталога с картами по умолчанию, располагается в корне.
     */
    private static final String DEFAULT_PARENT_MAPS_DIR_NAME = "MapsWithMe";

    /**
     * Имя каталога для загруженных карт, располагается в каталоге downloads.
     */
    private static final String DOWNLOAD_DIR_NAME = "MapsWithMe maps";

    /**
     * Имя каталога для сохранения оригинальных карт, располагается в корне.
     */
    private static final String BACKUP_MAPS_DIR_NAME = "MapsWithMe backup";

    private interface JsonFields {
        String MAP_SUB_DIR = "directory";

        String FILES = "files";

        String FILE_NAME = "name";
        String FILE_TIMESTAMP = "timestamp";
    }

    private MapFilesHelper() {
    }

    @NonNull
    private static File getMapsInfoFile(@NonNull Context context) {
        return new File(context.getFilesDir(), MAPS_INFO_FILE_NAME);
    }

    @NonNull
    public static File getDefaultParentMapsDir() {
        return new File(Environment.getExternalStorageDirectory(), DEFAULT_PARENT_MAPS_DIR_NAME);
    }

    @NonNull
    public static File getDownloadDir() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                DOWNLOAD_DIR_NAME);
    }

    @NonNull
    public static File getBackupMapsDir() {
        return new File(Environment.getExternalStorageDirectory(), BACKUP_MAPS_DIR_NAME);
    }

    public static boolean readFromJSONFile(@NonNull Context context, @NonNull MapFiles mapFiles) {
        try {
            File file = getMapsInfoFile(context);

            if (!UtilsFiles.isFileExists(file)) {
                return false;
            }

            JSONObject json = UtilsFiles.readJSON(file);

            UtilsLog.d(LOG_ENABLED, TAG, "readFromJSONFile", "json == " + json.toString());

            mapFiles.setMapSubDir(json.getString(JsonFields.MAP_SUB_DIR));

            JSONArray files = json.getJSONArray(JsonFields.FILES);

            JSONObject fileInfo;
            List<FileInfo> fileInfoList = mapFiles.getFileList();
            for (int i = 0, l = files.length(); i < l; i++) {
                fileInfo = files.getJSONObject(i);
                fileInfoList.add(new FileInfo(fileInfo.getString(JsonFields.FILE_NAME), new Date(fileInfo.getLong(JsonFields.FILE_TIMESTAMP))));
            }

            Collections.sort(fileInfoList);

            return true;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            UtilsLog.e(TAG, "readFromJSONFile", e);
        }

        return false;
    }

    public static boolean writeToJSONFile(@NonNull Context context, @NonNull MapFiles mapFiles) {
        try {
            JSONObject json = new JSONObject();

            json.put(JsonFields.MAP_SUB_DIR, mapFiles.getMapSubDir());

            JSONArray files = new JSONArray();

            JSONObject fileInfoObject;

            for (FileInfo fileInfo : mapFiles.getFileList()) {
                fileInfoObject = new JSONObject();

                fileInfoObject.put(JsonFields.FILE_NAME, fileInfo.getMapName());
                fileInfoObject.put(JsonFields.FILE_TIMESTAMP, fileInfo.getDate().getTime());

                files.put(fileInfoObject);
            }

            json.put(JsonFields.FILES, files);

            UtilsLog.d(LOG_ENABLED, TAG, "writeToJSONFile", "json == " + json.toString());

            File file = getMapsInfoFile(context);

            UtilsFiles.writeJSON(file, json);

            return true;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            UtilsLog.e(TAG, "writeToJSONFile", e);
        }

        return false;
    }

    public static void deleteJSONFile(@NonNull Context context) {
        File file = getMapsInfoFile(context);
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    @Nullable
    private static FileInfo getFileInfo(@NonNull File mapSubDir, @NonNull String mapName) {
        long lastModified;

        if (BuildConfig.DEBUG && DEBUG_DUMMY_FILE_INFO) {
            Random rand = new Random();

            lastModified = -946771200000L + (Math.abs(rand.nextLong()) % (70L * 365 * 24 * 60 * 60 * 1000));
        } else {
            File file = new File(mapSubDir, mapName + Consts.MAP_FILE_NAME_EXT);

            lastModified = file.lastModified();
        }

        if (lastModified == 0) {
            UtilsLog.e(TAG, "getFileInfo", "lastModified == 0");
            return null;
        }

        return new FileInfo(mapName, new Date(lastModified));
    }

    public static long mapDirNameToTimestamp(@NonNull String mapDirName) {
        if (!TextUtils.isEmpty(mapDirName)) {
            try {
                // mapSubDir == '171232' ==> '180101';
                return new SimpleDateFormat("yyMMdd", Locale.US).parse(mapDirName).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
                UtilsLog.e(TAG, "mapDirNameToDate", e);
            }
        }

        return 0;
    }

    @NonNull
    public static MapFiles find(@NonNull String mapDirName) {
        if (BuildConfig.DEBUG && DEBUG_WAIT_ENABLED) {
            for (int i = 0, waitSeconds = 5; i < waitSeconds; i++) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                UtilsLog.d(true, TAG, "findFiles", "wait... " + (waitSeconds - i));
            }
        }

        MapFiles mapFiles = new MapFiles();

        if (mapDirName.isEmpty()) {
            UtilsLog.e(TAG, "findFiles", "Maps directory empty");
            return mapFiles;
        }

        File mapDir = new File(mapDirName);

        if (!mapDir.exists() || !mapDir.isDirectory()) {
            UtilsLog.e(TAG, "findFiles", "Maps directory not exists or not directory");
            return mapFiles;
        }

        mapFiles.setMapDir(mapDirName);

        File[] listFiles = mapDir.listFiles();

        if (listFiles == null || listFiles.length == 0) {
            UtilsLog.e(TAG, "findFiles", "Maps directory empty");
            return mapFiles;
        }

        List<String> subDirNamesList = new ArrayList<>();

        String fileName;

        Matcher matcher;

        for (File file : listFiles) {
            if (file.isDirectory()) {
                fileName = file.getName();

                matcher = Consts.MAP_SUB_DIR_NAME_PATTERN.matcher(fileName);

                if (matcher.find()) {
                    subDirNamesList.add(fileName);
                }
            }
        }

        if (subDirNamesList.isEmpty()) {
            UtilsLog.e(TAG, "findFiles", "Subdirectories not exists");
            return mapFiles;
        }

        Collections.sort(subDirNamesList, MAP_SUB_DIR_COMPARATOR);

        String mapSubDirName = subDirNamesList.get(0);

        mapFiles.setMapSubDir(mapSubDirName);

        File mapSubDir = new File(mapDir, mapSubDirName);
        listFiles = mapSubDir.listFiles();

        if (listFiles == null || listFiles.length == 0) {
            UtilsLog.e(TAG, "findFiles", "Subdirectory empty");
            return mapFiles;
        }

        List<String> mapNameList = new ArrayList<>();

        for (File file : listFiles) {
            if (file.isFile()) {
                fileName = file.getName();

                matcher = Consts.MAP_FILE_NAME_PATTERN.matcher(fileName);

                if (matcher.find()) {
                    String mapName = matcher.group(Consts.MAP_FILE_NAME_PATTERN_GROUP_INDEX);

                    mapNameList.add(mapName);
                }
            }
        }

        if (mapNameList.isEmpty()) {
            UtilsLog.e(TAG, "findFiles", "Map files in subdirectory not exists");
            return mapFiles;
        }

        List<FileInfo> fileInfoList = new ArrayList<>();

        for (String mapName : mapNameList) {
            FileInfo fileInfo = getFileInfo(mapSubDir, mapName);

            UtilsLog.d(LOG_ENABLED, TAG, "find", "fileInfo == " + fileInfo);

            if (fileInfo != null) {
                fileInfoList.add(fileInfo);
            }
        }

        if (fileInfoList.isEmpty()) {
            UtilsLog.e(TAG, "findFiles", "Map files read file info error");
            return mapFiles;
        }

        Collections.sort(fileInfoList);

        mapFiles.setFileList(fileInfoList);

        return mapFiles;
    }

    public static long getLatestTimestamp(@NonNull List<FileInfo> fileInfoList) {
        List<Long> dates = new ArrayList<>();

        for (FileInfo fileInfo : fileInfoList) {
            dates.add(fileInfo.getDate().getTime());
        }

        long timestamp = Consts.BAD_DATETIME;

        if (!dates.isEmpty()) {
            Collections.sort(dates, LATEST_TIMESTAMP_COMPARATOR);

            timestamp = dates.get(0);
        }

        return timestamp;
    }
}