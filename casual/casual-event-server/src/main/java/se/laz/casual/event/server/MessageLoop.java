package se.laz.casual.event.server;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface MessageLoop extends Consumer<Supplier<Boolean>>
{
    void handleMessages();
}
