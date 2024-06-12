# Reverse Inbound

## What is it?

*Reverse Inbound* is not specified in the *JCA-specification* at all, it is an addition in *casual-jca* as the opposite
( *Reverse Outbound* ) is available in casual.

## How does it work

By specifying a reverse inbound section in the casual configuration file, such as:
```json
{
  "reverseInbound":[
    {
      "address": {
        "host": "some-reverse-outbound",
        "port": 7771
      },
      "size": 4,
      "maxConnectionBackoffMillis": 30000
    }
  ]
}
```

On *casual-jca* startup, this configuration is read and for each configuration an *outbound* connection towards the defined
host:port. If no size is specified then only one such connection is created per configuration.

After the connection has succeeded, this connection now works as it was a normal *inbound* connection.
It is still in fact a client though.

## Error handling

If `maxConnectionBackoffMillis` is specified for a reverse inbound connection then that value will be the maximum
connection backoff time if the target host isn't available. When nothing is configured a default value of 30 seconds for
maximum backoff time will be used. The backoff will be performed in increasingly larger steps and will start att 1/10 of
the maximum value and increase for each connection failure. The increments are 1/10, 1/9, 1/8 .. 1/3, 1/2, 1/1 of the
set max connection backoff value. Once the maximum backoff value is reached that value will be used for each connection
failure until a connection can be successfully established.

Failed connections will be re-scheduled on a `ScheduledExecutionService` which may be a default managed variant on the application server
(should be on JBoss/WildFly, WebLogic), otherwise a shared instance of `ScheduledThreadPoolExecutor` will be used. It
has a default pool size of `10` and the pool size of this shared `ScheduledThreadPoolExecutor` can be set through the
environment variable `CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE`.
