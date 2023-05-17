/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound;

import java.util.UUID;

public interface DomainDisconnectListener
{
    void domainDisconnecting(UUID execution);
}
