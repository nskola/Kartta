package fi.neskola.kartta.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import fi.neskola.kartta.models.Point;

@Dao
public interface PointDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Point point);

    @Query("DELETE FROM points")
    void deleteAll();

    @Query("SELECT * from points WHERE parent_id = :parent_id")
    List<Point> getPointsForParent(long parent_id);

    @Query("SELECT * from points WHERE parent_id = :target_id")
    Point getPointForTarget(long target_id);

}
