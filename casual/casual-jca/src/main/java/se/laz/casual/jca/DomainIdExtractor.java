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

    /**
     * Returns the domain ID carried by the supplied connection request information.
     *
     * <p>The JCA API uses {@code null} when a caller does not supply connection-specific
     * information. This method treats {@code null} and unsupported request-information types as
     * requests without a domain ID.
     *
     * @param cxRequestInfo the connection request information, or {@code null} when none is supplied
     * @return the requested domain ID, or an empty value when no domain ID is supplied
     */
    public static Optional<DomainId> getDomainId(ConnectionRequestInfo cxRequestInfo)
    {
        return cxRequestInfo instanceof CasualRequestInfo requestInfo ? requestInfo.getDomainId() : Optional.empty();
    }
}
