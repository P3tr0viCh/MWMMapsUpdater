package ru.p3tr0vich.mwmmapsupdater.Models;

import java.util.ArrayList;
import java.util.List;

public class MapFiles {

    private String mMapDir = null;
    private String mMapSubDir = null;

    private List<String> mFileList;

    public MapFiles(String mapDir) {
        mMapDir = mapDir;

        mFileList = new ArrayList<>();
    }

    public String getMapDir() {
        return mMapDir;
    }

    public String getMapSubDir() {
        return mMapSubDir;
    }

    public List<String> getFileList() {
        return mFileList;
    }
}