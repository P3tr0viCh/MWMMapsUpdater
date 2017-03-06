package ru.p3tr0vich.mwmmapsupdater.observers;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import ru.p3tr0vich.mwmmapsupdater.AppContentProvider;
import ru.p3tr0vich.mwmmapsupdater.Consts;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public abstract class SyncProgressObserver extends ContentObserverBase {

    private static final String TAG = "SyncProgressObserver";

    private static final boolean LOG_ENABLED = false;

    public void register(@NonNull Context context) {
        register(context, AppContentProvider.UriList.SYNC_PROGRESS, true);
    }

    @Override
    public final void onChange(boolean selfChange, Uri uri) {
        UtilsLog.d(LOG_ENABLED, TAG, "onChange", "selfChange == " + selfChange + ", uri == " + uri);

        if (uri == null) {
            return;
        }

        String lastPath = uri.getLastPathSegment();

        int uriMatch = AppContentProvider.uriMatch(uri);

        if (LOG_ENABLED) {
            String sUriMatch = null;

            switch (uriMatch) {
                case AppContentProvider.UriMatchResult.SYNC_PROGRESS_LOCAL_MAPS_CHECKED_ITEM:
                    sUriMatch = "SYNC_PROGRESS_LOCAL_MAPS_CHECKED_ITEM";
                case AppContentProvider.UriMatchResult.SYNC_PROGRESS_SERVER_MAPS_CHECKED_ITEM:
                    sUriMatch = "SYNC_PROGRESS_SERVER_MAPS_CHECKED_ITEM";
                case AppContentProvider.UriMatchResult.SYNC_PROGRESS_CHECK_SERVER_TIMESTAMP_ITEM:
                    sUriMatch = "SYNC_PROGRESS_CHECK_SERVER_TIMESTAMP_ITEM";
                case AppContentProvider.UriMatchResult.SYNC_PROGRESS_ERROR_OCCURRED_ITEM:
                    sUriMatch = "SYNC_PROGRESS_ERROR_OCCURRED_ITEM";
            }

            UtilsLog.d(true, TAG, "onChange", "uriMatch == " + sUriMatch + ", lastPath == " + lastPath);
        }

        long timestamp = Consts.BAD_DATETIME;
        int error = 0;
        switch (uriMatch) {
            case AppContentProvider.UriMatchResult.SYNC_PROGRESS_LOCAL_MAPS_CHECKED_ITEM:
            case AppContentProvider.UriMatchResult.SYNC_PROGRESS_SERVER_MAPS_CHECKED_ITEM:
            case AppContentProvider.UriMatchResult.SYNC_PROGRESS_CHECK_SERVER_TIMESTAMP_ITEM:
                if (!TextUtils.isEmpty(lastPath)) {
                    timestamp = Long.parseLong(lastPath);
                }
                break;
            case AppContentProvider.UriMatchResult.SYNC_PROGRESS_ERROR_OCCURRED_ITEM:
                if (!TextUtils.isEmpty(lastPath)) {
                    error = Integer.parseInt(lastPath);
                }
                break;
        }

        switch (uriMatch) {
            case AppContentProvider.UriMatchResult.SYNC_PROGRESS_LOCAL_MAPS_CHECKED_ITEM:
                onLocalMapsChecked(timestamp);
                break;
            case AppContentProvider.UriMatchResult.SYNC_PROGRESS_SERVER_MAPS_CHECKED_ITEM:
                onServerMapsChecked(timestamp);
                break;
            case AppContentProvider.UriMatchResult.SYNC_PROGRESS_CHECK_SERVER_TIMESTAMP_ITEM:
                onCheckServerTimestamp(timestamp);
                break;
            case AppContentProvider.UriMatchResult.SYNC_PROGRESS_ERROR_OCCURRED_ITEM:
                onErrorOccurred(error);
                break;
        }
    }

    public abstract void onLocalMapsChecked(long timestamp);

    public abstract void onServerMapsChecked(long timestamp);

    public abstract void onCheckServerTimestamp(long timestamp);

    public abstract void onErrorOccurred(int error);

    public static void notifyLocalMapsChecked(@NonNull Context context, long timestamp) {
        Uri uri = Uri.withAppendedPath(AppContentProvider.UriList.SYNC_PROGRESS_LOCAL_MAPS_CHECKED,
                String.valueOf(timestamp));

        notifyChange(context, uri);
    }

    public static void notifyServerMapsChecked(@NonNull Context context, long timestamp) {
        Uri uri = Uri.withAppendedPath(AppContentProvider.UriList.SYNC_PROGRESS_SERVER_MAPS_CHECKED,
                String.valueOf(timestamp));

        notifyChange(context, uri);
    }

    public static void notifyCheckServerTimestamp(@NonNull Context context, long timestamp) {
        Uri uri = Uri.withAppendedPath(AppContentProvider.UriList.SYNC_PROGRESS_CHECK_SERVER_TIMESTAMP,
                String.valueOf(timestamp));

        notifyChange(context, uri);
    }


    public static void notifyErrorOccurred(@NonNull Context context, int error) {
        Uri uri = Uri.withAppendedPath(AppContentProvider.UriList.SYNC_PROGRESS_ERROR_OCCURRED,
                String.valueOf(error));

        notifyChange(context, uri);
    }
}