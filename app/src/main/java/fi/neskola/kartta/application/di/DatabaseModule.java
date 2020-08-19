package fi.neskola.kartta.application.di;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import fi.neskola.kartta.database.KarttaDatabase;
import fi.neskola.kartta.database.PointDao;
import fi.neskola.kartta.database.RecordDao;

@Module
public class DatabaseModule {
    //Application context
    private Context mContext;

    public DatabaseModule(Application mApplication) {
        mContext = mApplication;
    }

    @Singleton
    @Provides
    KarttaDatabase providesKarttaDatabase() {
        return KarttaDatabase.buildDataBase(mContext);
    }

    @Singleton
    @Provides
    PointDao providesPointDao(KarttaDatabase karttaDatabase) {
        return karttaDatabase.pointDao();
    }

    @Singleton
    @Provides
    RecordDao providesRecordDao(KarttaDatabase karttaDatabase) {
        return karttaDatabase.recordDao();
    }
}
