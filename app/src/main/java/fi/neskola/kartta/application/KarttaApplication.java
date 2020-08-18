package fi.neskola.kartta.application;

import android.app.Application;

import fi.neskola.kartta.di.ApplicationComponent;
import fi.neskola.kartta.di.ApplicationModule;
import fi.neskola.kartta.di.DaggerApplicationComponent;
import fi.neskola.kartta.di.DatabaseModule;

public class KarttaApplication extends Application {
    protected ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplicationComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .databaseModule(new DatabaseModule(this))
                .build();
        mApplicationComponent.inject(this);
    }

    public ApplicationComponent getComponent() {
        return mApplicationComponent;
    }
}
