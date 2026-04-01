/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.service;

import se.laz.casual.jca.SpanId;

import java.util.Objects;
import java.util.UUID;

public record OutboundContext(SpanId parentSpan, SpanId span, String parentName, UUID execution)
{
    public OutboundContext
    {
        Objects.requireNonNull(parentName);
        Objects.requireNonNull(execution);
    }
}
