package net.azisaba.simpleproxy.api.event;

public class EventException extends RuntimeException {
    public EventException(String message, Throwable cause) {
        super(message, cause);
    }
}
