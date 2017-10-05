package se.kodarkatten.casual.network.messages.parseinfo;

public enum DequeueReplySizes
{
    EXECUTION(16, 16),
    NUMBER_OF_MESSAGES(8, 8),
    MESSAGE_ID(16, 16),
    MESSAGE_PROPERTIES_SIZE(8,8),
    MESSAGE_REPLY_SIZE(8,8),
    MESSAGE_AVAILABLE_SINCE_EPOC(8,8),
    MESSAGE_TYPE_SIZE(8,8),
    MESSAGE_PAYLOAD_SIZE(8,8),
    MESSAGE_REDELIVERED_COUNT(8,8),
    MESSAGE_TIMESTAMP_SINCE_EPOC(8,8);

    private final int nativeSize;
    private final int networkSize;
    DequeueReplySizes(int nativeSize, int networkSize)
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
