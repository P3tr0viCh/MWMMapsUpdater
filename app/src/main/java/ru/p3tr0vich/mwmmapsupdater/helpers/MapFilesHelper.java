package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
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

    private interface JsonFields {
        String MAP_SUB_DIR = "directory";

        String FILES = "files";

        String FILE_NAME = "name";
        String FILE_TIMESTAMP = "timestamp";
    }

    private MapFilesHelper() {
    }

    public static boolean readFromJSONFile(@NonNull Context context, @NonNull MapFiles mapFiles) {
        try {
            File file = FilesHelper.getMapsInfoFile(context);

            if (!UtilsFiles.isFileExists(file)) {
                return false;
            }

            JSONObject json = UtilsFiles.readJSON(file);

            UtilsLog.d(LOG_ENABLED, TAG, "readFromJSONFile", "json == " + json.toString());

            mapFiles.setMapSubDir(json.getString(JsonFields.MAP_SUB_DIR));

            JSONArray files = json.getJSONArray(JsonFields.FILES);

            JSONObject jsonFileInfo;
            FileInfo fileInfo;

            List<FileInfo> fileInfoList = mapFiles.getFileList();

            for (int i = 0, l = files.length(); i < l; i++) {
                jsonFileInfo = files.getJSONObject(i);

                fileInfo = new FileInfo(jsonFileInfo.getString(JsonFields.FILE_NAME));
                fileInfo.setTimestamp(jsonFileInfo.getLong(JsonFields.FILE_TIMESTAMP));

                fileInfoList.add(fileInfo);
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
                fileInfoObject.put(JsonFields.FILE_TIMESTAMP, fileInfo.getTimestamp());

                files.put(fileInfoObject);
            }

            json.put(JsonFields.FILES, files);

            UtilsLog.d(LOG_ENABLED, TAG, "writeToJSONFile", "json == " + json.toString());

            File file = FilesHelper.getMapsInfoFile(context);

            UtilsFiles.writeJSON(file, json);

            return true;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            UtilsLog.e(TAG, "writeToJSONFile", e);
        }

        return false;
    }

    public static void deleteJSONFile(@NonNull Context context) {
        File file = FilesHelper.getMapsInfoFile(context);
        //noinspection ResultOfMethodCallIgnored
        file.delete();
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

    private static void updateFileInfo(@NonNull File mapSubDir, @NonNull FileInfo fileInfo) {
        long lastModified;

        if (BuildConfig.DEBUG && DEBUG_DUMMY_FILE_INFO) {
            Random rand = new Random();

            lastModified = -946771200000L + (Math.abs(rand.nextLong()) % (70L * 365 * 24 * 60 * 60 * 1000));
        } else {
            File file = new File(mapSubDir, fileInfo.getMapName() + Consts.MAP_FILE_NAME_EXT);

            lastModified = file.lastModified();
        }

        if (lastModified == 0) {
            UtilsLog.e(TAG, "updateFileInfo", "lastModified == 0");
        }

        fileInfo.setTimestamp(lastModified);
    }

    public static void updateFileInfoList(@NonNull MapFiles mapFiles) {
        File mapSubDir = new File(mapFiles.getMapDir(), mapFiles.getMapSubDir());

        for (FileInfo fileInfo : mapFiles.getFileList()) {
            updateFileInfo(mapSubDir, fileInfo);
        }
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
            fileInfoList.add(new FileInfo(mapName));
        }

        Collections.sort(fileInfoList);

        mapFiles.setFileList(fileInfoList);

        updateFileInfoList(mapFiles);

        if (LOG_ENABLED) {
            for (FileInfo fileInfo : mapFiles.getFileList()) {
                UtilsLog.d(true, TAG, "find", "fileInfo == " + fileInfo);
            }
        }

        return mapFiles;
    }

    public static long getLatestTimestamp(@NonNull List<FileInfo> fileInfoList) {
        List<Long> dates = new ArrayList<>();

        for (FileInfo fileInfo : fileInfoList) {
            dates.add(fileInfo.getTimestamp());
        }

        long timestamp = Consts.BAD_DATETIME;

        if (!dates.isEmpty()) {
            Collections.sort(dates, LATEST_TIMESTAMP_COMPARATOR);

            timestamp = dates.get(0);
        }

        return timestamp;
    }
}