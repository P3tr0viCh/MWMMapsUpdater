package ru.p3tr0vich.mwmmapsupdater.utils;

import android.support.annotation.NonNull;

import java.io.File;

public class UtilsFiles {

    public static boolean deleteAll(@NonNull File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                if (!deleteAll(child)) {
                    return false;
                }
            }
        }

        return fileOrDirectory.delete();
    }
}