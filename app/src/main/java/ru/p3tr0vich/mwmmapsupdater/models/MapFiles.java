package ru.p3tr0vich.mwmmapsupdater.models;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class MapFiles {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RESULT_OK, RESULT_DIR_NOT_EXISTS, RESULT_SUB_DIR_NOT_EXISTS, RESULT_FILES_NOT_EXISTS})
    public @interface Result {
    }

    public static final int RESULT_OK = 0;
    public static final int RESULT_DIR_NOT_EXISTS = 1;
    public static final int RESULT_SUB_DIR_NOT_EXISTS = 2;
    public static final int RESULT_FILES_NOT_EXISTS = 3;

    @Result
    private int mResult;

    private String mMapDir;
    private String mMapSubDir;

    private List<String> mFileList;

    public MapFiles(@Result int result, @NonNull String mapDir, @Nullable String mapSubDir, @Nullable List<String> fileList) {
        mResult = result;
        mMapDir = mapDir;
        mMapSubDir = mapSubDir;
        mFileList = fileList;
    }

    @Result
    public int getResult() {
        return mResult;
    }

    @Nullable
    public String getMapDir() {
        return mMapDir;
    }

    @Nullable
    public String getMapSubDir() {
        return mMapSubDir;
    }

    @Nullable
    public List<String> getFileList() {
        return mFileList;
    }

    @Override
    public String toString() {
        return "MapFiles{" +
                "mResult=" + mResult +
                ", mMapDir='" + mMapDir + '\'' +
                ", mMapSubDir='" + mMapSubDir + '\'' +
                ", mFileList=" + mFileList +
                '}';
    }
}