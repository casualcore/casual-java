/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import java.util.Objects;
import java.util.Optional;

public class InboundThreadLocal implements AutoCloseable
{
    private static final InheritableThreadLocal<InboundThreadContext> THREAD_LOCAL = new InheritableThreadLocal<>();
    private InboundThreadLocal(InboundThreadContext context)
    {
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
        THREAD_LOCAL.remove();
    }
    public static Optional<InboundThreadContext> getContext()
    {
        return Optional.ofNullable(THREAD_LOCAL.get());
    }
}
