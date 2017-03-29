package ru.p3tr0vich.mwmmapsupdater;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import ru.p3tr0vich.mwmmapsupdater.factories.FragmentFactory;
import ru.p3tr0vich.mwmmapsupdater.helpers.FragmentHelper;
import ru.p3tr0vich.mwmmapsupdater.models.MapItem;

public class ActivityMain extends AppCompatActivity implements
        FragmentMain.OnListFragmentInteractionListener,
        FragmentInterface.OnFragmentChangeListener {

    private static final String KEY_CURRENT_FRAGMENT_ID = "KEY_CURRENT_FRAGMENT_ID";

    private Toolbar mToolbarMain;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;

    private FragmentHelper mFragmentHelper;

    @FragmentFactory.Ids.Id
    private int mCurrentFragmentId;
    private int mClickedMenuId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initDrawer();

        mFragmentHelper = new FragmentHelper(this);

        if (savedInstanceState == null) {
            mFragmentHelper.addMainFragment();
        } else {
            mCurrentFragmentId = FragmentFactory.intToFragmentId(savedInstanceState.getInt(KEY_CURRENT_FRAGMENT_ID));
            if (mCurrentFragmentId == FragmentFactory.Ids.PREFERENCES) {
                FragmentPreferences fragmentPreferences = mFragmentHelper.getFragmentPreferences();
                if (fragmentPreferences != null) {
                    mDrawerToggle.setDrawerIndicatorEnabled(fragmentPreferences.isInRoot());
                }
            }
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

                selectItem(mClickedMenuId);
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

        mNavigationView = (NavigationView) findViewById(R.id.drawer_navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                mClickedMenuId = menuItem.getItemId();
                // Если текущий фрагмент -- настройки, может отображаться стрелка влево.
                // Если нажат другой пункт меню, показывается значок меню.
                if (mCurrentFragmentId == FragmentFactory.Ids.PREFERENCES &&
                        mCurrentFragmentId != menuIdToFragmentId(mClickedMenuId)) {
                    mDrawerToggle.setDrawerIndicatorEnabled(true);
                }

                mDrawerLayout.closeDrawer(GravityCompat.START);

                return true;
            }
        });
    }

    @FragmentFactory.Ids.Id
    private int menuIdToFragmentId(int menuId) {
        switch (menuId) {
            case R.id.action_main:
                return FragmentFactory.Ids.MAIN;
            case R.id.action_preferences:
                return FragmentFactory.Ids.PREFERENCES;
            case R.id.action_about:
                return FragmentFactory.Ids.ABOUT;
            default:
                throw new IllegalArgumentException("Bad menu id == " + menuId);
        }
    }

    private int fragmentIdToMenuId(@FragmentFactory.Ids.Id int fragmentId) {
        switch (fragmentId) {
            case FragmentFactory.Ids.MAIN:
                return R.id.action_main;
            case FragmentFactory.Ids.PREFERENCES:
                return R.id.action_preferences;
            case FragmentFactory.Ids.ABOUT:
                return R.id.action_about;
            case FragmentFactory.Ids.BAD_ID:
            default:
                return -1;
        }
    }

    private void selectItem(int menuId) {
        if (menuId == -1) return;

        mClickedMenuId = -1;

        int fragmentId = menuIdToFragmentId(menuId);

        if (mCurrentFragmentId == fragmentId) {
            if (mCurrentFragmentId == FragmentFactory.Ids.PREFERENCES) {
                FragmentPreferences fragmentPreferences = mFragmentHelper.getFragmentPreferences();
                if (fragmentPreferences != null) {
                    fragmentPreferences.goToRootScreen();
                }
            }

            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment fragmentMain = mFragmentHelper.getFragment(FragmentFactory.MainFragment.ID);

        if (fragmentMain != null) {
            if (!fragmentMain.isVisible()) {
                fragmentManager.popBackStack();
            }
        }

        if (fragmentId == FragmentFactory.MainFragment.ID) return;

        Bundle bundle = new Bundle();

        mFragmentHelper.replaceFragment(fragmentId, bundle);
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
        mCurrentFragmentId = FragmentFactory.intToFragmentId(fragment.getFragmentId());

        setTitle(fragment.getTitle());
        setSubtitle(fragment.getSubtitle());

        mNavigationView.getMenu().findItem(fragmentIdToMenuId(mCurrentFragmentId)).setChecked(true);
    }

    @Override
    public void setTitle(CharSequence title) {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar == null) return;

        if (title != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayShowTitleEnabled(true);
        } else {
            actionBar.setTitle(null);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void setSubtitle(@Nullable String subtitle) {
        mToolbarMain.setSubtitle(subtitle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}