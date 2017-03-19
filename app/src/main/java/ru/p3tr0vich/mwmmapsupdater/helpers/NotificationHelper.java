package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import ru.p3tr0vich.mwmmapsupdater.ActivityMain;
import ru.p3tr0vich.mwmmapsupdater.R;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsDate;

public class NotificationHelper {

    private static final int NOTIFICATION_ID = 1;

    private final Context mContext;

    private final NotificationManager mNotificationManager;

    private final Notification.Builder mBuilder;

    public NotificationHelper(@NonNull Context context) {
        mContext = context;

        mNotificationManager = SystemServicesHelper.getNotificationManager(context);

        Intent intent = new Intent(context, ActivityMain.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

        mBuilder = new Notification.Builder(context);
        mBuilder.setContentIntent(contentIntent);
    }

    private void notify(@NonNull Notification notification) {
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void cancel() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    public void notifyHasUpdates(long timestamp) {
        mBuilder.setTicker(mContext.getText(R.string.text_notification_has_updates_ticker));
        mBuilder.setContentTitle(mContext.getText(R.string.text_notification_has_updates_title));
        mBuilder.setContentText(mContext.getString(R.string.text_notification_maps_version_text,
                UtilsDate.format(timestamp)));
        mBuilder.setSmallIcon(R.mipmap.ic_notification);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher));
        mBuilder.setAutoCancel(true);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);

        notify(mBuilder.build());
    }

    public void notifyDownloadStart() {
        mBuilder.setTicker(mContext.getText(R.string.text_notification_download_start_ticker));
        mBuilder.setContentTitle(mContext.getText(R.string.text_notification_download_start_title));
        mBuilder.setContentText(null);
        mBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        mBuilder.setAutoCancel(false);
        mBuilder.setOngoing(true);

        notify(mBuilder.build());
    }

    public void notifyDownloadMapStart(@NonNull String mapName, int i, int count) {
        mBuilder.setContentText(mContext.getString(R.string.text_notification_download_map_start_text, mapName, i, count));

        notify(mBuilder.build());
    }

    public void notifyDownloadProgress(int progress) {
        mBuilder.setProgress(100, progress, false);

        notify(mBuilder.build());
    }

    public void notifyDownloadEnd(long timestamp) {
        mBuilder.setTicker(mContext.getText(R.string.text_notification_download_end_ticker));
        mBuilder.setContentTitle(mContext.getText(R.string.text_notification_download_end_title));
        mBuilder.setContentText(mContext.getString(R.string.text_notification_maps_version_text,
                UtilsDate.format(timestamp)));
        mBuilder.setProgress(0, 0, false);
        mBuilder.setSmallIcon(R.mipmap.ic_notification);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher));
        mBuilder.setAutoCancel(true);
        mBuilder.setOngoing(false);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);

        notify(mBuilder.build());
    }

    public void notifyInstallEnd(long timestamp) {
        mBuilder.setTicker(mContext.getText(R.string.text_notification_install_end_ticker));
        mBuilder.setContentTitle(mContext.getText(R.string.text_notification_install_end_title));
        mBuilder.setContentText(mContext.getString(R.string.text_notification_maps_version_text,
                UtilsDate.format(timestamp)));
        mBuilder.setProgress(0, 0, false);
        mBuilder.setSmallIcon(R.mipmap.ic_notification);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher));
        mBuilder.setAutoCancel(true);
        mBuilder.setOngoing(false);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);

        notify(mBuilder.build());
    }
}