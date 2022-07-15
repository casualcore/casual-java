/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

public interface ConnectionObserver
{
    /**
     * The connection is gone and can not be used after this
     * @param domainId
     */
    void connectionGone(DomainId domainId);
}
