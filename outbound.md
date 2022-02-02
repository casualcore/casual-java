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

