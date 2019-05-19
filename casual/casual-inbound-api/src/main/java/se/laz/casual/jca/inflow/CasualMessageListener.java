/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inflow;


import io.netty.channel.Channel;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackRequestMessage;

import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkManager;

/**
 * CasualMessageListener Inbound Message Listener.
 *
 * Performs dispatch of the various network messages received.
 *
 */
public interface CasualMessageListener
{
   /**
    * Process the Domain Connect request and write the resulting response to the {@link Channel}.
    *
    * @param message received.
    * @param channel for response.
    */
   void domainConnectRequest(CasualNWMessage<CasualDomainConnectRequestMessage> message, Channel channel );

   /**
    * Process the Domain Discovery request and write the resulting response to the {@link Channel}.
    * @param message received.
    * @param channel for the response.
    */
   void domainDiscoveryRequest(CasualNWMessage<CasualDomainDiscoveryRequestMessage> message, Channel channel );

   /**
    * Process the Service Call request making use of the provided {@link WorkManager} to handle long running executions.
    * Write the resulting response to the provided {@link Channel}.
    *
    * @param message received.
    * @param channel for the response.
    * @param workManager for managing long running execution.
    */
   void serviceCallRequest(CasualNWMessage<CasualServiceCallRequestMessage> message, Channel channel, WorkManager workManager );

   /**
    * Process the transaction Prepare request utilising the provided {@link XATerminator}.
    *
    * @param message received.
    * @param channel for the response.
    * @param xaTerminator for controlling transaction.
    */
   void prepareRequest(CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> message, Channel channel, XATerminator xaTerminator);

   /**
    * Process the transaction Commit request utilising the provided {@link XATerminator}.
    *
    * @param message received.
    * @param channel for the response.
    * @param xaTerminator for controlling transaction.
    */
   void commitRequest(CasualNWMessage<CasualTransactionResourceCommitRequestMessage> message, Channel channel, XATerminator xaTerminator);

   /**
    * Process the transaction Rollbacak request utilising the providded {@link XATerminator}.
    *
    * @param message
    * @param channel
    * @param xaTerminator
    */
   void requestRollback(CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> message, Channel channel, XATerminator xaTerminator);
}
