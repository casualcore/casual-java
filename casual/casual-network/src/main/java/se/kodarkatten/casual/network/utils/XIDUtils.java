package se.kodarkatten.casual.network.utils;

import se.kodarkatten.casual.api.xa.XIDFormatType;
import javax.transaction.xa.Xid;

/**
 * Created by aleph on 2017-03-21.
 */
public final class XIDUtils
{
    public static final int XID_FORMAT_NETWORK_SIZE = 8;
    public static final int XID_GTRID_NETWORK_SIZE = 8;
    public static final int XID_BQUAL_NETWORK_SIZE = 8;
    private XIDUtils()
    {}

    public static int getXIDNetworkSize(final Xid xid)
    {
        if(XIDFormatType.isNullType(xid.getFormatId()))
        {
            return XID_FORMAT_NETWORK_SIZE;
        }
        return XID_FORMAT_NETWORK_SIZE +
            XID_GTRID_NETWORK_SIZE +
            XID_BQUAL_NETWORK_SIZE +
            xid.getGlobalTransactionId().length + xid.getBranchQualifier().length;
    }
}
