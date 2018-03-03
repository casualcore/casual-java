/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.parseinfo;

/**
 * Created by aleph on 2017-02-23.
 */
public enum DiscoveryReplySizes
{
    EXECUTION(16, 16),
    DOMAIN_ID(16, 16),
    DOMAIN_NAME_SIZE(8, 8),
    SERVICES_SIZE(8, 8),
    SERVICES_ELEMENT_NAME_SIZE(8, 8),
    SERVICES_ELEMENT_NAME_DATA(128, 128),
    SERVICES_ELEMENT_CATEGORY_SIZE(8, 8),
    SERVICES_ELEMENT_TRANSACTION(2, 2),
    SERVICES_ELEMENT_TIMEOUT(8, 8),
    SERVICES_ELEMENT_HOPS(8, 8),
    QUEUES_SIZE(8, 8),
    QUEUES_ELEMENT_SIZE(8, 8),
    QUEUES_ELEMENT_DATA(128, 128),
    QUEUES_ELEMENT_RETRIES(8, 8);

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
