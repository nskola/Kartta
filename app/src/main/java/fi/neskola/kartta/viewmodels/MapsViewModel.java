package fi.neskola.kartta.viewmodels;

import javax.inject.Inject;
import javax.inject.Singleton;

import fi.neskola.kartta.repository.KarttaRepository;

@Singleton
public class MapsViewModel {

    private final KarttaRepository karttaRepository;

    @Inject
    public MapsViewModel(KarttaRepository karttaRepository) {
        this.karttaRepository = karttaRepository;
    }


}
