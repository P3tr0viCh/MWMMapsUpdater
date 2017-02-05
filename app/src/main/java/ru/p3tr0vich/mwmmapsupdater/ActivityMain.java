package ru.p3tr0vich.mwmmapsupdater;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import ru.p3tr0vich.mwmmapsupdater.Models.MapItem;
import ru.p3tr0vich.mwmmapsupdater.dummy.DummyContent;
import ru.p3tr0vich.mwmmapsupdater.helpers.FragmentHelper;

public class ActivityMain extends AppCompatActivity implements
        FragmentMain.OnListFragmentInteractionListener,
        FragmentInterface.OnFragmentChangeListener {

    private FragmentHelper mFragmentHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentHelper = new FragmentHelper(this);

        if (savedInstanceState == null) {
            mFragmentHelper.addMainFragment();
        }
    }

    @Override
    public void onListFragmentInteraction(MapItem item) {
        Toast.makeText(this, item.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFragmentChange(FragmentInterface fragment) {

    }
}