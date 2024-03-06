package se.laz.casual.event;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class Process
{
    private static final Logger LOG = Logger.getLogger(Process.class.getName());
    private static final long CAN_NOT_GET_PID = -1L;
    private static long currentPID = CAN_NOT_GET_PID;
    private Process()
    {}
    public static long pid()
    {
        try
        {
            currentPID = currentPID == CAN_NOT_GET_PID ? ProcessHandle.current().pid() : currentPID;
        }
        catch(Exception e)
        {
            LOG.log(Level.WARNING, e, () -> "could not get process id");
        }
        return currentPID;
    }
}
