package fi.neskola.kartta.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fi.neskola.kartta.models.IRecord;
import fi.neskola.kartta.models.Point;
import fi.neskola.kartta.models.Target;
import fi.neskola.kartta.repository.KarttaRepository;

import static fi.neskola.kartta.viewmodels.ViewState.copyViewStateFromOld;

@Singleton
public class KarttaViewModel {

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
            newViewState.targetList.clear();
            for (IRecord record : records) {
                if (record instanceof Target)
                    newViewState.targetList.add((Target) record);
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

    public void onMapPaused(LatLng cameraPosition) {
        if (viewStateObservable.getValue() != null)
            viewStateObservable.getValue().center = cameraPosition;
    }
}
