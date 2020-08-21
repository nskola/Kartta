package fi.neskola.kartta.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import java.util.List;

@Entity(tableName = "records")
public class Record {

    public enum Type {
        TARGET("target"),
        TRACK("track"),
        AREA("area");

        private final String text;

        Type(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "record_id")
    private long id;

    @ColumnInfo(name = "record_type")
    private Type type;

    @ColumnInfo(name = "name")
    private String name;

    //Local
    @Ignore
    private List<Point> points;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class TypeConverter {

        @androidx.room.TypeConverter
        public static String fromTypeToString(Type value) {
            return value.name();
        }

        @androidx.room.TypeConverter
        public static Type fromStringToType(String value) {
            return Type.valueOf(value);
        }

    }

}


