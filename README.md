# Casual Java Integration

The purpose of this project is to implement a 
java native communication implementation for 
casual-middleware. It also contains a JCA 1.7
ResourceAdapter to allow for distributed XA
transactions between a compliant JEE7+ server
and casual-middleware

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

See [casual caller documentation](casual/casual-caller-api/README.md) for more in depth information.

## Further information
[Deployment](deployment.md)

[Configuration](configuration.md)

[Inbound Server](inbound.md)

[Outbound](outbound.md)