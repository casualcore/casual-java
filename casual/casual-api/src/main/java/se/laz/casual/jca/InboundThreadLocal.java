/*
 * Copyright (c) 2025 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import java.util.Objects;
import java.util.Optional;

public class InboundThreadLocal implements AutoCloseable
{
    private static final ThreadLocal<InboundThreadContext> THREAD_LOCAL = new ThreadLocal<>();
    private final InboundThreadContext previousContext;
    private boolean closed;
    private InboundThreadLocal(InboundThreadContext context)
    {
        this.previousContext = THREAD_LOCAL.get();
        THREAD_LOCAL.set(context);
    }
    public static InboundThreadLocal of(InboundThreadContext context)
    {
        Objects.requireNonNull(context);
        return new InboundThreadLocal(context);
    }
    @Override
    public void close()
    {
        if (!closed)
        {
            if (previousContext == null)
            {
                THREAD_LOCAL.remove();
            }
            else
            {
                THREAD_LOCAL.set(previousContext);
            }
            closed = true;
        }
    }
    public static Optional<InboundThreadContext> getContext()
    {
        return Optional.ofNullable(THREAD_LOCAL.get());
    }
}
