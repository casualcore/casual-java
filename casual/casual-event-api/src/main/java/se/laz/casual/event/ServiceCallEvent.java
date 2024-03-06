/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event;

import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.util.PrettyPrinter;

import javax.transaction.xa.Xid;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ServiceCallEvent
{
    private final String service;
    private final String parent;
    private final long pid;
    private final String execution;
    private final String transactionId;
    private final long start;
    private final long end;
    private final long pending;
    private final String code;
    private final String order;

    private ServiceCallEvent(Builder builder)
    {
        service = builder.service;
        parent = builder.parent;
        pid = builder.pid;
        execution = PrettyPrinter.casualStringify(builder.execution);
        transactionId = PrettyPrinter.casualStringify(builder.transactionId);
        start = builder.start;
        end = builder.end;
        pending = builder.pending;
        code = builder.code.name();
        order = builder.order.name();
    }


    public String getService()
    {
        return service;
    }

    public Optional<String> getParent()
    {
        return Optional.of(parent);
    }

    public long getPid()
    {
        return pid;
    }

    public String getExecution()
    {
        return execution;
    }

    public String getTransactionId()
    {
        return transactionId;
    }

    public long getStart()
    {
        return start;
    }

    public long getEnd()
    {
        return end;
    }

    public long getPending()
    {
        return pending;
    }

    public String getCode()
    {
        return code;
    }

    public String getOrder()
    {
        return order;
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private String service;
        private String parent = "";
        private long pid = Process.pid();
        private UUID execution;
        private Xid transactionId;
        private long start;
        private long end;
        private long pending;
        private ErrorState code;
        private Order order;

        public Builder withService(String service)
        {
            this.service = service;
            return this;
        }

        public Builder withParent(String parent)
        {
            this.parent = parent;
            return this;
        }

        public Builder withExecution(UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public Builder withTransactionId(Xid transactionId)
        {
            this.transactionId = transactionId;
            return this;
        }

        public Builder withStart(long start)
        {
            this.start = start;
            return this;
        }

        public Builder withEnd(long end)
        {
            this.end = end;
            return this;
        }

        public Builder withPending(long pending)
        {
            this.pending = pending;
            return this;
        }

        public Builder withCode(ErrorState code)
        {
            this.code = code;
            return this;
        }

        public Builder withPID(long pid)
        {
            this.pid = pid;
            return this;
        }

        public Builder withOrder(Order order)
        {
            this.order = order;
            return this;
        }

        public ServiceCallEvent build()
        {
            return new ServiceCallEvent(this);
        }

    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        ServiceCallEvent that = (ServiceCallEvent) o;
        return getPid() == that.getPid() && getStart() == that.getStart() && getEnd() == that.getEnd() && getPending() == that.getPending() && getCode() == that.getCode() && Objects.equals(getService(), that.getService()) && Objects.equals(getParent(), that.getParent()) && Objects.equals(getExecution(), that.getExecution()) && Objects.equals(getTransactionId(), that.getTransactionId()) && getOrder() == that.getOrder();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getService(), getParent(), getPid(), getExecution(), getTransactionId(), getStart(), getEnd(), getPending(), getCode(), getOrder());
    }

    @Override
    public String toString()
    {
        return "ServiceCallEventImpl{" +
                "service='" + service + '\'' +
                ", parent='" + parent + '\'' +
                ", domainId=" + pid +
                ", execution=" + execution +
                ", transactionId=" + transactionId +
                ", start=" + start +
                ", end=" + end +
                ", pending=" + pending +
                ", code=" + code +
                ", order=" + order +
                '}';
    }
}
