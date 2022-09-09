/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

public interface CasualConnectionListener
{
    void newConnection(DomainId domainId);
    void connectionGone(DomainId domainId);

    boolean equals(Object o);
    int hashCode();
}
