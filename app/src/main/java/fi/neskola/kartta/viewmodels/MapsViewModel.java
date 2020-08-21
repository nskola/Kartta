package fi.neskola.kartta.viewmodels;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fi.neskola.kartta.models.Point;
import fi.neskola.kartta.models.Record;
import fi.neskola.kartta.repository.KarttaRepository;

@Singleton
public class MapsViewModel {

    Marker tempMarker;

    List<Marker> markers = new ArrayList<>();

    private final KarttaRepository karttaRepository;

    @Inject
    public MapsViewModel(KarttaRepository karttaRepository) {
        this.karttaRepository = karttaRepository;
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
}
