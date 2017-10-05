package se.kodarkatten.casual.network.messages.parseinfo;

public enum ConnectReplySizes
{
    EXECUTION(16, 16),
    DOMAIN_ID(16, 16),
    // We restrict this internally to Integer.MAX_VALUE
    DOMAIN_NAME_SIZE(8, 8),
    PROTOCOL_VERSION_SIZE(8,8);

    private final int nativeSize;
    private final int networkSize;
    ConnectReplySizes(int nativeSize, int networkSize)
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
