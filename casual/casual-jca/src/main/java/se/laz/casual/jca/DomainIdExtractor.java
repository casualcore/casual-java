/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import jakarta.resource.spi.ConnectionRequestInfo;

import java.util.Optional;

public final class DomainIdExtractor
{
    private DomainIdExtractor() {}
    public static Optional<DomainId> getDomainId(ConnectionRequestInfo cxRequestInfo)
    {
        return cxRequestInfo instanceof CasualRequestInfo requestInfo ? requestInfo.getDomainId() : Optional.empty();
    }
}
