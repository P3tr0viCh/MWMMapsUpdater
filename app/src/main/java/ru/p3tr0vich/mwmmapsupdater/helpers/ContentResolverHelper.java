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
    public static final long POLL_FREQUENCY_6_HRS_IN_SECS = TimeUnit.HOURS.toSeconds(6);

    public static final int POLL_FREQUENCY_12_HRS = 1;
    public static final long POLL_FREQUENCY_12_HRS_IN_SECS = TimeUnit.HOURS.toSeconds(12);

    public static void requestSync(@NonNull AppAccount appAccount) {
        UtilsLog.d(LOG_ENABLED, TAG, "requestSync");

        if (isSyncActive(appAccount)) {
            UtilsLog.d(LOG_ENABLED, TAG, "requestSync", "sync active");
            return;
        }

        Bundle extras = new Bundle();

        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        ContentResolver.requestSync(appAccount.getAccount(), appAccount.getAuthority(), extras);
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