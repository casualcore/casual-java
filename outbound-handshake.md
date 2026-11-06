# Outbound handshake

Normal outbound and reverse outbound connections use the same domain-connect handshake after they establish a TCP channel. Normal outbound
opens the channel, while reverse outbound accepts a channel opened by a reverse inbound peer.

## Connection establishment

- Normal outbound opens the TCP channel and performs the handshake on the calling thread.
- Reverse outbound accepts the TCP channel and schedules the handshake using an executor. The handshake must not block the Netty event
  loop that decodes its reply. After the handshake completes, reverse outbound passes the initialized connection to the configured connection
  consumer, which adds it to the virtual network pool.

## Handshake sequence

The handshake establishes the peer domain ID and negotiated protocol version before the connection handles any potential unsolicited messages -
`DOMAIN_DISCOVERY_TOPOLOGY_UPDATE` or `DOMAIN_DISCONNECT_REQUEST`.

1. `NettyNetworkConnection` registers itself as the outbound message listener with `CasualMessageHandler`. The connection owns the domain ID
   holder, receives read-only access to the protocol holder, and installs domain-disconnect handling with access to the domain ID supplier.
2. `NettyNetworkConnection` sends a `DOMAIN_CONNECT_REQUEST` that contains the supported protocol versions.
3. The peer selects a protocol version and sends a `DOMAIN_CONNECT_REPLY`.
4. `CasualNWMessageDecoder` decodes the reply and immediately stores the selected version and peer domain ID in the outbound connection value holders.
5. Because `DOMAIN_CONNECT_REPLY` is a correlated response rather than an unsolicited message, `CasualMessageHandler` passes it to the
   correlator, which completes the handshake request.
6. The waiting handshake thread resumes and makes the initialized connection available to its consumer.

```mermaid
sequenceDiagram
    participant H as Handshake thread
    participant C as NettyNetworkConnection
    participant E as Netty event loop
    participant P as Peer domain

    H->>C: Install listener and start handshake
    C->>P: DOMAIN_CONNECT_REQUEST
    P-->>E: DOMAIN_CONNECT_REPLY
    E->>E: Decode reply and store protocol version and domain ID
    E->>E: Complete correlated request
    E-->>H: Release handshake wait
    H->>H: Publish initialized connection
    P-->>E: Unsolicited message
    E->>C: Route message to connection listener
```

## Ordering guarantee

Netty processes inbound events serially for an individual channel. The decoder and message handler therefore finish the connect-reply steps
before the same channel handles a following frame. Other channels can perform their handshakes concurrently without changing this
per-channel ordering.

Storing the negotiated version and peer domain ID in the decoder lets `NettyNetworkConnection` use one source for each value. The decoder
stores both values before the message handler completes the correlated request. Completing both actions before the correlator releases the
handshake thread prevents a following frame from overtaking connection initialization.

The supported unsolicited messages depend on the negotiated protocol version:

- Version 1.0 does not support unsolicited outbound messages.
- Version 1.1 adds domain-disconnect messages.
- Version 1.2 adds topology-update messages.

## Handshake completion

Normal outbound returns the connection to its caller only after the handshake completes. Reverse outbound adds the accepted connection to its
named network pool only after the same handshake completes. If the handshake fails, the connection closes the channel and does not make the
connection available.
