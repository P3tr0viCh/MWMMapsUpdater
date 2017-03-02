package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.FormatException;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import ru.p3tr0vich.mwmmapsupdater.AppContentProvider;

public class ProviderPreferencesHelper {

    private static final String TAG = "ProviderPreferencesHelper";

    private final PreferencesHelper mPreferencesHelper;

    private final ContentProviderClient mProvider;

    public ProviderPreferencesHelper(Context context, ContentProviderClient provider) {
        mPreferencesHelper = PreferencesHelper.getInstance(context);
        mProvider = provider;
    }

    @NonNull
    private ContentValues query(@Nullable String preference) throws RemoteException, FormatException {
        final Cursor cursor = mProvider.query(
                TextUtils.isEmpty(preference) ?
                        AppContentProvider.UriList.PREFERENCES :
                        Uri.withAppendedPath(AppContentProvider.UriList.PREFERENCES, preference),
                null, null, null, null);

        if (cursor == null) {
            throw new FormatException(TAG + " -- query: cursor == null");
        } else if (cursor.getCount() == 0) {
            throw new FormatException(TAG + " -- query: cursor.getCount() == 0");
        }

        ContentValues result = new ContentValues();

        String key;

        try {
            if (cursor.moveToFirst()) {
                do {
                    key = cursor.getString(0);

                    switch (mPreferencesHelper.getPreferenceType(key)) {
                        case PreferencesHelper.PREFERENCE_TYPE_STRING:
                            result.put(key, cursor.getString(1));
                            break;
                        case PreferencesHelper.PREFERENCE_TYPE_LONG:
                            result.put(key, cursor.getLong(1));
                            break;
                        case PreferencesHelper.PREFERENCE_TYPE_INT:
                            result.put(key, cursor.getInt(1));
                            break;
                        case PreferencesHelper.PREFERENCE_TYPE_BOOL:
                            result.put(key, cursor.getInt(1) != 0);
                            break;
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    @NonNull
    private String queryGetAsString(@NonNull String preference) throws RemoteException, FormatException {
        return query(preference).getAsString(preference);
    }

    private long queryGetAsLong(@NonNull String preference) throws RemoteException, FormatException {
        return query(preference).getAsLong(preference);
    }

    private int queryGetAsInt(@NonNull String preference) throws RemoteException, FormatException {
        return query(preference).getAsInteger(preference);
    }

    private boolean queryGetAsBool(@NonNull String preference) throws RemoteException, FormatException {
        return query(preference).getAsBoolean(preference);
    }

    private void update(@NonNull ContentValues contentValues,
                        @Nullable String preference) throws RemoteException {
        mProvider.update(TextUtils.isEmpty(preference) ?
                        AppContentProvider.UriList.PREFERENCES :
                        Uri.withAppendedPath(AppContentProvider.UriList.PREFERENCES, preference),
                contentValues, null, null);
    }

    public String getParentMapsDir() throws RemoteException, FormatException {
        return queryGetAsString(mPreferencesHelper.keys.parentMapsDir);
    }

//    public void putCheckServerTimestamp(long dateTime) throws RemoteException {
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(mPreferencesHelper.keys.checkServerTimestamp, dateTime);
//
//        update(contentValues, mPreferencesHelper.keys.checkServerTimestamp);
//    }
//
//    public long getServerMapsTimestamp() throws RemoteException, FormatException {
//        return queryGetAsLong(mPreferencesHelper.keys.serverMapsTimestamp);
//    }
//
//    public void putServerMapsTimestamp(long date) throws RemoteException {
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(mPreferencesHelper.keys.serverMapsTimestamp, date);
//
//        update(contentValues, mPreferencesHelper.keys.serverMapsTimestamp);
//    }

    @PreferencesHelper.ActionOnHasUpdates
    public int getActionOnHasUpdates() throws RemoteException, FormatException {
        return PreferencesHelper.getActionOnHasUpdatesFromInt(queryGetAsInt(mPreferencesHelper.keys.actionOnHasUpdates));
    }

    public boolean isDownloadOnlyOnWifi() throws RemoteException, FormatException {
        return queryGetAsBool(mPreferencesHelper.keys.downloadOnlyOnWifi);
    }
}