package fi.neskola.kartta.di;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import fi.neskola.kartta.application.KarttaApplication;

@Module
public class ApplicationModule {
    private final KarttaApplication mApplication;

    public ApplicationModule(KarttaApplication app) {
        mApplication = app;
    }

    @Singleton
    @Provides
    Application provideApplication() {
        return mApplication;
    }
}
