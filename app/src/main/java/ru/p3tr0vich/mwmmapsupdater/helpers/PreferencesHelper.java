package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ru.p3tr0vich.mwmmapsupdater.R;

public class PreferencesHelper {

    @SuppressLint("StaticFieldLeak")
    private static PreferencesHelper instance;

    private static final String TAG = "PreferencesHelper";

    private static final boolean LOG_ENABLED = false;

    /**
     * Имя каталога с картами по умолчанию.
     */
    public static final String DEFAULT_MWM_MAPS_DIR = "MapsWithMe";

    private final Context mContext; // == ApplicationContext
    private final SharedPreferences mSharedPreferences;

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
        String DirName = getString(keys.mapsDir);
        if (DirName.isEmpty()) {
            File Dir = new File(Environment.getExternalStorageDirectory(), DEFAULT_MWM_MAPS_DIR);
            DirName = Dir.getAbsolutePath();
        }
        return DirName;
    }

    public long getDateServer() {
        return mSharedPreferences.getLong(keys.dateServer, 0);
    }

    public void putDateServer(long date) {
        mSharedPreferences
                .edit()
                .putLong(keys.dateServer, date)
                .apply();
    }

    public long getCheckServerDateTime() {
        return mSharedPreferences.getLong(keys.checkServerDateTime, 0);
    }

    public void putCheckServerDateTime(long dateTime) {
        mSharedPreferences
                .edit()
                .putLong(keys.checkServerDateTime, dateTime)
                .apply();
    }
}