package se.kodarkatten.casual.jca.inbound.handler.fielded;

import se.kodarkatten.casual.api.services.CasualService;

public interface SimpleService
{
    @CasualService(name="TestEcho", jndiName = "se.kodarkatten.casual.test.Service" )
    SimpleObject echo( SimpleObject message);
}
