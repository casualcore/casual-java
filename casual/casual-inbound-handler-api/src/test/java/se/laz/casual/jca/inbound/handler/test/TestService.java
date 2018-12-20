package se.laz.casual.jca.inbound.handler.test;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.laz.casual.api.service.CasualService;
import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;

public interface TestService
{
    @CasualService(name="test1")
    CasualBuffer echo( CasualBuffer buffer );

    @CasualService(name="test2")
    InboundResponse echo(InboundRequest request );

    @CasualService(name="test3" )
    String echo( String object );

    @CasualService(name="test4")
    FieldedTypeBuffer echo(FieldedTypeBuffer buffer );

}
