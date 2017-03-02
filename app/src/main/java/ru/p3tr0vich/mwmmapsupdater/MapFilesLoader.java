package ru.p3tr0vich.mwmmapsupdater;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import java.io.File;

import ru.p3tr0vich.mwmmapsupdater.broadcastreceivers.BroadcastReceiverMapFilesLoading;
import ru.p3tr0vich.mwmmapsupdater.helpers.MapFilesHelper;
import ru.p3tr0vich.mwmmapsupdater.models.MapFiles;
import ru.p3tr0vich.mwmmapsupdater.observers.MapFilesObserver;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class MapFilesLoader extends AsyncTaskLoader<MapFiles> {

    private static final String TAG = "MapFilesLoader";

    private static final boolean LOG_ENABLED = true;

    private final String mMapDir;

    private MapFiles mMapFiles;

    private MapFilesObserver mMapDirObserver;
    private MapFilesObserver mMapSubDirObserver;

    public MapFilesLoader(Context context, @NonNull String mapDir) {
        super(context);
        mMapDir = mapDir;
    }

    @Override
    @Nullable
    public MapFiles loadInBackground() {
        UtilsLog.d(LOG_ENABLED, TAG, "loadInBackground");

        BroadcastReceiverMapFilesLoading.send(getContext(), true);

        try {
            return MapFilesHelper.find(getContext(), mMapDir);
        } finally {
            BroadcastReceiverMapFilesLoading.send(getContext(), false);
        }
    }

    @Override
    public void deliverResult(MapFiles data) {
        UtilsLog.d(LOG_ENABLED, TAG, "deliverResult", "data == " + data);

        if (isReset()) {
            if (data != null) {
                onReleaseResources(data);
            }
        }

        MapFiles oldMapFiles = mMapFiles;

        mMapFiles = data;

        if (mMapDirObserver != null) {
            mMapDirObserver.stopWatching();
        }
        if (mMapSubDirObserver != null) {
            mMapSubDirObserver.stopWatching();
        }

        if (mMapFiles != null) {
            String mapDir = mMapFiles.getMapDir();

            if (!mapDir.isEmpty()) {
                mMapDirObserver = new MapFilesObserver(this, mapDir);
                mMapDirObserver.startWatching();

                String mapSubDir = mMapFiles.getMapSubDir();

                if (!mapSubDir.isEmpty()) {
                    mapSubDir = mapDir + File.separator + mapSubDir;

                    mMapSubDirObserver = new MapFilesObserver(this, mapSubDir);
                    mMapSubDirObserver.startWatching();
                }
            }
        }

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldMapFiles != null) {
            onReleaseResources(oldMapFiles);
        }
    }

    @Override
    protected void onStartLoading() {
        UtilsLog.d(LOG_ENABLED, TAG, "onStartLoading");

        if (mMapFiles != null) {
            deliverResult(mMapFiles);
        }

        if (takeContentChanged() || mMapFiles == null) {
            forceLoad();
        }
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
    }

    @Override
    protected void onStopLoading() {
        UtilsLog.d(LOG_ENABLED, TAG, "onStopLoading");
        cancelLoad();
    }

    @Override
    public void onCanceled(MapFiles data) {
        super.onCanceled(data);

        onReleaseResources(data);
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();

        if (mMapFiles != null) {
            onReleaseResources(mMapFiles);
            mMapFiles = null;
        }
    }

    protected void onReleaseResources(MapFiles data) {
        UtilsLog.d(LOG_ENABLED, TAG, "onReleaseResources", "data == " + data);
    }
}