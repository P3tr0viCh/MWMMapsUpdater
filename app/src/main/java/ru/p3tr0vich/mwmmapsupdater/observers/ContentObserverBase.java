package ru.p3tr0vich.mwmmapsupdater.observers;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

public class ContentObserverBase extends ContentObserver {

    private ContentObserverBase(Handler handler) {
        super(handler);
    }

    ContentObserverBase() {
        this(new Handler(Looper.getMainLooper()));
    }

    /**
     * Регистрирует наблюдатель.
     *
     * @see android.content.ContentResolver#registerContentObserver(Uri, boolean, ContentObserver)
     */
    protected final void register(@NonNull Context context, @NonNull Uri uri, boolean notifyForDescendents) {
        context.getContentResolver().registerContentObserver(uri, notifyForDescendents, this);
    }

    /**
     * Удаляет наблюдатель.
     *
     * @see android.content.ContentResolver#unregisterContentObserver(ContentObserver)
     */
    public final void unregister(@NonNull Context context) {
        context.getContentResolver().unregisterContentObserver(this);
    }

    @Override
    public final void onChange(boolean selfChange) {
        onChange(selfChange, null);
    }

    public static void notifyChange(@NonNull Context context, Uri uri) {
        context.getContentResolver().notifyChange(uri, null, false);
    }
}