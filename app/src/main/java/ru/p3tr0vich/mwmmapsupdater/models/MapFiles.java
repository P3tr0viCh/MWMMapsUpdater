package ru.p3tr0vich.mwmmapsupdater.models;

import android.support.annotation.Nullable;

import java.util.List;

public class MapFiles {

    private String mMapDir;
    private String mMapSubDir;

    private List<String> mFileList;

    public MapFiles(@Nullable String mapDir, @Nullable String mapSubDir, @Nullable List<String> fileList) {
        mMapDir = mapDir;
        mMapSubDir = mapSubDir;
        mFileList = fileList;
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
}