package ru.p3tr0vich.mwmmapsupdater;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ru.p3tr0vich.mwmmapsupdater.adapters.MapItemRecyclerViewAdapter;
import ru.p3tr0vich.mwmmapsupdater.broadcastreceivers.BroadcastReceiverMapFilesLoading;
import ru.p3tr0vich.mwmmapsupdater.helpers.ConnectivityHelper;
import ru.p3tr0vich.mwmmapsupdater.helpers.ContentResolverHelper;
import ru.p3tr0vich.mwmmapsupdater.models.FileInfo;
import ru.p3tr0vich.mwmmapsupdater.models.MapFiles;
import ru.p3tr0vich.mwmmapsupdater.models.MapItem;
import ru.p3tr0vich.mwmmapsupdater.observers.SyncProgressObserver;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsFiles;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

import static ru.p3tr0vich.mwmmapsupdater.utils.Utils.toast;

public class FragmentMain extends FragmentBase implements
        LoaderManager.LoaderCallbacks<MapFiles>,
        SyncStatusObserver {

    private static final String TAG = "FragmentMain";

    private static final boolean LOG_ENABLED = true;

    private static final int MAP_FILES_LOADER_ID = 0;

    private static final long RECHECK_SERVER_MILLIS = BuildConfig.DEBUG ?
            TimeUnit.SECONDS.toMillis(10) : TimeUnit.MINUTES.toMillis(10);

    private static final String KEY_DATE_LOCAL = "KEY_DATE_LOCAL";
    private static final String KEY_DATE_SERVER = "KEY_DATE_SERVER";
    private static final String KEY_CHECK_SERVER_DATE_TIME = "KEY_CHECK_SERVER_DATE_TIME";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());

    private ViewGroup mLayoutError;
    private ViewGroup mLayoutMain;

    private TextView mTextDateLocal;
    private TextView mTextDateServer;

    private ImageView mImgCheckServer;
    private Animation mAnimationCheckServer;

    private Date mDateLocal;
    private Date mDateServer;
    private long mCheckServerDateTime;

    private MapItemRecyclerViewAdapter mMapItemRecyclerViewAdapter;

    private OnListFragmentInteractionListener mListener;

    private BroadcastReceiverMapFilesLoading mBroadcastReceiverMapFilesLoading;

    private SyncProgressObserver mSyncProgressObserver;

    private AppAccount mAppAccount;
    private Object mSyncMonitor;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new ImplementException(context, OnListFragmentInteractionListener.class);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UtilsLog.d(LOG_ENABLED, TAG, "onCreate", "savedInstanceState " + (savedInstanceState == null ? "=" : "!") + "= null");

        mAppAccount = new AppAccount(getContext());

        initAnimationCheckServer();

        initMapFilesLoadingStatusReceiver();
        initSyncProgressObserver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        UtilsLog.d(LOG_ENABLED, TAG, "onCreateView", "savedInstanceState " + (savedInstanceState == null ? "=" : "!") + "= null");

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        setHasOptionsMenu(true);

        mLayoutError = (ViewGroup) view.findViewById(R.id.layout_error);
        mLayoutMain = (ViewGroup) view.findViewById(R.id.layout_main);

        mTextDateLocal = (TextView) view.findViewById(R.id.text_date_local);
        mTextDateServer = (TextView) view.findViewById(R.id.text_date_server);

        mImgCheckServer = (ImageView) view.findViewById(R.id.image_check_server);

        Context context = view.getContext();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        recyclerView.setAdapter(mMapItemRecyclerViewAdapter = new MapItemRecyclerViewAdapter(mListener));

        view.findViewById(R.id.btn_check_server).setOnClickListener(mBtnCheckServerClickListener);
        view.findViewById(R.id.btn_retry_find_maps).setOnClickListener(mBtnRetryFindMapsClickListener);

        mLayoutError.setVisibility(View.GONE);
        mLayoutMain.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        UtilsLog.d(LOG_ENABLED, TAG, "onActivityCreated", "savedInstanceState " + (savedInstanceState == null ? "=" : "!") + "= null");

        getLoaderManager().initLoader(MAP_FILES_LOADER_ID, null, this);

        if (savedInstanceState == null) {
            long dateLocal = preferencesHelper.getDateLocal();
            long dateServer = preferencesHelper.getDateServer();

            mDateLocal = dateLocal == Consts.BAD_DATETIME ? null : new Date(dateLocal);
            mDateServer = dateServer == Consts.BAD_DATETIME ? null : new Date(dateServer);

            mCheckServerDateTime = preferencesHelper.getCheckServerDateTime();
        } else {
            mDateLocal = (Date) savedInstanceState.getSerializable(KEY_DATE_LOCAL);
            mDateServer = (Date) savedInstanceState.getSerializable(KEY_DATE_SERVER);
            mCheckServerDateTime = savedInstanceState.getLong(KEY_CHECK_SERVER_DATE_TIME);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        UtilsLog.d(LOG_ENABLED, TAG, "onStart");

        mSyncMonitor = ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE, this);

        if (((System.currentTimeMillis() - mCheckServerDateTime) > RECHECK_SERVER_MILLIS) || mDateServer == null) {
            ContentResolverHelper.requestSync(mAppAccount);
        } else {
            updateDateServer(mDateServer);
        }
    }

    @Override
    public void onStop() {
        UtilsLog.d(LOG_ENABLED, TAG, "onStop");

        ContentResolver.removeStatusChangeListener(mSyncMonitor);

        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(KEY_DATE_LOCAL, mDateLocal);
        outState.putSerializable(KEY_DATE_SERVER, mDateServer);
        outState.putLong(KEY_CHECK_SERVER_DATE_TIME, mCheckServerDateTime);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        mSyncProgressObserver.unregister(getContext());

        mBroadcastReceiverMapFilesLoading.unregister(getContext());

        super.onDestroy();
    }

    private void initAnimationCheckServer() {
        mAnimationCheckServer = new RotateAnimation(360.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mAnimationCheckServer.setInterpolator(new LinearInterpolator());
        mAnimationCheckServer.setDuration(getResources().getInteger(R.integer.animation_duration_check_server));
        mAnimationCheckServer.setRepeatCount(Animation.INFINITE);
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(MapItem item);
    }

    private void initMapFilesLoadingStatusReceiver() {
        mBroadcastReceiverMapFilesLoading = new BroadcastReceiverMapFilesLoading() {
            @Override
            public void onReceive(boolean loading) {
            }
        };
        mBroadcastReceiverMapFilesLoading.register(getContext());
    }

    private void initSyncProgressObserver() {
        mSyncProgressObserver = new SyncProgressObserver() {
            @Override
            public void onCheckServerDateTime(long dateTime) {
                mCheckServerDateTime = dateTime;
            }

            @Override
            public void onDateChecked(@Nullable Date date) {
                mDateServer = date;
                updateDateServer(date);
            }
        };
        mSyncProgressObserver.register(getContext());
    }

    private final View.OnClickListener mBtnCheckServerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (ContentResolverHelper.isSyncActive(mAppAccount)) {
                toast(getContext(), R.string.message_check_server_active);
            } else {
                if (ConnectivityHelper.getConnectedState(getContext()) != ConnectivityHelper.DISCONNECTED) {
                    ContentResolverHelper.requestSync(mAppAccount);
                } else {
                    toast(getContext(), R.string.message_error_no_internet);
                }
            }
        }
    };

    private final View.OnClickListener mBtnRetryFindMapsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getLoaderManager().restartLoader(MAP_FILES_LOADER_ID, null, FragmentMain.this);
        }
    };

    private void updateTextDate(@NonNull TextView textView, @Nullable Date date) {
        textView.setText(date != null ? DATE_FORMAT.format(date) : getString(R.string.text_error_date_server_null));
    }

    private void updateDateLocal(@Nullable Date date) {
        updateTextDate(mTextDateLocal, date);
    }

    private void updateDateServer(@Nullable Date date) {
        updateTextDate(mTextDateServer, date);
    }

    private void updateError(String mapDir) {
        TextView textView = (TextView) mLayoutError.findViewById(R.id.text_date_mwm_maps_not_found_description);
        textView.setText(getString(R.string.text_error_mwm_maps_not_found_path, mapDir));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (BuildConfig.DEBUG) {
            inflater.inflate(R.menu.menu_main, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = false;

        File mapDir = new File(preferencesHelper.getParentMapsDir());

        File[] listFiles;

        String mapSubDirName;

        Random random = new Random();

        int i;

        switch (item.getItemId()) {
            case R.id.action_main_update:
                getLoaderManager().restartLoader(MAP_FILES_LOADER_ID, null, this);
                break;
            case R.id.action_main_create_mwm_dir:
                result = mapDir.mkdir();

                break;
            case R.id.action_main_delete_mwm_dir:
                result = UtilsFiles.recursiveDelete(mapDir);

                break;
            case R.id.action_main_create_mwm_sub_dir:
                mapSubDirName = "17";

                i = 1 + random.nextInt(12);
                if (i < 10) {
                    mapSubDirName += "0";
                }
                mapSubDirName += String.valueOf(i);

                i = 1 + random.nextInt(29);
                if (i < 10) {
                    mapSubDirName += "0";
                }
                mapSubDirName += String.valueOf(i);

                UtilsLog.d(LOG_ENABLED, TAG, "create sub dir", mapSubDirName);

                File mapSubDir = new File(mapDir, mapSubDirName);

                result = mapSubDir.mkdirs();

                break;
            case R.id.action_main_delete_all_mwm_sub_dirs:
                result = true;

                listFiles = mapDir.listFiles();

                if (listFiles != null) {
                    for (File file : listFiles) {
                        if (result && file.isDirectory()) {
                            UtilsLog.d(LOG_ENABLED, TAG, "delete sub dir", file.getName());

                            result = UtilsFiles.recursiveDelete(file);
                        }
                    }
                }

                break;
            case R.id.action_main_create_files:
            case R.id.action_main_create_random_files:
                listFiles = mapDir.listFiles();

                List<String> subDirNamesList = new ArrayList<>();

                if (listFiles != null) {
                    for (File file : listFiles) {
                        if (file.isDirectory()) {
                            String fileName = file.getName();

                            if (fileName.matches("\\d{6}")) {
                                subDirNamesList.add(fileName);
                            }
                        }
                    }
                }

                if (subDirNamesList.isEmpty()) {
                    mapSubDirName = "17";

                    i = 1 + random.nextInt(12);
                    if (i < 10) {
                        mapSubDirName += "0";
                    }
                    mapSubDirName += String.valueOf(i);

                    i = 1 + random.nextInt(29);
                    if (i < 10) {
                        mapSubDirName += "0";
                    }
                    mapSubDirName += String.valueOf(i);

                    UtilsLog.d(LOG_ENABLED, TAG, "create sub dir", mapSubDirName);

                    mapSubDir = new File(mapDir, mapSubDirName);

                    result = mapSubDir.mkdirs();
                } else {
                    Collections.sort(subDirNamesList, new Comparator<String>() {
                        @Override
                        public int compare(String o1, String o2) {
                            return o2.compareTo(o1);
                        }
                    });

                    mapSubDirName = subDirNamesList.get(0);

                    UtilsLog.d(LOG_ENABLED, TAG, "use existing sub dir", mapSubDirName);

                    mapSubDir = new File(mapDir, mapSubDirName);

                    result = true;
                }

                if (result) {
                    if (item.getItemId() == R.id.action_main_create_random_files) {
                        File file;
                        for (i = 0; i < 5; i++) {
                            file = new File(mapSubDir, String.valueOf(i) + ".mwm");
                            try {
                                result = file.createNewFile();
                            } catch (IOException e) {
                                result = false;
                            }

                            if (!result) {
                                break;
                            }
                        }
                        for (i = 0; i < 5; i++) {
                            file = new File(mapSubDir, String.valueOf(random.nextLong()) + ".mwm");
                            try {
                                result = file.createNewFile();
                            } catch (IOException e) {
                                result = false;
                            }

                            if (!result) {
                                break;
                            }
                        }
                        for (i = 0; i < 5; i++) {
                            file = new File(mapSubDir, String.valueOf(random.nextLong()) + ".mwm2");
                            try {
                                result = file.createNewFile();
                            } catch (IOException e) {
                                result = false;
                            }

                            if (!result) {
                                break;
                            }
                        }
                        for (i = 5; i < 10; i++) {
                            file = new File(mapSubDir, String.valueOf(i));
                            try {
                                result = file.createNewFile();
                            } catch (IOException e) {
                                result = false;
                            }

                            if (!result) {
                                break;
                            }
                        }
                    } else {
                        File file1 = new File(mapSubDir, random.nextBoolean() ? "Anguilla.mwm" : "Andorra.mwm");
                        File file2 = new File(mapSubDir, random.nextBoolean() ? "Tuvalu.mwm" : "Tokelau.mwm");
                        File file3 = new File(mapSubDir, random.nextBoolean() ? "Spain_Ceuta.mwm" : "British Indian Ocean Territory.mwm");

                        if (UtilsFiles.recursiveDeleteInDirectory(mapSubDir)) {
                            UtilsLog.d(LOG_ENABLED, TAG, "existing files deleted in", mapSubDir.getAbsolutePath());
                        }

                        try {
                            result = file1.createNewFile() &&
                                    file2.createNewFile() &&
                                    file3.createNewFile();

                            if (result) {
                                UtilsLog.d(LOG_ENABLED, TAG, "created files",
                                        "[" + file1.getName() + ", " + file2.getName() + ", " + file3.getName() + "]");
                            }
                        } catch (IOException e) {
                            result = false;
                        }
                    }
                }

                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return result;
    }

    @Override
    public Loader<MapFiles> onCreateLoader(int id, Bundle args) {
        UtilsLog.d(LOG_ENABLED, TAG, "onCreateLoader");

        return new MapFilesLoader(getContext(), preferencesHelper.getParentMapsDir());
    }

    @Nullable
    private JSONObject getMapNamesAndDescriptions() {
        String json = "";

        try {
            String language = Locale.getDefault().getLanguage();

            if (!"ru".equals(language)) {
                language = "en";
            }

            InputStream inputStream = getResources().getAssets().open(getString(R.string.countries_strings_json, language));

            int size = inputStream.available();

            byte[] buffer = new byte[size];

            //noinspection ResultOfMethodCallIgnored
            inputStream.read(buffer);

            inputStream.close();

            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            UtilsLog.e(TAG, "getMapNamesAndDescriptions", e);
        }

        JSONObject jsonObject = null;

        if (!json.isEmpty()) {
            try {
                jsonObject = new JSONObject(json);
            } catch (JSONException e) {
                e.printStackTrace();
                UtilsLog.e(TAG, "getMapNamesAndDescriptions", e);
            }
        }

        return jsonObject;
    }

    @Override
    public void onLoadFinished(Loader<MapFiles> loader, MapFiles data) {
        String mapDir = null;

        UtilsLog.d(LOG_ENABLED, TAG, "onLoadFinished", "data == " + data);

        if (data != null) {
            mapDir = data.getMapDir();

            List<FileInfo> fileInfoList = data.getFileList();

            if (!fileInfoList.isEmpty()) {

                long timeStamp = data.getTimestamp();

                mDateLocal = timeStamp == Consts.BAD_DATETIME ? null : new Date(timeStamp);
                updateDateLocal(mDateLocal);
                preferencesHelper.putDateLocal(mDateLocal.getTime());

                List<MapItem> mapItems = new ArrayList<>();

                JSONObject namesAndDescriptions = getMapNamesAndDescriptions();

                for (FileInfo fileInfo : fileInfoList) {
                    String mapName = fileInfo.getMapName();

                    String name = mapName;
                    String description = null;

                    if (namesAndDescriptions != null) {
                        try {
                            name = namesAndDescriptions.getString(name);
                            description = namesAndDescriptions.getString(mapName + " Description");
                        } catch (JSONException e) {
                            UtilsLog.e(TAG, "onLoadFinished", e);
                        }
                    }

                    mapItems.add(new MapItem(mapName, name, description));
                }

                mMapItemRecyclerViewAdapter.swapItems(mapItems);

                mLayoutError.setVisibility(View.GONE);
                mLayoutMain.setVisibility(View.VISIBLE);

                return;
            }
        }

        // data == null or file list empty

        preferencesHelper.putDateLocal(Consts.BAD_DATETIME); // TODO: delete?
        preferencesHelper.putDateServer(Consts.BAD_DATETIME);

        mMapItemRecyclerViewAdapter.notifyItemRangeRemoved(0, mMapItemRecyclerViewAdapter.getItemCount());

        mMapItemRecyclerViewAdapter.swapItems(null);

        if (TextUtils.isEmpty(mapDir)) {
            mapDir = getString(R.string.text_error_mwm_maps_empty_path);
        }
        updateError(mapDir);

        mLayoutMain.setVisibility(View.GONE);
        mLayoutError.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<MapFiles> loader) {
        mMapItemRecyclerViewAdapter.swapItems(null);
    }

    private void updateCheckServerStatus() {
        if (ContentResolverHelper.isSyncActive(mAppAccount)) {
            mImgCheckServer.startAnimation(mAnimationCheckServer);
        } else {
            mImgCheckServer.clearAnimation();
        }
    }

    @Override
    public void onStatusChanged(int which) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateCheckServerStatus();
            }
        });
    }
}