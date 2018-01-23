package se.kodarkatten.casual.network.utils;

import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.api.xa.XIDFormatType;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.ExecutionException;

/**
 * Created by aleph on 2017-03-21.
 */
public final class XIDUtils
{
    public static final int XID_FORMAT_NETWORK_SIZE = 8;
    public static final int XID_GTRID_NETWORK_SIZE = 8;
    public static final int XID_BQUAL_NETWORK_SIZE = 8;

    private XIDUtils()
    {
    }

    public static int getXIDNetworkSize(final Xid xid)
    {
        if (XIDFormatType.isNullType(xid.getFormatId()))
        {
            return XID_FORMAT_NETWORK_SIZE;
        }
        return XID_FORMAT_NETWORK_SIZE +
            XID_GTRID_NETWORK_SIZE +
            XID_BQUAL_NETWORK_SIZE +
            xid.getGlobalTransactionId().length + xid.getBranchQualifier().length;
    }

    public static Xid readXid(final AsynchronousByteChannel channel)
    {
        try
        {
            final ByteBuffer xidFormatBuffer = ByteUtils.readFully(channel, XID_FORMAT_NETWORK_SIZE).get();
            final long xidFormat = xidFormatBuffer.getLong();
            if (XIDFormatType.isNullType(xidFormat))
            {
                return XID.NULL_XID;
            }
            final ByteBuffer gtridAndBqualLengthBuffer = ByteUtils.readFully(channel, XID_GTRID_NETWORK_SIZE + XID_BQUAL_NETWORK_SIZE).get();
            final int gtrid = (int) gtridAndBqualLengthBuffer.getLong();
            final int bqual = (int) gtridAndBqualLengthBuffer.getLong();
            final ByteBuffer payload = ByteUtils.readFully(channel, gtrid + bqual).get();
            return XID.of(gtrid, bqual, payload.array(), xidFormat);
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualTransportException("failed reading xid", e);
        }
    }

    public static Xid readXid(final ReadableByteChannel channel)
    {
        final ByteBuffer xidFormatBuffer = ByteUtils.readFully(channel, XID_FORMAT_NETWORK_SIZE);
        final long xidFormat = xidFormatBuffer.getLong();
        if (XIDFormatType.isNullType(xidFormat))
        {
            return XID.NULL_XID;
        }
        final ByteBuffer gtridAndBqualLengthBuffer = ByteUtils.readFully(channel, XID_GTRID_NETWORK_SIZE + XID_BQUAL_NETWORK_SIZE);
        final int gtrid = (int) gtridAndBqualLengthBuffer.getLong();
        final int bqual = (int) gtridAndBqualLengthBuffer.getLong();
        final ByteBuffer payload = ByteUtils.readFully(channel, gtrid + bqual);
        return XID.of(gtrid, bqual, payload.array(), xidFormat);
    }

}
