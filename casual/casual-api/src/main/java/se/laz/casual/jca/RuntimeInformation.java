/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class RuntimeInformation
{
    private static final String INBOUND_SERVER_STARTED = "INBOUND_SERVER_STARTED";
    private static final Map<String, Boolean> CACHE = new ConcurrentHashMap<>();
    private static final GlobalTransactionIdHandler GLOBAL_TRANSACTION_ID_HANDLER = GlobalTransactionIdHandler.of();

    private RuntimeInformation()
    {}

    public static boolean isInboundStarted()
    {
        return Optional.ofNullable(CACHE.get(INBOUND_SERVER_STARTED)).orElse(false);
    }

    public static void setInboundStarted(boolean started)
    {
        CACHE.put(INBOUND_SERVER_STARTED, started);
    }

    public static void addGtrid(GlobalTransactionId globalTransactionId, DomainId belongsTo)
    {
        GLOBAL_TRANSACTION_ID_HANDLER.addGtrid(globalTransactionId, belongsTo);
    }

    public static boolean exists(GlobalTransactionId globalTransactionId)
    {
        return GLOBAL_TRANSACTION_ID_HANDLER.exists(globalTransactionId);
    }

    public static void removeGtrid(GlobalTransactionId globalTransactionId, DomainId domainId)
    {
        GLOBAL_TRANSACTION_ID_HANDLER.removeGtrid(globalTransactionId, domainId);
    }

    public static void removeAllGtridsFor(DomainId domainId)
    {
        GLOBAL_TRANSACTION_ID_HANDLER.removeAllGtridsFor(domainId);
    }

}
