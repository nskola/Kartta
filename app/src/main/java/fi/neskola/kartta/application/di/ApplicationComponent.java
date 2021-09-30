package fi.neskola.kartta.application.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import fi.neskola.kartta.ui.activities.MapsActivity;
import fi.neskola.kartta.application.KarttaApplication;
import fi.neskola.kartta.repository.KarttaRepository;
import fi.neskola.kartta.ui.fragments.KarttaFragment;
import fi.neskola.kartta.ui.fragments.RecordListFragment;

@Singleton
@Component(modules = {
        ApplicationModule.class,
        DatabaseModule.class,
        ViewModelFactoryModule.class,
        ViewModelsModule.class
})
public interface ApplicationComponent {
    void inject (KarttaApplication karttaApplication);
    void inject (MapsActivity mapsActivity);
    void inject (KarttaFragment karttaFragment);
    void inject (RecordListFragment recordListFragment);

    Application getApplication();
    KarttaRepository getKarttaRepository();

}