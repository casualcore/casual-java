package se.laz.casual.jca.inbound.handler.service.javaee.extension;

import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.buffer.BufferHandler;
import se.laz.casual.jca.inbound.handler.service.extension.ServiceHandlerExtension;
import se.laz.casual.jca.inbound.handler.service.extension.ServiceHandlerExtensionContext;
import se.laz.casual.spi.Priority;

import jakarta.ejb.Remote;

public class TestExtension implements ServiceHandlerExtension
{
    private ServiceHandlerExtension mock;

    public ServiceHandlerExtension getMock()
    {
        return mock;
    }

    public void setMock( ServiceHandlerExtension mock )
    {
        this.mock = mock;
    }

    @Override
    public Priority getPriority()
    {
        return Priority.LEVEL_3;
    }

    @Override
    public boolean canHandle( String name )
    {
        return name.equals( Remote.class.getName() );
    }

    @Override
    public ServiceHandlerExtensionContext before( InboundRequest request, BufferHandler bufferHandler )
    {
        if( mock != null )
        {
            return mock.before( request, bufferHandler );
        }
        return ServiceHandlerExtension.super.before( request, bufferHandler );
    }

    @Override
    public Object[] convertRequestParams( ServiceHandlerExtensionContext context, Object[] params )
    {
        if( mock != null )
        {
            return mock.convertRequestParams( context, params );
        }
        return ServiceHandlerExtension.super.convertRequestParams( context, params );
    }

    @Override
    public void after( ServiceHandlerExtensionContext context )
    {
        if( mock != null )
        {
            mock.after( context );
        }
    }

    @Override
    public InboundResponse handleError( ServiceHandlerExtensionContext context, InboundRequest request,
                                        InboundResponse response, Throwable e )
    {
        if( mock != null )
        {
            return mock.handleError( context, request, response, e );
        }
        return ServiceHandlerExtension.super.handleError( context, request, response, e );
    }

    @Override
    public InboundResponse handleSuccess( ServiceHandlerExtensionContext context, InboundResponse response )
    {
        if( mock != null )
        {
            return mock.handleSuccess( context, response );
        }
        return ServiceHandlerExtension.super.handleSuccess( context, response );
    }
}
