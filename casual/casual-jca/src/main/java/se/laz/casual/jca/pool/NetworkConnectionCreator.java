/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.pool;

import se.laz.casual.jca.Address;
import se.laz.casual.network.outbound.NetworkListener;

@FunctionalInterface
public interface NetworkConnectionCreator
{
    ReferenceCountedNetworkConnection createNetworkConnection(Address address, NetworkListener networkListener, ReferenceCountedNetworkCloseListener referenceCountedNetworkCloseListener, NetworkListener ownListener);
}
