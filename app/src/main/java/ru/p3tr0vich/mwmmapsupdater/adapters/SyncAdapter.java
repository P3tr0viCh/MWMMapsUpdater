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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import ru.p3tr0vich.mwmmapsupdater.BuildConfig;
import ru.p3tr0vich.mwmmapsupdater.Consts;
import ru.p3tr0vich.mwmmapsupdater.helpers.ConnectivityHelper;
import ru.p3tr0vich.mwmmapsupdater.helpers.MapFilesHelper;
import ru.p3tr0vich.mwmmapsupdater.helpers.MapFilesServerHelper;
import ru.p3tr0vich.mwmmapsupdater.helpers.NotificationHelper;
import ru.p3tr0vich.mwmmapsupdater.helpers.PreferencesHelper;
import ru.p3tr0vich.mwmmapsupdater.helpers.ProviderPreferencesHelper;
import ru.p3tr0vich.mwmmapsupdater.models.MapFiles;
import ru.p3tr0vich.mwmmapsupdater.observers.SyncProgressObserver;
import ru.p3tr0vich.mwmmapsupdater.utils.Utils;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SyncAdapter";

    private static final boolean LOG_ENABLED = true;

    private static final boolean DEBUG_WAIT_ENABLED = false;
    private static final boolean DEBUG_ALWAYS_HAS_UPDATES = true;
    private static final boolean DEBUG_NOT_CHECK_SAVED_TIMESTAMP = true;

    public SyncAdapter(Context context) {
        super(context, true);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "start");

        try {
            @ConnectivityHelper.ConnectedState
            final int connectedState = ConnectivityHelper.getConnectedState(getContext());

            switch (connectedState) {
                case ConnectivityHelper.CONNECTED_ROAMING:
                    UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "connectedState == CONNECTED_ROAMING");
                    break;
                case ConnectivityHelper.CONNECTED_WIFI:
                    UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "connectedState == CONNECTED_WIFI");
                    break;
                case ConnectivityHelper.CONNECTED:
                    UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "connectedState == CONNECTED");
                    break;
                case ConnectivityHelper.DISCONNECTED:
                    UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "connectedState == DISCONNECTED");
                    break;
            }

            if (connectedState == ConnectivityHelper.CONNECTED_ROAMING || connectedState == ConnectivityHelper.DISCONNECTED) {
                return;
            }

            final ProviderPreferencesHelper providerPreferencesHelper = new ProviderPreferencesHelper(getContext(), provider);

            final NotificationHelper notificationHelper = new NotificationHelper(getContext());

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

                final long serverMapsTimestamp = MapFilesServerHelper.getTimestamp(mapFiles);

                final long currentTimeMillis = System.currentTimeMillis();

                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "local maps date == " +
                        (localMapsTimestamp != Consts.BAD_DATETIME ?
                                UtilsLog.DATETIME_FORMAT.format(localMapsTimestamp) : "BAD_DATETIME"));
                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "server maps date == " +
                        (serverMapsTimestamp != Consts.BAD_DATETIME ?
                                UtilsLog.DATETIME_FORMAT.format(serverMapsTimestamp) : "BAD_DATETIME"));
                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "current date == " +
                        UtilsLog.DATETIME_FORMAT.format(currentTimeMillis));

                providerPreferencesHelper.putCheckServerTimestamp(currentTimeMillis);
                SyncProgressObserver.notifyCheckServerTimestamp(getContext(), currentTimeMillis);

                long savedServerMapsTimestamp = providerPreferencesHelper.getServerMapsTimestamp();

                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "saved server maps date == " +
                        UtilsLog.DATETIME_FORMAT.format(savedServerMapsTimestamp));

                providerPreferencesHelper.putServerMapsTimestamp(serverMapsTimestamp);
                SyncProgressObserver.notifyServerMapsChecked(getContext(), serverMapsTimestamp);

                if (localMapsTimestamp == Consts.BAD_DATETIME) {
                    throw new IOException("localMapsTimestamp == BAD_DATETIME");
                }
                if (serverMapsTimestamp == Consts.BAD_DATETIME) {
                    throw new IOException("serverMapsTimestamp == BAD_DATETIME");
                }

                //noinspection ConstantConditions
                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync",
                        "serverMapsTimestamp > localMapsTimestamp == " + (serverMapsTimestamp > localMapsTimestamp) +
                                ((BuildConfig.DEBUG && DEBUG_ALWAYS_HAS_UPDATES) ? " (ignored)" : ""));
                //noinspection ConstantConditions
                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync",
                        "serverMapsTimestamp != savedServerMapsTimestamp == " + (serverMapsTimestamp != savedServerMapsTimestamp) +
                                ((BuildConfig.DEBUG && DEBUG_NOT_CHECK_SAVED_TIMESTAMP) ? " (ignored)" : ""));

                if ((serverMapsTimestamp > localMapsTimestamp) || (BuildConfig.DEBUG && DEBUG_ALWAYS_HAS_UPDATES)) {
                    UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "has updates");

                    if ((serverMapsTimestamp != savedServerMapsTimestamp) || (BuildConfig.DEBUG && DEBUG_NOT_CHECK_SAVED_TIMESTAMP)) {
                        @PreferencesHelper.ActionOnHasUpdates
                        int actionOnHasUpdates = providerPreferencesHelper.getActionOnHasUpdates();

                        boolean downloadOnlyOnWifi = providerPreferencesHelper.isDownloadOnlyOnWifi();

                        switch (actionOnHasUpdates) {
                            case PreferencesHelper.ACTION_ON_HAS_UPDATES_DO_NOTHING:
                                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "actionOnHasUpdates == do nothing");

                                break;
                            case PreferencesHelper.ACTION_ON_HAS_UPDATES_SHOW_NOTIFICATION:
                                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "actionOnHasUpdates == show notification");

                                notificationHelper.notifyHasUpdates(serverMapsTimestamp);

                                break;
                            case PreferencesHelper.ACTION_ON_HAS_UPDATES_DOWNLOAD:
                                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "actionOnHasUpdates == download");

                                download(getContext(), connectedState, downloadOnlyOnWifi, mapFiles,
                                        notificationHelper, serverMapsTimestamp);

                                break;
                            case PreferencesHelper.ACTION_ON_HAS_UPDATES_INSTALL:
                                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "actionOnHasUpdates == download and install");

                                // TODO: 28.02.2017 download and copy to mwm dir

                                break;
                        }
                    }
                }
            } catch (Exception e) {
                notificationHelper.cancel();

                SyncProgressObserver.notifyErrorOccurred(getContext());

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

    private static void handleException(Exception e, SyncResult syncResult) {
        if (e instanceof RemoteException) syncResult.databaseError = true;
        else if (e instanceof IOException) syncResult.stats.numIoExceptions++;
        else if (e instanceof FormatException) syncResult.stats.numParseExceptions++;
        else syncResult.databaseError = true;

        UtilsLog.e(TAG, "handleException", e);
    }

    private static boolean download(@NonNull final Context context, int connectedState, boolean downloadOnlyOnWifi,
                                    @NonNull MapFiles mapFiles, @NonNull final NotificationHelper notificationHelper,
                                    final long serverMapsTimestamp) throws IOException {
        if (!BuildConfig.DEBUG) {
            if (downloadOnlyOnWifi && connectedState != ConnectivityHelper.CONNECTED_WIFI) {
                // TODO: 28.02.2017 write pref "check on wifi enabled" and start sync after wifi connected
                return false;
            }
        }

        MapFilesServerHelper.downloadMaps(mapFiles, new MapFilesServerHelper.OnDownloadProgress() {

            private final JSONObject namesAndDescriptions = Utils.getMapNamesAndDescriptions(context);

            @Override
            public void onStart() {
                notificationHelper.notifyDownloadStart();
            }

            @Override
            public void onMapStart(@NonNull String mapName) {
                String name = mapName;

                try {
                    name = namesAndDescriptions.getString(name);
                } catch (JSONException e) {
                    UtilsLog.e(TAG, "download > OnDownloadProgress > onMapStart", e);
                }

                notificationHelper.notifyDownloadMapStart(name);
            }

            @Override
            public void onProgress(int progress) {
                notificationHelper.notifyDownloadProgress(progress);
            }

            @Override
            public void onEnd() {
                notificationHelper.notifyDownloadEnd(serverMapsTimestamp);
            }
        });

        return true;
    }
}