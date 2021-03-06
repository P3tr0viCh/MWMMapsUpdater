package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ru.p3tr0vich.mwmmapsupdater.BuildConfig;
import ru.p3tr0vich.mwmmapsupdater.Consts;
import ru.p3tr0vich.mwmmapsupdater.exceptions.CancelledException;
import ru.p3tr0vich.mwmmapsupdater.exceptions.InternetException;
import ru.p3tr0vich.mwmmapsupdater.models.FileInfo;
import ru.p3tr0vich.mwmmapsupdater.models.MapFiles;
import ru.p3tr0vich.mwmmapsupdater.utils.Utils;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsFiles;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class MapFilesServerHelper {

    private static final String TAG = "MapFilesServerHelper";

    private static final boolean LOG_ENABLED = true;

    private static final boolean DEBUG_DUMMY_FILE_INFO = false;
    private static final boolean DEBUG_DUMMY_DOWNLOAD = false;
    private static final boolean DEBUG_DUMMY_DOWNLOAD_LOG_PROGRESS = false;
    private static final boolean DEBUG_RETURN_CURRENT_DATE = false;
    private static final boolean DEBUG_DOWNLOAD_WAIT_ENABLED = true;
    private static final boolean DEBUG_DOWNLOAD_LOG_PROGRESS = true;
    private static final boolean DEBUG_FILE_INFO_WAIT_ENABLED = false;
    private static final boolean DEBUG_FILE_INFO_LIST = false;

    private static final String PROTOCOL = "http";
    private static final String HOST = "direct.mapswithme.com";
    private static final String PATH = "regular/daily";

    private static final int CONNECT_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);

    private static final int BUFFER_LENGTH = 4 * 1024;

    private static final int MAX_DOWNLOAD_ATTEMPT_COUNT = 3;

    private MapFilesServerHelper() {
    }

    public interface OnCancelled {
        void checkCancelled() throws CancelledException;
    }

    public interface OnDownloadProgress {
        void onStart();

        void onMapStart(@NonNull String mapName, int i, int count);

        void onProgress(int progress);

        void onEnd();
    }

    @NonNull
    private static URL getUrl(@NonNull String mapName) {
        URL url = null;

        String path = PATH + '/' + mapName + Consts.MAP_FILE_NAME_EXT;

        try {
            url = new URL(PROTOCOL, HOST, path);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            UtilsLog.e(TAG, "getUrl", "url == null from protocol == " + PROTOCOL + ", host == " + HOST + ", path == " + path);
        }

        assert url != null;

        return url;
    }

    @NonNull
    private static File getDownloadFile(@NonNull String mapName) {
        return new File(FilesHelper.getDownloadDir(), mapName + Consts.MAP_FILE_NAME_EXT);
    }

    @Nullable
    private static FileInfo getFileInfo(@NonNull String mapName) throws InternetException {
        if (BuildConfig.DEBUG && DEBUG_FILE_INFO_WAIT_ENABLED) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long lastModified;

        if (BuildConfig.DEBUG && DEBUG_DUMMY_FILE_INFO) {
            Random rand = new Random();

            lastModified = -946771200000L + (Math.abs(rand.nextLong()) % (70L * 365 * 24 * 60 * 60 * 1000));
        } else {
            URL url = getUrl(mapName);

            HttpURLConnection connection;
            try {
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(CONNECT_TIMEOUT);

                connection.connect();
            } catch (IOException e) {
                throw new InternetException(e.getMessage());
            }

            lastModified = connection.getLastModified();

            connection.disconnect();
        }

        if (lastModified == 0) {
            UtilsLog.e(TAG, "getFileInfo", "lastModified == 0");
            return null;
        }

        FileInfo fileInfo = new FileInfo(mapName);
        fileInfo.setTimestamp(lastModified);

        return fileInfo;
    }

    @NonNull
    private static List<FileInfo> getFileInfoList(@NonNull List<String> mapNames) throws InternetException {
        List<FileInfo> fileInfoList = new ArrayList<>();

        UtilsLog.d(LOG_ENABLED, TAG, "getFileInfoList", "start");

        for (String mapName : mapNames) {
            FileInfo fileInfo = getFileInfo(mapName);

            UtilsLog.d(DEBUG_FILE_INFO_LIST, TAG, "getFileInfoList", "fileInfo == " + fileInfo);

            if (fileInfo != null) {
                fileInfoList.add(fileInfo);
            }
        }

        UtilsLog.d(LOG_ENABLED, TAG, "getFileInfoList", "end");

        return fileInfoList;
    }

    public static long getServerFilesTimestamp(@NonNull MapFiles mapFiles) throws InternetException {
        if (BuildConfig.DEBUG && DEBUG_RETURN_CURRENT_DATE) {
            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            return calendar.getTime().getTime();
        }

        if (BuildConfig.DEBUG && DEBUG_DUMMY_FILE_INFO) {
            Random rand = new Random();

            if (rand.nextInt(100) < 10) {
                return Consts.BAD_DATETIME;
            }
        }

        List<FileInfo> fileInfoList = mapFiles.getFileList();

        List<String> mapNames = new ArrayList<>();

        for (FileInfo fileInfo : fileInfoList) {
            mapNames.add(fileInfo.getMapName());
        }

        fileInfoList = getFileInfoList(mapNames);

        return MapFilesHelper.getLatestTimestamp(fileInfoList);
    }

    private static int inputRead(InputStream input, byte b[]) throws InternetException {
        try {
            return input.read(b);
        } catch (IOException e) {
            throw new InternetException(e.getMessage());
        }
    }

    private static void outputWrite(OutputStream output, byte data[], int len) throws IOException {
        output.write(data, 0, len);
    }

    private static File download(@NonNull String mapName, @NonNull OnCancelled onCancelled, @NonNull OnDownloadProgress onDownloadProgress) throws IOException, CancelledException {
        UtilsLog.d(LOG_ENABLED, TAG, "download", "start, mapName == " + mapName);

        long fileLength;

        int length;

        long total = 0;

        int progress;

        try {
            File file = getDownloadFile(mapName);

            if (BuildConfig.DEBUG && DEBUG_DUMMY_DOWNLOAD) {
                Random random = new Random();

                fileLength = 1024 + random.nextInt(1024);

                UtilsLog.d(LOG_ENABLED, TAG, "download", "fileLength == " + fileLength);

                do {
                    onCancelled.checkCancelled();

                    length = 10 + random.nextInt(90);

                    total += length;

                    progress = (int) (total * 100 / fileLength);

                    if (progress > 100) progress = 100;

                    if (BuildConfig.DEBUG && DEBUG_DOWNLOAD_WAIT_ENABLED) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (DEBUG_DUMMY_DOWNLOAD_LOG_PROGRESS) {
                        UtilsLog.d(LOG_ENABLED, TAG, "download",
                                "length == " + length + ", total == " + total + ", progress == " + progress);
                    }

                    onDownloadProgress.onProgress(progress);
                } while (total <= fileLength);

                UtilsFiles.createFile(file);

                return file;
            } // DEBUG_DUMMY_DOWNLOAD end

            URL url = getUrl(mapName);

            HttpURLConnection connection;

            InputStream input;

            try {
                connection = (HttpURLConnection) url.openConnection();

                connection.setConnectTimeout(CONNECT_TIMEOUT);

                connection.connect();

                fileLength = connection.getContentLength();

                UtilsLog.d(LOG_ENABLED, TAG, "download", "fileLength == " + fileLength);

                input = new BufferedInputStream(connection.getInputStream());
            } catch (IOException e) {
                throw new InternetException(e.getMessage());
            }

            File fileDownloadInProgress = new File(file.getAbsolutePath() + ".downloading");

            OutputStream output = new FileOutputStream(fileDownloadInProgress);

            try {
                byte data[] = new byte[BUFFER_LENGTH];

                int currentProgress = 0;

                while ((length = inputRead(input, data)) != -1) {
                    onCancelled.checkCancelled();

                    total += length;

                    progress = (int) (total * 100 / fileLength);

                    if (DEBUG_DOWNLOAD_LOG_PROGRESS) {
                        UtilsLog.d(LOG_ENABLED, TAG, "download", "length == " + length + ", progress == " + progress);
                    }

                    if (progress != currentProgress) {
                        currentProgress = progress;
                        onDownloadProgress.onProgress(progress);
                    }

                    outputWrite(output, data, length);
                }
            } finally {
                output.flush();

                output.close();
            }

            input.close();

            connection.disconnect();

            UtilsFiles.rename(fileDownloadInProgress, file);

            return file;
        } finally {
            UtilsLog.d(LOG_ENABLED, TAG, "download", "end");
        }
    }

    private interface JsonFields {
        String FILES = "files";

        String FILE_NAME = "name";
        String FILE_STATUS = "status";
        String FILE_TIMESTAMP = "timestamp";
    }

    private interface JsonValuesStatus {
        String WAITING = "waiting";
        String DOWNLOADING = "downloading";
        String DOWNLOADED = "download";
    }

    public static void downloadMaps(Context context, @NonNull MapFiles mapFiles, @NonNull OnCancelled onCancelled, @NonNull OnDownloadProgress onDownloadProgress) throws IOException, CancelledException {
        UtilsLog.d(LOG_ENABLED, TAG, "downloadMaps", "map count == " + mapFiles.getFileList().size());

        onDownloadProgress.onStart();

        File downloadInfoFile = FilesHelper.getDownloadInfoFile(context);

        File downloadDir = FilesHelper.getDownloadDir();

        File mapFile;

        UtilsFiles.makeDir(downloadDir);

        UtilsFiles.recursiveDeleteInDirectory(downloadDir);

        List<FileInfo> fileInfoList = mapFiles.getFileList();

        int i = 0, count = fileInfoList.size(), attemptNum;

        JSONObject json = new JSONObject();
        JSONArray files = new JSONArray();

        JSONObject fileInfoObject;

        for (FileInfo fileInfo : fileInfoList) {
            fileInfoObject = new JSONObject();
            try {
                fileInfoObject.put(JsonFields.FILE_NAME, fileInfo.getMapName());
                fileInfoObject.put(JsonFields.FILE_STATUS, JsonValuesStatus.WAITING);

                files.put(fileInfoObject);
            } catch (JSONException e) {
                UtilsLog.e(TAG, "downloadMaps", e);

                throw new IOException(e);
            }
        }

        long lastModified;

        for (FileInfo fileInfo : fileInfoList) {
            String mapName = fileInfo.getMapName();

            onCancelled.checkCancelled();

            onDownloadProgress.onMapStart(mapName, i, count);

            attemptNum = 0;
            do {
                try {
                    fileInfoObject = files.getJSONObject(i);

                    fileInfoObject.put(JsonFields.FILE_STATUS, JsonValuesStatus.DOWNLOADING);

                    files.put(i, fileInfoObject);

                    json.put(JsonFields.FILES, files);

                    UtilsFiles.writeJSON(downloadInfoFile, json);

//                  ***
                    mapFile = download(mapName, onCancelled, onDownloadProgress);
//                  ***

                    attemptNum = MAX_DOWNLOAD_ATTEMPT_COUNT;

                    fileInfoObject.put(JsonFields.FILE_STATUS, JsonValuesStatus.DOWNLOADED);
                    fileInfoObject.put(JsonFields.FILE_TIMESTAMP, mapFile.lastModified());

                    files.put(i, fileInfoObject);

                    json.put(JsonFields.FILES, files);

                    UtilsFiles.writeJSON(downloadInfoFile, json);
                } catch (JSONException e) {
                    UtilsLog.e(TAG, "downloadMaps", e);

                    throw new IOException(e);
                } catch (InternetException e) {
                    attemptNum++;

                    UtilsLog.e(TAG, "downloadMaps", e);

                    if (attemptNum == MAX_DOWNLOAD_ATTEMPT_COUNT) {
                        throw new InternetException(e.getMessage());
                    }
                }
            } while (attemptNum < MAX_DOWNLOAD_ATTEMPT_COUNT);

            i++;
        }

        onDownloadProgress.onEnd();
    }
}