package ru.p3tr0vich.mwmmapsupdater;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import ru.p3tr0vich.mwmmapsupdater.broadcastreceivers.BroadcastReceiverMapFilesLoading;
import ru.p3tr0vich.mwmmapsupdater.helpers.MapFilesHelper;
import ru.p3tr0vich.mwmmapsupdater.models.MapFiles;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class MapFilesLoader extends AsyncTaskLoader<MapFiles> {

    private static final String TAG = "MapFilesLoader";

    private static final boolean LOG_ENABLED = false;

    private final String mMapDir;

    public MapFilesLoader(Context context, @NonNull String mapDir) {
        super(context);
        mMapDir = mapDir;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
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
}