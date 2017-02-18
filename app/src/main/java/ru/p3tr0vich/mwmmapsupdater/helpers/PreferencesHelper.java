package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

import ru.p3tr0vich.mwmmapsupdater.R;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class PreferencesHelper {

    @SuppressLint("StaticFieldLeak")
    private static PreferencesHelper instance;

    private static final String TAG = "PreferencesHelper";

    private static final boolean LOG_ENABLED = false;

    /**
     * Ошибочное время.
     */
    public static final int BAD_DATETIME = 0;

    /**
     * Имя каталога с картами по умолчанию.
     */
    public static final String DEFAULT_MWM_MAPS_DIR = "MapsWithMe";

    @SuppressWarnings("FieldCanBeLocal")
    private final Context mContext; // == ApplicationContext

    private final SharedPreferences mSharedPreferences;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PREFERENCE_TYPE_STRING, PREFERENCE_TYPE_LONG})
    public @interface PreferenceType {
    }

    public static final int PREFERENCE_TYPE_STRING = 0;
    public static final int PREFERENCE_TYPE_LONG = 1;

    public static class Keys {
        @Retention(RetentionPolicy.SOURCE)
        @IntDef({UNKNOWN, MAPS_DIR, DATE_SERVER, CHECK_SERVER_DATE_TIME})
        public @interface KeyAsInt {
        }

        public static final int UNKNOWN = -1;

        public final String mapsDir;
        public static final int MAPS_DIR = R.string.pref_key_maps_dir;

        public final String dateServer;
        public static final int DATE_SERVER = R.string.pref_key_date_server;
        public final String checkServerDateTime;
        public static final int CHECK_SERVER_DATE_TIME = R.string.pref_key_check_server_date_time;

        private Keys(@NonNull Context context) {
            mapsDir = context.getString(MAPS_DIR);

            dateServer = context.getString(DATE_SERVER);
            checkServerDateTime = context.getString(CHECK_SERVER_DATE_TIME);
        }

        @KeyAsInt
        public int getAsInt(@Nullable String key) {
            if (mapsDir.equals(key)) return MAPS_DIR;
            if (dateServer.equals(key)) return DATE_SERVER;
            if (checkServerDateTime.equals(key)) return CHECK_SERVER_DATE_TIME;
            else return UNKNOWN;
        }
    }

    public final Keys keys;

    private PreferencesHelper(@NonNull Context context) {
        mContext = context.getApplicationContext();

        keys = new Keys(context);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public static synchronized PreferencesHelper getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new PreferencesHelper(context);
        }

        return instance;
    }

    @SuppressLint("SwitchIntDef")
    @PreferenceType
    public int getPreferenceType(@NonNull String key) {
        switch (keys.getAsInt(key)) {
            case Keys.CHECK_SERVER_DATE_TIME:
            case Keys.DATE_SERVER:
                return PREFERENCE_TYPE_LONG;
            default:
                return PREFERENCE_TYPE_STRING;
        }
    }

    @NonNull
    private ContentValues getPreferences(@Nullable String preference) {
        ContentValues result = new ContentValues();

        if (TextUtils.isEmpty(preference)) {
            Map<String, ?> map = mSharedPreferences.getAll();

            String key;
            Object value;

            for (Map.Entry<String, ?> entry : map.entrySet()) {
                key = entry.getKey();

                value = entry.getValue();

                if (value instanceof String) result.put(key, (String) value);
                else if (value instanceof Long) result.put(key, (Long) value);
                else if (value instanceof Integer) result.put(key, (Integer) value);
                else if (value instanceof Boolean) result.put(key, (Boolean) value);
                else if (value instanceof Float) result.put(key, (Float) value);
                else {
                    UtilsLog.e(TAG, "getPreferences",
                            "unhandled class == " + value.getClass().getSimpleName());
                }
            }
        } else {
            switch (keys.getAsInt(preference)) {
                case Keys.MAPS_DIR:
                    result.put(preference, getMapsDir());
                    break;
                case Keys.CHECK_SERVER_DATE_TIME:
                    result.put(preference, getCheckServerDateTime());
                    break;
                case Keys.DATE_SERVER:
                    result.put(preference, getDateServer());
                    break;
                case Keys.UNKNOWN:
                default:
                    UtilsLog.e(TAG, "getPreferences", "unhandled preference == " + preference);
            }
        }

        for (String key : result.keySet()) {
            UtilsLog.d(LOG_ENABLED, TAG, "getPreferences", "key == " + key + ", value == " + result.getAsString(key));
        }

        return result;
    }

    @NonNull
    private Cursor getPreferencesCursor(@Nullable String preference) {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key", "value"});

        ContentValues preferences = getPreferences(preference);
        for (String key : preferences.keySet()) {
            matrixCursor.addRow(new Object[]{key, preferences.get(key)});
        }

        return matrixCursor;
    }

    @NonNull
    public Cursor getPreferences() {
        return getPreferencesCursor(null);
    }

    @NonNull
    public Cursor getPreference(@Nullable String preference) {
        return getPreferencesCursor(preference);
    }

    @SuppressLint({"ApplySharedPref"})
    public int setPreferences(@Nullable ContentValues preferences,
                              @Nullable String preference) {
        if (preferences == null || preferences.size() == 0) {
            return -1;
        }

        UtilsLog.d(LOG_ENABLED, TAG, "setPreferences", "preference == " + preference);

        if (TextUtils.isEmpty(preference)) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            try {
                Object value;

                for (String key : preferences.keySet()) {
                    value = preferences.get(key);

                    if (value instanceof String) editor.putString(key, (String) value);
                    else if (value instanceof Long) editor.putLong(key, (Long) value);
                    else if (value instanceof Integer) editor.putInt(key, (Integer) value);
                    else if (value instanceof Boolean) editor.putBoolean(key, (Boolean) value);
                    else if (value instanceof Float) editor.putFloat(key, (Float) value);
                    else {
                        UtilsLog.e(TAG, "setPreferences",
                                "unhandled class == " + value.getClass().getSimpleName());
                    }
                }
            } finally {
                editor.commit();
            }

            return preferences.size();
        } else {
            switch (keys.getAsInt(preference)) {
                case Keys.MAPS_DIR:
                    putMapsDir(preferences.getAsString(preference));
                    break;
                case Keys.CHECK_SERVER_DATE_TIME:
                    putCheckServerDateTime(preferences.getAsLong(preference));
                    break;
                case Keys.DATE_SERVER:
                    putDateServer(preferences.getAsLong(preference));
                    break;
                case Keys.UNKNOWN:
                default:
                    UtilsLog.e(TAG, "setPreferences", "unhandled preference == " + preference);
                    return -1;
            }

            return 1;
        }
    }

    @NonNull
    private String getString(String key, @NonNull String defValue) {
        return mSharedPreferences.getString(key, defValue);
    }

    @NonNull
    public String getString(String key) {
        return getString(key, "");
    }

    @NonNull
    public String getMapsDir() {
        String dirName = getString(keys.mapsDir);
        if (dirName.isEmpty()) {
            File Dir = new File(Environment.getExternalStorageDirectory(), DEFAULT_MWM_MAPS_DIR);
            dirName = Dir.getAbsolutePath();
        }
        return dirName;
    }

    public void putMapsDir(String dirName) {
        mSharedPreferences
                .edit()
                .putString(keys.mapsDir, dirName)
                .apply();
    }

    public long getDateServer() {
        return mSharedPreferences.getLong(keys.dateServer, BAD_DATETIME);
    }

    public void putDateServer(long date) {
        mSharedPreferences
                .edit()
                .putLong(keys.dateServer, date)
                .apply();
    }

    public long getCheckServerDateTime() {
        return mSharedPreferences.getLong(keys.checkServerDateTime, BAD_DATETIME);
    }

    public void putCheckServerDateTime(long dateTime) {
        mSharedPreferences
                .edit()
                .putLong(keys.checkServerDateTime, dateTime)
                .apply();
    }
}