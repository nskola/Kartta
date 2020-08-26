package fi.neskola.kartta.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import fi.neskola.kartta.models.Target;

@Dao
public interface TargetDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Target target);

    @Query("DELETE FROM targets")
    void deleteAll();

    @Query("SELECT * from targets ORDER BY target_id ASC")
    List<Target> getAllTargets();

    @Query("DELETE FROM targets WHERE target_id = :id")
    void delete(long id);

}
