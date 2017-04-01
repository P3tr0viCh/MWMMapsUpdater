package ru.p3tr0vich.mwmmapsupdater.utils;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

@SuppressWarnings("TryFinallyCanBeTryWithResources") // need min 19 api, current 17
public class UtilsFiles {

    private static final String TAG = "UtilsFiles";

    public static boolean recursiveDelete(@NonNull File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                if (!recursiveDelete(child)) {
                    return false;
                }
            }
        }

        return fileOrDirectory.delete();
    }

    public static boolean recursiveDeleteInDirectory(@NonNull File directory) {
        if (directory.isDirectory()) {
            for (File child : directory.listFiles()) {
                if (!recursiveDelete(child)) {
                    return false;
                }
            }
        }

        return true;
    }

    @NonNull
    public static JSONObject readJSON(@NonNull File file) throws IOException, JSONException {
        FileInputStream fileInputStream = new FileInputStream(file);

        try {
            int size = fileInputStream.available();

            byte[] buffer = new byte[size];

            //noinspection ResultOfMethodCallIgnored
            fileInputStream.read(buffer);

            String json = new String(buffer, "UTF-8");

            return new JSONObject(json);
        } finally {
            fileInputStream.close();
        }
    }

    public static void writeJSON(@NonNull File file, @NonNull JSONObject jsonObject) throws IOException, JSONException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

            try {
                outputStreamWriter.write(jsonObject.toString(3));
            } finally {
                outputStreamWriter.close();
            }
        } finally {
            fileOutputStream.close();
        }
    }

    public static void createFile(@NonNull File file) throws IOException {
        if (file.exists()) {
            if (!file.isFile()) {
                throw new FileNotFoundException(TAG + " -- createFile: " + file.toString()
                        + " exists and is not a file");
            }
        } else {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        }
    }

    @CheckResult
    public static boolean isFileExists(@NonNull File file) {
        return file.exists() && file.isFile();
    }

    @CheckResult
    public static boolean isDirExists(@NonNull File dir) {
        return dir.exists() && dir.isDirectory();
    }

    public static void makeDir(@NonNull File dir) throws IOException {
        if (isDirExists(dir)) {
            return;
        }

        if (!dir.mkdirs()) {
            throw new IOException(TAG + " -- makeDir: can not create dir " + dir.toString());
        }
    }

    public static void rename(@NonNull File source, @NonNull File dest) throws IOException {
        if (!source.renameTo(dest)) {
            throw new IOException(TAG + " -- rename: can not rename file from " +
                    source.toString() + " to " + dest.toString());
        }
    }

    public static void delete(@NonNull File fileOrDir) throws IOException {
        if (!fileOrDir.delete()) {
            throw new IOException(TAG + " -- delete: can not delete file " + fileOrDir.toString());
        }
    }
}