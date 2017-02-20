package ru.p3tr0vich.mwmmapsupdater.models;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Date;
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
    private final int mResult;

    private final String mMapDir;
    private final String mMapSubDir;

    private final List<FileInfo> mFileList;

    private final Date mDate;

    public MapFiles(@Result int result, @NonNull String mapDir, @NonNull String mapSubDir, @NonNull List<FileInfo> fileList, @NonNull Date date) {
        mResult = result;
        mMapDir = mapDir;
        mMapSubDir = mapSubDir;
        mFileList = fileList;
        mDate = date;
    }

    @Result
    public int getResult() {
        return mResult;
    }

    @NonNull
    public String getMapDir() {
        return mMapDir;
    }

    @NonNull
    public String getMapSubDir() {
        return mMapSubDir;
    }

    @NonNull
    public List<FileInfo> getFileList() {
        return mFileList;
    }

    public Date getDate() {
        return mDate;
    }

    @NonNull
    public List<String> getMapNameList() {
        List<String> mapNames = new ArrayList<>();

        for (FileInfo fileInfo : mFileList) {
            mapNames.add(fileInfo.getMapName());
        }

        return mapNames;
    }

    @Override
    public String toString() {
        return "MapFiles{" +
                "mResult=" + mResult +
                ", mMapDir='" + mMapDir + '\'' +
                ", mMapSubDir='" + mMapSubDir + '\'' +
                ", mFileList=" + mFileList +
                ", mDate=" + mDate +
                '}';
    }
}