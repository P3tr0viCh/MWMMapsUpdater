package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import ru.p3tr0vich.mwmmapsupdater.ActivityMain;
import ru.p3tr0vich.mwmmapsupdater.R;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsDate;

public class NotificationHelper {

    private static final int NOTIFICATION_ID_HAS_UPDATES = 1;
    private static final int NOTIFICATION_ID_DOWNLOAD = 2;

    private NotificationHelper() {
    }

    public static void notifyHasUpdates(@NonNull Context context, long timestamp) {
        Intent intent = new Intent(context, ActivityMain.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification.Builder builder = new Notification.Builder(context)
                .setTicker(context.getText(R.string.text_notification_has_updates_ticker))
                .setContentTitle(context.getText(R.string.text_notification_has_updates_title))
                .setContentText(context.getString(R.string.text_notification_has_updates_text,
                        UtilsDate.format(timestamp)))
                .setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        SystemServicesHelper.getNotificationManager(context).notify(NOTIFICATION_ID_HAS_UPDATES, builder.build());
    }

    public static void cancelHasUpdates(@NonNull Context context) {
        SystemServicesHelper.getNotificationManager(context).cancel(NOTIFICATION_ID_HAS_UPDATES);
    }

    public static void notifyDownloadProgress(@NonNull Context context, @NonNull String mapName, int progress) {
        Intent intent = new Intent(context, ActivityMain.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification.Builder builder = new Notification.Builder(context)
                .setTicker(context.getText(R.string.text_notification_download_progress_ticker))
                .setContentTitle(context.getText(R.string.text_notification_download_progress_title))
                .setContentText(mapName)
                .setProgress(100, progress, false)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(false)
                .setOngoing(true)
                .setDefaults(Notification.DEFAULT_ALL);

        SystemServicesHelper.getNotificationManager(context).notify(NOTIFICATION_ID_DOWNLOAD, builder.build());
    }

    public static void notifyDownloadEnd(@NonNull Context context, long timestamp) {
        Intent intent = new Intent(context, ActivityMain.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification.Builder builder = new Notification.Builder(context)
                .setTicker(context.getText(R.string.text_notification_download_end_ticker))
                .setContentTitle(context.getText(R.string.text_notification_download_end_title))
                .setContentText(context.getString(R.string.text_notification_has_updates_text,
                        UtilsDate.format(timestamp)))
                .setProgress(0, 0, false)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setOngoing(false)
                .setDefaults(Notification.DEFAULT_ALL);

        SystemServicesHelper.getNotificationManager(context).notify(NOTIFICATION_ID_DOWNLOAD, builder.build());
    }
}