package se.kodarkatten.casual.jca.inbound.handler;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class InboundRequest implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String serviceName;
    private final List<byte[]> payload;

    private InboundRequest(String serviceName, List<byte[]> payload )
    {
        this.serviceName = serviceName;
        this.payload = payload;
    }

    public static InboundRequest of( String serviceName, List<byte[]> payload )
    {
        Objects.requireNonNull( serviceName, "Service name is null." );
        Objects.requireNonNull( payload, "Payload is null." );
        return new InboundRequest( serviceName, payload );
    }

    public String getServiceName()
    {
        return this.serviceName;
    }

    public List<byte[]> getPayload()
    {
        return this.payload;
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
        InboundRequest that = (InboundRequest) o;
        return Objects.equals(serviceName, that.serviceName) &&
                Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(serviceName, payload);
    }

    @Override
    public String toString()
    {
        return "InboundRequest{" +
                "serviceName='" + serviceName + '\'' +
                ", payload=" + payload +
                '}';
    }
}
