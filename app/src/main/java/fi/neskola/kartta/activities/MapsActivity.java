package fi.neskola.kartta.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import fi.neskola.kartta.R;

public class MapsActivity extends AppCompatActivity{

    enum Mode {
        SHOW_MAP,DRAW_ROUTE,MARK_POINT,TRACK_ROUTE
    }

    private AppBarConfiguration mAppBarConfiguration;

    View obstructor;
    ExtendedFloatingActionButton fab1;
    ExtendedFloatingActionButton fab2;
    private boolean isFABOpen = false;
    Mode mode = Mode.SHOW_MAP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        obstructor = findViewById(R.id.obstructor);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.fragment_container);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (ExtendedFloatingActionButton) findViewById(R.id.sub_fab1);
        fab2 = (ExtendedFloatingActionButton) findViewById(R.id.sub_fab2);

        //Start shrinked
        fab1.setExtended(false);
        fab2.setExtended(false);
        fab.setOnClickListener( (v) -> {
            switch (mode) {
                case SHOW_MAP:
                    if (!isFABOpen) {
                        showFABMenu();
                    } else {
                        closeFABMenu();
                    }
                    break;
                case MARK_POINT:
                    break;
                default:
                    break;
            }
        });

        obstructor.setOnClickListener((v) -> closeFABMenu());

    }

    private void showFABMenu(){
        isFABOpen=true;
        obstructor.setVisibility(View.VISIBLE);
        fab1.extend();
        fab2.extend();
        fab1.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fab2.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        obstructor.setVisibility(View.INVISIBLE);
        fab2.shrink();
        fab1.shrink();
        fab1.animate().translationY(0);
        fab2.animate().translationY(0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.fragment_container);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if(!isFABOpen){
            super.onBackPressed();
        } else {
            closeFABMenu();
        }
    }

}