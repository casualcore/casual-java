/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import se.laz.casual.network.connection.CasualConnectionException;
import se.laz.casual.network.inbound.ProtocolVersionValueHolder;

import java.util.Objects;

/**
 * Groups the resources used while constructing one outbound connection.
 *
 * @param connectionInformation the outbound connection configuration
 * @param conversationMessageStorage the conversation message storage
 * @param conversationMessageHandler the conversation message handler
 * @param messageHandler the general outbound message handler
 * @param exceptionHandler the network exception handler
 * @param protocolVersionValueHolder the negotiated protocol version holder
 * @param domainIdValueHolder the connected peer domain ID holder
 * @param errorInformer the network error listener registry
 */
record ConnectionResources(NettyConnectionInformation connectionInformation,
                           ConversationMessageStorage conversationMessageStorage,
                           ConversationMessageHandler conversationMessageHandler,
                           CasualMessageHandler messageHandler,
                           ExceptionHandler exceptionHandler,
                           ProtocolVersionValueHolder protocolVersionValueHolder,
                           DomainIdValueHolder domainIdValueHolder,
                           ErrorInformer errorInformer)
{
    ConnectionResources
    {
        Objects.requireNonNull(connectionInformation, "connectionInformation can not be null");
        Objects.requireNonNull(conversationMessageStorage, "conversationMessageStorage can not be null");
        Objects.requireNonNull(conversationMessageHandler, "conversationMessageHandler can not be null");
        Objects.requireNonNull(messageHandler, "messageHandler can not be null");
        Objects.requireNonNull(exceptionHandler, "exceptionHandler can not be null");
        Objects.requireNonNull(protocolVersionValueHolder, "protocolVersionValueHolder can not be null");
        Objects.requireNonNull(domainIdValueHolder, "domainIdValueHolder can not be null");
        Objects.requireNonNull(errorInformer, "errorInformer can not be null");
    }

    static ConnectionResources create(NettyConnectionInformation connectionInformation, NetworkListener networkListener)
    {
        ErrorInformer errorInformer =
                ErrorInformer.of(new CasualConnectionException("network connection is gone"));
        errorInformer.addListener(networkListener);

        Correlator correlator = connectionInformation.getCorrelator();
        ConversationMessageStorage conversationMessageStorage =
                ConversationMessageStorageImpl.of();

        OnNetworkError onNetworkError =
                channel -> NetworkErrorHandler.notifyListenersIfNotConnected(
                        channel,
                        errorInformer);

        return new ConnectionResources(
                connectionInformation,
                conversationMessageStorage,
                ConversationMessageHandler.of(conversationMessageStorage),
                CasualMessageHandler.of(correlator),
                ExceptionHandler.of(correlator, onNetworkError),
                ProtocolVersionValueHolder.of(),
                DomainIdValueHolder.of(),
                errorInformer);
    }
}
