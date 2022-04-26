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
import se.laz.casual.api.service.CasualService;
import se.laz.casual.connection.caller.CasualCaller;
import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Optional;

@Stateless
@Remote(TestService.class)
public class TestServiceImpl implements TestService
{
    @Inject
    CasualCaller casualCaller;

    @CasualService(name="javaEcho")
    @Override
    public InboundResponse casualEcho(InboundRequest buffer)
    {
        return InboundResponse.createBuilder()
                              .buffer( buffer.getBuffer() )
                              .build();
    }

    @CasualService(name="javaForward")
    @Override
    public InboundResponse forward(InboundRequest buffer)
    {
        String forwardName = getForwardName();
        CasualBuffer returnBuffer = makeFieldedCall(casualCaller, forwardName, buffer.getBuffer(), Flag.of(AtmiFlags.NOFLAG));
        return InboundResponse.createBuilder().buffer( returnBuffer).build();
    }

    private String getForwardName()
    {
        return Optional.ofNullable(System.getenv("JAVA_FORWARD_SERVICE_NAME")).orElseThrow(() -> new ForwardDefinitionMissingException("Env var JAVA_FORWARD_SERVICE_NAME undefined"));
    }

    private static CasualBuffer makeFieldedCall(final CasualCaller caller, String serviceName, CasualBuffer msg, Flag<AtmiFlags> flags)
    {
        ServiceReturn<CasualBuffer> reply = caller.tpcall(serviceName, msg, flags);
        if(reply.getServiceReturnState() == ServiceReturnState.TPSUCCESS)
        {
            return reply.getReplyBuffer();
        }
        throw new ServiceCallFailedException("tpcall failed: " + reply.getErrorState());
    }

}
