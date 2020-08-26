package fi.neskola.kartta.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fi.neskola.kartta.models.IRecord;
import fi.neskola.kartta.repository.KarttaRepository;

@Singleton
public class RecordListViewModel {

    KarttaRepository karttaRepository;

    MutableLiveData<EventWrapper<ViewEvent>> viewEventObservable = new MutableLiveData<>();

    @Inject
    public RecordListViewModel(KarttaRepository karttaRepository) {
        this.karttaRepository = karttaRepository;
    }

    public LiveData<List<IRecord>> getRecordListObservable() {
        return karttaRepository.getRecordListObservable();
    }

    public MutableLiveData<EventWrapper<ViewEvent>>  getViewEventObservable() {
        return viewEventObservable;
    }

    public void onListItemClicked(IRecord record) {
        viewEventObservable.setValue(new EventWrapper<>(new ViewEvent(ViewEvent.Event.REQUEST_REMOVE, record)));
    }

    public void onRemoveRecord(IRecord record) {
        karttaRepository.removeRecord(record);
    }

}
