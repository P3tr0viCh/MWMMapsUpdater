package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.support.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class MapFilesServerHelper {

    private static final String TAG = "MapFilesServerHelper";

    private static final String PROTOCOL = "http";
    private static final String HOST = "direct.mapswithme.com";
    private static final String PATH = "regular/daily";

    public static Date getVersion(@NonNull List<String> fileList) {
        URL url = null;
        try {
            url = new URL(PROTOCOL, HOST, PATH + "/" + "Abkhazia" + ".mwm");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        UtilsLog.d(TAG, "getVersion", url != null ? url.toString() : "url == null");

        return null;
    }
}