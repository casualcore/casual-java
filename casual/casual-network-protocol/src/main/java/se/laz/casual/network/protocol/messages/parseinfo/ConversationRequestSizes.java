/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.parseinfo;

/**
 * Created by aleph on 2017-02-23.
 */
public enum ConversationRequestSizes
{
    EXECUTION(16, 16),
    DUPLEX(2, 2),
    RESULT_CODE(4, 4),
    USER_CODE(8,8),
    BUFFER_TYPE_NAME_SIZE(8, 8),
    BUFFER_PAYLOAD_SIZE(8, 8);

    private final int nativeSize;
    private final int networkSize;
    ConversationRequestSizes(int nativeSize, int networkSize)
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
