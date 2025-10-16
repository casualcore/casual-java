/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import java.util.Objects;
import java.util.UUID;

public record InboundThreadContext (SpanId spanId, String parentName, UUID execution)
{
    public InboundThreadContext
    {
        Objects.requireNonNull(spanId);
        Objects.requireNonNull(parentName);
        Objects.requireNonNull(execution);
    }
}
