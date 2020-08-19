package fi.neskola.kartta.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import javax.inject.Inject;

import fi.neskola.kartta.R;
import fi.neskola.kartta.application.KarttaApplication;
import fi.neskola.kartta.repository.KarttaRepository;
import fi.neskola.kartta.services.LocationService;
import fi.neskola.kartta.ui.fragments.MarkPointBottomSheetFragment;
import fi.neskola.kartta.viewmodels.MapsViewModel;

public class MapsActivity extends AppCompatActivity{

    /*enum Mode {
        SHOW_MAP,DRAW_ROUTE,MARK_POINT,TRACK_ROUTE
    }*/

    @Inject
    public KarttaRepository karttaRepository;

    @Inject
    MapsViewModel mapsViewModel;

    private AppBarConfiguration mAppBarConfiguration;
    BottomSheetBehavior bottomSheetBehavior;

    View backgroundDimmer;
    ExtendedFloatingActionButton markPointButton;
    ExtendedFloatingActionButton drawRouteButton;
    FloatingActionButton mainFAB;
    private boolean isFABOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Dagger inject
        ((KarttaApplication) getApplication()).getComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Begin the transaction
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.bottom_sheet_container_fragment, MarkPointBottomSheetFragment.newInstance());
        ft.commit();

        backgroundDimmer = findViewById(R.id.background_dimmer);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ConstraintLayout bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.fragment_container);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        mainFAB = (FloatingActionButton) findViewById(R.id.fab);
        markPointButton = (ExtendedFloatingActionButton) findViewById(R.id.sub_fab1);
        drawRouteButton = (ExtendedFloatingActionButton) findViewById(R.id.sub_fab2);

        //Start shrinked
        markPointButton.setExtended(false);
        drawRouteButton.setExtended(false);

        mainFAB.setOnClickListener( (v) -> {
            if (!isFABOpen) {
                showFABMenu();
            } else {
                closeFABMenu();
            }
        });

        markPointButton.setOnClickListener((v) ->  {
            markPointToMap();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            backgroundDimmer.setVisibility(View.VISIBLE);
            hideFABs();
        });

        backgroundDimmer.setOnClickListener((v) -> closeFABMenu());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent locationStartIntent = new Intent(this, LocationService.class);
        startService(locationStartIntent);
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.fragment_container);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (isFABOpen || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            setNormalState();
        } else {
            super.onBackPressed();
        }
    }

    public void markPointToMap() {
        Intent locationFixIntent = new Intent("set_mark_center");
        LocalBroadcastManager.getInstance(this).sendBroadcast(locationFixIntent);
    }

    public void hideFABs(){
        mainFAB.setVisibility(View.GONE);
        markPointButton.setVisibility(View.GONE);
        drawRouteButton.setVisibility(View.GONE);
    }

    public void showFABs(){
        mainFAB.setVisibility(View.VISIBLE);
        markPointButton.setVisibility(View.VISIBLE);
        drawRouteButton.setVisibility(View.VISIBLE);
    }

    private void showFABMenu(){
        isFABOpen=true;
        markPointButton.extend();
        drawRouteButton.extend();
        markPointButton.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        drawRouteButton.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        drawRouteButton.shrink();
        markPointButton.shrink();
        markPointButton.animate().translationY(0);
        drawRouteButton.animate().translationY(0);
    }

    private void setNormalState(){
        backgroundDimmer.setVisibility(View.INVISIBLE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        closeFABMenu();
        showFABs();
    }

}