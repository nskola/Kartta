package fi.neskola.kartta.models;

public interface IRecord {
    long getId();
    String getName();
    RecordType getType();
}
