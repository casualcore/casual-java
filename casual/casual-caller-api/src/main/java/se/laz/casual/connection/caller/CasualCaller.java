/*
 * Copyright (c) 2021. The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller;

import se.laz.casual.api.CasualQueueApi;
import se.laz.casual.api.CasualServiceApi;

/**
 * Convenience layer
 *
 * Removes the need for applications to have any knowledge of underlying connection factories
 *
 * The prerequisite is that outbound pools in the application server are configured correctly
 * and that casual lookup is configured to make use of those JNDI-names
 *
 * Does an exhaustive initial search that is then cached
 * Thus if for instance a service is available via several connection factories - the call can be dispatched to any of them
 *
 * We choose randomly between matching entries when selecting which one to issue the call to - if more than one is found
 *
 */
public interface CasualCaller extends CasualServiceApi, CasualQueueApi
{}
