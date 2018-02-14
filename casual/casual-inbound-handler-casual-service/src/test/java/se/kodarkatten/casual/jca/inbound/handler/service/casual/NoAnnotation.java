package se.kodarkatten.casual.jca.inbound.handler.service.casual;

import se.kodarkatten.casual.api.services.CasualService;

public class NoAnnotation implements SomeInterface
{
    @CasualService(name="someMethod", jndiName = "" )
    @Override
    public void someMethod()
    {

    }
}
