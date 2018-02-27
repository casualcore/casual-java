/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package se.kodarkatten.casual.jca.inflow;


import io.netty.channel.Channel;
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage;
import se.kodarkatten.casual.network.protocol.io.LockableSocketChannel;

import se.kodarkatten.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage;
import se.kodarkatten.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.kodarkatten.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitRequestMessage;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareRequestMessage;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackRequestMessage;

import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkManager;

/**
 * CasualMessageListener
 *
 * @version $Revision: $
 */
public interface CasualMessageListener
{
   void domainConnectRequest(CasualNWMessage<CasualDomainConnectRequestMessage> message, Channel channel );

   void domainDiscoveryRequest(CasualNWMessage<CasualDomainDiscoveryRequestMessage> message, Channel channel );

   void serviceCallRequest(CasualNWMessage<CasualServiceCallRequestMessage> message, Channel channel, WorkManager workManager );

   void prepareRequest(CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> message, Channel channel, XATerminator xaTerminator);

   void commitRequest(CasualNWMessage<CasualTransactionResourceCommitRequestMessage> message, Channel channel, XATerminator xaTerminator);

   void requestRollback(CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> message, Channel channel, XATerminator xaTerminator);
}
