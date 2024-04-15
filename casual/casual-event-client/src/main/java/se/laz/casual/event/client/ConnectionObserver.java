package se.laz.casual.event.client;

@FunctionalInterface
public interface ConnectionObserver
{
    void connectionClosed();
}
