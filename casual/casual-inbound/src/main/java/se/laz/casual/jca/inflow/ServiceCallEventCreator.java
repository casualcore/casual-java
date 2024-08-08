/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow;

import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.event.Order;
import se.laz.casual.event.ServiceCallEvent;

public final class ServiceCallEventCreator
{
    private ServiceCallEventCreator()
    {}
    public static ServiceCallEvent createEvent(ServiceCallEvent.Builder eventBuilder, WorkResponseContext responseContext, ErrorState errorState)
    {
        return eventBuilder.withTransactionId(responseContext.xid())
                           .withExecution(responseContext.execution())
                           .withParent(responseContext.parentName())
                           .withService(responseContext.serviceName())
                           .withOrder(Order.SEQUENTIAL)
                           .withCode(errorState)
                           .build();
    }
}
