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

import java.util.concurrent.ThreadLocalRandom;

public class OutboundTracing
{
    private OutboundTracing()
    {}
    /**
     * Creates a new outbound context with a fresh parent span that uses:
     * - The same global trace id (from the inbound context).
     * - A new parent span id generated randomly as a 64-bit number, formatted as 16 hex digits.
     */
    public static Context createNewOutboundContext()
    {
        // Get the current active span from the inbound context.
        Span inboundSpan = Span.current();
        SpanContext inboundSpanContext = inboundSpan.getSpanContext();
        // Extract the trace id (32-character hex string).
        String traceIdHex = inboundSpanContext.getTraceId();

        // Generate a new parent span id as a random 64-bit number.
        long randomParentSpan = ThreadLocalRandom.current().nextLong();
        // Convert to a 16-digit hexadecimal string.
        String newParentSpanId = String.format("%016x", randomParentSpan);

        // Build a new SpanContext for the outbound request, preserving the trace id.
        SpanContext outboundSpanContext = SpanContext.create(
                traceIdHex,       // global trace id remains the same.
                newParentSpanId,  // new random parent's span id.
                TraceFlags.getDefault(),
                TraceState.getDefault()
        );

        // Wrap this SpanContext as a Span and add it to a new Context.
        Span outboundParentSpan = Span.wrap(outboundSpanContext);
        return Context.current().with(outboundParentSpan);
    }
}
