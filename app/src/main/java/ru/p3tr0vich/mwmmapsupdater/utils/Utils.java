package ru.p3tr0vich.mwmmapsupdater.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.widget.Toast;

public class Utils {

    public static void toast(Context context, @NonNull CharSequence text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void toast(Context context, @StringRes int resId) {
        toast(context, context.getResources().getText(resId));
    }
}