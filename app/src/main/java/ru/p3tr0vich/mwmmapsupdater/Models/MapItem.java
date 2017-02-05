package ru.p3tr0vich.mwmmapsupdater.Models;

import java.util.Date;

public class MapItem {

    private String mName;
    private Date mDateLocal;
    private Date mDateServer;

    public MapItem(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public Date getDateLocal() {
        return mDateLocal;
    }

    public void setDateLocal(Date dateLocal) {
        mDateLocal = dateLocal;
    }

    public Date getDateServer() {
        return mDateServer;
    }

    public void setDateServer(Date dateServer) {
        mDateServer = dateServer;
    }

    @Override
    public String toString() {
        return "name: " + mName + ", local date: " + mDateLocal + ", server date: " + mDateServer;
    }
}