package fi.neskola.kartta.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.navigation.NavigationView;

import javax.inject.Inject;

import fi.neskola.kartta.R;
import fi.neskola.kartta.application.KarttaApplication;
import fi.neskola.kartta.repository.KarttaRepository;
import fi.neskola.kartta.services.LocationService;
import fi.neskola.kartta.viewmodels.KarttaViewModel;
import fi.neskola.kartta.viewmodels.RecordListViewModel;
import fi.neskola.kartta.viewmodels.ViewModelProviderFactory;

public class MapsActivity extends AppCompatActivity{

    @Inject
    public KarttaRepository karttaRepository;

    @Inject
    KarttaViewModel karttaViewModel;

    RecordListViewModel recordListViewModel;

    @Inject
    public ViewModelProviderFactory providerFactory;

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Dagger inject
        ((KarttaApplication) getApplication()).getComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_map, R.id.nav_record_list)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.fragment_container);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        recordListViewModel = new ViewModelProvider(this, providerFactory).get(RecordListViewModel.class);

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

}