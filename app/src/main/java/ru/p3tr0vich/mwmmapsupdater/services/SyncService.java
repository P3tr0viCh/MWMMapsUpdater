package ru.p3tr0vich.mwmmapsupdater.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import ru.p3tr0vich.mwmmapsupdater.adapters.SyncAdapter;

public class SyncService extends Service {

    private static SyncAdapter sSyncAdapter = null;

    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext());
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}