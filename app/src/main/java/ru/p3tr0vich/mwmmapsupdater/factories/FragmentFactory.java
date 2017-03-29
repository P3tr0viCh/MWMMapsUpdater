package ru.p3tr0vich.mwmmapsupdater.factories;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ru.p3tr0vich.mwmmapsupdater.FragmentAbout;
import ru.p3tr0vich.mwmmapsupdater.FragmentBase;
import ru.p3tr0vich.mwmmapsupdater.FragmentMain;
import ru.p3tr0vich.mwmmapsupdater.FragmentPreferences;

public class FragmentFactory {

    public interface Ids {

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({BAD_ID,
                MAIN,
                PREFERENCES,
                ABOUT})
        @interface Id {
        }

        int BAD_ID = -1;
        int MAIN = 0;
        int PREFERENCES = 1;
        int ABOUT = 2;
    }

    private interface Tags {
        String MAIN = FragmentMain.class.getSimpleName();
        String PREFERENCES = FragmentPreferences.class.getSimpleName();
        String ABOUT = FragmentAbout.class.getSimpleName();
    }

    public interface MainFragment {
        int ID = Ids.MAIN;
        String TAG = Tags.MAIN;
    }

    private FragmentFactory() {
    }

    @NonNull
    public static Fragment getFragmentNewInstance(@Ids.Id int fragmentId, @Nullable Bundle args) {
        Fragment fragment;

        switch (fragmentId) {
            case Ids.MAIN:
                fragment = new FragmentMain();
                break;
            case Ids.PREFERENCES:
                fragment = new FragmentPreferences();
                break;
            case Ids.ABOUT:
                fragment = new FragmentAbout();
                break;
            case Ids.BAD_ID:
            default:
                throw new IllegalArgumentException("Fragment bad ID");
        }

        return FragmentBase.newInstance(fragmentId, fragment, args);
    }

    @NonNull
    public static Fragment getFragmentNewInstance(@Ids.Id int fragmentId) {
        return getFragmentNewInstance(fragmentId, null);
    }

    @Ids.Id
    public static int intToFragmentId(int id) {
        return id;
    }

    @NonNull
    public static String fragmentIdToTag(@Ids.Id int id) {
        switch (id) {
            case Ids.MAIN:
                return Tags.MAIN;
            case Ids.PREFERENCES:
                return Tags.PREFERENCES;
            case Ids.ABOUT:
                return Tags.ABOUT;
            case Ids.BAD_ID:
            default:
                throw new IllegalArgumentException("Fragment bad ID");
        }
    }
}