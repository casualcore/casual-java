# Reverse Outbound

## Overview

The JCA specification does not define *Reverse Outbound*. It is the symmetric counterpart to [Reverse Inbound](reverse-inbound.md). 

In this model, the Enterprise Information System (EIS) initiates a TCP connection to a port configured in `casual-jca`.

After the handshake completes, `casual-jca` uses the connection as a standard outbound connection.

## How it works

To enable Reverse Outbound, add a `reverseOutbound` section to your casual configuration file:

```json
{
  "reverseOutbound": [
    {
      "name": "myReverseOutbound",
      "port": 7773
    }
  ]
}

```

When a connection is accepted, `casual-jca` issues a domain connect request identical to an EIS-initiated connection. The reverse inbound side waits for the outbound side to initiate the handshake.

Once the handshake completes, `NetworkPoolHandler` adds the connection to a named reverse network connection pool keyed by the configured `name`.

> **Note:** Connection names must be unique. Duplicate entries are skipped during startup.

Each connected instance behaves as if it has its own dedicated connection pool. The reverse pool holds all established connections.

A request that includes a domain ID matches or creates a managed connection pinned exclusively to that domain. The application server pools pinned and unpinned connections side by side. If the pinned instance is unavailable, connection allocation fails in the same manner as a total casual outage.

`casual-caller` exposes each connected instance as a standalone connection factory entry. With *n* connected reverse inbounds, `casual-caller` detects *n* distinct pools, each maintaining its own service discovery, validity state, and failover priority.

## Consumption

Consume connections through a standard application server connection factory. Configure it like a pooled outbound connection factory, but set `networkConnectionPoolName` to your reverse outbound name:

```xml
<connection-definition class-name="se.laz.casual.jca.CasualManagedConnectionFactory"
                       jndi-name="java:/eis/casualReverse" ...>
    <config-property name="hostName">reverse</config-property>
    <config-property name="portNumber">0</config-property>
    <config-property name="networkConnectionPoolName">myReverseOutbound</config-property>
    <config-property name="networkConnectionPoolSize">1</config-property>
</connection-definition>

```

Reverse pools ignore `hostName`, `portNumber`, and `networkConnectionPoolSize`. The pool accepts as many connections as the EIS establishes.

Because the application server manages the connection factory, standard pooling, XA enlistment, and XA recovery operate normally. Applications—including `casual-caller`—look up the connection factory in JNDI using standard workflows.

## Size the application server pool

A single connection definition backs *n* virtual pools (one per connected instance). Because a connection pinned to one instance cannot serve requests for another, the application server pool must hold connections for all instances simultaneously.

Configure your pool according to the following guidelines:

* **`max-pool-size`:** Set this to a high value—at least equal to the total expected concurrent calls across all instances, plus extra headroom for instance churn. A managed connection is not a physical socket; physical connections are managed by the reverse network pool. Setting this value too low causes allocations for one instance to stall behind connections pinned to other instances.
* **`min-pool-size`:** Set this to `0`. Prefilling creates unpinned managed connections, whereas `casual-caller` requests only pinned connections.
* **Background Validation:** Enable background validation so the server removes managed connections pinned to disconnected instances between calls.

## Error handling and operational behavior

If no EIS connection is available during a connection request, allocation fails in the same way as a standard outbound casual outage. The application retries and succeeds once the EIS reconnects.

Multiple EIS instances can connect to the same listener; each connection is added to the pool automatically. If an EIS connection drops, it is removed from the pool, and the EIS is responsible for re-establishing the connection.

When Resource Adapter (RA) deactivation occurs, the system closes both the listening socket and all established connections. The reverse pool remains registered but empty until the EIS reconnects.

### Additional runtime characteristics

Reverse Outbound acts as a server listener and does not support custom pool sizing or backoff parameters.

```
