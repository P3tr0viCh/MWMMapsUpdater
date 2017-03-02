package ru.p3tr0vich.mwmmapsupdater.observers;

import android.os.FileObserver;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import ru.p3tr0vich.mwmmapsupdater.MapFilesLoader;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class MapFilesObserver extends FileObserver {

    private static final String TAG = "MapFilesObserver";

    private static final boolean LOG_ENABLED = false;

    private static final long ON_CONTENT_CHANGED_DELAY = TimeUnit.SECONDS.toMillis(5);

    private MapFilesLoader mLoader;

    private final Handler mHandler = new Handler();

    public MapFilesObserver(@NonNull MapFilesLoader loader, @NonNull String path) {
        super(path, CREATE | DELETE | DELETE_SELF | MOVED_FROM | MOVED_TO | MOVE_SELF);

        UtilsLog.d(LOG_ENABLED, TAG, "constructor", "path == " + path);

        mLoader = loader;
    }

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mLoader.onContentChanged();
        }
    };

    @Override
    protected void finalize() {
        mHandler.removeCallbacks(mRunnable);
        super.finalize();
    }

    @Override
    public void onEvent(int event, String path) {
        mHandler.removeCallbacks(mRunnable);

        UtilsLog.d(LOG_ENABLED, TAG, "onEvent", "path == " + path);

        if (path != null) {
            mHandler.postDelayed(mRunnable, ON_CONTENT_CHANGED_DELAY);
        }
    }
}