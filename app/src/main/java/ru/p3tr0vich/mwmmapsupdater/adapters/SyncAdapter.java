package ru.p3tr0vich.mwmmapsupdater.adapters;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.nfc.FormatException;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ru.p3tr0vich.mwmmapsupdater.AppContentProvider;
import ru.p3tr0vich.mwmmapsupdater.BuildConfig;
import ru.p3tr0vich.mwmmapsupdater.helpers.MapFilesLocalHelper;
import ru.p3tr0vich.mwmmapsupdater.helpers.MapFilesServerHelper;
import ru.p3tr0vich.mwmmapsupdater.helpers.ProviderPreferencesHelper;
import ru.p3tr0vich.mwmmapsupdater.models.MapFiles;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SyncAdapter";

    private static final boolean LOG_ENABLED = true;

    private static final boolean WAIT_ENABLED = false;

    public SyncAdapter(Context context) {
        super(context, true);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "start");

        ProviderPreferencesHelper providerPreferencesHelper = new ProviderPreferencesHelper(getContext(), provider);

        try {
            try {
                if (BuildConfig.DEBUG && WAIT_ENABLED) {
                    for (int i = 0, waitSeconds = 5; i < waitSeconds; i++) {
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        UtilsLog.d(LOG_ENABLED, TAG, "onPerformSync", "wait... " + (waitSeconds - i));
                    }
                }

                providerPreferencesHelper.putCheckServerDateTime(System.currentTimeMillis());

                String mapDirName = providerPreferencesHelper.getMapsDir();

                Date date = getMapsVersion(mapDirName);

                Uri uri = Uri.withAppendedPath(AppContentProvider.URI_SYNC_PROGRESS_DATE_CHECKED,
                        date != null ? String.valueOf(date.getTime()) : "");

                getContext().getContentResolver().notifyChange(uri, null, false);

                providerPreferencesHelper.putDateServer(date != null ? date.getTime() : 0);

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

        UtilsLog.e(TAG, "handleException", "error  == " + e.toString());
    }

    @Nullable
    private Date getMapsVersion(@NonNull String mapDirName) {
        MapFiles mapFiles = MapFilesLocalHelper.find(mapDirName);

        if (mapFiles.getResult() == MapFiles.RESULT_OK) {
            List<String> fileList = mapFiles.getFileList();

            if (fileList != null) {
                Date date = MapFilesServerHelper.getVersion(fileList);

                UtilsLog.d(LOG_ENABLED, TAG, "getMapsVersion", "date == " + date);

                return date;
            }
        }

        UtilsLog.e(TAG, "getMapsVersion", "date  == null");

        return null;
    }
}