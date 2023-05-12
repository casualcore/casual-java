package se.laz.casual.network.outbound;

import java.util.Objects;
import java.util.UUID;

public class NetworkConnectionId
{
    private final UUID id = UUID.randomUUID();
    private NetworkConnectionId()
    {}
    public static NetworkConnectionId of()
    {
        return new NetworkConnectionId();
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
        NetworkConnectionId that = (NetworkConnectionId) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }

    @Override
    public String toString()
    {
        return "NetworkConnectionId{" +
                "id=" + id +
                '}';
    }
}
