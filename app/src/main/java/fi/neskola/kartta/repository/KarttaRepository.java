package fi.neskola.kartta.repository;

import android.app.Application;
import android.content.Context;

import androidx.room.Room;

import java.util.List;

import fi.neskola.kartta.database.KarttaDatabase;
import fi.neskola.kartta.database.PointDao;
import fi.neskola.kartta.database.RecordDao;
import fi.neskola.kartta.models.Point;
import fi.neskola.kartta.models.Record;

public class KarttaRepository {

    private PointDao mPointDao;
    private RecordDao mRecordDao;
    private List<Record> mRecords;

    private static volatile KarttaRepository INSTANCE;

    public static KarttaRepository getRepository(final Application application) {
        if (INSTANCE == null) {
            synchronized (KarttaRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new KarttaRepository(application);
                }
            }
        }
        return INSTANCE;
    }

    // Note that in order to unit test the Repository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    KarttaRepository(Application application) {
        KarttaDatabase db = KarttaDatabase.getDatabase(application);
        mPointDao = db.pointDao();
        mRecordDao = db.recordDao();
        KarttaDatabase.databaseWriteExecutor.execute(() -> mRecords = mRecordDao.getAllRecords());

    }

    List<Record> getAllRecords() {
        return mRecords;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(Point point) {
        KarttaDatabase.databaseWriteExecutor.execute(() -> {
            mPointDao.insert(point);
        });
    }
}
