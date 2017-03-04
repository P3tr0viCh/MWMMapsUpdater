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

        String lastPath;

        int uriMatch = AppContentProvider.uriMatch(uri);

        switch (uriMatch) {
            case AppContentProvider.UriMatchResult.SYNC_PROGRESS_LOCAL_MAPS_CHECKED_ITEM:
            case AppContentProvider.UriMatchResult.SYNC_PROGRESS_SERVER_MAPS_CHECKED_ITEM:
            case AppContentProvider.UriMatchResult.SYNC_PROGRESS_CHECK_SERVER_TIMESTAMP_ITEM:
                lastPath = uri.getLastPathSegment();

                if (LOG_ENABLED) {
                    String sUriMatch = null;

                    switch (uriMatch) {
                        case AppContentProvider.UriMatchResult.SYNC_PROGRESS_LOCAL_MAPS_CHECKED_ITEM:
                            sUriMatch = "SYNC_PROGRESS_LOCAL_MAPS_CHECKED_ITEM";
                        case AppContentProvider.UriMatchResult.SYNC_PROGRESS_SERVER_MAPS_CHECKED_ITEM:
                            sUriMatch = "SYNC_PROGRESS_SERVER_MAPS_CHECKED_ITEM";
                        case AppContentProvider.UriMatchResult.SYNC_PROGRESS_CHECK_SERVER_TIMESTAMP_ITEM:
                            sUriMatch = "SYNC_PROGRESS_CHECK_SERVER_TIMESTAMP_ITEM";
                    }

                    UtilsLog.d(true, TAG, "onChange", "uriMatch == " + sUriMatch + ", lastPath == " + lastPath);
                }

                long timestamp = TextUtils.isEmpty(lastPath) ? Consts.BAD_DATETIME : Long.parseLong(lastPath);

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
                }

                break;
            case AppContentProvider.UriMatchResult.SYNC_PROGRESS_ERROR_OCCURRED:
                onErrorOccurred();

                break;
        }
    }

    public abstract void onLocalMapsChecked(long timestamp);

    public abstract void onServerMapsChecked(long timestamp);

    public abstract void onCheckServerTimestamp(long timestamp);

    public abstract void onErrorOccurred();

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


    public static void notifyErrorOccurred(@NonNull Context context) {
        notifyChange(context, AppContentProvider.UriList.SYNC_PROGRESS_ERROR_OCCURRED);
    }
}