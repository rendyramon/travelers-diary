package com.travelersdiary.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.squareup.otto.Subscribe;
import com.travelersdiary.R;
import com.travelersdiary.bus.BusProvider;
import com.travelersdiary.fragments.DiaryListFragment;
import com.travelersdiary.services.LocationTrackingService;
import com.travelersdiary.ui.FABScrollBehavior;

import butterknife.Bind;
import butterknife.OnClick;

public class DiaryListActivity extends BaseActivity {
    private static final String DIARY_LIST_FRAGMENT_TAG = "DIARY_LIST_FRAGMENT_TAG";

    @Bind(R.id.main_activity_toolbar)
    Toolbar mToolbar;

    @Bind(R.id.main_activity_fab)
    FloatingActionButton mDiaryListActivityFab;

    @OnClick(R.id.main_activity_fab)
    public void onFabClick() {
        Intent intent = new Intent(this, DiaryActivity.class);
        intent.putExtra(DiaryActivity.NEW_DIARY_NOTE, true);
        startActivity(intent);
    }

    private ActionMode mActionMode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_list);
        BusProvider.bus().register(this);

        setSupportActionBar(mToolbar);
        setupNavigationView(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            DiaryListFragment fragment = new DiaryListFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment, DIARY_LIST_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.setCheckedItem(R.id.nav_diary);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Exit me", true);
        startActivity(intent);
        finish();
    }

    @Override
    protected boolean useDrawerToggle() {
        return true;
    }

    @Override
    protected void onDrawerOpened(View drawerView) {
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }
        super.onDrawerOpened(drawerView);
    }

    private void hideFloatingActionButton() {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mDiaryListActivityFab.getLayoutParams();
        params.setBehavior(null);
        mDiaryListActivityFab.setLayoutParams(params);
        mDiaryListActivityFab.hide();
    }

    private void showFloatingActionButton() {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mDiaryListActivityFab.getLayoutParams();
        params.setBehavior(new FABScrollBehavior());
        mDiaryListActivityFab.setLayoutParams(params);
        mDiaryListActivityFab.show();
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        mActionMode = mode;
        hideFloatingActionButton();
    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
        super.onSupportActionModeFinished(mode);
        showFloatingActionButton();
        mActionMode = null;
    }

    @Override
    protected void onDestroy() {
        BusProvider.bus().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void checkTracking(LocationTrackingService.CheckTrackingEvent event) {
        switchStartStop(event.isTrackingEnabled);
    }
}
