package ru.p3tr0vich.mwmmapsupdater.models;

import android.support.annotation.NonNull;

import ru.p3tr0vich.mwmmapsupdater.Consts;

public class FileInfo implements Comparable<FileInfo> {

    private final String mMapName;
    private long mTimestamp;

    public FileInfo(@NonNull String mapName) {
        mMapName = mapName;
        mTimestamp = Consts.BAD_DATETIME;
    }

    public String getMapName() {
        return mMapName;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfo fileInfo = (FileInfo) o;

        return mMapName.equals(fileInfo.mMapName) && mTimestamp == fileInfo.mTimestamp;
    }

    @Override
    public int hashCode() {
        int result = mMapName.hashCode();
        result = 31 * result + (int) (mTimestamp ^ (mTimestamp >>> 32));
        return result;
    }

    @Override
    public int compareTo(@NonNull FileInfo o) {
        return mMapName.compareTo(o.mMapName);
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "mMapName='" + mMapName + '\'' +
                ", mTimestamp=" + mTimestamp +
                '}';
    }
}