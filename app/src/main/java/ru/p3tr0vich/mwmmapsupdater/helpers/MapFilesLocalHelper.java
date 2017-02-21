package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.p3tr0vich.mwmmapsupdater.BuildConfig;
import ru.p3tr0vich.mwmmapsupdater.models.FileInfo;
import ru.p3tr0vich.mwmmapsupdater.models.LocalMapsInfo;
import ru.p3tr0vich.mwmmapsupdater.models.MapFiles;
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

        boolean result;
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
                        result = false;
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
                                result = false;
                            } else {
                                 for (String mapName : mapNameList) {
                                    FileInfo fileInfo = getFileInfo(mapSubDir, mapName);

                                    UtilsLog.d(LOG_ENABLED, TAG, "find", "fileInfo == " + fileInfo);

                                    if (fileInfo != null) {
                                        fileInfoList.add(fileInfo);
                                    }
                                }

                                if (fileInfoList.isEmpty()) {
                                    result = false;
                                } else {
                                    Collections.sort(fileInfoList);
                                    result = true;
                                }
                            }
                        } else {
                            result = false;
                        }
                    }
                } else {
                    result = false;
                }
            } else {
                result = false;
            }
        } else {
            result = false;
        }

        Date date;

        if (result) {
            LocalMapsInfo localMapsInfo = new LocalMapsInfo(context, mapSubDirName, fileInfoList);

            LocalMapsInfo usedLocalMapsInfo = new LocalMapsInfo(context);

            boolean filesEquals = usedLocalMapsInfo.readFromJSONFile();

            if (filesEquals) {
                filesEquals = usedLocalMapsInfo.equals(localMapsInfo);
            }

            if (filesEquals) {
                date = usedLocalMapsInfo.getDate();
            } else {
                date = localMapsInfo.getDate();
                localMapsInfo.writeToJSONFile();
            }

            UtilsLog.d(LOG_ENABLED, TAG, "find", "filesEquals == " + filesEquals);
        } else {
            date = new Date();
            LocalMapsInfo.deleteJSONFile(context);
        }

        return new MapFiles(result, parentMapsDir, mapSubDirName, fileInfoList, date);
    }
}