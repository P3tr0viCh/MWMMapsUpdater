package ru.p3tr0vich.mwmmapsupdater.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import ru.p3tr0vich.mwmmapsupdater.R;

public class Utils {
    private static final String TAG = "Utils";

    public static void toast(Context context, @NonNull CharSequence text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void toast(Context context, @StringRes int resId) {
        toast(context, context.getResources().getText(resId));
    }

    @NonNull
    public static JSONObject getMapNamesAndDescriptions(Context context) {
        try {
            String language = Locale.getDefault().getLanguage();

            if (!"ru".equals(language)) {
                language = "en";
            }

            InputStream inputStream = context.getResources().getAssets().open(
                    context.getString(R.string.countries_strings_json, language));

            int size = inputStream.available();

            byte[] buffer = new byte[size];

            //noinspection ResultOfMethodCallIgnored
            inputStream.read(buffer);

            inputStream.close();

            return new JSONObject(new String(buffer, "UTF-8"));
        } catch (IOException | JSONException e) {
            e.printStackTrace();

            UtilsLog.e(TAG, "getMapNamesAndDescriptions", e);

            return new JSONObject();
        }
    }
}