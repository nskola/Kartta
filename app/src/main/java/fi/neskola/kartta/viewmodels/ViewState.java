package fi.neskola.kartta.viewmodels;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import fi.neskola.kartta.models.Target;

public class ViewState {

    public enum State {
        VIEW_MAP,
        NEW_TARGET,
        VIEW_TARGET
    }

    public State stateName;
    public LatLng center;
    public List<Target> targetList = new ArrayList<>();
    public Target focusedTarget;

    public static ViewState copyViewStateFromOld(ViewState oldViewState) {
        ViewState newViewState = new ViewState();
        newViewState.center = oldViewState.center;
        newViewState.stateName = oldViewState.stateName;
        newViewState.targetList.addAll(oldViewState.targetList);
        newViewState.focusedTarget = oldViewState.focusedTarget;
        return newViewState;
    }

    public static ViewState makeInitialState(){
        ViewState state = new ViewState();
        state.stateName = ViewState.State.VIEW_MAP;
        state.center = new LatLng(0,0);
        state.targetList =  new ArrayList<>();
        state.focusedTarget = null;
        return state;
    }

}
