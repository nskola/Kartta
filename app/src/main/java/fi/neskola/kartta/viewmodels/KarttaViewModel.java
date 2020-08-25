package fi.neskola.kartta.viewmodels;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fi.neskola.kartta.models.Point;
import fi.neskola.kartta.models.Target;
import fi.neskola.kartta.repository.KarttaRepository;

@Singleton
public class KarttaViewModel {

    private MutableLiveData<ViewState> viewState = new MutableLiveData<>();

    private final KarttaRepository karttaRepository;

    public static class ViewState {
        public enum State {VIEW_MAP, NEW_TARGET, VIEW_TARGET}

        public State stateName = State.VIEW_MAP;
        public LatLng center = new LatLng(50,40);
        public List<Target> targetList =  new ArrayList<>();
        public Target focusedTarget = null;
    }

    @Inject
    public KarttaViewModel(KarttaRepository karttaRepository) {
        this.karttaRepository = karttaRepository;
        karttaRepository.getTargetLiveData().observeForever( targets -> {
            ViewState newViewState;
            if (viewState.getValue() == null)
                newViewState = new ViewState();
            else
                newViewState = copyViewStateFromOld(viewState.getValue());
            newViewState.targetList.clear();
            newViewState.targetList.addAll(targets);
            viewState.setValue(newViewState);
        });

    }

    public MutableLiveData<ViewState> getViewState() {
        return viewState;
    }

    public void onAddTargetButtonClicked(LatLng latLng){
        ViewState oldState = viewState.getValue();
        if (oldState == null)
            throw new IllegalStateException("Old state was null");
        ViewState nameNewTargetState = copyViewStateFromOld(oldState);
        nameNewTargetState.stateName = ViewState.State.NEW_TARGET;
        nameNewTargetState.center = latLng;
        viewState.setValue(nameNewTargetState);
    }

    public void onTargetSaveClicked(String targetName){
        ViewState oldState = viewState.getValue();
        if (oldState == null)
            throw new IllegalStateException("Old state was null");
        LatLng latLng = oldState.center;
        Target target = new Target(targetName);
        target.setPoint(new Point(latLng));
        karttaRepository.insertTarget(target, newRecord -> {
            ViewState viewTargetState = copyViewStateFromOld(oldState);
            viewTargetState.stateName = ViewState.State.VIEW_TARGET;
            viewTargetState.targetList.add(newRecord);
            viewTargetState.focusedTarget = newRecord;
            viewState.setValue(viewTargetState);
        });
    }

    public void onTargetSaveCancelClicked() {
        ViewState oldState = viewState.getValue();
        if (oldState == null)
            throw new IllegalStateException("Old state was null");
        ViewState viewState = copyViewStateFromOld(oldState);
        viewState.stateName = ViewState.State.VIEW_MAP;
        this.viewState.setValue(viewState);
    }

    public void onMarkerClicked(long recordId) {
        ViewState oldState = viewState.getValue();
        if (oldState == null)
            throw new IllegalStateException("Old state was null");
        for (Target target : oldState.targetList) {
            if (target.getId() == recordId) {
                ViewState viewTargetState = copyViewStateFromOld(oldState);
                viewTargetState.stateName = ViewState.State.VIEW_TARGET;
                viewTargetState.focusedTarget = target;
                viewState.setValue(viewTargetState);
                break;
            }
        }
    }

    public void onMapClicked(){
        ViewState oldState = viewState.getValue();
        if (oldState == null)
            throw new IllegalStateException("Old state was null");
        ViewState viewState = copyViewStateFromOld(oldState);
        viewState.stateName = ViewState.State.VIEW_MAP;
        viewState.center = null;
        this.viewState.setValue(viewState);
    }

    private static ViewState copyViewStateFromOld(ViewState oldViewState) {
        ViewState newViewState = new ViewState();
        newViewState.center = oldViewState.center;
        newViewState.stateName = oldViewState.stateName;
        newViewState.targetList.addAll(oldViewState.targetList);
        newViewState.focusedTarget = oldViewState.focusedTarget;
        return newViewState;
    }



}
