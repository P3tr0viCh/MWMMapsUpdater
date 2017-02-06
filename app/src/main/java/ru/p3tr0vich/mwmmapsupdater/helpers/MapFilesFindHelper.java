package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.concurrent.TimeUnit;

import ru.p3tr0vich.mwmmapsupdater.Models.MapFiles;

public class MapFilesFindHelper {

    private static final boolean WAIT_ENABLED = true;

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
            return 1;
        }

        return 0;
    }
}