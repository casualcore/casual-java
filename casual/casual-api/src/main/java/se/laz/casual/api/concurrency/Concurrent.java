/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.concurrency;

import se.laz.casual.jca.InboundContextScope;
import se.laz.casual.jca.InboundThreadContext;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Helper functions for the case when you use threading in your application that has the pattern of being called by
 * an inbound request.
 * This is to make sure that the tracing data is propagated to the new thread.
 */
public final class Concurrent
{
    private Concurrent()
    {}
    public static Runnable wrap(Runnable task) {
        var current = InboundContextScope.getContext().orElse(null);
        if (current == null)
        {
            return task;
        }
        return () -> runWithContext(current, task);
    }

    public static <T> Callable<T> wrap(Callable<T> task) {
        var current = InboundContextScope.getContext().orElse(null);
        if (current == null)
        {
            return task;
        }
        return () -> callWithContext(current, task);
    }

    public static <T> Supplier<T> wrap(Supplier<T> supplier)
    {
        var current = InboundContextScope.getContext().orElse(null);
        if (current == null) {
            return supplier;
        }
        return () -> supplyWithContext(current, supplier);
    }

    // This is needed due to the warning and unused variable and using java 17
    // From java 22 this can be handled with _
    @SuppressWarnings("try")
    private static <T> T supplyWithContext(InboundThreadContext ctx, Supplier<T> supplier)
    {
        try (var ignored = InboundContextScope.of(ctx))
        {
            return supplier.get();
        }
    }

    @SuppressWarnings("try")
    private static void runWithContext(InboundThreadContext ctx, Runnable task) {
        try (var ignored = InboundContextScope.of(ctx))
        {
            task.run();
        }
    }

    @SuppressWarnings("try")
    private static <T> T callWithContext(InboundThreadContext ctx, Callable<T> task) throws Exception {
        try (var ignored = InboundContextScope.of(ctx))
        {
            return task.call();
        }
    }

}
