package se.kodarkatten.casual.api.xa;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import javax.transaction.xa.Xid;

/**
 * Created by aleph on 2017-03-14.
 */
public final class XID implements Xid
{
    public static final int MAX_XID_DATA_SIZE = 128;
    // default to null format
    private final long formatType;
    // length of the transaction gtrid part
    private final int gtridLength;
    // length of the transaction branch part
    private final int bqualLength;
    // (size = gtridLength + bqualLength) <= MAX_XID_DATA_SIZE
    private final byte[] globalTransactionId;
    private final byte[] branchQualifier;
    private XID(int gtridLength, int bqualLength, long formatType, final byte[] globalTransactionId, final byte[] branchQualifier)
    {
        this.gtridLength = gtridLength;
        this.bqualLength = bqualLength;
        this.formatType = formatType;
        this.globalTransactionId = globalTransactionId;
        this.branchQualifier = branchQualifier;
    }

    private XID()
    {
        this(0, 0, XIDFormatType.NULL.getType(), null, null);
    }

    /**
     * Null XID
     * @return
     */
    public static Xid of()
    {
        return new XID();
    }

    public static Xid of(final Xid xid)
    {
        if(XIDFormatType.isNullType(xid.getFormatId()))
        {
            return XID.of();
        }
        final byte[] gtridId = xid.getGlobalTransactionId();
        final byte[] bqual = xid.getBranchQualifier();
        return new XID(gtridId.length, bqual.length, xid.getFormatId(), gtridId, bqual);
    }

    public static Xid of(int gtridLength, int bqualLength, final byte[] data, final long formatType)
    {
        Objects.requireNonNull(data, "data for xid can not be null!");
        if((gtridLength + bqualLength) != data.length)
        {
            throw new XIDException("(gtridLength + bqualLength) != data.length " + "(" + gtridLength + " + " + bqualLength + ") != " + data.length);
        }
        else if((gtridLength + bqualLength) > MAX_XID_DATA_SIZE)
        {
            throw new XIDException("xid total length > MAX_XID_DATA_SIZE " + (gtridLength + bqualLength) + " > " + MAX_XID_DATA_SIZE);
        }
        final byte[] globalTransactionId = (0 == gtridLength) ? null : Arrays.copyOf(data, gtridLength);
        final byte[] branchQualifier = (0 == bqualLength) ? null : Arrays.copyOfRange(data, gtridLength, gtridLength + bqualLength);
        return XID.of(globalTransactionId, branchQualifier, formatType);
    }

    public static Xid of(final byte[] globalTransactionId, final byte[] branchQualifier, final long formatType)
    {
        int gtridLength = (null != globalTransactionId) ? globalTransactionId.length : 0;
        int bqualLength = (null != branchQualifier) ? branchQualifier.length : 0;
        final int totalLength = gtridLength + bqualLength;
        if(totalLength > MAX_XID_DATA_SIZE)
        {
            throw new XIDException("xid total length > MAX_XID_DATA_SIZE " + totalLength + " > " + MAX_XID_DATA_SIZE);
        }
        else if(isNullFormatTypeButAdditionalInformationProvided(globalTransactionId, branchQualifier, formatType))
        {
            throw new XIDException("You can not create a NULL XID with: gtrid length " + gtridLength + " and bqual length " + bqualLength);
        }
        return new XID(gtridLength, bqualLength, formatType, globalTransactionId, branchQualifier);
    }

    private static boolean isNullFormatTypeButAdditionalInformationProvided(byte[] globalTransactionId, byte[] branchQualifier, long formatType)
    {
        if(!XIDFormatType.isNullType(formatType))
        {
            return false;
        }
        return !(null == globalTransactionId && null == branchQualifier);
    }

    /**
     * globalTransactionId + branchQualifier
     * @return
     */
    public byte[] getData()
    {
        final byte[] data = new byte[gtridLength + bqualLength];
        ByteBuffer b = ByteBuffer.wrap(data);
        b.put(globalTransactionId);
        b.put(branchQualifier);
        return b.array();
    }

    // it's fine
    @SuppressWarnings("squid:S1067")
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
        XID xid = (XID) o;
        return formatType == xid.formatType &&
               gtridLength == xid.gtridLength &&
               bqualLength == xid.bqualLength &&
               Arrays.equals(globalTransactionId, xid.globalTransactionId) &&
               Arrays.equals(branchQualifier, xid.branchQualifier);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(formatType, gtridLength, bqualLength, globalTransactionId, branchQualifier);
    }

    @Override
    public int getFormatId()
    {
        return (int)formatType;
    }

    @Override
    public byte[] getGlobalTransactionId()
    {
        return (null == globalTransactionId) ? null : Arrays.copyOf(globalTransactionId, globalTransactionId.length);
    }

    @Override
    public byte[] getBranchQualifier()
    {
        return (null == branchQualifier) ? null : Arrays.copyOf(branchQualifier, branchQualifier.length);
    }
}
