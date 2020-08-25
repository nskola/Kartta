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

    public interface ListCallback {
        void result(List<Target> targets);
        //TODO: void error()
    }

    KarttaDatabase database;

    MutableLiveData<List<Target>> targetListObservable = new MutableLiveData<>();

    public LiveData<List<Target>> getTargetListObservable() {
        return targetListObservable;
    }

    @Inject
    public KarttaRepository(KarttaDatabase database) {
        this.database = database;
        getTargets((targets -> targetListObservable.setValue(targets)));
    }

    public void insertTarget(Target target){
        Executor.execute(() -> {
            long id = database.targetDao().insert(target);
            if (id > 0) {
                target.setId(id);
                Point point = target.getPoint();
                point.setParent_id(id);
                database.pointDao().insert(point);
                getTargetsAndEmitResult();
            }
        });
    }

    private void getTargets(ListCallback callback){
        Executor.execute(() -> {
            List<Target> targets = database.targetDao().getAllTargets();
            for (Target target : targets) {
                target.setPoint(database.pointDao().getPointForTarget(target.getId()));
            }
            new Handler(Looper.getMainLooper()).post(() -> callback.result(targets));
        });
    }

    private void getTargetsAndEmitResult(){
        getTargets((targets -> targetListObservable.setValue(targets)));
    }

}
