package se.kodarkatten.casual.network.messages.request.service;

import se.kodarkatten.casual.api.flags.AtmiFlags;
import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.network.messages.CasualNWMessageType;
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable;
import se.kodarkatten.casual.network.messages.common.ServiceBuffer;

import java.util.List;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-14.
 */
public final class CasualServiceCallRequest implements CasualNetworkTransmittable
{
    private UUID execution;
    private int callDescriptor;
    private String serviceName;
    private long timeout;
    private String parentName;
    private XID xid;
    private Flag<AtmiFlags> xatmiFlags;
    private ServiceBuffer serviceBuffer;

    private CasualServiceCallRequest()
    {}

    @Override
    public CasualNWMessageType getType()
    {
        return CasualNWMessageType.SERVICE_CALL_REQUEST;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        return null;
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public UUID getExecution()
    {
        return execution;
    }

    public int getCallDescriptor()
    {
        return callDescriptor;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public String getParentName()
    {
        return parentName;
    }

    public XID getXid()
    {
        return XID.of(xid);
    }

    public Flag<AtmiFlags> getXatmiFlags()
    {
        return Flag.of(xatmiFlags);
    }

    /**
     * Note, not immutable
     * @return
     */
    public ServiceBuffer getServiceBuffer()
    {
        return serviceBuffer;
    }

    public static class Builder
    {
        private UUID execution;
        private int callDescriptor;
        private String serviceName;
        private long timeout;
        private String parentName;
        private XID xid;
        private Flag<AtmiFlags> xatmiFlags;
        private ServiceBuffer serviceBuffer;

        public Builder setExecution(UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public Builder setCallDescriptor(int callDescriptor)
        {
            this.callDescriptor = callDescriptor;
            return this;
        }

        public Builder setServiceName(String serviceName)
        {
            this.serviceName = serviceName;
            return this;
        }

        public Builder setTimeout(long timeout)
        {
            this.timeout = timeout;
            return this;
        }

        public Builder setParentName(String parentName)
        {
            this.parentName = parentName;
            return this;
        }

        public Builder setXid(XID xid)
        {
            this.xid = xid;
            return this;
        }

        public Builder setXatmiFlags(Flag<AtmiFlags> xatmiFlags)
        {
            this.xatmiFlags = xatmiFlags;
            return this;
        }

        public Builder setServiceBuffer(ServiceBuffer serviceBuffer)
        {
            this.serviceBuffer = serviceBuffer;
            return this;
        }
        public CasualServiceCallRequest build()
        {
            CasualServiceCallRequest r = new CasualServiceCallRequest();
            r.execution = execution;
            r.callDescriptor = callDescriptor;
            r.serviceName = serviceName;
            r.timeout = timeout;
            r.parentName = parentName;
            r.xid = XID.of(xid);
            r.xatmiFlags = xatmiFlags;
            r.serviceBuffer = serviceBuffer;
            return r;
        }
    }

}
