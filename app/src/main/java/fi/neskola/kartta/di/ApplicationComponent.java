package fi.neskola.kartta.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import fi.neskola.kartta.activities.MapsActivity;
import fi.neskola.kartta.application.KarttaApplication;
import fi.neskola.kartta.repository.KarttaRepository;

@Singleton
@Component(modules = {
        ApplicationModule.class,
        DatabaseModule.class,
})
public interface ApplicationComponent {
    void inject (KarttaApplication karttaApplication);
    void inject (MapsActivity mapsActivity);

    Application getApplication();
    KarttaRepository getKarttaRepository();

}