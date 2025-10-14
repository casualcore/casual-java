/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import javax.transaction.xa.Xid;
import java.util.Objects;
import java.util.UUID;

public record InboundThreadContext (UUID execution, Xid xid)
{
    public InboundThreadContext
    {
        Objects.requireNonNull(execution);
        Objects.requireNonNull(xid);
    }
}
