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

        switch (AppContentProvider.uriMatch(uri)) {
            case AppContentProvider.UriMatchResult.SYNC_PROGRESS_CHECK_SERVER_TIMESTAMP_ITEM:
                lastPath = uri.getLastPathSegment();

                UtilsLog.d(LOG_ENABLED, TAG, "onChange", "uriMatch == SYNC_PROGRESS_CHECK_SERVER_TIMESTAMP_ITEM, lastPath == " + lastPath);

                long checkServerTimestamp;
                if (TextUtils.isEmpty(lastPath)) {
                    checkServerTimestamp = Consts.BAD_DATETIME;
                } else {
                    checkServerTimestamp = Long.parseLong(lastPath);
                }

                onCheckServerTimestamp(checkServerTimestamp);

                break;
            case AppContentProvider.UriMatchResult.SYNC_PROGRESS_SERVER_MAPS_CHECKED_ITEM:
                lastPath = uri.getLastPathSegment();

                UtilsLog.d(LOG_ENABLED, TAG, "onChange", "uriMatch == SYNC_PROGRESS_SERVER_MAPS_CHECKED_ITEM, lastPath == " + lastPath);

                long serverMapsTimestamp;
                if (TextUtils.isEmpty(lastPath)) {
                    serverMapsTimestamp = Consts.BAD_DATETIME;
                } else {
                    serverMapsTimestamp = Long.parseLong(lastPath);
                }

                onServerMapsChecked(serverMapsTimestamp);

                break;
        }
    }

    public abstract void onCheckServerTimestamp(long timestamp);

    public abstract void onServerMapsChecked(long timestamp);

    public static void notifyCheckServerTimestamp(@NonNull Context context, long timestamp) {
        Uri uri = Uri.withAppendedPath(AppContentProvider.UriList.SYNC_PROGRESS_CHECK_SERVER_TIMESTAMP,
                String.valueOf(timestamp));

        notifyChange(context, uri);
    }

    public static void notifyServerMapsChecked(@NonNull Context context, long timestamp) {
        Uri uri = Uri.withAppendedPath(AppContentProvider.UriList.SYNC_PROGRESS_SERVER_MAPS_CHECKED,
                String.valueOf(timestamp));

        notifyChange(context, uri);
    }
}