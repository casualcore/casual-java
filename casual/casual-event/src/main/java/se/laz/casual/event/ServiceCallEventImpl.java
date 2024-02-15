/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event;

import javax.transaction.xa.Xid;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ServiceCallEventImpl implements ServiceCallEvent
{
    private final String service;
    private final String parent;
    private final int processId;
    private final UUID execution;
    private final Xid transactionId;
    private final long start;
    private final long end;
    private final long pending;
    private final int code;
    private final Order order;

    private ServiceCallEventImpl(Builder builder)
    {
        service = builder.service;
        parent = builder.parent;
        processId = builder.processId;
        execution = builder.execution;
        transactionId = builder.transactionId;
        start = builder.start;
        end = builder.end;
        pending = builder.pending;
        code = builder.code;
        order = builder.order;
    }

    @Override
    public String getService()
    {
        return service;
    }

    @Override
    public Optional<String> getParent()
    {
        return Optional.of(parent);
    }

    @Override
    public int getProcessId()
    {
        return processId;
    }

    @Override
    public UUID getExecution()
    {
        return execution;
    }

    @Override
    public Xid getTransactionId()
    {
        return transactionId;
    }

    @Override
    public long getStart()
    {
        return start;
    }

    @Override
    public long getEnd()
    {
        return end;
    }

    @Override
    public long getPending()
    {
        return pending;
    }

    @Override
    public int getCode()
    {
        return code;
    }

    @Override
    public Order getOrder()
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
        private String parent;
        private int processId;
        private UUID execution;
        private Xid transactionId;
        private long start;
        private long end;
        private long pending;
        private int code;
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

        public Builder withPid(int processId)
        {
            this.processId = processId;
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

        public Builder withCode(int code)
        {
            this.code = code;
            return this;
        }

        public Builder withOrder(Order order)
        {
            this.order = order;
            return this;
        }

        public ServiceCallEventImpl build()
        {
            return new ServiceCallEventImpl(this);
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
        ServiceCallEventImpl that = (ServiceCallEventImpl) o;
        return getProcessId() == that.getProcessId() && getStart() == that.getStart() && getEnd() == that.getEnd() && getPending() == that.getPending() && getCode() == that.getCode() && Objects.equals(getService(), that.getService()) && Objects.equals(getParent(), that.getParent()) && Objects.equals(getExecution(), that.getExecution()) && Objects.equals(getTransactionId(), that.getTransactionId()) && getOrder() == that.getOrder();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getService(), getParent(), getProcessId(), getExecution(), getTransactionId(), getStart(), getEnd(), getPending(), getCode(), getOrder());
    }

    @Override
    public String toString()
    {
        return "ServiceCallEventImpl{" +
                "service='" + service + '\'' +
                ", parent='" + parent + '\'' +
                ", processId=" + processId +
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
