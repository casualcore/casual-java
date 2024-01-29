package se.laz.casual.event;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class EventInformation
{
    private static final Deque<ServiceCallEvent> EVENTS = new ConcurrentLinkedDeque<>();
    public static void store(ServiceCallEvent event)
    {
        EVENTS.add(event);
    }
}
