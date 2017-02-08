package ru.p3tr0vich.mwmmapsupdater;

import android.content.Context;
import android.os.Bundle;
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

import ru.p3tr0vich.mwmmapsupdater.Models.MapFiles;
import ru.p3tr0vich.mwmmapsupdater.Models.MapItem;
import ru.p3tr0vich.mwmmapsupdater.Models.MapVersion;
import ru.p3tr0vich.mwmmapsupdater.dummy.DummyMapItems;
import ru.p3tr0vich.mwmmapsupdater.dummy.DummyMapVersion;
import ru.p3tr0vich.mwmmapsupdater.helpers.MapFilesFindHelper;

public class FragmentMain extends FragmentBase {

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
                updateMapList();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        updateMapList();
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
                updateMapList();
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

    private void updateMapList() {
        mLayoutError.setVisibility(View.GONE);
        mLayoutMain.setVisibility(View.GONE);

        String mapDir = preferencesHelper.getMapsDir();

        MapFiles mapFiles = new MapFiles(mapDir);

        @MapFilesFindHelper.Result
        int result = MapFilesFindHelper.find(mapFiles);

        if (result != MapFilesFindHelper.RESULT_OK) {
            updateError(mapDir);

            mLayoutError.setVisibility(View.VISIBLE);
        } else {
            mLayoutMain.setVisibility(View.VISIBLE);
        }
    }
}