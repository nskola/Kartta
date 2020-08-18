package fi.neskola.kartta.repository;

import android.app.Application;
import android.content.Context;

import androidx.room.Room;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fi.neskola.kartta.database.Executor;
import fi.neskola.kartta.database.KarttaDatabase;
import fi.neskola.kartta.database.PointDao;
import fi.neskola.kartta.database.RecordDao;
import fi.neskola.kartta.models.Point;
import fi.neskola.kartta.models.Record;

@Singleton
public class KarttaRepository {

    KarttaDatabase database;

    private List<Record> mRecords;

    @Inject
    public KarttaRepository(KarttaDatabase database) {
        this.database = database;
    }

    List<Record> getAllRecords() {
        return mRecords;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(Point point) {
        Executor.execute(() -> {
            database.pointDao().insert(point);
        });
    }
}
