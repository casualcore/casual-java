package se.kodarkatten.casual.jca.inbound.handler;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class InboundResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final boolean successful;
    private final List<byte[]> payload;

    private InboundResponse( boolean successful, List<byte[]> payload )
    {
        this.successful = successful;
        this.payload = payload;
    }

    public static InboundResponse of( boolean successful, List<byte[]> payload )
    {
        Objects.requireNonNull( payload, "Payload is null." );
        return new InboundResponse( successful, payload );
    }

    public boolean isSuccessful()
    {
        return this.successful;
    }

    public List<byte[]> getPayload( )
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
        InboundResponse that = (InboundResponse) o;
        return successful == that.successful &&
                Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(successful, payload);
    }

    @Override
    public String toString()
    {
        return "InboundResponse{" +
                "successful=" + successful +
                ", payload=" + payload +
                '}';
    }
}
