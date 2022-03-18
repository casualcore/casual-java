/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ConversationReturn;

import java.util.Optional;

/**
 *  API to hold a conversation
 *
 *  <pre>
 *      Example usage:
 *      {@code
 *
 *      try(CasualConnection connection = connectionFactory.getConnection())
 *      {
 *          OctetBuffer buffer = OctetBuffer.of("Initial payload\n".getBytes(StandardCharsets.UTF_8));
 *          try(Conversation conversation = connection.tpconnect("some_service", Optional.of(buffer), Flag.of(AtmiFlags.TPSENDONLY)))
 *          {
 *              StringBuilder b = new StringBuilder("Payload:\n");
 *              buffer = OctetBuffer.of("Extra, extra, read all about it!\n".getBytes(StandardCharsets.UTF_8));
 *              // send buffer and hand over control
 *              conversation.tpsend(buffer,true, Optional.empty());
 *              ErrorState errorState = ErrorState.OK;
 *              while(conversation.isReceiving() && errorState == ErrorState.OK)
 *              {
 *                  ConversationReturn<CasualBuffer> conversationReturn = conversation.tprecv();
 *                  Optional<ErrorState> maybeError = conversationReturn.getErrorState();
 *                  errorState = maybeError.orElse(ErrorState.OK);
 *                  if(errorState == ErrorState.OK)
 *                  {
 *                      buffer = OctetBuffer.of(conversationReturn.getReplyBuffer().getBytes());
 *                      Optional<byte[]> payload = buffer.getBytes().isEmpty() ? Optional.empty() : Optional.ofNullable(buffer.getBytes().get(0));
 *                      payload.ifPresent(d -> b.append(new String(d)));
 *                  }
 *               }
 *               return b.toString() + "\n Error: " + errorState.name();
 *            }
 *        }
 *        }
 *  </pre>
 *
 */
public interface Conversation extends AutoCloseable
{
    /**
     * Abruptly disconnect conversation
     */
    void tpdiscon();

    /**
     * Note, blocks until a message is available
     * Throws if called when tpsend is supposed to be called
     * @return The result - {@link ConversationReturn<CasualBuffer>}
     */
    ConversationReturn<CasualBuffer> tprecv();

    /**
     * Are we supposed to be sending?
     * @return true if we are supposed to be sending
     */
    boolean isSending();

    /**
     * Are we supposed to be receiving?
     * @return true if we are supposed to be receiving
     */
    boolean isReceiving();

    /**
     * Note: blocks until message has successfully been sent
     * Throws if called when tprecv is supposed to be called
     * @param data
     * @param handOverControl - true if you are in control but want to hand over control and start receiving
     */
    void tpsend(CasualBuffer data, boolean handOverControl, Optional<Long> userCode);

    /**
     * When receiving the direction can be switched by the party in control, if so - the direction is switched
     * @return true if the connection has been switched, false if not
     */
    boolean isDirectionSwitched();

    /**
     * Closes the conversation and removes any messages that have not yet been consumed
     */
    @Override
    void close();
}
