package fi.neskola.kartta.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fi.neskola.kartta.database.Executor;
import fi.neskola.kartta.database.KarttaDatabase;
import fi.neskola.kartta.models.IRecord;
import fi.neskola.kartta.models.Point;
import fi.neskola.kartta.models.Target;

@Singleton
public class KarttaRepository {

    public interface ListCallback {
        void result(List<IRecord> records);
        //TODO: void error()
    }

    KarttaDatabase database;

    MutableLiveData<List<IRecord>> recordListObservable = new MutableLiveData<>();

    public LiveData<List<IRecord>> getRecordListObservable() {
        return recordListObservable;
    }

    @Inject
    public KarttaRepository(KarttaDatabase database) {
        this.database = database;
        getRecords((targets -> recordListObservable.setValue(targets)));
    }

    public void insertTarget(Target target){
        Executor.execute(() -> {
            long id = database.targetDao().insert(target);
            if (id > 0) {
                target.setId(id);
                Point point = target.getPoint();
                point.setParent_id(id);
                database.pointDao().insert(point);
                getRecordsAndEmitResult();
            }
        });
    }

    public void removeRecord(IRecord record) {
        switch (record.getType()) {
            case TARGET:
                Executor.execute(() -> {
                    database.targetDao().delete(record.getId());
                    getRecordsAndEmitResult();
                });
                break;
            case AREA:
            case ROUTE:
                break;
        }

    }

    private void getRecords(ListCallback callback){
        Executor.execute(() -> {
            List<IRecord> records = new ArrayList<>();

            List<Target> targets = database.targetDao().getAllTargets();
            for (Target target : targets) {
                target.setPoint(database.pointDao().getPointForTarget(target.getId()));
            }
            records.addAll(targets);

            new Handler(Looper.getMainLooper()).post(() -> callback.result(records));
        });
    }

    /**
     * gets all records from db and emits them forward, so all observers get the latest data
     */
    private void getRecordsAndEmitResult(){
        getRecords((targets -> recordListObservable.setValue(targets)));
    }

}
