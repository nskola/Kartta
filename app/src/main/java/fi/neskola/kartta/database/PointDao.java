package fi.neskola.kartta.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import fi.neskola.kartta.models.Point;

@Dao
public interface PointDao {
    // allowing the insert of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Point point);

    @Query("DELETE FROM points")
    void deleteAll();

    @Query("SELECT * from points WHERE parent_id = :parent_id")
    List<Point> getPointsForRecord(long parent_id);

}
