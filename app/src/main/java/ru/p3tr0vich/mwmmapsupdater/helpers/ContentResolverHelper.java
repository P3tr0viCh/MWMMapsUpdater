package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class ContentResolverHelper {

    private static final String TAG = "ContentResolverHelper";

    private static final boolean LOG_ENABLED = true;

    public static void requestSync(@NonNull Context context) {
        UtilsLog.d(LOG_ENABLED, TAG, "requestSync");

        SyncAccountHelper syncAccountHelper = SyncAccountHelper.getInstance(context);

        if (syncAccountHelper.isSyncActive()) {
            UtilsLog.d(LOG_ENABLED, TAG, "requestSync", "sync active");
            return;
        }

        Bundle extras = new Bundle();

        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        ContentResolver.requestSync(syncAccountHelper.getAccount(), syncAccountHelper.getAuthority(), extras);
    }
}
