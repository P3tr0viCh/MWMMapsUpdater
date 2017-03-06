package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.content.ContentResolver;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

import ru.p3tr0vich.mwmmapsupdater.AppAccount;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class ContentResolverHelper {

    private static final String TAG = "ContentResolverHelper";

    private static final boolean LOG_ENABLED = true;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({POLL_FREQUENCY_6_HRS, POLL_FREQUENCY_12_HRS})
    public @interface PollFrequency {
    }

    public static final int POLL_FREQUENCY_6_HRS = 0;
    private static final long POLL_FREQUENCY_6_HRS_IN_SECS = TimeUnit.HOURS.toSeconds(6);

    public static final int POLL_FREQUENCY_12_HRS = 1;
    private static final long POLL_FREQUENCY_12_HRS_IN_SECS = TimeUnit.HOURS.toSeconds(12);

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({REQUEST_SYNC_CHECK_LOCAL_FILES, REQUEST_SYNC_CHECK_SERVER,
            REQUEST_SYNC_DOWNLOAD, REQUEST_SYNC_INSTALL})
    public @interface RequestSync {
    }

    public static final int REQUEST_SYNC_CHECK_LOCAL_FILES = 0;
    public static final int REQUEST_SYNC_CHECK_SERVER = 1;
    public static final int REQUEST_SYNC_DOWNLOAD = 2;
    public static final int REQUEST_SYNC_INSTALL = 3;

    public static final String SYNC_EXTRAS_REQUEST = "request";

    private ContentResolverHelper() {
    }

    private static void requestSync(@NonNull AppAccount appAccount, @NonNull Bundle extras) {
        UtilsLog.d(LOG_ENABLED, TAG, "requestSync");

        if (isSyncActive(appAccount)) {
            UtilsLog.d(LOG_ENABLED, TAG, "requestSync", "sync active");
            return;
        }

        ContentResolver.requestSync(appAccount.getAccount(), appAccount.getAuthority(), extras);
    }

    public static void requestSync(@NonNull AppAccount appAccount, @RequestSync int request) {
        Bundle extras = new Bundle();

        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        extras.putInt(SYNC_EXTRAS_REQUEST, request);

        requestSync(appAccount, extras);
    }

    public static void requestSyncDebug(@NonNull AppAccount appAccount) {
        requestSync(appAccount, Bundle.EMPTY);
    }

    public static void cancelSync(@NonNull AppAccount appAccount) {
        ContentResolver.cancelSync(appAccount.getAccount(), appAccount.getAuthority());
    }

    public static boolean isSyncActive(@NonNull AppAccount appAccount) {
        return ContentResolver.isSyncActive(appAccount.getAccount(), appAccount.getAuthority());
    }

    public static void setIsSyncable(@NonNull AppAccount appAccount, boolean syncable) {
        ContentResolver.setIsSyncable(appAccount.getAccount(), appAccount.getAuthority(), syncable ? 1 : 0);
    }

    public static void setSyncAutomatically(@NonNull AppAccount appAccount, boolean sync) {
        ContentResolver.setSyncAutomatically(appAccount.getAccount(), appAccount.getAuthority(), sync);
    }

    public static void addPeriodicSync(@NonNull AppAccount appAccount, @PollFrequency int pollFrequency) {
        long pollFrequencyInSecs;

        switch (pollFrequency) {
            case POLL_FREQUENCY_12_HRS:
                pollFrequencyInSecs = POLL_FREQUENCY_12_HRS_IN_SECS;
                break;
            default:
            case POLL_FREQUENCY_6_HRS:
                pollFrequencyInSecs = POLL_FREQUENCY_6_HRS_IN_SECS;
                break;
        }

        ContentResolver.addPeriodicSync(appAccount.getAccount(), appAccount.getAuthority(), Bundle.EMPTY, pollFrequencyInSecs);
    }
}