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

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class OutboundTracing
{
    private OutboundTracing()
    {}
    /**
     * Creates a new outbound context with a fresh parent span that uses:
     * - the same global trace id (from the inbound context, if it exists)
     * - a new parent span id generated randomly as a 64-bit number, formatted as 16 hex digits.
     * @param execution - the execution, will be used in case there is not execution from previous inbound call
     */
    public static OutboundContext createNewOutboundContext(UUID execution)
    {
        Span currentSpan = Span.current();
        SpanContext currentSpanContext = currentSpan.getSpanContext();
        String traceIdHex;

        UUID traceId = null;
        if (currentSpanContext.isValid()) {
            // there is an inbound call: reuse its trace ID (which should be a 32-character hex string).
            traceIdHex = currentSpanContext.getTraceId();
        } else {
            // no inbound context exists: generate a new trace ID based on a newly generated UUID.
            traceId = execution;
            traceIdHex = traceId.toString().replace("-", "");
        }

        if(null == traceId)
        {
            traceId = UUIDConverter.convertTraceIdHexToUUID(traceIdHex);
        }

        // generate a new parent's span id as a random 64-bit number formatted as 16 hex digits.
        long randomParentSpan = ThreadLocalRandom.current().nextLong();
        String newParentSpanId = String.format("%016x", randomParentSpan);

        // create a new SpanContext with the determined trace id and new parent span id.
        SpanContext outboundSpanContext = SpanContext.create(
                traceIdHex,
                newParentSpanId,
                TraceFlags.getDefault(),
                TraceState.getDefault()
        );

        Span outboundParentSpan = Span.wrap(outboundSpanContext);
        return new OutboundContext(Context.current().with(outboundParentSpan), traceId, randomParentSpan);
    }
}
