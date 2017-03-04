package ru.p3tr0vich.mwmmapsupdater.models;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MapFiles {
    private String mMapDir;
    private String mMapSubDir;

    private final List<FileInfo> mFileList;

    public MapFiles() {
        mMapDir = "";
        mMapSubDir = "";

        mFileList = new ArrayList<>();
    }

    @NonNull
    public String getMapDir() {
        return mMapDir;
    }

    public void setMapDir(@NonNull String mapDir) {
        mMapDir = mapDir;
    }

    @NonNull
    public String getMapSubDir() {
        return mMapSubDir;
    }

    public void setMapSubDir(@NonNull String mapSubDir) {
        mMapSubDir = mapSubDir;
    }

    @NonNull
    public List<FileInfo> getFileList() {
        return mFileList;
    }

    public void setFileList(@NonNull List<FileInfo> fileList) {
        mFileList.clear();
        mFileList.addAll(fileList);
    }

    @Override
    public String toString() {
        return "MapFiles{" +
                "mMapDir='" + mMapDir + '\'' +
                ", mMapSubDir='" + mMapSubDir + '\'' +
                ", mFileList=" + mFileList +
                '}';
    }
}