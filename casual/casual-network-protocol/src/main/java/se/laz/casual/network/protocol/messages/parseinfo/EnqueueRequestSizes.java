/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.parseinfo;

public enum EnqueueRequestSizes
{
    EXECUTION(16, 16),
    NAME_SIZE(8, 8),
    MESSAGE_ID(16, 16),
    MESSAGE_PROPERTIES_SIZE(8, 8),
    MESSAGE_REPLY_SIZE(8, 8),
    MESSAGE_AVAILABLE(8, 8),
    MESSAGE_TYPE_SIZE(8, 8),
    MESSAGE_PAYLOAD_SIZE(8, 8);
    private final int nativeSize;
    private final int networkSize;
    EnqueueRequestSizes(int nativeSize, int networkSize)
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
