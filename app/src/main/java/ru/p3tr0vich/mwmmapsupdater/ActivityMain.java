package ru.p3tr0vich.mwmmapsupdater;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import ru.p3tr0vich.mwmmapsupdater.helpers.FragmentHelper;
import ru.p3tr0vich.mwmmapsupdater.models.MapItem;

public class ActivityMain extends AppCompatActivity implements
        FragmentMain.OnListFragmentInteractionListener,
        FragmentInterface.OnFragmentChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentHelper fragmentHelper = new FragmentHelper(this);

        if (savedInstanceState == null) {
            fragmentHelper.addMainFragment();
        }
    }

    @Override
    public void onListFragmentInteraction(MapItem item) {
        if (BuildConfig.DEBUG) {
            Toast.makeText(this, item.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFragmentChange(FragmentInterface fragment) {

    }
}