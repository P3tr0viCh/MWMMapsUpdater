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

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import ru.p3tr0vich.mwmmapsupdater.dummy.DummyMapItems;
import ru.p3tr0vich.mwmmapsupdater.dummy.DummyMapVersion;
import ru.p3tr0vich.mwmmapsupdater.models.MapFiles;
import ru.p3tr0vich.mwmmapsupdater.models.MapItem;
import ru.p3tr0vich.mwmmapsupdater.models.MapVersion;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class FragmentMain extends FragmentBase implements LoaderManager.LoaderCallbacks<MapFiles> {

    private static final String TAG = "FragmentMain";

    private static final boolean LOG_ENABLED = true;

    private static final int MAP_FILES_LOADER_ID = 0;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());

    private ViewGroup mLayoutError;
    private ViewGroup mLayoutMain;

    private TextView mTextDateLocal;
    private TextView mTextDateServer;

    private OnListFragmentInteractionListener mListener;

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

        recyclerView.setAdapter(new MapItemRecyclerViewAdapter(DummyMapItems.ITEMS, mListener));

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
    public void onStart() {
        super.onStart();

//        updateMapList();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(MapItem item);
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
        File mapDir = new File(preferencesHelper.getMapsDir());

        switch (item.getItemId()) {
            case R.id.action_main_update:
                getLoaderManager().restartLoader(MAP_FILES_LOADER_ID, null, this);
                break;
            case R.id.action_main_create_mwm_dir:
                if (!mapDir.exists()) {
                    return mapDir.mkdir();
                }
                break;
            case R.id.action_main_delete_mwm_dir:
                if (mapDir.exists()) {
                    return mapDir.delete();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

//    private void updateMapList() {
//        mLayoutError.setVisibility(View.GONE);
//        mLayoutMain.setVisibility(View.GONE);
//
//        String mapDir = preferencesHelper.getMapsDir();
//
//        MapFiles mapFiles = new MapFiles(mapDir);
//
//        @MapFilesFindHelper.Result
//        int result = MapFilesFindHelper.find(mapFiles);
//
//        if (result != MapFilesFindHelper.RESULT_OK) {
//            updateError(mapDir);
//
//            mLayoutError.setVisibility(View.VISIBLE);
//        } else {
//            mLayoutMain.setVisibility(View.VISIBLE);
//        }
//    }

    @Override
    public Loader<MapFiles> onCreateLoader(int id, Bundle args) {
        if (LOG_ENABLED) {
            UtilsLog.d(TAG, "onCreateLoader");
        }

        return new MapFilesLoader(getContext(), preferencesHelper.getMapsDir());
    }

    @Override
    public void onLoadFinished(Loader<MapFiles> loader, MapFiles data) {
        if (LOG_ENABLED) {
            UtilsLog.d(TAG, "onLoadFinished");
        }

        String mapDir = "";

        if (data != null) {
            mapDir = data.getMapDir();

            if (mapDir != null && !mapDir.isEmpty()) {
                String mapSubDir = data.getMapSubDir();

                if (mapSubDir != null && !mapSubDir.isEmpty()) {
                    mLayoutMain.setVisibility(View.VISIBLE);
                    mLayoutError.setVisibility(View.GONE);

                    return;
                }
            }
        }

        updateError(mapDir);
        mLayoutMain.setVisibility(View.GONE);
        mLayoutError.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<MapFiles> loader) {
//
    }
}