package fi.neskola.kartta.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import fi.neskola.kartta.models.Record;

@Dao
public interface RecordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Record record);

    @Query("DELETE FROM records")
    void deleteAll();

    @Query("SELECT * from records ORDER BY record_id ASC")
    List<Record> getAllRecords();

}
