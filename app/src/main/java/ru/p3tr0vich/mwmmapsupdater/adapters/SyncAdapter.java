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
import ru.p3tr0vich.mwmmapsupdater.helpers.ContentResolverHelper;
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

    private static final boolean LOG_ENABLED = false;

    private static final boolean DEBUG_WAIT_ENABLED = false;
    private static final boolean DEBUG_ALWAYS_HAS_UPDATES = true;
    private static final boolean DEBUG_NOT_CHECK_SAVED_TIMESTAMP = true;

    private ProviderPreferencesHelper mProviderPreferencesHelper;

    private NotificationHelper mNotificationHelper;

    private MapFiles mMapFiles;

    public SyncAdapter(Context context) {
        super(context, true);
    }

    @ConnectivityHelper.ConnectedState
    private int updateConnectedState() throws IOException {
        int connectedState = ConnectivityHelper.getConnectedState(getContext());

        UtilsLog.d(LOG_ENABLED, TAG, "updateConnectedState", "mConnectedState == " +
                (connectedState == ConnectivityHelper.CONNECTED ? "CONNECTED" :
                        connectedState == ConnectivityHelper.CONNECTED_WIFI ? "CONNECTED_WIFI" :
                                connectedState == ConnectivityHelper.CONNECTED_ROAMING ? "CONNECTED_ROAMING" :
                                        connectedState == ConnectivityHelper.DISCONNECTED ? "DISCONNECTED" : "?"));

        if (connectedState == ConnectivityHelper.CONNECTED_ROAMING) {
            throw new IOException("connectedState == CONNECTED_ROAMING");
        } else {
            if (connectedState == ConnectivityHelper.DISCONNECTED)
                throw new IOException("connectedState == DISCONNECTED");
        }

        return connectedState;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "start");

        boolean manualStart =
                extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false) &&
                        extras.getBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, false);

        UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "manualStart == " + manualStart);

        @ContentResolverHelper.RequestSync
        int manualRequest;
        switch (extras.getInt(ContentResolverHelper.SYNC_EXTRAS_REQUEST, ContentResolverHelper.REQUEST_SYNC_CHECK)) {
            case ContentResolverHelper.REQUEST_SYNC_DOWNLOAD:
                manualRequest = ContentResolverHelper.REQUEST_SYNC_DOWNLOAD;
                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "manualRequest == REQUEST_SYNC_DOWNLOAD");
                break;
            default:
            case ContentResolverHelper.REQUEST_SYNC_CHECK:
                manualRequest = ContentResolverHelper.REQUEST_SYNC_CHECK;
                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "manualRequest == REQUEST_SYNC_CHECK");
                break;
        }

        init(provider);

        try {
            int connectedState = updateConnectedState();

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

            String parentMapsDir = mProviderPreferencesHelper.getParentMapsDir();

            mMapFiles = MapFilesHelper.find(getContext(), parentMapsDir);

            final long localMapsTimestamp = mMapFiles.getLocalTimestamp();

            UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "local maps date == " + UtilsLog.formatDate(localMapsTimestamp));

            if (localMapsTimestamp == Consts.BAD_DATETIME) {
                throw new IOException("localMapsTimestamp == BAD_DATETIME");
            }

            final long savedServerMapsTimestamp = mMapFiles.getServerTimestamp();

            UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "saved server maps date == " + UtilsLog.formatDate(savedServerMapsTimestamp));

            MapFilesServerHelper.checkServerTimestamp(mMapFiles);

            final long serverMapsTimestamp = mMapFiles.getServerTimestamp();

            UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "server maps date == " + UtilsLog.formatDate(serverMapsTimestamp));

            SyncProgressObserver.notifyServerMapsChecked(getContext(), serverMapsTimestamp);

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

            if (manualStart) {
                manualSync(manualRequest);
            } else {
                autoSync(localMapsTimestamp, serverMapsTimestamp, savedServerMapsTimestamp, connectedState);
            }

            serverChecked();

            MapFilesHelper.writeToJSONFile(getContext(), mMapFiles);
        } catch (Exception e) {
            mNotificationHelper.cancel();

            SyncProgressObserver.notifyErrorOccurred(getContext());

            handleException(e, syncResult);
        } finally {
            UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "finish" +
                    (syncResult.hasError() ? ", errors == " + syncResult.toString() : ", all ok"));

            if (manualStart || BuildConfig.DEBUG) {
                syncResult.clear();
            }

            destroy();
        }
    }

    private static void handleException(Exception e, SyncResult syncResult) {
        if (e instanceof RemoteException) syncResult.databaseError = true;
        else if (e instanceof IOException) syncResult.stats.numIoExceptions++;
        else if (e instanceof FormatException) syncResult.stats.numParseExceptions++;
        else syncResult.databaseError = true;

        UtilsLog.e(TAG, "handleException", e);
    }

    private void init(ContentProviderClient provider) {
        mProviderPreferencesHelper = new ProviderPreferencesHelper(getContext(), provider);

        mNotificationHelper = new NotificationHelper(getContext());
    }

    private void destroy() {
        mProviderPreferencesHelper = null;

        mNotificationHelper = null;

        mMapFiles = null;
    }

    private void manualSync(@ContentResolverHelper.RequestSync int request) throws FormatException, RemoteException, IOException {
        if (request == ContentResolverHelper.REQUEST_SYNC_DOWNLOAD) {
            download();
        }
    }

    private void autoSync(long localMapsTimestamp, long serverMapsTimestamp, long savedServerMapsTimestamp,
                          @ConnectivityHelper.ConnectedState int connectedState) throws RemoteException, FormatException, IOException {
        if ((serverMapsTimestamp > localMapsTimestamp) || (BuildConfig.DEBUG && DEBUG_ALWAYS_HAS_UPDATES)) {
            UtilsLog.d(LOG_ENABLED, TAG, "autoSync", "has updates");

            if ((serverMapsTimestamp != savedServerMapsTimestamp) || (BuildConfig.DEBUG && DEBUG_NOT_CHECK_SAVED_TIMESTAMP)) {
                @PreferencesHelper.ActionOnHasUpdates
                int actionOnHasUpdates = mProviderPreferencesHelper.getActionOnHasUpdates();

                switch (actionOnHasUpdates) {
                    case PreferencesHelper.ACTION_ON_HAS_UPDATES_DO_NOTHING:
                        UtilsLog.d(LOG_ENABLED, TAG, "autoSync", "actionOnHasUpdates == do nothing");

                        break;
                    case PreferencesHelper.ACTION_ON_HAS_UPDATES_SHOW_NOTIFICATION:
                        UtilsLog.d(LOG_ENABLED, TAG, "autoSync", "actionOnHasUpdates == show notification");

                        mNotificationHelper.notifyHasUpdates(serverMapsTimestamp);

                        break;
                    case PreferencesHelper.ACTION_ON_HAS_UPDATES_DOWNLOAD:
                        UtilsLog.d(LOG_ENABLED, TAG, "autoSync", "actionOnHasUpdates == download");

                        checkWifi(connectedState);

                        download();

                        break;
                    case PreferencesHelper.ACTION_ON_HAS_UPDATES_INSTALL:
                        UtilsLog.d(LOG_ENABLED, TAG, "autoSync", "actionOnHasUpdates == download and install");

                        // TODO: 28.02.2017 download and copy to mwm dir

                        break;
                }
            }
        }
    }

    private void serverChecked() throws RemoteException {
        long currentTimeMillis = System.currentTimeMillis();

        UtilsLog.d(LOG_ENABLED, TAG, "serverChecked", "current date == " + UtilsLog.formatDate(currentTimeMillis));

        mMapFiles.setLastCheckTimestamp(currentTimeMillis);

        SyncProgressObserver.notifyCheckServerTimestamp(getContext(), currentTimeMillis);
    }

    private void checkWifi(@ConnectivityHelper.ConnectedState int connectedState) throws RemoteException, FormatException, IOException {
        if (!BuildConfig.DEBUG) {
            boolean downloadOnlyOnWifi = mProviderPreferencesHelper.isDownloadOnlyOnWifi();

            if (downloadOnlyOnWifi && connectedState != ConnectivityHelper.CONNECTED_WIFI) {
                // TODO: 28.02.2017 write pref "check on wifi enabled" and start sync after wifi connected

                throw new IOException("wifi required");
            }
        }
    }

    private void download() throws IOException, RemoteException, FormatException {
        MapFilesServerHelper.downloadMaps(mMapFiles, new MapFilesServerHelper.OnDownloadProgress() {

            private final JSONObject namesAndDescriptions = Utils.getMapNamesAndDescriptions(getContext());

            @Override
            public void onStart() {
                mNotificationHelper.notifyDownloadStart();
            }

            @Override
            public void onMapStart(@NonNull String mapName) {
                String name = mapName;

                try {
                    name = namesAndDescriptions.getString(name);
                } catch (JSONException e) {
                    UtilsLog.e(TAG, "download > OnDownloadProgress > onMapStart", e);
                }

                mNotificationHelper.notifyDownloadMapStart(name);
            }

            @Override
            public void onProgress(int progress) {
                mNotificationHelper.notifyDownloadProgress(progress);
            }

            @Override
            public void onEnd() {
                mNotificationHelper.notifyDownloadEnd(mMapFiles.getServerTimestamp());
            }
        });
   }
}