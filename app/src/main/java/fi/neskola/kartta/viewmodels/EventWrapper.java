package fi.neskola.kartta.viewmodels;

/**
 * Wrapper class for events, because we want emit them only single time
 * @param <T> event
 */
public class EventWrapper<T> {

    private boolean hasBeenHandled = false;
    private T content;

    public EventWrapper(T content) {
        this.content = content;
    }

    /**
     * Returns the content and prevents its use again.
     */
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

}
