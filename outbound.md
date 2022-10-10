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
    "unmanaged": true
  }
}
```

See configuration(configuration.md) for more info regarding how configuration works in general.

It is fine to only configure one attribute, the other one will then use the default value.

If you configure to run *unmanaged*, that is no ManagedExecutor service will be used, you can still configure *numberOfThreads* if you so wish. 

### Default values

If you do not provide any configuration for outbound the defaults are:

* *java:comp/DefaultManagedExecutorService*

* *0*


0 for netty means: 
```java
Math.max(1, SystemPropertyUtil.getInt( "io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2))
```

If you are running on an older JVM in a containerized world, that might be suboptimal.

## Pool configuration

Without any specific configuration the mapping of Managed Connection entries, pool entries, and physical network connection is 1 to 1.
You can specify the number of physical connections by specifying the network pool name and the number of connections you want.

Example for wildfly:
```
$connectionDefinitionNode/config-properties=NetworkConnectionPoolName:add(value=pool-one)
$connectionDefinitionNode/config-properties=NetworkConnectionPoolSize:add(value=1)
```

Note that each physical network connection is multiplexing on its own since we are running on top of Netty.
On another note, we recommend that if you are connecting via a load balancer - create different pools each with the  NetworkConnectionPoolSize of 1.
This means that for each pool, each ManagedConnection is still talking to the same EIS.
