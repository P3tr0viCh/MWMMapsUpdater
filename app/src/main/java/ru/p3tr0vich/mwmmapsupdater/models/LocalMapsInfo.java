package ru.p3tr0vich.mwmmapsupdater.models;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.p3tr0vich.mwmmapsupdater.utils.UtilsFiles;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class LocalMapsInfo {

    private static final String TAG = "LocalMapsInfo";

    private static final boolean LOG_ENABLED = true;

    private static final String MAPS_INFO_FILE_NAME = "maps_info.json";

    private static final String JSON_TIMESTAMP = "timestamp";
    private static final String JSON_MAP_SUB_DIR = "directory";
    private static final String JSON_FILES = "files";
    private static final String JSON_FILE_NAME = "name";
    private static final String JSON_FILE_TIMESTAMP = "timestamp";

    private Context mContext;

    private long mTimeStamp;

    private String mMapSubDirName;

    private List<FileInfo> mFileInfoList;

    public LocalMapsInfo(@NonNull Context context) {
        mContext = context;
        mFileInfoList = new ArrayList<>();
        clear();
    }

    public LocalMapsInfo(@NonNull Context context, @NonNull String mapSubDirName, @NonNull List<FileInfo> fileInfoList) {
        this(context);

        mMapSubDirName = mapSubDirName;

        mTimeStamp = mapDirNameToDate(mapSubDirName).getTime();

        mFileInfoList.addAll(fileInfoList);
    }

    private void clear() {
        mTimeStamp = 0;
        mMapSubDirName = "";
        mFileInfoList.clear();
    }

    @NonNull
    private static Date mapDirNameToDate(@NonNull String mapDirName) {
        if (!TextUtils.isEmpty(mapDirName)) {
            try {
                // mapSubDir == '171232' ==> '180101';
                return new SimpleDateFormat("yyMMdd", Locale.US).parse(mapDirName);
            } catch (ParseException e) {
                e.printStackTrace();
                UtilsLog.e(TAG, "mapDirNameToDate", e);
            }
        }

        return new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalMapsInfo that = (LocalMapsInfo) o;

        return mMapSubDirName.equals(that.mMapSubDirName) && mFileInfoList.equals(that.mFileInfoList);
    }

    @NonNull
    private static File getFile(@NonNull Context context) {
        return new File(context.getFilesDir(), MAPS_INFO_FILE_NAME);
    }

    @NonNull
    public Date getDate() {
        return new Date(mTimeStamp);
    }

    public boolean readFromJSONFile() {
        clear();

        File file = getFile(mContext);

        try {
            UtilsFiles.checkExists(file);

            JSONObject json = UtilsFiles.readJSON(file);

            UtilsLog.d(LOG_ENABLED, TAG, "readFromJSONFile", "json == " + json.toString());

            mTimeStamp = json.getLong(JSON_TIMESTAMP);

            mMapSubDirName = json.getString(JSON_MAP_SUB_DIR);

            JSONArray files = json.getJSONArray(JSON_FILES);

            JSONObject fileInfo;
            for (int i = 0, l = files.length(); i < l; i++) {
                fileInfo = files.getJSONObject(i);
                mFileInfoList.add(new FileInfo(fileInfo.getString(JSON_FILE_NAME), new Date(fileInfo.getLong(JSON_FILE_TIMESTAMP))));
            }

            Collections.sort(mFileInfoList);

            return true;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            UtilsLog.e(TAG, "readFromJSONFile", e);
        }

        return false;
    }

    public boolean writeToJSONFile() {
        JSONObject json = new JSONObject();

        try {
            json.put(JSON_TIMESTAMP, mTimeStamp);
            json.put(JSON_MAP_SUB_DIR, mMapSubDirName);

            JSONArray files = new JSONArray();

            JSONObject fileInfoObject;

            for (FileInfo fileInfo : mFileInfoList) {
                fileInfoObject = new JSONObject();

                fileInfoObject.put(JSON_FILE_NAME, fileInfo.getMapName());
                fileInfoObject.put(JSON_FILE_TIMESTAMP, fileInfo.getDate().getTime());

                files.put(fileInfoObject);
            }

            json.put(JSON_FILES, files);

            UtilsLog.d(LOG_ENABLED, TAG, "writeToJSONFile", "json == " + json.toString());

            File file = getFile(mContext);

            UtilsFiles.writeJSON(file, json);

            return true;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            UtilsLog.e(TAG, "writeToJSONFile", e);
        }

        return false;
    }

    public static void deleteJSONFile(@NonNull Context context) {
        File file = getFile(context);
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }
}