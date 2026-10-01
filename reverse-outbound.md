# Reverse outbound

## Overview

The JCA specification does not define *Reverse Outbound*. It is the symmetric counterpart to [Reverse Inbound](reverse-inbound.md).

In this model, the Enterprise Information System (EIS) initiates a TCP connection to a port configured in `casual-jca`.

After the handshake completes, `casual-jca` uses the incoming connection as a standard outbound connection.

## How it works

To enable Reverse Outbound, add a `reverseOutbound` section to your casual configuration file and a corresponding connection factory in 
your application server configuration:

Casual configuration:
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

Wildfly application server configuration:

Add a reverse outbound connections as a standard application server connection factory. 
Configure it as a pooled outbound connection factory, ensure you set `networkConnectionPoolName` to match the reverse outbound name:

```xml
<connection-definition class-name="se.laz.casual.jca.CasualManagedConnectionFactory"
                       jndi-name="java:/eis/casualReverse" ...>
    <config-property name="hostName">reverse</config-property>
    <config-property name="portNumber">0</config-property>
    <config-property name="networkConnectionPoolName">myReverseOutbound</config-property>
</connection-definition>

```
You must provide a nonblank pool name that matches your reverse outbound configuration. You can omit `networkConnectionPoolSize` for 
reverse outbound; if you specify it, the reverse pool ignores it. Normal outbound still requires a positive size. Reverse pools do not 
use `hostName` or `portNumber` to establish connections. The pool accepts connections established by the EIS.

You look up the connection factory through JNDI. The application server manages connection pooling and XA enlistment.


### Connection handshake

Connections to the reverse outbound are initiated by reverse inbound clients. These can be other `casual` and/or `casual-jca` instances.

A remote reverse inbound connection is initiated to a reverse outbound configured in `casual-jca`. 

`casual-jca` accepts the connection and issues a domain connect request. The reverse inbound side waits for the outbound side to initiate 
the domain connect request handshake. This is similar to a standard outbound connection, only `casual-jca` does not initiate the network 
connection, rather it listens for incoming network connections. 

Once the handshake completes, `NetworkPoolHandler` adds the connection to a named reverse outbound network connection pool keyed by the 
configured `name`.

**Note:** Connection names must be unique. Duplicate entries are skipped during startup.

Each connected instance behaves as if it has its own dedicated connection pool. The reverse pool holds all established connections.

A request that includes a domain ID matches or creates a managed connection pinned exclusively to that domain. The application server pools 
pinned and unpinned connections side by side. If the pinned instance is unavailable, connection allocation fails in the same manner as a 
total casual outage.

`casual-caller` exposes each connected instance as a standalone connection factory entry. With *n* connected reverse inbounds, 
`casual-caller` detects *n* distinct pools, each maintaining its own service discovery, validity state, and failover priority.

## Size the application server pool

A single reverse outbound definition backs *n* virtual pools (one per connected reverse inbound instance). Because a connection pinned to one 
domain cannot serve requests for another domain, the application server pool must hold managed connections for all reverse inbound instances 
simultaneously.

Configure your pool according to the following guidelines:

* **`max-pool-size`:** Set this to a high value—at least equal to the total expected concurrent calls across all instances, plus extra 
headroom for instance churn. A managed connection is not a physical socket; physical connections are managed by the reverse network pool. 
Setting this value too low causes allocations for one instance to stall behind connections pinned to other instances.
* **`min-pool-size`:** *MUST* be set this to `0`. Physical connections cannot be created by `casual-jca` but rather are added when remote instances connect.
* **`initial-pool-size`:** *MUST* be set this to `0`. As per `min-pool-size`.
* **Background validation:** Enable background validation so the server removes managed connections pinned to disconnected instances between calls.

## Error handling and operational behavior

If no EIS connection is available during a connection request, allocation fails in the same way as a standard outbound casual outage. 
You can retry acquisition after the EIS reconnects.

Multiple EIS instances can connect to the same listener; each connection is added to the pool automatically. If an EIS connection drops, it 
is removed from the pool, and the EIS is responsible for re-establishing the connection.

When Resource Adapter (RA) deactivation occurs, the system closes both the listening socket and all established connections. The reverse 
pool remains registered but empty until the EIS reconnects.

### Additional runtime characteristics

Reverse Outbound acts as a server listener and does not support custom pool sizing or backoff parameters.
