/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.test;

import org.apache.commons.io.IOUtils;
import se.laz.casual.api.Conversation;
import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ConversationReturn;
import se.laz.casual.api.buffer.type.OctetBuffer;
import se.laz.casual.api.conversation.TpConnectReturn;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.connection.caller.CasualCaller;
import se.laz.casual.test.service.remote.TPConnectFailedException;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Stateless
@Path("/conversation")
public class CasualConversation
{
    @Inject
    CasualCaller casualCaller;

    @Resource
    private EJBContext ctx;

    @POST
    @Consumes("application/casual-x-octet")
    @Path("{serviceName}")
    public Response conversationRequest(@PathParam("serviceName") String serviceName, InputStream inputStream)
    {
        try
        {
            byte[] data = IOUtils.toByteArray(inputStream);
            Flag<AtmiFlags> flags = Flag.of(AtmiFlags.TPSENDONLY);
            OctetBuffer buffer = OctetBuffer.of(data);
            return Response.ok().entity(makeConversation(buffer, serviceName, flags)).build();
        }
        catch (Exception e)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            if (!ctx.getRollbackOnly())
            {
                ctx.setRollbackOnly();
            }
            return Response.serverError().entity(sw.toString()).build();
        }
    }

    private String makeConversation(CasualBuffer msg, String serviceName, Flag<AtmiFlags> flags)
    {
        try(TpConnectReturn tpConnectReturn = casualCaller.tpconnect(serviceName, msg, flags))
        {
            if(tpConnectReturn.getErrorState() != ErrorState.OK)
            {
                throw new TPConnectFailedException(tpConnectReturn.getErrorState().name());
            }
            Conversation conversation = tpConnectReturn.getConversation().orElseThrow(() -> new TPConnectFailedException("ErrorState.OK but no conversation!"));
            StringBuilder b = new StringBuilder("Payload:\n");
            msg = OctetBuffer.of("Extra, extra, read all about it!\n".getBytes(StandardCharsets.UTF_8));
            // send buffer and hand over control
            conversation.tpsend(msg,true);
            ErrorState errorState = ErrorState.OK;
            while(conversation.isReceiving() && errorState == ErrorState.OK)
            {
                ConversationReturn<CasualBuffer> conversationReturn = conversation.tprecv();
                Optional<ErrorState> maybeError = conversationReturn.getErrorState();
                errorState = maybeError.orElse(ErrorState.OK);
                if(errorState == ErrorState.OK)
                {
                    msg = OctetBuffer.of(conversationReturn.getReplyBuffer().getBytes());
                    Optional<byte[]> payload = msg.getBytes().isEmpty() ? Optional.empty() : Optional.ofNullable(msg.getBytes().get(0));
                    payload.ifPresent(d -> b.append(new String(d)));
                }
            }
            return b.toString() + "\n Error: " + errorState.name();
        }
    }

}
