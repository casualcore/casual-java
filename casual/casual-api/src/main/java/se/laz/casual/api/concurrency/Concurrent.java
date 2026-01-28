/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.concurrency;

import se.laz.casual.jca.InboundThreadContext;
import se.laz.casual.jca.InboundThreadLocal;

import java.util.concurrent.Callable;

public class Concurrent
{
    public static Runnable wrap(Runnable task) {
        var current = InboundThreadLocal.getContext().orElse(null);
        if (current == null)
        {
            return task;
        }
        return () -> runWithContext(current, task);
    }

    public static <T> Callable<T> wrap(Callable<T> task) {
        var current = InboundThreadLocal.getContext().orElse(null);
        if (current == null)
        {
            return task;
        }
        return () -> callWithContext(current, task);
    }

    // This is needed due to the warning and unused variable and using java 17
    // From java 22 this can be handled with _
    @SuppressWarnings("try")
    private static void runWithContext(InboundThreadContext ctx, Runnable task) {
        try (var ignored = InboundThreadLocal.of(ctx)) {
            task.run();
        }
    }

    @SuppressWarnings("try")
    private static <T> T callWithContext(InboundThreadContext ctx, Callable<T> task) throws Exception {
        try (var ignored = InboundThreadLocal.of(ctx)) {
            return task.call();
        }
    }

}
