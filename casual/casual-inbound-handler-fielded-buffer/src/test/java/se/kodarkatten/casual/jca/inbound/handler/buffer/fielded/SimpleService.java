package se.kodarkatten.casual.jca.inbound.handler.buffer.fielded;

import se.kodarkatten.casual.api.service.CasualService;
import se.kodarkatten.casual.api.service.CasualServiceJndiName;

@CasualServiceJndiName("se.kodarkatten.casual.test.Service")
public interface SimpleService
{
    @CasualService(name="TestEcho" )
    SimpleObject echo( SimpleObject message);
}
