package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;

public class FilesHelper {

    private static final String MAPS_INFO_FILE_NAME = "maps_info.json";
    private static final String DOWNLOAD_INFO_FILE_NAME = "download.json";

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

    private FilesHelper() {
    }

    @NonNull
    public static File getMapsInfoFile(@NonNull Context context) {
        return new File(context.getFilesDir(), MAPS_INFO_FILE_NAME);
    }

    @NonNull
    public static File getDownloadInfoFile(@NonNull Context context) {
        return new File(context.getFilesDir(), DOWNLOAD_INFO_FILE_NAME);
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
}