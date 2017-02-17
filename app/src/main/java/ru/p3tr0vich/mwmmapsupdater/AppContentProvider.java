package ru.p3tr0vich.mwmmapsupdater;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.p3tr0vich.mwmmapsupdater.helpers.PreferencesHelper;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class AppContentProvider extends ContentProvider {

    private static final String TAG = "AppContentProvider";

    private static final boolean LOG_ENABLED = true;

    private static class BaseUri {

        private static final String SCHEME = ContentResolver.SCHEME_CONTENT;
        private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

        private BaseUri() {
        }

        public static Uri getUri(String path) {
            return new Uri.Builder()
                    .scheme(SCHEME)
                    .authority(AUTHORITY)
                    .path(path)
                    .build();
        }
    }

    private static class UriPath {
        private static final String SYNC_PROGRESS = "sync_progress";
        private static final String SYNC_PROGRESS_DATE_CHECKED = SYNC_PROGRESS + "/date_checked";
        private static final String SYNC_PROGRESS_DATE_CHECKED_ITEM = SYNC_PROGRESS_DATE_CHECKED + "/*";

        private static final String PREFERENCES = "preferences";
        private static final String PREFERENCES_ITEM = PREFERENCES + "/*";
    }

    public static final Uri URI_SYNC_PROGRESS_DATE_CHECKED = BaseUri.getUri(UriPath.SYNC_PROGRESS_DATE_CHECKED);

    public static final Uri URI_PREFERENCES = BaseUri.getUri(UriPath.PREFERENCES);

    public static final int SYNC_PROGRESS_DATE_CHECKED_ITEM = 20;

    private static final int PREFERENCES = 30;
    private static final int PREFERENCES_ITEM = 31;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(BaseUri.AUTHORITY, UriPath.SYNC_PROGRESS_DATE_CHECKED_ITEM, SYNC_PROGRESS_DATE_CHECKED_ITEM);

        sURIMatcher.addURI(BaseUri.AUTHORITY, UriPath.PREFERENCES, PREFERENCES);
        sURIMatcher.addURI(BaseUri.AUTHORITY, UriPath.PREFERENCES_ITEM, PREFERENCES_ITEM);
    }

    private static final String CURSOR_DIR_BASE_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/vnd." + BaseUri.AUTHORITY + ".";
    private static final String CURSOR_ITEM_BASE_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd." + BaseUri.AUTHORITY + ".";

    private static final String CURSOR_DIR_BASE_TYPE_PREFERENCES =
            CURSOR_DIR_BASE_TYPE + UriPath.PREFERENCES;
    private static final String CURSOR_ITEM_BASE_TYPE_PREFERENCES =
            CURSOR_ITEM_BASE_TYPE + UriPath.PREFERENCES;

    private PreferencesHelper mPreferencesHelper;

    @Override
    public boolean onCreate() {
        if (getContext() == null) {
            UtilsLog.e(TAG, "onCreate", "Context == null");
        }

        assert getContext() != null;

        mPreferencesHelper = PreferencesHelper.getInstance(getContext());

        return true;
    }

    public static int uriMatch(@NonNull Uri uri) {
        return sURIMatcher.match(uri);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatch(uri)) {
            case PREFERENCES:
                return CURSOR_DIR_BASE_TYPE_PREFERENCES;
            case PREFERENCES_ITEM:
                return CURSOR_ITEM_BASE_TYPE_PREFERENCES;
            default:
                UtilsLog.d(LOG_ENABLED, TAG, "getType", "sURIMatcher.match() == default, uri == " + uri);
                return null;
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        try {
            switch (uriMatch(uri)) {
                case PREFERENCES:
                    return mPreferencesHelper.getPreferences();
                case PREFERENCES_ITEM:
                    return mPreferencesHelper.getPreference(uri.getLastPathSegment());
                default:
                    UtilsLog.d(LOG_ENABLED, TAG, "query", "sURIMatcher.match() == default, uri == " + uri);
                    return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            UtilsLog.e(TAG, "query", "exception == " + e.toString());
            return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        try {
            switch (uriMatch(uri)) {
                case PREFERENCES:
                    return mPreferencesHelper.setPreferences(values, null);
                case PREFERENCES_ITEM:
                    return mPreferencesHelper.setPreferences(values, uri.getLastPathSegment());
                default:
                    UtilsLog.d(LOG_ENABLED, TAG, "update", "sURIMatcher.match() == default, uri == " + uri);
                    return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            UtilsLog.e(TAG, "update", "exception == " + e.toString());
            return -1;
        }
    }
}