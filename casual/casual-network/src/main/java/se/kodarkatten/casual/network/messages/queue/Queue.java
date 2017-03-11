package se.kodarkatten.casual.network.messages.queue;

import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;
import se.kodarkatten.casual.network.messages.parseinfo.DiscoveryReplySizes;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by aleph on 2017-03-07.
 */
public final class Queue
{
    private final String name;
    private long retries;
    private Queue(String name)
    {
        this.name = name;
    }

    public static Queue of(String name)
    {
        return new Queue(name);
    }

    public String getName()
    {
        return name;
    }

    public long getRetries()
    {
        return retries;
    }

    public Queue setRetries(long retries)
    {
        this.retries = retries;
        return this;
    }

    /**
     * We assume that everything for a Queue fits into Integer.MAX_VALUE
     * If not, we throw a CasualTransportException
     * @return
     */
    public List<byte[]> toNetworkBytes()
    {
        final List<byte[]> l = new ArrayList<>();
        final byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        final long networkSize = DiscoveryReplySizes.QUEUES_ELEMENT_SIZE.getNetworkSize() + nameBytes.length + (long)DiscoveryReplySizes.QUEUES_ELEMENT_RETRIES.getNetworkSize();
        if(networkSize > Integer.MAX_VALUE)
        {
            throw new CasualTransportException("Queue byte size is larger than Integer.MAX_VALUE: " + networkSize);
        }
        final ByteBuffer b = ByteBuffer.allocate((int)networkSize);
        b.putLong(nameBytes.length)
         .put(nameBytes)
         .putLong(retries);
        l.add(b.array());
        return l;
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
        Queue queue = (Queue) o;
        return retries == queue.retries &&
            Objects.equals(name, queue.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, retries);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("Queue{");
        sb.append("name='").append(name).append('\'');
        sb.append(", retries=").append(retries);
        sb.append('}');
        return sb.toString();
    }
}
