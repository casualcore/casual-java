/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.otel;

import io.opentelemetry.context.Context;

import java.util.Objects;
import java.util.UUID;

public record OutboundContext(Context context, UUID traceId, long span, String parentName)
{
    public OutboundContext
    {
        Objects.requireNonNull(context, "context can not be null");
        Objects.requireNonNull(traceId, "traceId can not be null");
        Objects.requireNonNull(parentName, "parentName can not be null");
    }
}
