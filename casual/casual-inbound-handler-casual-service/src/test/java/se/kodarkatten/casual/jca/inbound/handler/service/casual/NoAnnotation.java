package se.kodarkatten.casual.jca.inbound.handler.service.casual;

import se.kodarkatten.casual.api.service.CasualService;

public class NoAnnotation implements SomeInterface
{
    @CasualService(name="someMethod" )
    @Override
    public void someMethod()
    {

    }
}
