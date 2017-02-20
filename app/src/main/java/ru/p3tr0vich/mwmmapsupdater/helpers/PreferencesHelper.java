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
import java.util.Set;

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
    public static final String DEFAULT_PARENT_MAPS_DIR_NAME = "MapsWithMe";

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
        @IntDef({UNKNOWN, PARENT_MAPS_DIR, USED_MAP_DIR, USED_MAP_FILES,
                DATE_LOCAL, DATE_SERVER, CHECK_SERVER_DATE_TIME})
        public @interface KeyAsInt {
        }

        public static final int UNKNOWN = -1;

        public final String parentMapsDir;
        public static final int PARENT_MAPS_DIR = R.string.pref_key_parent_maps_dir;

        public final String usedMapDir;
        public static final int USED_MAP_DIR = R.string.pref_key_used_map_dir;

        public final String usedMapFiles;
        public static final int USED_MAP_FILES = R.string.pref_key_used_map_files;

        public final String dateLocal;
        public static final int DATE_LOCAL = R.string.pref_key_date_local;

        public final String dateServer;
        public static final int DATE_SERVER = R.string.pref_key_date_server;
        public final String checkServerDateTime;
        public static final int CHECK_SERVER_DATE_TIME = R.string.pref_key_check_server_date_time;

        private Keys(@NonNull Context context) {
            parentMapsDir = context.getString(PARENT_MAPS_DIR);

            usedMapDir = context.getString(USED_MAP_DIR);
            usedMapFiles = context.getString(USED_MAP_FILES);

            dateLocal = context.getString(DATE_LOCAL);

            dateServer = context.getString(DATE_SERVER);
            checkServerDateTime = context.getString(CHECK_SERVER_DATE_TIME);
        }

        @KeyAsInt
        public int getAsInt(@Nullable String key) {
            if (parentMapsDir.equals(key)) return PARENT_MAPS_DIR;
            if (usedMapDir.equals(key)) return USED_MAP_DIR;
            if (usedMapFiles.equals(key)) return USED_MAP_FILES;
            if (dateLocal.equals(key)) return DATE_LOCAL;
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

    @PreferenceType
    public int getPreferenceType(@NonNull String key) {
        switch (keys.getAsInt(key)) {
            case Keys.CHECK_SERVER_DATE_TIME:
            case Keys.DATE_LOCAL:
            case Keys.DATE_SERVER:
                return PREFERENCE_TYPE_LONG;
            default:
            case Keys.UNKNOWN:
                UtilsLog.e(TAG, "getPreferenceType", "unhandled preference == " + key);
            case Keys.PARENT_MAPS_DIR:
            case Keys.USED_MAP_DIR:
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
                case Keys.PARENT_MAPS_DIR:
                    result.put(preference, getParentMapsDir());
                    break;
                case Keys.USED_MAP_DIR:
                    result.put(preference, getUsedMapDir());
                    break;
                case Keys.DATE_LOCAL:
                    result.put(preference, getDateLocal());
                    break;
                case Keys.DATE_SERVER:
                    result.put(preference, getDateServer());
                    break;
                case Keys.CHECK_SERVER_DATE_TIME:
                    result.put(preference, getCheckServerDateTime());
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
                case Keys.PARENT_MAPS_DIR:
                    putParentMapsDir(preferences.getAsString(preference));
                    break;
                case Keys.USED_MAP_DIR:
                    putUsedMapDir(preferences.getAsString(preference));
                    break;
                case Keys.DATE_LOCAL:
                    putDateLocal(preferences.getAsLong(preference));
                    break;
                case Keys.DATE_SERVER:
                    putDateServer(preferences.getAsLong(preference));
                    break;
                case Keys.CHECK_SERVER_DATE_TIME:
                    putCheckServerDateTime(preferences.getAsLong(preference));
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
    private String getString(String key) {
        return getString(key, "");
    }

    @NonNull
    public String getParentMapsDir() {
        String dirName = getString(keys.parentMapsDir);
        if (dirName.isEmpty()) {
            File Dir = new File(Environment.getExternalStorageDirectory(), DEFAULT_PARENT_MAPS_DIR_NAME);
            dirName = Dir.getAbsolutePath();
        }
        return dirName;
    }

    public void putParentMapsDir(String dirName) {
        mSharedPreferences
                .edit()
                .putString(keys.parentMapsDir, dirName)
                .apply();
    }

    @NonNull
    public String getUsedMapDir() {
        return getString(keys.usedMapDir);
    }

    public void putUsedMapDir(@Nullable String usedMapDir) {
        mSharedPreferences
                .edit()
                .putString(keys.usedMapDir, usedMapDir)
                .apply();
    }

    @Nullable
    public Set<String> getUsedMapFiles() {
        return mSharedPreferences.getStringSet(keys.usedMapFiles, null);
    }

    public void putUsedMapFiles(@Nullable Set<String> usedMapFiles) {
        mSharedPreferences
                .edit()
                .putStringSet(keys.usedMapFiles, usedMapFiles)
                .apply();
    }

    public long getDateLocal() {
        return mSharedPreferences.getLong(keys.dateLocal, BAD_DATETIME);
    }

    public void putDateLocal(long date) {
        mSharedPreferences
                .edit()
                .putLong(keys.dateLocal, date)
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