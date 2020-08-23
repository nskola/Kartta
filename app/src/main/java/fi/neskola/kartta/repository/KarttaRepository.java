package fi.neskola.kartta.repository;

import android.app.Application;
import android.content.Context;
import android.util.Log;

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

    void insertPointWithRecord(Record record, Point point){
        Executor.execute(() -> {
            long result = database.recordDao().insert(record);
            if (result > 0) {
                point.setFk_record_id(result);
                database.pointDao().insert(point);
            }
        });
    }

    public void saveRecordToDb(Record record) {
        switch (record.getType()) {
            case TARGET:
                Point point = record.getPoints().get(0);
                insertPointWithRecord(record, point);
                break;
            default:
                break;
        }
    }
}
