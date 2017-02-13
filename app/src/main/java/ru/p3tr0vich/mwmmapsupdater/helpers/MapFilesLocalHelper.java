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

public class MapFilesLocalHelper {

    private static final String TAG = "MapFilesLocalHelper";

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
                            fileNameList = new ArrayList<>();

                            for (File file : listFiles) {
                                if (file.isFile()) {
                                    fileName = file.getName();

                                    matcher = MAP_FILE_NAME_PATTERN.matcher(fileName);

                                    if (matcher.find()) {
                                        fileNameList.add(matcher.group(MAP_FILE_NAME_PATTERN_GROUP_INDEX));
                                    }
                                }
                            }

                            if (fileNameList.isEmpty()) {
                                result = MapFiles.RESULT_FILES_NOT_EXISTS;
                            } else {
                                Collections.sort(fileNameList);

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