/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow;

import io.netty.channel.Channel;
import se.laz.casual.jca.inflow.work.CasualServiceCallWork;

import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkListener;

public class ServiceCallWorkListener implements WorkListener
{
    private final Channel channel;

    public ServiceCallWorkListener(Channel channel )
    {
        this.channel = channel;
    }

    public Channel getSocketChannel()
    {
        return this.channel;
    }

    @Override
    public void workAccepted(WorkEvent e)
    {
        //NOP
    }

    @Override
    public void workRejected(WorkEvent e)
    {
        // what do we do if a piece of work was rejected?
        // close the channel?
    }

    @Override
    public void workStarted(WorkEvent e)
    {
        //NOP
    }

    @Override
    public void workCompleted(WorkEvent e)
    {
        CasualServiceCallWork work = (CasualServiceCallWork)e.getWork();
        channel.writeAndFlush(work.getResponse());
    }
}
