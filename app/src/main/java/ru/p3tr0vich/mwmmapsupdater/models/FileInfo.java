package ru.p3tr0vich.mwmmapsupdater.models;

import android.support.annotation.NonNull;

import java.util.Date;

public class FileInfo implements Comparable<FileInfo> {

    private final String mMapName;
    private final Date mDate;

    public FileInfo(@NonNull String mapName, @NonNull Date date) {
        mMapName = mapName;
        mDate = date;
    }

    public String getMapName() {
        return mMapName;
    }

    @NonNull
    public Date getDate() {
        return mDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfo fileInfo = (FileInfo) o;

        return mMapName.equals(fileInfo.mMapName) && mDate.equals(fileInfo.mDate);
    }

    @Override
    public int hashCode() {
        int result = mMapName.hashCode();
        result = 31 * result + mDate.hashCode();
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
                ", mDate=" + mDate +
                '}';
    }
}