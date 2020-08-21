package fi.neskola.kartta.application.di;

import android.app.Application;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import javax.inject.Singleton;

import dagger.Component;
import fi.neskola.kartta.ui.activities.MapsActivity;
import fi.neskola.kartta.application.KarttaApplication;
import fi.neskola.kartta.repository.KarttaRepository;
import fi.neskola.kartta.ui.fragments.MapsFragment;

@Singleton
@Component(modules = {
        ApplicationModule.class,
        DatabaseModule.class,
})
public interface ApplicationComponent {
    void inject (KarttaApplication karttaApplication);
    void inject (MapsActivity mapsActivity);
    void inject (MapsFragment mapsFragment);

    Application getApplication();
    KarttaRepository getKarttaRepository();

}