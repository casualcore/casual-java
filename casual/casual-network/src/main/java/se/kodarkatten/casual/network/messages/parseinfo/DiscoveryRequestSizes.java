package se.kodarkatten.casual.network.messages.parseinfo;

/**
 * Created by aleph on 2017-02-23.
 */
public enum DiscoveryRequestSizes
{
    EXECUTION(16, 16),
    DOMAIN_ID(16, 16),
    DOMAIN_NAME_SIZE(8, 8),
    SERVICES_SIZE(8, 8),
    SERVICES_ELEMENT_SIZE(8,8),
    SERVICES_ELEMENT_DATA(128, 128);

    private final int nativeSize;
    private final int networkSize;
    private DiscoveryRequestSizes(int nativeSize, int networkSize)
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
