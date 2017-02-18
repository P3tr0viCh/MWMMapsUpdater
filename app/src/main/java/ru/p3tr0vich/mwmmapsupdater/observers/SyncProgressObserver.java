package ru.p3tr0vich.mwmmapsupdater.observers;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Date;

import ru.p3tr0vich.mwmmapsupdater.AppContentProvider;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

import static ru.p3tr0vich.mwmmapsupdater.helpers.PreferencesHelper.BAD_DATETIME;

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
            case AppContentProvider.UriMatchResult.SYNC_PROGRESS_CHECK_SERVER_DATETIME_ITEM:
                lastPath = uri.getLastPathSegment();

                UtilsLog.d(LOG_ENABLED, TAG, "onChange", "uriMatch == SYNC_PROGRESS_CHECK_SERVER_DATETIME_ITEM, lastPath == " + lastPath);

                long checkServerDateTime;
                if (TextUtils.isEmpty(lastPath)) {
                    checkServerDateTime = BAD_DATETIME;
                } else {
                    checkServerDateTime = Long.parseLong(lastPath);
                }

                onCheckServerDateTime(checkServerDateTime);

                break;
            case AppContentProvider.UriMatchResult.SYNC_PROGRESS_DATE_CHECKED_ITEM:
                lastPath = uri.getLastPathSegment();

                UtilsLog.d(LOG_ENABLED, TAG, "onChange", "uriMatch == SYNC_PROGRESS_DATE_CHECKED_ITEM, lastPath == " + lastPath);

                Date date;
                if (TextUtils.isEmpty(lastPath)) {
                    date = null;
                } else {
                    date = new Date(Long.parseLong(lastPath));
                }

                onDateChecked(date);

                break;
        }
    }

    public abstract void onCheckServerDateTime(long dateTime);

    public abstract void onDateChecked(@Nullable Date date);

    public static void notifyCheckServerDateTime(@NonNull Context context, long date) {
        Uri uri = Uri.withAppendedPath(AppContentProvider.UriList.SYNC_PROGRESS_CHECK_SERVER_DATETIME,
                String.valueOf(date));

        notifyChange(context, uri);
    }

    public static void notifyDateChecked(@NonNull Context context, @Nullable Date date) {
        Uri uri = Uri.withAppendedPath(AppContentProvider.UriList.SYNC_PROGRESS_DATE_CHECKED,
                date != null ? String.valueOf(date.getTime()) : "");

        notifyChange(context, uri);
    }
}