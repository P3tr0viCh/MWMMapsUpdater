package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

import ru.p3tr0vich.mwmmapsupdater.Models.MapFiles;

public class MapFilesFindHelper {

    private static final boolean WAIT_ENABLED = true;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RESULT_OK, RESULT_DIR_NOT_EXISTS, RESULT_SUB_DIR_NOT_EXISTS, RESULT_FILES_NOT_EXISTS})
    public @interface Result {
    }

    public static final int RESULT_OK = 0;
    public static final int RESULT_DIR_NOT_EXISTS = 1;
    public static final int RESULT_SUB_DIR_NOT_EXISTS = 2;
    public static final int RESULT_FILES_NOT_EXISTS = 3;

    @Result
    public static int find(@NonNull MapFiles mapFiles) {
        File mapDirFile = new File(mapFiles.getMapDir());

        if (WAIT_ENABLED) {
            for (int i = 0, waitSeconds = 3; i < waitSeconds; i++) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                UtilsLog.d(TAG, "findFiles", "wait... " + (waitSeconds - i));
            }
        }

        if (!mapDirFile.exists() || !mapDirFile.isDirectory()) {
            return RESULT_DIR_NOT_EXISTS;
        }

        return RESULT_OK;
    }
}