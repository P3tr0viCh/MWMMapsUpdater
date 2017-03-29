package ru.p3tr0vich.mwmmapsupdater;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;

import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class FragmentPreferences extends FragmentPreferencesBase {

    private static final String TAG = "FragmentPreferences";

    private static final boolean LOG_ENABLED = false;

    public static final String KEY_PREFERENCE_SCREEN = "KEY_PREFERENCE_SCREEN";


    private boolean mIsInRoot;
    private PreferenceScreen mRootPreferenceScreen;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        UtilsLog.d(LOG_ENABLED, TAG, "onCreatePreferences");

        addPreferencesFromResource(R.xml.preferences);

        mRootPreferenceScreen = getPreferenceScreen();

        String keyPreferenceScreen = null;

        if (savedInstanceState == null) {
            Bundle arguments = getArguments();
            if (arguments != null) {
                keyPreferenceScreen = arguments.getString(KEY_PREFERENCE_SCREEN);
            }
        } else {
            keyPreferenceScreen = savedInstanceState.getString(KEY_PREFERENCE_SCREEN);
        }

        if (TextUtils.isEmpty(keyPreferenceScreen)) {
            mIsInRoot = true;
        } else {
            navigateToScreen((PreferenceScreen) findPreference(keyPreferenceScreen));
        }
    }

    @Nullable
    @Override
    public String getTitle() {
        return mIsInRoot ? getString(R.string.title_prefs) : (String) getPreferenceScreen().getTitle();
    }

    public boolean isInRoot() {
        return mIsInRoot;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(KEY_PREFERENCE_SCREEN, mIsInRoot ? null : getPreferenceScreen().getKey());
    }

    @Override
    public void onNavigateToScreen(PreferenceScreen preferenceScreen) {
        navigateToScreen(preferenceScreen);

        super.onNavigateToScreen(preferenceScreen);
    }

    private void navigateToScreen(@Nullable PreferenceScreen preferenceScreen) {
        mIsInRoot = preferenceScreen == null || preferenceScreen.equals(mRootPreferenceScreen);

        setPreferenceScreen(mIsInRoot ? mRootPreferenceScreen : preferenceScreen);

        // TODO: 29.03.2017
//        mOnPreferenceScreenChangeListener.onPreferenceScreenChanged(getTitle(), mIsInRoot);
    }

    public boolean goToRootScreen() {
        if (mIsInRoot) return false;

        navigateToScreen(null);

        return true;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}