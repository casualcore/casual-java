package se.kodarkatten.casual.api.xa;

import se.kodarkatten.casual.api.xa.exceptions.XIDException;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by aleph on 2017-03-14.
 */
public final class XID
{
    private static final int XID_DATA_SIZE = 128;
    private long formatId;
    // length of the transaction gtrid part
    private final long gtridLength;
    // length of the transaction branch part
    private final long bqualLength;
    // (size = gtridLength + bqualLength) <= XID_DATA_SIZE
    private final byte[] data;
    private XID(long gtridLength, long bqualLength, final byte[] data)
    {
        this.gtridLength = gtridLength;
        this.bqualLength = bqualLength;
        this.data = data;
    }

    public static XID of(long gtridLength, long bqualLength, final byte[] data)
    {
        if((gtridLength + bqualLength) > data.length)
        {
            throw new XIDException("(gtridLength + bqualLength) > data.length " + "(" + gtridLength + " + " + bqualLength + ") > " + data.length);
        }
        else if(data.length > XID_DATA_SIZE)
        {
            throw new XIDException("data.length > XID_DATA_SIZE " + data.length + " > " + XID_DATA_SIZE);
        }
        return new XID(gtridLength, bqualLength, data);
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
        XID xid = (XID) o;
        return formatId == xid.formatId &&
            gtridLength == xid.gtridLength &&
            bqualLength == xid.bqualLength &&
            Arrays.equals(data, xid.data);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(formatId, gtridLength, bqualLength, data);
    }
}
