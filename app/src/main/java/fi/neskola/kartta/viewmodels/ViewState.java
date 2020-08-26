package fi.neskola.kartta.viewmodels;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import fi.neskola.kartta.models.IRecord;

public class ViewState {

    public enum State {
        VIEW_MAP,
        NEW_TARGET,
        VIEW_TARGET,
        SHOW_USER_LOCATION
    }

    public State stateName;
    public LatLng center;
    public List<IRecord> recordList = new ArrayList<>();
    public IRecord focusedRecord;

    public static ViewState copyViewStateFromOld(ViewState oldViewState) {
        ViewState newViewState = new ViewState();
        newViewState.center = oldViewState.center;
        newViewState.stateName = oldViewState.stateName;
        newViewState.recordList.addAll(oldViewState.recordList);
        newViewState.focusedRecord = oldViewState.focusedRecord;
        return newViewState;
    }

    public static ViewState makeInitialState(){
        ViewState state = new ViewState();
        state.stateName = ViewState.State.VIEW_MAP;
        state.center = new LatLng(40,50);
        state.recordList =  new ArrayList<>();
        state.focusedRecord = null;
        return state;
    }

}
