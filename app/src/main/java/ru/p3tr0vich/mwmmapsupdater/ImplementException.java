package ru.p3tr0vich.mwmmapsupdater;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Arrays;

class ImplementException extends ClassCastException {

    ImplementException(@NonNull Context context, @NonNull Class[] ints) {
        super(context.getClass().getName() + " must implement " + Arrays.toString(ints));
    }

    ImplementException(@NonNull Context context, @NonNull Class cls) {
        this(context, new Class[]{cls});
    }
}
