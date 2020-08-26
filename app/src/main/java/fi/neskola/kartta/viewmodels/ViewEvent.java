package fi.neskola.kartta.viewmodels;

import fi.neskola.kartta.models.IRecord;

public class ViewEvent {

    public enum Event {
        REQUEST_REMOVE
    }

    public IRecord record;
    public Event event;

    public ViewEvent(Event event, IRecord record) {
        this.event = event;
        this.record = record;
    }

}
