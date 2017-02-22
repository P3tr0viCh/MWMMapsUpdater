package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.content.Context;
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
import java.util.regex.Pattern;

import ru.p3tr0vich.mwmmapsupdater.BuildConfig;
import ru.p3tr0vich.mwmmapsupdater.models.FileInfo;
import ru.p3tr0vich.mwmmapsupdater.models.MapFiles;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsFiles;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class MapFilesHelper {

    private static final String TAG = "MapFilesHelper";

    private static final boolean LOG_ENABLED = true;

    private static final boolean DUMMY_FILE_INFO = false;
    private static final boolean WAIT_ENABLED = false;

    private static final Comparator<String> MAP_SUB_DIR_COMPARATOR = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o2.compareTo(o1);
        }
    };

    private static final Pattern MAP_SUB_DIR_NAME_PATTERN = Pattern.compile("\\d{6}", Pattern.CASE_INSENSITIVE);

    private static final Pattern MAP_FILE_NAME_PATTERN = Pattern.compile("^(.+)(\\.mwm)$", Pattern.CASE_INSENSITIVE);
    private static final int MAP_FILE_NAME_PATTERN_GROUP_INDEX = 1;

    private static final String MAPS_INFO_FILE_NAME = "maps_info.json";

    private static final String JSON_TIMESTAMP = "timestamp";
    private static final String JSON_MAP_SUB_DIR = "directory";
    private static final String JSON_FILES = "files";
    private static final String JSON_FILE_NAME = "name";
    private static final String JSON_FILE_TIMESTAMP = "timestamp";

    @NonNull
    private static File getFile(@NonNull Context context) {
        return new File(context.getFilesDir(), MAPS_INFO_FILE_NAME);
    }

    private static boolean readFromJSONFile(@NonNull Context context, @NonNull MapFiles mapFiles) {
        File file = getFile(context);

        try {
            UtilsFiles.checkExists(file);

            JSONObject json = UtilsFiles.readJSON(file);

            UtilsLog.d(LOG_ENABLED, TAG, "readFromJSONFile", "json == " + json.toString());

            mapFiles.setTimestamp(json.getLong(JSON_TIMESTAMP));

            mapFiles.setMapSubDir(json.getString(JSON_MAP_SUB_DIR));

            JSONArray files = json.getJSONArray(JSON_FILES);

            JSONObject fileInfo;
            List<FileInfo> fileInfoList = mapFiles.getFileList();
            for (int i = 0, l = files.length(); i < l; i++) {
                fileInfo = files.getJSONObject(i);
                fileInfoList.add(new FileInfo(fileInfo.getString(JSON_FILE_NAME), new Date(fileInfo.getLong(JSON_FILE_TIMESTAMP))));
            }

            Collections.sort(fileInfoList);

            return true;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            UtilsLog.e(TAG, "readFromJSONFile", e);
        }

        return false;
    }

    private static boolean writeToJSONFile(@NonNull Context context, @NonNull MapFiles mapFiles) {
        JSONObject json = new JSONObject();

        try {
            json.put(JSON_TIMESTAMP, mapFiles.getTimestamp());
            json.put(JSON_MAP_SUB_DIR, mapFiles.getMapSubDir());

            JSONArray files = new JSONArray();

            JSONObject fileInfoObject;

            for (FileInfo fileInfo : mapFiles.getFileList()) {
                fileInfoObject = new JSONObject();

                fileInfoObject.put(JSON_FILE_NAME, fileInfo.getMapName());
                fileInfoObject.put(JSON_FILE_TIMESTAMP, fileInfo.getDate().getTime());

                files.put(fileInfoObject);
            }

            json.put(JSON_FILES, files);

            UtilsLog.d(LOG_ENABLED, TAG, "writeToJSONFile", "json == " + json.toString());

            File file = getFile(context);

            UtilsFiles.writeJSON(file, json);

            return true;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            UtilsLog.e(TAG, "writeToJSONFile", e);
        }

        return false;
    }

    public static void deleteJSONFile(@NonNull Context context) {
        File file = getFile(context);
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    @Nullable
    private static FileInfo getFileInfo(@NonNull File mapSubDir, @NonNull String mapName) {
        long lastModified;

        if (BuildConfig.DEBUG && DUMMY_FILE_INFO) {
            Random rand = new Random();

            lastModified = -946771200000L + (Math.abs(rand.nextLong()) % (70L * 365 * 24 * 60 * 60 * 1000));
        } else {
            File file = new File(mapSubDir, mapName + ".mwm");

            lastModified = file.lastModified();
        }

        if (lastModified == 0) {
            UtilsLog.e(TAG, "getFileInfo", "lastModified == 0");
            return null;
        }

        return new FileInfo(mapName, new Date(lastModified));
    }

    private static long mapDirNameToDate(@NonNull String mapDirName) {
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
    public static MapFiles find(@NonNull Context context, @NonNull String parentMapsDir) {
        MapFiles mapFiles = findFiles(parentMapsDir);

        if (mapFiles.getFileList().isEmpty()) {
            deleteJSONFile(context);
        } else {
            checkFiles(context, mapFiles);
        }

        return mapFiles;
    }

    @NonNull
    private static MapFiles findFiles(@NonNull String mapDirName) {
        if (BuildConfig.DEBUG && WAIT_ENABLED) {
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

                matcher = MAP_SUB_DIR_NAME_PATTERN.matcher(fileName);

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

                matcher = MAP_FILE_NAME_PATTERN.matcher(fileName);

                if (matcher.find()) {
                    // fileName без расширения
                    String mapName = matcher.group(MAP_FILE_NAME_PATTERN_GROUP_INDEX);

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

    private static void checkFiles(@NonNull Context context, @NonNull MapFiles mapFiles) {
        MapFiles savedMapFiles = new MapFiles();

        boolean filesEquals = readFromJSONFile(context, savedMapFiles);

        if (filesEquals) {
            filesEquals = mapFiles.getMapSubDir().equals(savedMapFiles.getMapSubDir()) &&
                    mapFiles.getFileList().equals(savedMapFiles.getFileList());
        }

        UtilsLog.d(LOG_ENABLED, TAG, "checkFiles", "filesEquals == " + filesEquals);

        if (filesEquals) {
            mapFiles.setTimestamp(savedMapFiles.getTimestamp());
        } else {
            mapFiles.setTimestamp(mapDirNameToDate(mapFiles.getMapSubDir()));
            writeToJSONFile(context, mapFiles);
        }
    }
}