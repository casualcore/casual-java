/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.test;

import org.apache.commons.io.IOUtils;
import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ServiceReturn;
import se.laz.casual.api.buffer.type.OctetBuffer;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.flags.ServiceReturnState;
import se.laz.casual.api.queue.DequeueReturn;
import se.laz.casual.api.queue.EnqueueReturn;
import se.laz.casual.api.queue.MessageSelector;
import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.queue.QueueMessage;
import se.laz.casual.connection.caller.CasualCaller;
import se.laz.casual.test.service.remote.QueueCallFailedException;
import se.laz.casual.test.service.remote.ServiceCallFailedException;

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
@Stateless
@Path("/casual")
public class CasualService
{
    @Inject
    CasualCaller casualCaller;

    @Resource
    private EJBContext ctx;

    @POST
    @Consumes("application/casual-x-octet")
    @Path("{serviceName}")
    public Response serviceRequest(@PathParam("serviceName") String serviceName, InputStream inputStream)
    {
        try
        {
            byte[] data = IOUtils.toByteArray(inputStream);
            Flag<AtmiFlags> flags = Flag.of(AtmiFlags.NOFLAG);
            OctetBuffer buffer = OctetBuffer.of(data);
            return Response.ok().entity(makeServiceCall(buffer, serviceName, flags).getBytes().get(0)).build();
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

    @POST
    @Consumes("application/casual-x-octet")
    @Path("enqueue/{queueName}")
    public Response enqueue(@PathParam("queueName") String queueName, InputStream inputStream)
    {
        try
        {
            byte[] data = IOUtils.toByteArray(inputStream);
            OctetBuffer buffer = OctetBuffer.of(data);
            return Response.ok().entity(makeEnqueueCall(buffer, queueName)).build();
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

    @POST
    @Consumes("application/casual-x-octet")
    @Path("dequeue/{queueName}")
    public Response dequeue(@PathParam("queueName") String queueName)
    {
        try
        {
            return Response.ok().entity(makeDequeueCall(queueName)).build();
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

    private CasualBuffer makeServiceCall(CasualBuffer msg, String serviceName, Flag<AtmiFlags> flags)
    {
        ServiceReturn<CasualBuffer> reply = casualCaller.tpcall(serviceName, msg, flags);
        if(reply.getServiceReturnState() == ServiceReturnState.TPSUCCESS)
        {
            return reply.getReplyBuffer();
        }
        throw new ServiceCallFailedException("tpcall failed: " + reply.getErrorState());
    }

    private EnqueueReturn makeEnqueueCall(CasualBuffer msg, String queueName)
    {
        EnqueueReturn reply = casualCaller.enqueue(QueueInfo.of(queueName), QueueMessage.of(msg));
        if(reply.getErrorState() == ErrorState.OK)
        {
            return reply;
        }
        throw new QueueCallFailedException("enqueue for queue: " + queueName + " failed with: " + reply.getErrorState());
    }

    private QueueMessage makeDequeueCall(String queueName)
    {
        DequeueReturn reply = casualCaller.dequeue(QueueInfo.of(queueName), MessageSelector.of());
        if(reply.getErrorState() == ErrorState.OK)
        {
            return reply.getQueueMessage().orElseThrow(() -> new QueueCallFailedException("dequeue: " + queueName + " errorstate ok but missing message!"));
        }
        throw new QueueCallFailedException("dequeue for queue: " + queueName + " failed with: " + reply.getErrorState());
    }

}
