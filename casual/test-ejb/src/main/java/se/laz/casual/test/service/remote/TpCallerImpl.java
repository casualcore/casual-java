/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.test.service.remote;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ServiceReturn;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.flags.ServiceReturnState;
import se.laz.casual.connection.caller.CasualCaller;

import javax.inject.Inject;

public class TpCallerImpl implements TpCaller
{
    private CasualCaller casualCaller;

    // wls
    public TpCallerImpl()
    {}

    @Inject
    public TpCallerImpl(CasualCaller casualCaller)
    {
        this.casualCaller = casualCaller;
    }
    @Override
    public CasualBuffer makeTpCall(String serviceName, CasualBuffer msg, Flag<AtmiFlags> flags)
    {
        ServiceReturn<CasualBuffer> reply = casualCaller.tpcall(serviceName, msg, flags);
        if(reply.getServiceReturnState() == ServiceReturnState.TPSUCCESS)
        {
            return reply.getReplyBuffer();
        }
        throw new ServiceCallFailedException("tpcall failed: " + reply.getErrorState());
    }
}
