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
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    private void update(@NonNull ContentValues contentValues,
                        @Nullable String preference) throws RemoteException {
        mProvider.update(TextUtils.isEmpty(preference) ?
                        AppContentProvider.UriList.PREFERENCES :
                        Uri.withAppendedPath(AppContentProvider.UriList.PREFERENCES, preference),
                contentValues, null, null);
    }

    public String getParentMapsDir() throws RemoteException, FormatException {
        return query(mPreferencesHelper.keys.parentMapsDir).getAsString(mPreferencesHelper.keys.parentMapsDir);
    }

    public void putCheckServerDateTime(long dateTime) throws RemoteException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(mPreferencesHelper.keys.checkServerDateTime, dateTime);

        update(contentValues, mPreferencesHelper.keys.checkServerDateTime);
    }


    public void putDateServer(long date) throws RemoteException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(mPreferencesHelper.keys.dateServer, date);

        update(contentValues, mPreferencesHelper.keys.dateServer);
    }
}