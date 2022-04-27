/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.test;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ServiceReturn;
import se.laz.casual.api.buffer.type.OctetBuffer;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.flags.ServiceReturnState;
import se.laz.casual.api.qualifier.Casual;
import se.laz.casual.connection.caller.CasualCaller;
import se.laz.casual.test.service.remote.ServiceCallFailedException;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
@Stateless
@Path("/echo")
public class EchoService
{
    @Casual
    @Inject
    CasualCaller casualCaller;

    @Resource
    private EJBContext ctx;

    @GET
    @Consumes("application/casual-x-octet")
    @Path("{serviceName}/{data}")
    public Response echoRequest(@PathParam("serviceName") @DefaultValue("casual%2Fexample%2Fecho") String serviceName, @PathParam("data") String data)
    {
        try
        {
            Flag<AtmiFlags> flags = Flag.of(AtmiFlags.NOFLAG);
            OctetBuffer buffer = OctetBuffer.of(data.getBytes(StandardCharsets.UTF_8));
            return Response.ok().entity(new String(makeCasualCall(buffer, serviceName, flags).getBytes().get(0), StandardCharsets.UTF_8)).build();
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
    private CasualBuffer makeCasualCall(CasualBuffer msg, String serviceName, Flag<AtmiFlags> flags)
    {
        ServiceReturn<CasualBuffer> reply = casualCaller.tpcall(serviceName, msg, flags);
        if(reply.getServiceReturnState() == ServiceReturnState.TPSUCCESS)
        {
            return reply.getReplyBuffer();
        }
        throw new ServiceCallFailedException("tpcall failed: " + reply.getErrorState());
    }


}
