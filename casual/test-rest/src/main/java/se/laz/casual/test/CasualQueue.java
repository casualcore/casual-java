/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test;

import org.apache.commons.io.IOUtils;
import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.type.OctetBuffer;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.queue.DequeueReturn;
import se.laz.casual.api.queue.EnqueueReturn;
import se.laz.casual.api.queue.MessageSelector;
import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.queue.QueueMessage;
import se.laz.casual.connection.caller.CasualCaller;
import se.laz.casual.test.service.remote.QueueCallFailedException;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Stateless
@Path("/queue")
public class CasualQueue
{
    @Inject
    CasualCaller casualCaller;

    @Resource
    private EJBContext ctx;

    @POST
    @Consumes("application/casual-x-octet")
    @Path("enqueue/{queueName}")
    public Response enqueue(@PathParam("queueName") String queueName, InputStream inputStream)
    {
        try
        {
            byte[] data = IOUtils.toByteArray(inputStream);
            OctetBuffer buffer = OctetBuffer.of(data);
            UUID uuid = makeEnqueueCall(buffer, queueName).getId().orElse(null);
            String response = null == uuid ? "no message available" : uuid.toString();
            return Response.ok().entity(response).build();
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

    /**
     * In case of no uuid it will pop the oldest message of the queue - if any
     * @param queueName name of the queue.
     * @param uuid optional uuid to pop.
     * @return response containing dequeued message if found.
     */
    @GET
    @Path("dequeue/{queueName}")
    public Response dequeue(@PathParam("queueName") String queueName, @QueryParam("uuid") String uuid)
    {
        try
        {
            UUID messageId = null != uuid && !uuid.isEmpty() ? UUID.fromString(uuid) : null;
            return Response.ok().entity(makeDequeueCall(queueName, messageId)).build();
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

    private EnqueueReturn makeEnqueueCall(CasualBuffer msg, String queueName)
    {
        EnqueueReturn reply = casualCaller.enqueue(QueueInfo.of(queueName), QueueMessage.of(msg));
        if(reply.getErrorState() == ErrorState.OK)
        {
            return reply;
        }
        throw new QueueCallFailedException("enqueue for queue: " + queueName + " failed with: " + reply.getErrorState());
    }

    private String makeDequeueCall(String queueName, UUID messageId)
    {
        MessageSelector messageSelector = null == messageId ? MessageSelector.of() : MessageSelector.of(messageId);
        DequeueReturn reply = casualCaller.dequeue(QueueInfo.of(queueName), messageSelector);
        if(reply.getErrorState() == ErrorState.OK)
        {
            QueueMessage msg = reply.getQueueMessage().orElse(null);
            if(null != msg)
            {
                return new String(msg.getPayload().getBytes().get(0), StandardCharsets.UTF_8) + "\nmsg id: " + msg.getId();
            }
            return "No more messages on queue: " + queueName;
        }
        throw new QueueCallFailedException("dequeue for queue: " + queueName + " failed with: " + reply.getErrorState());
    }

}
