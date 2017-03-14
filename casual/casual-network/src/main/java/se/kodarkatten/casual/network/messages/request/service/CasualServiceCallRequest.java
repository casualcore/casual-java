package se.kodarkatten.casual.network.messages.request.service;

import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.network.messages.CasualNWMessageType;
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable;

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
}
