package fi.neskola.kartta.viewmodels;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;
import javax.inject.Singleton;

import fi.neskola.kartta.models.IRecord;
import fi.neskola.kartta.models.Point;
import fi.neskola.kartta.models.Target;
import fi.neskola.kartta.repository.KarttaRepository;

import static fi.neskola.kartta.viewmodels.ViewState.copyViewStateFromOld;

@Singleton
public class KarttaViewModel extends ViewModel {

    private MutableLiveData<ViewState> viewStateObservable = new MutableLiveData<>();

    private final KarttaRepository karttaRepository;

    @Inject
    public KarttaViewModel(KarttaRepository karttaRepository) {
        this.karttaRepository = karttaRepository;
        karttaRepository.getRecordListObservable().observeForever(records -> {
            ViewState newViewState;
            if (viewStateObservable.getValue() == null)
                newViewState = ViewState.makeInitialState();
            else
                newViewState = copyViewStateFromOld(viewStateObservable.getValue());
            newViewState.recordList.clear();

            boolean currentRecordExists = false;

            for (IRecord record : records) {
                newViewState.recordList.add((Target) record);
                if (newViewState.focusedRecord != null && record.getId() == newViewState.focusedRecord.getId())
                    currentRecordExists = true;
            }

            //If currently focused record was removed, fallback to VIEW_MAP state
            if (!currentRecordExists && newViewState.stateName == ViewState.State.VIEW_TARGET) {
                newViewState.focusedRecord = null;
                newViewState.stateName = ViewState.State.VIEW_MAP;
            }

            viewStateObservable.setValue(newViewState);
        });

    }

    public MutableLiveData<ViewState> getViewStateObservable() {
        return viewStateObservable;
    }

    public void onAddTargetButtonClicked(LatLng latLng){
        ViewState oldState = viewStateObservable.getValue();
        if (oldState == null)
            throw new IllegalStateException("Old state was null");
        ViewState nameNewTargetState = copyViewStateFromOld(oldState);
        nameNewTargetState.stateName = ViewState.State.NEW_TARGET;
        nameNewTargetState.center = latLng;
        viewStateObservable.setValue(nameNewTargetState);
    }

    public void onTargetSaveClicked(String targetName){
        ViewState oldState = viewStateObservable.getValue();
        if (oldState == null)
            throw new IllegalStateException("Old state was null");
        LatLng latLng = oldState.center;
        Target target = new Target(targetName);
        target.setPoint(new Point(latLng));

        //This will be only temporary target, until repository emits new record list for us
        ViewState viewTargetState = copyViewStateFromOld(oldState);
        viewTargetState.stateName = ViewState.State.VIEW_TARGET;
        viewTargetState.recordList.add(target);
        viewTargetState.focusedRecord = target;
        viewStateObservable.setValue(viewTargetState);

        karttaRepository.insertTarget(target);
    }

    public void onTargetSaveCancelClicked() {
        ViewState oldState = viewStateObservable.getValue();
        if (oldState == null)
            throw new IllegalStateException("Old state was null");
        ViewState viewState = copyViewStateFromOld(oldState);
        viewState.stateName = ViewState.State.VIEW_MAP;
        this.viewStateObservable.setValue(viewState);
    }

    public void onMarkerClicked(long recordId) {
        ViewState oldState = viewStateObservable.getValue();
        if (oldState == null)
            throw new IllegalStateException("Old state was null");
        for (IRecord record : oldState.recordList) {
            if (record.getId() == recordId) {
                ViewState viewTargetState = copyViewStateFromOld(oldState);
                viewTargetState.stateName = ViewState.State.VIEW_TARGET;
                viewTargetState.focusedRecord = record;
                viewStateObservable.setValue(viewTargetState);
                break;
            }
        }
    }

    public void onFocusUserLocation(LatLng latLng) {
        onViewMap(latLng, true);
    }

    public void onMapClicked() {
        onViewMap(null, false);
    }

    private void onViewMap(@Nullable LatLng latLng, boolean isUserLocation){
        ViewState oldState = viewStateObservable.getValue();
        if (oldState == null)
            throw new IllegalStateException("Old state was null");
        ViewState viewState = copyViewStateFromOld(oldState);
        viewState.stateName = isUserLocation ? ViewState.State.SHOW_USER_LOCATION : ViewState.State.VIEW_MAP;
        viewState.focusedRecord = null;
        viewState.center = latLng;
        this.viewStateObservable.setValue(viewState);
    }

    public void onMapPaused(LatLng cameraPosition) {
        if (viewStateObservable.getValue() != null)
            viewStateObservable.getValue().center = cameraPosition;
    }
}
