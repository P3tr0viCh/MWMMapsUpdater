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

        if (LOG_ENABLED) {
            String sEvent = null;

            switch (event) {
                case CREATE:
                    sEvent = "CREATE";
                    break;
                case DELETE:
                    sEvent = "DELETE";
                    break;
                case DELETE_SELF:
                    sEvent = "DELETE_SELF";
                    break;
                case MOVED_FROM:
                    sEvent = "MOVED_FROM";
                    break;
                case MOVED_TO:
                    sEvent = "MOVED_TO";
                    break;
                case MOVE_SELF:
                    sEvent = "MOVE_SELF";
                    break;
                default:
                    sEvent = "" + event;
            }

            UtilsLog.d(true, TAG, "onEvent", "event == " + sEvent + ", path == " + path);
        }

        switch (event) {
            case CREATE:
            case DELETE:
            case DELETE_SELF:
            case MOVED_FROM:
            case MOVED_TO:
            case MOVE_SELF:
                mHandler.postDelayed(mRunnable, ON_CONTENT_CHANGED_DELAY);
        }
    }
}