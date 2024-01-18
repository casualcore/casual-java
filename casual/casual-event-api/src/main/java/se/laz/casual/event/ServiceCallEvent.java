/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event;

import javax.transaction.xa.Xid;
import java.util.Optional;
import java.util.UUID;

public interface ServiceCallEvent
{
    /**
     * @return The name of the invoked service
     */
    String getService();
    /**
     * @return The name of the parent service, if any
     */
    Optional<String> getParent();
    /**
     * @return The process id of the invoked instance
     */
    int getPID();
    /**
     * @return The unique execution id, like breadcrumbs
     */
    UUID getExecution();
    /*
     * The transaction id
     */
    Xid getTrid();
    /*
     * When service was invoked,
     * milliseconds since epoch
     */
    long getStart();
    /*
     * When service was done,
     * milliseconds since epoch
     */
    long getEnd();
    /*
     * How long caller had to wait - in milliseconds
     */
    long getPending();
    /*
     * Outcome of the service call if ok,
     * if not - the error reported from the service
     */
    int getCode();
    /*
     * Order of the service - sequential or concurrent denoted by s or c.
     * s reserves a process, c does not.
     */
    Order getOrder();
}
