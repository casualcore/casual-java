/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.test.service.remote;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.service.CasualService;
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
    public static final String JAVA_FORWARD_ENV_NAME = "JAVA_FORWARD_SERVICE_NAME";
    private TpCaller tpCaller;

    // wls
    public TestServiceImpl()
    {}

    @Inject
    public TestServiceImpl(TpCaller tpCaller)
    {
        this.tpCaller = tpCaller;
    }

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
        String forwardName = Optional.ofNullable(getForwardNameIfAny()).orElseThrow(() -> new ForwardDefinitionMissingException("env var JAVA_FORWARD_SERVICE_NAME undefined, forward will not work ( does not know where to forward to)"));
        CasualBuffer returnBuffer = tpCaller.makeTpCall(forwardName, buffer.getBuffer(), Flag.of(AtmiFlags.NOFLAG));
        return InboundResponse.createBuilder().buffer( returnBuffer).build();
    }

    private String getForwardNameIfAny()
    {
        return System.getenv(JAVA_FORWARD_ENV_NAME);
    }
}
