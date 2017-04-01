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
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

import ru.p3tr0vich.mwmmapsupdater.BuildConfig;
import ru.p3tr0vich.mwmmapsupdater.Consts;
import ru.p3tr0vich.mwmmapsupdater.exceptions.CancelledException;
import ru.p3tr0vich.mwmmapsupdater.exceptions.InternetException;
import ru.p3tr0vich.mwmmapsupdater.helpers.ConnectivityHelper;
import ru.p3tr0vich.mwmmapsupdater.helpers.ContentResolverHelper;
import ru.p3tr0vich.mwmmapsupdater.helpers.MapFilesHelper;
import ru.p3tr0vich.mwmmapsupdater.helpers.MapFilesServerHelper;
import ru.p3tr0vich.mwmmapsupdater.helpers.NotificationHelper;
import ru.p3tr0vich.mwmmapsupdater.helpers.PreferencesHelper;
import ru.p3tr0vich.mwmmapsupdater.helpers.ProviderPreferencesHelper;
import ru.p3tr0vich.mwmmapsupdater.models.FileInfo;
import ru.p3tr0vich.mwmmapsupdater.models.MapFiles;
import ru.p3tr0vich.mwmmapsupdater.observers.SyncProgressObserver;
import ru.p3tr0vich.mwmmapsupdater.utils.Utils;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsFiles;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class SyncAdapter extends AbstractThreadedSyncAdapter implements MapFilesServerHelper.OnCancelled {

    private static final String TAG = "SyncAdapter";

    private static final boolean LOG_ENABLED = true;

    private static final boolean DEBUG_WAIT_ENABLED = false;
    private static final boolean DEBUG_ALWAYS_HAS_UPDATES = true;
//    private static final boolean DEBUG_NOT_CHECK_SAVED_TIMESTAMP = true;

    private ProviderPreferencesHelper mProviderPreferencesHelper;

    private NotificationHelper mNotificationHelper;

    private MapFiles mMapFiles;

    private boolean mSyncCancelled;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SYNC_ERROR_OTHER, SYNC_ERROR_CANCELLED, SYNC_ERROR_INTERNET})
    public @interface SyncError {
    }

    public static final int SYNC_ERROR_OTHER = 0;
    public static final int SYNC_ERROR_CANCELLED = 1;
    public static final int SYNC_ERROR_INTERNET = 2;

    public SyncAdapter(Context context) {
        super(context, true);
    }

    @ConnectivityHelper.ConnectedState
    private int updateConnectedState() throws InternetException {
        int connectedState = ConnectivityHelper.getConnectedState(getContext());

        UtilsLog.d(LOG_ENABLED, TAG, "updateConnectedState", "mConnectedState == " +
                (connectedState == ConnectivityHelper.CONNECTED ? "CONNECTED" :
                        connectedState == ConnectivityHelper.CONNECTED_WIFI ? "CONNECTED_WIFI" :
                                connectedState == ConnectivityHelper.CONNECTED_ROAMING ? "CONNECTED_ROAMING" :
                                        connectedState == ConnectivityHelper.DISCONNECTED ? "DISCONNECTED" : "?"));

        if (connectedState == ConnectivityHelper.CONNECTED_ROAMING) {
            throw new InternetException("connectedState == CONNECTED_ROAMING");
        } else {
            if (connectedState == ConnectivityHelper.DISCONNECTED) {
                throw new InternetException("connectedState == DISCONNECTED");
            }
        }

        return connectedState;
    }

    private static boolean isManualStart(Bundle extras) {
        return extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false) &&
                extras.getBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, false);
    }

    @ContentResolverHelper.RequestSync
    private static int getManualStartRequest(Bundle extras) {
        return extras.getInt(ContentResolverHelper.SYNC_EXTRAS_REQUEST, ContentResolverHelper.REQUEST_SYNC_CHECK_LOCAL_FILES);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "start");

        mSyncCancelled = false;

        final boolean manualStart = isManualStart(extras);

        UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "manualStart == " + manualStart);

        init(provider);

        try {
            mNotificationHelper.setDefaults(
                    mProviderPreferencesHelper.isNotificationUseDefaultSound(),
                    mProviderPreferencesHelper.isNotificationUseDefaultVibrate(),
                    mProviderPreferencesHelper.isNotificationUseDefaultLights());

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

            mMapFiles = MapFilesHelper.find(mProviderPreferencesHelper.getParentMapsDir());

            final long localMapsTimestamp = getLocalMapsTimestamp();

            final long serverMapsTimestamp;

            if (manualStart) {
                mNotificationHelper.cancel();

                switch (getManualStartRequest(extras)) {
                    case ContentResolverHelper.REQUEST_SYNC_DOWNLOAD:
                        UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "manualStartRequest == REQUEST_SYNC_DOWNLOAD");

                        serverMapsTimestamp = getServerMapsTimestamp();

                        download();

                        mNotificationHelper.notifyDownloadEnd(serverMapsTimestamp);

                        break;
                    case ContentResolverHelper.REQUEST_SYNC_INSTALL:
                        UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "manualStartRequest == REQUEST_SYNC_INSTALL");

                        serverMapsTimestamp = getServerMapsTimestamp();

                        if (needDownload()) {
                            download();
                        }

                        install(serverMapsTimestamp);

                        mNotificationHelper.notifyInstallEnd(serverMapsTimestamp);

                        break;
                    case ContentResolverHelper.REQUEST_SYNC_CHECK_LOCAL_FILES:
                        UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "manualStartRequest == REQUEST_SYNC_CHECK_LOCAL_FILES");

                        break;
                    case ContentResolverHelper.REQUEST_SYNC_CHECK_SERVER:
                        UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "manualStartRequest == REQUEST_SYNC_CHECK_SERVER");

                        getServerMapsTimestamp();

                        break;
                }
            } else {
//                final long savedServerMapsTimestamp = getSavedServerMapsTimestamp();

                serverMapsTimestamp = getServerMapsTimestamp();

                //noinspection ConstantConditions
                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync",
                        "serverMapsTimestamp > localMapsTimestamp == " + (serverMapsTimestamp > localMapsTimestamp) +
                                ((BuildConfig.DEBUG && DEBUG_ALWAYS_HAS_UPDATES) ? " (ignored)" : ""));
