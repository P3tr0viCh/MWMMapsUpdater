package ru.p3tr0vich.mwmmapsupdater.models;

import java.util.Date;

public class MapVersion {

    private Date mDateLocal;
    private Date mDateServer;

    public MapVersion() {
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
        return "local date: " + mDateLocal + ", server date: " + mDateServer;
    }
}