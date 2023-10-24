/*
 * Copyright (c) 2017 - 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow;

import io.netty.channel.Channel;
import se.laz.casual.jca.inflow.work.CasualServiceCallWork;

import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkListener;
import java.util.logging.Logger;

/**
 * Work Listener to handle completion of {@link javax.resource.spi.work.Work} item by
 * {@link javax.resource.spi.work.WorkManager} to flush the response to the netty {@link Channel}.
 */
public class ServiceCallWorkListener implements WorkListener
{
    private static final Logger LOG = Logger.getLogger(ServiceCallWorkListener.class.getName());
    private final Channel channel;
    private final boolean isTpNoReply;

    public ServiceCallWorkListener(Channel channel )
    {
        this(channel, false);
    }

    public ServiceCallWorkListener(Channel channel, boolean isTpNoReply)
    {
        this.channel = channel;
        this.isTpNoReply = isTpNoReply;
    }

    public Channel getSocketChannel()
    {
        return this.channel;
    }

    @Override
    public void workAccepted(WorkEvent e)
    {
        //No Op
    }

    @Override
    public void workRejected(WorkEvent e)
    {
        // No Op
    }

    @Override
    public void workStarted(WorkEvent e)
    {
        //No Op
    }

    @Override
    public void workCompleted(WorkEvent e)
    {
        if(!isTpNoReply)
        {
            CasualServiceCallWork work = (CasualServiceCallWork) e.getWork();
            channel.writeAndFlush(work.getResponse());
        }
        else
        {
            LOG.finest(() -> "service call, TPNOREPLY, finished: " + e);
        }
    }
}
