# Casual Outbound Server

## Configuration

You can configure which ManagedExecutorService should be used and also the number of threads
that should be passed on to the netty library.

Example configurations:
```json
{
   "outbound":
   {
     "managedExecutorServiceName": "java:comp/env/concurrent/casualManagedExecutorService",
     "numberOfThreads": 10
   }
}
```


```json
{
  "outbound":
  {
    "unmanaged": true,
    "useEpoll": true
  }
}
```

Note that the usage of both `unmanaged` and `useEpoll` in the outbound configuration section is `deprecated`.
You should instead configure these properties at the root level of the configuration.

See configuration(configuration.md) for more info regarding how configuration works in general.

It is fine to only configure one attribute, the other one will then use the default value.

If you configure to run *unmanaged*, that is no ManagedExecutor service will be used, you can still configure *numberOfThreads* if you so wish. 

### Default values

If you do not provide any configuration for outbound the defaults are:

* *java:comp/DefaultManagedExecutorService*

* *0*

* Netty uses NIO

0 for netty means: 
```java
Math.max(1, SystemPropertyUtil.getInt( "io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2))
```

If you are running on an older JVM in a containerized world, that might be suboptimal.


## Pool configuration

You must configure `NetworkConnectionPoolName` with a unique nonblank name and `NetworkConnectionPoolSize` with a positive integer. The adapter rejects missing or invalid network pool settings. Managed connections share the physical connections in the named network pool; unpooled physical connections are not supported.

Example for wildfly:
```
$connectionDefinitionNode/config-properties=NetworkConnectionPoolName:add(value=pool-one)
$connectionDefinitionNode/config-properties=NetworkConnectionPoolSize:add(value=1)
```

Note that each physical network connection is multiplexing on its own since we are running on top of Netty.
We recommend that you use a NetworkConnectionPoolSize of 1 for most cases.
