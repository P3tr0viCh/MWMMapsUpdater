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
        @IntDef({UNKNOWN, MAPS_DIR})
        public @interface KeyAsInt {
        }

        public static final int UNKNOWN = -1;

        public final String mapsDir;
        public static final int MAPS_DIR = R.string.pref_key_maps_dir;

        private Keys(@NonNull Context context) {
            mapsDir = context.getString(R.string.pref_key_maps_dir);
        }

        @KeyAsInt
        public int getAsInt(@Nullable String key) {
            if (mapsDir.equals(key)) return MAPS_DIR;
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
}