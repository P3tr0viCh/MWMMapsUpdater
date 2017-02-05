package ru.p3tr0vich.mwmmapsupdater;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import ru.p3tr0vich.mwmmapsupdater.factories.FragmentFactory;

interface FragmentInterface {

    @FragmentFactory.Ids.Id
    int getFragmentId();

    @SuppressWarnings("unused")
    @StringRes
    int getTitleId();

    @Nullable
    String getTitle();

    @SuppressWarnings("unused")
    @StringRes
    int getSubtitleId();

    @Nullable
    String getSubtitle();

    interface OnFragmentChangeListener {
        void onFragmentChange(FragmentInterface fragment);
    }

    /**
     * Обрабатывает нажатие кнопки "назад".
     *
     * @return Истина, если нажатие было обработано во фрагменте,
     * ложь, если нажатие нужно обработать в активности.
     * По умолчанию ложь.
     * @see android.app.Activity#onBackPressed
     */
    boolean onBackPressed();
}
