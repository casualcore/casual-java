/*
 * Copyright (c) 2025 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import java.util.Objects;
import java.util.Optional;

/**
 * A scoped {@link ThreadLocal} holder for {@link InboundThreadContext}.
 *
 * <p>Implements {@link AutoCloseable} so it can be used in a try-with-resources block to
 * guarantee that the thread-local context is restored when the scope ends.
 * On close, the previous context is reinstated — or the thread-local is removed entirely
 * if there was no previous context — preventing thread-local leaks.</p>
 *
 * <p>Usage example:</p>
 * <pre>{
 * try (var ignored = InboundContextScope.of(context))
 * {
 *     // InboundContextScope.getContext() returns this context on the current thread
 *     // Nested scopes are supported; closing restores the outer context.
 * }
 * </pre>
 *
 * @see InboundThreadContext
 */
public class InboundContextScope implements AutoCloseable
{
    private static final ThreadLocal<InboundThreadContext> THREAD_LOCAL = new ThreadLocal<>();
    private final InboundThreadContext previousContext;
    private boolean closed;
    private InboundContextScope(InboundThreadContext context)
    {
        this.previousContext = THREAD_LOCAL.get();
        THREAD_LOCAL.set(context);
    }
    public static InboundContextScope of(InboundThreadContext context)
    {
        Objects.requireNonNull(context);
        return new InboundContextScope(context);
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
    public static void remove()
    {
        THREAD_LOCAL.remove();
    }
}
