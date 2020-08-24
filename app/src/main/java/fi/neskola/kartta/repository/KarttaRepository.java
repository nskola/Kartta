package fi.neskola.kartta.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fi.neskola.kartta.database.Executor;
import fi.neskola.kartta.database.KarttaDatabase;
import fi.neskola.kartta.models.Point;
import fi.neskola.kartta.models.Target;

@Singleton
public class KarttaRepository {

    public interface Callback {
        void result(Target target);
        //TODO: void error()
    }

    KarttaDatabase database;

    MutableLiveData<List<Target>> targets = new MutableLiveData<>();

    @Inject
    public KarttaRepository(KarttaDatabase database) {
        this.database = database;
    }

    LiveData<List<Target>> getTargets() {
        return targets;
    }

    public void insertTarget(Target target, Callback callback){
        Executor.execute(() -> {
            long id = database.recordDao().insert(target);
            if (id > 0) {
                target.setId(id);
                Point point = target.getPoint();
                point.setParent_id(id);
                long pointId = database.pointDao().insert(point);
                point.setId(pointId);
                new Handler(Looper.getMainLooper()).post(() -> callback.result(target));
            }
        });
    }

}
