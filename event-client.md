# Event client

The purpose of the event client is to provide an easy and efficient way to consume events from the [Event server](event-server.md).

Example usage:

```java
EventClient client = EventClient.createBuilder()
                                .withHost("localhost")
                                .withPort(7698)
                                .withEventObserver (eventObserver)
                                .withConnectionObserver(connectionObserver)
                                .build();

```

This will create a client that uses NIO channel class and event loop group, if you want to select something more appropriate for your platform - please feel free to do so.

As soon as the client is connected, any events published by the event server will be available via the eventObserver.
If the connection goes way, the connectionObserver is notified.
