package se.kodarkatten.casual.jca.inflow.work;

import se.kodarkatten.casual.jca.CasualResourceAdapterException;
import se.kodarkatten.casual.network.protocol.io.LockableSocketChannel;

import javax.resource.spi.work.WorkException;
import java.util.function.Consumer;

public final class SocketChannelConsumer implements Consumer<LockableSocketChannel>
{
    private final CasualInboundWork work;

    public SocketChannelConsumer( CasualInboundWork work )
    {
        this.work = work;
    }

    @Override
    public void accept(LockableSocketChannel socketChannel)
    {
        try
        {
            work.getWorkManager().startWork( new CasualSocketWork( socketChannel, work ) );
        } catch (WorkException e)
        {
            throw new CasualResourceAdapterException( "Error starting worker.", e );
        }
    }
}
