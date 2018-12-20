/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer.fielded;

import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.laz.casual.api.service.CasualService;
import se.laz.casual.api.service.CasualServiceJndiName;
import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;

@CasualServiceJndiName("se.laz.casual.test.Service")
public interface SimpleService
{
    @CasualService(name="TestEcho" )
    SimpleObject echo( SimpleObject message);

    @CasualService(name="TestBuffer" )
    InboundResponse echoBuffer(InboundRequest message );

    @CasualService(name="TestFieldedBuffer" )
    FieldedTypeBuffer echoFieldedBuffer( FieldedTypeBuffer message );


}
