package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
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

public class MapFilesLocalHelper {

    private static final String TAG = "MapFilesLocalHelper";

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

    private static final DateFormat MAP_SUB_DIR_DATE_FORMAT = new SimpleDateFormat("yyMMdd", Locale.US);

    private static final String MAPS_INFO_FILE_NAME = "maps_info.json";

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

    @NonNull
    private static Date mapDirNameToDate(@NonNull String mapDirName) {
        if (!TextUtils.isEmpty(mapDirName)) {
            try {
                // mapSubDir == '171232' ==> '180101';
                return MAP_SUB_DIR_DATE_FORMAT.parse(mapDirName);
            } catch (ParseException e) {
                e.printStackTrace();
                UtilsLog.e(TAG, "mapDirNameToDate", "ParseException error == " + e.toString());
            }
        }

        return new Date();
    }

    @NonNull
    public static MapFiles find(@NonNull Context context, @NonNull String parentMapsDir) {
        if (BuildConfig.DEBUG && WAIT_ENABLED) {
            for (int i = 0, waitSeconds = 3; i < waitSeconds; i++) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                UtilsLog.d(true, TAG, "find", "wait... " + (waitSeconds - i));
            }
        }

        @MapFiles.Result int result;
        String mapSubDirName = "";
        List<FileInfo> fileInfoList = new ArrayList<>();

        if (!parentMapsDir.isEmpty()) {

            File mapDir = new File(parentMapsDir);

            if (mapDir.exists() && mapDir.isDirectory()) {
                File[] listFiles = mapDir.listFiles();

                if (listFiles != null) {
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
                        result = MapFiles.RESULT_SUB_DIR_NOT_EXISTS;
                    } else {
                        Collections.sort(subDirNamesList, MAP_SUB_DIR_COMPARATOR);

                        mapSubDirName = subDirNamesList.get(0);

                        File mapSubDir = new File(mapDir, mapSubDirName);
                        listFiles = mapSubDir.listFiles();

                        if (listFiles != null) {
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
                                result = MapFiles.RESULT_FILES_NOT_EXISTS;
                            } else {
                                // TODO: sort fileInfoList
                                Collections.sort(mapNameList);

                                for (String mapName : mapNameList) {
                                    FileInfo fileInfo = getFileInfo(mapSubDir, mapName);

                                    UtilsLog.d(LOG_ENABLED, TAG, "find", "fileInfo == " + fileInfo);

                                    if (fileInfo != null) {
                                        fileInfoList.add(fileInfo);
                                    }
                                }

                                if (fileInfoList.isEmpty()) {
                                    result = MapFiles.RESULT_FILES_NOT_EXISTS;
                                } else {
                                    result = MapFiles.RESULT_OK;
                                }
                            }
                        } else {
                            result = MapFiles.RESULT_FILES_NOT_EXISTS;
                        }
                    }
                } else {
                    result = MapFiles.RESULT_SUB_DIR_NOT_EXISTS;
                }
            } else {
                result = MapFiles.RESULT_DIR_NOT_EXISTS;
            }
        } else {
            result = MapFiles.RESULT_DIR_NOT_EXISTS;
        }

        Date date = null;

        File mapInfoFile = new File(context.getFilesDir(), MAPS_INFO_FILE_NAME);

        if (result == MapFiles.RESULT_OK) {
            boolean filesEquals;
            try {
                UtilsFiles.checkExists(mapInfoFile);
                filesEquals = true;
            } catch (FileNotFoundException e) {
                filesEquals = false;
            }

            long usedDate = 0;

            if (filesEquals) {
                String usedMapSubDirName = "";

                List<FileInfo> usedFileInfoList = new ArrayList<>();

                try {
                    JSONObject readObject = UtilsFiles.readJSON(mapInfoFile);

                    UtilsLog.d(LOG_ENABLED, TAG, "find", "readObject == " + readObject.toString());

                    usedDate = readObject.getLong("timestamp local");

                    usedMapSubDirName = readObject.getString("directory name");

                    JSONArray usedFileInfoArrayObject = readObject.getJSONArray("file list");

                    JSONObject jsonObject;
                    for (int i = 0, l = usedFileInfoArrayObject.length(); i < l; i++) {
                        jsonObject = usedFileInfoArrayObject.getJSONObject(i);
                        usedFileInfoList.add(new FileInfo(jsonObject.getString("name"), new Date(jsonObject.getLong("timestamp"))));
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    UtilsLog.e(TAG, "find", "read JSON error == " + e.toString());
                }

                filesEquals = usedMapSubDirName.equals(mapSubDirName);

                // TODO: sort usedFileInfoList

                if (filesEquals) {
                    filesEquals = usedFileInfoList.equals(fileInfoList);
                }
            }

            UtilsLog.d(LOG_ENABLED, TAG, "find", "filesEquals == " + filesEquals);

            if (filesEquals) {
                date = new Date(usedDate);
            } else {
                date = mapDirNameToDate(mapSubDirName);

                JSONObject resultObject = new JSONObject();
                try {
                    resultObject.put("timestamp local", date.getTime());
                    resultObject.put("directory name", mapSubDirName);

                    JSONArray fileInfoArrayObject = new JSONArray();

                    JSONObject fileInfoObject;

                    for (FileInfo fileInfo : fileInfoList) {
                        fileInfoObject = new JSONObject();
                        fileInfoObject.put("name", fileInfo.getMapName());
                        fileInfoObject.put("timestamp", fileInfo.getDate().getTime());
                        fileInfoArrayObject.put(fileInfoObject);
                    }

                    resultObject.put("file list", fileInfoArrayObject);

                    UtilsLog.d(LOG_ENABLED, TAG, "find", "resultObject == " + resultObject.toString());

                    UtilsFiles.writeJSON(mapInfoFile, resultObject);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    UtilsLog.e(TAG, "find", "write JSON error == " + e.toString());
                }
            }
        } else {
            //noinspection ResultOfMethodCallIgnored
            mapInfoFile.delete();
        }

        return new MapFiles(result, parentMapsDir, mapSubDirName, fileInfoList, date != null ? date : new Date());

    }
}