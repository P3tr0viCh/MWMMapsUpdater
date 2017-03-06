package ru.p3tr0vich.mwmmapsupdater.exceptions;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Arrays;

public class ImplementException extends ClassCastException {

    ImplementException(@NonNull Context context, @NonNull Class[] ints) {
        super(context.getClass().getName() + " must implement " + Arrays.toString(ints));
    }

    public ImplementException(@NonNull Context context, @NonNull Class cls) {
        this(context, new Class[]{cls});
    }
}