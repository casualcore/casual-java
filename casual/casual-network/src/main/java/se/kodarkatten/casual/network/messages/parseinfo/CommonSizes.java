package se.kodarkatten.casual.network.messages.parseinfo;

/**
 * Created by aleph on 2017-04-03.
 */
public enum CommonSizes
{
    EXECUTION(16, 16),
    XID_FORMAT(8, 8),
    XID_GTRID_LENGTH(8, 8),
    XID_BQUAL_LENGTH(8, 8),
    // byte array with the size of gtrid_length + bqual_length (max 128)
    XID_PAYLOAD(32, 32),
    TRANSACTION_RESOURCE_ID(4, 8),
    TRANSACTION_RESOURCE_FLAGS(4, 8),
    TRANSACTION_RESOURCE_STATE(4, 8);

    private final int nativeSize;
    private final int networkSize;
    CommonSizes(int nativeSize, int networkSize)
    {
        this.nativeSize = nativeSize;
        this.networkSize = networkSize;
    }
    public int getNativeSize()
    {
        return nativeSize;
    }
    public int getNetworkSize()
    {
        return networkSize;
    }
}
