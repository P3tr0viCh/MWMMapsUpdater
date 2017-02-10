package ru.p3tr0vich.mwmmapsupdater;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import ru.p3tr0vich.mwmmapsupdater.broadcastreceivers.BroadcastReceiverMapFilesLoading;
import ru.p3tr0vich.mwmmapsupdater.helpers.MapFilesFindHelper;
import ru.p3tr0vich.mwmmapsupdater.models.MapFiles;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class MapFilesLoader extends AsyncTaskLoader<MapFiles> {

    private static final String TAG = "MapFilesLoader";

    private static final boolean LOG_ENABLED = true;

    private String mMapDir;

    public MapFilesLoader(Context context, @NonNull String mapDir) {
        super(context);
        mMapDir = mapDir;

        if (LOG_ENABLED) {
            UtilsLog.d(TAG, "constructor");
        }
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    @Nullable
    public MapFiles loadInBackground() {
        if (LOG_ENABLED) {
            UtilsLog.d(TAG, "loadInBackground");
        }

        BroadcastReceiverMapFilesLoading.send(getContext(), true);

        try {
            return MapFilesFindHelper.find(mMapDir);
        } finally {
            BroadcastReceiverMapFilesLoading.send(getContext(), false);
        }
    }
}