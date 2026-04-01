/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.service;

import se.laz.casual.jca.InboundContextScope;
import se.laz.casual.jca.InboundThreadContext;
import se.laz.casual.jca.SpanId;
import se.laz.casual.network.ProtocolVersion;

import java.util.Optional;
import java.util.UUID;

import static se.laz.casual.network.ProtocolVersion.VERSION_1_3;

public final class OutboundContextCreator
{
    private OutboundContextCreator()
    {}
    public static OutboundContext create(UUID execution, ProtocolVersion protocolVersion)
    {
        return protocolVersion.isGreaterThanOrEqualTo( VERSION_1_3 )
                ? createForProtocolVersionThatSupportsSpanId(execution)
                : createForProtocolThatDoesNotSupportSpanId(execution);
    }

    private static OutboundContext createForProtocolThatDoesNotSupportSpanId(UUID execution)
    {
        Optional<InboundThreadContext> inboundThreadContext = InboundContextScope.getContext();
        // we still want to propagate the inbound execution and parentName if available ( call comes from inbound)
        UUID effectiveExecution = inboundThreadContext.map(InboundThreadContext::execution)
                                                      .orElse(execution);
        String parentName = inboundThreadContext.map(InboundThreadContext::parentName)
                                                .orElse("");
        return new OutboundContext(null, null, parentName, effectiveExecution);
    }

    private static OutboundContext createForProtocolVersionThatSupportsSpanId(UUID execution)
    {
        Optional<InboundThreadContext> inboundThreadContext = InboundContextScope.getContext();
        SpanId parentSpan = inboundThreadContext.map(InboundThreadContext::spanId)
                                                .orElse(null);
        String parentName = inboundThreadContext.map(InboundThreadContext::parentName)
                                                .orElse("");
        UUID effectiveExecution = inboundThreadContext.map(InboundThreadContext::execution)
                                                      .orElse(execution);
        return new OutboundContext(parentSpan, SpanId.of(), parentName, effectiveExecution);
    }
}
