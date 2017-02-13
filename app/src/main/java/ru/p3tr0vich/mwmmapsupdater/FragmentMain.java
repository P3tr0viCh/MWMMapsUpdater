package ru.p3tr0vich.mwmmapsupdater;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import ru.p3tr0vich.mwmmapsupdater.broadcastreceivers.BroadcastReceiverMapFilesLoading;
import ru.p3tr0vich.mwmmapsupdater.dummy.DummyMapVersion;
import ru.p3tr0vich.mwmmapsupdater.models.MapFiles;
import ru.p3tr0vich.mwmmapsupdater.models.MapItem;
import ru.p3tr0vich.mwmmapsupdater.models.MapVersion;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsFiles;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class FragmentMain extends FragmentBase implements LoaderManager.LoaderCallbacks<MapFiles> {

    private static final String TAG = "FragmentMain";

    private static final boolean LOG_ENABLED = true;

    private static final int MAP_FILES_LOADER_ID = 0;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
    private static final DateFormat MAP_SUB_DIR_DATE_FORMAT = new SimpleDateFormat("yyMMdd", Locale.getDefault());

    private ViewGroup mLayoutError;
    private ViewGroup mLayoutMain;

    private TextView mTextDateLocal;
    private TextView mTextDateServer;

    private MapItemRecyclerViewAdapter mMapItemRecyclerViewAdapter;

    private OnListFragmentInteractionListener mListener;

    private BroadcastReceiverMapFilesLoading mBroadcastReceiverMapFilesLoading;

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

        if (LOG_ENABLED) {
            UtilsLog.d(TAG, "onCreate", "savedInstanceState " + (savedInstanceState == null ? "=" : "!") + "= null");
        }

        initMapFilesLoadingStatusReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        setHasOptionsMenu(true);

        mLayoutError = (ViewGroup) view.findViewById(R.id.layout_error);
        mLayoutMain = (ViewGroup) view.findViewById(R.id.layout_main);

        mTextDateLocal = (TextView) view.findViewById(R.id.text_date_local);
        mTextDateServer = (TextView) view.findViewById(R.id.text_date_server);

        Context context = view.getContext();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        recyclerView.setAdapter(mMapItemRecyclerViewAdapter = new MapItemRecyclerViewAdapter(mListener));

        updateVersions(DummyMapVersion.VERSION);

        view.findViewById(R.id.btn_retry_find_maps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLoaderManager().restartLoader(MAP_FILES_LOADER_ID, null, FragmentMain.this);
            }
        });

        mLayoutError.setVisibility(View.GONE);
        mLayoutMain.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (LOG_ENABLED) {
            UtilsLog.d(TAG, "onActivityCreated", "savedInstanceState " + (savedInstanceState == null ? "=" : "!") + "= null");
        }

        getLoaderManager().initLoader(MAP_FILES_LOADER_ID, null, this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        mBroadcastReceiverMapFilesLoading.unregister(getContext());

        super.onDestroy();
    }

    interface OnListFragmentInteractionListener {
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

    private void updateVersions(MapVersion mapVersion) {
        mTextDateLocal.setText(DATE_FORMAT.format(mapVersion.getDateLocal()));
        mTextDateServer.setText(DATE_FORMAT.format(mapVersion.getDateServer()));
    }

    private void updateError(String mapDir) {
        TextView textView = (TextView) mLayoutError.findViewById(R.id.text_date_mwm_maps_not_found_description);
        textView.setText(getString(R.string.text_error_mwm_maps_not_found_path, mapDir));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = false;

        File mapDir = new File(preferencesHelper.getMapsDir());

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
                result = UtilsFiles.deleteAll(mapDir);

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

                UtilsLog.d(TAG, "create sub dir", mapSubDirName);

                File mapSubDir = new File(mapDir, mapSubDirName);

                result = mapSubDir.mkdirs();

                break;
            case R.id.action_main_delete_all_mwm_sub_dirs:
                result = true;

                listFiles = mapDir.listFiles();

                if (listFiles != null) {
                    for (File file : listFiles) {
                        if (result && file.isDirectory()) {
                            UtilsLog.d(TAG, "delete sub dir", file.getName());

                            result = UtilsFiles.deleteAll(file);
                        }
                    }
                }

                break;
            case R.id.action_main_create_files:
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

                    UtilsLog.d(TAG, "create sub dir", mapSubDirName);

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

                    UtilsLog.d(TAG, "use existing sub dir", mapSubDirName);

                    mapSubDir = new File(mapDir, mapSubDirName);

                    result = true;
                }

                if (result) {
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
                }

            default:
                return super.onOptionsItemSelected(item);
        }

        return result;
    }

    @Override
    public Loader<MapFiles> onCreateLoader(int id, Bundle args) {
        if (LOG_ENABLED) {
            UtilsLog.d(TAG, "onCreateLoader");
        }

        return new MapFilesLoader(getContext(), preferencesHelper.getMapsDir());
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
        }

        JSONObject jsonObject = null;

        if (!json.isEmpty()) {
            try {
                jsonObject = new JSONObject(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jsonObject;
    }

    @Override
    public void onLoadFinished(Loader<MapFiles> loader, MapFiles data) {
        String mapDir = "<empty>";

        if (LOG_ENABLED) {
            UtilsLog.d(TAG, "onLoadFinished", "data == " + data);
        }

        if (data != null) {
            mapDir = data.getMapDir();

            if (data.getResult() == MapFiles.RESULT_OK) {
                MapVersion mapVersion = new MapVersion();

                Date mapDateLocal = null;
                try {
                    // mapSubDir == '171232' ==> '180101';
                    mapDateLocal = MAP_SUB_DIR_DATE_FORMAT.parse(data.getMapSubDir());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                mapVersion.setDateLocal(mapDateLocal);
                mapVersion.setDateServer(new Date());

                updateVersions(mapVersion);

                List<MapItem> mapItems = new ArrayList<>();

                List<String> fileList = data.getFileList();

                MapItem mapItem;

                if (fileList != null) {
                    JSONObject namesAndDescriptions = getMapNamesAndDescriptions();

                    for (String fileName : fileList) {
                        mapItem = new MapItem(fileName);

                        String name = fileName;
                        String description = null;

                        if (namesAndDescriptions != null) {
                            try {
                                name = namesAndDescriptions.getString(fileName);
                                description = namesAndDescriptions.getString(fileName + " Description");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        mapItem.setName(name);
                        mapItem.setDescription(description);

                        mapItems.add(mapItem);
                    }

                    mMapItemRecyclerViewAdapter.swapItems(mapItems);

                    mLayoutError.setVisibility(View.GONE);
                    mLayoutMain.setVisibility(View.VISIBLE);

                    return;
                }
            }
        }

        mMapItemRecyclerViewAdapter.notifyItemRangeRemoved(0, mMapItemRecyclerViewAdapter.getItemCount());

        mMapItemRecyclerViewAdapter.swapItems(null);

        updateError(mapDir);

        mLayoutMain.setVisibility(View.GONE);
        mLayoutError.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<MapFiles> loader) {
        mMapItemRecyclerViewAdapter.swapItems(null);
    }
}