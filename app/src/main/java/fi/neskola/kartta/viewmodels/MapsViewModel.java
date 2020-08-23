package fi.neskola.kartta.viewmodels;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fi.neskola.kartta.models.Point;
import fi.neskola.kartta.models.Record;
import fi.neskola.kartta.repository.KarttaRepository;

@Singleton
public class MapsViewModel {

    private MutableLiveData<ViewState> viewState = new MutableLiveData<>();

    private final KarttaRepository karttaRepository;

    @Inject
    public MapsViewModel(KarttaRepository karttaRepository) {
        this.karttaRepository = karttaRepository;
        viewState.setValue(new ViewState());
    }

    public MutableLiveData<ViewState> getViewState() {
        return viewState;
    }

    public static class ViewState {
        public enum State {VIEW_MAP, NEW_TARGET, VIEW_TARGET}

        public State state_name = State.VIEW_MAP;
        public LatLng focused_point = new LatLng(50,40);
        public List<Record> recordList =  new ArrayList<>();
    }

    public void onMarkTargetButtonClicked(LatLng latLng){
        ViewState oldState = viewState.getValue();
        if (oldState == null)
            throw new IllegalStateException("Old state was null");
        ViewState nameNewTargetState = copyViewStateFromOld(oldState);
        nameNewTargetState.state_name = ViewState.State.NEW_TARGET;
        nameNewTargetState.focused_point = latLng;
        viewState.setValue(nameNewTargetState);
    }

    public void onTargetSaveClicked(String targetName){
        ViewState oldState = viewState.getValue();
        if (oldState == null)
            throw new IllegalStateException("Old state was null");
        LatLng latLng = oldState.focused_point;
        Record record = new Record(targetName);
        record.setType(Record.Type.TARGET);
        record.addPoint(new Point(latLng));
        karttaRepository.saveRecordToDb(record);

        ViewState viewTargetState = copyViewStateFromOld(oldState);
        viewTargetState.state_name = ViewState.State.VIEW_TARGET;
        viewTargetState.recordList.add(record);
        viewState.setValue(viewTargetState);
    }

    public void onTargetSaveCancelClicked() {
        ViewState oldState = viewState.getValue();
        if (oldState == null)
            throw new IllegalStateException("Old state was null");
        ViewState viewState = copyViewStateFromOld(oldState);
        viewState.state_name = ViewState.State.VIEW_MAP;
        this.viewState.setValue(viewState);
    }

    private static ViewState copyViewStateFromOld(ViewState oldViewState) {
        ViewState newViewState = new ViewState();
        newViewState.focused_point = oldViewState.focused_point;
        newViewState.state_name = oldViewState.state_name;
        newViewState.recordList.addAll(oldViewState.recordList);
        return newViewState;
    }

}
