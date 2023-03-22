package se.laz.casual.jca.inbound.handler.service.casual.extension;

import se.laz.casual.api.service.CasualService;
import se.laz.casual.jca.inbound.handler.service.extension.ServiceHandlerExtension;

public class DefaultCasualServiceHandlerExtension implements ServiceHandlerExtension
{
    @Override
    public boolean canHandle( String name )
    {
        return name.equals( CasualService.class.getName() );
    }
}
