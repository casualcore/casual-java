/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.parseinfo;

/**
 * Created by aleph on 2017-02-23.
 */
public enum ConversationDisconnectSizes
{
    EXECUTION(16, 16),
    ROUTES_SIZE(8,8),
    ROUTE_ELEMENT_SIZE(16,16),
    EVENTS(8, 8),
    RESULT_CODE(4, 4),
    BUFFER_TYPE_NAME_SIZE(8, 8),
    BUFFER_PAYLOAD_SIZE(8, 8);

    private final int nativeSize;
    private final int networkSize;
    ConversationDisconnectSizes(int nativeSize, int networkSize)
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
