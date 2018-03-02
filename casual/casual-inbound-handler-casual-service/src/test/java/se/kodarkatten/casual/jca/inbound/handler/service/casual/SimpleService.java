package se.kodarkatten.casual.jca.inbound.handler.service.casual;

import se.kodarkatten.casual.api.service.CasualService;
import se.kodarkatten.casual.api.service.CasualServiceJndiName;

@CasualServiceJndiName("se.kodarkatten.casual.test.Service")
public interface SimpleService
{
    @CasualService(name="TestEcho", category = "mycategory" )
    SimpleObject echo( SimpleObject message);
}
