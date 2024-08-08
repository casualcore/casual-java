/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow;

import javax.transaction.xa.Xid;
import java.util.Objects;
import java.util.UUID;

public record WorkResponseContext(String parentName, String serviceName, Xid xid, UUID correlationId, UUID execution)
{
    public WorkResponseContext
    {
        Objects.requireNonNull(parentName, "parentName can not be null");
        Objects.requireNonNull(serviceName, "serviceName can not be null");
        Objects.requireNonNull(xid, "xid can not be null");
        Objects.requireNonNull(correlationId, "correlationId can not be null");
        Objects.requireNonNull(execution, "execution can not be null");
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private String parentName;
        private String serviceName;
        private Xid xid;
        private UUID correlationId;
        private UUID execution;
        public Builder withParentName(String parentName)
        {
            this.parentName = parentName;
            return this;
        }
        public Builder withServiceName(String serviceName)
        {
            this.serviceName = serviceName;
            return this;
        }
        public Builder withXid(Xid xid)
        {
            this.xid = xid;
            return this;
        }
        public Builder withCorrelationId(UUID correlationId)
        {
            this.correlationId = correlationId;
            return this;
        }
        public Builder withExecution(UUID execution)
        {
            this.execution = execution;
            return this;
        }
        public WorkResponseContext build()
        {
            return new WorkResponseContext(parentName, serviceName, xid, correlationId, execution);
        }
    }
}
