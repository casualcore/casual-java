package se.kodarkatten.casual.network.io.writers.utils;

import se.kodarkatten.casual.api.xa.XIDFormatType;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-07.
 */
public final class CasualNetworkWriterUtils
{
    private CasualNetworkWriterUtils()
    {}

    public static ByteBuffer writeDynamicArray(ByteBuffer b, List<byte[]> data)
    {
        if(!data.isEmpty())
        {
            b.putLong(data.size());
            for(int i = 0; i < data.size(); ++i)
            {
                b.putLong(data.get(i).length);
                b.put(data.get(i));
            }
        }
        else
        {
            // need to inform that there's nothing to read
            b.putLong(0l);
        }
        return b;
    }

    public static void writeDynamicArray(List<byte[]> l, List<byte[]> data, int headerSize, int elementSize)
    {
        if(!data.isEmpty())
        {
            ByteBuffer header = ByteBuffer.allocate(headerSize);
            header.putLong(data.size());
            l.add(header.array());
            for(int i = 0; i < data.size(); ++i)
            {
                ByteBuffer elementSizeBuffer = ByteBuffer.allocate(elementSize);
                final byte[] elementData = data.get(i);
                elementSizeBuffer.putLong(elementData.length);
                l.add(elementSizeBuffer.array());
                l.add(elementData);
            }
        }
        else
        {
            // need to inform that there's nothing to read
            final ByteBuffer nothingToRead = ByteBuffer.allocate(headerSize);
            nothingToRead.putLong(0l);
            l.add(nothingToRead.array());
        }
    }

    public static ByteBuffer writeUUID(final UUID id, final ByteBuffer b)
    {
        b.putLong(id.getMostSignificantBits())
         .putLong(id.getLeastSignificantBits());
        return b;
    }

    public static ByteBuffer writeXID(final Xid xid, final ByteBuffer b)
    {
        b.putLong(xid.getFormatId());
        if(!XIDFormatType.isNullType(xid.getFormatId()))
        {
            final byte[] gtridId = xid.getGlobalTransactionId();
            final byte[] bqual = xid.getBranchQualifier();
            b.putLong(gtridId.length)
             .putLong(bqual.length)
             .put(gtridId)
             .put(bqual);
        }
        return b;
    }

    public static byte[] writeLong(long v)
    {
        ByteBuffer b = ByteBuffer.allocate(Long.BYTES);
        b.putLong(v);
        return b.array();
    }

}
