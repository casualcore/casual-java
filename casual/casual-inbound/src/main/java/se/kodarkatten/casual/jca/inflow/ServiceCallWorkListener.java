package se.kodarkatten.casual.jca.inflow;

import se.kodarkatten.casual.jca.inflow.work.CasualServiceCallWork;
import se.kodarkatten.casual.network.protocol.io.CasualNetworkWriter;
import se.kodarkatten.casual.network.protocol.io.LockableSocketChannel;

import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkListener;

public class ServiceCallWorkListener implements WorkListener
{
    private final LockableSocketChannel channel;

    public ServiceCallWorkListener(LockableSocketChannel channel )
    {
        this.channel = channel;
    }

    public LockableSocketChannel getSocketChannel()
    {
        return this.channel;
    }

    @Override
    public void workAccepted(WorkEvent e)
    {

    }

    @Override
    public void workRejected(WorkEvent e)
    {

    }

    @Override
    public void workStarted(WorkEvent e)
    {

    }

    @Override
    public void workCompleted(WorkEvent e)
    {
        CasualServiceCallWork work = (CasualServiceCallWork)e.getWork();

        CasualNetworkWriter.write( channel, work.getResponse() );
    }
}
