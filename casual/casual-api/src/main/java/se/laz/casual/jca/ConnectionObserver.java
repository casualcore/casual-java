/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

public interface ConnectionObserver
{
    /**
     * Notifies when a topology has changed
     * Note:
     * You may NOT issue any outbound calls in this callback!
     * @param domainId
     */
    void topologyChanged(DomainId domainId);
}
