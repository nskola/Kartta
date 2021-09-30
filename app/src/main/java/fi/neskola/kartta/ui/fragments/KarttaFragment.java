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
import android.widget.Toast;

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
import fi.neskola.kartta.models.IRecord;
import fi.neskola.kartta.models.Point;
import fi.neskola.kartta.models.RecordType;
import fi.neskola.kartta.models.Target;
import fi.neskola.kartta.ui.activities.MapsActivity;
import fi.neskola.kartta.viewmodels.KarttaViewModel;
import fi.neskola.kartta.viewmodels.RecordListViewModel;
import fi.neskola.kartta.viewmodels.ViewState;

import static fi.neskola.kartta.services.LocationService.EXTRA_LATITUDE;
import static fi.neskola.kartta.services.LocationService.EXTRA_LONGITUDE;
import static fi.neskola.kartta.viewmodels.ViewState.State.NEW_TARGET;
import static fi.neskola.kartta.viewmodels.ViewState.State.SHOW_USER_LOCATION;
import static fi.neskola.kartta.viewmodels.ViewState.State.VIEW_MAP;
import static fi.neskola.kartta.viewmodels.ViewState.State.VIEW_TARGET;

public class KarttaFragment extends Fragment {
    KarttaViewModel viewModel;

    private GoogleMap googleMap;

    private BottomSheetBehavior<ConstraintLayout> bottomSheetBehavior;
    private ConstraintLayout bottomSheet;
    private View backgroundDimmer;
    private EditText bottomSheetEditTextName;
    private TextView bottomSheetTextViewLatitude, bottomSheetTextViewLongitude;
    private Button bottomSheetSaveButton;
    private Button bottomSheetCancelButton;
    private ExtendedFloatingActionButton markPointButton;
    private FloatingActionButton mainFAB;

    private boolean isFABOpen = false;
    private LatLng lastUserLocation;

    private BroadcastReceiver broadcastReceiver;

    final static int REQUEST_FINE_LOCATION = 555;

    @Override
    public void onAttach(Context context) {
        viewModel = ((MapsActivity) context).viewModelProvider.get(KarttaViewModel.class);
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

        //Start shrinked
        markPointButton.setExtended(false);

        mainFAB.setOnClickListener( (v) -> {
            if (!isFABOpen) {
                showFABMenu();
            } else {
                closeFABMenu();
            }
        });

        markPointButton.setOnClickListener((v) ->  {
            viewModel.onAddTargetButtonClicked(getCameraPosition());
        });

        bottomSheetSaveButton.setOnClickListener( (v) -> {
            viewModel.onTargetSaveClicked(bottomSheetEditTextName.getText().toString());
        });

        bottomSheetCancelButton.setOnClickListener( (v) -> {
            viewModel.onTargetSaveCancelClicked();

        });

        return view;
    }
    
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            KarttaFragment.this.googleMap = googleMap;
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            if(checkLocationPermissions()) {
                googleMap.setMyLocationEnabled(true);
            }

            googleMap.setOnMarkerClickListener((marker) -> {
                long id = (long) marker.getTag();
                viewModel.onMarkerClicked(id);
                return false;
            });

            googleMap.setOnMapClickListener((latLng) -> viewModel.onMapClicked());

            //Start observing ViewSate from ViewModel
            viewModel.getViewStateObservable().observe(getViewLifecycleOwner(), viewState -> {
                if (viewState == null)
                    return;
                addMarkers(viewState);

                if (VIEW_MAP == viewState.stateName) {
                    closeBottomSheet();
                    LatLng center = viewState.center;
                    if (center != null)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(center));
                } else if (NEW_TARGET == viewState.stateName) {
                    showNewTargetBottomSheet(viewState);
                    MarkerOptions options = new MarkerOptions()
                            .position(viewState.center);
                    googleMap.addMarker(options);
                    backgroundDimmer.setVisibility(View.VISIBLE);
                    hideFABs();
                } else if (VIEW_TARGET == viewState.stateName) {
                    showTargetInBottomSheet(viewState);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(viewState.focusedRecord.getPoints().get(0).getLatLng()));
                    peekBottomSheet();
                } else if (SHOW_USER_LOCATION == viewState.stateName) {
                    closeBottomSheet();
                    LatLng center = viewState.center;
                    if (center != null)
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(center));
                }
            });
        }
    };

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
                    viewModel.onFocusUserLocation(lastUserLocation);
                else
                    Toast.makeText(getContext(),"Location not available", Toast.LENGTH_SHORT).show();
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
        if (broadcastReceiver == null)
            broadcastReceiver = getBroadcastReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver,intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.onMapPaused(getCameraPosition());
        if (broadcastReceiver != null)
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
    }

    public void hideFABs(){
        mainFAB.setVisibility(View.GONE);
        markPointButton.setVisibility(View.GONE);
    }

    public void showFABs(){
        mainFAB.setVisibility(View.VISIBLE);
        markPointButton.setVisibility(View.VISIBLE);
    }

    private void showFABMenu(){
        isFABOpen=true;
        markPointButton.extend();
        markPointButton.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        markPointButton.shrink();
        markPointButton.animate().translationY(0);
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

    private void showNewTargetBottomSheet(ViewState viewState) {
        bottomSheetEditTextName.getText().clear();
        bottomSheetEditTextName.setInputType(InputType.TYPE_CLASS_TEXT);
        bottomSheetEditTextName.requestFocus();
        bottomSheetTextViewLatitude.setText(String.valueOf(viewState.center.latitude));
        bottomSheetTextViewLongitude.setText(String.valueOf(viewState.center.longitude));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void showTargetInBottomSheet(ViewState viewState) {
        bottomSheetEditTextName.clearFocus();
        bottomSheetEditTextName.setInputType(InputType.TYPE_NULL);
        bottomSheetEditTextName.getText().clear();
        bottomSheetTextViewLatitude.setText(String.valueOf(viewState.focusedRecord.getPoints().get(0).getLatitude()));
        bottomSheetTextViewLongitude.setText(String.valueOf(viewState.focusedRecord.getPoints().get(0).getLongitude()));
        bottomSheetEditTextName.getText().append(viewState.focusedRecord.getName());
    }

    private void addMarkers(ViewState state) {
        googleMap.clear();
        for (IRecord record : state.recordList) {
            if (record.getType() == RecordType.TARGET) {
                MarkerOptions markerOptions = new MarkerOptions().title(record.getName());
                Point point = record.getPoints().get(0);
                markerOptions.position(point.getLatLng());
                Marker marker = googleMap.addMarker(markerOptions);
                marker.setTag(record.getId());
                if (state.stateName == VIEW_TARGET && state.focusedRecord.equals(record)) {
                    marker.showInfoWindow();
                }
            }
        }
    }

    private LatLng getCameraPosition(){
        if (googleMap == null)
            return null;
        CameraPosition mUpCameraPosition = googleMap.getCameraPosition();
        return new LatLng(mUpCameraPosition.target.latitude, mUpCameraPosition.target.longitude);
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

    private BroadcastReceiver getBroadcastReceiver(){
        return new BroadcastReceiver() {
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
    }
}