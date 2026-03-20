# Concurrency and Inbound Context Propagation

This is since casual-java v3.4.0 due to implementing tracing functionality.

When an inbound service call is received, casual-java automatically establishes a thread-local context
(`InboundThreadContext`) on the executing thread. This context carries tracing and correlation data
(span ID, parent service name, execution ID) that is transparently propagated to any outbound calls
made during the processing of that inbound request.

## The Problem

If your inbound service implementation offloads work to other threads — for example via
`ManagedExecutorService`, `ManagedScheduledExecutorService`, or `CompletableFuture` — the inbound
context is **not** automatically available on those threads. Any outbound casual-java calls made from
those threads will lack the tracing and correlation data from the originating inbound request.

## The Solution

The `se.laz.casual.api.concurrency.Concurrent` utility class provides `wrap()` methods that capture
the current inbound context and restore it on the executing thread.

Supported types:
* `Runnable`
* `Callable<T>`
* `Supplier<T>`

### Usage with ManagedExecutorService

```java
import se.laz.casual.api.concurrency.Concurrent;

@Resource
ManagedExecutorService executor;

@CasualService(name = "myService")
public InboundResponse myService(InboundRequest request)
{
    // The inbound context is active on this thread.
    // Wrap the task before submitting to preserve it.
    Future<String> future = executor.submit(Concurrent.wrap(() -> {
        // Inbound context is available here.
        // Outbound calls made here will carry the correct
        // span ID, parent name, and execution ID.
        return someOutboundCall();
    }));

    // ...
}
```

### Usage with CompletableFuture

```java
import se.laz.casual.api.concurrency.Concurrent;

@CasualService(name = "myService")
public InboundResponse myService(InboundRequest request)
{
    CompletableFuture.supplyAsync(Concurrent.wrap(() -> {
        // Inbound context is available here.
        return someOutboundCall();
    }), executor);

    // ...
}
```

### Usage with Runnable

```java
import se.laz.casual.api.concurrency.Concurrent;

@CasualService(name = "myService")
public InboundResponse myService(InboundRequest request)
{
    executor.submit(Concurrent.wrap(() -> {
        // Inbound context is available here.
        performOutboundWork();
    }));

    // ...
}
```

## When Wrapping Is Not Needed

If your inbound service processes the request entirely on the calling thread — the typical case —
no wrapping is needed. The context is managed automatically by the resource adapter.

## What Happens Without Wrapping

If a task is submitted to another thread **without** `Concurrent.wrap()`, any outbound casual-java calls
from that thread will behave as standalone outbound calls with no parent context. They will not carry
the span ID, parent service name, or execution ID from the originating inbound request. The calls
will still succeed, but tracing continuity will be lost.
