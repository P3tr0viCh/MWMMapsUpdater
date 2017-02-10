package ru.p3tr0vich.mwmmapsupdater.broadcastreceivers;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import ru.p3tr0vich.mwmmapsupdater.BuildConfig;

public abstract class BroadcastReceiverMapFilesLoading extends BroadcastReceiverLocalBase {

    private static final String ACTION = BuildConfig.APPLICATION_ID + ".ACTION_MAP_FILES_LOADING";
    private static final String EXTRA_LOADING = BuildConfig.APPLICATION_ID + ".EXTRA_MAP_FILES_LOADING";

    @Override
    protected final String getAction() {
        return ACTION;
    }

    public static void send(@NonNull Context context, boolean loading) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION)
                .putExtra(EXTRA_LOADING, loading));
    }

    @Override
    public final void onReceive(Context context, Intent intent) {
        onReceive(intent.getBooleanExtra(EXTRA_LOADING, false));
    }

    public abstract void onReceive(boolean loading);
}