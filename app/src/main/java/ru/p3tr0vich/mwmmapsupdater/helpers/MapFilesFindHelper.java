package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.p3tr0vich.mwmmapsupdater.models.MapFiles;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class MapFilesFindHelper {

    private static final String TAG = "MapFilesFindHelper";

    private static final boolean WAIT_ENABLED = false;

    private static final Comparator<String> MAP_SUB_DIR_COMPARATOR = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o2.compareTo(o1);
        }
    };

    @NonNull
    public static MapFiles find(@NonNull String mapDirName) {
        if (WAIT_ENABLED) {
            for (int i = 0, waitSeconds = 3; i < waitSeconds; i++) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                UtilsLog.d(TAG, "findFiles", "wait... " + (waitSeconds - i));
            }
        }

        @MapFiles.Result int result;
        String mapSubDirName = null;
        List<String> fileNameList = null;

        if (!mapDirName.isEmpty()) {

            File mapDir = new File(mapDirName);

            if (mapDir.exists() && mapDir.isDirectory()) {
                File[] listFiles = mapDir.listFiles();

                if (listFiles != null) {
                    List<String> subDirNamesList = new ArrayList<>();

                    String fileName;

                    for (File file : listFiles) {
                        if (file.isDirectory()) {
                            fileName = file.getName();

                            if (fileName.matches("\\d{6}")) {
                                subDirNamesList.add(fileName);
                            }
                        }
                    }

                    if (subDirNamesList.isEmpty()) {
                        result = MapFiles.RESULT_SUB_DIR_NOT_EXISTS;
                    } else {
                        UtilsLog.d(TAG, "findFiles", "before sort");
                        for (String name : subDirNamesList) {
                            UtilsLog.d(TAG, "findFiles", name);
                        }

                        Collections.sort(subDirNamesList, MAP_SUB_DIR_COMPARATOR);

                        UtilsLog.d(TAG, "findFiles", "after sort");
                        for (String name : subDirNamesList) {
                            UtilsLog.d(TAG, "findFiles", name);
                        }

                        mapSubDirName = subDirNamesList.get(0);

                        File mapSubDir = new File(mapDir, mapSubDirName);
                        listFiles = mapSubDir.listFiles();

                        if (listFiles != null) {
                            fileNameList = new ArrayList<>();

                            final Pattern pattern = Pattern.compile("^(\\w+)(\\.mwm)$", Pattern.CASE_INSENSITIVE);

                            for (File file : listFiles) {
                                if (file.isFile()) {
                                    fileName = file.getName();

                                    final Matcher matcher = pattern.matcher(fileName);

                                    if (matcher.find()) {
                                        fileNameList.add(matcher.group(0));
                                    }
                                }
                            }

                            if (fileNameList.isEmpty()) {
                                result = MapFiles.RESULT_FILES_NOT_EXISTS;
                            } else {
                                UtilsLog.d(TAG, "findFiles", "before sort");
                                for (String name : fileNameList) {
                                    UtilsLog.d(TAG, "findFiles", name);
                                }

                                Collections.sort(fileNameList);

                                UtilsLog.d(TAG, "findFiles", "after sort");
                                for (String name : fileNameList) {
                                    UtilsLog.d(TAG, "findFiles", name);
                                }

                                result = MapFiles.RESULT_OK;
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

        return new MapFiles(result, mapDirName, mapSubDirName, fileNameList);
    }
}