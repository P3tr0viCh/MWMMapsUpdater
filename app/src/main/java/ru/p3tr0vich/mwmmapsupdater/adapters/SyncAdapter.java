package ru.p3tr0vich.mwmmapsupdater.adapters;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.nfc.FormatException;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ru.p3tr0vich.mwmmapsupdater.BuildConfig;
import ru.p3tr0vich.mwmmapsupdater.Consts;
import ru.p3tr0vich.mwmmapsupdater.helpers.MapFilesHelper;
import ru.p3tr0vich.mwmmapsupdater.helpers.MapFilesServerHelper;
import ru.p3tr0vich.mwmmapsupdater.helpers.ProviderPreferencesHelper;
import ru.p3tr0vich.mwmmapsupdater.models.FileInfo;
import ru.p3tr0vich.mwmmapsupdater.models.MapFiles;
import ru.p3tr0vich.mwmmapsupdater.observers.SyncProgressObserver;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SyncAdapter";

    private static final boolean LOG_ENABLED = true;

    private static final boolean DEBUG_WAIT_ENABLED = false;
    private static final boolean DEBUG_ALWAYS_HAS_UPDATES = true;

    public SyncAdapter(Context context) {
        super(context, true);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "start");

        ProviderPreferencesHelper providerPreferencesHelper = new ProviderPreferencesHelper(getContext(), provider);

        try {
            try {
                if (BuildConfig.DEBUG && DEBUG_WAIT_ENABLED) {
                    for (int i = 0, waitSeconds = 5; i < waitSeconds; i++) {
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        UtilsLog.d(true, TAG, "onPerformSync", "wait... " + (waitSeconds - i));
                    }
                }

                String parentMapsDir = providerPreferencesHelper.getParentMapsDir();

                MapFiles mapFiles = MapFilesHelper.find(getContext(), parentMapsDir);

                final long localMapsTimestamp = mapFiles.getTimestamp();

                final long serverMapsTimestamp = getServerMapsTimestamp(mapFiles);

                final long currentTimeMillis = System.currentTimeMillis();

                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync",
                        "local maps date == " +
                                (localMapsTimestamp != Consts.BAD_DATETIME ?
                                        UtilsLog.DATETIME_FORMAT.format(localMapsTimestamp) :
                                        "BAD_DATETIME") +
                                ", server maps date == " +
                                (serverMapsTimestamp != Consts.BAD_DATETIME ?
                                        UtilsLog.DATETIME_FORMAT.format(serverMapsTimestamp) : "BAD_DATETIME") +
                                ", current date == " +
                                UtilsLog.DATETIME_FORMAT.format(currentTimeMillis));

                providerPreferencesHelper.putCheckServerDateTime(currentTimeMillis);
                SyncProgressObserver.notifyCheckServerDateTime(getContext(), currentTimeMillis);

                providerPreferencesHelper.putDateServer(serverMapsTimestamp);
                SyncProgressObserver.notifyDateChecked(getContext(), serverMapsTimestamp);

                if (localMapsTimestamp == Consts.BAD_DATETIME) {
                    throw new IOException("localMapsTimestamp == BAD_DATETIME");
                }
                if (serverMapsTimestamp == Consts.BAD_DATETIME) {
                    throw new IOException("serverMapsTimestamp == BAD_DATETIME");
                }

                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync",
                        "serverMapsTimestamp > localMapsTimestamp == " + (serverMapsTimestamp > localMapsTimestamp));

                if ((serverMapsTimestamp > localMapsTimestamp) || (BuildConfig.DEBUG && DEBUG_ALWAYS_HAS_UPDATES)) {
                    // TODO: 22.02.2017
                    UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "has updates");
                }
            } catch (Exception e) {
                handleException(e, syncResult);
            }
        } finally {
            UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "finish" +
                    (syncResult.hasError() ? ", errors == " + syncResult.toString() : ", all ok"));

            if (extras.getBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, false)) {
                syncResult.clear();
            }
        }
    }

    private void handleException(Exception e, SyncResult syncResult) {
        if (e instanceof RemoteException) syncResult.databaseError = true;
        else if (e instanceof IOException) syncResult.stats.numIoExceptions++;
        else if (e instanceof FormatException) syncResult.stats.numParseExceptions++;
        else syncResult.databaseError = true;

        UtilsLog.e(TAG, "handleException", e);
    }

    private long getServerMapsTimestamp(@NonNull MapFiles mapFiles) throws IOException {
        List<FileInfo> fileInfoList = mapFiles.getFileList();

        if (fileInfoList.isEmpty()) {
            UtilsLog.e(TAG, "getServerMapsTimestamp", "fileInfoList empty");

            return Consts.BAD_DATETIME;
        }

        List<String> mapNames = new ArrayList<>();

        for (FileInfo fileInfo : fileInfoList) {
            mapNames.add(fileInfo.getMapName());
        }

        return MapFilesServerHelper.getTimestamp(mapNames);
    }
}