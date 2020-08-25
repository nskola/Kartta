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
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

import javax.inject.Inject;

import fi.neskola.kartta.R;
import fi.neskola.kartta.application.KarttaApplication;
import fi.neskola.kartta.models.Point;
import fi.neskola.kartta.models.Target;
import fi.neskola.kartta.viewmodels.KarttaViewModel;

import static fi.neskola.kartta.services.LocationService.EXTRA_LATITUDE;
import static fi.neskola.kartta.services.LocationService.EXTRA_LONGITUDE;
import static fi.neskola.kartta.viewmodels.KarttaViewModel.ViewState.State.NEW_TARGET;
import static fi.neskola.kartta.viewmodels.KarttaViewModel.ViewState.State.VIEW_MAP;
import static fi.neskola.kartta.viewmodels.KarttaViewModel.ViewState.State.VIEW_TARGET;

public class KarttaFragment extends Fragment {

    @Inject
    KarttaViewModel karttaViewModel;

    GoogleMap googleMap;

    BottomSheetBehavior<ConstraintLayout> bottomSheetBehavior;
    ConstraintLayout bottomSheet;
    View backgroundDimmer;
    EditText bottomSheetEditTextName;
    TextView bottomSheetTextViewLatitude, bottomSheetTextViewLongitude;
    Button bottomSheetSaveButton;
    Button bottomSheetCancelButton;
    ExtendedFloatingActionButton markPointButton;
    ExtendedFloatingActionButton drawRouteButton;
    FloatingActionButton mainFAB;

    private boolean isFABOpen = false;
    private LatLng lastUserLocation;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (googleMap == null)
                return;
            if ("location_fix".equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                if (extras == null)
                    return;
                double latitude = extras.getDouble(EXTRA_LATITUDE, -1);
                double longitude = extras.getDouble(EXTRA_LONGITUDE, -1);
                if (latitude != -1)
                    lastUserLocation = new LatLng(latitude, longitude);
            }
        }
    };

    final static int REQUEST_FINE_LOCATION = 555;
    
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            KarttaFragment.this.googleMap = googleMap;
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            if(checkLocationPermissions()) {
                googleMap.setMyLocationEnabled(true);
            }

            googleMap.setOnMarkerClickListener((marker) -> {
                long id = (long) marker.getTag();
                karttaViewModel.onMarkerClicked(id);
                return false;
            });

            googleMap.setOnMapClickListener((latLng) -> karttaViewModel.onMapClicked());

            karttaViewModel.getViewState().observeForever((viewState) -> {
                if (viewState == null)
                    return;
                addMarkers(viewState);

                if (VIEW_MAP == viewState.stateName) {
                    closeBottomSheet();
                    LatLng center = viewState.center;
                    if (center != null)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(center));
                } else if (NEW_TARGET == viewState.stateName) {
                    bottomSheetEditTextName.getText().clear();
                    bottomSheetEditTextName.setInputType(InputType.TYPE_CLASS_TEXT);
                    bottomSheetEditTextName.requestFocus();
                    bottomSheetTextViewLatitude.setText(String.valueOf(viewState.center.latitude));
                    bottomSheetTextViewLongitude.setText(String.valueOf(viewState.center.longitude));
                    MarkerOptions options = new MarkerOptions()
                            .position(viewState.center);
                    googleMap.addMarker(options);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    backgroundDimmer.setVisibility(View.VISIBLE);
                    hideFABs();
                } else if (VIEW_TARGET == viewState.stateName) {
                    Target target = viewState.focusedTarget;
                    bottomSheetEditTextName.clearFocus();
                    bottomSheetEditTextName.setInputType(InputType.TYPE_NULL);
                    bottomSheetEditTextName.getText().clear();
                    bottomSheetTextViewLatitude.setText(String.valueOf(target.getPoint().getLatitude()));
                    bottomSheetTextViewLongitude.setText(String.valueOf(target.getPoint().getLongitude()));
                    bottomSheetEditTextName.getText().append(viewState.focusedTarget.getName());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(viewState.focusedTarget.getPoint().getLatLng()));
                    peekBottomSheet();
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
        bottomSheetTextViewLatitude = view.findViewById(R.id.latitude_value);
        bottomSheetTextViewLongitude = view.findViewById(R.id.longitude_value);
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
            karttaViewModel.onAddTargetButtonClicked(latLng);
        });

        bottomSheetSaveButton.setOnClickListener( (v) -> {
            karttaViewModel.onTargetSaveClicked(bottomSheetEditTextName.getText().toString());
        });

        bottomSheetCancelButton.setOnClickListener( (v) -> {
            karttaViewModel.onTargetSaveCancelClicked();

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.focus_to_user_location:
                if (lastUserLocation != null)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(lastUserLocation));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private boolean checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestLocationPermission();
            return false;
        }
    }

    private void requestLocationPermission() {
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

    private void peekBottomSheet(){
        backgroundDimmer.setVisibility(View.INVISIBLE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        closeFABMenu();
        showFABs();
    }

    private void addMarkers(KarttaViewModel.ViewState state) {
        googleMap.clear();
        for (Target target : state.targetList) {
            MarkerOptions markerOptions = new MarkerOptions().title(target.getName());
            Point point = target.getPoint();
            markerOptions.position(point.getLatLng());
            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag(target.getId());
            if (state.stateName == VIEW_TARGET && state.focusedTarget.equals(target)) {
                marker.showInfoWindow();
            }
        }

    }

}