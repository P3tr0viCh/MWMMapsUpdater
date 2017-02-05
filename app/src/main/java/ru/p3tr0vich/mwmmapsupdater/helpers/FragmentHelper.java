package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import ru.p3tr0vich.mwmmapsupdater.R;
import ru.p3tr0vich.mwmmapsupdater.factories.FragmentFactory;

public class FragmentHelper {
    private final FragmentActivity mFragmentActivity;

    public FragmentHelper(@NonNull FragmentActivity fragmentActivity) {
        mFragmentActivity = fragmentActivity;
    }

    public void addMainFragment() {
        mFragmentActivity.getSupportFragmentManager().beginTransaction()
                .add(R.id.content_frame,
                        FragmentFactory.getFragmentNewInstance(FragmentFactory.MainFragment.ID),
                        FragmentFactory.MainFragment.TAG)
                .setTransition(FragmentTransaction.TRANSIT_NONE)
                .commit();
    }
}