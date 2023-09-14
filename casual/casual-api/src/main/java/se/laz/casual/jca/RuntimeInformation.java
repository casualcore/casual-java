/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RuntimeInformation
{
   private static final String INBOUND_SERVER_STARTED = "INBOUND_SERVER_STARTED";
   private static final Map<String, Boolean> CACHE = new ConcurrentHashMap<>();
   private static final Set<GlobalTransactionId> PREPARED_GTRIDS = ConcurrentHashMap.newKeySet();
   private static final Map<DomainId, Set<GlobalTransactionId>> DOMAIN_ID_TO_PREPARED_GTRID = new ConcurrentHashMap<>();

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
      Objects.requireNonNull(globalTransactionId);
      PREPARED_GTRIDS.add(globalTransactionId);
      DOMAIN_ID_TO_PREPARED_GTRID.computeIfAbsent(belongsTo, domainId -> ConcurrentHashMap.newKeySet()).add(globalTransactionId);
   }

   public static boolean exists(GlobalTransactionId globalTransactionId)
   {
      Objects.requireNonNull(globalTransactionId);
      return PREPARED_GTRIDS.contains(globalTransactionId);
   }

   public static void removeGtrid(GlobalTransactionId globalTransactionId)
   {
      Objects.requireNonNull(globalTransactionId);
      PREPARED_GTRIDS.remove(globalTransactionId);
   }

   public static void removeAllGtridsFor(DomainId domainId)
   {
       Set<GlobalTransactionId> domainIds = DOMAIN_ID_TO_PREPARED_GTRID.remove(domainId);
       if(null != domainIds)
       {
           PREPARED_GTRIDS.removeAll(domainIds);
       }
   }

}
