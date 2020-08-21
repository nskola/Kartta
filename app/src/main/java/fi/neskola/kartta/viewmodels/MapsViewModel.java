package fi.neskola.kartta.viewmodels;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import javax.inject.Inject;
import javax.inject.Singleton;

import fi.neskola.kartta.repository.KarttaRepository;

@Singleton
public class MapsViewModel {

    Marker tempMarker;

    //List<Marker> markers = new ArrayList<>();

    MutableLiveData<ViewState> viewState = new MutableLiveData<>();

    MutableLiveData<ActionState> actionState = new MutableLiveData<>();

    private final KarttaRepository karttaRepository;

    @Inject
    public MapsViewModel(KarttaRepository karttaRepository) {
        this.karttaRepository = karttaRepository;
        viewState.setValue(new ViewState());
    }

    public void setTempMarker(Marker marker) {
        tempMarker = marker;
    }

    public void saveTempMarker(){
        LatLng latLng = tempMarker.getPosition();
        karttaRepository.saveTargetToDb(tempMarker.getTitle(), latLng.latitude, latLng.longitude);
    }

    public void removeTempMarker(){
        tempMarker.remove();
        tempMarker = null;
    }

    public Marker getTempMarker() {
        return tempMarker;
    }

    public MutableLiveData<ViewState> getViewState() {
        return viewState;
    }

    public static class ViewState {
        public enum State {VIEW_MAP, NEW_TARGET, VIEW_TARGET}

        public State state_name = State.VIEW_MAP;
        public LatLng focused_point = new LatLng(50,40);
        //public Marker tempMarker = null;
    }

    public static class ActionState {
        public enum State {CHOOSE_MAP_ACTION}
        public State state = State.CHOOSE_MAP_ACTION;
    }

    public void onMarkTargetButtonClicked(LatLng latLng){
        ViewState nameNewTargetState = new ViewState();
        nameNewTargetState.state_name = ViewState.State.NEW_TARGET;
        nameNewTargetState.focused_point = latLng;
        viewState.setValue(nameNewTargetState);
    }

    public void onTargetSaveClicked(String targetName){
        ViewState oldState = viewState.getValue();
        if (oldState == null)
            throw new IllegalStateException("Old state was null");
        LatLng latLng = oldState.focused_point;
        karttaRepository.saveTargetToDb(targetName, latLng.latitude, latLng.longitude);

        ViewState viewTargetState = new ViewState();
        viewTargetState.state_name = ViewState.State.VIEW_TARGET;
        viewTargetState.focused_point = latLng;
        viewState.setValue(viewTargetState);
    }

    public void onTargetSaveCancelClicked() {
        ViewState oldState = viewState.getValue();
        if (oldState == null)
            throw new IllegalStateException("Old state was null");
        LatLng latLng = oldState.focused_point;

        ViewState viewTargetState = new ViewState();
        viewTargetState.state_name = ViewState.State.VIEW_MAP;
        viewTargetState.focused_point = latLng;
        viewState.setValue(viewTargetState);
    }



}
