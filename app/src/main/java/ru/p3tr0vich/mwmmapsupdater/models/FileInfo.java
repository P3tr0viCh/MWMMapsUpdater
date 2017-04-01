package ru.p3tr0vich.mwmmapsupdater.models;

import android.support.annotation.NonNull;

import ru.p3tr0vich.mwmmapsupdater.Consts;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

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

//        return mMapName.equals(fileInfo.mMapName) && mTimestamp == fileInfo.mTimestamp;

        if (!mMapName.equals(fileInfo.mMapName)) return false;

        if (mTimestamp == fileInfo.mTimestamp) return true;

        // TODO: 01.04.2017 bug?

        if (Math.abs(mTimestamp - fileInfo.mTimestamp) > 1000) {
            return false;
        } else {
            UtilsLog.d(true, "FileInfo", "mMapName == " + mMapName,
                    "this.mTimestamp == " + mTimestamp + ", o.mTimestamp == " + fileInfo.mTimestamp +
                            ", abs(diff) == " + Math.abs(mTimestamp - fileInfo.mTimestamp));

            return true;
        }
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