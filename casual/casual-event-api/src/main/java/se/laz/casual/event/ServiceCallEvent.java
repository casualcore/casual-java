/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event;

import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.util.PrettyPrinter;

import javax.transaction.xa.Xid;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    private final char order;

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
        order = builder.order.getValue();
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

    public char getOrder()
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

        public Builder withStart(Instant start)
        {
            // from ChronoUnit.between:
            // Implementations should perform any queries or calculations using the units available in ChronoUnit
            // or the fields available in ChronoField.
            // If the unit is not supported an UnsupportedTemporalTypeException must be thrown.
            // Implementations must not alter the specified temporal objects.
            //
            // On the platform what we currently support, this is not a problem
            // We also do not know of any platform where this does not work
            // The same goes for end below
            this.start = ChronoUnit.MICROS.between(Instant.EPOCH, start);
            return this;
        }

        public Builder withEnd(Instant end)
        {
            this.end = ChronoUnit.MICROS.between(Instant.EPOCH, end);
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
        return getPid() == that.getPid() && getStart() == that.getStart() && getEnd() == that.getEnd() && getPending() == that.getPending() && getOrder() == that.getOrder() && Objects.equals(getService(), that.getService()) && Objects.equals(getParent(), that.getParent()) && Objects.equals(getExecution(), that.getExecution()) && Objects.equals(getTransactionId(), that.getTransactionId()) && Objects.equals(getCode(), that.getCode());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getService(), getParent(), getPid(), getExecution(), getTransactionId(), getStart(), getEnd(), getPending(), getCode(), getOrder());
    }

    @Override
    public String toString()
    {
        return "ServiceCallEvent{" +
                "service='" + service + '\'' +
                ", parent='" + parent + '\'' +
                ", pid=" + pid +
                ", execution='" + execution + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", pending=" + pending +
                ", code='" + code + '\'' +
                ", order=" + order +
                '}';
    }
}
