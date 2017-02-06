package ru.p3tr0vich.mwmmapsupdater;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import ru.p3tr0vich.mwmmapsupdater.Models.MapItem;
import ru.p3tr0vich.mwmmapsupdater.Models.MapVersion;
import ru.p3tr0vich.mwmmapsupdater.dummy.DummyMapItems;
import ru.p3tr0vich.mwmmapsupdater.dummy.DummyMapVersion;

public class FragmentMain extends FragmentBase {

    private static final DateFormat DATE_FORMAT = SimpleDateFormat.getDateInstance(DateFormat.SHORT);

    private TextView mTextDateLocal;
    private TextView mTextDateServer;

    private OnListFragmentInteractionListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

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

        return view;
    }

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
}