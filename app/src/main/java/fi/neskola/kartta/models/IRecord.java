package fi.neskola.kartta.models;

import java.util.List;

public interface IRecord {
    long getId();
    String getName();
    RecordType getType();
    List<Point> getPoints();
}
