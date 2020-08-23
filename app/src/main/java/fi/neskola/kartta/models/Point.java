package fi.neskola.kartta.models;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

@Entity(tableName = "points")
public class Point {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "point_id")
    private long id;

    //foreign key
    @ColumnInfo(name = "fk_record_id")
    private long fk_record_id;

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "longitude")
    private double longitude;

    public Point(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Ignore
    public Point(LatLng latLng) {
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public LatLng getLatLng(){
        return new LatLng(latitude,longitude);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getFk_record_id() {
        return fk_record_id;
    }

    public void setFk_record_id(long fk_record_id) {
        this.fk_record_id = fk_record_id;
    }
}