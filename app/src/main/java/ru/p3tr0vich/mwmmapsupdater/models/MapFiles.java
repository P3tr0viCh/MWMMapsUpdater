package ru.p3tr0vich.mwmmapsupdater.models;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MapFiles {

    private long mTimestamp;

    private String mMapDir;
    private String mMapSubDir;

    private final List<FileInfo> mFileList;

    public MapFiles() {
        mFileList = new ArrayList<>();
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MapFiles that = (MapFiles) o;

        return mMapDir.equals(that.mMapDir) &&
                mMapSubDir.equals(that.mMapSubDir) &&
                mFileList.equals(that.mFileList);
    }

    @Override
    public String toString() {
        return "MapFiles{" +
                "mTimestamp=" + mTimestamp +
                ", mMapDir='" + mMapDir + '\'' +
                ", mMapSubDir='" + mMapSubDir + '\'' +
                ", mFileList=" + mFileList +
                '}';
    }
}