package se.kodarkatten.casual.network.messages.parseinfo;

/**
 * Created by aleph on 2017-02-23.
 */
public enum MessageSizes
{
    TYPE(8, 8),
    HEADER_CORRELATION(16, 16),
    HEADER_OFFSET(8, 8),
    HEADER_COUNT(8,8),
    COMPLETE_SIZE(8, 8),
    PAYLOAD(16344, 16344);

    private final int nativeSize;
    private final int networkSize;
    private MessageSizes(int nativeSize, int networkSize)
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
