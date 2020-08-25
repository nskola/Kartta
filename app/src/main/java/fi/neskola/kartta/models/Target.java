package fi.neskola.kartta.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "targets")
public class Target implements IRecord {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "target_id")
    private long id;

    @ColumnInfo(name = "name")
    private String name;

    //Local
    @Ignore
    private Point point = null;

    public Target(String name) {
        this.name = name;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public RecordType getType() {
        return RecordType.TARGET;
    }

    public void setId(long id) {
        this.id = id;
    }



    public void setName(String name) {
        this.name = name;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }
}