//                //noinspection ConstantConditions
//                UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync",
//                        "serverMapsTimestamp != savedServerMapsTimestamp == " + (serverMapsTimestamp != savedServerMapsTimestamp) +
//                                ((BuildConfig.DEBUG && DEBUG_NOT_CHECK_SAVED_TIMESTAMP) ? " (ignored)" : ""));

                if ((serverMapsTimestamp > localMapsTimestamp) || (BuildConfig.DEBUG && DEBUG_ALWAYS_HAS_UPDATES)) {
                    UtilsLog.d(LOG_ENABLED, TAG, "autoSync", "has updates");

                    switch (mProviderPreferencesHelper.getActionOnHasUpdates()) {
                        case PreferencesHelper.ACTION_ON_HAS_UPDATES_DO_SHOW_NOTIFICATION:
                            UtilsLog.d(LOG_ENABLED, TAG, "autoSync", "actionOnHasUpdates == show notification");

                            mNotificationHelper.notifyHasUpdates(serverMapsTimestamp);

                            break;
                        case PreferencesHelper.ACTION_ON_HAS_UPDATES_DO_DOWNLOAD:
                            UtilsLog.d(LOG_ENABLED, TAG, "autoSync", "actionOnHasUpdates == download");

                            if (checkWifi(connectedState)) {
                                download();

                                mNotificationHelper.notifyDownloadEnd(serverMapsTimestamp);
                            } else {
                                UtilsLog.d(LOG_ENABLED, TAG, "autoSync", "download not started: wifi required");
                            }

                            break;
                        case PreferencesHelper.ACTION_ON_HAS_UPDATES_DO_INSTALL:
                            UtilsLog.d(LOG_ENABLED, TAG, "autoSync", "actionOnHasUpdates == download and install");

                            if (checkWifi(connectedState)) {
                                download();

                                install(serverMapsTimestamp);

                                mNotificationHelper.notifyInstallEnd(serverMapsTimestamp);
                            } else {
                                UtilsLog.d(LOG_ENABLED, TAG, "autoSync", "download and install not started: wifi required");
                            }

                            break;
                    }
                }
            }

            serverChecked();
        } catch (Exception e) {
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

    private void handleException(Exception e, SyncResult syncResult) {
        mNotificationHelper.cancel();

        @SyncError
        int error = SYNC_ERROR_OTHER;

        if (e instanceof RemoteException) {
            syncResult.databaseError = true;
        } else {
            if (e instanceof InternetException) {
                syncResult.stats.numIoExceptions++;
                error = SYNC_ERROR_INTERNET;
            } else {
                if (e instanceof IOException) {
                    syncResult.stats.numIoExceptions++;
                } else {
                    if (e instanceof FormatException) {
                        syncResult.stats.numParseExceptions++;
                    } else {
                        if (e instanceof CancelledException) {
                            syncResult.stats.numIoExceptions++;
                            error = SYNC_ERROR_CANCELLED;
                        } else {
                            if (e instanceof InterruptedException) {
                                syncResult.stats.numIoExceptions++;
                            } else {
                                syncResult.databaseError = true;
                            }
                        }
                    }
                }
            }
        }

        UtilsLog.e(TAG, "handleException", e);

        SyncProgressObserver.notifyErrorOccurred(getContext(), error);
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

    @Override
    public void onSyncCanceled() {
        super.onSyncCanceled();

        mSyncCancelled = true;

        UtilsLog.d(LOG_ENABLED, TAG, "onSyncCanceled");
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

    private long getServerMapsTimestamp() throws InternetException, RemoteException {
        long timestamp = MapFilesServerHelper.getServerFilesTimestamp(mMapFiles);

        UtilsLog.d(LOG_ENABLED, TAG, "getServerMapsTimestamp", "server maps date == " + UtilsLog.formatDate(timestamp));

        mProviderPreferencesHelper.putServerMapsTimestamp(timestamp);

        SyncProgressObserver.notifyServerMapsChecked(getContext(), timestamp);

        if (timestamp == Consts.BAD_DATETIME) {
            throw new InternetException("server maps timestamp == BAD_DATETIME");
        }

        return timestamp;
    }

    private long getSavedServerMapsTimestamp() throws RemoteException, FormatException {
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

    private boolean checkWifi(@ConnectivityHelper.ConnectedState int connectedState) throws RemoteException, FormatException {
        if (BuildConfig.DEBUG) {
            return true;
        }

        boolean downloadOnlyOnWifi = mProviderPreferencesHelper.isDownloadOnlyOnWifi();

        if (downloadOnlyOnWifi) {
            if (connectedState == ConnectivityHelper.CONNECTED_WIFI) {
                return true;
            } else {
                // TODO: 28.02.2017 write pref "check on wifi enabled" and start sync after wifi connected
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void checkCancelled() throws CancelledException {
        if (mSyncCancelled) {
            throw new CancelledException();
        }
    }

    private void download() throws IOException, CancelledException {
        MapFilesServerHelper.downloadMaps(mMapFiles, this,
                new MapFilesServerHelper.OnDownloadProgress() {

                    private final JSONObject namesAndDescriptions = Utils.getMapNamesAndDescriptions(getContext());

                    @Override
                    public void onStart() {
                        mNotificationHelper.notifyDownloadStart();
                    }

                    @Override
                    public void onMapStart(@NonNull String mapName, int i, int count) {
                        String name = mapName;

                        try {
                            name = namesAndDescriptions.getString(name);
                        } catch (JSONException e) {
                            UtilsLog.e(TAG, "download > OnDownloadProgress > onMapStart", e);
                        }

                        mNotificationHelper.notifyDownloadMapStart(name, i, count);
                    }

                    @Override
                    public void onProgress(int progress) {
                        mNotificationHelper.notifyDownloadProgress(progress);
                    }

                    @Override
                    public void onEnd() {
                        mNotificationHelper.cancel();
                    }
                });
    }

    private boolean needDownload() {
        File downloadDir = MapFilesHelper.getDownloadDir();

        if (!UtilsFiles.isDirExists(downloadDir)) {
            UtilsLog.d(LOG_ENABLED, TAG, "needDownload", "downloaded maps directory not exists or not directory");
            return true;
        }

        File[] listFiles = downloadDir.listFiles();

        if (listFiles == null || listFiles.length == 0) {
            UtilsLog.d(LOG_ENABLED, TAG, "needDownload", "downloaded maps directory empty");

            return true;
        }

        // TODO: 11.03.2017 check maps exists in download dir, not changed and has current version

        UtilsLog.d(LOG_ENABLED, TAG, "needDownload", "return false");

        return false;
    }

    private void install(long timestamp) throws RemoteException, FormatException, IOException, CancelledException {
        UtilsLog.d(LOG_ENABLED, TAG, "install", "start");

        if (mProviderPreferencesHelper.isSaveOriginalMaps()) {
            saveOriginalMaps();
        }

        moveDownloaded();

        MapFilesHelper.updateFileInfoList(mMapFiles);
        MapFilesHelper.writeToJSONFile(getContext(), mMapFiles);

        mProviderPreferencesHelper.putLocalMapsTimestamp(timestamp);

        SyncProgressObserver.notifyLocalMapsChecked(getContext(), timestamp);

        // TODO: 17.03.2017 update MAPS.ME edits.xml

        UtilsLog.d(LOG_ENABLED, TAG, "install", "end");
    }

    private void saveOriginalMaps() throws IOException, CancelledException {
        UtilsLog.d(LOG_ENABLED, TAG, "saveOriginalMaps", "start");

        File backupMapsDirParent = MapFilesHelper.getBackupMapsDir();

        File backupMapsDir = new File(backupMapsDirParent, mMapFiles.getMapSubDir());

        UtilsLog.d(LOG_ENABLED, TAG, "saveOriginalMaps", "backup dir == " + backupMapsDir.getAbsolutePath());

        UtilsFiles.makeDir(backupMapsDir);

        String mapFileName;

        File mapFileBackup;
        File mapFileOriginal;

        File mapDirOriginal = new File(mMapFiles.getMapDir(), mMapFiles.getMapSubDir());

        for (FileInfo fileInfo : mMapFiles.getFileList()) {
            checkCancelled();

            mapFileName = fileInfo.getMapName() + Consts.MAP_FILE_NAME_EXT;

            mapFileBackup = new File(backupMapsDir, mapFileName);

            if (!UtilsFiles.isFileExists(mapFileBackup)) {
                mapFileOriginal = new File(mapDirOriginal, mapFileName);

                UtilsFiles.rename(mapFileOriginal, mapFileBackup);

                UtilsLog.d(LOG_ENABLED, TAG, "saveOriginalMaps", "'" + mapFileOriginal.getName() + "' moved");
            }
        }

        UtilsLog.d(LOG_ENABLED, TAG, "saveOriginalMaps", "end");
    }

    private void moveDownloaded() throws CancelledException, IOException {
        UtilsLog.d(LOG_ENABLED, TAG, "moveDownloaded", "start");

        File mapDir = new File(mMapFiles.getMapDir(), mMapFiles.getMapSubDir());

        File downloadDir = MapFilesHelper.getDownloadDir();

        String mapFileName;

        File mapFile;
        File mapFileDownloaded;

        for (FileInfo fileInfo : mMapFiles.getFileList()) {
            checkCancelled();

            mapFileName = fileInfo.getMapName() + Consts.MAP_FILE_NAME_EXT;

            mapFile = new File(mapDir, mapFileName);
            mapFileDownloaded = new File(downloadDir, mapFileName);

            UtilsFiles.rename(mapFileDownloaded, mapFile);

            UtilsLog.d(LOG_ENABLED, TAG, "moveDownloaded", "'" + mapFileDownloaded.getName() + "' moved");
        }

        UtilsFiles.delete(downloadDir);

        UtilsLog.d(LOG_ENABLED, TAG, "moveDownloaded", "end");
    }
}