package ru.p3tr0vich.mwmmapsupdater;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import ru.p3tr0vich.mwmmapsupdater.helpers.FragmentHelper;
import ru.p3tr0vich.mwmmapsupdater.models.MapItem;

public class ActivityMain extends AppCompatActivity implements
        FragmentMain.OnListFragmentInteractionListener,
        FragmentInterface.OnFragmentChangeListener {

    private Toolbar mToolbarMain;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private FragmentHelper mFragmentHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initDrawer();

        mFragmentHelper = new FragmentHelper(this);

        if (savedInstanceState == null) {
            mFragmentHelper.addMainFragment();
        }

    }

    private void initToolbar() {
        mToolbarMain = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbarMain);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, mToolbarMain, R.string.app_name, R.string.app_name) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                FragmentMain fragmentMain = mFragmentHelper.getFragmentMain();
                if (fragmentMain != null && fragmentMain.isVisible()) {
                    fragmentMain.setFloatingActionButtonVisible(false);
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                FragmentMain fragmentMain = mFragmentHelper.getFragmentMain();
                if (fragmentMain != null && fragmentMain.isVisible()) {
                    fragmentMain.setFloatingActionButtonVisible(true);
                }
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();
        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentHelper.getCurrentFragment().onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            FragmentInterface fragment = mFragmentHelper.getCurrentFragment();

            if (fragment.onBackPressed()) {
                return;
            }

            if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}