package se.kodarkatten.casual.jca.work;

import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkListener;
import java.util.logging.Logger;

public class NetworkReaderWorkListener implements WorkListener
{
    private static final Logger log = Logger.getLogger(NetworkReaderWorkListener.class.getName());
    @Override
    public void workAccepted(WorkEvent e)
    {
        log.finest("workAccepted: " + e);
    }

    @Override
    public void workRejected(WorkEvent e)
    {
        log.warning("workRejected: " + e);
    }

    @Override
    public void workStarted(WorkEvent e)
    {
        log.finest("workStarted: " + e);
    }

    @Override
    public void workCompleted(WorkEvent e)
    {
        log.info("workCompleted: " + e);
    }
}
