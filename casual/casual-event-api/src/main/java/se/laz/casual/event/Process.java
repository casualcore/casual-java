/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event;

import static java.lang.System.Logger.Level.*;

public final class Process
{
    private static final System.Logger LOG = System.getLogger(Process.class.getName());
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
            LOG.log(WARNING, () -> "could not get process id",e);
        }
        return currentPID;
    }
}
