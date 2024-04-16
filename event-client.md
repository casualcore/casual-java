# Event client

The purpose of the event client is to provide an easy and efficient way to consume events from the [Event server](event-server.md).

Example usage:

```java
EventClientInformation clientInformation = EventClientInformation.createBuilder()
                                                                 .withChannelClass(EpollSocketChannel.class)
                                                                 .withEventLoopGroup(new EpollEventLoopGroup())
                                                                 .withConnectionInformation(new ConnectionInformation("localhost", 7289))
                                                                 .build();
EventClient client = EventClient.of(clientInformation, eventObserver, connectionObserver,  enableLogging);
client.connect();

```

As soon as the client is connected, any events published by the event server will be available via the eventObserver.
If the connection goes way, the connectionObserver is notified.
