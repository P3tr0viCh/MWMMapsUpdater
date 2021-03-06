package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

import ru.p3tr0vich.mwmmapsupdater.Consts;
import ru.p3tr0vich.mwmmapsupdater.R;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class PreferencesHelper {

    @SuppressLint("StaticFieldLeak")
    private static PreferencesHelper instance;

    private static final String TAG = "PreferencesHelper";

    private static final boolean LOG_ENABLED = false;

    @SuppressWarnings("FieldCanBeLocal")
    private final Context mContext; // == ApplicationContext

    private final SharedPreferences mSharedPreferences;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PREFERENCE_TYPE_STRING, PREFERENCE_TYPE_LONG, PREFERENCE_TYPE_INT, PREFERENCE_TYPE_BOOL})
    public @interface PreferenceType {
    }

    public static final int PREFERENCE_TYPE_STRING = 0;
    public static final int PREFERENCE_TYPE_LONG = 1;
    public static final int PREFERENCE_TYPE_INT = 2;
    public static final int PREFERENCE_TYPE_BOOL = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ACTION_ON_HAS_UPDATES_DO_SHOW_NOTIFICATION,
            ACTION_ON_HAS_UPDATES_DO_DOWNLOAD, ACTION_ON_HAS_UPDATES_DO_INSTALL})
    public @interface ActionOnHasUpdates {
    }

    public static final int ACTION_ON_HAS_UPDATES_DO_SHOW_NOTIFICATION = 0;
    public static final int ACTION_ON_HAS_UPDATES_DO_DOWNLOAD = 1;
    public static final int ACTION_ON_HAS_UPDATES_DO_INSTALL = 2;

    @ActionOnHasUpdates
    public static int getActionOnHasUpdatesFromInt(int actionOnHasUpdates) {
        switch (actionOnHasUpdates) {
            case ACTION_ON_HAS_UPDATES_DO_DOWNLOAD:
            case ACTION_ON_HAS_UPDATES_DO_INSTALL:
                return actionOnHasUpdates;
            default:
            case ACTION_ON_HAS_UPDATES_DO_SHOW_NOTIFICATION:
                return ACTION_ON_HAS_UPDATES_DO_SHOW_NOTIFICATION;
        }
    }

    public static class Keys {
        @Retention(RetentionPolicy.SOURCE)
        @IntDef({UNKNOWN,
                PARENT_MAPS_DIR,
                LOCAL_MAPS_TIMESTAMP, SERVER_MAPS_TIMESTAMP, CHECK_SERVER_TIMESTAMP,
                ACTION_ON_HAS_UPDATES, DOWNLOAD_ONLY_ON_WIFI, SAVE_ORIGINAL_MAPS,
                NOTIFICATION_DEFAULTS_SOUND, NOTIFICATION_DEFAULTS_VIBRATE, NOTIFICATION_DEFAULTS_LIGHTS})
        public @interface KeyAsInt {
        }

        public static final int UNKNOWN = -1;

        public final String parentMapsDir;
        public static final int PARENT_MAPS_DIR = R.string.pref_key_parent_maps_dir;

        public final String localMapsTimestamp;
        public static final int LOCAL_MAPS_TIMESTAMP = R.string.pref_key_local_maps_timestamp;

        public final String serverMapsTimestamp;
        public static final int SERVER_MAPS_TIMESTAMP = R.string.pref_key_server_maps_timestamp;

        public final String checkServerTimestamp;
        public static final int CHECK_SERVER_TIMESTAMP = R.string.pref_key_check_server_timestamp;

        public final String actionOnHasUpdates;
        public static final int ACTION_ON_HAS_UPDATES = R.string.pref_key_action_on_has_updates;

        public final String downloadOnlyOnWifi;
        public static final int DOWNLOAD_ONLY_ON_WIFI = R.string.pref_key_download_only_on_wifi;

        public final String saveOriginalMaps;
        public static final int SAVE_ORIGINAL_MAPS = R.string.pref_key_save_original_maps;

        public final String notificationDefaultsSound;
        public static final int NOTIFICATION_DEFAULTS_SOUND = R.string.pref_key_notification_defaults_sound;
        public final String notificationDefaultsVibrate;
        public static final int NOTIFICATION_DEFAULTS_VIBRATE = R.string.pref_key_notification_defaults_vibrate;
        public final String notificationDefaultsLights;
        public static final int NOTIFICATION_DEFAULTS_LIGHTS = R.string.pref_key_notification_defaults_lights;

        private Keys(@NonNull Context context) {
            parentMapsDir = context.getString(PARENT_MAPS_DIR);

            localMapsTimestamp = context.getString(LOCAL_MAPS_TIMESTAMP);
            serverMapsTimestamp = context.getString(SERVER_MAPS_TIMESTAMP);
            checkServerTimestamp = context.getString(CHECK_SERVER_TIMESTAMP);

            actionOnHasUpdates = context.getString(ACTION_ON_HAS_UPDATES);
            downloadOnlyOnWifi = context.getString(DOWNLOAD_ONLY_ON_WIFI);
            saveOriginalMaps = context.getString(SAVE_ORIGINAL_MAPS);

            notificationDefaultsSound = context.getString(NOTIFICATION_DEFAULTS_SOUND);
            notificationDefaultsVibrate = context.getString(NOTIFICATION_DEFAULTS_VIBRATE);
            notificationDefaultsLights = context.getString(NOTIFICATION_DEFAULTS_LIGHTS);
        }

        @KeyAsInt
        public int getAsInt(@Nullable String key) {
            if (parentMapsDir.equals(key)) return PARENT_MAPS_DIR;

            if (localMapsTimestamp.equals(key)) return LOCAL_MAPS_TIMESTAMP;
            if (serverMapsTimestamp.equals(key)) return SERVER_MAPS_TIMESTAMP;
            if (checkServerTimestamp.equals(key)) return CHECK_SERVER_TIMESTAMP;

            if (actionOnHasUpdates.equals(key)) return ACTION_ON_HAS_UPDATES;
            if (downloadOnlyOnWifi.equals(key)) return DOWNLOAD_ONLY_ON_WIFI;
            if (saveOriginalMaps.equals(key)) return SAVE_ORIGINAL_MAPS;

            if (notificationDefaultsSound.equals(key)) return NOTIFICATION_DEFAULTS_SOUND;
            if (notificationDefaultsVibrate.equals(key)) return NOTIFICATION_DEFAULTS_VIBRATE;
            if (notificationDefaultsLights.equals(key)) return NOTIFICATION_DEFAULTS_LIGHTS;

            return UNKNOWN;
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
            case Keys.CHECK_SERVER_TIMESTAMP:
            case Keys.LOCAL_MAPS_TIMESTAMP:
            case Keys.SERVER_MAPS_TIMESTAMP:
                return PREFERENCE_TYPE_LONG;

            case Keys.PARENT_MAPS_DIR:
                return PREFERENCE_TYPE_STRING;

            case Keys.ACTION_ON_HAS_UPDATES:
                return PREFERENCE_TYPE_INT;

            case Keys.DOWNLOAD_ONLY_ON_WIFI:
            case Keys.SAVE_ORIGINAL_MAPS:
            case Keys.NOTIFICATION_DEFAULTS_SOUND:
            case Keys.NOTIFICATION_DEFAULTS_VIBRATE:
            case Keys.NOTIFICATION_DEFAULTS_LIGHTS:
                return PREFERENCE_TYPE_BOOL;

            default:
            case Keys.UNKNOWN:
                UtilsLog.e(TAG, "getPreferenceType", "unhandled preference == " + key);
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

                case Keys.LOCAL_MAPS_TIMESTAMP:
                    result.put(preference, getLocalMapsTimestamp());
                    break;
                case Keys.SERVER_MAPS_TIMESTAMP:
                    result.put(preference, getServerMapsTimestamp());
                    break;
                case Keys.CHECK_SERVER_TIMESTAMP:
                    result.put(preference, getCheckServerTimestamp());
                    break;

                case Keys.ACTION_ON_HAS_UPDATES:
                    result.put(preference, getActionOnHasUpdates());
                    break;
                case Keys.DOWNLOAD_ONLY_ON_WIFI:
                    result.put(preference, isDownloadOnlyOnWifi());
                    break;
                case Keys.SAVE_ORIGINAL_MAPS:
                    result.put(preference, isSaveOriginalMaps());
                    break;

                case Keys.NOTIFICATION_DEFAULTS_SOUND:
                    result.put(preference, isNotificationUseDefaultSound());
                    break;
                case Keys.NOTIFICATION_DEFAULTS_VIBRATE:
                    result.put(preference, isNotificationUseDefaultVibrate());
                    break;
                case Keys.NOTIFICATION_DEFAULTS_LIGHTS:
                    result.put(preference, isNotificationUseDefaultLights());
                    break;
                case Keys.UNKNOWN:
                default:
                    UtilsLog.e(TAG, "getPreferences", "unhandled preference == " + preference);
            }
        }

        if (LOG_ENABLED) {
            for (String key : result.keySet()) {
                UtilsLog.d(true, TAG, "getPreferences", "key == " + key + ", value == " + result.getAsString(key));
            }
        }

        return result;
    }

    @NonNull
    private Cursor getPreferencesCursor(@Nullable String preference) {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key", "value"});

        ContentValues preferences = getPreferences(preference);
        for (String key : preferences.keySet()) {
            switch (getPreferenceType(key)) {
                case PREFERENCE_TYPE_STRING:
                    matrixCursor.addRow(new Object[]{key, preferences.getAsString(key)});
                    break;
                case PREFERENCE_TYPE_LONG:
                    matrixCursor.addRow(new Object[]{key, preferences.getAsLong(key)});
                    break;
                case PREFERENCE_TYPE_INT:
                    matrixCursor.addRow(new Object[]{key, preferences.getAsInteger(key)});
                    break;
                case PREFERENCE_TYPE_BOOL:
                    // TODO: 19.04.2017 check
                    //noinspection UnnecessaryBoxing
                    matrixCursor.addRow(new Object[]{key, Integer.valueOf(preferences.getAsBoolean(key) ? 1 : 0)});
                    break;
            }
        }

        if (LOG_ENABLED) {
            if (matrixCursor.moveToFirst()) {
                do {
                    UtilsLog.d(true, TAG, "getPreferencesCursor",
                            "key == " + matrixCursor.getString(0) + ", type == " + matrixCursor.getType(1) + ", value == " + matrixCursor.getString(1));
                } while (matrixCursor.moveToNext());
            }
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

                case Keys.LOCAL_MAPS_TIMESTAMP:
                    putLocalMapsTimestamp(preferences.getAsLong(preference));
                    break;
                case Keys.SERVER_MAPS_TIMESTAMP:
                    putServerMapsTimestamp(preferences.getAsLong(preference));
                    break;
                case Keys.CHECK_SERVER_TIMESTAMP:
                    putCheckServerTimestamp(preferences.getAsLong(preference));
                    break;

                case Keys.ACTION_ON_HAS_UPDATES:
                    putActionOnHasUpdates(getActionOnHasUpdatesFromInt(preferences.getAsInteger(preference)));
                    break;
                case Keys.DOWNLOAD_ONLY_ON_WIFI:
                    putDownloadOnlyOnWifi(preferences.getAsBoolean(preference));
                    break;
                case Keys.SAVE_ORIGINAL_MAPS:
                    putSaveOriginalMaps(preferences.getAsBoolean(preference));
                    break;

                case Keys.NOTIFICATION_DEFAULTS_SOUND:
                    putNotificationUseDefaultSound(preferences.getAsBoolean(preference));
                    break;
                case Keys.NOTIFICATION_DEFAULTS_VIBRATE:
                    putNotificationUseDefaultVibrate(preferences.getAsBoolean(preference));
                    break;
                case Keys.NOTIFICATION_DEFAULTS_LIGHTS:
                    putNotificationUseDefaultLights(preferences.getAsBoolean(preference));
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
    private String getString(String key, @SuppressWarnings("SameParameterValue") @NonNull String defValue) {
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
            File dir = FilesHelper.getDefaultParentMapsDir();
            dirName = dir.getAbsolutePath();
        }

        return dirName;
    }

    public void putParentMapsDir(String dirName) {
        mSharedPreferences
                .edit()
                .putString(keys.parentMapsDir, dirName)
                .apply();
    }

    public long getLocalMapsTimestamp() {
        return mSharedPreferences.getLong(keys.localMapsTimestamp, Consts.BAD_DATETIME);
    }

    public void putLocalMapsTimestamp(long date) {
        mSharedPreferences
                .edit()
                .putLong(keys.localMapsTimestamp, date)
                .apply();
    }

    public long getServerMapsTimestamp() {
        return mSharedPreferences.getLong(keys.serverMapsTimestamp, Consts.BAD_DATETIME);
    }

    public void putServerMapsTimestamp(long date) {
        mSharedPreferences
                .edit()
                .putLong(keys.serverMapsTimestamp, date)
                .apply();
    }

    public long getCheckServerTimestamp() {
        return mSharedPreferences.getLong(keys.checkServerTimestamp, Consts.BAD_DATETIME);
    }

    public void putCheckServerTimestamp(long dateTime) {
        mSharedPreferences
                .edit()
                .putLong(keys.checkServerTimestamp, dateTime)
                .apply();
    }

    @ActionOnHasUpdates
    private int getActionOnHasUpdates() {
        return getActionOnHasUpdatesFromInt(mSharedPreferences.getInt(keys.actionOnHasUpdates, ACTION_ON_HAS_UPDATES_DO_INSTALL));
    }

    private void putActionOnHasUpdates(@ActionOnHasUpdates int actionOnHasUpdates) {
        mSharedPreferences
                .edit()
                .putInt(keys.actionOnHasUpdates, actionOnHasUpdates)
                .apply();
    }

    public boolean isDownloadOnlyOnWifi() {
        return mSharedPreferences.getBoolean(keys.downloadOnlyOnWifi, true);
    }

    private void putDownloadOnlyOnWifi(boolean downloadOnlyOnWifi) {
        mSharedPreferences
                .edit()
                .putBoolean(keys.downloadOnlyOnWifi, downloadOnlyOnWifi)
                .apply();
    }

    public boolean isSaveOriginalMaps() {
        return mSharedPreferences.getBoolean(keys.saveOriginalMaps, true);
    }

    private void putSaveOriginalMaps(boolean saveOriginalMaps) {
        mSharedPreferences
                .edit()
                .putBoolean(keys.saveOriginalMaps, saveOriginalMaps)
                .apply();
    }

    public boolean isNotificationUseDefaultSound() {
        return mSharedPreferences.getBoolean(keys.notificationDefaultsSound, mContext.getResources().getBoolean(R.bool.pref_notification_defaults_sound_default_value));
    }

    private void putNotificationUseDefaultSound(boolean b) {
        mSharedPreferences
                .edit()
                .putBoolean(keys.notificationDefaultsSound, b)
                .apply();
    }

    public boolean isNotificationUseDefaultVibrate() {
        return mSharedPreferences.getBoolean(keys.notificationDefaultsVibrate, mContext.getResources().getBoolean(R.bool.pref_notification_defaults_vibrate_default_value));
    }

    private void putNotificationUseDefaultVibrate(boolean b) {
        mSharedPreferences
                .edit()
                .putBoolean(keys.notificationDefaultsVibrate, b)
                .apply();
    }

    public boolean isNotificationUseDefaultLights() {
        return mSharedPreferences.getBoolean(keys.notificationDefaultsLights, mContext.getResources().getBoolean(R.bool.pref_notification_defaults_lights_default_value));
    }

    private void putNotificationUseDefaultLights(boolean b) {
        mSharedPreferences
                .edit()
                .putBoolean(keys.notificationDefaultsLights, b)
                .apply();
    }

}