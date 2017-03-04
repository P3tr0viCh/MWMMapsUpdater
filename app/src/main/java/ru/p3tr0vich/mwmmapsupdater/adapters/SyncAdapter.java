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

    private static final boolean LOG_ENABLED = true;

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
        switch (extras.getInt(ContentResolverHelper.SYNC_EXTRAS_REQUEST, ContentResolverHelper.REQUEST_SYNC_CHECK_LOCAL_FILES)) {
            case ContentResolverHelper.REQUEST_SYNC_CHECK_SERVER:
                manualRequest = ContentResolverHelper.REQUEST_SYNC_CHECK_SERVER;
                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "manualRequest == REQUEST_SYNC_CHECK_SERVER");
                break;
            case ContentResolverHelper.REQUEST_SYNC_DOWNLOAD:
                manualRequest = ContentResolverHelper.REQUEST_SYNC_DOWNLOAD;
                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "manualRequest == REQUEST_SYNC_DOWNLOAD");
                break;
            case ContentResolverHelper.REQUEST_SYNC_INSTALL:
                manualRequest = ContentResolverHelper.REQUEST_SYNC_INSTALL;
                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "manualRequest == REQUEST_SYNC_INSTALL");
                break;
            default:
            case ContentResolverHelper.REQUEST_SYNC_CHECK_LOCAL_FILES:
                manualRequest = ContentResolverHelper.REQUEST_SYNC_CHECK_LOCAL_FILES;
                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "manualRequest == REQUEST_SYNC_CHECK_LOCAL_FILES");
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

            mMapFiles = MapFilesHelper.find(parentMapsDir);

            final long localMapsTimestamp = getLocalMapsTimestamp();

            if (manualStart) {
                if (manualRequest == ContentResolverHelper.REQUEST_SYNC_CHECK_SERVER ||
                        manualRequest == ContentResolverHelper.REQUEST_SYNC_DOWNLOAD ||
                        manualRequest == ContentResolverHelper.REQUEST_SYNC_INSTALL) {

                    final long serverMapsTimestamp = getServerMapsTimestamp();

                    if (manualRequest == ContentResolverHelper.REQUEST_SYNC_DOWNLOAD ||
                            manualRequest == ContentResolverHelper.REQUEST_SYNC_INSTALL) {
                        download(serverMapsTimestamp);
                    }
                }
            } else {
                final long savedServerMapsTimestamp = getSavedServerMapsTimestamp();

                final long serverMapsTimestamp = getServerMapsTimestamp();

                //noinspection ConstantConditions
                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync",
                        "serverMapsTimestamp > localMapsTimestamp == " + (serverMapsTimestamp > localMapsTimestamp) +
                                ((BuildConfig.DEBUG && DEBUG_ALWAYS_HAS_UPDATES) ? " (ignored)" : ""));
                //noinspection ConstantConditions
                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync",
                        "serverMapsTimestamp != savedServerMapsTimestamp == " + (serverMapsTimestamp != savedServerMapsTimestamp) +
                                ((BuildConfig.DEBUG && DEBUG_NOT_CHECK_SAVED_TIMESTAMP) ? " (ignored)" : ""));

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

                                if (checkWifi(connectedState)) {
                                    download(serverMapsTimestamp);
                                }

                                break;
                            case PreferencesHelper.ACTION_ON_HAS_UPDATES_INSTALL:
                                UtilsLog.d(LOG_ENABLED, TAG, "autoSync", "actionOnHasUpdates == download and install");

                                // TODO: 28.02.2017 download and copy to mwm dir

                                break;
                        }
                    }
                }
            }

            serverChecked();
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

    private long getLocalMapsTimestamp() throws RemoteException, FormatException, IOException {
        long timestamp = Consts.BAD_DATETIME;

        if (!mMapFiles.getFileList().isEmpty()) {
            MapFiles savedMapFiles = new MapFiles();

            boolean filesEquals = MapFilesHelper.readFromJSONFile(getContext(), savedMapFiles);

            if (filesEquals) {
                filesEquals = mMapFiles.getMapSubDir().equals(savedMapFiles.getMapSubDir()) &&
                        mMapFiles.getFileList().equals(savedMapFiles.getFileList());
            }

            UtilsLog.d(LOG_ENABLED, TAG, "getLocalMapsTimestamp", "filesEquals == " + filesEquals);

            if (filesEquals) {
                timestamp = mProviderPreferencesHelper.getLocalMapsTimestamp();
            } else {
                MapFilesHelper.writeToJSONFile(getContext(), mMapFiles);
            }

            if (timestamp == Consts.BAD_DATETIME) {
                timestamp = MapFilesHelper.mapDirNameToTimestamp(mMapFiles.getMapSubDir());

                mProviderPreferencesHelper.putLocalMapsTimestamp(timestamp);
            }
        } else {
            MapFilesHelper.deleteJSONFile(getContext());

            mProviderPreferencesHelper.putLocalMapsTimestamp(timestamp);
        }

        UtilsLog.d(LOG_ENABLED, TAG, "getLocalMapsTimestamp", "local maps date == " + UtilsLog.formatDate(timestamp));

        SyncProgressObserver.notifyLocalMapsChecked(getContext(), timestamp);

        if (timestamp == Consts.BAD_DATETIME) {
            throw new IOException("local maps timestamp == BAD_DATETIME");
        }

        return timestamp;
    }

    private long getServerMapsTimestamp() throws IOException, RemoteException {
        long timestamp = MapFilesServerHelper.getServerFilesTimestamp(mMapFiles);

        UtilsLog.d(LOG_ENABLED, TAG, "getServerMapsTimestamp", "server maps date == " + UtilsLog.formatDate(timestamp));

        mProviderPreferencesHelper.putServerMapsTimestamp(timestamp);

        SyncProgressObserver.notifyServerMapsChecked(getContext(), timestamp);

        if (timestamp == Consts.BAD_DATETIME) {
            throw new IOException("server maps timestamp == BAD_DATETIME");
        }

        return timestamp;
    }

    private long getSavedServerMapsTimestamp() throws IOException, RemoteException, FormatException {
        long timestamp = mProviderPreferencesHelper.getServerMapsTimestamp();

        UtilsLog.d(LOG_ENABLED, TAG, "getSavedServerMapsTimestamp", "saved server maps date == " + UtilsLog.formatDate(timestamp));

        return timestamp;
    }

    private void serverChecked() throws RemoteException {
        long currentTimeMillis = System.currentTimeMillis();

        UtilsLog.d(LOG_ENABLED, TAG, "serverChecked", "current date == " + UtilsLog.formatDate(currentTimeMillis));

        mProviderPreferencesHelper.putCheckServerTimestamp(currentTimeMillis);

        SyncProgressObserver.notifyCheckServerTimestamp(getContext(), currentTimeMillis);
    }

    private boolean checkWifi(@ConnectivityHelper.ConnectedState int connectedState) throws RemoteException, FormatException, IOException {
        if (!BuildConfig.DEBUG) {
            boolean downloadOnlyOnWifi = mProviderPreferencesHelper.isDownloadOnlyOnWifi();

            if (downloadOnlyOnWifi && connectedState != ConnectivityHelper.CONNECTED_WIFI) {
                // TODO: 28.02.2017 write pref "check on wifi enabled" and start sync after wifi connected

                return false;
            }
        }

        return true;
    }

    private void download(final long serverFilesTimestamp) throws IOException, RemoteException, FormatException {
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
                mNotificationHelper.notifyDownloadEnd(serverFilesTimestamp);
            }
        });
    }
}