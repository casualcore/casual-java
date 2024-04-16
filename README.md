# Casual Java Integration

The purpose of this project is to implement a 
java native communication implementation for 
casual-middleware. It also contains a JCA 1.7
ResourceAdapter to allow for distributed XA
transactions between a compliant JEE7+ server
and casual-middleware

## Note
This 3.2 branch is for Jakarta EE.
For Java EE see the 2.2 branch.

## Casual API

This project is the casual API, compare this 
to the javax:javaee-api

## Casual Network

This is the network implementation

## Casual network protocol

This is the network protocol implementation

## Casual JCA

This is the JCA ResourceAdapter

## Casual Caller

Abstraction layer on top of 1-n outbound connection pools

See [casual caller documentation](https://github.com/casualcore/casual-caller) for more in depth information.

## Further information
[Deployment](deployment.md)

[Configuration](configuration.md)

[Inbound Server](inbound.md)

[Reverse Inbound Server](reverse-inbound.md)

[Outbound](outbound.md)

[Event server](event-server.md)
[Event client](event-client.md)
