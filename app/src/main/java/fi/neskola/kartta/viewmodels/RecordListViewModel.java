package fi.neskola.kartta.viewmodels;

import androidx.lifecycle.LiveData;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fi.neskola.kartta.models.IRecord;
import fi.neskola.kartta.repository.KarttaRepository;

@Singleton
public class RecordListViewModel {

    KarttaRepository karttaRepository;

    @Inject
    public RecordListViewModel(KarttaRepository karttaRepository) {
        this.karttaRepository = karttaRepository;
    }

    public LiveData<List<IRecord>> getRecordListObservable() {
        return karttaRepository.getRecordListObservable();
    }




}
