/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.network.protocol.messages;

import java.util.List;
import java.util.UUID;

public interface Routable
{
    List<UUID> getRoutes();
    void setRoutes(List<UUID> routes);
}
