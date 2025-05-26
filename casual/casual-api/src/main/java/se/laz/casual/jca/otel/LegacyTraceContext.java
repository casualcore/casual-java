/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.otel;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public record LegacyTraceContext(UUID traceId, long parentSpan, String parentName)
{
    private static final int RADIX = 64;
    public static final String TRACER_NAME = "casual-jca";
    public LegacyTraceContext
    {
        Objects.requireNonNull(traceId, "traceId can not be null");
    }
    public static LegacyTraceContext of(UUID traceId)
    {
        Random random = new Random();
        BigInteger value = new BigInteger(RADIX, random);
        return new LegacyTraceContext(traceId, value.longValue(),"");
    }
    public Context toOtelContext()
    {
        String traceIdHex = traceId.toString().replace("-", "");
        String parentSpanHex = String.format("%016x", parentSpan);
        TraceState traceState = TraceState.builder().put("parentName", parentName).build();
        SpanContext spanContext = SpanContext.create(
                traceIdHex,
                parentSpanHex,
                TraceFlags.getDefault(),
                traceState
        );
        Span legacyParentSpan = Span.wrap(spanContext);
        return Context.current().with(legacyParentSpan);
    }
}
