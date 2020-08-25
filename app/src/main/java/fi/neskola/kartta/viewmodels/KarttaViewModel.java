package fi.neskola.kartta.viewmodels;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fi.neskola.kartta.models.IRecord;
import fi.neskola.kartta.models.Point;
import fi.neskola.kartta.models.Target;
import fi.neskola.kartta.repository.KarttaRepository;

@Singleton
public class KarttaViewModel {

    private MutableLiveData<ViewState> viewStateObservable = new MutableLiveData<>();

    private MutableLiveData<List<IRecord>> recordListObservable = new MutableLiveData<>();

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
        karttaRepository.getTargetListObservable().observeForever(targets -> {
            ViewState newViewState;
            if (viewStateObservable.getValue() == null)
                newViewState = new ViewState();
            else
                newViewState = copyViewStateFromOld(viewStateObservable.getValue());
            newViewState.targetList.clear();
            newViewState.targetList.addAll(targets);
            List<IRecord> recordList = new ArrayList<>(targets);
            this.recordListObservable.setValue(recordList);
            viewStateObservable.setValue(newViewState);
        });

    }

    public MutableLiveData<ViewState> getViewStateObservable() {
        return viewStateObservable;
    }

    public MutableLiveData<List<IRecord>> getRecordListObservable() { return recordListObservable; }

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
        viewTargetState.targetList.add(target);
        viewTargetState.focusedTarget = target;
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
        for (Target target : oldState.targetList) {
            if (target.getId() == recordId) {
                ViewState viewTargetState = copyViewStateFromOld(oldState);
                viewTargetState.stateName = ViewState.State.VIEW_TARGET;
                viewTargetState.focusedTarget = target;
                viewStateObservable.setValue(viewTargetState);
                break;
            }
        }
    }

    public void onMapClicked(){
        ViewState oldState = viewStateObservable.getValue();
        if (oldState == null)
            throw new IllegalStateException("Old state was null");
        ViewState viewState = copyViewStateFromOld(oldState);
        viewState.stateName = ViewState.State.VIEW_MAP;
        viewState.center = null;
        this.viewStateObservable.setValue(viewState);
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
