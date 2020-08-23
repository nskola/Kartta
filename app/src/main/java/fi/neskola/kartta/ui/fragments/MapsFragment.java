package fi.neskola.kartta.ui.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fi.neskola.kartta.R;
import fi.neskola.kartta.application.KarttaApplication;
import fi.neskola.kartta.models.Point;
import fi.neskola.kartta.models.Record;
import fi.neskola.kartta.repository.KarttaRepository;
import fi.neskola.kartta.viewmodels.MapsViewModel;

public class MapsFragment extends Fragment {

    @Inject
    MapsViewModel mapsViewModel;
    @Inject
    KarttaRepository karttaRepository;

    GoogleMap googleMap;

    BottomSheetBehavior<ConstraintLayout> bottomSheetBehavior;
    ConstraintLayout bottomSheet;
    View backgroundDimmer;
    EditText bottomSheetEditTextName;
    Button bottomSheetSaveButton;
    Button bottomSheetCancelButton;
    ExtendedFloatingActionButton markPointButton;
    ExtendedFloatingActionButton drawRouteButton;
    FloatingActionButton mainFAB;

    private boolean isFABOpen = false;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (googleMap == null)
                return;
            if ("location_fix".equals(intent.getAction())) {}
        }
    };

    final static int REQUEST_FINE_LOCATION = 555;
    
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsFragment.this.googleMap = googleMap;
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            if(checkPermissions()) {
                googleMap.setMyLocationEnabled(true);
            }

            mapsViewModel.getViewState().observeForever((state) -> {

                if (state == null)
                    return;

                addMarkers(state);

                if (MapsViewModel.ViewState.State.VIEW_MAP == state.state_name) {
                    closeBottomSheet();
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(state.focused_point));
                } else if (MapsViewModel.ViewState.State.NEW_TARGET == state.state_name) {
                    MarkerOptions options = new MarkerOptions()
                            .position(state.focused_point);
                    googleMap.addMarker(options);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    backgroundDimmer.setVisibility(View.VISIBLE);
                    hideFABs();
                } else if (MapsViewModel.ViewState.State.VIEW_TARGET == state.state_name) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(state.focused_point));
                    closeBottomSheet();
                }
            });
        }
    };

    @Override
    public void onAttach(Context context) {
        //Dagger inject
        ((KarttaApplication) context.getApplicationContext()).getComponent().inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        backgroundDimmer = view.findViewById(R.id.background_dimmer);
        bottomSheet = view.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetSaveButton = view.findViewById(R.id.bottom_sheet_save_button);
        bottomSheetCancelButton = view.findViewById(R.id.bottom_sheet_cancel_button);
        bottomSheetEditTextName = view.findViewById(R.id.bottom_sheet_edittext_name);
        mainFAB =  view.findViewById(R.id.fab);
        markPointButton = view.findViewById(R.id.sub_fab1);
        drawRouteButton =  view.findViewById(R.id.sub_fab2);

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
            CameraPosition mUpCameraPosition = googleMap.getCameraPosition();
            LatLng latLng = new LatLng(mUpCameraPosition.target.latitude, mUpCameraPosition.target.longitude);
            mapsViewModel.onMarkTargetButtonClicked(latLng);
        });

        bottomSheetSaveButton.setOnClickListener( (v) -> {
            mapsViewModel.onTargetSaveClicked(bottomSheetEditTextName.getText().toString());
        });

        bottomSheetCancelButton.setOnClickListener( (v) -> {
            mapsViewModel.onTargetSaveCancelClicked();

        });

       return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_fragment_toolbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("location_fix");
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver,intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions();
            return false;
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions( getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_FINE_LOCATION);
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

    private void closeBottomSheet(){
        backgroundDimmer.setVisibility(View.INVISIBLE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        closeFABMenu();
        showFABs();
    }

    private void addMarkers(MapsViewModel.ViewState state) {
        googleMap.clear();
        for (Record record : state.recordList) {
            switch (record.getType()) {
                case TARGET:
                    MarkerOptions markerOptions = new MarkerOptions().title(record.getName());
                    for (Point point : record.getPoints()) {
                        markerOptions.position(point.getLatLng());
                    }
                    googleMap.addMarker(markerOptions);
                    break;
                case TRACK:

                default:
                    break;
            }
        }
    }

}