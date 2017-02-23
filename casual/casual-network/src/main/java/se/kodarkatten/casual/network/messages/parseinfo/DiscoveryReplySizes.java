package se.kodarkatten.casual.network.messages.parseinfo;

/**
 * Created by aleph on 2017-02-23.
 */
public enum DiscoveryReplySizes
{
    EXECUTION(16, 16),
    DOMAIN_ID(16, 16),
    DOMAIN_NAME_SIZE(8, 8),
    SERVICES_SIZE(8, 8),
    SERVICES_ELEMENT_NAME_SIZE(8,8),
    SERVICES_ELEMENT_NAME_DATA(128, 128),
    SERVICES_ELEMENT_TYPE(8, 8),
    SERVICES_ELEMENT_TIMEOUT(8, 8),
    SERVICES_ELEMENT_TRANSACTION(2, 2),
    SERVICES_ELEMENT_HOPS(2,2);

    private final int nativeSize;
    private final int networkSize;
    private DiscoveryReplySizes(int nativeSize, int networkSize)
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
