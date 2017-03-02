package ru.p3tr0vich.mwmmapsupdater.models;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.p3tr0vich.mwmmapsupdater.Consts;

public class MapFiles {

    private long mLocalTimestamp;
    private long mServerTimestamp;

    private long mLastCheckTimestamp;

    private String mMapDir;
    private String mMapSubDir;

    private final List<FileInfo> mFileList;

    public MapFiles() {
        mLocalTimestamp = Consts.BAD_DATETIME;
        mServerTimestamp = Consts.BAD_DATETIME;

        mLastCheckTimestamp = Consts.BAD_DATETIME;

        mFileList = new ArrayList<>();
    }

    public long getLocalTimestamp() {
        return mLocalTimestamp;
    }

    public void setLocalTimestamp(long timestamp) {
        mLocalTimestamp = timestamp;
    }

    public long getServerTimestamp() {
        return mServerTimestamp;
    }

    public void setServerTimestamp(long timestamp) {
        mServerTimestamp = timestamp;
    }

    public long getLastCheckTimestamp() {
        return mLastCheckTimestamp;
    }

    public void setLastCheckTimestamp(long timestamp) {
        mLastCheckTimestamp = timestamp;
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
                "mLocalTimestamp=" + mLocalTimestamp +
                ", mServerTimestamp=" + mServerTimestamp +
                ", mLastCheckTimestamp=" + mLastCheckTimestamp +
                ", mMapDir='" + mMapDir + '\'' +
                ", mMapSubDir='" + mMapSubDir + '\'' +
                ", mFileList=" + mFileList +
                '}';
    }
}