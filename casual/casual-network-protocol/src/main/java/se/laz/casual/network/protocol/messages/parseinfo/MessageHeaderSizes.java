/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.parseinfo;

/**
 * Created by aleph on 2017-02-23.
 */
public enum MessageHeaderSizes
{
    HEADER_TYPE(8, 8),
    // UUID type 4
    HEADER_CORRELATION(16, 16),
    HEADER_PAYLOAD_SIZE(8, 8);

    private final int nativeSize;
    private final int networkSize;

    private MessageHeaderSizes(int nativeSize, int networkSize)
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

    /**
     * Complete header byte size
     * @return
     */
    public static int getHeaderNetworkSize()
    {
        return HEADER_TYPE.getNetworkSize() +
               HEADER_CORRELATION.getNetworkSize() +
               HEADER_PAYLOAD_SIZE.getNetworkSize();
    }
}
